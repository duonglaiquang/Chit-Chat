package Client.Controller;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class ChatController {
  public Label topLabel;
  public AnchorPane chatBox;
  public TextField property;

  public void initData(String value) {
    property.setText(value);
    String[] arrValue = value.split("#");
    topLabel.setText("Welcome to " + arrValue[1] + ". Please be polite to each other!");
  }
}
