import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.swagger.client.model.SkierVertical;
import io.swagger.client.model.SkierVerticalResorts;
import java.io.IOException;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "SkierServlet", value = "/SkierServlet")
public class SkiersServlet extends HttpServlet {

  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");
    String urlPath = req.getPathInfo();

    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("Invalid path");
      return;
    }

    String[] urlParts = urlPath.split("/");

    if (!isUrlValid(urlPath)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("Invalid path or parameters supplied");
    } else if (Endpoint.GET_LIFT_RIDES.pattern.matcher(urlPath).matches()) {
      if (Integer.parseInt(urlParts[5]) < 0 || Integer.parseInt(urlParts[5]) > 365) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.getWriter().write("Invalid day value");
        return;
      }

      res.setStatus(HttpServletResponse.SC_OK);
      res.getWriter().write(34507);
    } else if (req.getParameter("resort") == null) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("Missing parameter: 'resort'");
    } else {
      res.setStatus(HttpServletResponse.SC_OK);

      String json =
          gson.toJson(
              new SkierVertical()
                  .addResortsItem(new SkierVerticalResorts().seasonID("2016").totalVert(855))
                  .addResortsItem(new SkierVerticalResorts().seasonID("2017").totalVert(777))
                  .addResortsItem(new SkierVerticalResorts().seasonID("2020").totalVert(900)));
      res.getWriter().write(json);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");
    String urlPath = req.getPathInfo();

    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("Invalid path");
      return;
    }

    JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);

    for (String param : new String[] {"time", "liftID"}) {
      if (body.get(param) == null) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.getWriter().write("Missing parameter: '" + param + "'");
        return;
      }
    }

    String[] urlParts = urlPath.split("/");

    if (!Endpoint.POST_LIFT_RIDES.pattern.matcher(urlPath).matches()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("Invalid path or parameters supplied");
    } else if (Integer.parseInt(urlParts[5]) < 0 || Integer.parseInt(urlParts[5]) > 365) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("Invalid day value");
    } else {
      res.setStatus(HttpServletResponse.SC_CREATED);
      res.getWriter().write("Lift ride created!");
    }
  }

  private boolean isUrlValid(String url) {
    for (Endpoint endpoint : Endpoint.values()) {
      Pattern pattern = endpoint.pattern;

      if (pattern.matcher(url).matches()) {
        return true;
      }
    }

    return false;
  }

  private enum Endpoint {
    GET_LIFT_RIDES(Pattern.compile("/\\d+/seasons/\\d+/days/\\d+/skiers/\\d+")),
    POST_LIFT_RIDES(Pattern.compile("/\\d+/seasons/\\d+/days/\\d+/skiers/\\d+")),
    GET_VERTICAL(Pattern.compile("/\\d+/vertical"));

    public final Pattern pattern;

    Endpoint(Pattern pattern) {
      this.pattern = pattern;
    }
  }
}
