package minesweeper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

public class Minesweeper {

	static JFrame frame;

	public static void main(String[] args) throws IOException {

		// creates a window for settings
		JFrame f = new JFrame("Minesweeper Options");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// button to start game
		JButton b = new JButton("Enter");
		b.setBounds(70, 200, 140, 40);

		// text
		JLabel wid = new JLabel();
		wid.setText("Width:");
		wid.setBounds(50, 10, 100, 100);

		JLabel hei = new JLabel();
		hei.setText("Height:");
		hei.setBounds(50, 50, 100, 100);

		JLabel min = new JLabel();
		min.setText("Mines:");
		min.setBounds(50, 90, 100, 100);

		JLabel label1 = new JLabel();
		label1.setBounds(10, 115, 400, 115);

		JLabel label2 = new JLabel();
		label2.setBounds(10, 125, 400, 125);

		// text fields to take input for variable width, height, # of mines
		JTextField widtext = new JTextField();
		widtext.setBounds(100, 45, 130, 30);

		JTextField heitext = new JTextField();
		heitext.setBounds(100, 85, 130, 30);

		JTextField mintext = new JTextField();
		mintext.setBounds(100, 125, 130, 30);

		f.add(label1);
		f.add(label2);
		f.add(widtext);
		f.add(heitext);
		f.add(mintext);
		f.add(wid);
		f.add(hei);
		f.add(min);
		f.add(b);
		f.setSize(300, 300);
		f.setLayout(null);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLocationRelativeTo(null);
		f.setResizable(false);

		// actionlistener detects when the button is pressed
		b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// will try to take text field inputs as numerical variables, if fails to parse
				// as Integer, display fail message
				try {
					if (Integer.parseInt(widtext.getText()) >= 5 && Integer.parseInt(widtext.getText()) <= 30
							&& Integer.parseInt(heitext.getText()) >= 5 && Integer.parseInt(heitext.getText()) <= 30
							&& Integer.parseInt(mintext.getText()) >= 1
							&& Integer.parseInt(mintext.getText()) <= Integer.parseInt(heitext.getText())
									* Integer.parseInt(widtext.getText()) - 1) {

						int x = Integer.parseInt(widtext.getText());
						int y = Integer.parseInt(heitext.getText());
						int numMines = Integer.parseInt(mintext.getText());

						// create new window for game GUI
						frame = new JFrame("Minesweeper");
						frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

						// initiate class with game's code, passes on input variables
						GameCode game = new GameCode(x, y, numMines);

						// adds game code to new window
						frame.getContentPane().add(game);

						// initialize game
						try {
							game.init();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						frame.pack();

						// try best to center window on screen
						Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
						frame.setLocation((int) (screenSize.getWidth() / 2 - (x * 15.5)),
								(int) (screenSize.getHeight() / 2 - (y * 18)));

						frame.setSize(x * 31 + 20, y * 31 + 140);
						f.setLayout(null);
						frame.setResizable(false);
						frame.setVisible(true);

						// delete settings window
						f.dispose();

					} else { // if the numbers entered do not fall within a certain range, display error
								// message
						label1.setText("Width and height must be between 5-30.");
						label2.setText("Mines must be between 1 and total tiles - 1.");
					}
				} catch (NumberFormatException e) { // display fail message if inputs are not integers
					label1.setText("Please enter numbers.");
				}

			}
		});

	}

	// when player clicks settings in-game, delete game window and re-launch
	// settings window
	public static void restart() {
		frame.dispose();
		try {
			main(null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class GameCode extends JPanel implements ActionListener, Runnable, MouseListener {

	private static final long serialVersionUID = 1L;

	// to color the background
	Rectangle back;
	int[][] board;
	// show- tells if the tile has been revealed, flagged- tells if the tile has
	// been flagged
	boolean[][] show, flagged;
	// wid, hei, numMines- user inputs from settings window
	// seconds- timer, numFlags- number of flags placed, correctFlags- number of
	// correct flags placed
	int wid, hei, numMines, seconds, numFlags, correctFlags;
	// ingame timer
	Timer time;
	// sec- used during board generation to track if tile already has a mine there
	// down- tracks if a mouse button is being held down
	// lose, win- tracks if hit a mine or cleared all mines successfully
	// first- tracks whether first move has been made, re- tracks if have restarted
	boolean sec, down, lose, win, first, re;
	// store mine x and y values
	ArrayList<Integer> minesx, minesy;
	// store which mouse buttons were clicked
	ArrayList<Integer> keys;
	// image resources
	BufferedImage mine1, mine2, flag;

	/*
	 * lays out all 4 possible combinations of left and right clicks before mouse release
	 * NOTE: although m12 and m21 are both clicking both mouse buttons, 
	 * in one case, mouse1 is clicked first, and the other, mouse2 is clicked first
	 * both cases need to be noted
	 * 
	 */
	final ArrayList<Integer> m1 = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1L;

		{
			add(1);
		}
	};
	final ArrayList<Integer> m2 = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1L;

		{
			add(3);
		}
	};
	final ArrayList<Integer> m12 = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1L;

		{
			add(1);
			add(3);
		}
	};
	final ArrayList<Integer> m21 = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1L;

		{
			add(3);
			add(1);
		}
	};

	// initialize variables with inputs from settings
	public GameCode(int w, int h, int num) {
		wid = w;
		hei = h;
		numMines = num;
	}

	// initialize all other variables
	public void init() throws IOException {
		back = new Rectangle(0, 0, wid * 30, hei * 30);
		minesx = new ArrayList<Integer>();
		minesy = new ArrayList<Integer>();
		keys = new ArrayList<Integer>();

		// show and flagged initialized with a "frame" of unused tiles to avoid
		// indexOutOfBounds errors
		// greatly reduces number of edge-case checks needed to perform on each move
		show = new boolean[wid + 2][hei + 2];
		flagged = new boolean[wid + 2][hei + 2];
		down = false;
		lose = false;
		first = false;
		re = false;
		win = false;

		correctFlags = 0;
		seconds = 0;
		numFlags = 0;

		// load in image files from resource folder
		mine1 = ImageIO.read(getClass().getClassLoader().getResource("mine.png"));
		mine2 = ImageIO.read(getClass().getClassLoader().getResource("mine2.png"));
		flag = ImageIO.read(getClass().getClassLoader().getResource("flag.png"));

		// start timer
		time = new Timer(100, this);
		time.start();

		// add mouseListener to window to detect inputs
		this.addMouseListener(this);
		setFocusable(true);
	}

	public void restart() throws IOException {
		// resets all variables to initial values, clearing the board
		minesx.clear();
		minesy.clear();
		keys.clear();
		down = false;
		lose = false;
		first = false;
		win = false;
		seconds = 0;
		numFlags = 0;
		correctFlags = 0;

		time.restart();

		for (int k = 0; k < wid + 2; k++) {
			for (int i = 0; i < hei + 2; i++) {
				show[k][i] = false;
				flagged[k][i] = false;
			}
		}

	}

	@Override
	// handles all the graphics
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		// fill the background
		g.setColor(Color.black);
		g2.fill(back);

		// count the number of flags placed
		int temp = 0;
		for (int k = 1; k < flagged.length - 1; k++) {
			for (int i = 1; i < flagged[0].length - 1; i++) {
				if (flagged[k][i])
					temp++;
			}
		}
		numFlags = temp;

		// drawing surrounding graphics including time elapsed and number of flags
		// placed
		g.fillRect(wid * 30 - 45, hei * 30 + 10, 43, 25);
		g.fillRect(5, hei * 30 + 10, 43, 25);
		g.setFont(new Font("Helvetica", Font.BOLD, 20));
		g.setColor(Color.RED);
		g.drawString(String.format("%03d", seconds / 10), wid * 30 - 40, hei * 30 + 30);
		g.drawString(String.format("%03d", (numMines - numFlags)), 10, hei * 30 + 30);
		g.setColor(Color.gray);
		g.fillRect(wid * 15 + 5, hei * 30 + 50, wid * 15 - 10, 35);
		g.fillRect(5, hei * 30 + 50, wid * 15 - 10, 35);

		// draws buttons at bottom of game window
		g.setColor(Color.black);
		g.setFont(new Font("Courier", Font.BOLD, 12));
		g.drawString("New Map", 15, hei * 30 + 73);
		g.drawString("Settings", wid * 15 + 10, hei * 30 + 73);

		// draws each tile
		for (int k = 1; k < wid + 1; k++) {
			for (int i = 1; i < hei + 1; i++) {
				g.setColor(Color.white);
				// if tile has been revealed, draw as different color
				if (show[k][i])
					g.setColor(Color.LIGHT_GRAY);
				g.fillRect((k - 1) * 30 + 1, (i - 1) * 30 + 1, 28, 28);

				// if tile has been flagged, draw flag in place of tile
				if (flagged[k][i]) {
					g.drawImage(flag, (k - 1) * 30 + 1, (i - 1) * 30 + 1, 28, 28, null);
				}

				// if revealed, draw number of mines surrounding a tile
				g.setFont(new Font("Helvetica", Font.BOLD, 20));
				if (show[k][i] && board[k][i] == 1) {
					g.setColor(Color.blue);
					g.drawString(((Integer) board[k][i]).toString(), (k - 1) * 30 + 10, (i - 1) * 30 + 23);
				}
				if (show[k][i] && board[k][i] == 2) {
					g.setColor(new Color(0, 130, 0));
					g.drawString(((Integer) board[k][i]).toString(), (k - 1) * 30 + 10, (i - 1) * 30 + 23);
				}
				if (show[k][i] && board[k][i] == 3) {
					g.setColor(new Color(255, 60, 60));
					g.drawString(((Integer) board[k][i]).toString(), (k - 1) * 30 + 10, (i - 1) * 30 + 23);
				}
				if (show[k][i] && board[k][i] == 4) {
					g.setColor(new Color(10, 0, 120));
					g.drawString(((Integer) board[k][i]).toString(), (k - 1) * 30 + 10, (i - 1) * 30 + 23);
				}
				if (show[k][i] && board[k][i] == 5) {
					g.setColor(new Color(121, 0, 18));
					g.drawString(((Integer) board[k][i]).toString(), (k - 1) * 30 + 10, (i - 1) * 30 + 23);
				}
				if (show[k][i] && board[k][i] == 6) {
					g.setColor(new Color(0, 216, 230));
					g.drawString(((Integer) board[k][i]).toString(), (k - 1) * 30 + 10, (i - 1) * 30 + 23);
				}
				if (show[k][i] && board[k][i] == 7) {
					g.setColor(Color.black);
					g.drawString(((Integer) board[k][i]).toString(), (k - 1) * 30 + 10, (i - 1) * 30 + 23);
				}
				if (show[k][i] && board[k][i] == 8) {
					g.setColor(Color.DARK_GRAY);
					g.drawString(((Integer) board[k][i]).toString(), (k - 1) * 30 + 10, (i - 1) * 30 + 23);
				}
			}
		}

		// if completed board, draw win message
		if (win) {
			g.setColor(Color.white);
			g.fillRect(wid * 15 - 50, hei * 15 - 10, 95, 25);
			g.setColor(Color.red);
			g.setFont(new Font("TimesRoman", Font.BOLD, 25));
			g.drawString("You Win", wid * 15 - 50, hei * 15 + 10);
		}

		// if died, draw lose message and reveal all mine locations
		if (lose) {
			for (int j = 0; j < minesx.size(); j++) {
				if (!flagged[minesx.get(j)][minesy.get(j)])
					g.drawImage(mine2, (minesx.get(j) - 1) * 30 + 1, (minesy.get(j) - 1) * 30 + 1, 28, 28, null);
				if (show[minesx.get(j)][minesy.get(j)])
					g.drawImage(mine1, (minesx.get(j) - 1) * 30 + 1, (minesy.get(j) - 1) * 30 + 1, 28, 28, null);
			}
			g.setColor(Color.white);
			g.fillRect(wid * 15 - 50, hei * 15 - 10, 100, 25);
			g.setColor(Color.red);
			g.setFont(new Font("TimesRoman", Font.BOLD, 25));
			g.drawString("You Lose", wid * 15 - 50, hei * 15 + 10);
		}

	}

	// generates board
	public void gen(int x1, int y1) {
		// first time board generates, declare variable
		if (!re) {
			board = new int[wid + 2][hei + 2];
			// after first time, reset board to values of 0
		} else {
			for (int k = 0; k < wid + 2; k++) {
				for (int i = 0; i < hei + 2; i++) {
					board[k][i] = 0;
				}
			}
		}

		// repeat until all mines have been placed in unique locations
		for (int k = 0; k < numMines; k++) {
			do {
				// randomizes location of mine
				int x = (int) (Math.random() * wid) + 1;
				int y = (int) (Math.random() * hei) + 1;
				// if statement ensures that the first square clicked WILL NOT contain a mine
				if ((x < x1 / 30 - 1 || x > x1 / 30 + 1) || (y < y1 / 30 - 1 || y > y1 / 30 + 1)) {
					// if the tile already contains a mine, rerandomize
					if (board[x][y] == -1) {
						sec = true;
					} else { // if not, place a mine there (value of -1)
						sec = false;
						board[x][y] = -1;
						minesx.add(x);
						minesy.add(y);
					}
				} else {
					sec = true;
				}
			} while (sec); // repeat until finds a location where no mine has previously been placed
		}

		// count- tracks number of mines surrounding a tile
		int count;

		// iterate through entire board
		for (int k = 0; k < wid + 2; k++) {
			for (int i = 0; i < hei + 2; i++) {
				// ALL edge tiles are set to a meaningless value of -2; avoids indexOutOfBounds
				// errors and reduces edge cases
				if (k == 0 || k == wid + 1 || i == 0 || i == hei + 1) {
					board[k][i] = -2;
				} else if (board[k][i] != -1) { // if the tile does not contain a mine, count how many mines surround it
					count = 0;
					for (int k1 = k - 1; k1 < k + 2; k1++) {
						for (int i1 = i - 1; i1 < i + 2; i1++) {
							if (board[k1][i1] == -1)
								count++;
						}
					}
					// set the tile's value to the mines surrounding count
					board[k][i] = count;
				}
			}
		}
	}

	// updates game after mouse clicks
	public void update(ArrayList<Integer> key, int x, int y) {

		if (!lose && y < hei * 30 + 30) {
			if (key.equals(m1)) { // if only mouse1 was clicked, call reveal method for that tile; if it contains
									// a mine, lose game
				reveal(x / 30, y / 30);
				if (board[x / 30][y / 30] == -1) {
					show[x / 30][y / 30] = true;
					lose = true;
				}
			}

			// if only mouse2 was clicked, toggle flag status of tile
			if (key.equals(m2)) {
				if (!show[x / 30][y / 30])
					flagged[x / 30][y / 30] = !flagged[x / 30][y / 30];

				if (flagged[x / 30][y / 30]) { // if correctly flagged, count towards correctFlags
					for (int k = 0; k < minesx.size(); k++) {
						if (x / 30 == minesx.get(k) && y / 30 == minesy.get(k))
							correctFlags++;
					}
				} else { // if a correct flag is removed, subtract from correctFlags
					for (int k = 0; k < minesx.size(); k++) {
						if (x / 30 == minesx.get(k) && y / 30 == minesy.get(k))
							correctFlags--;
					}
				}
			}

			//if double click, check if all surrounding tiles are correct; if yes, reveal remaining tiles
			if (key.equals(m12) || key.equals(m21)) {
				//temp variables translating mouse click location to tile x,y coords
				int xx = x / 30;
				int yy = y / 30;
				//count- counts number of correctly flagged tiles surrounding tile
				int count = 0;
				//count2- counts number of tiles flagged surrounding tile
				int count2 = 0;
				//check all tiles surrounding given tile
				if (flagged[xx - 1][yy - 1] && board[xx - 1][yy - 1] == -1)
					count++;
				if (flagged[xx - 1][yy] && board[xx - 1][yy] == -1)
					count++;
				if (flagged[xx - 1][yy + 1] && board[xx - 1][yy + 1] == -1)
					count++;
				if (flagged[xx][yy - 1] && board[xx][yy - 1] == -1)
					count++;
				if (flagged[xx][yy + 1] && board[xx][yy + 1] == -1)
					count++;
				if (flagged[xx + 1][yy - 1] && board[xx + 1][yy - 1] == -1)
					count++;
				if (flagged[xx + 1][yy] && board[xx + 1][yy] == -1)
					count++;
				if (flagged[xx + 1][yy + 1] && board[xx + 1][yy + 1] == -1)
					count++;
				if (flagged[xx - 1][yy - 1])
					count2++;
				if (flagged[xx - 1][yy])
					count2++;
				if (flagged[xx - 1][yy + 1])
					count2++;
				if (flagged[xx][yy - 1])
					count2++;
				if (flagged[xx][yy + 1])
					count2++;
				if (flagged[xx + 1][yy - 1])
					count2++;
				if (flagged[xx + 1][yy])
					count2++;
				if (flagged[xx + 1][yy + 1])
					count2++;
				
				//if the number of correctly flagged tiles matches the number of surrounding mines, reveal ALL surrounding tiles
				if (count == board[xx][yy]) {
					int temp2 = board[x / 30][y / 30];
					board[x / 30][y / 30] = 0;
					show[x / 30][y / 30] = false;
					reveal(x / 30, y / 30);
					board[x / 30][y / 30] = temp2;
				} else if (count2 >= board[xx][yy]) {  //if number of flags surrounding exceeds number of surrounding mines, lose game
					lose = true;
				}
			}
		}
		
		if (key.equals(m1)) {
			//if mouse1 was used to click settings button, reopen the SETTINGS window, ending game
			if (x > 5 && x < wid * 15 + 25 && y > hei * 30 + 80 && y < hei * 30 + 115) {
				try {
					restart();
					re = true;
				} catch (IOException e) {

				}
			} else if (x > wid * 15 + 5 && x < wid * 30 + 25 && y > hei * 30 + 80 && y < hei * 30 + 115) {
				//if mouse1 was used to click restart button, restart game
				Minesweeper.restart();
			}
		}
		
		repaint();
	}

	//reveals tile values
	public void reveal(int x, int y) {
		//if not already revealed and
		if (!show[x][y]) {
			//there are NO mines surrounding the tile, reveal ALL surrounding tiles
			//NOTE: this mechanic opens patches of empty tiles
			if (board[x][y] == 0) {
				show[x][y] = true;
				reveal(x - 1, y - 1);
				reveal(x - 1, y);
				reveal(x - 1, y + 1);
				reveal(x, y - 1);
				reveal(x, y + 1);
				reveal(x + 1, y - 1);
				reveal(x + 1, y);
				reveal(x + 1, y + 1);
			} else if (board[x][y] != -1 && board[x][y] != -2) {
				//if the tile has surrounding mines, then ONLY reveal how many mines surround it
				show[x][y] = true;
			}
		}
	}

	@Override
	//every tick of game timer, repaint and update time elapsed
	public void actionPerformed(ActionEvent e) {
		repaint();
		if (first && seconds < 999 * 10 && !lose) {
			seconds++;
		}
		
		//if the number of correctly flagged tiles equals the total number of mines, run win check
		if (correctFlags == numFlags) {
			win = true;
			//if all squares are either revealed or correctly flagged, win game
			for (int i = 1; i < show.length - 1; i++) {
				for (int k = 1; k < show[i].length - 1; k++) {
					if (!(show[i][k] || flagged[i][k])) {
						win = false;
					}
				}
			}
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	//creates a queue of mouse inputs, clear queue on next mouse input
	//NOTE: this allows for double clicks
	public void mousePressed(MouseEvent arg0) {
		if (!down) {
			down = true;
			keys.clear();
		}
		keys.add(arg0.getButton());
	}

	@Override
	//when mouse button is released, call update on the tile that the cursor is on
	public void mouseReleased(MouseEvent arg0) {
		//sends board generation method cursor location on first click to ensure there will NEVER be a mine on first click location
		if (!first) {
			gen(arg0.getX() + 30, arg0.getY() + 30);
			first = true;
		}
		down = false;
		update(keys, arg0.getX() + 30, arg0.getY() + 30);
	}
}