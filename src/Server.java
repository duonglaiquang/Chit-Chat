import java.io.*;
import java.net.*;

public class Server {
  public static void main(String[] args)throws IOException, InterruptedException {
    //create socket and get ip
    ServerSocket ss = new ServerSocket(1234);
    System.out.println("Running Server");

    Socket s = ss.accept();
    System.out.println("Client Connected "+ s);

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
                System.out.println("Server exiting ...");
                break;
              }
              System.out.println("Waiting for client response ...");
            }
          }
        } catch (IOException e){
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
              System.out.println("Client say: "+strReceive);
              if(strReceive.equals("bye")){
                System.out.println("Connection Closed");
                break;
              }
            }
          }
        } catch (IOException e){
          e.printStackTrace();
        }
      }
    });

    thReceive.start();
    thSender.start();

    thSender.join();
    thReceive.join();
  }
}
