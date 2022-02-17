import com.beust.jcommander.JCommander;
import java.net.InetSocketAddress;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {

  protected static final AtomicInteger NUM_SUCCESSFUL = new AtomicInteger(0);
  protected static final AtomicInteger NUM_FAILED = new AtomicInteger(0);
  private static final Integer DAY_LENGTH = 420;
  private static ExecutorService pool;
  private static CyclicBarrier barrier;

  public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
    Args opts = new Args();
    JCommander.newBuilder().addObject(opts).build().parse(args);

    int t = opts.getNumThreads(),
        s = opts.getNumSkiers(),
        l = opts.getNumLifts(),
        r = opts.getNumRuns();

    InetSocketAddress address = opts.getAddress();
    String serverUrl = "http://" + address.getHostName() + ":" + address.getPort() + "/Server_war";

    int phaseOneThreads = t / 4;
    int phaseThreeThreads = t / 10;

    pool = Executors.newFixedThreadPool(phaseOneThreads + t + phaseThreeThreads);
    barrier = new CyclicBarrier(phaseOneThreads + t + phaseThreeThreads);

    PhaseOptions p1Opts =
        new PhaseOptions(phaseOneThreads, s, l, r, serverUrl, 0.2, 1, 90, (int) ((r * 0.2) * (s / (phaseOneThreads))));

    PhaseOptions p2Opts =
        new PhaseOptions(t, s, l, r, serverUrl, 0.2, 91, 360, (int) ((r * 0.6) * (s / t)));

    PhaseOptions p3Opts =
        new PhaseOptions(phaseThreeThreads, s, l, r, serverUrl, 1.0, 361, 420, (int) ((r * 0.1)));

    long start = System.currentTimeMillis();
    executePhase(p1Opts);
    System.out.println("------ PHASE ONE COMPLETE ------");

    executePhase(p2Opts);
    System.out.println("------ PHASE TWO COMPLETE ------");

    executePhase(p3Opts);
    System.out.println("------ PHASE THREE COMPLETE ------");

    barrier.await();
    long end = System.currentTimeMillis();

    System.out.println("Total successes: " + NUM_SUCCESSFUL);
    System.out.println("Total failures: " + NUM_FAILED);
    System.out.println("Time elapsed (sec): " + ((float) (end - start) / 1000));
  }

  public static void executePhase(PhaseOptions opts) throws InterruptedException {
    int t = opts.getNumThreads();
    int s = opts.getNumSkiers();

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
              opts.getServerUrl(),
              opts.getNumReqs(),
              opts.getNumLifts(),
              latch,
              barrier));
    }

    latch.await();
  }
}
