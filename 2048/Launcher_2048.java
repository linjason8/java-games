
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

class GameCode extends JPanel implements KeyListener, Runnable {

	Rectangle back; // background
	ArrayList<Square> box; //
	Square logo; // 2048 icon in top left
	boolean move, over; // move- tracks if move was made after key press, over- tracks if game over
	int score; // tracks score

	public void init() {
		back = new Rectangle(25, 225, 600, 600);
		box = new ArrayList<>();
		logo = new Square(25, 25, 150);
		logo.setVal(2048);
		score = 0;

		over = false;
		move = false;

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
		g.setFont(new Font("TimesRoman", Font.BOLD, 50));
		for (int k = 0; k < 16; k++) {
			g.setColor(box.get(k).getColor()); // get color of each square
			g2.fill(box.get(k).getRect());
			if (box.get(k).getVal() != 0) {
				if (box.get(k).getVal() >= 8) // change color of text to be legible
					g.setColor(Color.WHITE);
				else
					g.setColor(Color.BLACK);
				g.drawString(((Integer) box.get(k).getVal()).toString(), // display value of tiles at a centered
																			// location
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

	public void left() {
		for (int i = 0; i < 4; i++) { // iterate the 4 rows
			for (int x = 0; x < 4; x++) { // repeat each row 4 times to ensure everything hits left wall
				int k = i * 4; // iterate left to right for each row
				while (k < (i + 1) * 4 - 1) { // while k is in the same row
					// if the box exists, has the same value as box to the right, and both have the
					// same value, combine
					if (box.get(k).getVal() != 0 && box.get(k).combine && box.get(k + 1).combine
							&& box.get(k + 1).getVal() == box.get(k).getVal()) {
						box.get(k).setVal(box.get(k).getVal() * 2); // double left box value
						box.get(k + 1).setVal(0); // set right box to 0
						box.get(k).combine = false; // set left box to be unable to combine again in the same move
						move = true; // detect that a move has been made
						score += box.get(k).getVal(); // add value to score
					}
					k++;
				}

				while (k > i * 4) { // if the box to the left has a value of 0, move box over to the left
					if (box.get(k).getVal() != 0 && box.get(k - 1).getVal() == 0) {
						box.get(k - 1).setVal((box.get(k)).getVal()); // set left box value to right box value
						box.get(k).setVal(0); // set right box to 0
						move = true; //detect a move has been made
					}
					k--;
				}
				repaint();
			}
		}
		if (move) //if any tile moved, spawn a new block
			newBlock();
	}

	public void right() { // same logic
		for (int i = 0; i < 4; i++) {
			for (int x = 0; x < 4; x++) {
				int k = 4 * (i + 1) - 1;
				while (k > i * 4) {
					if (box.get(k).getVal() != 0 && box.get(k).combine && box.get(k - 1).combine
							&& box.get(k - 1).getVal() == box.get(k).getVal()) {
						box.get(k).setVal(box.get(k).getVal() * 2);
						box.get(k - 1).setVal(0);
						box.get(k).combine = false;
						move = true;
						score += box.get(k).getVal();
					}
					k -= 1;
				}
				while (k < (i + 1) * 4 - 1) {
					if (box.get(k).getVal() != 0 && box.get(k + 1).getVal() == 0) {
						box.get(k + 1).setVal((box.get(k)).getVal());
						box.get(k).setVal(0);
						move = true;
					}
					k += 1;
				}
				repaint();
			}
		}
		if (move)
			newBlock();
	}

	public void up() { // same logic
		for (int i = 0; i < 4; i++) {
			for (int x = 0; x < 4; x++) {
				int k = i;
				while (k < 12) {
					if (box.get(k).getVal() != 0 && box.get(k).combine && box.get(k + 4).combine
							&& box.get(k + 4).getVal() == box.get(k).getVal()) {
						box.get(k).setVal(box.get(k).getVal() * 2);
						box.get(k + 4).setVal(0);
						box.get(k).combine = false;
						move = true;
						score += box.get(k).getVal();
					}
					k += 4;
				}
				while (k > 3) {
					if (box.get(k).getVal() != 0 && box.get(k - 4).getVal() == 0) {
						box.get(k - 4).setVal((box.get(k)).getVal());
						box.get(k).setVal(0);
						move = true;
					}
					k -= 4;
				}
				repaint();
			}
		}
		if (move)
			newBlock();
	}

	public void down() { // same logic
		for (int i = 0; i < 4; i++) {
			for (int x = 0; x < 4; x++) {
				int k = 15 - i;
				while (k > 3) {
					if (box.get(k).getVal() != 0 && box.get(k).combine && box.get(k - 4).combine
							&& box.get(k - 4).getVal() == box.get(k).getVal()) {
						box.get(k).setVal(box.get(k).getVal() * 2);
						box.get(k - 4).setVal(0);
						box.get(k).combine = false;
						move = true;
						score += box.get(k).getVal();
					}
					k -= 4;
				}
				while (k < 12) {
					if (box.get(k).getVal() != 0 && box.get(k + 4).getVal() == 0) {
						box.get(k + 4).setVal((box.get(k)).getVal());
						box.get(k).setVal(0);
						move = true;
					}
					k += 4;
				}
				repaint();
			}
		}
		if (move)
			newBlock();
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {

		move = false; // reset boolean detecting if move was made on key press
		for (int k = 0; k < 16; k++)
			box.get(k).combine = true; // all tiles are available to be combined (no tiles have yet been combined)
		switch (e.getKeyCode()) {
		case 37: // left
			left();
			break;
		case 38: // up
			up();
			break;
		case 39: // right
			right();
			break;
		case 40: // down
			down();
			break;
		default:
			if (over) // restart if key pressed after death
				init();
			break;
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void run() {
	}
}

// class for separate tiles
class Square {

	private Rectangle sq;
	private int x, y, val; // position and value of tile
	private Color c; // color of tile
	public boolean combine; // true if tile is available to combine (if it has not been combined yet)

	public Square(int x1, int y1, int s1) {
		x = x1;
		y = y1;
		sq = new Rectangle(x1, y1, s1, s1);
		val = 0;
		combine = true;
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
		return x;
	}

	public int getY() {
		return y;
	}

	public Rectangle getRect() {
		return sq;
	}
}
