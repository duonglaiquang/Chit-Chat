package ChitChat.TestClient;

import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

public class Client {
  static Socket s;

  public void requestMatch() throws IOException {
    DataOutputStream dos = new DataOutputStream(s.getOutputStream());
    dos.writeUTF("request#match");
    dos.flush();
  }

  public static void main(String[] args) throws IOException {
    //create socket and get ip
    s = new Socket("192.168.110.122", 1234);
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
              System.out.println(strReceived);
            }
          }
        } catch (EOFException e) {
          System.out.println("Disconnected!");
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });

    thSender.start();
    thReceiver.start();
  }
}
