package Client;

import ChatRoom.SerializableImage;
import Client.Controller.ModalController;
import Client.Controller.RoomListController;
import Client.Controller.RootController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Client {
  public Socket s;
  public ObjectOutputStream oos;
  public ObjectInputStream ois;
  public String color = null;

  public void request(String action) throws IOException {
    oos.writeObject("request#" + action);
    oos.flush();
  }

  public void sendImage(Image image) throws IOException {
    SerializableImage img = new SerializableImage();
    img.setImage(image);
    oos.writeObject(img);
    oos.flush();
  }

  public void start() throws IOException {
    s = new Socket("192.168.110.122", 1234);
    oos = new ObjectOutputStream(s.getOutputStream());
    ois = new ObjectInputStream(s.getInputStream());

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
                  System.out.println("Client Matched!");
                  rc = new RootController();
                  rc.changeScene("chatBox");
                  Main.cc.init("Stranger");
                  break;

                case "Searching":
                  System.out.println("Searching...");
                  break;

                case "Search_Canceled":
                  System.out.println("Search Canceled!");
                  rc.changeScene("root");
                  break;

                case "Stranger_Disconnected":
                  Main.cc.showSystemMessage("Stranger Has Left The Chat!", true);
                  break;

                case "Chat_Left":
                  rc.changeScene("root");
                  break;

                case "Connected":
                  Main.connected = true;
                  Main.rc.updateConnectionStatus();
                  break;

                case "Stranger_Joined":
                  System.out.println("Stranger Joined!");
                  Main.cc.showSystemMessage("Stranger has joined the chat!", false);
                  break;

                case "Stranger_Left":
                  Main.cc.showSystemMessage("Stranger has left the chat!", false);
                  break;

                case "Room_Created":
                  System.out.println("Room Created!");
                  color = st.nextToken();
                  String roomName = st.nextToken();
                  mc.roomCreated();
                  Main.cc.init(roomName);
                  Main.cc.showSystemMessage("You are <" + color.toUpperCase() + ">", false);
                  break;

                case "No_Room_Available":
                  System.out.println("No Room Available!");
                  rc.changeScene("root");
                  rc.newStage("warning", "Warning", "Room List Empty!");
                  break;

                case "Room_Joined":
                  System.out.println("Room Joined!");
                  color = st.nextToken();
                  String room = st.nextToken();
                  rc = new RootController();
                  rc.changeScene("chatBox");
                  Main.cc.init(room);
                  Main.cc.showSystemMessage("You are <" + color.toUpperCase() + ">", false);
                  break;

                case "Room_Left":
                  System.out.println("Room Left");
                  request("roomls");
                  break;

                case "Room_Full":
                  rc.newStage("warning", "Warning", "Room Full!");
                  break;

                default:
                  System.out.println("Unknown Response: " + strReceived);
                  break;
              }
            } else {
              System.out.println(strReceived);
              if (st.hasMoreTokens()) {
                String strangerColor = st.nextToken();
                Main.cc.addMessage(from, true, strangerColor);
              } else {
                Main.cc.addMessage(from, true, null);
              }
            }
          } else if (objReceived instanceof ArrayList) {
            FXMLLoader fXMLLoader = new FXMLLoader();
            fXMLLoader.setLocation(getClass().getResource("View/roomList.fxml"));
            Scene scene = new Scene(fXMLLoader.load(), 600, 400);
            Stage stage = Main.homeStage;
            RoomListController rc = fXMLLoader.getController();
            rc.init(objReceived);
            Platform.runLater(() -> stage.setScene(scene));
          } else {
            SerializableImage image = (SerializableImage) objReceived;
            Image img = image.getImage();
            Main.cc.addImage(img, true);
          }
        }
      } catch (EOFException e) {
        System.out.println("Disconnected!");
      } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
      }
    });

    thReceiver.start();
  }
}
