package Server;

import CustomClass.ChatRoom;
import CustomClass.Color;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
  final static String[] tags = {null, "game", "movie", "book", "music", "boy", "girl"};
  //TODO : replace none syncronized collection
  //TODO : recheck roomls function
  //TODO : build with gradle ,package dmg
  //TODO : revamp UI

  static Map<Socket, ObjectOutputStream> oosOf = Collections.synchronizedMap(new HashMap<>());
  static Map<Socket, ChatRoom> currentRoom = Collections.synchronizedMap(new HashMap<>());
  static Map<String, LinkedList<Socket>> searching = Collections.synchronizedMap(new HashMap<>());
  static Map<Socket, Socket> pair = Collections.synchronizedMap(new HashMap<>());
  static Map<Socket, Boolean> cancel = Collections.synchronizedMap(new HashMap<>());
  static List<Socket> isSearching = Collections.synchronizedList(new ArrayList<>());
  static List<ChatRoom> rooms = Collections.synchronizedList(new ArrayList<>());
  static AtomicInteger userCount = new AtomicInteger(0);
  static AtomicInteger roomCount = new AtomicInteger(0);

  static void increment(AtomicInteger i) {
    while(true) {
      int existingValue = i.get();
      int newValue = existingValue + 1;
      if(i.compareAndSet(existingValue, newValue)) {
        return;
      }
    }
  }

  static void decrement(AtomicInteger i) {
    while(true) {
      int existingValue = i.get();
      int newValue = existingValue - 1;
      if(i.compareAndSet(existingValue, newValue)) {
        return;
      }
    }
  }

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
      oos.writeObject("server#Matched#" + tag);
      oos.flush();
      ObjectOutputStream os = oosOf.get(socket);
      os.writeObject("server#Matched#" + tag);
      os.flush();
    }
  }

  static void cancelSearch(Socket s, ObjectOutputStream oos, String tag) throws IOException {
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

  static void match(Socket s, String tag, ObjectOutputStream oos) throws IOException, InterruptedException, NullPointerException {
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

  static void loadRoomList(ObjectOutputStream oos) throws IOException {
    if (roomCount.get() == 0) {
      oos.writeObject("server#No_Room_Available");
    } else {
      System.out.println(rooms.size());
      oos.writeObject(rooms);
    }
    oos.flush();
  }

  static void createRoom(Socket s, ObjectOutputStream oos, String name, String description) throws IOException {
    ChatRoom room = new ChatRoom(roomCount.get(), name, description);
    increment(room.clientCount);
    room.sockets.add(s);
    rooms.add(room);
    currentRoom.put(s, room);
    String color = colorPicker(room);
    room.colorOf.put(s, color);
    increment(roomCount);
    oos.writeObject("server#Room_Created#" + color + "#" + name);
    oos.flush();
  }

  static String colorPicker(ChatRoom room) {
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

  static void joinRoom(Socket s, ObjectOutputStream oos, Integer id) throws IOException {
    ChatRoom room = rooms.get(id);
    if (room.clientCount.get() < ChatRoom.MAX_CLIENT) {
      room.sockets.add(s);
      increment(room.clientCount);
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

  static void leaveChat(Socket s, ObjectOutputStream oos) throws IOException {
    if (pair.get(s) != null) {
      Socket socket = pair.get(s);
      ObjectOutputStream os = oosOf.get(socket);
      pair.remove(s);
      pair.remove(socket);
      os.writeObject("server#Stranger_Disconnected");
      oos.writeObject("server#Chat_Left");
      os.flush();
      oos.flush();
    } else if (currentRoom.get(s) != null) {
      ChatRoom room = currentRoom.get(s);
      if (room.clientCount.get() == 1) {
        rooms.remove(room);
        decrement(roomCount);
      } else {
        decrement(room.clientCount);
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

  static void quit(Socket s, ObjectOutputStream oos) throws IOException {
    if (oosOf.get(s) != null) {
      leaveChat(s, oos);
      oosOf.remove(s);
      decrement(userCount);
      System.out.println("User disconnected, Count updated: " + userCount);
    }
  }

  static void getStatus(ObjectOutputStream oos) throws IOException {
    oos.writeObject("server#Client_Count#" + userCount);
  }

  static void checkCommand(String str, Socket s, ObjectOutputStream oos) throws IOException {
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

        case "getStatus":
          getStatus(oos);
          break;
        default:
          oos.writeObject("server#Wrong Request Command!");
      }
    } else {
      transportMsg(s, str);
    }
  }

  static void transportMsg(Socket s, Object obj) throws IOException, NullPointerException {
    if (pair.get(s) != null) {
      ObjectOutputStream os = oosOf.get(pair.get(s));
      os.writeObject(obj);
    } else {
      ChatRoom room = currentRoom.get(s);
      for (Socket socket : room.sockets) {
        if (!socket.equals(s)) {
          ObjectOutputStream os = oosOf.get(socket);
          if (obj instanceof String) {
            os.writeObject(obj + "#" + room.colorOf.get(s));
          } else {
            os.writeObject(obj);
          }
        }
      }
    }
  }

  public static void main(String[] args) throws IOException {
    try (ServerSocket ss = new ServerSocket(1234)) {
      System.out.println("Running Server on " + ss + "...");
      Socket s;

      while (true) {
        s = ss.accept();
        increment(userCount);
        System.out.println("New Client Connected At " + s);
        System.out.println("Client count: " + userCount);
        ClientHandler handler = new ClientHandler(s);
        Thread t = new Thread(handler);
        t.start();
      }
    }
  }
}
