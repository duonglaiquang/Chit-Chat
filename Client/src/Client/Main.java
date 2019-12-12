package Client;

import Client.Controller.ChatController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
  public static Client client = new Client();
  public static Stage homeStage;
  public static Stage currentStage;
  public static Parent root;
  public static ChatController cc;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    client.start();
    homeStage = primaryStage;
    currentStage = primaryStage;
    root = FXMLLoader.load(getClass().getResource("View/root.fxml"));
    primaryStage.setTitle("Chit Chat");
    primaryStage.setScene(new Scene(root, 600, 400));
    primaryStage.show();
    homeStage.setOnCloseRequest(e -> {
      Platform.exit();
      System.exit(0);
    });
  }
}
