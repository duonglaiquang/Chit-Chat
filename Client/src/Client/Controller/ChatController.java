package Client.Controller;

import Client.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ChatController {
  @FXML private ScrollPane chatscroll;
  @FXML private VBox chatbox;
  @FXML private TextField message;
  @FXML private Label topLabel;
  @FXML private TextField property;

  public void submit() throws FileNotFoundException {
    String str = message.getText();
    try {
      Main.client.oos.writeObject(str);
    } catch (IOException e) {
      e.printStackTrace();
    }
    message.clear();
    addMessage(str, false);
  }

  public void showSystemMessage(String msg){
    Label label = new Label(msg);
    label.getStyleClass().add("system-grey");
    label.getStylesheets().add(getClass().getResource("../Assets/css/chatBox.css").toExternalForm());
    Button rematchBtn = new Button("Rematch");
    rematchBtn.setOnAction(event -> {
      RootController rc = new RootController();
      try {
        rc.matched();
      } catch (IOException e) {
        e.printStackTrace();
      }
    });

    Button backBtn = new Button("Back");
    backBtn.setOnAction(event -> {
      RootController rc = new RootController();
      try {
        rc.changeScene("root");
      } catch (IOException e) {
        e.printStackTrace();
      }
    });

    HBox hBox = new HBox(15, rematchBtn, backBtn);
    hBox.setAlignment(Pos.TOP_CENTER);
    Platform.runLater(()->{
      chatbox.getChildren().add(label);
      chatbox.getChildren().add(hBox);
      chatbox.setSpacing(10);
      chatscroll.vvalueProperty().bind(chatbox.heightProperty());
      chatscroll.setFitToWidth(true);
    });
  }

  public void addMessage(String msg, Boolean left) throws FileNotFoundException {
    Label label = new Label(msg);
    label.getStylesheets().add(getClass().getResource("../Assets/css/chatBox.css").toExternalForm());
    label.getStyleClass().add("chat-bubble");

    FileInputStream input = new FileInputStream("src/Client/Assets/images/human.png");
    Image image = new Image(input);
    ImageView imageView = new ImageView(image);
    imageView.setFitHeight(25);
    imageView.setFitWidth(25);
    HBox hBox;
    if(left){
      hBox=new HBox(imageView, label);
      label.getStyleClass().add("receive");
      hBox.setAlignment(Pos.CENTER_LEFT);
      HBox.setMargin(imageView, new Insets(0, 0, 0, 10));
    } else {
      hBox=new HBox(label);
      label.getStyleClass().add("send");
      hBox.setAlignment(Pos.CENTER_RIGHT);
      HBox.setMargin(label, new Insets(0, 10, 0, 0));
    }
    Platform.runLater(()->{
      chatbox.getChildren().add(hBox);
      chatbox.setSpacing(10);
      chatscroll.vvalueProperty().bind(chatbox.heightProperty());
      chatscroll.setFitToWidth(true);
    });
  }
}
