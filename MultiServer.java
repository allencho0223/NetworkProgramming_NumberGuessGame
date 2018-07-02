import java.io.*;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MultiServer {

   // Declare global fields and objects
   protected static final int PORT = 18745;
   protected static final int LOBBY = 1;

   protected static PrintWriter commLogWriter = null;
   protected static FileWriter commLogFile = null;

   public static void main(String[] args) {

      ServerSocket serverSocket = null;

      try {
         // Define server socket with the port and lobby number (1)
         serverSocket = new ServerSocket(PORT, LOBBY);

         // Define log writer
         commLogFile = new FileWriter("multi_communication_log.txt");
         commLogWriter = new PrintWriter(commLogFile, true);

         // Display and write the log into the file
         System.out.println("[SERVER SAYS] >> Server running on port " + serverSocket.getLocalPort());
         writeCommLog("[SERVER SAYS] >> Server running on port " + serverSocket.getLocalPort());
         writeCommLog(getTimeLog());

         System.out.println("[SERVER SAYS] >> Waiting for player connections...");
         writeCommLog("[SERVER SAYS] >> Waiting for player connections...");
         writeCommLog(getTimeLog());

         // Open the game lobby
         GameLobby gameLobby = new GameLobby(serverSocket , commLogWriter);
         System.out.println("[SERVER SAYS] >> The game lobby is now open");
         writeCommLog("[SERVER SAYS] >> The game lobby is now open");
         writeCommLog(getTimeLog());
         
         // Start the game
         gameLobby.start();

      } catch (IOException e) {
         System.err.println(e.getLocalizedMessage());
      }

   }

   public static synchronized void writeCommLog(String msg) {

      // Write communication log between the server and players into the text file
      commLogWriter.println(msg);
   }

   public static String getTimeLog() {
      // Check the time log
      SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
      Date date = new Date();
      return "[SERVER SAYS] >> Current time log " + formatter.format(date).toString() + "\n";
   }

}
