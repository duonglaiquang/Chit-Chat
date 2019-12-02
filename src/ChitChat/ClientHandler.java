package ChitChat;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler implements Runnable {
  final DataOutputStream dos;
  final DataInputStream dis;
  final Socket s;

  public ClientHandler(Socket s) throws IOException {
    this.s = s;
    this.dis = new DataInputStream(s.getInputStream());
    this.dos = new DataOutputStream(s.getOutputStream());
  }

  @Override
  public void run() {
    Thread thReceiver;
    thReceiver = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          while (true) {
            synchronized (this) {
              String strReceived;
              strReceived = dis.readUTF();
              Server.checkCommand(strReceived, s, dos);
            }
          }

        } catch (EOFException | NullPointerException e) {
          Server.userCount--;
          System.out.println("Client disconnected abruptly!");
          try {
            Server.variablesCorrection(s);
          } catch (IOException | NullPointerException ex) {
            ex.printStackTrace();
          }
        } catch (SocketException e) {
          System.out.println("Client disconnected!");
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
    thReceiver.start();
  }
}
