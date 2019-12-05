package ChitChat.Controller;

import ChitChat.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class RootController {
  public Button matchBtn;
  public AnchorPane rootPane;

  public void match() throws IOException, NullPointerException {
//    AnchorPane pane = FXMLLoader.load(getClass().getResource("../View/searching.fxml"));
//    rootPane.getChildren().setAll(pane);
//    Main.client.requestMatch();

    Parent searchPane = FXMLLoader.load(getClass().getResource("../View/searching.fxml"));
    Stage stage = Main.homestage;
    Scene scene = new Scene(searchPane, 600, 400);
    stage.setScene(scene);
    Main.client.requestMatch();
  }
}
