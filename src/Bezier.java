import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.*;

public class Bezier extends JPanel implements MouseListener
{
	private static final long serialVersionUID = -6313305405016151719L;
	
	/**
	 * The window of this program
	 */
	private JFrame f;
	
	/**
	 * The <code>BufferedImage</code> in which the lines are drawn
	 */
	private BufferedImage i = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	private Graphics2D g2d = i.createGraphics();
	
	/**
	 * The <code>BufferedImage</code> in which the Bézier curve is drawn
	 */
	private BufferedImage c = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	private Graphics2D g2d2 = c.createGraphics();
	
	/**
	 * The points of the Bézier curve
	 */
	private ArrayList<Location> points = new ArrayList<>();

	public Bezier()
	{
		//Configure this panel
		setSize(640, 480);
		setPreferredSize(getSize());
		addMouseListener(this);
		
		//Configure the window
		f = new JFrame("Bézier curve");
		f.add(this);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.pack();
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}
	
	/**
	 * A list of colors for the lines
	 */
	private Color[] colors = new Color[]{new Color(255, 31, 31), new Color(255, 143, 31), new Color(255, 255, 31), new Color(143, 255, 31), new Color(31, 255, 31), new Color(31, 255, 143), new Color(31, 255, 255), new Color(31, 143, 255), new Color(31, 31, 255)};
	/**
	 * The previous point in the curve
	 */
	private Location lastPoint;
	
	/**
	 * Opacity of the lines
	 */
	private float opacity = 1;
	
	/**
	 * The <i>t</i> value of the Bézier curve animation
	 */
	private float tick = 0;
	
	/**
	 * Last time the <code>paint(g)</code> function was executed, used to calculate FPS
	 */
	private long lastTick = 0;
	
	public void paint(Graphics g)
	{
		long time = System.currentTimeMillis();
		
		//Check if the window has been resized, and update the canvas if it has
		updateCanvas();
		//Paint the actual curve
		renderCurve((time - lastTick) / 1000f);
		
		//Draw everything to screen
		g2d.drawImage(c, 0, 0, null);
		g.drawImage(i, 0, 0, null);
		
		lastTick = time;
	}
	
	/**
	 * Renders the curve
	 * @param fps Frames Per Second of the animation
	 */
	private void renderCurve(float fps)
	{
		g2d.clearRect(0, 0, getWidth(), getHeight());
		
		if (points.size() > 1)
		{
			Location lastp = renderLines();
			g2d2.setColor(new Color(Math.max(0, Math.min(1, (1 - tick) * 4 - 2f)),
					(float) Math.max(0, Math.min(1, 1 - Math.pow((tick * 2 - 1), 2))),
					Math.max(0, Math.min(1, (tick) * 4 - 2f)), (float) Math.max(0, Math.min((1 - Math.pow(tick*2 -1, 4)) * 2, 1))));
			if (lastPoint != null)
				lastp.paintTo(lastPoint, g2d2, getWidth(), getHeight());
			lastPoint = lastp;
			
			if (tick < 1)
			{
				//Update the t value of the curve animation and continue to repaint it
				tick = Math.min(1, tick + fps / 3);
				repaint();
			} else if (opacity > 0)
			{
				//Animate the background lines out
				opacity = Math.max(0, opacity - fps);
				repaint();
			}
		}
	}
	
	/**
	 * Renders every single line in the background
	 * @return A single point that is part of the curve
	 */
	private Location renderLines()
	{
		ArrayList<Location> p = points;
		int color = 0;
		while (p.size() > 1)
		{
			renderLinesInList(p, color++);
			p = calculate(p, tick);
		}
		return p.get(0);
	}
	
	/**
	 * Connects the points in the list of points
	 * @param p The list of points
	 * @param color The color all these lines should be rendered in
	 */
	private void renderLinesInList(ArrayList<Location> p, int color)
	{
		Color c = colors[color % colors.length];
		g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (opacity * opacity / (opacity * opacity + (1 - opacity) * (1 - opacity)) * 255)));
		
		Location lastp = p.get(0);
		for (int i = 1; i < p.size(); i++)
		{
			lastp.paintTo(p.get(i), g2d, getWidth(), getHeight());
			lastp = p.get(i);
		}
	}
	
	/**
	 * Checks if the dimensions of the <code>JPanel</code> changed, and changes the dimensions of the <code>BufferedImage</code>s accordingly
	 */
	private void updateCanvas()
	{
		if (getWidth() != i.getWidth() || getHeight() != i.getHeight())
		{
			i = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
			g2d = i.createGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setBackground(Color.BLACK);
			
			c = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
			g2d2 = c.createGraphics();
			g2d2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d2.setBackground(new Color(0, 0, 0, 0));
			g2d2.setStroke(new BasicStroke(2));
			g2d2.setColor(Color.WHITE);
			
			resetCurve();
		}
	}
	
	/**
	 * Resets the curve animation
	 */
	private void resetCurve()
	{
		tick = 0;
		opacity = 1;
		g2d2.clearRect(0, 0, getWidth(), getHeight());
		lastTick = System.currentTimeMillis();
	}
	
	/**
	 * Calculates a list of points that are in between every line in the provided list of points
	 * @param l The list of points that represent lines
	 * @param time The place where the new points will be placed in the old lines
	 * @return A new list of points
	 */
	private ArrayList<Location> calculate(ArrayList<Location> l, float time)
	{
		ArrayList<Location> n = new ArrayList<Location>();
		Location last = l.get(0);
		for (int i = 1; i < l.size(); i++)
		{
			//Get a vector representing the length of the line
			Location s = l.get(i).subtract(last);
			//Add the previous vector multiplied by time to the lines location
			//This will give a point on the line at 'time'
			n.add(last.add(s.multiply(time)));
			last = l.get(i);
		}
		return n;
	}
	
	private static class Location
	{
		float x;
		float y;
		
		public Location(float x, float y)
		{
			this.x = x;
			this.y = y;
		}
		
		public Location subtract(Location l)
		{
			return new Location(x - l.x, y - l.y);
		}
		
		public Location multiply(float t)
		{
			return new Location(x * t, y * t);
		}
		
		public Location add(Location l)
		{
			return new Location(x + l.x, y + l.y);
		}
		
		public void paintTo(Location l, Graphics2D g2d, int width, int height)
		{
			g2d.drawLine((int) (x * width), (int) (y * height), (int) (l.x * width), (int) (l.y * height));
		}
	}

	public void mouseClicked(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0)
	{
		if (arg0.getButton() == MouseEvent.BUTTON3)
			points.clear();
		else if (arg0.getButton() == MouseEvent.BUTTON2)
		{
			//Generate random points
			points.clear();
			int size = (int) (Math.random() * 48) + 16;
			for (int i = 0; i < size; i++)
				points.add(new Location((float) Math.random(), (float) Math.random()));
		} else if (arg0.getButton() == MouseEvent.BUTTON1)
		{
			//Add the new point
			points.add(new Location((float) arg0.getX() / (float) getWidth(), (float) arg0.getY() / (float) getHeight()));
			lastPoint = points.get(0);
		}
		resetCurve();
		repaint();
	}

	public static void main(String[] args)
	{
		new Bezier();
	}
}
