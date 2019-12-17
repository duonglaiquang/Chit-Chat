package Client.Controller;

import Client.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class RootController {

  @FXML private ComboBox<String> tagbox;
  @FXML private Label connection;

  public void init(){
    String[] tags = {"", "game", "book", "music", "boy", "girl"};
    tagbox.getItems().addAll(tags);
  }

  public void match() throws IOException, NullPointerException {
    if(Main.connected){
      changeScene("searching");
      Main.client.request("match");
    }
  }

  public void cancelMatch() throws IOException {
    Main.client.request("cancel");
  }

  public void createRoom() throws IOException {
    if(Main.connected)
      newStage("createRoomModal", "Create Room");
  }

  public void showRoom() throws IOException {
    if(Main.connected){
      Main.client.request("roomls");
    }
  }

  public void changeScene(String name) throws IOException {
    FXMLLoader fXMLLoader = new FXMLLoader();
    fXMLLoader.setLocation(getClass().getResource("../View/" + name + ".fxml"));
    Scene scene = new Scene(fXMLLoader.load(), 600, 400);
    Stage stage = Main.homeStage;
    if(name.equals("root")){
      RootController rc = fXMLLoader.getController();
      rc.updateConnectionStatus();
      Platform.runLater(rc::init);
    }
    if(name.equals("chatBox")){
      Main.cc = fXMLLoader.getController();
    }
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

  public void updateConnectionStatus() {
    Platform.runLater(()->{
      connection.setText("Connected to Server");
      connection.setStyle("-fx-text-fill: green;");
    });
  }
}
