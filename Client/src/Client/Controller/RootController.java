package Client.Controller;

import Client.Main;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
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
    changeScene("chatBox");
  }

  public void createRoom() throws IOException {
    newStage("createRoomModal", "Create Room", null);
  }

  public void showRoom() throws IOException {
    Main.client.request("roomls");
    newStage("roomList", "Room List", null);
  }

  public void changeScene(String name) throws IOException {
    Parent pane = FXMLLoader.load(getClass().getResource("../View/" + name + ".fxml"));
    Stage stage = Main.homeStage;
    Scene scene = new Scene(pane, 600, 400);
    Platform.runLater(() -> stage.setScene(scene));
  }

  public void newStage(String name, String title, String data) throws IOException {
    FXMLLoader fXMLLoader = new FXMLLoader();
    fXMLLoader.setLocation(getClass().getResource("../View/" + name + ".fxml"));
    Scene scene = new Scene(fXMLLoader.load());
    if (data != null) {
      ChatController controller = fXMLLoader.getController();
      controller.initData(data);
    }
    if (new File("../Assets/css/" + name + ".css").isFile()) {
      scene.getStylesheets().add("../Assets/css/" + name + ".css");
    }

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
