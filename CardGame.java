import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

/*
    We have abided by the UNCG Academic Honor Code on this assignment
                Candice Overcash and Anthony Ratliff
    This portion of the program represents the CardGame object and is used for
    creating the GUI and responsible for all game play.
    The CardGame class inherits from the JFrame class thus allowing all methods
    and fields from the JFrame class to be available when creating the GUI.
    12/06/2014
*/

public class CardGame extends JFrame{

    // Declare the private class fields.
    private int playerCardPlaceholder, computerCardPlaceholder, playerTotal, computerTotal, computerWinTotal, playerWinTotal,deckPlaceCounter, winStreak;    // Integers to reference various game play data.
    private final String version;   // String to hold the current version number.
    private String playerText, computerText, winnerText;    // Strings to hold various text fields throughout game play.
    private boolean playerBust, computerBust, loopBreak,gameLoopBreak;  // Boolean switches to aid in decion making loops.
    private final AtomicBoolean paused; // Atomic Boolean used to create a switch that runs in a different thread.
    private final Random generator = new Random();  // To reference a Random Object used to genereate various random numbers.
    private ArrayList <String> arrayDeck;   // To reference an Array List used to emulate a deck of cards.
    private final File highScore = new File("highScore.txt");
    
    // Reference ten different labels for the gamePlayPanel.
    private JLabel computerCard1,computerCard2,computerCard3,computerCard4,computerCard5,computerCard6,computerCard7,computerCard8;
    private JLabel playerCard1,playerCard2,playerCard3,playerCard4,playerCard5,playerCard6,playerCard7,playerCard8;
    
    // Reference four different labels for the gameStatsPanel.
    private JLabel playerTotalLabel, computerTotalLabel;
    private JLabel playerWinLabel, computerWinLabel;
    
    // Reference two different buttons and a text area for anotherGamePanel.
    private JButton yesButton, noButton;
    private JTextArea winnerTextArea;
    
    // Reference two different buttons and a text label for hitStayPanel.
    private JButton hitButton, stayButton;
    private JLabel YesNoQuestionLabel,HitStayQuestionLabel;
    private JPanel anotherGamePanel;
    private JPanel hitStayPanel;
    private JPanel blankPanel;
    final private Dimension southPanelHolderSize = new Dimension(400,95);
    
    // Declare and initialize constants to use for frame size.
    private final int WINDOW_WIDTH = 960;  // Window width
    private final int WINDOW_HEIGHT = 400;  // Window height
    
    // Single constructor which accepts no parameters.
    public CardGame(){
        
        // Initialize some of the variables and build the GUI
        version = "1.0 Final";
        computerWinTotal = 0; playerWinTotal = 0;
        loopBreak = false;
        gameLoopBreak = false;
        paused = new AtomicBoolean(false);
        
        // Start to build the Frame
        setTitle("Twenty-One - version: " + version);   // Set the Frame title.
        setSize(WINDOW_WIDTH,WINDOW_HEIGHT);    // Set the frame dimentions.
        setResizable(false); // Set if the Frame is resizable or not.
        setLocationRelativeTo(null);    // Set the starting location to center of screen.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Specify what happens when the close button is clicked.
        
        //  Build all of the panels, add them to the Frame, and display the GUI.
        buildPanels();   
    }
    
    // Public Properties
    
    // Get version information.
    public String getVersion(){
        return version;
    }
    
    // Get the player's score/status.
    public String getPlayerScore(){
        return playerText;
    }
    
    // Get the computer's score/status.
    public String getComputerScore(){
        return computerText;
    }
    
    // Get the boolean result of if the player as busted.
    public boolean getPlayerBust(){
        return playerBust;
    }
    
    // Get the boolean result of if the computer has busted.
    public boolean getComputerBust(){
        return computerBust;
    }
    
    // Class methods
    
    /*
        The only public method in the class that is called to start and play the game.
    */
    public void startGame() throws InterruptedException, FileNotFoundException{
        
        
        showWelcome();  // Start the game and give the option to display the rules.
        do {
        refreshTable(); // Remove old cards and replace them with the place holders.
        shuffle();      // Shuffle the deck of cards.       
        dealCards();    // Deal two cards each for both the player and the computer and start the running total.
        playerTurn();   // Let the player start first.

        // Generate the 2nd number for the computer, total the results to determine the winner.
        if (!playerBust) computerTurn();
           
        // If neither the player or the computer has playerBusted, display the results and add to the wins counter.
        if (!playerBust && !computerBust) {
            if (playerTotal > computerTotal) {
                playerWinTotal++;
                winnerText = "Congratulations.  You have won this round.";
            } else {
                computerWinTotal++;
                winnerText = "I am sorry.  The computer has won this round.";
            }
        }
           
            if (playerBust){ 
                playerText = "BUSTED";
                winnerText = "I am sorry.  The computer has won this round.";
            }
            computerTotalLabel.setText("Computer Total: " + computerTotal);
             String resultText = "You have decided to stay with " + playerText + " and\nthe computer has"
                    + " a total of " + computerText + "\n\n" + winnerText;
             playerWinLabel.setText("Player Wins: " + playerWinTotal);
             computerWinLabel.setText("Computer Wins: " + computerWinTotal);
             
             // Switch out the panels
             remove(hitStayPanel);
             invalidate();
             validate();
             add(anotherGamePanel, BorderLayout.SOUTH);
             winnerTextArea.setText(resultText);

            /*
                    Create an event driven loop that will pause the program
                    until the user decided to YES or NO.
            */
            paused.set(true);
            while (paused.get()){
              // Repeat loop until user clicks a button.
            }
                
                // Remove the anotherGamePanel in order to prepare for the next HitStayPanel.
                remove(anotherGamePanel);
                invalidate();
                validate();
        } while (!gameLoopBreak);
        winStreak = playerWinTotal - computerWinTotal;
        if(winStreak > 0) setHighScore(winStreak);
        
            
        setVisible(false);
    }
    
        /*
            Method for displaying the welcome screen and reading the high score from the log file.
        */
        private void showWelcome() throws FileNotFoundException{
            // Declaration and initialization of local variables.
            ImageIcon WelcomeIcon = new ImageIcon("card_gray_icon.png");
            String welcome = "Welcome to our game! 21 is a card "
                    + "game where a combination \nof dealt cards must total higher "
                    + "than the cards received by \nthe computer without totaling "
                    + "in excess of 21.  Similar to Blackjack, \n"
                    + "our game assigns point values to different cards."
                    + " However, the \ndifference are that Aces are only worth 1 point and ties go to the computer.\n "
                    + "\nHighscore: "
                    + getHighscore();
                    
            String title = "Welcome to 21 by C. Overcash and T. Ratliff  version:  " + version;

            // Display the welcome message.
            JOptionPane.showMessageDialog(null, welcome , title, JOptionPane.INFORMATION_MESSAGE, WelcomeIcon);
        }
        
        private String getHighscore() throws FileNotFoundException{
            
            
            String score;
            int scoreValue;
            if(!highScore.exists()){
                PrintWriter writer = new PrintWriter("highScore.txt");
                writer.println("0");
                writer.close();
                score = "No Highscore";
                return score;
            }
            else{Scanner inFile = new Scanner(highScore);
                if(inFile.hasNext()){
                    scoreValue = inFile.nextInt();
                    if(scoreValue == 0) {
                        score ="No Highscore";
                        return score;
                    }
                    
                    if(scoreValue == 1) score = scoreValue + " win in a row";
                    else score = scoreValue + " wins in a row";
                    return score;
                }
                else{
                    score = "No Highscore";
                    return score;
                }
            } 
                
        }
        private void setHighScore(int x) throws FileNotFoundException{
            int y;
            Scanner inFile = new Scanner(highScore);
            y = inFile.nextInt();
            if(x > y){
                PrintWriter writer = new PrintWriter("highScore.txt");
                writer.println(x);
                writer.close();
            }
        }
        
        /*
            Method for initilaizing the game and dealing the first
            two cards to both the player (bottom display) and computer
            dealer (top display).
        */
        private void dealCards(){
            // Initialize all variables for a new game.
            playerTotal = 0; computerTotal = 0; deckPlaceCounter = 0;
            playerText = ""; computerText = ""; winnerText = "";
            playerBust = false; computerBust = false;
        
            // Deal the first two cards to each player.
            playerTotal += getPoints(arrayDeck.get(deckPlaceCounter));
            playerCard1.setIcon(new ImageIcon(arrayDeck.get(deckPlaceCounter++)+ ".png"));
            computerTotal += getPoints(arrayDeck.get(deckPlaceCounter));
            computerCard1.setIcon(new ImageIcon(arrayDeck.get(deckPlaceCounter++) + ".png"));
            playerTotal += getPoints(arrayDeck.get(deckPlaceCounter));
            playerCard2.setIcon(new ImageIcon(arrayDeck.get(deckPlaceCounter++) + ".png"));
            computerTotalLabel.setText("Computer Total: " + computerTotal);           
            computerTotal += getPoints(arrayDeck.get(deckPlaceCounter++));
            computerCard2.setIcon(new ImageIcon("BACK.png"));
       
            // Set the text fields and labels to current status.
            computerText = Integer.toString(computerTotal);
            playerTotalLabel.setText("Player Total: " + playerTotal);
            remove(blankPanel);
                invalidate();
                validate();
        }
        
        /* Private method to represent the player's turn.  This method will generate 
            the 3rd and additional cards for the player, total the results and prompt
            the player to hit or stay.
        */
        private void playerTurn(){
            playerCardPlaceholder = 3;  // reset the playerCardPlaceholder variable to the 3rd position.
            // Loop gameplay until player either stays or busts.
            do {
                // Steps to perform if the player busts.
                if (playerTotal > 21) {
                    // Create and display the player busted message dialog.
                    ImageIcon loserIcon = new ImageIcon( "sad joker.png");
                    JOptionPane.showMessageDialog(null, "Sorry your total of: " + playerTotal + " is a Bust.", "PLAYER BUSTED",JOptionPane.INFORMATION_MESSAGE, loserIcon);
                    playerBust = true;  // Set playerBust
                    computerWinTotal++;  // increment the win counter
                    loopBreak = true;  // set to break the loop
                    computerCard2.setIcon(new ImageIcon(arrayDeck.get(3) + ".png"));    // Uncover the 2nd computer card.
                    computerTotalLabel.setText("Computer Total: " + computerTotal);     // update the computer total label with the value of the uncovered card.
                } 
            // Steps to perform if the player is still in the game.
            else {
                add(hitStayPanel, BorderLayout.SOUTH);  // Display the hitStayPanel.
                
                /*
                    Create an event driven loop that will pause the program
                    until the user decided to HIT or STAY.
                */
                paused.set(true);
                while (paused.get()){
                // Repeat loop until user clicks a button.
                }
                
                // If player chooses to HIT, next card is displayed and results totaled.
                if (!loopBreak) {
                    // Switch statement used to determine the current player card position.
                    switch (playerCardPlaceholder){
                        case 3:
                            playerTotal += getPoints(arrayDeck.get(deckPlaceCounter));
                            playerCard3.setIcon(new ImageIcon(arrayDeck.get(deckPlaceCounter++) + ".png"));
                            playerCardPlaceholder++;
                            break;
                        case 4:
                            playerTotal += getPoints(arrayDeck.get(deckPlaceCounter));
                            playerCard4.setIcon(new ImageIcon(arrayDeck.get(deckPlaceCounter++) + ".png"));
                            playerCardPlaceholder++;
                            break;
                        case 5:
                            playerTotal += getPoints(arrayDeck.get(deckPlaceCounter));
                            playerCard5.setIcon(new ImageIcon(arrayDeck.get(deckPlaceCounter++) + ".png"));
                            playerCardPlaceholder++;
                            break;
                        case 6:
                            playerTotal += getPoints(arrayDeck.get(deckPlaceCounter));
                            playerCard6.setIcon(new ImageIcon(arrayDeck.get(deckPlaceCounter++) + ".png"));
                            playerCardPlaceholder++;
                            break;
                        case 7:
                            playerTotal += getPoints(arrayDeck.get(deckPlaceCounter));
                            playerCard7.setIcon(new ImageIcon(arrayDeck.get(deckPlaceCounter++) + ".png"));
                            playerCardPlaceholder++;
                            break;
                        case 8:
                            playerTotal += getPoints(arrayDeck.get(deckPlaceCounter));
                            playerCard8.setIcon(new ImageIcon(arrayDeck.get(deckPlaceCounter++) + ".png"));
                            playerCardPlaceholder++;
                    }
                    
                    playerTotalLabel.setText("Player Total: " + playerTotal);   // Update the player total label.
                }
            }
                playerText = Integer.toString(playerTotal);  // Set the playerText to the current total.
           
            } while (!loopBreak);  // Continue to repeat the steps until loopBreak is true either by user choice (button click) or to break the loop (when player busts)
        }
        
        /* Private method to represent the computer's turn.  This method will generate 
            the 3rd and additional cards for the computer, total the results and will stay if 
            computer total is greater than the player but less than 22.
        */ 
        private void computerTurn() throws InterruptedException{
            computerCardPlaceholder = 3;    // Reset the computerCardPlaceholder variable to the 3rd position.
            computerCard2.setIcon(new ImageIcon(arrayDeck.get(3) + ".png"));    // Uncover the 2nd computer card.
            computerTotalLabel.setText("Computer Total: " + computerTotal);     // update the computer total label with the value of the uncovered card.
            
            while (computerTotal < 18){
                // Switch statement used to determine and make necessary changes to the current computer card position.
                switch (computerCardPlaceholder){
                        case 3:
                            computerTotal += getPoints(arrayDeck.get(deckPlaceCounter));
                            computerCard3.setIcon(new ImageIcon(arrayDeck.get(deckPlaceCounter++) + ".png"));
                            computerCardPlaceholder++;
                            break;
                        case 4:
                            computerTotal += getPoints(arrayDeck.get(deckPlaceCounter));
                            computerCard4.setIcon(new ImageIcon(arrayDeck.get(deckPlaceCounter++) + ".png"));
                            computerCardPlaceholder++;
                            break;
                        case 5:
                            computerTotal += getPoints(arrayDeck.get(deckPlaceCounter));
                            computerCard5.setIcon(new ImageIcon(arrayDeck.get(deckPlaceCounter++) + ".png"));
                            computerCardPlaceholder++;
                            break;
                        case 6:
                            computerTotal += getPoints(arrayDeck.get(deckPlaceCounter));
                            computerCard6.setIcon(new ImageIcon(arrayDeck.get(deckPlaceCounter++) + ".png"));
                            computerCardPlaceholder++;
                            break;
                        case 7:
                            computerTotal += getPoints(arrayDeck.get(deckPlaceCounter));
                            computerCard7.setIcon(new ImageIcon(arrayDeck.get(deckPlaceCounter++) + ".png"));
                            computerCardPlaceholder++;
                            break;
                        case 8:
                            computerTotal += getPoints(arrayDeck.get(deckPlaceCounter));
                            computerCard8.setIcon(new ImageIcon(arrayDeck.get(deckPlaceCounter++) + ".png"));
                            computerCardPlaceholder++;
                 }
                computerTotalLabel.setText("Computer Total: " + computerTotal);

                if (computerTotal > 21) {
                    ImageIcon winIcon = new ImageIcon( "happy joker.png");
                    JOptionPane.showMessageDialog(null, "The computer has a total of: " + computerTotal + " and has Busted.", "COMPUTER BUSTED",JOptionPane.INFORMATION_MESSAGE, winIcon);
                    computerBust = true;
                    winnerText = "Congratulations.  You have won this round.";
                    computerText = "BUSTED";
                    playerWinTotal++;
                }
                else computerText = Integer.toString(computerTotal);
                Thread.sleep(500);  // Simulate a half second delay when dealing computer cards.
            } 
        }
        
        /*
            The buildPanels method will create each of the panels used in the frame
            by adding different components to the panel as each requires.
        */
        
        private void buildPanels(){
            // First build the gamePlayPanel which should be a grid with two rows and five columns.
             // Reference four different panels to use in the frame.
            JPanel gamePlayPanel = new JPanel();   // To reference a panel for displaying the cards.
            JPanel gameStatsPanel = new JPanel();  // To reference a panel for displaying the game statictics.
            JPanel currentScorePanel = new JPanel();  // To reference one of two smaller panels that will reside inside the gameStatsPanel.
            JPanel currentWinPanel = new JPanel();  // To reference the 2nd of two smaller panels that will reside inside the gameStatsPanel.
            JPanel innerPanel = new JPanel();  // To reference the inner pannel used to properly display the Hit/Stay buttons.
            blankPanel = new JPanel();  // To reference a blank pannel used to fill the empty space.
            anotherGamePanel = new JPanel();  // To reference a panel for asking the player if he/she wishes to play another game.
            hitStayPanel = new JPanel();  // To reference a panel for asking the player if he/she wishes to take a 'hit' or 'stay' with their current hand.
            
            // Set the properties of the blank and gamePlayPanel.
            blankPanel.setPreferredSize(southPanelHolderSize);
            blankPanel.setBackground(Color.BLACK);
            gamePlayPanel.setLayout(new GridLayout(2,5));
            gamePlayPanel.setBackground(Color.green);
            
            // Create sixteen labels and set their default images to cardPlaceholder.png.
            computerCard1 = new JLabel();
            computerCard2 = new JLabel();
            computerCard3 = new JLabel();
            computerCard4 = new JLabel();
            computerCard5 = new JLabel();
            computerCard6 = new JLabel();
            computerCard7 = new JLabel();
            computerCard8 = new JLabel();
            
            playerCard1 = new JLabel();
            playerCard2 = new JLabel();
            playerCard3 = new JLabel();
            playerCard4 = new JLabel();
            playerCard5 = new JLabel();
            playerCard6 = new JLabel();
            playerCard7 = new JLabel();
            playerCard8 = new JLabel();
            refreshTable();
            
            // Add the ten labels to the gamePlayPanel
            gamePlayPanel.add(computerCard1);
            gamePlayPanel.add(computerCard2);
            gamePlayPanel.add(computerCard3);
            gamePlayPanel.add(computerCard4);
            gamePlayPanel.add(computerCard5);
            gamePlayPanel.add(computerCard6);
            gamePlayPanel.add(computerCard7);
            gamePlayPanel.add(computerCard8);
            gamePlayPanel.add(playerCard1);
            gamePlayPanel.add(playerCard2);
            gamePlayPanel.add(playerCard3);
            gamePlayPanel.add(playerCard4);
            gamePlayPanel.add(playerCard5);
            gamePlayPanel.add(playerCard6);
            gamePlayPanel.add(playerCard7);
            gamePlayPanel.add(playerCard8);
            
            // Second build the gameStatsPanel which should be border layout using only north and south borders.
            gameStatsPanel.setLayout(new BorderLayout());
            currentScorePanel.setLayout(new GridLayout(2,1));
            Border firstBorder = BorderFactory.createLineBorder(Color.WHITE,2,true);
            gameStatsPanel.setBackground(Color.RED);
            currentScorePanel.setBackground(Color.RED);
            gameStatsPanel.setBorder(BorderFactory.createTitledBorder(firstBorder,"Scoreboard",2,2,null,Color.WHITE));
            currentScorePanel.setBorder(BorderFactory.createEmptyBorder());
            
            // Create the four labels.
            playerTotalLabel = new JLabel("Player Total: 0",SwingConstants.LEFT);
            playerTotalLabel.setFont(new Font("Arial",Font.PLAIN,26));
            playerTotalLabel.setForeground(Color.WHITE);
            computerTotalLabel = new JLabel("Computer Total: 0",SwingConstants.LEFT);
            computerTotalLabel.setFont(new Font("Arial",Font.PLAIN,26));
            computerTotalLabel.setForeground(Color.WHITE);
            playerWinLabel = new JLabel("Player Wins: 0");
            playerWinLabel.setFont(new Font("Arial",Font.PLAIN,16));
            computerWinLabel = new JLabel("Computer Wins: 0");
            computerWinLabel.setFont(new Font("Arial",Font.PLAIN,16));
            
            // Add the four labels to the inner panels.
            currentScorePanel.add(computerTotalLabel);
            currentScorePanel.add(playerTotalLabel);           
            currentWinPanel.add(playerWinLabel);
            currentWinPanel.add(computerWinLabel);
            
            // Add the two inner panels to the gameStatsPanel
            gameStatsPanel.add(currentScorePanel,BorderLayout.NORTH);
            gameStatsPanel.add(currentWinPanel,BorderLayout.SOUTH);
            
            // Create Hit/Stay Buttons
            hitButton = new JButton("HIT");
            stayButton = new JButton("STAY");
            hitButton.setPreferredSize(new Dimension(120,50));
            stayButton.setPreferredSize(new Dimension(120,50));
            hitButton.setFont(new Font("Arial", Font.BOLD, 26));
            stayButton.setFont(new Font("Arial", Font.BOLD, 26));
            hitButton.setBackground(Color.WHITE);
            hitButton.setForeground(Color.GREEN);
            stayButton.setBackground(Color.WHITE);
            stayButton.setForeground(Color.RED);
          
            // Create Yes/No Buttons
            yesButton = new JButton("Yes");
            noButton = new JButton("No");
            yesButton.setPreferredSize(new Dimension(120,80));
            noButton.setPreferredSize(new Dimension(120,80));
            yesButton.setFont(new Font("Arial", Font.BOLD, 26));
            noButton.setFont(new Font("Arial", Font.BOLD, 26));
            yesButton.setBackground(Color.WHITE);
            yesButton.setForeground(Color.GREEN);
            noButton.setBackground(Color.WHITE);
            noButton.setForeground(Color.RED);
            
            // Register an Event Listener to all 4 buttons.
            hitButton.addActionListener(new ButtonListener());
            stayButton.addActionListener(new ButtonListener());
            yesButton.addActionListener(new ButtonListener());
            noButton.addActionListener(new ButtonListener());
            
             // Create the question text label.
            HitStayQuestionLabel = new JLabel("Would you like another card?");
            HitStayQuestionLabel.setFont(new Font("Arial",Font.PLAIN,28));
            HitStayQuestionLabel.setForeground(Color.red);
            HitStayQuestionLabel.setHorizontalAlignment(SwingConstants.CENTER);
            YesNoQuestionLabel = new JLabel("Would you like to play again?");
            YesNoQuestionLabel.setFont(new Font("Arial",Font.PLAIN,28));
            YesNoQuestionLabel.setForeground(Color.RED);
            
            // Create Winner Text Area
            winnerTextArea = new JTextArea();
            winnerTextArea.setPreferredSize(new Dimension(300,80));
            winnerTextArea.setFont(new Font("Arial",Font.PLAIN,14));
            winnerTextArea.setForeground(Color.ORANGE);
            winnerTextArea.setBackground(Color.BLACK);
            
            // Create an inner panel for holding the Hit/Stay Buttons
            innerPanel.add(hitButton);
            innerPanel.add(stayButton);
            innerPanel.setBackground(Color.BLACK);
            
            //  Add all of the objects to the hitStayPanel and set its properties.
            //hitStayPanel.setSize(new Dimension(100,20));
            hitStayPanel.setLayout(new BorderLayout());
            hitStayPanel.setBackground(Color.BLACK);
            hitStayPanel.setPreferredSize(southPanelHolderSize);
            hitStayPanel.add(HitStayQuestionLabel, BorderLayout.NORTH);
            hitStayPanel.add(innerPanel, BorderLayout.SOUTH);
            
            // Add all of the objects to the yesNoPanel
            anotherGamePanel.setBackground(Color.BLACK);
            anotherGamePanel.setPreferredSize(southPanelHolderSize);
            anotherGamePanel.add(winnerTextArea);
            anotherGamePanel.add(YesNoQuestionLabel);
            anotherGamePanel.add(yesButton);
            anotherGamePanel.add(noButton);
            
            // Add the panels to the frame.
            setLayout(new BorderLayout());
            add(gamePlayPanel, BorderLayout.CENTER);
            add(gameStatsPanel, BorderLayout.EAST);
            add(blankPanel, BorderLayout.SOUTH);
        
            // Display the frame
            validate();
            setVisible(true);
        }
        
        /*
            Private method to reset the table by removing all cards and 
            replacing them with placeholders.
        */
        private void refreshTable(){
            computerCard1.setIcon(new ImageIcon("cardPlaceholder.png"));
            computerCard2.setIcon(new ImageIcon("cardPlaceholder.png"));
            computerCard3.setIcon(new ImageIcon("cardPlaceholder.png"));
            computerCard4.setIcon(new ImageIcon("cardPlaceholder.png"));
            computerCard5.setIcon(new ImageIcon("cardPlaceholder.png"));
            computerCard6.setIcon(new ImageIcon("cardPlaceholder.png"));
            computerCard7.setIcon(new ImageIcon("cardPlaceholder.png"));
            computerCard8.setIcon(new ImageIcon("cardPlaceholder.png"));
            playerCard1.setIcon(new ImageIcon("cardPlaceholder.png"));
            playerCard2.setIcon(new ImageIcon("cardPlaceholder.png"));
            playerCard3.setIcon(new ImageIcon("cardPlaceholder.png"));
            playerCard4.setIcon(new ImageIcon("cardPlaceholder.png"));
            playerCard5.setIcon(new ImageIcon("cardPlaceholder.png"));
            playerCard6.setIcon(new ImageIcon("cardPlaceholder.png"));
            playerCard7.setIcon(new ImageIcon("cardPlaceholder.png"));
            playerCard8.setIcon(new ImageIcon("cardPlaceholder.png"));
        }
        
        /*
            Private method for creating an ArrayList and filling it with 10
            random cards from the deck.
        */
        private void shuffle(){
            // Declare and initialize any local variables.
            final int ARRAY_SIZE = 16;  // Set the size of the deck to 10 cards.
            int intRandom;  // To reference a random number from 13 to 65.
            String stringCard;  // To reference a card file name.
            boolean cardPresent;    // Switch to determine if random card is a repeat and already present in the deck.
            arrayDeck = new ArrayList(ARRAY_SIZE);  // To reference the actual Array List.
            
            // Start a loop that coninues until the arrayDeck is full.
            do {
                cardPresent = false;
                intRandom = (generator.nextInt(51) + 13);
                stringCard = "CARD_" + intRandom;
                
                if (!arrayDeck.isEmpty()) {
                    for (String value : arrayDeck) {
                        if (value.equals(stringCard)) {
                            cardPresent = true;
                            break;
                        }
                            }
                    if (!cardPresent) arrayDeck.add(stringCard);
                }
                
                else arrayDeck.add(stringCard);  // The deck is empty and first card is added.
           
            } while (arrayDeck.size() != ARRAY_SIZE);
        }
        
        /*
            Private function used to determine the point value for any card passed into it.
            The method accepts a string representing the card and returns an integer
            containing the point value of card.
        */
        private int getPoints (String card){
            int point;
            
            // Read the last two letters of the card string passed into the function.
            if (card != null && card.length() >= 2) {  
            point = (Integer.parseInt(card.substring(card.length() - 2))% 13) +1;
            if (point > 10) point = 10;
            return point; 
            }
            else return 0;
        }
        /* 
            Private inner class that handles the event when a user clicks a button.
        */
        private class ButtonListener implements ActionListener {
            public void actionPerformed(ActionEvent e){
                // Determine which of the 4 buttons was pressed and perform action for each one.
                if (e.getSource() == hitButton){
                    loopBreak = false;
                    paused.set(false);
                }
                else if (e.getSource() == stayButton){
                    loopBreak = true;
                    paused.set(false);
                }
                else if (e.getSource() == yesButton){
                    gameLoopBreak = false;
                    paused.set(false);
                }
                else if (e.getSource() == noButton){
                    gameLoopBreak = true;
                    paused.set(false);
                }
            } 
        }
}
