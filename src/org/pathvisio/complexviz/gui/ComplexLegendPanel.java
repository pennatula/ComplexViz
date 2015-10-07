//
//package org.pathvisio.complexviz.gui;
//
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.Rectangle;
//import java.awt.geom.Rectangle2D;
//
//import javax.swing.BorderFactory;
//import javax.swing.JPanel;
//
//import org.pathvisio.complexviz.ComplexColours;
//import org.pathvisio.complexviz.plugins.VisualisePercentScores;
//import org.pathvisio.desktop.visualization.ColorGradient;
//import org.pathvisio.desktop.visualization.ColorGradient.ColorValuePair;
//import org.pathvisio.desktop.visualization.ColorSetManager;
//import org.pathvisio.desktop.visualization.Visualization;
//import org.pathvisio.desktop.visualization.VisualizationEvent;
//import org.pathvisio.desktop.visualization.VisualizationManager;
//import org.pathvisio.desktop.visualization.VisualizationManager.VisualizationListener;
//import org.pathvisio.desktop.visualization.VisualizationMethod;
//
///**
// * This class shows a legend for the currently loaded visualization and
// * color-sets.
// */
//@SuppressWarnings("serial")
//public class ComplexLegendPanel extends JPanel implements VisualizationListener {
//
//	final VisualizationManager visualizationManager;
//	static VisualisePercentScores cc ;
//	private static ColorGradient gradient;
//
//	public ComplexLegendPanel(VisualizationManager visualizationManager) {
//		this.visualizationManager = visualizationManager;
//		visualizationManager.addListener(this);
//		setBorder(BorderFactory.createLineBorder(Color.BLACK));
//		setBackground(Color.white);
//		createContents();
//		rebuildContent();
//	}
//
//	public Dimension getPreferredSize() {
//		return new Dimension(200, 600);
//	}
//
//	public void paintComponent(Graphics g) {
//		super.paintComponent(g);
//		drawVisualization(visualizationManager.getActiveVisualization(),
//				visualizationManager.getColorSetManager(), (Graphics2D) g,
//				new Rectangle2D.Double(0, 0, 100, 100), 1.0);
//	}
//
//	/**
//	 * Rebuild the contents of the legend (refresh the names in colorSetCombo
//	 * and refresh the content)
//	 */
//	public void rebuildContent() {
//		refreshContent();
//	}
//
//	public static void drawVisualization(Visualization v,
//			ColorSetManager colorSetManager, Graphics2D g, Rectangle2D area,
//			double zoomFactor) {
//		if (v == null)
//			return;
//
//		double xpos = (int) (zoomFactor * MARGIN_LEFT + area.getMinX());
//		double ypos = (int) (zoomFactor * MARGIN_TOP + area.getMinY());
//
//		for (VisualizationMethod vm : v.getMethods()) {
//			if (vm instanceof VisualisePercentScores) {
//				ypos = drawComplexGradientColour(g, xpos, ypos, zoomFactor);
//				ypos = ypos + SEPARATOR;
////				ypos = ypos + INNER_MARGIN;
//				ypos = drawComplexColourRule(g, xpos, ypos+SEPARATOR, zoomFactor);
//			}
//		}
//				
//		}
//
//	private static int drawComplexColourRule(Graphics2D g, double startx, double starty,
//			double zoomFactor) {
//		Graphics gCritNotMet = g.create();
//		Graphics gCritMet = g.create();
//
//		double lineHeight = g.getFontMetrics().getHeight();
//		double partWidth = COLOR_BOX_SIZE * zoomFactor;
//
//		// criteria (color rule) not met for complexes
//		gCritNotMet.setColor(cc.getRuleColor());
//		gCritNotMet.fillRect((int) startx, (int) (starty), (int) partWidth, (int) partWidth);
//		gCritNotMet.setColor(Color.BLACK);
//		gCritNotMet.drawRect((int) startx, (int) (starty), (int) partWidth, (int) partWidth);
//		double labelLeft2 = startx + (partWidth / 2) + partWidth;
//		double labelTop2 = starty + lineHeight;
//		String label2 = "Complex Color rule met";
//		gCritNotMet.drawString(label2, (int) labelLeft2, (int) (labelTop2));
//		
//		// criteria (color rule) not met for complexes
//		gCritMet.setColor(ComplexColours.COLOR_RULE_NOT_MET);
//		gCritMet.fillRect((int) startx, (int) (starty+lineHeight+INNER_MARGIN), (int) partWidth, (int) partWidth);
//		gCritMet.setColor(Color.BLACK);
//		gCritMet.drawRect((int) startx, (int) (starty+lineHeight+INNER_MARGIN), (int) partWidth, (int) partWidth);
//		double labelLeft = startx + (partWidth / 2) + partWidth;
//		double labelTop = starty + lineHeight + lineHeight + INNER_MARGIN;
//		String label = "Complex Colour rule not met";
//		gCritMet.drawString(label, (int) labelLeft, (int) (labelTop));
//		
//		return (int) (starty + (INNER_MARGIN * zoomFactor));
//		}
//
//	private static final double COLOR_BOX_SIZE = 20;
//	private static final double COLOR_GRADIENT_WIDTH = 80;
//	private static final double COLOR_GRADIENT_MARGIN = 50;
//	private static final double MARGIN_LEFT = 5;
//	private static final double MARGIN_TOP = 5;
//	private static final double INNER_MARGIN = 5;
//	private static final double SEPARATOR = 15;
//
//	private static double drawComplexGradientColour(Graphics2D g, double left,
//			double top, double zoomFactor) {
//		double xco = left;
//		double yco = top;
//		g.setColor(Color.BLACK);
//
//		if (gradient != null)
//			yco = drawGradient(g, gradient, xco, yco, zoomFactor);
//		
//		return (int) (yco + (INNER_MARGIN * zoomFactor));
//	}
//
//	public static void setGradient(ColorGradient grad) {
//		gradient = grad;
//		}
//	
//	private static double drawGradient(Graphics2D g, ColorGradient cg,
//			double left, double top, double zoomFactor) {
//		int height = g.getFontMetrics().getHeight();
//		int base = height - g.getFontMetrics().getDescent();
//		double xco = left + (zoomFactor * COLOR_GRADIENT_MARGIN);
//		double yco = top;
//		Rectangle bounds = new Rectangle((int) xco, (int) yco,
//				(int) (COLOR_GRADIENT_WIDTH * zoomFactor),
//				(int) (COLOR_BOX_SIZE * zoomFactor));
//		cg.paintPreview(g, bounds);
//		g.setColor(Color.BLACK); // paintPreview will change pen Color
//		g.draw(bounds);
//		yco += zoomFactor * COLOR_BOX_SIZE;
//
//		int num = cg.getColorValuePairs().size();
//		double w = (zoomFactor * COLOR_GRADIENT_WIDTH) / (num - 1);
//		for (int i = 0; i < num; ++i) {
//			ColorValuePair cvp = cg.getColorValuePairs().get(i);
//			double labelLeft = xco + i * w;
//			double labelTop = yco + (INNER_MARGIN * zoomFactor);
//			String label = "" + cvp.getValue();
//			int labelWidth = (int) g.getFontMetrics().getStringBounds(label, g)
//					.getWidth();
//			g.drawString(label, (int) (labelLeft - labelWidth / 2),
//					(int) (labelTop + base));
//			g.drawLine((int) labelLeft, (int) yco, (int) labelLeft,
//					(int) labelTop);
//		}
//
//		return yco + height + (zoomFactor * (INNER_MARGIN + INNER_MARGIN));
//	}
//
//	/**
//	 * Refresh the content of the legend
//	 */
//	void refreshContent() {
//	}
//
//	/**
//	 * Create the contents of the legend
//	 */
//	void createContents() {
//	}
//
//	public void visualizationEvent(final VisualizationEvent e) {
//		repaint();
//	}
//
//}