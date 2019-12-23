package Client.Controller;

import Client.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ChatController {
  @FXML private Button leaveBtn;
  @FXML private Label tagLb;
  @FXML private ImageView attach;
  @FXML private Label title;
  @FXML private ScrollPane chatscroll;
  @FXML private VBox chatbox;
  @FXML private TextField message;

  public void init(String roomName) {
    message.setOnKeyPressed(event -> {
      if(event.getCode() == KeyCode.ENTER){
        try {
          submit();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });

    Platform.runLater(() -> {
      title.setText(roomName);
      attach.setImage(new Image(new File("src/Client/Assets/images/attach.png").toURI().toString()));
      leaveBtn.setOnAction(event -> {
        try {
          leave();
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    });
  }

  public void overrideBtn() {
    leaveBtn.setOnAction(null);
    leaveBtn.setOnAction(event -> {
      RootController rc = new RootController();
      try {
        rc.changeScene("root");
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  public void setTag(String tag) {
    Platform.runLater(() -> tagLb.setText(tag));
  }

  public void submit() throws IOException {
    String str;
    str = message.getText();
    message.clear();
    Main.client.oos.writeObject(str);
    addMessage(str, false, null);
  }

  public void showSystemMessage(String msg, Boolean button) {
    Label label = new Label(msg);
    label.getStyleClass().add("system-grey");
    label.getStylesheets().add(getClass().getResource("../Assets/css/chatBox.css").toExternalForm());
    HBox hBox = null;
    if (button) {
      Button rematchBtn = new Button("Rematch");
      rematchBtn.setOnAction(event -> {
        RootController rc = new RootController();
        String tag = Main.cc.tagLb.getText();
        try {
          rc.callMatch(tag);
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

      hBox = new HBox(15, rematchBtn, backBtn);
      hBox.setAlignment(Pos.TOP_CENTER);
    }
    HBox finalHBox = hBox;
    Platform.runLater(() -> {
      chatbox.getChildren().add(label);
      if (button) {
        chatbox.getChildren().add(finalHBox);
      }
      chatbox.setSpacing(10);
      chatscroll.vvalueProperty().bind(chatbox.heightProperty());
      chatscroll.setFitToWidth(true);
    });
  }

  public void addMessage(String msg, Boolean left, String color) throws FileNotFoundException {
    Label label = new Label(msg);
    label.getStylesheets().add(getClass().getResource("../Assets/css/chatBox.css").toExternalForm());
    label.getStyleClass().add("chat-bubble");

    FileInputStream input = new FileInputStream("src/Client/Assets/images/human.png");
    Image image = new Image(input);
    ImageView avatar = new ImageView(image);
    avatar.setFitHeight(25);
    avatar.setFitWidth(25);
    HBox hBox;
    if (left) {
      hBox = new HBox(avatar, label);
      if (color != null) {
        label.setStyle("-fx-background-color: " + color);
      } else {
        label.getStyleClass().add("receive");
      }
      hBox.setAlignment(Pos.CENTER_LEFT);
      HBox.setMargin(avatar, new Insets(0, 0, 0, 10));
    } else {
      hBox = new HBox(label);
      label.getStyleClass().add("send");
      hBox.setAlignment(Pos.CENTER_RIGHT);
      HBox.setMargin(label, new Insets(0, 10, 0, 0));
    }
    Platform.runLater(() -> {
      chatbox.getChildren().add(hBox);
      chatbox.setSpacing(10);
      chatscroll.vvalueProperty().bind(chatbox.heightProperty());
      chatscroll.setFitToWidth(true);
    });
  }

  public void leave() throws IOException {
    RootController rc = new RootController();
    rc.newStage("confirm", "Confirm", null);
  }

  public void attachFile() throws IOException {
    FileChooser fileChooser = new FileChooser();
    File selectedFile = fileChooser.showOpenDialog(Main.homeStage);
    Image img = new Image(new FileInputStream(selectedFile.getPath()));
    Main.client.sendImage(img);
    Platform.runLater(() -> {
      try {
        Main.cc.addImage(img, false);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    });
  }

  public void addImage(Image img, Boolean left) throws FileNotFoundException {
    ImageView imageView = new ImageView(img);
    imageView.setFitHeight(180);
    imageView.setFitWidth(220);

    imageView.setOnMouseClicked(mouseEvent -> {
      FXMLLoader fXMLLoader = new FXMLLoader();
      fXMLLoader.setLocation(getClass().getResource("../View/enlargeImage.fxml"));
      Scene scene = null;
      try {
        scene = new Scene(fXMLLoader.load());
      } catch (IOException e) {
        e.printStackTrace();
      }
      ImageController ic = fXMLLoader.getController();
      Scene finalScene = scene;
      Platform.runLater(() -> {
        Stage stage = new Stage();
        stage.setScene(finalScene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("ImageViewer");
        ic.showImage(img);
        stage.show();
        Main.currentStage = stage;
      });
    });

    Image image = new Image(new FileInputStream("src/Client/Assets/images/human.png"));
    ImageView avatar = new ImageView(image);
    avatar.setFitHeight(25);
    avatar.setFitWidth(25);

    HBox hBox;
    if (left) {
      hBox = new HBox(avatar, imageView);
      hBox.setAlignment(Pos.CENTER_LEFT);
      HBox.setMargin(avatar, new Insets(0, 0, 0, 10));
    } else {
      hBox = new HBox(imageView);
      hBox.setAlignment(Pos.CENTER_RIGHT);
      HBox.setMargin(imageView, new Insets(0, 10, 0, 0));
    }
    Platform.runLater(() -> {
      chatbox.getChildren().add(hBox);
      chatbox.setSpacing(10);
      chatscroll.vvalueProperty().bind(chatbox.heightProperty());
      chatscroll.setFitToWidth(true);
    });
  }
}
