import com.beust.jcommander.JCommander;
import java.net.InetSocketAddress;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {

  protected static final AtomicInteger NUM_SUCCESSFUL = new AtomicInteger(0);
  protected static final AtomicInteger NUM_FAILED = new AtomicInteger(0);
  private static Integer numSkiers;
  private static Integer numLifts;
  private static String serverUrl;
  private static ExecutorService pool;
  private static CyclicBarrier barrier;

  public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
    Args opts = new Args();
    JCommander.newBuilder().addObject(opts).build().parse(args);

    int t = opts.getNumThreads();
    int numRuns = opts.getNumRuns();
    numSkiers = opts.getNumSkiers();
    numLifts = opts.getNumLifts();

    InetSocketAddress address = opts.getAddress();
    serverUrl = "http://" + address.getHostName() + ":" + address.getPort() + "/Server_war";

    int phaseOneThreads = t / 4;
    int phaseThreeThreads = t / 10;
    int totalThreads = phaseOneThreads + t + phaseThreeThreads;

    pool = Executors.newFixedThreadPool(totalThreads);
    barrier = new CyclicBarrier(totalThreads);

    PhaseOptions p1Opts =
        new PhaseOptions(
            "Phase One",
            phaseOneThreads,
            0.2,
            1,
            90,
            (int) ((numRuns * 0.2) * (numSkiers / (phaseOneThreads))));

    PhaseOptions p2Opts =
        new PhaseOptions("Phase Two", t, 0.2, 91, 360, (int) ((numRuns * 0.6) * (numSkiers / t)));

    PhaseOptions p3Opts =
        new PhaseOptions("Phase Three", phaseThreeThreads, 1.0, 361, 420, (int) ((numRuns * 0.1)));

    long start = System.currentTimeMillis();
    executePhase(p1Opts);
    System.out.println(Thread.activeCount());
    executePhase(p2Opts);
    System.out.println(Thread.activeCount());
    executePhase(p3Opts);
    System.out.println(Thread.activeCount());

    barrier.await();
    pool.shutdown();
    long end = System.currentTimeMillis();

    System.out.println("Total successes: " + NUM_SUCCESSFUL);
    System.out.println("Total failures: " + NUM_FAILED);
    System.out.println("Time elapsed (sec): " + ((float) (end - start) / 1000));
    System.out.println(
        "Total throughput (reqs/sec): "
            + (NUM_SUCCESSFUL.get() + NUM_FAILED.get()) / ((float) (end - start) / 1000));
    System.out.println(Thread.activeCount());
  }

  public static void executePhase(PhaseOptions opts) throws InterruptedException {
    System.out.println("------ " + opts.getName().toUpperCase() + " INITIALIZED ------");
    int t = opts.getNumThreads();
    int s = numSkiers;

    CountDownLatch latch = new CountDownLatch((int) Math.ceil(t * opts.getThreshold()));

    for (int i = 0; i < t; i++) {
      int startId = (s / t) * i + 1;
      int endId = i == t - 1 ? s : (s / t) * (i + 1);

      pool.execute(
          new WorkerRunnable(
              startId,
              endId,
              opts.getStartTime(),
              opts.getEndTime(),
              serverUrl,
              opts.getNumReqs(),
              numLifts,
              latch,
              barrier));
    }

    latch.await();
    System.out.println(
        "------ "
            + opts.getName().toUpperCase()
            + ": "
            + ((int) (opts.getThreshold() * 100))
            + "% OF THREADS COMPLETE ------");
  }
}
