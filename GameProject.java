
import java.io.FileNotFoundException;



/*  We have abided by the UNCG Academic Honor Code on this assignment
    Candice Overcash and Anthony Ratliff
    Driver program to create and start an instance of the CardGame class object.
    12/06/2014
 */
public class GameProject {

    // Declaration of the main class
    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
       CardGame blackJack = new CardGame(); // Create an instance of the object.
       blackJack.startGame();   // Start the game.
       blackJack.dispose(); // End the game.
    }
}
