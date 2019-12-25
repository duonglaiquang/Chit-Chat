package Client.Controller;

import Client.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class RootController {

  @FXML private Button cancelBtn;
  @FXML private Label onlineCount;
  @FXML private ImageView nyan;
  @FXML private ComboBox<String> tagbox;
  @FXML private Label connection;

  public void init(){
    String[] tags = {"", "game", "movie", "book", "music", "boy", "girl"};
    tagbox.getItems().addAll(tags);
    updateConnectionStatus();
    Main.client.updateCount();
  }

  public void updateUserCount(String count){
    if(onlineCount != null)
      Platform.runLater(()->onlineCount.setText(count + " Client Online"));
  }

  public void nyan(){
    nyan.setImage(new Image(new File("src/Client/Assets/images/nyan-hd.gif").toURI().toString()));
  }

  public void match() throws IOException {
    callMatch(null);
  }

  public void callMatch(String oldTag) throws IOException, NullPointerException {
    if(Main.connected){
      String tag;
      if(tagbox != null){
        tag = tagbox.getValue();
      } else {
        tag = oldTag;
      }
      changeScene("searching");
      Main.client.request("match#"+tag);
    }
  }

  public void cancelMatch() throws IOException {
    cancelBtn.setDisable(true);
    Main.client.request("cancel");
  }

  public void createRoom() throws IOException {
    if(Main.connected)
      newStage("createRoomModal", "Create Room", null);
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
      Main.rc = rc;
      Platform.runLater(rc::init);
    }
    if(name.equals("chatBox")){
      Main.cc = fXMLLoader.getController();
      scene.getStylesheets().add(getClass().getResource("../Assets/css/chatBox.css").toExternalForm());
    }
    if(name.equals("searching")){
      RootController rc = fXMLLoader.getController();
      Platform.runLater(rc::nyan);
    }
    Main.currentScene = name;
    Platform.runLater(() -> stage.setScene(scene));
  }

  public void newStage(String name, String title, String message) throws IOException {
    FXMLLoader fXMLLoader = new FXMLLoader();
    fXMLLoader.setLocation(getClass().getResource("../View/" + name + ".fxml"));
    Scene scene = new Scene(fXMLLoader.load());
    if (name.equals("warning")) {
      ModalController mc = fXMLLoader.getController();
      mc.init(message);
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

  public void updateConnectionStatus() {
    Platform.runLater(()->{
      if(Main.connected){
        connection.setText("Connected to Server");
        connection.setStyle("-fx-text-fill: green;");
      } else {
        connection.setText("Unable to connect to Server");
        connection.setStyle("-fx-text-fill: red;");
      }
    });
  }
}
