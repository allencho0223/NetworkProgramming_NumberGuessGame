import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class GuessGameThread extends Thread {

   // Declare global field objects
   protected Socket playerSocket = null;
   protected int playerIndex = 0;

   protected static StringBuilder secretCode = new StringBuilder();

   protected static GameConfig gc = new GameConfig();

   protected static CountDownLatch codeLatch = new CountDownLatch(1);

   /**
    * 
    * @param playerSocket
    *           - Accepted socket from the game lobby
    * @param playerIndex
    *           - Check the player index First player is in charge of deciding the
    *           secret code length The other players just wait for the secret code
    *           to be created
    */
   public GuessGameThread(Socket playerSocket, int playerIndex) {
      this.playerSocket = playerSocket;
      this.playerIndex = playerIndex;

   }

   public void run() {

      try {

         // Declare a local object to communicate with player socket
         ServerCoordinator sc = new ServerCoordinator(playerSocket);

         // Send registration message to the players
         sc.sendMsgToPlayer("Register with your first name");

         // Check if the name already exists in the server
         sc.checkPlayerAuthentication();

         // Send a specific message only to the first player
         if (this.playerIndex == 1) {
            sc.sendMsgToPlayer("Please enter the secret code length between 3 and 8");

            String codeLength = sc.receivePlayerInput();

            sc.sendMsgToPlayer("The secret code is being generated....");

            secretCode = sc.generateSecretCode(codeLength);
            codeLatch.countDown();
            sc.sendMsgToPlayer(
                  "The secret code has been successfully generated with " + secretCode.length() + " digits!");

            System.out.println("[SERVER SAYS] >> The secret code is " + secretCode.toString());

            // Send a waiting message to the rest players
         } else if (this.playerIndex != 1) {
            sc.sendMsgToPlayer("Enter any key to get ready for the game");

            if (sc.receivePlayerInput() != null) {
               sc.sendMsgToPlayer("The secret code is being generated....");
            }
            try {
               codeLatch.await();
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
            sc.sendMsgToPlayer(
                  "The secret code has been successfully generated with " + secretCode.length() + " digits!");
         }

         // Start the game
         startGuessGame(sc);

      } catch (IOException e) {
         System.err.println(e.getLocalizedMessage());
      }
   }

   @SuppressWarnings("static-access")
   private void startGuessGame(ServerCoordinator sc) {

      // Declare local variables
      int playerTurn = 1;
      int maxGuess = 10;
      String playerResp = "";
      try {

         // Send players to let them realise the length of the secret code
         sc.sendMsgToPlayer(Integer.toString(secretCode.length()));

         // Send round 1 message
         sc.sendMsgToPlayer("Round " + sc.gc.getPlayerTurn() + "\n");

         /**
          * This while loop works in different ways 
          *    1. If player turn reaches out to the max guess (10) 
          *    2. If a player forfeits 
          *    3. If a player corrects the secret code
          */
         while (playerTurn <= maxGuess) {

            // Gets into the statement if the game is not finished
            if (!sc.gc.getIsEnd()) {

               // Receive player guess code
               playerResp = sc.receivePlayerInput();

               // Check if user wants to forfeit
               if (playerResp.contains("I want to forfeit this game")) {
                  // Set all the conditions as the loser
                  sc.gc.setIsEnd(true);
                  sc.gc.setPlayerScore(0);
                  sc.gc.setPlayerTurn(11);
                  sc.gc.setIsPlayerDone(sc.gc.getIsPlayerDone() + 1);
                  sc.playerRankings.replace(sc.checkNameAuth, sc.gc.getPlayerScore());
               } else {
                  // Compare the user guess code with the secret code
                  sc.compareSecretCode(playerResp);
               }

               // Gets into if the game is not finished, and user could not guess the code correctly
               if (!sc.gc.getIsEnd()) {
                  sc.gc.setPlayerTurn((sc.gc.getPlayerTurn() + 1));
                  sc.gc.setPlayerScore((sc.gc.getPlayerScore()) - 10);
                  sc.playerRankings.replace(sc.checkNameAuth, sc.gc.getPlayerScore());
               }
            }

            // Gets into If the user finishes the game
            if (sc.gc.getIsEnd()) {
               // Send rankings to all the users joined the game
               if (sc.gc.getIsPlayerDone() == GameLobby.playerNum) {
                  synchronized (gc.lock) {
                     gc.lock.notifyAll();
                     sc.sendGameRankings();
                     break;
                  }
               } else {
                  // Wait until every player finishes their game
                  synchronized (gc.lock) {
                     try {
                        gc.lock.wait();
                     } catch (InterruptedException e) {
                        System.err.println(e.getLocalizedMessage());
                     }

                  }
               }

            }

            // Increment player turn
            playerTurn++;
         }
      } catch (IOException e) {
         System.err.println(e.getLocalizedMessage());
      }
   }
}
