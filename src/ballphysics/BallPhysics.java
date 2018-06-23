package ballphysics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class BallPhysics extends JApplet {
    int tutorial = 1;
    String tutorialText;

    ArrayList<Ball> balls = new ArrayList<>();
    Ball ball;

    int mouseXstart;
    int mouseYstart;
    int mouseXcurrent;
    int mouseYcurrent;

    boolean creatingBall;
    int maxDiameter = 100;
    int minDiameter = 10;
    int diameterIncrementer;

    Thread determineDiameter;
    Thread determineColor;

    public void createRadiusThread() {
        //This method is to recreate the below threads, because threads can only run once, 
        //therefore they need to be reinstantiated if they are to be used again

        //Increase and decrease the diameter while the ball is being created.
        determineDiameter = new Thread() {
            @Override
            public void run() {
                diameterIncrementer = 1;
                while (creatingBall) {
                    try {
                        sleep(10);
                    } catch (InterruptedException ex) {
                    }
                    ball.setDiameter(ball.getDiameter() + diameterIncrementer);
                    if (ball.getDiameter() >= maxDiameter) {
                        diameterIncrementer = -1;
                    } else if (ball.getDiameter() <= minDiameter) {
                        diameterIncrementer = 1;
                    }
                }
            }
        };

        //Change the color while ball is being created.
        determineColor = new Thread() {
            @Override
            public void run() {
                diameterIncrementer = 1;
                while (creatingBall) {
                    ball.color = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
                    try {
                        sleep(300);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        };
    }

    @Override
    public void init() {

        Canvas canvas = new Canvas();
        super.add(canvas);

        //Check is space has been pressed.
        super.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == ' ') {
                    balls.clear();
                }
                if (tutorial == 3) {
                    tutorial++;
                }
            }
        });

        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    balls.add(new Ball(e.getX(), e.getY()));
                    ball = balls.get(balls.size() - 1);
                    creatingBall = true;
                    createRadiusThread();
                    determineDiameter.start();
                    determineColor.start();

                    //Set the velocity of the ball 
                    mouseXstart = e.getX();
                    mouseYstart = e.getY();
                    mouseXcurrent = e.getX();
                    mouseYcurrent = e.getY();
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    //If rigth mousebutton is pressed, spawn 20 random balls
                    for (int i = 0; i < 20; i++) {
                        balls.add(new Ball((int) (Math.random() * getContentPane().getWidth()),
                                (int) (Math.random() * getContentPane().getHeight()), (int) (((Math.random() * 800) - 400)) * 10,
                                (int) (Math.random() * 400 - 200),
                                (int) (Math.random() * (maxDiameter - minDiameter) + minDiameter),
                                new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255))));
                        balls.get(balls.size() - 1).startMove();
                    }
                    if (tutorial == 2) {
                        tutorial++;
                    }
                }
            }
        });

        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    //Give the ball the calculated velocity
                    ball.ySpeed = mouseYstart - mouseYcurrent;
                    ball.xSpeed = (mouseXstart - mouseXcurrent) * 10;

                    //Reset the mouse positions, and stop the ballcreation Threads, and start the ball moving Thread.
                    mouseXstart = 0;
                    mouseYstart = 0;
                    creatingBall = false;
                    ball.startMove();
                    if (tutorial == 1) {
                        tutorial++;
                    }
                }
            }
        });

        super.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                //Set the mouses current position, to calculate the velocity when the mouse is let go of.
                mouseXcurrent = e.getX();
                mouseYcurrent = e.getY();
            }
        });
    }

    private class Canvas extends JPanel {

        Thread update = new Thread() {
            @Override
            public void run() {
                while (true) {
                    //Update the UI/balls every 10 milliseconds
                    repaint();
                    try {
                        sleep(10);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        };

        public Canvas() {
            update.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setFont(new Font("Arial", Font.BOLD, 15));
            FontMetrics fm = g.getFontMetrics();

            for (Ball ball : balls) {
                //Paint all of the balls
                g.setColor(ball.color);
                g.fillOval(ball.ballX() - ball.getRadius(), ball.ballY() - ball.getRadius(), ball.getDiameter(), ball.getDiameter());
            }

            g.setColor(Color.black);

            if (mouseXstart > 0 && mouseYstart > 0) {
                g.drawLine(mouseXstart, mouseYstart, mouseXcurrent, mouseYcurrent);
            }

            switch (tutorial) {
                //Determine if there should be tutorial text on the screen
                case 1:
                    tutorialText = "Drag the left mouse-button to fling a ball!";
                    g.drawString(tutorialText, getContentPane().getWidth() / 2 - fm.stringWidth(tutorialText) / 2,
                            getContentPane().getHeight() / 2 - fm.getAscent() / 2);
                    break;
                case 2:
                    tutorialText = "Right-click to fill the screen!";
                    g.drawString(tutorialText, getContentPane().getWidth() / 2 - fm.stringWidth(tutorialText) / 2,
                            getContentPane().getHeight() / 2 - fm.getAscent() / 2);
                    break;
                case 3:
                    tutorialText = "Press space to clear all of the balls";
                    g.drawString(tutorialText, getContentPane().getWidth() / 2 - fm.stringWidth(tutorialText) / 2,
                            getContentPane().getHeight() / 2 - fm.getAscent() / 2);
                    break;
            }
        }
    }

    private class Ball {

        private int x;
        private int y;
        private int diameter = minDiameter;
        private Color color;
        private int xSpeed;
        private int ySpeed;
        private int xAcc = 5;
        private int yAcc = 3;

        Thread moveBallY = new Thread() {
            //Move the ball up and down
            @Override
            public void run() {
                while (true) {
                    try {
                        sleep(10);
                    } catch (InterruptedException ex) {
                    }
                    //If the ball is at the bottom of the frame, reverse the speed, and decrease it
                    if (ballY() + getRadius() + (ySpeed / 10) >= getContentPane().getHeight()) {
                        setBallY(getContentPane().getHeight() - getRadius());
                        ySpeed = -ySpeed + 20;
                    } else {
                        setBallY(ballY() + (ySpeed / 10));
                    }
                    //Decrease / increase the ySpeed by the yAcc
                    ySpeed += yAcc;
                    
                    //Adding frcition when the ball is touching the ground
                    if(ballY() + getRadius() == getContentPane().getHeight()){
                        if(xSpeed < -1){
                            xSpeed += 10;
                        } else if(xSpeed > 1){
                            xSpeed -= 10;
                        }
                    }
                }
            }
        };

        Thread moveBallX = new Thread() {
            @Override
            public void run() {
                if (xSpeed < 0) {
                    //Reverse the xAcc, if the xSpeed if below 0
                    xAcc = -xAcc;
                }
                while (xSpeed != 0) {
                    try {
                        sleep(10);
                    } catch (InterruptedException ex) {
                    }
                    //If the ball is at the left wall, reverse its xSpeed and xAcc
                    if (ballX() - getRadius() + (xSpeed / 100) <= 0) {
                        setBallX(getRadius());
                        xSpeed = -xSpeed;
                        xAcc = -xAcc;
                        //If the ball is at the right wall, reverse its xSpeed and xAcc
                    } else if (ballX() + getRadius() + (xSpeed / 100) >= getContentPane().getWidth()) {
                        setBallX(getContentPane().getWidth() - getRadius());
                        xSpeed = -xSpeed;
                        xAcc = -xAcc;
                    } else {
                        setBallX(ballX() + (xSpeed / 100));
                    }
                    xSpeed -= xAcc;
                }
            }
        };

        public void startMove() {
            moveBallY.start();
            moveBallX.start();
        }

        public Ball(int x, int y) {
            setBallX(x);
            setBallY(y);
        }

        public Ball(int x, int y, int xSpeed, int ySpeed, int diameter, Color color) {
            this.x = x;
            this.y = y;
            this.xSpeed = xSpeed;
            this.ySpeed = ySpeed;
            this.diameter = diameter;
            this.color = color;
        }

        public int ballX() {
            return x;
        }

        public void setBallX(int x) {
            this.x = x;
        }

        public int ballY() {
            return y;
        }

        public void setBallY(int y) {
            this.y = y;
        }

        public int getRadius() {
            return diameter / 2;
        }

        public int getDiameter() {
            return diameter;
        }

        public void setDiameter(int diameter) {
            this.diameter = diameter;
        }
    }

    public static void main(String[] args) {
        JFrame main = new JFrame("Ball Physics");
        main.setSize(800, 500);
        BallPhysics app = new BallPhysics();

        app.init();
        app.start();
        app.setFocusable(true);
        main.add(app);

        main.setLocationRelativeTo(null);
        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main.setVisible(true);
    }
}
