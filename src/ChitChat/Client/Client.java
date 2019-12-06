package ChitChat.Client;

import ChitChat.Main;

import java.io.*;
import java.net.*;
import java.util.StringTokenizer;
public class Client {
  public static Socket s;

  public static DataOutputStream dos;
  public static DataInputStream dis;

  public void request(String action) throws IOException {
    dos.writeUTF("request#" + action);
  }

  public void start() throws IOException {
    //create socket and get ip
    s = new Socket("192.168.110.122", 1234);
    dos = new DataOutputStream(s.getOutputStream());
    dis = new DataInputStream(s.getInputStream());
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    //create sender thread
    Thread thSender = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          while (true) {
//            synchronized (this) {
            //scan new message to send
            String strSend = br.readLine();
            if (strSend != null) {
              dos.writeUTF(strSend);
              dos.flush();
            }
//            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

    Thread thReceiver = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          while (true) {
//            synchronized (this) {
            String strReceived;
            strReceived = dis.readUTF();
            StringTokenizer st = new StringTokenizer(strReceived, "#");
            String from = st.nextToken();

            if (from.equals("server")) {
              String target = st.nextToken();
              if (target.equals("Matched")) {
                Main.sc.matched();
              } else if (target.equals("SearchCanceled")) {
                Main.rc.changeScene("root");
                System.out.println("Search Canceled!");
              } else System.out.println(target);
            } else System.out.println(strReceived);
//            }
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
