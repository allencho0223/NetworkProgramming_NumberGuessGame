
public class GameConfig {

   // Declare global field for use from other classes
   private int playerScore = 100;
   private int playerTurn = 1;
   protected final int maxGuess = 10;
   private static int isPlayerDone = 0;
   private boolean isEnd = false;
   
   // Use this object for player synchronisation
   protected Object lock = new Object();
   

   // Getter and setter of isPlayerDone
   public int getIsPlayerDone() {
      return isPlayerDone;
   }

   @SuppressWarnings("static-access")
   public void setIsPlayerDone(int isPlayerDone) {
      this.isPlayerDone = isPlayerDone;
   }

   // Getter and setter of playerScore
   public int getPlayerScore() {
      return playerScore;
   }

   public void setPlayerScore(int playerScore) {
      this.playerScore = playerScore;
   }

   // Getter and setter of playerTurn
   public int getPlayerTurn() {
      return playerTurn;
   }

   public void setPlayerTurn(int playerTurn) {
      this.playerTurn = playerTurn;
   }

   // Getter and setter of isEnd to check if the game is finished
   public boolean getIsEnd() {
      return isEnd;
   }

   public void setIsEnd(boolean isEnd) {
      this.isEnd = isEnd;
   }

}
