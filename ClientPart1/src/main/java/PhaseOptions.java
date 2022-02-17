public class PhaseOptions {

  private final Integer numThreads;
  private final Integer numSkiers;
  private final Integer numLifts;
  private final Integer numRuns;
  private final String serverUrl;
  private final Double threshold;
  private final Integer startTime;
  private final Integer endTime;
  private final Integer numReqs;

  public PhaseOptions(
      Integer numThreads,
      Integer numSkiers,
      Integer numLifts,
      Integer numRuns,
      String serverUrl,
      Double threshold,
      Integer startTime,
      Integer endTime,
      Integer numReqs) {
    this.numThreads = numThreads;
    this.numSkiers = numSkiers;
    this.numLifts = numLifts;
    this.numRuns = numRuns;
    this.serverUrl = serverUrl;
    this.threshold = threshold;
    this.startTime = startTime;
    this.endTime = endTime;
    this.numReqs = numReqs;
  }

  public Integer getNumThreads() {
    return numThreads;
  }

  public Integer getNumSkiers() {
    return numSkiers;
  }

  public Integer getNumLifts() {
    return numLifts;
  }

  public Integer getNumRuns() {
    return numRuns;
  }

  public String getServerUrl() {
    return serverUrl;
  }

  public Double getThreshold() {
    return threshold;
  }

  public Integer getStartTime() {
    return startTime;
  }

  public Integer getEndTime() {
    return endTime;
  }

  public Integer getNumReqs() {
    return numReqs;
  }
}
