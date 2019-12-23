package Client;

import Client.Controller.ModalController;
import Client.Controller.RoomListController;
import Client.Controller.RootController;
import CustomClass.SerializableImage;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.StringTokenizer;

public class Client {
  public Socket s;
  public ObjectOutputStream oos;
  public ObjectInputStream ois;
  public String color = null;

  public void request(String action) throws IOException, NullPointerException {
    oos.writeObject("request#" + action);
    oos.flush();
  }

  public void sendImage(Image image, String color) throws IOException {
    SerializableImage img = new SerializableImage();
    img.setImage(image);
    img.setColor(color);
    oos.writeObject(img);
    oos.flush();
  }

  public void updateCount() {
    Thread thUpdater = new Thread(() -> {
      while (Main.currentScene.equals("root")) {
        try {
          request("getStatus");
        } catch (IOException | NullPointerException e) {
          break;
        }
        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });

    thUpdater.start();
  }

  public void start() throws IOException {
    while (s == null) {
      try {
        s = new Socket("192.168.110.122", 1234);
        oos = new ObjectOutputStream(s.getOutputStream());
        ois = new ObjectInputStream(s.getInputStream());
      } catch (Exception ignored) {
      }
    }

    Thread thReceiver = new Thread(() -> {
      try {
        while (true) {
          Object objReceived = ois.readObject();
          if (objReceived instanceof String) {
            String strReceived = (String) objReceived;
            StringTokenizer st = new StringTokenizer(strReceived, "#");
            String from = st.nextToken();
            if (from.equals("server")) {
              String response = st.nextToken();
              RootController rc = new RootController();
              ModalController mc = new ModalController();
              switch (response) {
                case "Matched":
                  rc = new RootController();
                  rc.changeScene("chatBox");
                  Main.cc.setTag(st.nextToken());
                  Main.cc.init("Stranger", null);
                  break;

                case "Searching":
                  break;

                case "Search_Canceled":

                case "Chat_Left":
                  rc.changeScene("root");
                  break;

                case "Stranger_Disconnected":
                  Main.cc.showSystemMessage("Stranger Has Left The Chat!", true);
                  Main.cc.overrideBtn();
                  break;

                case "Connected":
                  Main.connected = true;
                  Main.rc.updateConnectionStatus();
                  Main.rc.updateUserCount(st.nextToken());
                  break;

                case "Stranger_Joined":
                  Main.cc.showSystemMessage("Stranger has joined the chat!", false);
                  break;

                case "Stranger_Left":
                  Main.cc.showSystemMessage("Stranger has left the chat!", false);
                  break;

                case "Room_Created":
                  color = st.nextToken();
                  String roomName = st.nextToken();
                  mc.roomCreated();
                  Main.cc.init(roomName, color);
                  Main.cc.showSystemMessage("You are <" + color.toUpperCase() + ">", false);
                  break;

                case "No_Room_Available":
                  rc.changeScene("root");
                  rc.newStage("warning", "Warning", "Room List Empty!");
                  break;

                case "Room_Joined":
                  color = st.nextToken();
                  String room = st.nextToken();
                  rc = new RootController();
                  rc.changeScene("chatBox");
                  Main.cc.init(room, color);
                  Main.cc.showSystemMessage("You are <" + color.toUpperCase() + ">", false);
                  break;

                case "Room_Left":
                  request("roomls");
                  break;

                case "Room_Full":
                  rc.newStage("warning", "Warning", "Room Full!");
                  break;

                case "Client_Count":
                  Main.rc.updateUserCount(st.nextToken());
                  break;

                default:
                  System.out.println("Unknown Response: " + strReceived);
                  break;
              }
            } else {
              if (st.hasMoreTokens()) {
                String strangerColor = st.nextToken();
                Main.cc.addMessage(from, true, strangerColor);
              } else {
                Main.cc.addMessage(from, true, null);
              }
            }
          } else if (objReceived instanceof List) {
            System.out.println(objReceived);
            System.out.println(((List) objReceived).size());
            FXMLLoader fXMLLoader = new FXMLLoader();
            fXMLLoader.setLocation(getClass().getResource("View/roomList.fxml"));
            Scene scene = new Scene(fXMLLoader.load(), 600, 400);
            Stage stage = Main.homeStage;
            RoomListController rc = fXMLLoader.getController();
            rc.init(objReceived);
            Main.currentScene = "roomList";
            Platform.runLater(() -> stage.setScene(scene));
          } else {
            SerializableImage image = (SerializableImage) objReceived;
            Image img;
            String color = null;
            try {
              img = image.getImage();
              color = image.getColor();
            } catch (IllegalArgumentException e) {
              img = new Image(new FileInputStream("src/Client/Assets/images/file-not-found.png"));
            }
            Main.cc.addImage(img, true, color);
          }
        }
      } catch (EOFException | SocketException e) {
        // Lost connection
        RootController rc = new RootController();
        try {
          rc.changeScene("root");
        } catch (IOException ignored) {
        }
        Main.connected = false;
        try {
          rc.newStage("warning", "Warning", "Disconnected from server!");
        } catch (IOException ignored) {
        }
        Main.rc.updateConnectionStatus();
        s = null;
        oos = null;
        ois = null;
        try {
          start();
        } catch (IOException ignored) {
        }
      } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
      }
    });

    thReceiver.start();
    updateCount();
  }
}
