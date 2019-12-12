package Client.Controller;

import Client.Main;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class RootController {

  public void match() throws IOException, NullPointerException {
    changeScene("searching");
    Main.client.request("match");
  }

  public void cancelMatch() throws IOException {
    Main.client.request("cancel");
  }

  public void matched() throws IOException {
    FXMLLoader fXMLLoader = new FXMLLoader();
    fXMLLoader.setLocation(getClass().getResource("../View/chatBox.fxml"));
    Scene scene = new Scene(fXMLLoader.load(), 600, 400);
    Main.cc = fXMLLoader.getController();
    Stage stage = Main.homeStage;
    Platform.runLater(() -> stage.setScene(scene));
  }

  public void createRoom() throws IOException {
    newStage("createRoomModal", "Create Room");
  }

  public void showRoom() throws IOException {
    Main.client.request("roomls");
    newStage("roomList", "Room List");
  }
//
  public void changeScene(String name) throws IOException {
    FXMLLoader fXMLLoader = new FXMLLoader();
    fXMLLoader.setLocation(getClass().getResource("../View/" + name + ".fxml"));
    Scene scene = new Scene(fXMLLoader.load(), 600, 400);
    Stage stage = Main.homeStage;
    Platform.runLater(() -> stage.setScene(scene));
  }

  public void newStage(String name, String title) throws IOException {
    FXMLLoader fXMLLoader = new FXMLLoader();
    fXMLLoader.setLocation(getClass().getResource("../View/" + name + ".fxml"));
    Scene scene = new Scene(fXMLLoader.load());
    Platform.runLater(() -> {
      Stage stage = new Stage();
      stage.setScene(scene);
      stage.initModality(Modality.APPLICATION_MODAL);
      stage.setTitle(title);
      stage.show();
      Main.currentStage = stage;
    });
  }
}
