/*
 * Copyright 2021 Kulikov Dmitriy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import javax.swing.UIManager;

public class UEIT extends javax.swing.JFrame implements Runnable
{
	protected static final double ASPECT_4_3		= 4.0 / 3.0;
	protected static final double ASPECT_16_9		= 16.0 / 9.0;
	
	protected static final double ASPECT_THRESHOLD	= (ASPECT_4_3 + ASPECT_16_9) / 2.0;
	
	protected static final BasicStroke STROKE_1PX = new BasicStroke(1);
	protected static final BasicStroke STROKE_2PX = new BasicStroke(2);
	
	protected static final Color COLOR_BLACK		= new Color(0x000000);
	protected static final Color COLOR_DARK_GRAY	= new Color(0x505050);
	protected static final Color COLOR_LIGHT_GRAY	= new Color(0xC0C0C0);
	
	protected static final Color COLOR_WHITE_75		= new Color(0xFFFFFF);
	protected static final Color COLOR_YELLOW_75	= new Color(0xFFFF6C);
	protected static final Color COLOR_CYAN_75		= new Color(0x6CFFFF);
	protected static final Color COLOR_GREEN_75		= new Color(0x6CFF6C);
	protected static final Color COLOR_MAGENTA_75	= new Color(0xFF6CFF);
	protected static final Color COLOR_RED_75		= new Color(0xFF6C6C);
	protected static final Color COLOR_BLUE_75		= new Color(0x6C6CFF);
	protected static final Color COLOR_BLACK_75		= new Color(0x404040);
	
	protected static final Color COLOR_WHITE_100	= new Color(0xFFFFFF);
	protected static final Color COLOR_YELLOW_100	= new Color(0xFFFF00);
	protected static final Color COLOR_CYAN_100		= new Color(0x00FFFF);
	protected static final Color COLOR_GREEN_100	= new Color(0x00FF00);
	protected static final Color COLOR_MAGENTA_100	= new Color(0xFF00FF);
	protected static final Color COLOR_RED_100		= new Color(0xFF0000);
	protected static final Color COLOR_BLUE_100		= new Color(0x0000FF);
	protected static final Color COLOR_BLACK_100	= new Color(0x000000);
	
	protected static final Color[][] COLOR_BARS =
	{
		{
			COLOR_WHITE_75,
			COLOR_YELLOW_75,
			COLOR_CYAN_75,
			COLOR_GREEN_75,
			COLOR_MAGENTA_75,
			COLOR_RED_75,
			COLOR_BLUE_75,
			COLOR_BLACK_75
		},
		{
			COLOR_WHITE_100,
			COLOR_YELLOW_100,
			COLOR_CYAN_100,
			COLOR_GREEN_100,
			COLOR_MAGENTA_100,
			COLOR_RED_100,
			COLOR_BLUE_100,
			COLOR_BLACK_100
		}
	};
	
	protected BufferedImage[] ueit = null;
	protected BufferedImage offscreen = null;
	
	protected double targetFramePeriod;
	
	protected int frameNumber;
	protected int currentFPS;
	
	protected int fpsCounter;
	protected long fpsPrevTime;
	
	protected boolean running;
	protected Thread thread;
	
	/**
	 * Creates new form UEIT
	 */
	public UEIT(double targetFrameRate)
	{
		initComponents();
		setLocationRelativeTo(null);
		setExtendedState(Frame.MAXIMIZED_BOTH);
//		setSize(1920 / 2, 1080 / 2);
//		setLocationRelativeTo(null);
		
		if(targetFrameRate < 1)
		{
			targetFrameRate = 1;
		}
		else if(targetFrameRate > 100)
		{
			targetFrameRate = 100;
		}
		
		targetFramePeriod = 1.0 / targetFrameRate;
		
		running = true;
		thread = new Thread(this);
		thread.start();
	}
	
	public void run()
	{
		long prevtime = System.currentTimeMillis();
		long time;
		double delta;
		double error = 0;
		long sleep;
		
		while(running)
		{
			repaint();
			
			time = System.currentTimeMillis();
			delta = (time - prevtime) / 1000.0;
			prevtime = time;
			
			error += targetFramePeriod - delta;
			
			if(error < -1.0)
			{
				error = -1.0;
			}
			else if(error > 1.0)
			{
				error = 1.0;
			}
			
			sleep = Math.round(error * 1000.0);
			
			try
			{
				if(sleep > 0)
				{
					Thread.sleep(sleep);
				}
				else
				{
					Thread.yield();
				}
			}
			catch(InterruptedException ex)
			{
			}
		}
	}
	
	public void paint(Graphics g)
	{
		int width = getWidth();
		int height = getHeight();
		
		if(ueit == null)
		{
			ueit = new BufferedImage[8];
		}
		
		if(ueit[0] == null || ueit[0].getWidth() != width || ueit[0].getHeight() != height)
		{
			for(int i = 0; i < ueit.length; i++)
			{
				ueit[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				paintUEIT((Graphics2D)ueit[i].getGraphics(), width, height, i, null, null);
			}
		}
		
		if(offscreen == null || offscreen.getWidth() != width || offscreen.getHeight() != height)
		{
			offscreen = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		}
		
		frameNumber = (frameNumber + 1) % 1000;
		fpsCounter++;
		
		long time = System.currentTimeMillis();
		long delta = time - fpsPrevTime;
		
		if(delta > 1000)
		{
			currentFPS = (int)((fpsCounter * 1000 + delta / 2) / delta);
			
			if(currentFPS < 0)
			{
				currentFPS = 0;
			}
			else if(currentFPS > 99)
			{
				currentFPS = 99;
			}
			
			fpsCounter = 0;
			fpsPrevTime = time;
		}
		
		StringBuilder infoline = new StringBuilder(10);
		
		infoline.append("FPS:");
		
		infoline.append(Integer.toString((currentFPS / 10) % 10));
		infoline.append(Integer.toString((currentFPS) % 10));
		
		infoline.append(" ");
		
		infoline.append(Integer.toString((frameNumber / 100) % 10));
		infoline.append(Integer.toString((frameNumber / 10) % 10));
		infoline.append(Integer.toString((frameNumber) % 10));
		
		paintUEIT((Graphics2D)offscreen.getGraphics(), width, height, -1, ueit[frameNumber % ueit.length], infoline.toString());
		
		g.drawImage(offscreen, 0, 0, null);
	}
	
	public void generateImageSet(int width, int height, int frameCount) throws IOException
	{
		if(ueit == null)
		{
			ueit = new BufferedImage[8];
		}
		
		if(ueit[0] == null || ueit[0].getWidth() != width || ueit[0].getHeight() != height)
		{
			for(int i = 0; i < ueit.length; i++)
			{
				ueit[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				paintUEIT((Graphics2D)ueit[i].getGraphics(), width, height, i, null, null);
			}
		}
		
		if(offscreen == null || offscreen.getWidth() != width || offscreen.getHeight() != height)
		{
			offscreen = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		}
		
		currentFPS = (int)Math.round(1.0 / targetFramePeriod);
		
		if(currentFPS < 0)
		{
			currentFPS = 0;
		}
		else if(currentFPS > 99)
		{
			currentFPS = 99;
		}
		
		String filename = "ueit_" + currentFPS + "_" + frameCount + "_" + width + "_" + height;
		
		PrintWriter psh = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename + ".h"), "UTF-8")));
		PrintWriter psc = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename + ".c"), "UTF-8")));
		
		psh.append("#ifndef ").append(filename.toUpperCase() + "_H").append("\r\n");
		psh.append("#define ").append(filename.toUpperCase() + "_H").append("\r\n");
		psh.append("\r\n");
		
		psh.append("#include <stdint.h>\r\n");
		psh.append("\r\n");
		
		psh.append("#define UEIT_WIDTH\t\t\t").append(Integer.toString(width)).append("\r\n");
		psh.append("#define UEIT_HEIGHT\t\t\t").append(Integer.toString(height)).append("\r\n");
		psh.append("\r\n");
		
		psh.append("#define UEIT_FPS\t\t\t").append(Integer.toString(currentFPS)).append("\r\n");
		psh.append("#define UEIT_NUM_FRAMES\t\t").append(Integer.toString(frameCount)).append("\r\n");
		psh.append("\r\n");
		
		psh.append("extern const uint32_t UEIT_DATA[UEIT_NUM_FRAMES][UEIT_WIDTH * UEIT_HEIGHT] __attribute__((aligned(32)));\r\n");
		psh.append("\r\n");
		
		psh.append("#endif\r\n");
		
		psc.append("#include \"").append(filename).append(".h\"\r\n");
		psc.append("\r\n");
		
		psc.append("const uint32_t UEIT_DATA[UEIT_NUM_FRAMES][UEIT_WIDTH * UEIT_HEIGHT] __attribute__((aligned(32))) =\r\n");
		psc.append("{\r\n");
		
		int[] rgb = new int[width * height];
		
		for(frameNumber = 0; frameNumber < frameCount; frameNumber++)
		{
			System.out.println("Generating image " + (frameNumber + 1) + " of " + frameCount + "...");
			
			StringBuilder infoline = new StringBuilder(10);

			infoline.append("FPS:");

			infoline.append(Integer.toString((currentFPS / 10) % 10));
			infoline.append(Integer.toString((currentFPS) % 10));

			infoline.append(" ");

			infoline.append(Integer.toString((frameNumber / 100) % 10));
			infoline.append(Integer.toString((frameNumber / 10) % 10));
			infoline.append(Integer.toString((frameNumber) % 10));

			paintUEIT((Graphics2D)offscreen.getGraphics(), width, height, -1, ueit[frameNumber % ueit.length], infoline.toString());
			
			offscreen.getRGB(0, 0, width, height, rgb, 0, width);
			
			psc.append("\t{");
			
			for(int i = 0; i < rgb.length; i++)
			{
				if(i % 8 == 0)
				{
					psc.append("\r\n\t");
				}
				
				psc.append("\t0x").append(Integer.toHexString(rgb[i] | 0xFF000000).toUpperCase()).append(",");
			}
			
			psc.append("\r\n");
			psc.append("\t},\r\n");
		}
		
		psc.append("};\r\n");
		
		psh.close();
		psc.close();
		
		frameNumber = 0;
		currentFPS = 0;
		
		ueit = null;
		offscreen = null;
	}
	
	public void paintUEIT(Graphics2D g, int width, int height, int segment, Image prerendered, String infoline)
	{
		int numRows = 18;
		int numCols;
		
		int x, y, w, h;
		boolean flag;
		
		double aspect = (double)width / (double)height;
		
		if(aspect >= ASPECT_THRESHOLD)
		{
			numCols = 32;
		}
		else
		{
			numCols = 24;
		}
		
		int bigCircleSize = numRows - 2;
		
		if(prerendered != null && infoline != null)
		{
			g.drawImage(prerendered, 0, 0, null);
			
			y = height * 7 / 2 / numRows;
			h = height * 1 / numRows;
			
			float targetFontHeight = (float)(h * 1.0);
			
			Font font = Font.decode(null);
			FontMetrics fm = g.getFontMetrics(font);
			
			font = font.deriveFont(font.getSize() * targetFontHeight / fm.getHeight());
			
			g.setFont(font);
			fm = g.getFontMetrics(font);

			g.setColor(COLOR_LIGHT_GRAY);
			g.setStroke(STROKE_1PX);
			
			for(int i = 0; i < Math.min(infoline.length(), 10); i++)
			{
				paintString(g, fm, infoline.substring(i, i + 1), width * (numCols - 10 + 1 + i * 2) / 2 / numCols, y);
			}
		}
		else
		{
			// Background

			g.setColor(COLOR_DARK_GRAY);
			g.setStroke(STROKE_1PX);
			g.fillRect(0, 0, width, height);

			// Big circle

			x = width * (numCols - bigCircleSize) / 2 / numCols;
			y = height * (numRows - bigCircleSize) / 2 / numRows;

			w = width * bigCircleSize / numCols;
			h = height * bigCircleSize / numRows;

			g.setColor(COLOR_LIGHT_GRAY);
			g.fillArc(x, y, w, h, 0, 360);

			// Top rectangle within big circle

			x = width * (numCols - 4) / 2 / numCols;
			y = height * 3 / (numRows * 2);

			w = width * 4 / numCols;
			h = height * 1 / numRows;

			g.setColor(COLOR_DARK_GRAY);
			g.fillRect(x, y, w, h);
			
			if(segment >= 0 && segment < 8)
			{
				x = width * ((numCols - 4) / 2 + segment % 4) / numCols;
				y = height * (3 + segment / 4) / (numRows * 2);
				
				w = width * 1 / numCols;
				h = height * 1 / 2 / numRows;
				
				g.setColor(COLOR_LIGHT_GRAY);
				g.fillRect(x, y, w, h);
			}

			// Second (info line) rectangle within big circle

			x = width * (numCols - 10) / 2 / numCols;
			y = height * 3 / (numRows);

			w = width * 10 / numCols;
			h = height * 1 / numRows;

			g.setColor(COLOR_DARK_GRAY);
			g.fillRect(x, y, w, h);

			// Bottom rectangle within big circle

			x = width * (numCols - 4) / 2 / numCols;
			y = height * (numRows * 2 - 5) / (numRows * 2);

			w = width * 4 / numCols;
			h = height * 1 / numRows;

			g.setColor(COLOR_DARK_GRAY);
			g.fillRect(x, y, w, h);
			
			if(segment >= 0 && segment < 8)
			{
				x = width * ((numCols - 4) / 2 + segment % 4) / numCols;
				y = height * (numRows * 2 - 5 + segment / 4) / (numRows * 2);
				
				w = width * 1 / numCols;
				h = height * 1 / 2 / numRows;
				
				g.setColor(COLOR_LIGHT_GRAY);
				g.fillRect(x, y, w, h);
			}

			// Main grid

			g.setColor(COLOR_LIGHT_GRAY);
			g.setStroke(STROKE_2PX);

			for(int i = 1; i < numRows; i++)
			{
				y = height * i / numRows;
				g.drawLine(0, y, width, y);
			}

			for(int i = 1; i < numCols; i++)
			{
				x = width * i / numCols;
				g.drawLine(x, 0, x, height);
			}
			
			// Third (center) rectangle within big circle

			x = width * (numCols - 6) / 2 / numCols;
			y = height * 8 / numRows;

			w = width * 6 / numCols;
			h = height * 2 / numRows;

			g.setColor(COLOR_DARK_GRAY);
			g.setStroke(STROKE_1PX);
			g.fillRect(x, y, w, h);
			
			g.setColor(COLOR_LIGHT_GRAY);
			g.setStroke(STROKE_2PX);
			
			g.drawLine(x + 0, y + 0, x + w, y + 0);
			g.drawLine(x + 0, y + h, x + w, y + h);
			
			g.drawLine(x + 0, y + 0, x + 0, y + h);
			g.drawLine(x + w, y + 0, x + w, y + h);

			x = width / 2;
			y = height / 2;

			w = width * 1 / numCols;
			h = height * 1 / numRows;

			g.setColor(COLOR_LIGHT_GRAY);
			g.setStroke(STROKE_2PX);

			g.drawLine(x - w, y + 0, x + w, y + 0);

			g.drawLine(x - w, y - h, x - w, y + h);
			g.drawLine(x + 0, y - h, x + 0, y + h);
			g.drawLine(x + w, y - h, x + w, y + h);

			g.drawLine(x - w * 2, y, x - w * 2, y);
			g.drawLine(x + w * 2, y, x + w * 2, y);

			// Center diagonal lines (left)

			x = width * (numCols / 2 - 3 - 5) / numCols;
			y = height * 8 / numRows;

			w = width * 5 / numCols;
			h = height * 1 / numRows;

			g.setStroke(STROKE_1PX);

			g.setColor(COLOR_LIGHT_GRAY);
			g.fillRect(x, y, w, h);

			g.setColor(COLOR_BLACK);
			g.fillRect(x, y + h, w, h);

			g.setStroke(STROKE_2PX);
			g.drawLine(x + w / 10, y, x + w / 10, y + h);

			g.setColor(COLOR_LIGHT_GRAY);
			g.drawLine(x + w / 10, y + h, x + w / 10, y + h * 2);

			x = width * (numCols / 2 - 3 - 4) / numCols;
			y = height * 9 / numRows;

			w = width * 7 / 2 / numCols;
			h = height * 1 / numRows;

			g.drawLine(x, y + h, x + w, y);

			// Center diagonal lines (right)

			x = width * (numCols / 2 + 3) / numCols;
			y = height * 8 / numRows;

			w = width * 5 / numCols;
			h = height * 1 / numRows;

			g.setStroke(STROKE_1PX);

			g.setColor(COLOR_BLACK);
			g.fillRect(x, y, w, h);

			g.setColor(COLOR_LIGHT_GRAY);
			g.fillRect(x, y + h, w, h);

			x = width * (numCols + 7) / 2 / numCols;
			y = height * 8 / numRows;

			w = width * 7 / 2 / numCols;
			h = height * 1 / numRows;

			g.setStroke(STROKE_2PX);
			g.drawLine(x, y + h, x + w, y);
			
			// Complementary color bars

			w = (width + numCols * 2 - 1) / (numCols * 2);
			h = height * 1 / numRows;

			y = height * 7 / numRows;
			
			g.setStroke(STROKE_1PX);

			flag = false;

			for(int i = 0; i < 10; i++)
			{
				g.setColor(flag ? COLOR_GREEN_75 : COLOR_MAGENTA_75);
				flag = !flag;

				x = width * (numCols - bigCircleSize + i) / (numCols * 2);
				g.fillRect(x, y, w, h);
			}

			flag = false;

			for(int i = 10; i < 22; i++)
			{
				g.setColor(flag ? COLOR_BLUE_75 : COLOR_YELLOW_75);
				flag = !flag;

				x = width * (numCols - bigCircleSize + i) / (numCols * 2);
				g.fillRect(x, y, w, h);
			}

			flag = false;

			for(int i = 22; i < 32; i++)
			{
				g.setColor(flag ? COLOR_CYAN_75 : COLOR_RED_75);
				flag = !flag;

				x = width * (numCols - bigCircleSize + i) / (numCols * 2);
				g.fillRect(x, y, w, h);
			}

			// Color gradient

			x = width * (numCols - bigCircleSize) / 2 / numCols;
			y = height * 10 / numRows;

			w = width * bigCircleSize / numCols;
			h = height * 1 / numRows;
			
			g.setStroke(STROKE_1PX);

			for(int i = 0; i < w; i++)
			{
				float v = (float)i / (float)(w - 1);

				g.setColor(new Color(v, (float)1.0 - v, v));
				g.drawLine(x + i, y, x + i, y + h);
			}

			// Tick packs

			int sidePackSize = numCols / 7;
			int mainPackSize = numCols - sidePackSize * 6;

			h = height * 1 / numRows;
			y = height * 11 / numRows;

			g.setColor(COLOR_BLACK);
			g.setStroke(STROKE_1PX);
			g.fillRect(0, y, width, h);

			g.setColor(COLOR_LIGHT_GRAY);

			// 1 px

			x = width * (numCols - mainPackSize) / 2 / numCols;
			w = width * mainPackSize / numCols;

			for(int i = 0; i < w; i += 2)
			{
				g.drawLine(x + i, y, x + i, y + h);
			}

			// 2 px

			x = width * ((numCols - mainPackSize) / 2 - sidePackSize) / numCols;
			w = width * sidePackSize / numCols;

			for(int i = 0; i < w; i += 4)
			{
				g.drawLine(x + i + 0, y, x + i + 0, y + h);
				g.drawLine(x + i + 1, y, x + i + 1, y + h);
			}

			x = width * ((numCols + mainPackSize) / 2) / numCols;
			w = width * sidePackSize / numCols;

			for(int i = 0; i < w; i += 4)
			{
				g.drawLine(x + i + 0, y, x + i + 0, y + h);
				g.drawLine(x + i + 1, y, x + i + 1, y + h);
			}

			// 3 px

			x = width * ((numCols - mainPackSize) / 2 - sidePackSize * 2) / numCols;
			w = width * sidePackSize / numCols;

			for(int i = 0; i < w; i += 6)
			{
				g.drawLine(x + i + 0, y, x + i + 0, y + h);
				g.drawLine(x + i + 1, y, x + i + 1, y + h);
				g.drawLine(x + i + 2, y, x + i + 2, y + h);
			}

			x = width * ((numCols + mainPackSize) / 2 + sidePackSize) / numCols;
			w = width * sidePackSize / numCols;

			for(int i = 0; i < w; i += 6)
			{
				g.drawLine(x + i + 0, y, x + i + 0, y + h);
				g.drawLine(x + i + 1, y, x + i + 1, y + h);
				g.drawLine(x + i + 2, y, x + i + 2, y + h);
			}

			// 4 px

			x = width * ((numCols - mainPackSize) / 2 - sidePackSize * 3) / numCols;
			w = width * sidePackSize / numCols;

			for(int i = 0; i < w; i += 8)
			{
				g.drawLine(x + i + 0, y, x + i + 0, y + h);
				g.drawLine(x + i + 1, y, x + i + 1, y + h);
				g.drawLine(x + i + 2, y, x + i + 2, y + h);
				g.drawLine(x + i + 3, y, x + i + 3, y + h);
			}

			x = width * ((numCols + mainPackSize) / 2 + sidePackSize * 2) / numCols;
			w = width * sidePackSize / numCols;

			for(int i = 0; i < w; i += 8)
			{
				g.drawLine(x + i + 0, y, x + i + 0, y + h);
				g.drawLine(x + i + 1, y, x + i + 1, y + h);
				g.drawLine(x + i + 2, y, x + i + 2, y + h);
				g.drawLine(x + i + 3, y, x + i + 3, y + h);
			}

			// Black squares at the bottom of the big circle

			x = width * (numCols - 10 + 1) / 2 / numCols;
			y = height * 14 / numRows;

			w = width * 1 / numCols;
			h = height * 1 / numRows;

			g.setColor(COLOR_BLACK);
			g.setStroke(STROKE_1PX);

			for(int i = 0; i < 5; i++)
			{
				g.fillRect(x + w * i * 2, y, w, h);
			}

			// Top small circles

			paintSmallCircle(g, false, 0, 0, numRows, numCols, width, height);
			paintSmallCircle(g, true, 0, numCols - 4, numRows, numCols, width, height);

			// Top color bars

			w = (width + 7) / 8;
			h = height * 2 / numRows;

			y = height * 4 / numRows;

			g.setStroke(STROKE_1PX);

			for(int i = 0; i < 8; i++)
			{
				x = width * i / 8;

				g.setColor(COLOR_BARS[0][i]);
				g.fillRect(x, y, w, h);
			}

			// Grayscale

			h = height * 1 / numRows;
			y = height * 6 / numRows;

			for(int i = 0; i < width; i++)
			{
				float v = (float)i / (float)(width - 1);

				g.setColor(new Color(v, v, v));
				g.drawLine(i, y, i, y + h);
			}

			// Bottom color bars

			w = (width + 7) / 8;
			h = height * 2 / numRows;

			y = height * 12 / numRows;

			for(int i = 0; i < 8; i++)
			{
				x = width * i / 8;

				g.setColor(COLOR_BARS[1][i]);
				g.fillRect(x, y, w, h);
			}

			// Bottom small circles

			paintSmallCircle(g, true, numRows - 4, 0, numRows, numCols, width, height);
			paintSmallCircle(g, false, numRows - 4, numCols - 4, numRows, numCols, width, height);

			// Outer frame

			g.setColor(COLOR_LIGHT_GRAY);
			g.setStroke(STROKE_2PX);
			g.drawRect(1, 1, width - 2, height - 2);
		}
	}
	
	protected void paintString(Graphics2D g, FontMetrics fm, String s, int x, int y)
	{
		g.drawString(s, x - fm.stringWidth(s) / 2, y - fm.getDescent() + (fm.getAscent() + fm.getDescent()) / 2);
	}
	
	protected void paintSmallCircle(Graphics2D g, boolean rotate, int row, int col, int numRows, int numCols, int width, int height)
	{
		int x = width * col / numCols;
		int y = height * row / numRows;
		
		int w = width * 4 / numCols;
		int h = height * 4 / numRows;
		
		g.setColor(COLOR_LIGHT_GRAY);
		g.fillArc(x, y, w, h, 0, 360);
		
		x = width * (col + 1) / numCols;
		y = height * (row + 1) / numRows;
		
		w = width * 2 / numCols;
		h = height * 2 / numRows;
		
		g.setColor(COLOR_BLACK);
		g.fillRect(x, y, w, h);
		
		g.setColor(COLOR_LIGHT_GRAY);
		g.setStroke(STROKE_1PX);
		
		if(rotate)
		{
			w = width * 2 / 3 / numCols;
			h = height * 2 / numRows;
			
			for(int i = y; i < y + h; i += 2)
			{
				g.drawLine(x - 2, i, x + w, i);
			}
			
			x += w * 2;
			
			for(int i = y; i < y + h; i += 4)
			{
				g.drawLine(x, i + 0, x + w + 2, i + 0);
				g.drawLine(x, i + 1, x + w + 2, i + 1);
			}
			
			x = width * (col + 2) / numCols;
			y = height * (row + 2) / numRows;
			
			g.setStroke(STROKE_2PX);
			g.drawLine(x - w / 3, y, x + w / 3, y);
			g.drawLine(x, y - w / 3, x, y + w / 3);
		}
		else 
		{
			w = width * 2 / numCols;
			h = height * 2 / 3 / numRows;
			
			for(int i = x; i < x + w; i += 2)
			{
				g.drawLine(i, y - 2, i, y + h);
			}
			
			y += h * 2;
			
			for(int i = x; i < x + w; i += 4)
			{
				g.drawLine(i + 0, y, i + 0, y + h + 2);
				g.drawLine(i + 1, y, i + 1, y + h + 2);
			}
			
			x = width * (col + 2) / numCols;
			y = height * (row + 2) / numRows;
			
			g.setStroke(STROKE_2PX);
			g.drawLine(x - h / 3, y, x + h / 3, y);
			g.drawLine(x, y - h / 3, x, y + h / 3);
		}
	}
	
	public void exit()
	{
		running = false;
		
		if(thread != null && thread.isAlive())
		{
			try
			{
				thread.join(1000);
			}
			catch(InterruptedException ex)
			{
			}
		}
		
		thread = null;
		
		dispose();
		System.exit(0);
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        FormListener formListener = new FormListener();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("UEIT-3");
        setUndecorated(true);
        addKeyListener(formListener);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.KeyListener
    {
        FormListener() {}
        public void keyPressed(java.awt.event.KeyEvent evt)
        {
            if (evt.getSource() == UEIT.this)
            {
                UEIT.this.formKeyPressed(evt);
            }
        }

        public void keyReleased(java.awt.event.KeyEvent evt)
        {
        }

        public void keyTyped(java.awt.event.KeyEvent evt)
        {
        }
    }// </editor-fold>//GEN-END:initComponents

    private void formKeyPressed(java.awt.event.KeyEvent evt)//GEN-FIRST:event_formKeyPressed
    {//GEN-HEADEREND:event_formKeyPressed
        if(evt.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			exit();
		}
    }//GEN-LAST:event_formKeyPressed

	/**
	 * @param args the command line arguments
	 */
	public static void main(final String args[])
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Throwable ex)
		{
		}

		/*
		 * Create and display the form
		 */
		java.awt.EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				double targetFrameRate = 60.0;
				
				if(args.length > 0)
				{
					try
					{
						targetFrameRate = Double.parseDouble(args[0]);
					}
					catch(Throwable ex)
					{
						System.err.println(ex.toString());
					}
				}
				
				UEIT ueit = new UEIT(targetFrameRate);
				
				if(args.length >= 4)
				{
					try
					{
						int frameCount = Integer.parseInt(args[1]);
						
						int width = Integer.parseInt(args[2]);
						int height = Integer.parseInt(args[3]);
						
						ueit.generateImageSet(width, height, frameCount);
					}
					catch(Throwable ex)
					{
						System.err.println(ex.toString());
					}
				}
				
				ueit.setVisible(true);
			}
		});
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
