package ChitChat;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class Server {
  static LinkedList<ClientHandler> handlers = new LinkedList<>();
  static LinkedList<ChatRoom> rooms = new LinkedList<>();
  static LinkedList<Socket> searching = new LinkedList<>();
  static HashMap<Socket, ChatRoom> currentRoom = new HashMap<>();
  static HashMap<Socket, Socket> pair = new HashMap<>();
  static int userCount = 0;
  static int roomCount = 0;
  final static String[] commands = {"cmd-create-<room_name>", "cmd-join-<room_id>", "cmd-match", "cmd-status",
      "cmd-roomls", "cmd-ls", "cmd-leave", "cmd-quit"};

  public static void main(String[] args) throws IOException {
    try (ServerSocket ss = new ServerSocket(1234)) {
      System.out.println("Running Server on " + ss + "...");
      Socket s;

      while (true) {
        s = ss.accept();
        userCount++;
        System.out.println("New Client Connected " + s);
        ClientHandler handler = new ClientHandler(s);
        handlers.add(handler);
        Thread t = new Thread(handler);
        t.start();
        loadRoomList(handler.s);
      }
    }
  }

  public static void loadRoomList(Socket s) throws IOException {
    DataOutputStream dos = new DataOutputStream(s.getOutputStream());
    if (roomCount == 0) {
      dos.writeUTF("server#No Room Chat Available!");
    } else {
      dos.writeUTF("server#Room List: ");
      for (ChatRoom room : rooms) {
        dos.writeUTF("server#" + room.id + "." + room.name);
      }
    }
  }

  public static void variablesCorrection(Socket s) throws IOException {
    ChatRoom room;
    DataOutputStream dos = new DataOutputStream(s.getOutputStream());
    if (pair.get(s) != null) {
      Socket socket = pair.get(s);
      DataOutputStream os = new DataOutputStream(socket.getOutputStream());
      pair.remove(s);
      pair.remove(socket);
      os.writeUTF("server#Stranger has left the chat!\nReturning to main screen...");
      dos.writeUTF("server#Stranger has left the chat!\nReturning to main screen...");
      loadRoomList(socket);
    } else {
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
    }
  }

  public static void checkCommand(String str, Socket s, DataOutputStream dos) {
    StringTokenizer st = new StringTokenizer(str, "-");
    String magicWord = st.nextToken();
    String action = null;
    String target = null;
    ChatRoom room;
    if (st.hasMoreTokens()) {
      action = st.nextToken();
      if (st.hasMoreTokens()) {
        target = st.nextToken();
      }
    }
    try {
      if (magicWord.equals("cmd") && action != null) {
        switch (action) {

          case "create":
            if (target != null) {
              room = new ChatRoom(roomCount, target);
              room.clientCount++;
              room.sockets.add(s);
              rooms.add(room);
              roomCount++;
              currentRoom.put(s, room);
              dos.writeUTF("server#Room Created Successfully");
              dos.writeUTF("Welcome to " + room.name);
            } else dos.writeUTF("server#Wrong Command!");
            break;

          case "join":
            if (target != null) {
              room = rooms.get(Integer.parseInt(target));
              room.clientCount++;
              for (Socket socket : room.sockets) {
                DataOutputStream os = new DataOutputStream(socket.getOutputStream());
                os.writeUTF("server#Stranger has join the chat.");
              }
              room.sockets.add(s);
              currentRoom.put(s, room);
              dos.writeUTF("server#" + room.name + " joined.");
            } else dos.writeUTF("server#Wrong Command!");
            break;

          case "match":
            searching.add(s);
            if (searching.size() % 2 == 0) {
              Socket socket = searching.get(searching.size() - 2);
              pair.put(s, socket);
              pair.put(socket, s);
              searching.remove(s);
              searching.remove(socket);
              DataOutputStream os = new DataOutputStream(socket.getOutputStream());
              dos.writeUTF("server#Stranger Matched!");
              os.writeUTF("server#Stranger Matched");
            }
            while (pair.get(s) == null) {
              dos.writeUTF("server#Serching...");
              Thread.sleep(1000);
            }
            break;

          case "leave":
            variablesCorrection(s);
            loadRoomList(s);
            break;

          case "quit":
            Server.variablesCorrection(s);
            s.close();
            break;

          case "roomls":
            loadRoomList(s);
            break;

          case "ls":
            for (String cmd : commands) {
              dos.writeUTF(cmd);
            }
            break;

          case "status":
            dos.writeUTF("server#Clients Connected: " + userCount);
            dos.writeUTF("server#Numbers of Room: " + roomCount);
            break;

          default:
            dos.writeUTF("server#Wrong Command!");
            break;
        }
      } else if (magicWord.equalsIgnoreCase("cmd") && action == null) {
        dos.writeUTF("server#Wrong Command!");
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
        } catch (NullPointerException e) {
          dos.writeUTF("server#You need to join/create a room or match with someone to chat!");
        }
      }
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
