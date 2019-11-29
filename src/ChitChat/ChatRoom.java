package ChitChat;

import java.net.Socket;
import java.util.LinkedList;

public class ChatRoom {
  Integer id;
  String name;
  Integer clientCount = 0;
  LinkedList<Socket> sockets = new LinkedList<>();

  public ChatRoom(Integer id, String name){
    this.id = id;
    this.name = name;
  }
}
