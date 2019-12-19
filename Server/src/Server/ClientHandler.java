package Server;

import ChatRoom.SerializableImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
  final ObjectOutputStream oos;
  final ObjectInputStream ois;
  final Socket s;

  public ClientHandler(Socket s) throws IOException {
    this.s = s;
    this.ois = new ObjectInputStream(s.getInputStream());
    this.oos = new ObjectOutputStream(s.getOutputStream());
    Server.oosOf.put(s, oos);
    oos.writeObject("server#Connected#"+Server.userCount);
  }

  @Override
  public void run() {
    Thread thReceiver;
    thReceiver = new Thread(() -> {
      try {
        while (true) {
          Object obj = ois.readObject();
          if(obj != null) {
            if (obj instanceof String) {
              String strReceived;
              strReceived = (String) obj;
              System.out.println(strReceived);
              Server.checkCommand(strReceived, s, oos);
            } else if (obj instanceof SerializableImage){
              Server.transportMsg(s, obj);
            }
          }
        }
      } catch (NullPointerException | IOException | ClassNotFoundException ignored) {
      }
    });

    thReceiver.start();
  }
}
