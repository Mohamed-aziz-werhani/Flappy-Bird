package flappyBird;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.Timer;

public class FlappyBird implements ActionListener, MouseListener, KeyListener
{

	public static FlappyBird flappyBird;

	public final int WIDTH = 800, HEIGHT = 800;

	public Renderer renderer;

	public final int Population = 50;
	
	public ArrayList<Rectangle> birds;
	
	public ArrayList<Brain> brains;

	public ArrayList<Rectangle> columns;
	
	public int[] yMotion;

	public int ticks; 
	public int[] score;

	public boolean gameOver, started;

	public Random rand;

	public FlappyBird()
	{
		JFrame jframe = new JFrame();
		Timer timer = new Timer(20, this);

		renderer = new Renderer();
		rand = new Random();

		jframe.add(renderer);
		jframe.setTitle("Flappy Bird");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setSize(WIDTH, HEIGHT);
		jframe.addMouseListener(this);
		jframe.addKeyListener(this);
		jframe.setResizable(false);
		jframe.setVisible(true);
		
		score = new int[Population];
		yMotion = new int[Population];
		columns = new ArrayList<Rectangle>();
		
		birds = new ArrayList<Rectangle>();
		for (int i=0; i<Population; i++) {
			birds.add( new Rectangle(WIDTH / 2 - 100, HEIGHT / 2 - 10 , 20, 20));
		}
		
		brains = new ArrayList<Brain>();
		for (int i=0; i<Population; i++) {
			brains.add( new Brain(4, 6, 1));
		}


		addColumn(true);
		addColumn(true);
		addColumn(true);
		addColumn(true);

		timer.start();
	}

	public void addColumn(boolean start)
	{
		int space = 300;
		int width = 100;
		int height = 50 + rand.nextInt(300);

		if (start)
		{
			columns.add(new Rectangle(WIDTH + width + columns.size() * 300, HEIGHT - height - 120, width, height));
			columns.add(new Rectangle(WIDTH + width + (columns.size() - 1) * 300, 0, width, HEIGHT - height - space));
		}
		else
		{
			columns.add(new Rectangle(columns.get(columns.size() - 1).x + 600, HEIGHT - height - 120, width, height));
			columns.add(new Rectangle(columns.get(columns.size() - 1).x, 0, width, HEIGHT - height - space));
		}
	}

	public void paintColumn(Graphics g, Rectangle column)
	{
		g.setColor(Color.green.darker());
		g.fillRect(column.x, column.y, column.width, column.height);
	}

	public void start() 
	{
		if (gameOver)
		{
			columns.clear();
			for(int i=0; i<Population; i++) {
				yMotion[i] = 0;				
			}
			for(int i=0; i<score.length; i++) {
				System.out.println(score[i]);
				score[i] = 0;				
			}

			addColumn(true);
			addColumn(true);
			addColumn(true);
			addColumn(true);
			
			//selection 
			Brain bestBrain = brains.get(0);
			Brain secondBestBrain = brains.get(1);
			
			int largest = 0;
			int secondLargest = 0;
			for (int i = 0; i < Population; i++) {
				  if(score[i] > largest) {
				    secondLargest = largest;
				    secondBestBrain = bestBrain;
				    largest = score[i];
				    bestBrain = brains.get(i);
				  }
				  if(score[i] > secondLargest && score[i] != largest) {
				    secondLargest = score[i];
				    secondBestBrain = brains.get(i);
				  }
			}
			
			
			/*
			Matrix.print(bestBrain.weightsIH);
			Matrix.print(bestBrain.weightsHO);
			
			Matrix.print(secondBestBrain.weightsHO);
			Matrix.print(secondBestBrain.weightsHO);
			*/
			
			//crossover
			brains.size();
			for(int i=0; i<Population/2; i++) {
				Brain[] babys = new Brain[2];
				try {
					babys = Brain.crossover(bestBrain, secondBestBrain);
				} catch (Exception e) {
					e.printStackTrace();
				}
				brains.set(i, babys[0]);
				brains.set(i+Population/2, babys[1]);
			}
			
			//mutation
			for (Brain brain: brains) {
				try {
					//Matrix.print(brain.weightsHO);
					//Matrix.print(brain.weightsIH);
					brain.mutate(0.1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			//this is to make sure the best brain is always in the game
			brains.set(3, bestBrain);
			
			for (int i=0; i<Population; i++) {
				birds.set(i, new Rectangle(WIDTH / 2 - 100, HEIGHT / 2 - 10 , 20, 20));
			}
			gameOver = false;
		}

		if (!started)
		{
			started = true;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(gameOver) {
			start();
		}
		int speed = 10;

		ticks++;

		if (started)
		{
			for (int i = 0; i < columns.size(); i++)
			{
				Rectangle column = columns.get(i);

				column.x -= speed;
			}
			
			for(int i=0; i<Population; i++) {
				if (ticks % 2 == 0 && yMotion[i] < 15)
				{
					yMotion[i] += 2;
				}
			}

			for (int i = 0; i < columns.size(); i++)
			{
				Rectangle column = columns.get(i);

				if (column.x + column.width < 0)
				{
					columns.remove(column);

					if (column.y == 0)
					{
						addColumn(false);
					}
				}
			}

			for (int i=0; i< Population; i++) {
				if (birds.get(i) != null) {
					birds.get(i).y += yMotion[i];

					if (ticks % 12 == 0) {

						score[i]++;
						double [][] inputArray ={
	                            {birds.get(i).y},
	                            {birds.get(i).x}, 
	                            {columns.get(0).x},
	                            {columns.get(0).y}
	                            };
						/*
						System.out.println("bird " + i + " bird X " +birds.get(i).x);
						System.out.println("bird " + i + " bird Y " +birds.get(i).y);
						System.out.println("bird " + i + " column X " +columns.get(0).x);
						System.out.println("bird " + i + " column Y " +columns.get(0).y);
					    */
						
				        Matrix inputs = new Matrix(inputArray.length, 1);
				        inputs.mapSigmoid(); 
				        inputs.matrix = inputArray;
				        try {
				            Matrix action = this.brains.get(i).predict(inputs);
				            System.out.println("bird " + i);
				            Matrix.print(action);
				            
				            if (action.matrix[0][0] > 0.95) {
				            	if (yMotion[i] > 0)
				    			{
				            		yMotion[i] = 0;
				    			}
				            	score[i] -= 10;
				            	yMotion[i] -= 10;
				            }
				        } catch (Exception error) {
				            error.printStackTrace();
				        }	
					}
				}
			}
			
			for (int i=0; i< birds.size(); i++) {
				
				for (Rectangle column : columns)
				{
					if(birds.get(i) != null) {
						if (column.y == 0 && birds.get(i).x + birds.get(i).width / 2 > column.x + column.width / 2 - 10 && birds.get(i).x + birds.get(i).width / 2 < column.x + column.width / 2 + 10)
						{
							score[i] += 2;
						}
		
						if (column.intersects(birds.get(i)))
						{
							//gameOver = true;
							//add logic to destroy the birds instead of a game over
							
							if (birds.get(i).x <= column.x)
							{
								birds.get(i).x = column.x - birds.get(i).width;
							}
							else
							{
								if (column.y != 0)
								{
									birds.get(i).y = column.y - birds.get(i).height;
								}
								else if (birds.get(i).y < column.height)
								{
									birds.get(i).y = column.height;
								}
							}
							
							birds.set(i, null);
						}
					}
				}
				if(birds.get(i) != null) {
					if (birds.get(i).y > HEIGHT - 120 || birds.get(i).y < 0)
					{
						//gameOver = true;
						birds.set(i, null);
					}
		
					/*if (birds.get(i).y + yMotion[i] >= HEIGHT - 120)
					{
						birds.get(i).y = HEIGHT - 120 - birds.get(i).height;
						birds.set(i, null);
						//gameOver = true;
					}*/
				}
			}
			for (int i=0; i< birds.size(); i++) {
				gameOver = true;
				
				if(birds.get(i) != null) {
					gameOver = false;
					break;
				}
			}
		}

		renderer.repaint();
	}

	public void repaint(Graphics g)
	{
		g.setColor(Color.cyan);
		g.fillRect(0, 0, WIDTH, HEIGHT);

		g.setColor(Color.orange);
		g.fillRect(0, HEIGHT - 120, WIDTH, 120);

		g.setColor(Color.green);
		g.fillRect(0, HEIGHT - 120, WIDTH, 20);

		g.setColor(Color.red);
		for (Rectangle bird : birds) {
			if(bird != null) {
				g.fillRect(bird.x, bird.y, bird.width, bird.height);							
			}
		}

		for (Rectangle column : columns)
		{
			paintColumn(g, column);
		}

		g.setColor(Color.white);
		g.setFont(new Font("Arial", 1, 100));

		if (!started)
		{
			g.drawString("Click to start!", 75, HEIGHT / 2 - 50);
		}

		if (gameOver)
		{
			//g.drawString("Game Over!", 100, HEIGHT / 2 - 50);
		}

		if (!gameOver && started)
		{
			//g.drawString(score.toString(), 0, 100);
		}
	}

	public static void main(String[] args)
	{
		flappyBird = new FlappyBird();
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		start();
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
		{
			start();
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e)
	{
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
	}

	@Override
	public void keyTyped(KeyEvent e)
	{

	}

	@Override
	public void keyPressed(KeyEvent e)
	{

	}

}
