//coded by Jason Lin

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;

public class Pong {

	public static void main(String[] args) {
		// initialize window
		JFrame frame = new JFrame("Pong Game");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// add game to window
		GameCode pong = new GameCode();
		frame.getContentPane().add(pong);
		pong.init();
		frame.pack();

		frame.setSize(680, 530);
		frame.setResizable(false);
		frame.setVisible(true);
	}
}

class GameCode extends JPanel implements ActionListener, KeyListener, MouseListener {

	private Rectangle player;
	private Rectangle ai; //computer
	private Rectangle back; //background
	private Rectangle ball; 
	private ArrayList<String> keysPressed; //stores which keys are currently pressed
	private int playerScore = 0;
	private int aiScore = 0; //computer score
	//aiVel- computer movement speed (changes based on difficulty)
	//yVel,xVel- directional components of ball velocity
	//totalVel- constant used to calculate xVel based on yVel (changes based on difficulty)
	//aiRandomizer- randomizes where the computer hits the ball to randomize yVel on impact
	//pauseTimer- pauses on restart
	private int aiVel, yVel, xVel, totalVel, aiRandomizer, pauseTimer;
	private boolean start, pause, controls;
	final int HEIGHT = 480;
	final int WIDTH = 640;

	public void init() {
		Timer time = new Timer(10, this); //game clock
		time.start();
		requestFocus();
		this.addKeyListener(this);
		this.addMouseListener(this);
		setFocusable(true);

		keysPressed = new ArrayList<String>();
		player = new Rectangle(30, 210, 16, 80);
		ai = new Rectangle(620, 210, 16, 80);
		back = new Rectangle(10, 10, WIDTH, HEIGHT);
		ball = new Rectangle(322, 242, 16, 16);
		start = false;
		pause = false;
		controls = false;
		pauseTimer = 0;
		aiRandomizer = 0;
		
		yVel = (int) (Math.random() * 7 - 3); //yVel set to random # between -3 - 3
		//xVel calculated using pythag to keep constant total speed, randomize left or right
		if (Math.random() * 2 > 1) { 
			xVel = (int) Math.sqrt(49 - yVel * yVel) + 1;
		} else {
			xVel = (int) (Math.sqrt(49 - yVel * yVel) + 1) * -1;
		}
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g.setColor(Color.black); //fill background
		g2.fill(back);

		g.setColor(Color.white);

		if (!start && !controls) { //start screen (choose difficulty)
			g.setFont(new Font("Helvetica", Font.BOLD, 100));
			g.drawString("PONG", 190, 200);
			g.setFont(new Font("Helvetica", Font.PLAIN, 20));
			g.drawString("Choose your difficulty:", 240, 290);
			g.drawString("Easy", 150, 360);
			g.drawString("Normal", 300, 360);
			g.drawString("Hard", 465, 360);
			g.drawString("Controls", 295, 420);
			g.drawRect(WIDTH / 2 - 54, 332, 130, 40);
			g.drawRect(110, 332, 130, 40);
			g.drawRect(420, 332, 130, 40);
			g.drawRect(WIDTH / 2 - 54, 392, 130, 40);
		} else if(controls){
			g.setFont(new Font("Helvetica", Font.BOLD, 40));
			g.drawString("CONTROLS", 220, 100);
			g.setFont(new Font("Helvetica", Font.PLAIN, 40));
			g.drawString("Move up:        W or Up Arrow", 60, 200);
			g.drawString("Move down:     S or Down Arrow", 40, 300);
			g.setFont(new Font("Helvetica", Font.PLAIN, 20));
			g.drawString("Back to Menu", 275, 380);
			g.drawRect(WIDTH / 2 - 54, 355, 140, 40);
		}else { //draw game
			g2.fill(player);
			g2.fill(ai);
			g2.fill(ball);
			int nety = 10; //draw middle net
			for (int i = 1; i <= 12; i++) {
				g.fillRect(328, nety, 4, 18);
				nety += 42;
			}
			g.setFont(new Font("TimesRoman", Font.PLAIN, 25));
			g.drawString(Integer.toString(playerScore), 55, 40);
			g.drawString(Integer.toString(aiScore), 595 - ((Integer.toString(aiScore).length() - 1) * 10), 40);
		}
	}

	public void update() {
		if (pauseTimer == 30) //pauses for a moment after each point
			pause = false;
		if (pause) {
			pauseTimer++;
		}
		if (start) {
			//player movement
			if (keysPressed.contains("up") && player.y > 10) {
				player.y -= 5;
			}
			if (keysPressed.contains("down") && player.y < HEIGHT - player.height + 10) {
				player.y += 5;
			}
			
			//computer movement with randomized point of contact
			if (ai.y + 32 + aiRandomizer < ball.y && ai.y < HEIGHT - 70)
				ai.y += aiVel;
			if (ai.y + 32 + aiRandomizer > ball.y && ai.y > 10)
				ai.y -= aiVel;
			
			//recalculating yVel based on point of contact, calculating xVel based off yVel and change ball direction
			if (ball.x < player.x + 16 && ball.x > player.x - 16 && ball.y < player.y + 80 && ball.y > player.y - 16) {
				yVel = (ball.y - player.y - 30) / 7;
				xVel = ((int) Math.sqrt(totalVel - yVel * yVel) + 1);
			}
			if (ball.x < ai.x + 16 && ball.x > ai.x - 16 && ball.y < ai.y + 80 && ball.y > ai.y - 16) {
				yVel = (ball.y - ai.y - 30) / 6;
				xVel = -((int) Math.sqrt(totalVel - yVel * yVel) + 1);
				aiRandomizer = (int) (Math.random() * 104 - 52); //reset randomizer for next hit
			}
			
			if (ball.y <= 10 || ball.y >= HEIGHT - 6) //bounce on top or bottom
				yVel = -yVel;
			if (ball.x < -16) { //out of bounds, point for computer
				aiScore++;
				reset();
			}
			if (ball.x > WIDTH) { //out of bounds, point for player
				playerScore++;
				reset();
			}
			if (!pause) { //ball movement
				ball.y += yVel;
				ball.x += xVel;
			}
		}
	}

	public void reset() {
		//reset everything after point scored
		ball.x = 322;
		ball.y = 242;
		yVel = (int) (Math.random() * 7 - 3);
		if (Math.random() * 2 > 1) {
			xVel = (int) Math.sqrt(49 - yVel * yVel) + 1;
		} else {
			xVel = (int) (Math.sqrt(49 - yVel * yVel) + 1) * -1;
		}
		pause = true;
		pauseTimer = 0;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//internal game clock
		update();
		repaint();
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		//detect keyboard input
		switch (e.getKeyCode()) {
		case 83:
			if (!keysPressed.contains("down"))
				keysPressed.add("down");
			break;
		case 87:
			if (!keysPressed.contains("up"))
				keysPressed.add("up");
			break;
		case 40:
			if (!keysPressed.contains("down"))
				keysPressed.add("down");
			break;
		case 38:
			if (!keysPressed.contains("up"))
				keysPressed.add("up");
			break;
		default:
			;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == 87 && keysPressed.contains("up")) {
			keysPressed.remove("up");
		}
		if (e.getKeyCode() == 83 && keysPressed.contains("down")) {
			keysPressed.remove("down");
		}
		if (e.getKeyCode() == 38 && keysPressed.contains("up")) {
			keysPressed.remove("up");
		}
		if (e.getKeyCode() == 40 && keysPressed.contains("down")) {
			keysPressed.remove("down");
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		//detect difficulty selected based on mouse click, difficulty changes computer movement speed and ball speed
		if (!start && !controls) {
			if (e.getX() >= 110 && e.getX() <= 240 && e.getY() >= 332 && e.getY() <= 372) {
				start = true;
				aiVel = 2;
				totalVel = 49;
				pause = true;
			}
			if (e.getX() >= WIDTH / 2 - 54 && e.getX() <= WIDTH / 2 - 54 + 130 && e.getY() >= 332 && e.getY() <= 372) {
				start = true;
				aiVel = 3;
				totalVel = 72;
				pause = true;
			}
			if (e.getX() >= 420 && e.getX() <= 550 && e.getY() >= 332 && e.getY() <= 372) {
				start = true;
				aiVel = 4;
				totalVel = 95;
				pause = true;
			}
			if(e.getX() >= WIDTH / 2 - 54 && e.getX() <= WIDTH / 2 - 54 + 130 && e.getY() >= 392 && e.getY() <= 432) {
				controls = true;
			}
		} else if(controls) {
			if(e.getX() >= WIDTH / 2 - 54 && e.getX() <= WIDTH / 2 - 54 + 140 && e.getY() >= 355 && e.getY() <= 395) {
				controls = false;
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

}