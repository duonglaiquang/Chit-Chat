package ChitChat.Controller;

import javafx.scene.control.Button;
import javafx.stage.Stage;

public class CreateRoomModal {
  public Button cancelBtn;

  public void cancel() {
    Stage stage = (Stage) cancelBtn.getScene().getWindow();
    stage.close();
  }
}
