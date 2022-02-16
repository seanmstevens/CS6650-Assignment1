import com.beust.jcommander.JCommander;

public class Client {

  private static final Integer DAY_LENGTH = 420;

  public static void main(String[] args) {
    Args options = new Args();
    JCommander.newBuilder()
        .addObject(options)
        .build()
        .parse(args);



  }

}
