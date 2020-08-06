//coded by Jason Lin

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;

public class Snake {

	public static void main(String[] args) {
		//initialize window
		JFrame frame = new JFrame("Snake Game");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//add game to window
		GameCode snake = new GameCode();
		frame.getContentPane().add(snake);
		snake.init();
		frame.pack();

		frame.setSize(330, 380);
		frame.setResizable(false);
		frame.setVisible(true);
	}
}

class GameCode extends JPanel implements ActionListener, KeyListener, Runnable {

	ArrayList<Rectangle> snake; // squares making up the player
	ArrayList<Coord> coords; // coordinates of player squares
	// sbox,back- black background, food- red square, marker- temp coordinates
	Rectangle sbox, back, food, marker; 
	//arrow key tracking, whether game started, whether player is alive, whether a game tick is currently processing
	boolean up, down, left, right, start, alive, tick; 
	//side- window side length, score tracking, count- every 10 game ticks adds 1 to sec
	int SIDE, score, count, sec;
	int hs = 0; // highscore tracking
	Scanner in; // input tracking
	Timer time; // game clock

	public void init() {
		snake = new ArrayList<>();
		coords = new ArrayList<>();
		count = sec = score = 0;
		alive = true;
		up = down = right = left = start = tick = false;
		SIDE = 310;
		marker = new Rectangle();
		//random first food location
		food = new Rectangle((int) (Math.random() * 31) * 10, (int) (Math.random() * 31) * 10, 9, 9);
		back = new Rectangle(0, 0, SIDE, SIDE);
		sbox = new Rectangle(0, SIDE + 5, SIDE, 20);
		//head of snake centered
		snake.add(new Rectangle(SIDE / 2 - 5, SIDE / 2 - 5, 9, 9));
		
		//start game timer
		time = new Timer(80, this);
		time.start();
		this.addKeyListener(this);
		setSize(SIDE, SIDE + 25);
		setFocusable(true);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g.setColor(Color.black);
		g2.fill(back);
		g2.fill(sbox);
		g.setColor(Color.red);
		g2.fill(food);
		g.setColor(Color.green);
		//draw snake
		for (int k = 0; k < snake.size(); k++) {
			g2.fill(snake.get(k));
		}
		//draw game information at bottom
		g.setColor(Color.white);
		g.setFont(new Font("TimesRoman", Font.PLAIN, 12));
		g.drawString("Score: " + score, 20, SIDE + 19);
		g.drawString("High Score: " + hs, 120, SIDE + 19);
		if (start) {
			g.drawString("Time: " + sec, 250, SIDE + 19);
		} else {
			//only displayed before game starts
			g.drawString("Time: " + 0, 250, SIDE + 19);
			g.setFont(new Font("TimesRoman", Font.PLAIN, 20));
			g.drawString("Click any arrow key to begin", 40, SIDE / 2 - 10);
		}
		if (start && !alive) {
			//only displayed after death
			g.setFont(new Font("TimesRoman", Font.BOLD, 40));
			g.drawString("Game Over", SIDE / 2 - 100, SIDE / 2 + 20);
			g.setFont(new Font("TimesRoman", Font.BOLD, 15));
			g.drawString("Press \"Space\" to play again", 62, SIDE / 2 + 50);
		}
	}

	public void update() {
		coords.clear();
		//deletes tail of snake, adds new head, simulates moving forward
		if (snake.size() > 1) {
			for (int k = snake.size() - 1; k >= 1; k--) {
				snake.get(k).x = snake.get(k - 1).x;
				snake.get(k).y = snake.get(k - 1).y;
				coords.add(new Coord(snake.get(k).x, snake.get(k).y));
			}
		}
		if (left && snake.get(0).x > 0) {
			snake.get(0).x -= 10;
		}
		if (right && snake.get(0).x < SIDE - 10) {
			snake.get(0).x += 10;
		}
		if (up && snake.get(0).y > 0) {
			snake.get(0).y -= 10;
		}
		if (down && snake.get(0).y < SIDE - 10) {
			snake.get(0).y += 10;
		}
		if (snake.get(0).x == food.x && snake.get(0).y == food.y) {
			eat();
		}
		//checks if crashed
		for (int k = 1; k < snake.size(); k++) {
			if (snake.get(0).x == snake.get(k).x && snake.get(0).y == snake.get(k).y) {
				alive = false;
			}
		}
		if (snake.get(0).x == marker.x && snake.get(0).y == marker.y) {
			alive = false;
		}
		marker.x = snake.get(0).x;
		marker.y = snake.get(0).y;
	}

	public void eat() {
		boolean same = false;
		score += 10;
		//makes sure new food does not spawn inside the player
		do {
			food.x = (int) (Math.random() * 31) * 10;
			food.y = (int) (Math.random() * 31) * 10;
			int k = 0;
			same = false;
			while (!same && k < coords.size()) {
				if (food.x == coords.get(k).getX() && food.y == coords.get(k).getY())
					same = true;
				k++;
			}
		} while (same);
		//snake length grows by 1
		snake.add(new Rectangle(1000, 1000, 9, 9));
		snake.add(new Rectangle(1000, 1000, 9, 9));
	}

	public void s() {
		//game started
		start = true;
		alive = true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//internal clock, updates game every timer tick
		if (alive) {
			tick = false;
			update();
			repaint();
			count++;
			if (count % 10 == 0)
				sec++;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		//arrow key tracking, makes sure player cannot double back (ex: going left, click right will not be allowed)
		if (!tick) {
			switch (e.getKeyCode()) {
			case 37: // left
				if (!right) {
					if (!start) {
						s();
					}
					left = true;
					up = false;
					down = false;
					right = false;
					tick = true;
				}
				break;
			case 38: // up
				if (!down) {
					if (!start) {
						s();
					}
					up = true;
					left = false;
					down = false;
					right = false;
					tick = true;
				}
				break;
			case 39: // right
				if (!left) {
					if (!start) {
						s();
					}
					right = true;
					up = false;
					down = false;
					left = false;
					tick = true;
				}
				break;
			case 40: // down
				if (!up) {
					if (!start) {
						s();
					}
					down = true;
					up = false;
					left = false;
					right = false;
					tick = true;
				}
				break;
			default:
				break;
			}
		}
		//resets game if space is pressed
		if (!alive && start) {
			switch (e.getKeyCode()) {
			case 32:
				if (score > hs)
					hs = score;
				time.stop();
				init();
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void run() {
		
	}

}

class Coord {
	private int x;
	private int y;

	public Coord(int x1, int y1) {
		x = x1;
		y = y1;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

}