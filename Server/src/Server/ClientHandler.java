package Server;

import CustomClass.SerializableImage;

import java.io.EOFException;
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
    oos.writeObject("server#Connected#" + Server.userCount);
  }

  @Override
  public void run() {
    Thread thReceiver;
    thReceiver = new Thread(() -> {
      try {
        while (true) {
          Object obj = ois.readObject();
          if (obj != null) {
            if (obj instanceof String) {
              String strReceived;
              strReceived = (String) obj;
              System.out.println(strReceived);

              Thread newThread = new Thread(() -> {
                try {
                  Server.checkCommand(strReceived, s, oos);
                } catch (IOException e) {
                  System.out.println("Exception check command thread!");
                }
              });

              newThread.start();
            } else if (obj instanceof SerializableImage) {

              Thread newThread = new Thread(() -> {
                try {
                  Server.transportMsg(s, obj);
                } catch (IOException e) {
                  System.out.println("Exception send image thread!");
                }
              });

              newThread.start();
            }
          }
        }
      } catch (EOFException e) {
        System.out.println("EOFE!");
        Thread newThread = new Thread(() -> {
          try {
            Server.quit(s, oos);
          } catch (IOException ignored) {
          }
        });
        newThread.start();

      } catch (NullPointerException | ClassNotFoundException | IOException e) {
        System.out.println("Exception in handler!");
        Thread newThread = new Thread(() -> {
          try {
            Server.quit(s, oos);
          } catch (IOException ignored) {
          }
        });
        newThread.start();
      }
    });

    thReceiver.start();
  }
}
