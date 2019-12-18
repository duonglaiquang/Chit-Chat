package Server;

import ChatRoom.ChatRoom;
import ChatRoom.Color;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class Server {
  final static String[] tags = {null, "game", "movie", "book", "music", "boy", "girl"};

  static HashMap<Socket, ObjectOutputStream> oosOf = new HashMap<>();
  static ArrayList<ChatRoom> rooms = new ArrayList<>();
  static HashMap<Socket, ChatRoom> currentRoom = new HashMap<>();
  static HashMap<String, LinkedList<Socket>> searching = new HashMap<>();
  static LinkedList<Socket> isSearching = new LinkedList<>();
  static HashMap<Socket, Socket> pair = new HashMap<>();
  static HashMap<Socket, Boolean> cancel = new HashMap<>();
  static int userCount = 0;
  static int roomCount = 0;

  static void searchTag(Socket s, String tag, ObjectOutputStream oos) throws IOException {
    LinkedList<Socket> soc;
    if (searching.get(tag) != null) {
      soc = searching.get(tag);
    } else {
      soc = new LinkedList<>();
    }
    soc.add(s);
    searching.put(tag, soc);
    if (soc.size() % 2 == 0) {
      Socket socket = soc.get(soc.size() - 2);
      pair.put(s, socket);
      pair.put(socket, s);
      soc.remove(s);
      soc.remove(socket);
      isSearching.remove(s);
      isSearching.remove(socket);
      searching.put(tag, soc);
      System.out.println("2 Clients has been matched with each other!");
      oos.writeObject("server#Matched");
      oos.flush();
      ObjectOutputStream os = oosOf.get(socket);
      os.writeObject("server#Matched");
      os.flush();
    }
  }

  public static void cancelSearch(Socket s, ObjectOutputStream oos, String tag) throws IOException {
    if (searching.get(tag).size() == 1) {
      searching.remove(tag);
    } else {
      searching.get(tag).remove(s);
    }
    cancel.remove(s);
    isSearching.remove(s);
    oos.writeObject("server#Search_Canceled");
    oos.flush();
  }

  public static void match(Socket s, String tag, ObjectOutputStream oos) throws IOException, InterruptedException, NullPointerException {
    isSearching.add(s);
    cancel.put(s, false);
    searchTag(s, tag, oos);
    while (pair.get(s) == null) {
      if (cancel.get(s)) {
        cancelSearch(s, oos, tag);
        break;
      }
      oos.writeObject("server#Searching");
      oos.flush();
      Thread.sleep(1000);
    }
  }

  public static void loadRoomList(ObjectOutputStream oos) throws IOException {
    if (roomCount == 0) {
      oos.writeObject("server#No_Room_Available");
    } else {
      oos.writeObject(rooms);
    }
    oos.flush();
  }

  public static void createRoom(Socket s, ObjectOutputStream oos, String name, String description) throws IOException {
    ChatRoom room = new ChatRoom(roomCount, name, description);
    room.clientCount++;
    room.sockets.add(s);
    rooms.add(room);
    currentRoom.put(s, room);
    String color = colorPicker(room);
    room.colorOf.put(s, color);
    oos.writeObject("server#Room_Created#" + color + "#" + name);
    oos.flush();
    roomCount++;
  }

  public static String colorPicker(ChatRoom room) {
    Color color = null;
    for (int i = 0; i < ChatRoom.MAX_CLIENT; i++) {
      if (room.color[i].isAvailable()) {
        room.color[i].setAvailable(false);
        color = room.color[i];
        break;
      }
    }
    return color.getName();
  }

  public static void joinRoom(Socket s, ObjectOutputStream oos, Integer id) throws IOException {
    ChatRoom room = rooms.get(id);
    if (room.clientCount < ChatRoom.MAX_CLIENT) {
      room.sockets.add(s);
      room.clientCount++;
      currentRoom.put(s, room);
      String color = colorPicker(room);
      room.colorOf.put(s, color);
      for (Socket socket : room.sockets) {
        if (!socket.equals(s)) {
          ObjectOutputStream os = oosOf.get(socket);
          os.writeObject("server#Stranger_Joined");
        }
      }
      oos.writeObject("server#Room_Joined#" + color + "#" + room.name);
      oos.flush();
    } else {
      oos.writeObject("server#Room_Full");
    }
  }

  public static void leaveChat(Socket s, ObjectOutputStream oos) throws IOException {
    if (pair.get(s) != null) {
      Socket socket = pair.get(s);
      ObjectOutputStream os = oosOf.get(socket);
      pair.remove(s);
      pair.remove(socket);
      //TODO remove tag garbage
      os.writeObject("server#Stranger_Disconnected");
      oos.writeObject("server#Chat_Left");
      os.flush();
      oos.flush();
    } else if (currentRoom.get(s) != null) {
      ChatRoom room = currentRoom.get(s);
      if (room.clientCount == 1) {
        rooms.remove(room);
        roomCount--;
      } else {
        room.clientCount--;
        room.sockets.remove(s);
        String colorName = room.colorOf.get(s);
        for (int i = 0; i < ChatRoom.MAX_CLIENT; i++) {
          if (room.color[i].getName().equals(colorName)) {
            room.color[i].setAvailable(true);
            break;
          }
        }
        room.colorOf.remove(s);
        for (Socket socket : room.sockets) {
          ObjectOutputStream os = oosOf.get(socket);
          os.writeObject("server#Stranger_Left");
        }
      }
      currentRoom.remove(s);
      oos.writeObject("server#Room_Left");
      oos.flush();
    } else {
      if (isSearching.contains(s)) {
        cancel.put(s, true);
      }
    }
  }

  public static void quit(Socket s, ObjectOutputStream oos) throws IOException {
    leaveChat(s, oos);
    userCount--;
    System.out.println("User disconnected, Count updated: " + userCount);
  }

  public static void getStatus() {
    //TODO
  }

  public static void checkCommand(String str, Socket s, ObjectOutputStream oos) throws IOException {
    ChatRoom room;
    StringTokenizer st = new StringTokenizer(str, "#");
    String cmd = st.nextToken();
    if (cmd.equals("request")) {
      String action = st.nextToken();
      switch (action) {
        case "match":
          String tag = st.nextToken();
          Thread thMatch = new Thread(() -> {
            try {
              match(s, tag, oos);
            } catch (IOException | InterruptedException | NullPointerException e) {
              System.out.println("Exception in matching!");
            }
          });
          thMatch.start();
          break;

        case "cancel":
          cancel.put(s, true);
          System.out.println("Search Canceled!");
          break;

        case "createRoom":
          String name = st.nextToken();
          String description = st.nextToken();
          if (description == null) {
            description = "Welcome to " + name + "!";
          }
          createRoom(s, oos, name, description);
          System.out.println("Room Created!");
          break;

        case "joinRoom":
          int id = Integer.parseInt(st.nextToken());
          joinRoom(s, oos, id);
          break;

        case "roomls":
          loadRoomList(oos);
          break;

        case "leaveChat":
          leaveChat(s, oos);
          break;

        case "disconnect":
          quit(s, oos);
          break;
        default:
          oos.writeObject("server#Wrong Request Command!");
      }
    } else {
      try {
        if (pair.get(s) != null) {
          ObjectOutputStream os = oosOf.get(pair.get(s));
          os.writeObject(str);
        } else {
          room = currentRoom.get(s);
          for (Socket socket : room.sockets) {
            if (!socket.equals(s)) {
              ObjectOutputStream os = oosOf.get(socket);
              os.writeObject(str + "#" + room.colorOf.get(s));
            }
          }
        }
      } catch (IOException | NullPointerException e) {
        e.printStackTrace();
      }
    }
  }

  public static void main(String[] args) throws IOException {
    try (ServerSocket ss = new ServerSocket(1234)) {
      System.out.println("Running Server on " + ss + "...");
      Socket s;

      while (true) {
        s = ss.accept();
        userCount++;
        System.out.println("New Client Connected At " + s);
        System.out.println("Client count: "+userCount);
        ClientHandler handler = new ClientHandler(s);
        Thread t = new Thread(handler);
        t.start();
      }
    }
  }
}
