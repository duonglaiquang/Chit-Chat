package Client;

import ChatRoom.ChatRoom;
import Client.Controller.CreateRoomModalController;
import Client.Controller.RoomListController;
import Client.Controller.RootController;
import javafx.fxml.FXMLLoader;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class Client {
  public Socket s;
  public ObjectOutputStream oos;
  public ObjectInputStream ois;
  List<ChatRoom> data = createData();

  public void request(String action) throws IOException {
    oos.writeObject("request#" + action);
  }

  private List<ChatRoom> createData() {
    List<ChatRoom> data = new ArrayList<>(50);
    for (int i = 0; i < 50; i++) {
      data.add(new ChatRoom(i, "abc", "asdasdasd"));
    }
    return data;
  }

  public void start() throws IOException {
    //create socket and get ip
    s = new Socket("192.168.110.122", 1234);
    oos = new ObjectOutputStream(s.getOutputStream());
    ois = new ObjectInputStream(s.getInputStream());
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    //create sender thread
    Thread thSender = new Thread(() -> {
      try {
        while (true) {
//            synchronized (this) {
          //scan new message to send
          String strSend = br.readLine();
          if (strSend != null) {
            oos.writeObject(strSend);
            oos.flush();
          }
//            }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    });

    Thread thReceiver = new Thread(() -> {
      try {
        while (true) {
//            synchronized (this) {
          Object objReceived = ois.readObject();
          if (objReceived instanceof String) {
            String strReceived = (String) objReceived;
            StringTokenizer st = new StringTokenizer(strReceived, "#");
            String from = st.nextToken();
            if (from.equals("server")) {
              String response = st.nextToken();
              RootController rc = new RootController();
              CreateRoomModalController mc = new CreateRoomModalController();
              switch (response) {
                case "Matched":
                  System.out.println("Client Matched!");
                  rc = new RootController();
                  rc.matched();
                  break;

                case "Searching":
                  System.out.println("Searching...");
                  break;

                case "Search_Canceled":
                  System.out.println("Search Canceled!");
                  rc.changeScene("root");
                  break;

                case "Stranger_Disconnected":
                  Main.cc.showSystemMessage("Stranger Has Left The Chat!");
                  break;

                case "Connected":
                  Main.connected = true;
                  Main.rc.updateConnectionStatus();
                  break;

                case "Room_Created":
                  System.out.println("Room Created!");
                  String id = st.nextToken();
                  String name = st.nextToken();
                  String description = st.nextToken();
                  mc.roomCreated(id, name, description);
                  break;

                case "No_Room_Available":
                  System.out.println("No Room Available!");
                  break;

                default:
                  System.out.println("Unknown Response: " + strReceived);
                  break;
              }
            } else {
              System.out.println(strReceived);
              Main.cc.addMessage(strReceived, true);
            }
          } else {
            System.out.println("Room Info Received");
            if (objReceived instanceof LinkedList) {
              int roomCount = ((LinkedList) objReceived).size();
              System.out.println(roomCount);
              FXMLLoader loader = new FXMLLoader(getClass().getResource("View/roomList.fxml"));
              loader.load();
              RoomListController roomListController = loader.getController();
//              roomList = new ArrayList<ChatRoom>((LinkedList) objReceived);
            }
          }
//            }
        }
      } catch (EOFException e) {
        System.out.println("Disconnected!");
      } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
      }
    });

    thSender.start();
    thReceiver.start();
  }
}
