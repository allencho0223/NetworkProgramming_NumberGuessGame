import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class GameLobby extends Thread {

   // Declare global field variables and objects for use
   protected ServerSocket serverSocket = null;

   protected Socket firstPlayer = null;
   protected Socket secondPlayer = null;
   protected Socket lastPlayer = null;

   protected static int player1Index = 1;
   protected static int player2Index = 2;
   protected static int player3Index = 3;

   protected static int playerNum = 0;
   protected final int maxLobbySize = 3;

   protected static MultiServer ms = new MultiServer();

   protected static ServerCoordinator sc = new ServerCoordinator();

   protected PrintWriter commLogWriter = null;

   // Constructor
   public GameLobby(ServerSocket serverSocket, PrintWriter commLogWriter) {
      this.serverSocket = serverSocket;
      this.commLogWriter = commLogWriter;
   }

   // Run method
   @SuppressWarnings("static-access")
   public void run() {

      Socket playerSocket = null;

      String input = "";

      // Define reader object to use for replaying game and
      // accepting more players in a game
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      try {

         // Loops until user types N to finish the game
         while (!input.equals("N")) {

            // Be able to accept unless user types "N" to quit or
            // the player number reaches out to the max lobby size
            while (playerNum < maxLobbySize) {

               // Initialise the number of players
               initPlayers(playerSocket);

               // Increment the player number
               playerNum++;

               System.out.print("[SYSTEM SAYS] >> Would you like to wait for another player (Y/N)? ");
               commLogWriter.println(("[SYSTEM SAYS] >> Would you like to wait for another player (Y/N)? "));

               // Ascertain if more players are required to play
               input = reader.readLine();
               while (!input.equals("Y") && !input.equals("N")) {
                  System.out.println("[SYSTEM SAYS] >> Input must be either Y or N");
                  System.out.print("[SYSTEM SAYS] >> Would you like to wait for another player (Y/N)? ");
                  input = reader.readLine();
               }

               // Check the user's response
               if (input.equals("N")) {
                  commLogWriter.println("[PLAYER SAYS] >> N");
                  break;
               } else if (input.equals("Y")) {
                  commLogWriter.println("[PLAYER SAYS] >> Y");
                  System.out.print("[SYSTEM SAYS] >> The server is waiting for other players...");
                  commLogWriter.println("[SYSTEM SAYS] >> The server is waiting for other players...");
                  commLogWriter.println(ms.getTimeLog());
               }

               System.out.println();
            }

            // Run the game thread
            runGameThread();

            // Ask the user if s/he wants to play another game
            System.out.print("[SYSTEM SAYS] >> Would you like to play another game (Y/N)? ");
            commLogWriter.println("[SYSTEM SAYS] >> Would you like to play another game (Y/N)?");
            commLogWriter.println(ms.getTimeLog());
            input = reader.readLine();
            while (!input.equals("Y") && !input.equals("N")) {
               System.out.println("[SYSTEM SAYS] >> Input must be either Y or N");
               input = reader.readLine();
            }
            if (input.equals("N")) {
               commLogWriter.println("[PLAYER SAYS] >> N");
               break;
            } else if (input.equals("Y")) {
               commLogWriter.println("[PLAYER SAYS] >> Y");
               System.out.print("[SYSTEM SAYS] >> The server is waiting for another game to get started...");
               commLogWriter.println("[SYSTEM SAYS] >> The server is waiting for another game to get started...");
               commLogWriter.println(ms.getTimeLog());
               resetConditions();
            }
         }

         // If the player says "N", terminate the server
         System.out.println("[SYSTEM SAYS] >> The server has been terminated");
         commLogWriter.println("[SYSTEM SAYS] >> The server has been terminated");
         commLogWriter.println(ms.getTimeLog());

      } catch (IOException e) {
         System.err.println(e.getLocalizedMessage());
      } finally {
         try {
            // Close streams
            closeStreams(firstPlayer, secondPlayer, lastPlayer, serverSocket);
            ServerCoordinator.closeGameStreams(sc.playerInputReader, sc.serverWriter, sc.gameLogWriter, sc.gameLogFile);

         } catch (IOException e) {
            e.printStackTrace();
         }
      }

   }

   @SuppressWarnings("static-access")
   public void initPlayers(Socket playerSocket) throws IOException {

      // Accept player socket
      playerSocket = serverSocket.accept();
      System.out.println("[SERVER SAYS] >> Player has been connected to the server!");
      commLogWriter.println("[SERVER SAYS] >> Player has been connected to the server!");
      commLogWriter.println(ms.getTimeLog());

      if (firstPlayer == null) {
         // Accept the first player
         firstPlayer = playerSocket;

         // Display player's details and write down into the log file
         System.out.println("[SERVER SAYS] >> First player on " + firstPlayer.getRemoteSocketAddress());
         commLogWriter.println("[SERVER SAYS] >> First player on " + firstPlayer.getRemoteSocketAddress());

         System.out.println("[SERVER SAYS] >> The player host name " + firstPlayer.getInetAddress().getHostName());
         commLogWriter.println("[SERVER SAYS] >> The player host name " + firstPlayer.getInetAddress().getHostName());

         commLogWriter.println(ms.getTimeLog());

      } else if (secondPlayer == null) {
         // Accept the second player
         secondPlayer = playerSocket;

         // Display player's details and write down into the log file
         System.out.println("[SERVER SAYS] >> Second player on " + secondPlayer.getRemoteSocketAddress());
         commLogWriter.println("[SERVER SAYS] >> Second player on " + secondPlayer.getRemoteSocketAddress());

         System.out.println("[SERVER SAYS] >> The player host name " + secondPlayer.getInetAddress().getHostName());
         commLogWriter.println("[SERVER SAYS] >> The player host name " + secondPlayer.getInetAddress().getHostName());

         commLogWriter.println(ms.getTimeLog());
      } else if (lastPlayer == null) {
         // Accept the last player
         lastPlayer = playerSocket;

         // Display player's details and write down into the log file
         System.out.println("[SERVER SAYS] >> Third player on" + lastPlayer.getRemoteSocketAddress());
         commLogWriter.println("[SERVER SAYS] >> Third player on" + lastPlayer.getRemoteSocketAddress());

         System.out.println("[SERVER SAYS] >> The player host name " + lastPlayer.getInetAddress().getHostName());
         commLogWriter.println("[SERVER SAYS] >> The player host name " + lastPlayer.getInetAddress().getHostName());

         commLogWriter.println(ms.getTimeLog());
      }
   }

   @SuppressWarnings("static-access")
   public void runGameThread() {
      if (playerNum == 1) {

         // Run single player game - only 1 player socket has been accepted
         GuessGameThread player1Thread = new GuessGameThread(firstPlayer, player1Index);
         player1Thread.start();
         try {
            System.out.println("[SERVER SAYS] >> Game starts with " + playerNum + " player");
            commLogWriter.println("[SERVER SAYS] >> Game starts with " + playerNum + " player");
            commLogWriter.println(ms.getTimeLog());
            player1Thread.join();

         } catch (InterruptedException e) {
            e.printStackTrace();
         }

      } else if (playerNum == 2) {

         // Run multi player game - 2 player sockets have been accepted
         GuessGameThread player1Thread = new GuessGameThread(firstPlayer, player1Index);
         GuessGameThread player2Thread = new GuessGameThread(secondPlayer, player2Index);
         player1Thread.start();
         player2Thread.start();
         try {
            System.out.println("[SERVER SAYS] >> Game start with " + playerNum + " players");
            commLogWriter.println("[SERVER SAYS] >> Game starts with " + playerNum + " players");
            commLogWriter.println(ms.getTimeLog());
            player1Thread.join();
            player2Thread.join();

         } catch (InterruptedException e) {
            e.printStackTrace();
         }

      } else if (playerNum == 3) {

         // Run triple player game - 3 player sockets have been accepted
         GuessGameThread player1Thread = new GuessGameThread(firstPlayer, player1Index);
         GuessGameThread player2Thread = new GuessGameThread(secondPlayer, player2Index);
         GuessGameThread player3Thread = new GuessGameThread(lastPlayer, player3Index);
         player1Thread.start();
         player2Thread.start();
         player3Thread.start();
         try {
            System.out.println("[SERVER SAYS] >> Game start with " + playerNum + " players");
            commLogWriter.println("[SERVER SAYS] >> Game starts with " + playerNum + " players");
            commLogWriter.println(ms.getTimeLog());
            player1Thread.join();
            player2Thread.join();
            player3Thread.join();

         } catch (InterruptedException e) {
            e.printStackTrace();
         }

      }
   }

   // Close streams after use
   private void closeStreams(Socket firstPlayer, Socket secondPlayer, Socket lastPlayer, ServerSocket serverSocket)
         throws IOException {

      if (firstPlayer != null) {
         firstPlayer.close();
      }
      if (secondPlayer != null) {
         secondPlayer.close();
      }
      if (lastPlayer != null) {
         lastPlayer.close();
      }
      if (serverSocket != null) {
         serverSocket.close();
      }

   }

   @SuppressWarnings("static-access")
   public void resetConditions() {

      GameConfig gc = new GameConfig();

      // Assign null to players previously played for next game
      if (firstPlayer != null) {
         firstPlayer = null;
      }
      if (secondPlayer != null) {
         secondPlayer = null;
      }
      if (lastPlayer != null) {
         lastPlayer = null;
      }

      // Reset all the conditions for next game - avoid any conflicts
      GuessGameThread.secretCode = new StringBuilder();

      GuessGameThread.codeLatch = new CountDownLatch(1);

      gc.setIsPlayerDone(0);

      playerNum = 0;
      sc.playerRankings.clear();
      sc.playerAuth.clear();
   }

}
