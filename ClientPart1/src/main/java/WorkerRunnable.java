import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public class WorkerRunnable implements Runnable {

  private static final Integer MAX_RETRIES = 5;

  private final Integer startId;
  private final Integer endId;
  private final Integer startTime;
  private final Integer endTime;
  private final String serverUrl;
  private final Integer numReqs;
  private final Integer numLifts;
  private final CountDownLatch latch;

  public WorkerRunnable(
      Integer startId,
      Integer endId,
      Integer startTime,
      Integer endTime,
      String serverUrl,
      Integer numReqs,
      Integer numLifts,
      CountDownLatch latch) {
    this.startId = startId;
    this.endId = endId;
    this.startTime = startTime;
    this.endTime = endTime;
    this.serverUrl = serverUrl;
    this.numReqs = numReqs;
    this.numLifts = numLifts;
    this.latch = latch;
  }

  @Override
  public void run() {
    SkiersApi apiInstance = new SkiersApi();
    apiInstance.getApiClient().setBasePath(serverUrl);

    for (int i = 0; i < numReqs; i++) {
      int id = ThreadLocalRandom.current().nextInt(startId, endId + 1);
      int time = ThreadLocalRandom.current().nextInt(startTime, endTime + 1);
      int liftId = ThreadLocalRandom.current().nextInt(1, numLifts + 1);
      int waitTime = ThreadLocalRandom.current().nextInt(1, 11);

      LiftRide ride = new LiftRide().time(time).liftID(liftId);

      boolean success = false;
      int numTries = 0;

      while (!success && numTries < MAX_RETRIES) {
        try {
          apiInstance.writeNewLiftRide(ride, 56, "2022", "200", id);
          Client.NUM_SUCCESSFUL.incrementAndGet();
          success = true;
        } catch (ApiException e) {
          // System.err.println("POST request failure: " + e.getMessage());
          Client.NUM_FAILED.incrementAndGet();
          try {
            Thread.sleep(getWaitTime(numTries++));
          } catch (InterruptedException ex) {
            ex.printStackTrace();
          }
        }
      }
    }

    latch.countDown();
  }

  private Integer getWaitTime(Integer n) {
    return 2 ^ n;
  }
}
