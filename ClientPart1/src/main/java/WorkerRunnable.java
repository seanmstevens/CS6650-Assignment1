import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkerRunnable implements Runnable {

  private static final Integer MAX_RETRIES = 5;

  private final Integer startId;
  private final Integer endId;
  private final Integer startTime;
  private final Integer endTime;
  private final CountDownLatch latch;

  public WorkerRunnable(
      Integer startId, Integer endId, Integer startTime, Integer endTime, CountDownLatch latch) {
    this.startId = startId;
    this.endId = endId;
    this.startTime = startTime;
    this.endTime = endTime;
    this.latch = latch;
  }

  @Override
  public void run() {
    SkiersApi apiInstance = new SkiersApi();
    apiInstance.getApiClient().setBasePath("http://54.245.60.22:8080/Server_war");

    for (int i = startId; i <= endId; i++) {
      LiftRide ride = new LiftRide().time(50).liftID(20);

      try {
        apiInstance.writeNewLiftRide(ride, 56, "2022", "200", 4444);
      } catch (ApiException e) {
        e.printStackTrace();
      }
    }

    latch.countDown();
    System.out.println("Thread exiting...");
  }
}
