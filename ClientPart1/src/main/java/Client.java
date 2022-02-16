import com.beust.jcommander.JCommander;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {

  private static final Integer DAY_LENGTH = 420;

  public static void main(String[] args) throws InterruptedException {
    Args options = new Args();
    JCommander.newBuilder().addObject(options).build().parse(args);

    int t = options.getNumThreads(),
        s = options.getNumSkiers(),
        l = options.getNumLifts(),
        r = options.getNumRuns();

    InetSocketAddress addr = options.getAddress();

    int n = t / 4;
    CountDownLatch latch = new CountDownLatch((int) Math.ceil(n * 0.2));

    System.out.println((int) Math.ceil(n * 0.2));
    ExecutorService pool = Executors.newFixedThreadPool(n);

    System.out.println(n);

    for (int i = 0; i < n; i++) {
      int startId = (s / n) * i + 1;
      int endId = i == n - 1 ? s : (s / n) * (i + 1);

      pool.execute(new WorkerRunnable(startId, endId, 1, 90, latch));
    }

    latch.await();
    System.out.println("20% of Threads completed!!!");
    pool.shutdown();
  }
}
