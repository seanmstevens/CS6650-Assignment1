import io.swagger.client.ApiException;
import io.swagger.client.api.ResortsApi;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import io.swagger.client.model.ResortIDSeasonsBody;
import io.swagger.client.model.SkierVertical;
import java.util.List;

public class ApiTest {

  public static void main(String[] args) {

    ResortsApi apiInstance = new ResortsApi();
    apiInstance.getApiClient().setBasePath("http://localhost:8080/Server_war_exploded");

    ResortIDSeasonsBody body = new ResortIDSeasonsBody().year("2022");
    Integer resortId = 56;

    try {
      SkiersApi api = new SkiersApi();
      SkierVertical vertical =
          api.getSkierResortTotals(
              555, List.of("res1", "res2", "res3"), List.of("2016", "2017", "2020"));
      LiftRide ride = new LiftRide().liftID(44).time(300);

      api.writeNewLiftRide(ride, resortId, "2022", "365", 4444);
      System.out.println(vertical);
    } catch (ApiException e) {
      System.err.println("Exception when calling ResortsApi#addSeason");
      e.printStackTrace();
    }
  }
}
