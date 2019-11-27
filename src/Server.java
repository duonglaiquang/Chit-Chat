import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.LinkedList;

public class Server {
  //store handler list
  static LinkedList<ClientHandler> handlers = new LinkedList<>();
  // count clients
  static int userCount = 0;

  public static void main(String[] args) throws IOException {
    new Server();
  }
  //create socket and get ip

  public Server() throws IOException {
    try (ServerSocket ss = new ServerSocket(1234)) {
      System.out.println("Running Server");
      Socket s = null;

      while (true) {
        s = ss.accept();
        userCount++;
        System.out.println("New Client Connected " + s);
        ClientHandler handler = new ClientHandler(s);
        handlers.add(handler);
        Thread t = new Thread(handler);
        t.start();

        if (userCount % 2 == 0) {
          handlers.get(userCount - 2).pair = handler;
          handler.pair = handlers.get(userCount - 2);
          System.out.println("2 client has been matched");
          handlers.get(userCount - 2).dos.writeUTF("Matched!\n");
          handlers.get(userCount - 1).dos.writeUTF("Matched!\n");
        } else {
          handler.dos.writeUTF("Searching for stranger...");
        }
      }
    }
  }
}
