package ChitChat.Server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
  static LinkedList<ClientHandler> handlers = new LinkedList<>();
  static LinkedList<ChatRoom> rooms = new LinkedList<>();
  static HashMap<String, LinkedList<Socket>> searching = new HashMap<>();
  static HashMap<Socket, ChatRoom> currentRoom = new HashMap<>();
  static HashMap<Socket, Socket> pair = new HashMap<>();
  static HashMap<Socket, Boolean> cancel = new HashMap<>();
  static int userCount = 0;
  static int roomCount = 0;
  final static String[] commands = {"cmd-create-<room_name>", "cmd-join-<room_id>", "cmd-match-<#tag1#tag2#tag3>", "cmd-status",
      "cmd-roomls", "cmd-ls", "cmd-leave", "cmd-quit"};
  final static String[] tags = {"game", "movie", "book", "music", "random"};

  static void checkTag(Socket s, String tag) {
    if (searching.get(tag) != null) {
      searching.get(tag).add(s);
    } else {
      LinkedList<Socket> soc = new LinkedList<>();
      soc.add(s);
      searching.put(tag, soc);
    }
  }

  static void searchTag(Socket s, String tag, DataOutputStream dos) throws IOException {
    checkTag(s, tag);
    LinkedList<Socket> soc = searching.get(tag);
    if (soc.size() % 2 == 0) {
      Socket socket = soc.get(soc.size() - 2);
      pair.put(s, socket);
      pair.put(socket, s);
      soc.remove(s);
      soc.remove(socket);
      DataOutputStream os = new DataOutputStream(socket.getOutputStream());
      dos.writeUTF("server#Matched");
      dos.flush();
      os.writeUTF("server#Matched");
      os.flush();
      System.out.println("2 Clients has been matched with each other!");
    }
  }

  public static void cancelSearch(Socket s, DataOutputStream dos, String tags) throws IOException {
    dos.writeUTF("server#SearchCanceled");
    dos.flush();
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

  public static void match(Socket s, String tags, DataOutputStream dos) throws IOException, InterruptedException {
    if (tags != null) {
      String[] tagArr = tags.split("#");
      for (int i = 1; i < tagArr.length; i++) {
        searchTag(s, tagArr[i], dos);
      }
    } else {
      searchTag(s, null, dos);
    }
    while (pair.get(s) == null) {
      if (cancel.get(s)) {
        cancelSearch(s,dos,tags);
        break;
      }
      dos.writeUTF("server#Serching");
      dos.flush();
      Thread.sleep(1000);
    }
  }

  public static void loadRoomList(Socket s) throws IOException {
    DataOutputStream dos = new DataOutputStream(s.getOutputStream());
    if (roomCount == 0) {
      dos.writeUTF("server#No Room Chat Available!");
      dos.flush();
    } else {
      dos.writeUTF("server#Room List: ");
      for (ChatRoom room : rooms) {
        dos.writeUTF("server#" + room.id + "." + room.name);
        dos.flush();
      }
    }
  }

  public static void variablesCorrection(Socket s) throws IOException, NullPointerException {
    ChatRoom room;
    DataOutputStream dos = new DataOutputStream(s.getOutputStream());
    if (pair.get(s) != null) {
      Socket socket = pair.get(s);
      DataOutputStream os = new DataOutputStream(socket.getOutputStream());
      pair.remove(s);
      pair.remove(socket);
      os.writeUTF("server#Stranger has left the chat!\nReturning to main screen...");
      dos.writeUTF("server#Stranger has left the chat!\nReturning to main screen...");
      os.flush();
      dos.flush();
      loadRoomList(socket);
    } else if (currentRoom.get(s) != null) {
      room = currentRoom.get(s);
      if (room.clientCount == 1) {
        rooms.remove(room);
        roomCount--;
      } else {
        room.clientCount--;
        room.sockets.remove(s);
        for (Socket socket : room.sockets) {
          DataOutputStream os = new DataOutputStream(socket.getOutputStream());
          os.writeUTF("server#Stranger has left the chat.");
        }
      }
      currentRoom.remove(s);
      dos.writeUTF("server#Leaved!");
      dos.flush();
    }
  }

  public static void createRoom(Socket s, String name) throws IOException {
    ChatRoom room = new ChatRoom(roomCount, name);
    DataOutputStream dos = new DataOutputStream(s.getOutputStream());
    room.clientCount++;
    room.sockets.add(s);
    rooms.add(room);
    roomCount++;
    currentRoom.put(s, room);
    dos.writeUTF("server#RoomCreated");
    dos.flush();
  }

  public static void joinRoom(Socket s, Integer id) throws IOException {
    ChatRoom room = rooms.get(id);
    DataOutputStream dos = new DataOutputStream(s.getOutputStream());
    room.clientCount++;
    for (Socket socket : room.sockets) {
      DataOutputStream os = new DataOutputStream(socket.getOutputStream());
      os.writeUTF("server#StrangerJoined");
    }
    room.sockets.add(s);
    currentRoom.put(s, room);
    dos.writeUTF("server#RoomJoined");
    dos.flush();
  }

  public static void leave(Socket s) throws IOException {
    variablesCorrection(s);
    loadRoomList(s);
    s.close();
  }

  public static void quit(Socket s) throws IOException {
    variablesCorrection(s);
    s.close();
  }

  public static void loadRoom(Socket s) throws IOException {
    loadRoomList(s);
  }

  public static void getStatsu() {
    //TODO
  }

  public static void checkCommand(String str, Socket s, DataOutputStream dos) {
    ChatRoom room;
    StringTokenizer st = new StringTokenizer(str, "#");
    String cmd = st.nextToken();
    if (cmd.equals("request")) {
      String action = st.nextToken();
      if (action.equals("match")) {
        Thread thMatch = new Thread(() -> {
          try {
            match(s, null, dos);
          } catch (IOException | InterruptedException e) {
            e.printStackTrace();
          }
        });
        thMatch.start();
      }
      if (action.equals("cancel")) {
        cancel.put(s, true);
      }
    } else {
      try {
        if (pair.get(s) != null) {
          DataOutputStream os = new DataOutputStream(pair.get(s).getOutputStream());
          os.writeUTF("Stranger: " + str);
        } else {
          room = currentRoom.get(s);
          for (Socket socket : room.sockets) {
            if (!socket.equals(s)) {
              DataOutputStream os = new DataOutputStream(socket.getOutputStream());
              os.writeUTF("Stranger: " + str);
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
        handlers.add(handler);
        Thread t = new Thread(handler);
        t.start();
      }
    }
  }
}
