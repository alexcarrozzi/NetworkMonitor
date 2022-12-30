package network;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.annotations.XYShapeAnnotation;

public class DisconnectSquare {

	private XYShapeAnnotation myShape;
	private Rectangle2D myRect;

	public DisconnectSquare(int xUL, int yUL, int width, int height) {
		Color disConnColor = new Color(0xFF7777);
		myRect = new Rectangle2D.Double(xUL, yUL, width, height);
		myShape = new XYShapeAnnotation(myRect, new BasicStroke(1.f), Color.BLACK, disConnColor);
	}

	public void updateSquare(int xUL, int yUL, int width, int height) {
		myRect.setRect(xUL, yUL, width, height);
	}

	public XYShapeAnnotation getAnnotation() {
		return myShape;
	}

	public String toString() {
		return "xUL: " + myRect.getX() + "; Y: " + myRect.getY() + "; Width: " + myRect.getWidth() + "; Height: "
				+ myRect.getHeight();
	}

}
