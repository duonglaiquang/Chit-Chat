package Server;

import ChatRoom.SerializableImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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
//
//    Thread thImageReceiver;
//    thImageReceiver = new Thread(() -> {
//      try {
//        while (true) {
//          BufferedImage image = ImageIO.read(ois);
//          if(image != null){
//            System.out.println(image);
//          }
//        }
//      } catch(IOException e){
//        e.printStackTrace();
//      }
//    });

    thReceiver.start();
//    thImageReceiver.start();
  }
}
