package Server;

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
    oos.writeObject("server#Connected#");
  }

  @Override
  public void run() {
    Thread thReceiver;
    thReceiver = new Thread(() -> {
      try {
        while (true) {
//            synchronized (this) {
          String strReceived;
          strReceived = (String) ois.readObject();
          System.out.println(strReceived);
          Server.checkCommand(strReceived, s, oos);
//            }
        }

      } catch (EOFException | NullPointerException e) {
        System.out.println("Client disconnected!");
//        try {
//          Server.leaveChat(s, oos);
//        } catch (IOException ex) {
//          System.out.println("Exception in correction method!");
//        }
      } catch (IOException | ClassNotFoundException e) {
//        try {
//          Server.leaveChat(s, oos);
//        } catch (IOException ex) {
//          System.out.println("Exception in correction method!");
//        }
      }
    });

    thReceiver.start();
  }
}
