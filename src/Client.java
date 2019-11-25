import java.io.*;
import java.net.*;

public class Client {
  public static void main(String[] args)throws IOException, InterruptedException {
    //create socket and get ip

    Socket s = new Socket("localhost",1234);
    BufferedReader br=new BufferedReader(new InputStreamReader(System.in));

    //create sender thread
    Thread thSender;
    thSender = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          while(true){
            synchronized (this){
              //scan new message to send
              String strSend = br.readLine();
              DataOutputStream dos = new DataOutputStream(s.getOutputStream());
              dos.writeUTF(strSend);
              dos.flush();
              if(strSend.equals("bye")){
                System.out.println("Client exiting ...");
                break;
              }
              System.out.println("Waiting for server response ...");
            }
          }
        } catch (Exception e){
          System.out.println("Exception occured");
          e.printStackTrace();
        }
      }
    });

    Thread thReceive;
    thReceive = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          while (true){
            synchronized (this){
              DataInputStream dis = new DataInputStream(s.getInputStream());
              String strReceive;
              strReceive = dis.readUTF();
              System.out.println("Server say: "+strReceive);
              if(strReceive.equals("bye")){
                System.out.println("Connection Closed");
                break;
              }
            }
          }
        } catch (Exception e){
          System.out.println("Exception occured");
          e.printStackTrace();
        }
      }
    });

    thSender.start();
    thReceive.start();

    thSender.join();
    thReceive.join();
  }
}
