package CustomClass;

import java.io.Serializable;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ChatRoom implements Serializable {
  public final static int MAX_CLIENT = 10;
  public Integer id;
  public String name;
  public String description;
  public AtomicInteger clientCount = new AtomicInteger(0);
  public Color[] color = new Color[MAX_CLIENT];
  public transient List<Socket> sockets = Collections.synchronizedList(new ArrayList<>());
  public transient String[] colorName = {"red", "blue", "orange", "green", "black", "purple", "pink", "magenta", "lime", "cyan"};
  public transient Map<Socket, Color> colorOf = Collections.synchronizedMap(new HashMap<>());

  public ChatRoom(Integer id, String name, String description) {
    this.id = id;
    this.name = name;
    this.description = description;
    for (int i = 0; i < MAX_CLIENT; i++) {
      color[i] = new Color(i, colorName[i]);
    }
  }
}
