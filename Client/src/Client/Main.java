package Client;

import Client.Controller.ChatController;
import Client.Controller.RootController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
  public static Client client = new Client();
  public static Stage homeStage;
  public static Stage currentStage;
  public static String currentScene = "root";
  public static Parent root;
  public static ChatController cc;
  public static RootController rc;
  public static boolean connected = false;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    homeStage = primaryStage;
    homeStage.setOnCloseRequest(e -> {
      try {
        client.request("disconnect");
      } catch (IOException ex) {
        ex.printStackTrace();
      }
      Platform.exit();
      System.exit(0);
    });
    currentStage = primaryStage;
    FXMLLoader fXMLLoader = new FXMLLoader();
    fXMLLoader.setLocation(getClass().getResource("View/root.fxml"));
    root = fXMLLoader.load();
    rc = fXMLLoader.getController();
    primaryStage.setTitle("Chit Chat");
    primaryStage.setScene(new Scene(root, 600, 400));
    primaryStage.setResizable(false);
    primaryStage.show();
    Platform.runLater(() -> rc.init());
    Thread thread = new Thread(() -> {
      try {
        client.start();
      } catch (IOException e) {
        try {
          rc.newStage("warning", "Warning", "Something gone wrong. Cant start application!");
        } catch (IOException ignored) {
        }
      }
    });
    thread.start();
  }
}
