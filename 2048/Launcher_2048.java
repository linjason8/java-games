//coded by Jason Lin

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;

public class Launcher_2048 {

	public static void main(String[] args) {
		// initialize window
		JFrame frame = new JFrame("2048");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// add game to window
		GameCode game = new GameCode();
		frame.getContentPane().add(game);
		game.init();
		frame.pack();

		frame.setSize(650, 900);
		frame.setResizable(false);
		frame.setVisible(true);
	}
}

class GameCode extends JPanel implements KeyListener, Runnable, ActionListener {

	Rectangle back; // background
	ArrayList<Square> box; //
	Square logo; // 2048 icon in top left
	// move- tracks if move was made after key press
	// midmove- tracks if move has already started
	// over- tracks if game over
	// direction of movement
	boolean move, midmove, over, left, right, up, down; 
	int score; // tracks score
	Timer time; //ingame timer

	public void init() {
		back = new Rectangle(25, 225, 600, 600);
		box = new ArrayList<>();
		logo = new Square(25, 25, 150);
		logo.setVal(2048);
		score = 0;

		over = false;
		move = midmove = false;
		left = right = up = down = false;

		time = new Timer(3, this);
		time.start();

		this.addKeyListener(this);
		setSize(650, 900);
		setFocusable(true);

		for (int i = 0; i < 4; i++) { // initialize all 16 squares
			for (int k = 0; k < 4; k++) {
				box.add(new Square(148 * k + 33, 148 * i + 233, 140));
			}
		}
		newBlock(); // adds 2 squares with values to start game
		newBlock();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g.setColor(Color.DARK_GRAY);
		g2.fill(back); // fill background
		g.setColor(Color.GRAY);
		for (int i = 0; i < 4; i++) { // draw background gray squares
			for (int k = 0; k < 4; k++) {
				g.fillRect(148 * k + 33, 148 * i + 233, 140, 140);
			}
		}
		g.setFont(new Font("TimesRoman", Font.BOLD, 50));
		for (int k = 0; k < 16; k++) {
			g.setColor(box.get(k).getColor()); // get color of each square
			if(box.get(k).getVal()!= 0) //only draw square if value is not 0
				g2.fill(box.get(k).getRect());
			if (box.get(k).getVal() != 0) {
				if (box.get(k).getVal() >= 8) // change color of text to be legible
					g.setColor(Color.WHITE);
				else
					g.setColor(Color.BLACK);
				// display value of tile at a centered location
				g.drawString(((Integer) box.get(k).getVal()).toString(),
						box.get(k).getX() + 70 - ((Integer) box.get(k).getVal()).toString().length() * 13,
						box.get(k).getY() + 85);
			}
		}
		g.setColor(Color.black); // drawing top bar
		g.fillRect(23, 23, 154, 154);
		g.fillRect(218, 53, 394, 94);
		g.setColor(logo.getColor()); // drawing the logo in the top left
		g2.fill(logo.getRect());
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(220, 55, 390, 90);
		g.setColor(Color.white);
		g.setFont(new Font("TimesRoman", Font.BOLD, 60));
		g.drawString("2048", 40, 120);
		g.setColor(Color.DARK_GRAY);
		g.setFont(new Font("TimesRoman", Font.BOLD, 30));
		g.drawString("SCORE", 240, 105);
		g.setFont(new Font("TimesRoman", Font.BOLD, 20));
		g.drawString("Use arrow keys to move", 310, 190);
		g.setFont(new Font("TimesRoman", Font.BOLD, 50));
		g.setColor(Color.white);
		g.drawString(((Integer) score).toString(), 380, 112);
		if (over) { // draw Game Over text when dead
			g.setColor(Color.black);
			g.fillRect(25, 400, 600, 220);
			g.setColor(Color.white);
			g.fillRect(27, 402, 596, 216);
			g.setColor(Color.black);
			g.setFont(new Font("TimesRoman", Font.BOLD, 80));
			g.drawString("Game Over", 130, 500);
			g.setFont(new Font("TimesRoman", Font.BOLD, 40));
			g.drawString("Space Bar to Restart", 150, 550);
		}
	}

	public void newBlock() {
		boolean preExists = true; // determines if there is already a block in the location
		while (preExists) { // randomly selects location for new block, reselects if already occupied
			int k = (int) (Math.random() * 16);
			if (box.get(k).getVal() == 0) {
				preExists = false;
				int i = (int) (Math.random() * 6);
				if (i < 5) // 5/6 chance of spawning with value of 2
					box.get(k).setVal(2);
				else // 1/6 chance of spawning with value of 4
					box.get(k).setVal(4);
			}
		}

		// after new block spawns, check if moves are still possible; if not, end game
		boolean moveAvailable = false; // default assume no move available
		int k = 0; // block index
		while (!moveAvailable && k < 16) {
			if (box.get(k).getVal() == 0) // if any square is empty, moves are still possible
				moveAvailable = true;
			k++;
		}
		if (!moveAvailable) { // if all squares are filled
			int i = 0;
			while (i < 12 && !moveAvailable) { // if any block has the same value as block below, move is possible
				if (box.get(i).getVal() == box.get(i + 4).getVal()) {
					moveAvailable = true;
				}
				i++;
			}
			i = 0;
			while (i < 15 && !moveAvailable) { // if any block has the same value as block to right, move is possible
				if (box.get(i).getVal() == box.get(i + 1).getVal()) {
					moveAvailable = true;
				}
				i++;
				if (i % 4 == 3)
					i++;
				if (i == 16) // end game if no moves are possible
					over = true;
			}
		}
	}

	public void update() {
		move = false; //first assume no motion occurs, affects if new block is spawned at end of update method
		if (left) { 
			for(int row = 0; row < 4; row++) { //iterate through rows
				for(int tile = row*4+1; tile < row*4+4; tile++) { //iterate through each tile of row
					//if the space to the left is empty, move left
					if((box.get(tile-1).getVal() == 0 && box.get(tile).getVal() != 0)) { 
						move = true; //motion has occured
						midmove = true;
						//if the tile has moved far enough left to enter the next square, transfer value and "combined status" to next square
						if(box.get(tile).getX() <= 148 * (tile%4-1) + 33) {
							box.get(tile).setX(148 * (tile%4) + 33);
							box.get(tile-1).setVal(box.get(tile).getVal());
							box.get(tile).setVal(0);
							box.get(tile-1).combined = box.get(tile).combined;
							box.get(tile).combined = false;
						}
						//if the tile has not moved far enough to enter the next square, keep moving left
						else box.get(tile).setX(box.get(tile).getX() - 15);
					}
					//if the square to the left has the same value and has not combined yet, combine values with left
					else if(box.get(tile).getVal() != 0 && box.get(tile).getVal() == box.get(tile-1).getVal() && !box.get(tile-1).combined && !box.get(tile).combined) {
						move = true; //motion has occured
						midmove = true;
						box.get(tile).setX(box.get(tile).getX() - 15); //move left
						//once box has moved far enough left to enter the next square, combine values into one square and reset other
						if(box.get(tile).getX() <= 148 * (tile%4-1) + 33) {
							box.get(tile).setX(148 * (tile%4) + 33);
							box.get(tile-1).setVal(box.get(tile).getVal()*2);
							box.get(tile-1).combined = true; //track that this square has been combined during this move, cannot combine again
							box.get(tile).setVal(0);
						}
					}
				}
			}
		}
		if (right) { //same logic, reversed
			for(int row = 0; row < 4; row++) {
				for(int tile = row*4+2; tile >= row*4; tile--) {
					if((box.get(tile+1).getVal() == 0 && box.get(tile).getVal() != 0)) {
						move = true;
						midmove = true;
						if(box.get(tile).getX() >= 148 * (tile%4+1) + 33) {
							box.get(tile).setX(148 * (tile%4) + 33);
							box.get(tile+1).setVal(box.get(tile).getVal());
							box.get(tile+1).combined = box.get(tile).combined;
							box.get(tile).combined = false;
							box.get(tile).setVal(0);
						}
						else box.get(tile).setX(box.get(tile).getX() + 15);
					}
					else if(box.get(tile).getVal() != 0 && box.get(tile).getVal() == box.get(tile+1).getVal() && !box.get(tile+1).combined && !box.get(tile).combined) {
						move = true;
						midmove = true;
						box.get(tile).setX(box.get(tile).getX() + 15);
						if(box.get(tile).getX() >= 148 * (tile%4+1) + 33) {
							box.get(tile).setX(148 * (tile%4) + 33);
							box.get(tile+1).setVal(box.get(tile).getVal()*2);
							box.get(tile+1).combined = true;
							box.get(tile).setVal(0);
						}
					}
				}
			}
		}
		if (up) { //same logic, vertical
			for(int row = 1; row < 4; row++) {
				for(int tile = row*4; tile < row*4+4; tile++) {
					if((box.get(tile-4).getVal() == 0 && box.get(tile).getVal() != 0)) {
						move = true;
						midmove = true;
						if(box.get(tile).getY() <= 148 * (tile/4-1) + 233) {
							box.get(tile).setY(148 * (tile/4) + 233);
							box.get(tile-4).setVal(box.get(tile).getVal());
							box.get(tile).setVal(0);
							box.get(tile-4).combined = box.get(tile).combined;
							box.get(tile).combined = false;
						}
						else box.get(tile).setY(box.get(tile).getY() - 15);
					}
					else if(box.get(tile).getVal() != 0 && box.get(tile).getVal() == box.get(tile-4).getVal()&& !box.get(tile-4).combined && !box.get(tile).combined) {
						move = true;
						midmove = true;
						box.get(tile).setY(box.get(tile).getY() - 15);
						if(box.get(tile).getY() <= 148 * (tile/4-1) + 233) {
							box.get(tile).setY(148 * (tile/4) + 233);
							box.get(tile-4).setVal(box.get(tile).getVal()*2);
							box.get(tile-4).combined = true;
							box.get(tile).setVal(0);
						}
					}
				}
			}
		}
		if (down) { //same logic, vertical and reversed
			for(int row = 2; row >= 0; row--) {
				for(int tile = row*4; tile < row*4+4; tile++) {
					if((box.get(tile+4).getVal() == 0 && box.get(tile).getVal() != 0)) {
						move = true;
						midmove = true;
						if(box.get(tile).getY() >= 148 * (tile/4+1) + 233) {
							box.get(tile).setY(148 * (tile/4) + 233);
							box.get(tile+4).setVal(box.get(tile).getVal());
							box.get(tile).setVal(0);
							box.get(tile+4).combined = box.get(tile).combined;
							box.get(tile).combined = false;
						}
						else box.get(tile).setY(box.get(tile).getY() + 15);
					}
					else if(box.get(tile).getVal() != 0 && box.get(tile).getVal() == box.get(tile+4).getVal()&& !box.get(tile+4).combined && !box.get(tile).combined) {
						move = true;
						midmove = true;
						box.get(tile).setY(box.get(tile).getY() + 15);
						if(box.get(tile).getY() >= 148 * (tile/4+1) + 233) {
							box.get(tile).setY(148 * (tile/4) + 233);
							box.get(tile+4).setVal(box.get(tile).getVal()*2);
							box.get(tile+4).combined = true;
							box.get(tile).setVal(0);
						}
					}
				}
			}
			
		}
		if(midmove && !move) { //if a move finished, spawn new block and set direction to none
			newBlock();
			left = right = up = down = false;
			midmove = false;
			//reset "combine status" to "uncombined"
			for (int k = 0; k < 16; k++)
				box.get(k).combined = false;
		} else if (!move) { //if no move occurred, set direction to none (do not spawn new block as no move was made)
			left = right = up = down = false;
			//reset "combine status" to "uncombined"
			for (int k = 0; k < 16; k++)
				box.get(k).combined = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		//if no move is currently in progress, detect arrow key presses
		if (!left && !right && !up && !down) {
			switch (e.getKeyCode()) {
			case 37: // left
				left = true;
				break;
			case 38: // up
				up = true;
				break;
			case 39: // right
				right = true;
				break;
			case 40: // down
				down = true;
				break;
			default:
				if (over)
					init();
				break;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void run() {

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		//ingame timer causes updates and repaints
		update();
		repaint();
	}
}


//class for separate tiles
class Square {

	private Rectangle sq;
	private int val; // position and value of tile
	private Color c; // color of tile
	public boolean combined; //"combine status" detects if a square has already been combined that move

	public Square(int x1, int y1, int s1) {
		sq = new Rectangle(x1, y1, s1, s1);
		val = 0;
		combined = false;
	}

	// return color of the tile depending on the value
	public Color getColor() {
		if (val == 0)
			c = Color.GRAY;
		else if (val == 2)
			c = Color.WHITE;
		else if (val == 4)
			c = new Color(253, 235, 208);
		else if (val == 8)
			c = new Color(248, 196, 113);
		else if (val == 16)
			c = new Color(230, 126, 34);
		else if (val == 32)
			c = new Color(236, 112, 99);
		else if (val == 64)
			c = new Color(255, 78, 78);
		else if (val == 128)
			c = new Color(255, 218, 113);
		else if (val == 256)
			c = new Color(255, 240, 92);
		else if (val == 512)
			c = new Color(255, 216, 40);
		else if (val == 1024)
			c = new Color(255, 211, 17);
		else if (val == 2048)
			c = new Color(255, 212, 0);
		else if (val == 4096)
			c = new Color(0, 255, 191);
		else if (val == 8192)
			c = new Color(0, 188, 178);
		else if (val == 8192 * 2)
			c = new Color(0, 164, 188);
		else if (val == 8192 * 4)
			c = new Color(0, 136, 188);
		else if (val == 8192 * 8)
			c = new Color(0, 66, 188);
		else if (val == 8192 * 16)
			c = new Color(117, 69, 255);
		return c;
	}

	public void setVal(int a) {
		val = a;
	}

	public int getVal() {
		return val;
	}

	public int getX() {
		return sq.x;
	}

	public int getY() {
		return sq.y;
	}

	public void setX(int xVal) {
		sq.x = xVal;
	}

	public void setY(int yVal) {
		sq.y = yVal;
	}
	
	public Rectangle getRect() {
		return sq;
	}
}
