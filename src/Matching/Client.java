package Matching;
import java.io.*;
import java.net.*;

public class Client {
  public static void main(String[] args) throws IOException, InterruptedException {
    new Client();
  }

  public Client() throws IOException, InterruptedException {
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
              System.out.println("You: " + strSend);
              dos.writeUTF(strSend);
              if(strSend.equals("bye")){
                s.close();
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
              System.out.println("Stranger: " + strReceived);
              if(strReceived.equals("bye")){
                s.close();
              }
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

    thSender.join();
    thReceiver.join();
  }
}
