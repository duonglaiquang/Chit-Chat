package Client.Controller;

import Client.Main;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class RoomController {
  public Button cancelBtn;
  public TextField name;
  public TextArea description;

  public void cancel() {
    Stage stage = (Stage) cancelBtn.getScene().getWindow();
    stage.close();
  }

  public void createRoom() throws IOException {
    Main.client.request("createRoom#" + name.getText() + "#" + description.getText());
  }

  public void roomCreated(String id, String rName, String rDescription) throws IOException {
    Stage stage = Main.currentStage;
    Platform.runLater(stage::close);
    String hidden = id + "#" + rName + "#" + rDescription;

    FXMLLoader loader = new FXMLLoader(getClass().getResource("View/root.fxml"));
    RootController rc = loader.getController();
    rc.newStage("chatBox", "Chat Room : " + rName, hidden);
  }

  public void prev_page(MouseEvent mouseEvent) {
  }

  public void next_page(MouseEvent mouseEvent) {
  }
}
