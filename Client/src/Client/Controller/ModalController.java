package Client.Controller;

import Client.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class ModalController {
  @FXML private Label warningLb;
  @FXML private Button cancelBtn;
  @FXML private TextField name;
  @FXML private TextArea description;

  public void init(String warning) {
    Platform.runLater(()->warningLb.setText(warning));
  }

  public void cancel() {
    Stage stage = (Stage) cancelBtn.getScene().getWindow();
    stage.close();
  }

  public void createRoom() throws IOException {
    Main.client.request("createRoom#" + name.getText() + "#" + description.getText());
  }

  public void roomCreated() throws IOException {
    Stage stage = Main.currentStage;
    Platform.runLater(stage::close);
    FXMLLoader loader = new FXMLLoader(getClass().getResource("../View/root.fxml"));;
    loader.load();
    RootController rc = loader.getController();
    rc.changeScene("chatBox");
  }

  public void leaveRoom() throws IOException {
    Main.client.request("leaveRoom");
    Stage stage = Main.currentStage;
    Platform.runLater(stage::close);
  }
}
