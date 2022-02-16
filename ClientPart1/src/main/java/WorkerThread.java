public class WorkerThread extends Thread {

  private final Integer startId;
  private final Integer endId;
  private final Integer startTime;
  private final Integer endTime;

  public WorkerThread(Integer startId, Integer endId, Integer startTime, Integer endTime) {
    this.startId = startId;
    this.endId = endId;
    this.startTime = startTime;
    this.endTime = endTime;
  }
}
