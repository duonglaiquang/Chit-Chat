package ChitChat.Controller;

import ChitChat.Main;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class SearchingController {
  public AnchorPane searchingPane;

  public void cancel() throws IOException {
    Main.client.request("cancel");
  }

  public void matched() throws IOException {
    Main.rc.changeScene("solo");
  }
}
