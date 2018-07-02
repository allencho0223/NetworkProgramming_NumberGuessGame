import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class SingleServer {

   // Declare field variables and objects
   public static final int PORT = 18745;
   public static final int BACKLOG = 1;
   
   public static StringBuilder secretCode = new StringBuilder();
   
   public static int playerTurn = 1;
   public static int maxGuess = 10;
   public static int playerScore = 100;
   public static boolean isCorrect = false;

   public static PrintWriter gameLogWriter = null;
   public static FileWriter gameLogFile = null;

   public static PrintWriter commLogWriter = null;
   public static FileWriter commLogFile = null;

   public static void main(String[] args) {

      // Declare variables and objects
      ServerSocket serverSocket = null;
      Socket playerSocket = null;

      BufferedReader playerInputReader = null;
      PrintWriter serverWriter = null;
      String playerResp = "";

      try {
         // Allow only 1 connection for single play game
         serverSocket = new ServerSocket(PORT, BACKLOG);

         // Declare constructors to write logs into text files
         gameLogFile = new FileWriter("single_game_log.txt");
         gameLogWriter = new PrintWriter(gameLogFile, true);

         commLogFile = new FileWriter("single_communication_log.txt");
         commLogWriter = new PrintWriter(commLogFile, true);

         // Display server and player (client) communication
         System.out.println("[SERVER SAYS] >> Server ruuning on port " + PORT);
         commLogWriter.println("[SERVER SAYS] >> Server ruuning on port " + PORT);

         System.out.println("[SERVER SAYS] >> Waiting for player connections....\n");
         commLogWriter.println("[SERVER SAYS] >> Waiting for player connections....\n");

         commLogWriter.println(getTimeLog());

         // Accept player connection
         playerSocket = serverSocket.accept();
         System.out.println("[SERVER SAYS] >> The player has been connected to the server!");
         commLogWriter.println("[SERVER SAYS] >> The player has been connected to the server!");

         System.out.println("[SERVER SAYS] >> The connected player on local port " + playerSocket.getPort());
         commLogWriter.println("[SERVER SAYS] >> The connected player on local port " + playerSocket.getPort());

         System.out.println("[SERVER SAYS] >> The player Inet address is " + playerSocket.getInetAddress());
         commLogWriter.println("[SERVER SAYS] >> The player Inet address is " + playerSocket.getInetAddress());

         System.out
               .println("[SERVER SAYS] >> The player local socket address is " + playerSocket.getLocalSocketAddress());
         commLogWriter
               .println("[SERVER SAYS] >> The player local socket address is " + playerSocket.getLocalSocketAddress());

         System.out.println("[SERVER SAYS] >> Player has been connected to the server!");
         commLogWriter.println("[SERVER SAYS] >> Player has been connected to the server!");

         commLogWriter.println(getTimeLog());

         // Declare objects to communicate with the server
         playerInputReader = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
         serverWriter = new PrintWriter(playerSocket.getOutputStream(), true);

         // Initialise the guess game
         initGuessGame(playerInputReader, serverWriter, playerResp);

         // Start the game
         startGuessGame(playerInputReader, serverWriter, playerResp);

      } catch (IOException ioe) {
         System.err.println(ioe.getLocalizedMessage());
      } finally {
         closeStreams(playerInputReader, serverWriter, playerSocket, serverSocket, gameLogWriter, gameLogFile, commLogWriter, commLogFile);
      }
   }

   public static void initGuessGame(BufferedReader playerInputReader, PrintWriter serverWriter, String playerResp)
         throws IOException {

      // Send messages to player to welcome
      serverWriter.println("+=========================================================+");
      serverWriter.println("| Welcome to NP Guess Game Single Player Mode!            |");
      serverWriter.println("|    Semester 1, 2018, Network Programming Assignment 1   |");
      serverWriter.println("|       s3558745 - Minyoung Cho                           |");
      serverWriter.println("+=========================================================+");

      // Send message to the player to get valid code length
      serverWriter.println("[SERVER SAYS] >> Please enter the secret code length between 3 and 8");
      gameLogWriter.println("[SERVER SAYS] >> Please enter the secret code length between 3 and 8");
      gameLogWriter.println(getTimeLog());

      // Read valid digit length from the player
      playerResp = playerInputReader.readLine();
      System.out.println("[PLAYER RESPONDS] >> " + playerResp);
      gameLogWriter.println("[PLAYER RESPONDS] >> " + playerResp);
      gameLogWriter.println(getTimeLog());

      // Generate the secret code based on the player response
      secretCode = generateSecretCode(playerResp);
      System.out.println("[SERVER SAYS] >> The secret code with " + playerResp + " digits has been successfully generated");
      
      gameLogWriter.println("[SERVER SAYS] >> The secret code with " + playerResp + " digits has been successfully generated");
      System.out.println("[SERVER SAYS] >> The secret code is " + secretCode.toString());
      
      gameLogWriter.println("[SERVER SAYS] >> The secret code is " + secretCode.toString());
      gameLogWriter.println(getTimeLog());

   }

   public static void startGuessGame(BufferedReader playerInputReader, PrintWriter serverWriter, String playerResp) {

      // Send start message to the player
      serverWriter.println("[SERVER SAYS] >> Let's start the game!");
      gameLogWriter.println("[SERVER SAYS] >> Let's start the game!");
      gameLogWriter.println(getTimeLog());
      
      try {

         while (playerTurn <= maxGuess) {
            // Read player's guess code and check if it's correct
            playerResp = playerInputReader.readLine();
            
            // If player sends message containing "Bye" at the end of the game,
            // Finish the game
            if (playerResp.contains("Bye!")) {
               break;
            }
            System.out.println("[PLAYER ATTEMP " + playerTurn + "] >> " + playerResp);
            gameLogWriter.println("[PLAYER ATTEMP " + playerTurn + "] >> " + playerResp);
            
            gameLogWriter.println(getTimeLog());
            
            compareSecretCode(serverWriter, playerResp);
         }

         // If player's turn reaches out to the max guess, send messages to the player
         if (playerTurn > maxGuess) {
            System.out.println("[SERVER SAYS] >> Player has reached out to the maximum guess!");
            commLogWriter.println("[SERVER SAYS] >> Player has reached out to the maximum guess!");
            
            System.out.println("[SERVER SAYS] >> Connection has been successfully terminated!");
            commLogWriter.println("[SERVER SAYS] >> Connection has been successfully terminated!");
            
            commLogWriter.println(getTimeLog());
            returnEndMsg(serverWriter);
         }
      } catch (IOException ioe) {
         System.err.println(ioe.getLocalizedMessage());
      }

   }

   private static void compareSecretCode(PrintWriter serverWriter, String playerResp) {

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
      for (int i = 0; i < secretCode.length(); i++) {
         // Same digit with the same position
         if (secretCode.toString().charAt(i) == playerResp.charAt(i)) {
            tempChar = playerResp.charAt(i);
            if (!correctDigit.contains(tempChar)) {
               correctDigit.add(playerResp.charAt(i));
               correctGuess++;
            }
         }

         for (int j = 0; j < playerResp.length(); j++) {
            // Same digit with wrong position
            if (secretCode.toString().charAt(i) == playerResp.charAt(j)) {
               incorrectPos.add(playerResp.charAt(j));
               incorrectGuess++;
            }
         }
      }

      // Remove redundant digits from incorrectPos 
      // (has the same value as corrected digits) 
      for (int i = 0; i < correctDigit.size(); i++) {
         if (incorrectPos.contains(correctDigit.get(i))) {
            incorrectPos.remove(correctDigit.get(i));
            incorrectGuess--;
         }
      }

      // If the player guessed secret code correctly
      if (correctGuess == secretCode.length()) {
         // If the player corrected secret code
         isCorrect = true;
         returnEndMsg(serverWriter);
         
         // If the player has 0 correct digit
      } else if (correctGuess == 0 && incorrectGuess == 0) {
         serverWriter.println("[SERVER SAYS] >> You have no correct digit!");
         gameLogWriter.println("[SERVER SAYS] >> You have no correct digit!");
         gameLogWriter.println(getTimeLog());
         
         // If the player has at least 1 correct digit with correct position,
         // or correct digit with wrong position
      } else {
         serverWriter.println("[SERVER SAYS] >> You have corrected " + correctGuess + " digits.");
         gameLogWriter.println("[SERVER SAYS] >> You have corrected " + correctGuess + " digits.");
         
         for (int i = 0; i < correctGuess; i++) {
            serverWriter.println("[SERVER SAYS] >> Correct digit >> " + correctDigit.get(i));
            gameLogWriter.println("[SERVER SAYS] >> Correct digit >> " + correctDigit.get(i));
            
         }
         for (int i = 0; i < incorrectGuess; i++) {
            serverWriter.println("[SERVER SAYS] >> Correct digit with wrong position >> " + incorrectPos.get(i));
            gameLogWriter.println("[SERVER SAYS] >> Correct digit with wrong position >> " + incorrectPos.get(i));
         }
         gameLogWriter.println(getTimeLog());
      }
      
      // If the user could not correct the secret code, send message for next turn
      if (playerTurn < maxGuess && !isCorrect) {
         serverWriter.println("[SERVER SAYS] >> Next guess!");
         gameLogWriter.println("[SERVER SAYS] >> Next guess!");
         
         gameLogWriter.println(getTimeLog());
      }
      
      // Increment turn value, and decrement score
      playerTurn++;
      playerScore -= 10;
   }

   public static StringBuilder generateSecretCode(String length) {

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

   public static void returnEndMsg(PrintWriter serverWriter) {

      // Failed to guess the code
      if (!isCorrect) {
         serverWriter.println("[SERVER SAYS] >> You have reached out to the maximum guess.");
         gameLogWriter.println("[SERVER SAYS] >> You have reached out to the maximum guess.");
         
         serverWriter.println("[SERVER SAYS] >> Your score is " + playerScore);
         gameLogWriter.println("[SERVER SAYS] >> Your score is " + playerScore);
         
         serverWriter.println("[SERVER SAYS] >> The secret code is " + secretCode.toString() + ".");
         gameLogWriter.println("[SERVER SAYS] >> The secret code is " + secretCode.toString() + ".");
         
         gameLogWriter.println(getTimeLog());
         
         serverWriter.println("[SERVER SAYS] >> Exit the game.");
         commLogWriter.println("[SERVER SAYS] >> Exit the game.");
         
         serverWriter.println("[SERVER SAYS] >> Connection has been successfully terminated!");
         commLogWriter.println("[SERVER SAYS] >> Connection has been successfully terminated!");
         
         commLogWriter.println(getTimeLog());
         
         // Succeeded to guess the code
      } else {
         serverWriter.println("[SERVER SAYS] >> You have corrected the secret code. Congratulations!");
         gameLogWriter.println("[SERVER SAYS] >> You have corrected the secret code. Congratulations!");
         
         serverWriter.println("[SERVER SAYS] >> Your score is " + playerScore);
         gameLogWriter.println("[SERVER SAYS] >> Your score is " + playerScore);
         
         serverWriter.println("[SERVER SAYS] >> The secret code is " + secretCode.toString());
         gameLogWriter.println("[SERVER SAYS] >> The secret code is " + secretCode.toString());
         
         gameLogWriter.println(getTimeLog());
         
         serverWriter.println("[SERVER SAYS] >> Exit the game.");
         commLogWriter.println("[SERVER SAYS] >> Exit the game.");
         
         serverWriter.println("[SERVER SAYS] >> Connection has been successfully terminated!");
         commLogWriter.println("[SERVER SAYS] >> Connection has been successfully terminated!");
         
         commLogWriter.println(getTimeLog());
      }
   }

   // Get the time log
   public static String getTimeLog() {
      SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
      Date date = new Date();
      return "[SERVER SAYS] >> Current time log " + formatter.format(date).toString() + "\n";
   }
   
   // Close streams after use
   public static void closeStreams(BufferedReader playerInputReader, PrintWriter serverWriter, Socket playerSocket,
         ServerSocket serverSocket, PrintWriter gameLogWriter, FileWriter gameLogFile, PrintWriter commLogWriter,
         FileWriter commLogFile) {
      try {
         if (playerInputReader != null) {
            playerInputReader.close();
         }
         if (serverWriter != null) {
            serverWriter.close();
         }
         if (playerSocket != null) {
            playerSocket.close();
         }
         if (serverSocket != null) {
            serverSocket.close();
         }
         if (gameLogWriter != null) {
            gameLogWriter.close();
         }
         if (gameLogFile != null) {
            gameLogFile.close();
         }
         if (commLogWriter != null) {
            commLogWriter.close();
         }
         if (commLogFile != null) {
            commLogFile.close();
         }
      } catch (IOException e) {
         System.err.println(e.getLocalizedMessage());
      }
   }

}
