import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.Random;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class MineSweeper{


	static int tiles = 300;
	static int grid[] = new int[tiles];
	static boolean flags[] = new boolean[tiles];
	static boolean discovered[] = new boolean[tiles];
	static JButton[] buttons = new JButton[tiles];
	static int[] xMod = {0, 1, 1, 1, 0, -1, -1, -1};
	static int[] yMod = {1, 1, 0, -1, -1, -1, 0, 1};
	static JFrame frame;
	static JPanel panel;
	static Random ran;
	static boolean gameOver = false;
	static boolean boardGenerated = false;
	static int rows, cols;
	static int buttonSize = 40;
	static int bombAmount = 50;

	// possible label colours (1 is blue, 2 is green ect.)
	static Color[] label = {new Color(0x0100FE), new Color(0x017F01), new Color(0x0FE0000), new Color(0x010080), new Color(0x810102), new Color(0x008081), new Color(0x000000), new Color(0x808080)};


	public static void initialize(){

		// rows = (int)Math.sqrt(tiles);
		// cols = (int)Math.sqrt(tiles);
		rows = 15;
		cols = 20;

		if(rows * cols != tiles){
			System.out.println("Error Invalid Number of Tiles");
		}

		// initialize objects
		ran = new Random();
		frame = new JFrame("Tatti Sweeper");
		panel = new JPanel();

		// create and add buttons to grid
		createButtons();

		// set default values
		clearBoard();

		// setup JFrame
		frame.getContentPane().add(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
		frame.setSize(buttonSize * cols, buttonSize * rows);
		frame.setLocationRelativeTo(null);
	}
	
	public static void clearBoard(){
		// populate arrays
				for (int i = 0; i < grid.length; i++){
					buttons[i].setEnabled(true);
					buttons[i].setText("");
					grid[i] = 0;
					flags[i] = false;
					discovered[i] = false;
				}
				gameOver = false;
	}

	private static void generateBoard(int click){

		plantBombs(bombAmount, click);
		updateButtonLabels();
	}

	// initilize button objects as well as attach mouse handler
	private static void createButtons(){

		// loop through buttons
		for (int i = 0; i < buttons.length; i++){

			buttons[i] = new JButton();
			buttons[i].setFont(new Font("verdana", Font.BOLD, 18));
			buttons[i].setMargin(new Insets(0, 0, 0, 0));
			buttons[i].setPreferredSize(new Dimension(buttonSize, buttonSize));
			buttons[i].setMaximumSize(new Dimension(buttonSize, buttonSize));
			buttons[i].setActionCommand(Integer.toString(i));
			buttons[i].addMouseListener(new MouseAdapter(){

				public void mousePressed(MouseEvent e){

					int button = Integer.parseInt(((AbstractButton)e.getSource()).getActionCommand());

					if(!boardGenerated && e.getButton() != MouseEvent.BUTTON3){
						generateBoard(button);
						sweep(button);
					}
					else if(!gameOver){
						// seperate right click flags and left click sweeps
						if(e.getButton() == MouseEvent.BUTTON3){
							// toggles the flag at requested button
							flag(button);
						}
						else{
							// normal sweeping
							sweep(button);
						}
					}
				}
			});

			// add buttons to JFrame
			panel.add(buttons[i]);
		}

		// create grid
		panel.setLayout(new GridLayout(rows, cols));

		// display the window
		frame.pack();
		frame.setVisible(true);
	}

	public static void updateButtonLabels(){

		// loop through all tiles
		for (int i = 0; i < grid.length; i++){

			// tiles must be discovered inorder for anything other then flags to be displayed
			if(discovered[i]){
				if(grid[i] == 0){ // valid range of colours
					// blank tiles get set to disabled for effect
					buttons[i].setText("");
					buttons[i].setEnabled(false);
				}
				else if(grid[i] < 9 && grid[i] > 0){
					buttons[i].setForeground(label[grid[i] - 1]);
					buttons[i].setText(Integer.toString(grid[i]));
				}
			}

			// gameover reveals all of the board
			if(gameOver){
				if(grid[i] > 0 && grid[i] < 9){
					buttons[i].setForeground(label[grid[i] - 1]);
					buttons[i].setText(Integer.toString(grid[i]));
				}
				else if(grid[i] == 0){
					buttons[i].setEnabled(false);
				}
				// show bombs as black 'X'
				else if(grid[i] == -1){
					buttons[i].setForeground(Color.BLACK);
					buttons[i].setText("X");
				}
			}
		}
	}

	// inverts the boolean flag value
	private static void flag(int i){

		// cannot flag already discovered tiles
		if(!discovered[i]){
			flags[i] = !flags[i];
			// update board
			updateButtonLabels();
		}

		// if tile is flagged, set as red 'F', otherwise clear button (for blank buttons previously marked)
		if(flags[i]){
			buttons[i].setForeground(Color.RED);
			buttons[i].setText("F");
		}
		else{
			buttons[i].setText("");
			updateButtonLabels();
		}
	}

	// selects specified amount of tiles to be bombs
	public static void plantBombs(int amount, int click){

		// loop until desired number is achieved
		for (int i = 0; i < amount; i++){
			int possibleLocation = ran.nextInt(tiles);

			// if tile is not already taken, set tile to be bomb, otherwise reloop
			if(grid[possibleLocation] != -1 && possibleLocation != click){
				grid[possibleLocation] = -1;
			}
			else{
				i--;
			}
		}
		generateNumbers();
		boardGenerated = true;
	}

	// populates the grid with numbers representing how many bombs are in the surrounding 8 tiles
	public static void generateNumbers(){

		// loop through grid
		for (int i = 0; i < grid.length; i++){

			// no markers are needed on tiles with bombs
			if(grid[i] != -1){

				// counter variable used to keep a running total of bombs
				int counter = 0;

				// up to 8 possible tiles around
				for (int k = 0; k < 8; k++){

					// convert 1D array number into rows and cols
					// then a modifier is added on to search in 3 x 3 area
					int x = i % cols + yMod[k];
					int y = i / cols + xMod[k];

					// make sure the new values are valid on the grid
					if(checkIfValid(x, y)){
						// if the grid location is a bomb then add to the total
						if(grid[y * cols + x] == -1){
							counter++;
						}
					}
				}
				// set current tile to total count
				grid[i] = counter;
			}
		}
	}

	// discovers blank and number tiles around the given source
	public static void sweep(int current){

		// if current tile is a flag, reveal it (mouse would have clicked on current tile meaning player wanted to reveal flag)
		if(flags[current]){
			// removes flag and discover
			flags[current] = false;
			discovered[current] = true;
		}
		// action depends on tile type
		switch (grid[current]) {
			// sweeping a bomb loses the game.
			case -1:
				// sets gameover and displays board
				gameOver = true;
				updateButtonLabels();
				// taunts player
				int n = JOptionPane.showConfirmDialog(frame, "You Suck\nTry Again?", "Lol Loser!", JOptionPane.YES_NO_OPTION);
				if(n == 0){
					boardGenerated = false;
					clearBoard();
				}
				break;
			// continues spread of tile is blank
			case 0:
				discovered[current] = true;
				spread(current);
				break;
			// stops spread on anything thats not blank
			default:
				discovered[current] = true;
				// updateboard
				break;
		}
		updateButtonLabels();
		checkWin();
	}

	// tests of the game has been won
	private static void checkWin(){

		// assumes the player has won until proven otherwise
		boolean win = true;

		// loops through grid
		for (int i = 0; i < grid.length && win; i++){
			// undiscovered nonbomb tiles or unflaged bombs means that the player has not won
			if(!discovered[i] && grid[i] != -1){
				win = false;
			}
		}

		// on winning
		if(win){
			int n = JOptionPane.showConfirmDialog(frame, "Winner Winner Chicken Dinner!\n Play another?", "Congrats!", JOptionPane.YES_NO_OPTION);
			if(n == 0){
				boardGenerated = false;
				clearBoard();
			}
			else{
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		}
	}

	private static void spread(int i){

		// convert values to rows and cols for ease of use;
		int x = i % cols;
		int y = i / cols;

		// test 8 tiles around
		for (int k = 0; k < 8; k++){

			// add modifier to cordinates to accese surrounding area
			int currentX = x + xMod[k];
			int currentY = y + yMod[k];

			// converts 2 coordinates back to single
			int current = currentY * cols + currentX;

			// make sure new location is valid the current tile being spread from is a blank space
			if(checkIfValid(currentX, currentY) && grid[i] == 0){

				// handling diffrent grid types
				switch (grid[current]) {
					case -1:
						// do not continue spreading at bomb
						break;
					case 0:
						// at a blank tile that is not discovered and not flaged, discover and spread from there
						if(!discovered[current] && !flags[current]){
							discovered[current] = true;
							spread(current);
						}
						break;
					default:
						// otherwise discover tile
						discovered[current] = true;
						break;
				}
			}
		}
	}

	// make sure x and y are in bounds of 0 to row length
	public static boolean checkIfValid(int x, int y){

		return (x > -1 && x < cols && y > -1 && y < rows);
	}

	public static void main(String args[]){

		javax.swing.SwingUtilities.invokeLater(new Runnable(){

			public void run(){

				initialize();
			}
		});
	}
}
