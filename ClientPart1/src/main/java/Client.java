import com.beust.jcommander.JCommander;
import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {

  private static final Integer DAY_LENGTH = 420;
  private static final Integer MAX_RETRIES = 5;
  private static final AtomicInteger numSuccessful = new AtomicInteger(0);
  private static final AtomicInteger numFailed = new AtomicInteger(0);

  public static void main(String[] args) throws InterruptedException {
    Args opts = new Args();
    JCommander.newBuilder().addObject(opts).build().parse(args);

    int t = opts.getNumThreads(),
        s = opts.getNumSkiers(),
        l = opts.getNumLifts(),
        r = opts.getNumRuns();

    InetSocketAddress addr = opts.getAddress();
    String serverUrl = "http://" + addr.getHostName() + ":" + addr.getPort() + "/Server_war";

    long start = System.currentTimeMillis();
    executePhaseOne(t, s, l, r, serverUrl);
    long end = System.currentTimeMillis();

    System.out.println("------ PHASE ONE COMPLETE ------");
    System.out.println("Time elapsed (ms): " + (end - start));
    System.out.println("Total successes: " + numSuccessful);
    System.out.println("Total failures: " + numFailed);
  }

  public static void executePhaseOne(
      Integer numThreads, Integer numSkiers, Integer numLifts, Integer numRuns, String serverUrl)
      throws InterruptedException {
    final double threshold = 0.2;
    final int startTime = 1;
    final int endTime = 90;

    int n = numThreads / 4;
    CountDownLatch latch = new CountDownLatch((int) Math.ceil(n));
    ExecutorService pool = Executors.newFixedThreadPool(n);

    for (int i = 0; i < n; i++) {
      int startId = (numSkiers / n) * i + 1;
      int endId = i == n - 1 ? numSkiers : (numSkiers / n) * (i + 1);

      pool.execute(
          () -> {
            SkiersApi apiInstance = new SkiersApi();
            apiInstance.getApiClient().setBasePath(serverUrl);

            int[] ids =
                ThreadLocalRandom.current()
                    .ints(startId, endId + 1)
                    .distinct()
                    .limit(endId - startId + 1)
                    .toArray();

            for (int id : ids) {
              int time = ThreadLocalRandom.current().nextInt(startTime, endTime + 1);
              int liftId = ThreadLocalRandom.current().nextInt(1, numLifts + 1);
              int waitTime = ThreadLocalRandom.current().nextInt(1, 11);

              LiftRide ride = new LiftRide().time(time).liftID(liftId);

              boolean success = false;
              int numTries = 0;

              while (!success && numTries < MAX_RETRIES) {
                try {
                  apiInstance.writeNewLiftRide(ride, 56, "2022", "200", id);
                  numSuccessful.incrementAndGet();
                  success = true;
                } catch (ApiException e) {
                  numFailed.incrementAndGet();
                  try {
                    Thread.sleep(2 ^ numTries);
                  } catch (InterruptedException ex) {
                    ex.printStackTrace();
                  }

                  numTries++;
                }
              }
            }

            latch.countDown();
            System.out.println("Thread exiting... ");
          });
    }

    latch.await();
  }
}
