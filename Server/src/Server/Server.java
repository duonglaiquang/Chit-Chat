package Server;

import ChatRoom.ChatRoom;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class Server {
  final static String[] commands = {"cmd-create-<room_name>", "cmd-join-<room_id>", "cmd-match-<#tag1#tag2#tag3>", "cmd-status",
      "cmd-roomls", "cmd-ls", "cmd-leave", "cmd-quit"};
  final static String[] tags = {"game", "movie", "book", "music", "random"};

  static HashMap<Socket, ObjectOutputStream> oosOf = new HashMap<>();
  static LinkedList<ChatRoom> rooms = new LinkedList<>();
  static HashMap<String, LinkedList<Socket>> searching = new HashMap<>();
  static HashMap<Socket, ChatRoom> currentRoom = new HashMap<>();
  static HashMap<Socket, Socket> pair = new HashMap<>();
  static HashMap<Socket, Boolean> cancel = new HashMap<>();
  static int userCount = 0;
  static int roomCount = 0;

  static void checkTag(Socket s, String tag) {
    if (searching.get(tag) != null) {
      searching.get(tag).add(s);
    } else {
      LinkedList<Socket> soc = new LinkedList<>();
      soc.add(s);
      searching.put(tag, soc);
    }
  }

  static void searchTag(Socket s, String tag, ObjectOutputStream oos) throws IOException {
    checkTag(s, tag);
    LinkedList<Socket> soc = searching.get(tag);
    if (soc.size() % 2 == 0) {
      Socket socket = soc.get(soc.size() - 2);
      pair.put(s, socket);
      pair.put(socket, s);
      soc.remove(s);
      soc.remove(socket);
      System.out.println("2 Clients has been matched with each other!");
      oos.writeObject("server#Matched");
      oos.flush();
      ObjectOutputStream os = oosOf.get(socket);
      os.writeObject("server#Matched");
      os.flush();
    }
  }

  public static void cancelSearch(Socket s, ObjectOutputStream oos, String tags) throws IOException {
    oos.writeObject("server#Search_Canceled");
    oos.flush();
    if (tags != null) {
      String[] tagArr = tags.split("#");
      for (int i = 1; i < tagArr.length; i++) {
        if (searching.get(tagArr[i]).size() == 1) {
          searching.remove(tagArr[i]);
        } else {
          searching.get(tagArr[i]).remove(s);
        }
      }
    } else {
      searching.get(null).remove(s);
    }
    cancel.put(s, false);
  }

  public static void match(Socket s, String tags, ObjectOutputStream oos) throws IOException, InterruptedException {
    if (tags != null) {
      String[] tagArr = tags.split("#");
      for (int i = 1; i < tagArr.length; i++) {
        searchTag(s, tagArr[i], oos);
      }
    } else {
      searchTag(s, null, oos);
    }
    while (pair.get(s) == null) {
      if (cancel.get(s)) {
        cancelSearch(s, oos, tags);
        break;
      }
      oos.writeObject("server#Searching");
      oos.flush();
      Thread.sleep(1000);
    }
  }

  public static void loadRoomList(Socket s, ObjectOutputStream oos) throws IOException {
    if (roomCount == 0) {
      oos.writeObject("server#No_Room_Available");
    } else {
      oos.writeObject(rooms);
    }
    oos.flush();
  }

  public static void variablesCorrection(Socket s, ObjectOutputStream oos) throws IOException, NullPointerException {
    ChatRoom room;
    if (pair.get(s) != null) {
      Socket socket = pair.get(s);
      ObjectOutputStream os = oosOf.get(socket);
      pair.remove(s);
      pair.remove(socket);
      os.writeObject("server#Stranger has left the chat!\nReturning to main screen...");
      oos.writeObject("server#Stranger has left the chat!\nReturning to main screen...");
      os.flush();
      oos.flush();
      loadRoomList(socket, oos);
    } else if (currentRoom.get(s) != null) {
      room = currentRoom.get(s);
      if (room.clientCount == 1) {
        rooms.remove(room);
        roomCount--;
      } else {
        room.clientCount--;
        room.sockets.remove(s);
        for (Socket socket : room.sockets) {
          ObjectOutputStream os = oosOf.get(socket);
          os.writeObject("server#Stranger has left the chat.");
        }
      }
      currentRoom.remove(s);
      oos.writeObject("server#Leaved!");
      oos.flush();
    }
  }

  public static void createRoom(Socket s, ObjectOutputStream oos, String name, String description) throws IOException {
    ChatRoom room = new ChatRoom(roomCount, name, description);
    room.clientCount++;
    room.sockets.add(s);
    rooms.add(room);
    currentRoom.put(s, room);
    oos.writeObject("server#Room_Created#" + roomCount + "#" + name + "#" + description);
    oos.flush();
    roomCount++;
  }

  public static void joinRoom(Socket s, Integer id) throws IOException {
    ChatRoom room = rooms.get(id);
    ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
    room.clientCount++;
    for (Socket socket : room.sockets) {
      ObjectOutputStream os = oosOf.get(socket);
      os.writeObject("server#StrangerJoined");
    }
    room.sockets.add(s);
    currentRoom.put(s, room);
    oos.writeObject("server#RoomJoined");
    oos.flush();
  }

//  public static void leave(Socket s) throws IOException {
//    variablesCorrection(s);
//    loadRoomList(s);
//    s.close();
//  }
//
//  public static void quit(Socket s) throws IOException {
//    variablesCorrection(s);
//    s.close();
//  }

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
          Thread thMatch = new Thread(() -> {
            try {
              match(s, null, oos);
            } catch (IOException | InterruptedException e) {
              e.printStackTrace();
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

        case "roomls":
          loadRoomList(s, oos);
          break;

        default:
          oos.writeObject("server#Wrong Request Command!");
      }
    } else {
      try {
        if (pair.get(s) != null) {
          ObjectOutputStream os = oosOf.get(pair.get(s));
          os.writeObject("Stranger: " + str);
        } else {
          room = currentRoom.get(s);
          for (Socket socket : room.sockets) {
            if (!socket.equals(s)) {
              ObjectOutputStream os = oosOf.get(socket);
              os.writeObject("Stranger: " + str);
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
        ClientHandler handler = new ClientHandler(s);
        cancel.put(s, false);
        Thread t = new Thread(handler);
        t.start();
      }
    }
  }
}
