package Client.Controller;

import Client.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
  @FXML private Label colorLb;
  @FXML private Button leaveBtn;
  @FXML private Label tagLb;
  @FXML private ImageView attach;
  @FXML private Label title;
  @FXML private ScrollPane chatscroll;
  @FXML private VBox chatbox;
  @FXML private TextField message;

  public void init(String roomName, String color) {
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
      colorLb.setText(color);
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
    addMessage(str, false, colorLb.getText());
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
        String tag = tagLb.getText();
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

  public void addMessage(String msg, Boolean left, String color) {
    Label label = new Label(msg);
    VBox msgVbox;
    Label senderName;
    HBox hbox;

    if (color != null) {
      if (left) {
        senderName = new Label(color);
        msgVbox = new VBox(5, senderName, label);
        msgVbox.setAlignment(Pos.CENTER_LEFT);
        hbox = new HBox(msgVbox);
        hbox.setAlignment(Pos.CENTER_LEFT);
      } else {
        senderName = new Label("You");
        msgVbox = new VBox(5, senderName, label);
        msgVbox.setAlignment(Pos.CENTER_RIGHT);
        hbox = new HBox(msgVbox);
        hbox.setAlignment(Pos.CENTER_RIGHT);
      }
      senderName.setStyle("-fx-font-weight: bold;" + "-fx-font-family: Arial;" +  "-fx-font-size: 14;" + "-fx-text-fill: " + color + ";");
      msgVbox.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;"
          + "-fx-border-width: 2;" + "-fx-border-insets: 5;"
          + "-fx-border-radius: 5;" + "-fx-border-color: " + color +";");
    } else {
      if (left) {
        senderName = new Label("Stranger");
        msgVbox = new VBox(5, senderName, label);
        msgVbox.setAlignment(Pos.CENTER_LEFT);
        hbox = new HBox(msgVbox);
        hbox.setAlignment(Pos.CENTER_LEFT);
      } else {
        senderName = new Label("You");
        msgVbox = new VBox(5, senderName, label);
        msgVbox.setAlignment(Pos.CENTER_RIGHT);
        hbox = new HBox(msgVbox);
        hbox.setAlignment(Pos.CENTER_RIGHT);
      }
      senderName.setStyle("-fx-font-weight: bold;" + "-fx-font-family: Arial;" +  "-fx-font-size: 14;");
      msgVbox.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;"
          + "-fx-border-width: 2;" + "-fx-border-insets: 5;"
          + "-fx-border-radius: 5;" + "-fx-border-color: lightblue;");
    }
    Platform.runLater(() -> {
      chatbox.getChildren().add(hbox);
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
    Main.client.sendImage(img, colorLb.getText());
    Platform.runLater(() -> {
      try {
        addImage(img, false, colorLb.getText());
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    });
  }

  public void addImage(Image img, Boolean left, String color) throws FileNotFoundException {
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

    VBox msgVbox;
    Label senderName;
    HBox hbox;

    if (color != null) {
      if (left) {
        senderName = new Label(color);
        msgVbox = new VBox(5, senderName, imageView);
        msgVbox.setAlignment(Pos.CENTER_LEFT);
        hbox = new HBox(msgVbox);
        hbox.setAlignment(Pos.CENTER_LEFT);
      } else {
        senderName = new Label("You");
        msgVbox = new VBox(5, senderName, imageView);
        msgVbox.setAlignment(Pos.CENTER_RIGHT);
        hbox = new HBox(msgVbox);
        hbox.setAlignment(Pos.CENTER_RIGHT);
      }
      senderName.setStyle("-fx-font-weight: bold;" + "-fx-font-family: Arial;" +  "-fx-font-size: 14;" + "-fx-text-fill: " + color + ";");
      msgVbox.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;"
          + "-fx-border-width: 2;" + "-fx-border-insets: 5;"
          + "-fx-border-radius: 5;" + "-fx-border-color: " + color +";");
    } else {
      if (left) {
        senderName = new Label("Stranger");
        msgVbox = new VBox(5, senderName, imageView);
        msgVbox.setAlignment(Pos.CENTER_LEFT);
        hbox = new HBox(msgVbox);
        hbox.setAlignment(Pos.CENTER_LEFT);
      } else {
        senderName = new Label("You");
        msgVbox = new VBox(5, senderName, imageView);
        msgVbox.setAlignment(Pos.CENTER_RIGHT);
        hbox = new HBox(msgVbox);
        hbox.setAlignment(Pos.CENTER_RIGHT);
      }
      senderName.setStyle("-fx-font-weight: bold;" + "-fx-font-family: Arial;" +  "-fx-font-size: 14;");
      msgVbox.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;"
          + "-fx-border-width: 2;" + "-fx-border-insets: 5;"
          + "-fx-border-radius: 5;" + "-fx-border-color: lightblue;");
    }

    Platform.runLater(() -> {
      chatbox.getChildren().add(hbox);
      chatbox.setSpacing(10);
      chatscroll.vvalueProperty().bind(chatbox.heightProperty());
      chatscroll.setFitToWidth(true);
    });
  }
}
