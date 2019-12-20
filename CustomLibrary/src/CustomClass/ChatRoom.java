package CustomClass;

import java.io.Serializable;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;

public class ChatRoom implements Serializable {
  public final static int MAX_CLIENT = 10;
  public Integer id;
  public String name;
  public String description;
  public Integer clientCount = 0;
  public Color[] color = new Color[MAX_CLIENT];
  public transient LinkedList<Socket> sockets = new LinkedList<>();
  public transient String[] colorName = {"white", "beige", "orange", "lightblue", "yellow", "lightgreen", "pink", "green", "lime", "cyan"};
  public transient HashMap<Socket, String> colorOf = new HashMap<>();

  public ChatRoom(Integer id, String name, String description) {
    this.id = id;
    this.name = name;
    this.description = description;
    for (int i = 0; i < MAX_CLIENT; i++) {
      color[i] = new Color(i, colorName[i]);
    }
  }
}
