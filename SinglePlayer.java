import java.io.*;
import java.net.Socket;
import java.util.*;

public class SinglePlayer {

   // Declare field variables and objects for global use
//    public static final String HOST = "localhost";
   public static final String HOST = "m1-c27n1.csit.rmit.edu.au";
   public static final int PORT = 18745;
   public static String codeLength = "";

   public static void main(String[] args) {

      Socket playerSocket = null;

      try {

         // Connect the socket with host and port
         playerSocket = new Socket(HOST, PORT);

         // Initialise guess game
         initAndPlayGuessGame(playerSocket);

      } catch (IOException ioe) {
         System.err.println(ioe.getLocalizedMessage());
      } finally {
         
         // Close streams after use
         try {
            if (playerSocket != null) {
               playerSocket.close();
            }
         } catch (IOException ioe) {
            System.err.println(ioe.getLocalizedMessage());
         }
      }

   }

   public static void initAndPlayGuessGame(Socket playerSocket) {

      // Declare objects and variables to use locally
      BufferedReader playerInputReader = null;
      BufferedReader serverReader = null;
      PrintWriter playerSender = null;
      String playerInput = "";
      String serverResp = "";
      char[] validDigit = new char[100];
      int digitCount = 0;
      int isValid = 1;

      try {
         // Declare objects with appropriate purposes
         playerInputReader = new BufferedReader(new InputStreamReader(System.in));
         playerSender = new PrintWriter(playerSocket.getOutputStream(), true);
         serverReader = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));

         // Read server messages
         while ((serverResp = serverReader.readLine()) != null) {
            if (serverResp.contains("Please enter the secret code length between 3 and 8")) {
               break;
            }
            System.out.println(serverResp);
         }
         System.out.println(serverResp);

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
               break;
            } else {
               System.out.println("[SERVER SAYS] >> Invalid input. Try again.");
               System.out.println("[SERVER SAYS] >> Enter the length for secret code");
            }
         }

         // Send valid code length to the server
         playerSender.println(codeLength);

         // Start the game
         startGuessGame(playerInputReader, playerSender, serverReader, playerInput, serverResp);

      } catch (IOException ioe) {
         System.err.println(ioe.getLocalizedMessage());
      } finally {
         // Close streams after use
         try {
            if (playerInputReader != null) {
               playerInputReader.close();
            }
            if (serverReader != null) {
               serverReader.close();
            }
            if (playerSender != null) {
               playerSender.close();
            }
         } catch (IOException ioe) {
            System.err.println(ioe.getLocalizedMessage());
         }
      }
   }

   private static void startGuessGame(BufferedReader playerInputReader, PrintWriter playerSender,
         BufferedReader serverReader, String playerInput, String serverResp) {

      // Declare variables
      int playerTurn = 1;
      int maxGuess = 10;
      List<Character> digit = new ArrayList<Character>();

      try {
         // Read start game message from the server
         serverResp = serverReader.readLine();
         System.out.println(serverResp);

         // Send guess
         while (playerTurn <= maxGuess) {
            while (true) {
               
               // Get the guess code
               System.out.print("[ATTEMP " + playerTurn + "] >> ");
               playerInput = playerInputReader.readLine();

               // check duplicates from user guess
               for (int i = 0; i < playerInput.length(); i++) {
                  if (!digit.contains(playerInput.charAt(i)) 
                        && playerInput.length() == Integer.parseInt(codeLength)
                        && isNumeric(playerInput)) {
                     digit.add(playerInput.charAt(i));
                  } else {
                     System.out.println("[SYSTEM SAYS] >> Code length not match or duplicates found!");
                     digit.removeAll(digit);
                     break;
                  }
               }
               if (digit.size() == Integer.parseInt(codeLength)) {
                  digit.removeAll(digit);
                  break;
               }
            }

            // Send valid guess code to the server
            playerSender.println(playerInput);

            // Read server messages including hints
            while ((serverResp = serverReader.readLine()) != null) {
               System.out.println(serverResp);
               if (serverResp.contains("Next guess!")) {
                  break;
               } else if (serverResp.contains("Connection has been successfully terminated!")) {
                  playerSender.println("Bye!");
                  System.exit(0);
               }
            }
            
            // Increment player turn
            playerTurn++;

            // If player turn reaches out to the max guess (10), exit the game
            if (playerTurn > maxGuess) {
               while ((serverResp = serverReader.readLine()) != null) {
                  System.out.println(serverResp);
                  if (serverResp.contains("Connection has been successfully terminated!")) {
                     playerSender.println("Bye!");
                     System.exit(0);
                  }
               }
               break;
            }
         }
      } catch (IOException ioe) {
         System.err.println(ioe.getLocalizedMessage());
      }

   }

   private static String getValidCodeLength(String input, BufferedReader inReader) {

      String validLength = " ";

      // Check if the secret code length is between 3 and 8
      while (true) {
         try {
            validLength = input;

            if (Integer.parseInt(validLength) < 3 || Integer.parseInt(validLength) > 8) {
               System.out.println("[SERVER SAYS] >> valid digit length is between 3 and 8. try again.");
               System.out.print("[SERVER SAYS] >> Enter the length of secret code: ");
               input = inReader.readLine();
            } else {
               break;
            }
            // Catch IOException and NumberFormatException (does not allow string or any
            // other else but digits)
         } catch (NumberFormatException ne) {
            System.out.println("[SERVER SAYS] >> valid digit length is between 3 and 8. try again.");
            System.out.print("[SERVER SAYS] >> Enter the length of secret code: ");
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
   
   // Check if the value is only numeric
   public static boolean isNumeric(String str) {
      try {
         double d = Double.parseDouble(str);
      } catch (NumberFormatException nfe) {
         return false;
      }
      return true;
   }

}
