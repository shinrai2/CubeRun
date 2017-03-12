package _default;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import java.awt.BorderLayout;
import java.awt.Color;


public class Window {
	protected Rectangle2D rect;//小方块
	protected double y;
	protected double y_v;//初速度
	protected double y_a;//加速度
	protected double random_monster_time;//随机怪物触发时间
	protected double random_cloud_time;//随机云朵触发时间
	protected double random_ground_time;//随机地面触发时间
	protected JPanel panel;
	protected TimerListener tl;//时钟Listener
	protected Timer jump_timer;//跳跃时钟
	protected Timer score_timer;//计分时钟
	protected Timer day2night_timer;//夜转时钟
	protected Timer monster_timer;//怪物时钟、云朵时钟
	protected boolean flag;//跳跃最高点标记
	protected boolean tflag;//跳开始的标记，防止二重跳
	private JFrame frame;
	protected boolean isStart;//是否已经开始
	protected boolean isPause;//是否暂停
	protected boolean isGameOver;//是否结束游戏
	protected boolean isNight;//白天黑夜
	protected boolean isBird;//飞鸟标记
	protected boolean isCheat;//作弊标记
	protected int score;//成绩
	protected int high_score;//最高成绩
	protected int cheat_score;//作弊碰撞成绩
	protected double speed;//游戏速度
	protected Color default_color;//默认颜色

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Window window = new Window();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Window() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		default_color = Color.GRAY;
		isCheat = false;
		high_score = 0;
		initial();
		
		tl = new TimerListener();
		jump_timer = new Timer(10,tl);
		score_timer = new Timer(85,tl);
		day2night_timer = new Timer(5,tl);
		monster_timer = new Timer(1,tl);
		frame = new JFrame();
		frame.setBounds(100, 100, 750, 400);
		frame.setResizable(false);
		frame.setTitle("Cube Run");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addKeyListener(new MyKeyListener());
		
		panel = new MyPanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setBackground(Color.WHITE); 
	}
	//初始化重复部分
	public void initial() {
		//初始化
		isGameOver = false;
		isStart = false;
		score = 0;
		cheat_score = 0;
		
		random_monster_time = Math.random()*500 + 500;
		random_cloud_time = Math.random()*300 + 280;
		random_ground_time = Math.random()*20 + 50;
		speed = 1;//地面速度，云相对低，鸟相对高
		
		isBird = false;
		isNight = false;
		
		isPause = false;
		
		tflag = false;
		
		y = 230;
	}
	
	class MyPanel extends JPanel{
		private Rectangle2D[] monster;
		private Rectangle2D[] cloud;
		private int which_cheat;
		private Line2D[] ground;
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
	        // 画矩形
	        double width = 35;
	        double height = 55;
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        rect = new Rectangle2D.Double(75, y, width, height);
	        Line2D line = new Line2D.Double(0,285,750,285);
	        if(isNight == true && isCheat == false) default_color = Color.LIGHT_GRAY; else default_color = Color.GRAY;
	        if(isCheat == true) {
	        	if(which_cheat == 0)
	        		default_color = Color.ORANGE;
	        	else default_color = Color.RED;
	        }
	        g2.setPaint(default_color);
	        
	        for (int i=0;i<4;i++) {
	        	g2.draw(cloud[i]);
	        	g2.draw(monster[i]);
	        	g2.fill(monster[i]);
			}
	        for (int i=0;i<50;i++) {
	        	g2.draw(ground[i]);
	        }
	        
	        g2.draw(rect);
	        g2.fill(rect);
	        g2.draw(line);
	        
	        
	        g2.setFont(new Font("SansSerif", Font.BOLD, 32));
	        if(isStart == false) g2.drawString("Ready?", 20, 50);
	        if(isPause == true && isStart == true) g2.drawString("Pause", 20, 50);
	        if(isGameOver) g2.drawString("Game Over", 20, 50);
	        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
	        g2.drawString("HI  " + transform(high_score) + "  " + transform(score), 600, 45);
	        if(isCheat) g2.drawString("CHEAT  " + transform(cheat_score), 600, 65);
		}
		private String transform(Integer arg0) {
			String tmp = arg0.toString();
			while (4 - tmp.length() > 0)
				tmp = "0" + tmp;
			return tmp;
		}
		//实例化并初始化
		public MyPanel() {
			super();
			monster = new Rectangle2D[4];
			cloud = new Rectangle2D[4];
			ground = new Line2D[50];
			which_cheat = 0;
			for (int i=0;i<4;i++) {
				monster[i] = new Rectangle2D.Double(800, 450, 55, 60);
				cloud[i] = new Rectangle2D.Double(800, 450, 65, 30);
			}
			for (int i=0;i<50;i++) {
				double x1 = Math.random()*750;
				double x2 = x1 + Math.random()*8 + 2;
				double y = Math.random()*13 + 293;
				if (i<25) 
					ground[i] = new Line2D.Double(x1,y,x2,y);
				else 
					ground[i] = new Line2D.Double(800,450,803,450);
			}
		}
		//地面触发
		public void ground_touch() {
			double w = Math.random()*8 + 2;
			double y = Math.random()*13 + 293;
			for (int i=0;i<50;i++) {
				if(ground[i].getX1()>760) {
					ground[i].setLine(750, y, 750 + w, y);
					break;
				}
			}
		}
		//怪物触发
		public void monster_touch() {
			double tmph = Math.random()*35 + 22;
			double tmpw = Math.random()*45 + 20;
			for (int i=0;i<4;i++) {
				if(monster[i].getX()>760) {
					//判断是否有鸟
					if (isBird) {
						double t = Math.random();
						if(t > 0.75) {
							if(t > 0.886) monster[i].setRect(750,175 + Math.random()*5,40,20);//上半层
							else monster[i].setRect(750,220 + Math.random()*21,40,20);//下半层
						}
						else monster[i].setRect(750,285-tmph,tmpw,tmph);
					}
					else monster[i].setRect(750,285-tmph,tmpw,tmph);
					break;
				}
			}
			panel.repaint();
		}
		//云朵触发
		public void cloud_touch() {
			double tmpw = 65;
			double tmph = 30;
			for (int i=0;i<4;i++) {
				if(cloud[i].getX()>760) {
					cloud[i].setRect(750,Math.random()*120 + 30,tmpw,tmph);
					break;
				}
			}
			panel.repaint();
		}
		//怪物移动
		public void monster_move() {
			double rect_Top = rect.getY();
			double rect_Bottom = rect_Top + rect.getHeight();
			double rect_Left = rect.getX();
			double rect_Right = rect_Left + rect.getWidth();
			double fix_bird1 = rect_Top + 18;
			double fix_bird2 = fix_bird1 + 18;
			double fix_bird3 = fix_bird2 + 18;
			
			for (int i=0;i<4;i++) {
				if(monster[i].getX()<760 && monster[i].getX()+monster[i].getWidth()>=0) {
					if(monster[i].getY() + monster[i].getHeight() == 285) {
						monster[i].setRect(monster[i].getX()- 1*speed,monster[i].getY(),monster[i].getWidth(),monster[i].getHeight());//地面障碍
					}
					else monster[i].setRect(monster[i].getX()- 1.3*speed,monster[i].getY(),monster[i].getWidth(),monster[i].getHeight());//鸟
					//碰撞检测
					//
					double monster_Top = monster[i].getY();
					double monster_Bottom = monster_Top + monster[i].getHeight();
					double monster_Left = monster[i].getX();
					double monster_Right = monster_Left + monster[i].getWidth();
					if((rect_Left>=monster_Left && rect_Left<=monster_Right && rect_Top>=monster_Top && rect_Top<=monster_Bottom) ||
							(rect_Right>=monster_Left && rect_Right<=monster_Right && rect_Top>=monster_Top && rect_Top<=monster_Bottom) ||
							(rect_Left>=monster_Left && rect_Left<=monster_Right && rect_Bottom>=monster_Top && rect_Bottom<=monster_Bottom) ||
							(rect_Right>=monster_Left && rect_Right<=monster_Right && rect_Bottom>=monster_Top && rect_Bottom<=monster_Bottom) ||
							(rect_Right>=monster_Left && rect_Right<=monster_Right && fix_bird1>=monster_Top && fix_bird1<=monster_Bottom) || //fix_bird防止飞鸟从中间穿过时无碰撞判定，下同
							(rect_Right>=monster_Left && rect_Right<=monster_Right && fix_bird2>=monster_Top && fix_bird2<=monster_Bottom) ||//
							(rect_Right>=monster_Left && rect_Right<=monster_Right && fix_bird3>=monster_Top && fix_bird3<=monster_Bottom)) {//
						if (!isCheat) {
							isGameOver = true;
							jump_timer.stop();
							score_timer.stop();
							monster_timer.stop();
						}
						else {
							//更改颜色
							if(which_cheat == 0) cheat_score++;
							which_cheat = 1;
						}
					}
					//
				}
				else if(monster[i].getX() < 0) {//回收
					monster[i].setRect(800, 450, 55, 60);
					if(isCheat) which_cheat = 0;
				}
			}
			panel.repaint();
		}
		//云朵移动
		public void cloud_move() {
			for (int i=0;i<4;i++) {
				if(cloud[i].getX()<760 && cloud[i].getX()+cloud[i].getWidth()>=0) {
					cloud[i].setRect(cloud[i].getX()- 0.4*speed,cloud[i].getY(),cloud[i].getWidth(),cloud[i].getHeight());
				}
				else if(cloud[i].getX() < 0) {//回收
					cloud[i].setRect(800, 450, 65, 30);
				}
			}
		}
		//地面移动
		public void ground_move() {
			for (int i=0;i<50;i++) {
				if(ground[i].getX1()<760 && ground[i].getX2()>=0) {
					ground[i].setLine(ground[i].getX1()- 1*speed, ground[i].getY1(), ground[i].getX2()- 1*speed, ground[i].getY2());
				}
				else if(ground[i].getX2()<0) {
					ground[i].setLine(800,450,803,450);
				}
			}
		}
		//云朵怪物刷新
		public void thing_init() {
			for (int i=0;i<4;i++) {
				monster[i].setRect(800, 450, 55, 60);
				cloud[i].setRect(800, 450, 65, 30);
			}
			panel.repaint();
		}
		
	}
	
	class MyKeyListener implements KeyListener {

		@Override
		public void keyPressed(KeyEvent arg0) {
			int keyCode = arg0.getKeyCode();
			// TODO Auto-generated method stub
			if(keyCode == KeyEvent.VK_C) {//作弊设定
				isCheat = !isCheat;
				panel.repaint();
			}
	        if(keyCode == KeyEvent.VK_SPACE) {
	        	if(isGameOver == false) {
	        		if(isStart == true) {
	        			if(isPause == false)
	        				if(tflag == false) {
				        		tflag = true;
				        		flag = false;
				        		y_v = 11;// * speed;
				        		y_a = y_v * y_v / 200;
				        		jump_timer.start();
				        	}
		        	}
		        	else {
		        		isStart = true;
		        		panel.repaint();
		        		// 开始游戏，计分
		        		score_timer.start();
		        		monster_timer.start();
		        	}
	        	}
	        	else {
	        		//重新开始游戏，初始化
	        		initial();
	        		
	        		day2night_timer.start();//日夜转换启动
	        		((MyPanel) panel).thing_init();
	        		y = 230;
	        	}
	        }
	        if(keyCode == KeyEvent.VK_ESCAPE) {
	        	if(isStart == true && isGameOver == false) {
	        		if(isPause == false) {
		        		isPause = true;
		        		panel.repaint();
		        		jump_timer.stop();
		        		score_timer.stop();
		        		monster_timer.stop();
		        	}
		        	else {
		        		isPause = false;
		        		panel.repaint();
		        		jump_timer.start();
		        		score_timer.start();
		        		monster_timer.start();
		        	}
	        	}
	        }
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void keyTyped(KeyEvent arg0) {
			// TODO Auto-generated method stub

		}

	}

	class TimerListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			if(arg0.getSource() == jump_timer) {
				if(flag == false) {
					y -= y_v;
					y_v -= y_a;
					if(y_v<0) {
						flag = true;
						y_v = 0;
					}
				}
				else {
					y += y_v;
					y_v += y_a; 
					if (y >= 229.8) {
						y = 230;
						jump_timer.stop();
						tflag = false;
					}
				}
				panel.repaint();
			}
			if(arg0.getSource() == score_timer) {
				score++;
				if(score > high_score) high_score = score;
				if(score > 300) isBird = true;//300分后启动飞鸟
				if(score%500==0) {
					isNight = !isNight;
					day2night_timer.start();
				}
				if(score%200==0 && speed < 2.5) {
					speed += 0.1;
				}
				panel.repaint();
			}
			if(arg0.getSource() == day2night_timer) {
				int r = panel.getBackground().getRed();
	        	int g = panel.getBackground().getGreen();
	        	int b = panel.getBackground().getBlue();
	        	if (isNight) {
	        		//day to night
	        		if(r>0 && g>0 && b>0) {
		        		panel.setBackground(new Color(r-5,b-5,g-5));
		        	}
	        		else day2night_timer.stop();
	        	}
	        	else {
	        		//night to day
	        		if(r<255 && g<255 && b<255) {
		        		panel.setBackground(new Color(r+5,b+5,g+5));
		        	}
	        		else day2night_timer.stop();
	        	}
	        	
				panel.repaint();
			}
			//移动，随机触发
			if(arg0.getSource() == monster_timer) {
				((MyPanel) panel).monster_move();
				((MyPanel) panel).cloud_move();
				((MyPanel) panel).ground_move();
				if(random_monster_time <= 0) {
					((MyPanel) panel).monster_touch();
					random_monster_time = Math.random()*550 + 150 + 150/speed;
				}
				else {
					random_monster_time--;
				}
				if(random_cloud_time <= 0) {
					((MyPanel) panel).cloud_touch();
					random_cloud_time = Math.random()*600 + 180 + 150/speed;
				}
				else {
					random_cloud_time--;
				}
				if(random_ground_time <= 0) {
					((MyPanel) panel).ground_touch();
					random_ground_time = Math.random()*35 + 1 + 8/speed;
				}
				else {
					random_ground_time--;
				}
			}
		}
		
	}
}
