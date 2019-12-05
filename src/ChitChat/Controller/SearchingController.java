package ChitChat.Controller;

import ChitChat.Main;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class SearchingController {
  public AnchorPane searchingPane;

  public void cancel() {
  }

  public void matched() throws IOException {
    Parent chatPane = FXMLLoader.load(getClass().getResource("../View/solo.fxml"));
    Stage stage = Main.homestage;
    Scene scene = new Scene(chatPane, 600, 400);
    Platform.runLater(() ->stage.setScene(scene));

//    AnchorPane pane = FXMLLoader.load(getClass().getResource("../View/solo.fxml"));
//    System.out.println(searchingPane);
//    System.out.println(pane);
//    searchingPane.getChildren().setAll(pane);

  }
}
