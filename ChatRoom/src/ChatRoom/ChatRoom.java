package ChatRoom;

import java.io.Serializable;
import java.net.Socket;
import java.util.LinkedList;

public class ChatRoom implements Serializable{
  public Integer id;
  public String name;
  public String description;
  public Integer clientCount = 0;
  public transient LinkedList<Socket> sockets = new LinkedList<>();

  public ChatRoom(Integer id, String name, String description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }
}
