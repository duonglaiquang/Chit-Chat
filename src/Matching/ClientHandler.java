package Matching;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
  final DataOutputStream dos;
  final DataInputStream dis;
  final Socket s;
  public ClientHandler pair = null;

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
              pair.dos.writeUTF(strReceived);
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });

    thReceiver.start();
    try {
      thReceiver.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
