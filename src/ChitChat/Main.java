package ChitChat;

import ChitChat.Client.Client;
import ChitChat.Controller.RootController;
import ChitChat.Controller.SearchingController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
  public static Client client = new Client();
  public static Stage homestage;
  public static RootController rc = new RootController();
  public static SearchingController sc = new SearchingController();

  @Override
  public void start(Stage primaryStage) throws Exception {
    client.start();
    homestage = primaryStage;
    Parent root = FXMLLoader.load(getClass().getResource("View/root.fxml"));
    primaryStage.setTitle("Chit Chat");
    primaryStage.setScene(new Scene(root, 600, 400));
    primaryStage.show();
    homestage.setOnCloseRequest(e -> {
      Platform.exit();
      System.exit(0);
    });
  }

  public static void main(String[] args) {
    launch(args);
  }
}
