import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MultiPlayer {

   // Declare global variables
//    protected static final String HOST = "localhost";
   private static final String HOST = "m1-c27n1.csit.rmit.edu.au";
   private static final int PORT = 18745;

   protected static String codeLength = "";

   public static int playerTurn = 1;
   public static final int maxGuess = 10;

   public static void main(String[] args) {

      // Declare objects
      Socket playerSocket = null;

      BufferedReader playerInputReader = null;
      BufferedReader serverReader = null;

      PrintWriter playerWriter = null;

      try {

         // Connect player socket with host and port
         playerSocket = new Socket(HOST, PORT);

         System.out.println("[SERVER SAYS] >> Inet address " + playerSocket.getInetAddress());
         System.out.println("[SERVER SAYS] >> Socket address " + playerSocket.getLocalSocketAddress());

         // Define objects based on the purposes appropriately
         playerInputReader = new BufferedReader(new InputStreamReader(System.in));
         serverReader = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
         playerWriter = new PrintWriter(playerSocket.getOutputStream(), true);

         // Display welcome messages
         System.out.println("+===========================================+");
         System.out.println("| Welcome To NP Multi Player Guessing Game! |");
         System.out.println("| Network Programming Assignment 1          |");
         System.out.println("|    Semester 1, 2018                       |");
         System.out.println("|       s3558745 - Minyoung Cho             |");
         System.out.println("+===========================================+");

         // Initialise the game
         initGame(playerInputReader, serverReader, playerWriter);

         // Play the game
         playGame(playerInputReader, serverReader, playerWriter);

      } catch (IOException e) {
         System.err.println(e.getLocalizedMessage());
      } finally {
         // Close streams after use
         closeStreams(playerInputReader, serverReader, playerWriter);
      }

   }

   private static void initGame(BufferedReader playerInputReader, BufferedReader serverReader, PrintWriter playerWriter)
         throws IOException {

      // Declare variables
      String serverResp = "";
      String playerInput = "";

      // Read messages from the server and display
      while ((serverResp = serverReader.readLine()) != null) {
         System.out.println(serverResp);
         if (serverResp.contains("first name")) {
            break;
         }
      }

      // Register with your first name
      System.out.print("[PLAYER SAYS] >> ");
      playerInput = playerInputReader.readLine();
      playerWriter.println(playerInput);

      // Read messages from the server and check if the name is valid
      while ((serverResp = serverReader.readLine()) != null) {
         System.out.println(serverResp);
         if (serverResp.contains("successfully registered in the server!")) {
            break;
         } else if (serverResp.contains("Register with your first name")) {
            System.out.print("[PLAYER SAYS] >> ");
            playerInput = playerInputReader.readLine();
            playerWriter.println(playerInput);
         }
      }

      /**
       * Read another message from the server First player - Set the secret code
       * length The other players - Enter any key to ready for the game Send input to
       * the server First user - secret code length The others - any key
       */

      // Client - Server Communication (The first user and the server)
      if ((serverResp = serverReader.readLine()).contains("Please enter the secret code length between 3 and 8")) {
         System.out.println(serverResp);

         // Declare local variables for limited use (less redundant)
         char[] validDigit = new char[100];
         int digitCount = 0;
         int isValid = 1;

         while (true) {
            // Get player input and check input validation
            System.out.print("[PLAYER SAYS] >> ");
            playerInput = playerInputReader.readLine();
            for (int i = 0; i < playerInput.length(); i++) {
               validDigit[i] = playerInput.charAt(i);
               if (Character.isDigit(validDigit[i]) && playerInput.length() == isValid) {
                  digitCount++;
               }
            }
            if (digitCount == isValid) {
               codeLength = getValidCodeLength(playerInput, playerInputReader);
               playerWriter.println(codeLength);
               break;
            } else {
               System.out.println("[SERVER SAYS] >> Invalid input. Try again.");
               System.out.println("[SERVER SAYS] >> Enter the length for secret code");
            }
         }

         // Client - Server Communication (The other players and the server)
      } else {
         System.out.println(serverResp);
         System.out.print("[PLAYER SAYS] >> ");
         playerInput = playerInputReader.readLine();
         playerWriter.println(playerInput);
      }

      // Read messages from the server
      while ((serverResp = serverReader.readLine()) != null) {
         System.out.println(serverResp);
         if (serverResp.contains("The secret code has been successfully generated")) {
            break;
         }
      }
   }

   private static void playGame(BufferedReader playerInputReader, BufferedReader serverReader,
         PrintWriter playerWriter) {

      // Declare variables and objects for limited use in a method
      String serverResp = "";
      String playerInput = "";
      List<Character> digit = new ArrayList<Character>();
      int secretCodeLength = 0;
      GameConfig gc = new GameConfig();
      boolean isForfeited = false;

      try {

         // Receive secret code length
         String[] tokens = new String[4];
         tokens = serverReader.readLine().split("\\s");
         secretCodeLength = Integer.parseInt(tokens[3]);

         // Receive attempt message
         serverResp = serverReader.readLine();
         System.out.println(serverResp);

         /**
          * This while loop works in different ways 
          *    1. If player turn reaches out to the max guess (10) 
          *    2. If a player forfeits 
          *    3. If a player corrects the secret code
          */
         while (playerTurn <= maxGuess) {
            while (true) {
               try {
                  // Gets into the statement if the game is not done yet
                  if (!gc.getIsEnd()) {
                     System.out.print("[PLAYER ATTEMPT " + playerTurn + "] >> ");
                     playerInput = playerInputReader.readLine();

                     // Check if the player entered forfeit keyword
                     // If so, reset the player details as loser
                     if (playerInput.equals("f")) {
                        isForfeited = true;
                        playerTurn = maxGuess + 1;
                        break;

                     }

                     // check duplicates from user guess
                     for (int i = 0; i < playerInput.length(); i++) {
                        if (!digit.contains(playerInput.charAt(i)) && playerInput.length() == secretCodeLength
                              && isNumeric(playerInput)) {
                           digit.add(playerInput.charAt(i));
                        } else {
                           System.out.println("[SYSTEM SAYS] >> Code length not match or duplicates found!");
                           digit.removeAll(digit);
                           break;
                        }
                     }

                     // Breaks the while loop only if the digit size is valid
                     if (digit.size() == secretCodeLength) {
                        digit.removeAll(digit);
                        break;
                     }
                  }

                  // Catch number format exception (only valid length of numbers are allowed)
               } catch (NumberFormatException e) {
                  System.out.println("[SYSTEM SAYS] >> Invalid iput has been entered. Try again.");
               }

            }

            // Check if the user forfeited
            if (isForfeited) {
               System.out.println("[SERVER SAYS] >> You have forfeited this game!");
               playerWriter.println("I want to forfeit this game");
               outOfGame(playerInputReader, serverReader);

               // Else, send valid guess code to the server
            } else {
               playerWriter.println(playerInput);
            }

            // Read server messages including hints
            while ((serverResp = serverReader.readLine()) != null) {
               System.out.println(serverResp);

               // Break the loop if the user reads next round message
               if (serverResp.contains("Round")) {
                  break;

                  // The user has guessed the code correctly
                  // Gets to outOfGame method because the game has been finished
               } else if (serverResp.contains("Exit the game.")) {
                  outOfGame(playerInputReader, serverReader);
                  break;
               }
            }

            // Increment player turn
            playerTurn++;

            // If the player turn has reached out to the max guess (10), break the loop
            if (playerTurn > maxGuess) {
               while ((serverResp = serverReader.readLine()) != null) {
                  System.out.println(serverResp);
                  if (serverResp.contains("Exit the game.")) {
                     outOfGame(playerInputReader, serverReader);
                     break;
                  }
               }
               break;
            }
         }
      } catch (IOException e) {
         System.err.println(e.getLocalizedMessage());
      }

   }

   private static void outOfGame(BufferedReader playerInputReader, BufferedReader serverReader) throws IOException {

      // Declare variables
      String playerInput = "";
      String serverResp = "";

      // Wait until every user finishes their game if there's more than 2 players
      System.out.println("[SERVER SAYS] >> Waiting for other players to finish their game....");
      // Read ending messages from the server
      while ((serverResp = serverReader.readLine()) != null) {
         System.out.println(serverResp);
         if (serverResp.contains("Good job all!")) {
            break;
         }
      }

      // Press "q" key to quit the game
      System.out.println("[SERVER SAYS] >> Press \"q\" to quit the game");
      while (!(playerInput = playerInputReader.readLine()).equals("q")) {
         System.out.println("[SERVER SAYS] >> Press \"q\" to quit the game");
      }
      System.exit(0);
   }

   private static String getValidCodeLength(String input, BufferedReader inReader) {

      String validLength = " ";

      // Check if the secret code length is between 3 and 8
      while (true) {
         try {
            validLength = input;

            if (Integer.parseInt(validLength) < 3 || Integer.parseInt(validLength) > 8) {
               System.out.println("[SERVER RESPONDS] >> valid digit length is between 3 and 8. try again.");
               System.out.print("[SERVER RESPONDS] >> Enter the length of secret code: ");
               input = inReader.readLine();
            } else {
               break;
            }
            // Catch IOException and NumberFormatException (does not allow string or any
            // other else)
         } catch (NumberFormatException ne) {
            System.out.println("[SERVER RESPONDS] >> valid digit length is between 3 and 8. try again.");
            System.out.print("[SERVER RESPONDS] >> Enter the length of secret code: ");
            try {
               input = inReader.readLine();
            } catch (IOException e) {
               e.printStackTrace();
            }
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
      return validLength;
   }

   public static boolean isNumeric(String str) {
      // Check if the value is numeric (strings are not allowed)
      try {
         double d = Double.parseDouble(str);
      } catch (NumberFormatException nfe) {
         return false;
      }
      return true;
   }

   private static void closeStreams(BufferedReader playerInputReader, BufferedReader serverReader,
         PrintWriter playerWriter) {
      // Close streams after use
      try {
         if (playerInputReader != null) {
            playerInputReader.close();
         }
         if (serverReader != null) {
            serverReader.close();
         }
         if (playerWriter != null) {
            playerWriter.close();
         }
      } catch (IOException e) {
         System.err.println(e.getLocalizedMessage());
      }

   }

}
