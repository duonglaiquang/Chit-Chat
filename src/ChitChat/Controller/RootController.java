package ChitChat.Controller;

import ChitChat.Client.Client;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class RootController {
  public Button cancelBtn;
  public Button matchBtn;
  public AnchorPane rootPane;
  public AnchorPane searchingPane;

  public void match(MouseEvent mouseEvent) throws IOException {
    Client client = new Client();
    client.start();
    AnchorPane pane = FXMLLoader.load(getClass().getResource("../View/searching.fxml"));
    rootPane.getChildren().setAll(pane);
  }

  public void cancel(MouseEvent mouseEvent) {
  }
}
