package RoomChat;

import java.io.*;
import java.net.*;
import java.util.StringTokenizer;

public class Client {
  public static void main(String[] args) throws IOException {
    new Client();
  }

  public Client() throws IOException {
    //create socket and get ip
    Socket s = new Socket("localhost", 1234);
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    DataOutputStream dos = new DataOutputStream(s.getOutputStream());
    DataInputStream dis = new DataInputStream(s.getInputStream());
    //create sender thread

    Thread thSender;
    thSender = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          while (true) {
            synchronized (this) {
              //scan new message to send
              String strSend = br.readLine();
              if (strSend != null) {
                dos.writeUTF(strSend);
              }
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

    Thread thReceiver;
    thReceiver = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          while (true) {
            synchronized (this) {
              String strReceived;
              strReceived = dis.readUTF();
              StringTokenizer st = new StringTokenizer(strReceived, "#");
              String from = st.nextToken();
              if (from.equals("server"))
                System.err.println(st.nextToken());
              else System.out.println(strReceived);
            }
          }
        } catch (Exception e) {
          System.out.println("Exception occured");
          e.printStackTrace();
        }
      }
    });

    thSender.start();
    thReceiver.start();
  }
}
