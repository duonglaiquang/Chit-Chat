package Client;


import Client.Controller.RoomController;
import Client.Controller.RootController;
import javafx.fxml.FXMLLoader;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;


public class Client {
  public static Socket s;

  public static ObjectOutputStream oos;
  public static ObjectInputStream ois;

  public void request(String action) throws IOException {
    oos.writeObject("request#" + action);
  }

  public void start() throws IOException {
    //create socket and get ip
    s = new Socket("192.168.161.97", 1234);
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
              FXMLLoader loader;
              RootController rootController;
              RoomController roomController;
              switch (response) {
                case "Matched":
                  System.out.println("Client Matched!");
                  loader = new FXMLLoader(getClass().getResource("View/root.fxml"));
                  loader.load();
                  rootController = loader.getController();
                  rootController.matched();
                  break;

                case "Searching":
                  System.out.println("Searching...");
                  break;

                case "Search_Canceled":
                  System.out.println("Search Canceled!");
                  loader = new FXMLLoader(getClass().getResource("View/root.fxml"));
                  loader.load();
                  rootController = loader.getController();
                  System.out.println(rootController);
                  rootController.changeScene("root");
                  break;

                case "Room_Created":
                  System.out.println("Room Created!");
                  String id = st.nextToken();
                  String name = st.nextToken();
                  String description = st.nextToken();
                  loader = new FXMLLoader(getClass().getResource("View/createRoomModal.fxml"));
                  loader.load();
                  roomController = loader.getController();
                  roomController.roomCreated(id, name, description);
                  break;

                case "No_Room_Available":
                  System.out.println("No Room Available!");
                  break;

                default:
                  System.out.println("Unknown Response: " + strReceived);
                  break;
              }
            } else System.out.println(strReceived);
          } else {
//            if (objReceived instanceof LinkedList) {
//              int roomcount = ((LinkedList) objReceived).size();
//              for (int i = 0; i < roomcount; i++) {
//                ChatRoom obj = (ChatRoom) ((LinkedList) objReceived).get(i);
//                System.out.println(obj.name+","+obj.description);
//              }
//            }
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
