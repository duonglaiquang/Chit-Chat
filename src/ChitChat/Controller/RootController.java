package ChitChat.Controller;

import ChitChat.Main;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class RootController {
  public Button matchBtn;
  public AnchorPane rootPane;

  public void match() throws IOException, NullPointerException {
    changeScene("searching");
    Main.client.request("match");
  }

  public void createRoom() throws IOException {
    FXMLLoader fXMLLoader = new FXMLLoader();
    fXMLLoader.setLocation(getClass().getResource("../View/createRoomModal.fxml"));
    Stage stage = new Stage();
    Scene scene = new Scene(fXMLLoader.load());
    stage.setScene(scene);
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.setTitle("Create Room");
    stage.show();
  }

  public void changeScene(String name) throws IOException {
    Parent pane = FXMLLoader.load(getClass().getResource("../View/"+name+".fxml"));
    Stage stage = Main.homestage;
    Scene scene = new Scene(pane, 600, 400);
    Platform.runLater(() ->stage.setScene(scene));
  }
}
