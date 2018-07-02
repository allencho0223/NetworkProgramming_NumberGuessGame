import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ServerCoordinator {

   // Declare global fields and objects
   protected BufferedReader playerInputReader = null;
   protected PrintWriter serverWriter = null;

   protected static PrintWriter gameLogWriter = null;
   protected static FileWriter gameLogFile = null;

   protected Socket playerSocket = null;

   protected GameConfig gc = new GameConfig();

   protected static List<String> playerAuth = new ArrayList<String>();

   protected static Map<String, Integer> playerRankings = new HashMap<String, Integer>();

   protected boolean isCorrect = false;

   protected String checkNameAuth = null;

   /**
    * 
    * @param playerSocket
    *           - Get the socket for appropriate socket communication
    */
   public ServerCoordinator(Socket playerSocket) {
      this.playerSocket = playerSocket;
      try {
         // Define objects for proper functionalities
         playerInputReader = new BufferedReader(new InputStreamReader(this.playerSocket.getInputStream()));
         serverWriter = new PrintWriter(this.playerSocket.getOutputStream(), true);

         gameLogFile = new FileWriter("multi_game_log.txt");
         gameLogWriter = new PrintWriter(gameLogFile, true);
      } catch (IOException e) {
         System.err.println(e.getLocalizedMessage());
      }
   }

   // Another constructor for flexible use
   public ServerCoordinator() {

   }

   // Write game log into a text file
   public static synchronized void writeGameLog(String msg) {
      gameLogWriter.println(msg);
   }

   // Send messages to the players and write them down into the log file
   public void sendMsgToPlayer(String msg) {
      serverWriter.println("[SERVER SAYS] >> " + msg);
      writeGameLog("[SERVER SAYS] >> " + msg);
   }

   // Receive player input and document
   public String receivePlayerInput() throws IOException {

      String playerResp = "";

      playerResp = playerInputReader.readLine();
      if (checkNameAuth == null) {
         System.out.println("[UNKNOWN PLAYER RESPONDS] >> " + playerResp);
         writeGameLog("[UNKNOWN PLAYER RESPONDS] >> " + playerResp);
      } else {
         System.out.println("[PLAYER " + checkNameAuth + " RESPONDS] >> " + playerResp);
         writeGameLog("[PLAYER " + checkNameAuth + " RESPONDS] >> " + playerResp);
      }

      return playerResp;
   }

   // Check user name authenticity
   public void checkPlayerAuthentication() throws IOException {

      while (true) {
         checkNameAuth = receivePlayerInput();
         if (!playerAuth.contains(checkNameAuth)) {

            // Add the user name into playerAuth (ArrayList), and playerRankins (HashMap)
            // for use
            playerAuth.add(checkNameAuth);
            playerRankings.put(checkNameAuth, gc.getPlayerScore());

            sendMsgToPlayer("Player " + checkNameAuth + " has been successfully registered in the server!");
            writeGameLog(MultiServer.getTimeLog());
            break;
         } else {
            // Send invalid messages if somebody has already taken the user name
            sendMsgToPlayer("Someone has already taken that name! Try again.");
            sendMsgToPlayer("Register with your first name");
            writeGameLog(MultiServer.getTimeLog());
         }
      }

   }

   public StringBuilder generateSecretCode(String length) {

      try {
         // Declare objects and variables
         int codeLength = Integer.parseInt(length);
         Random r = new Random();
         String code = "";
         int digit = 0;
         StringBuilder codeGenerator = new StringBuilder();
         int isGenerated = 0;

         // Loop until secret code is successfully generated
         while (isGenerated != codeLength) {
            digit = r.nextInt(9);
            code = Integer.toString(digit);

            // Check duplicates
            if (codeGenerator.indexOf(code) == -1) {
               codeGenerator.append(code);
               isGenerated++;
            }
         }
         return codeGenerator;
      } catch (NumberFormatException nfe) {
         System.err.println(nfe.getLocalizedMessage());
      }
      return null;
   }

   public void compareSecretCode(String playerResp) {

      // Declare variables and objects
      List<Character> correctDigit = new ArrayList<Character>();
      List<Character> incorrectPos = new ArrayList<Character>();
      int correctGuess = 0;
      int incorrectGuess = 0;
      char tempChar = 'a';

      /**
       * Compare secret code with playerGuess Split and assign correct digits and
       * incorrect digits into separate array list to give player hints based on the
       * number of incorrect digits
       */
      for (int i = 0; i < GuessGameThread.secretCode.length(); i++) {
         // Same digit with the same position
         if (GuessGameThread.secretCode.toString().charAt(i) == playerResp.charAt(i)) {
            tempChar = playerResp.charAt(i);
            if (!correctDigit.contains(tempChar)) {
               correctDigit.add(playerResp.charAt(i));
               correctGuess++;
            }

         }

         for (int j = 0; j < playerResp.length(); j++) {
            // Same digit with wrong position
            if (GuessGameThread.secretCode.toString().charAt(i) == playerResp.charAt(j)) {

               incorrectPos.add(playerResp.charAt(j));
               incorrectGuess++;

            }
         }
      }

      // Delete redundant digits (already exists in correct digit array list) from
      // incorrect position
      for (int i = 0; i < correctDigit.size(); i++) {
         if (incorrectPos.contains(correctDigit.get(i))) {
            incorrectPos.remove(correctDigit.get(i));
            incorrectGuess--;
         }
      }

      // If the player guessed secret code incorrectly
      if (correctGuess == GuessGameThread.secretCode.length()) {
         // If the player corrected secret code
         isCorrect = true;
         returnEndMsg();

         // Else if user could not guess any single digit
      } else if (correctGuess == 0 && incorrectGuess == 0) {

         sendMsgToPlayer("You have no correct digit!");
         writeGameLog(MultiServer.getTimeLog());

         // Else - if user corrected some and incorrected others
      } else {
         sendMsgToPlayer("You have corrected " + correctGuess + " digits.");
         for (int i = 0; i < correctGuess; i++) {
            sendMsgToPlayer("Correct digit >> " + correctDigit.get(i));
         }
         for (int i = 0; i < incorrectGuess; i++) {
            sendMsgToPlayer("Correct digit with wrong position >> " + incorrectPos.get(i));
         }

         writeGameLog(MultiServer.getTimeLog());
      }

      // Calls if the game is in progress
      if (gc.getPlayerTurn() < gc.maxGuess && !gc.getIsEnd()) {
         sendMsgToPlayer("Next guess!");
         sendMsgToPlayer("Round " + (gc.getPlayerTurn() + 1) + "\n");
         writeGameLog(MultiServer.getTimeLog());
      }

      // Calls if the game has been finished and the user failed to guess the code
      if (gc.getPlayerTurn() == gc.maxGuess) {
         gc.setPlayerScore(gc.getPlayerScore() - 10);
         playerRankings.replace(checkNameAuth, gc.getPlayerScore());
         isCorrect = false;
         returnEndMsg();
      }
   }

   public void returnEndMsg() {

      // Failed to guess the code
      if (!isCorrect) {
         sendMsgToPlayer("You have reached out to the maximum guess.");
         sendMsgToPlayer("Your score is " + gc.getPlayerScore());
         sendMsgToPlayer("The secret code is " + GuessGameThread.secretCode.toString() + ".");
         sendMsgToPlayer("Exit the game.");
         gc.setIsEnd(true);
         gc.setIsPlayerDone(gc.getIsPlayerDone() + 1);
         writeGameLog(MultiServer.getTimeLog());
      
      // Succeeded to guess the code
      } else {
         sendMsgToPlayer("You have corrected the secret code. Congratulations!");
         sendMsgToPlayer("Your score is " + gc.getPlayerScore());
         sendMsgToPlayer("The secret code is " + GuessGameThread.secretCode.toString());
         sendMsgToPlayer("Exit the game.");
         gc.setIsEnd(true);
         gc.setIsPlayerDone(gc.getIsPlayerDone() + 1);
         writeGameLog(MultiServer.getTimeLog());
      }

   }

   // Send rankings to the players
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public void sendGameRankings() {

      // Wait until every player finishes their game
      synchronized (GuessGameThread.gc.lock) {
         System.out.println("[SYSTEM SAYS] >> Waiting for every player to finish their game");
      }

      // Sort the order or rankings based on their scores from the lowest to the highest
      sendMsgToPlayer("Every player has finished their games!");
      Object[] rankings = playerRankings.entrySet().toArray();
      Arrays.sort(rankings, new Comparator() {
         public int compare(Object o1, Object o2) {
            return ((Map.Entry<String, Integer>) o1).getValue().compareTo(((Map.Entry<String, Integer>) o2).getValue());
         }
      });
      for (Object ranking : rankings) {
         sendMsgToPlayer("Player " + ((Map.Entry<String, Integer>) ranking).getKey() + " achieved "
               + ((Map.Entry<String, Integer>) ranking).getValue() + " point!");
      }

      sendMsgToPlayer("Good job all!");

   }

   public static void closeGameStreams(BufferedReader playerInputReader, PrintWriter serverWriter,
         PrintWriter gameLogWriter, FileWriter gameLogFile) {

      // Close streams
      try {
         if (gameLogWriter != null) {
            gameLogWriter.close();
         }
         if (gameLogFile != null) {
            gameLogFile.close();
         }
         if (playerInputReader != null) {
            playerInputReader.close();
         }
         if (serverWriter != null) {
            serverWriter.close();
         }
      } catch (IOException e) {
         System.err.println(e.getLocalizedMessage());
      }

   }
}
