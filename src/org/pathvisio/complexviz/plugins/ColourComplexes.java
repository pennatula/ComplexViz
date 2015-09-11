package org.pathvisio.complexviz.plugins;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.jdom.Element;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.view.GeneProduct;
import org.pathvisio.core.view.Graphics;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.desktop.gex.GexManager;
import org.pathvisio.desktop.visualization.AbstractVisualizationMethod;
import org.pathvisio.desktop.visualization.ColorGradient;
import org.pathvisio.desktop.visualization.ColorSetManager;
import org.pathvisio.desktop.visualization.ColorGradient.ColorValuePair;
import org.pathvisio.desktop.visualization.VisualizationManager;
import org.pathvisio.gui.SwingEngine;
import org.pathvisio.visualization.plugins.LegendPanel;

/**
 * Colours a complex node depending on percentage of components passing
 * criterion
 * 
 * @author anwesha
 */
public class ColourComplexes extends AbstractVisualizationMethod {

	static final Color DEFAULT_RULECOLOUR = Color.BLUE;
	static final String DEFAULT_EXPRESSION = "[Percent] > 25";

	Color c = Color.ORANGE;

	private int drawModel;
	
	private final GexManager gexManager;
	private String expression;
	ColorGradient gradient;
	private SwingEngine se;
	private Map<String, Float> cidpercentmap;
	private Map<String, Color> cidclrmap;
	private HashSet<String> cidset;
	private int RULE_MODEL = 1;
	private int GRADIENT_MODEL = 2;
	private Color DEFAULT_COMPLEX_COLOUR = Color.GRAY;
	private Color notrulecolour = Color.DARK_GRAY;
	private Color rulecolour = Color.BLUE;
	private ColorGradient DEFAULT_GRADIENT;
	private String XML_COMPLEX_COLOURS = "complex_colours";
	private String XML_COMPLEX_ID = "complex_id";
//	private LegendPanel lp;

	GexManager getGexManager() {
		return gexManager;
	}

	public ColourComplexes(SwingEngine swingEngine, GexManager gexManager,
			ColorSetManager csm, VisualizationManager vsm) {
		this.se = swingEngine;
		this.gexManager = gexManager;
//		lp = new LegendPanel(vsm);
		drawModel = RULE_MODEL;
		setExpression(DEFAULT_EXPRESSION);
		setDefaultColours();
		
		setIsConfigurable(true);
		setUseProvidedArea(false);
	}

	@Override
	public Component visualizeOnToolTip(Graphics g) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		return "Change colour of complexes based on component data";
	}

	@Override
	public String getName() {
		return "Percent scores on complexes ";
	}

	@Override
	public JPanel getConfigurationPanel() {
		JPanel complexpanel = new ComplexStatisticsPanel(this, se, gexManager);
		return complexpanel;
	}

	@Override
	public void visualizeOnDrawing(Graphics g, Graphics2D g2d) {
		if (g instanceof GeneProduct) {
			if (g.getPathwayElement().getObjectType() == ObjectType.DATANODE) {
				if (g.getPathwayElement().getDataNodeType()
						.equalsIgnoreCase("complex")) {
					// if (useSamples.size() == 0)
					// return; // Nothing to draw
					GeneProduct gp = (GeneProduct) g;
					drawArea(gp, g2d);
				}
			}
		}
	}

	private void drawArea(final GeneProduct gp, Graphics2D g2d) {
		Color rgb = cidclrmap.get(gp.getPathwayElement().getElementID());
		g2d.setPaint(rgb);
		g2d.setColor(rgb);
		g2d.fill(gp.getShape());
		g2d.draw(gp.getShape());
	}

	private Color getColour(Float float1) {
		Color clr = DEFAULT_COMPLEX_COLOUR;
		if (RULE_MODEL == drawModel) {
			clr = getColourByRule(float1, clr);
		} else if (GRADIENT_MODEL == drawModel) {
			System.out.println("gradient used");
			clr = getColourByGradient(float1, clr);
		}
		return clr;
	}

	private Color getColourByGradient(Float float1, Color clr) {
		ColorGradient gradientused = getGradient();
		if(gradientused == null){
			return clr;
		}
		double[] vals = gradientused.getMinMax();
		double minval = vals[0];
		double maxval = vals[1];

		VPathway vp = getVisualization().getManager().getEngine()
				.getActiveVPathway();
		if (vp != null) {
			if (float1 >= minval && float1 <= maxval) {
				clr = gradientused.getColor(float1);
			}
		}
		return clr;
	}

	private Color getColourByRule(Float float1, Color clr) {
		Color rc = getRuleColor();
		Color nrc = getNotRuleColor();
		String expr = getExpression();
		// System.out.println("rule color set: " + rc.getRGB());

//		String expr = expression == null ? DEFAULT_EXPRESSION : expression;
		VPathway vp = getVisualization().getManager().getEngine()
				.getActiveVPathway();
		if (vp != null) {
			if (evaluate(expr, float1)) {
				clr = rc;
			} else {
				clr = nrc;
			}

		}
		// System.out.println("rule color set: " + clr.getRGB());
		return clr;
	}

	private boolean evaluate(String expression2, Float float1) {
		Boolean booleanval = false;
		String[] exprParts;

		if (expression2.contains("<")) {
			if (expression2.contains("=")) {
				exprParts = expression2.split("<=");
				if (float1 <= Double.parseDouble(exprParts[1])) {
					booleanval = true;
				}
			} else {
				if (expression2.contains(">")) {
					exprParts = expression2.split("[!=]");
					if (!(float1 == Double.parseDouble(exprParts[1]))) {
						booleanval = true;
					}
				}
				exprParts = expression2.split("<");
				if (float1 < Double.parseDouble(exprParts[1])) {
					booleanval = true;
				}
			}
		}

		if (expression2.contains(">")) {
			if (expression2.contains("=")) {
				exprParts = expression2.split(">=");
				if (float1 > Double.parseDouble(exprParts[1])) {
					booleanval = true;
				}
			} else {
				exprParts = expression2.split(">");
				if (float1 > Double.parseDouble(exprParts[1])) {
					booleanval = true;
				}
			}

		}

		if (expression2.contains("=")) {
			exprParts = expression2.split("=");
			if (float1 == Double.parseDouble(exprParts[1])) {
				booleanval = true;
			}
		}
		return booleanval;
	}

	protected void setModel(int model) {
		drawModel = model;
	}

	static final String XML_ELEMENT = "sample";
	static final String XML_ATTR_ID = "id";
	static final String XML_ATTR_COLOuR = "colour";

	@Override
	public final Element toXML() {
		final Element xml = super.toXML();
		final Element elm = new Element(XML_COMPLEX_COLOURS);
		xml.addContent(elm);
		for (final String key : cidclrmap.keySet()) {
			final Element selm = new Element(XML_COMPLEX_ID);
			final Color bc = cidclrmap.get(key);
			final String hex = String.format("#%02x%02x%02x", bc.getRed(),
					bc.getGreen(), bc.getBlue());
			selm.setAttribute(key, hex);
			xml.addContent(selm);
		}
		return xml;
	}

	private String getColorHex(Color clr) {
		int r = clr.getRed();
		int g = clr.getGreen();
		int b = clr.getBlue();
		return "#" + toBrowserHexValue(r) + toBrowserHexValue(g)
				+ toBrowserHexValue(b);
	}

	private static String toBrowserHexValue(int number) {
		StringBuilder builder = new StringBuilder(
				Integer.toHexString(number & 0xff));
		while (builder.length() < 2) {
			builder.append("0");
		}
		return builder.toString().toUpperCase();
	}

	public String getExpression() {
		String expr = expression == null ? DEFAULT_EXPRESSION : expression;
		return expr;
	}

	@Override
	public final void loadXML(Element xml) {
		super.loadXML(xml);
		for (int i = 0; i < xml.getChildren(XML_COMPLEX_COLOURS).size(); i++) {
			try {
				for (final String key : cidclrmap.keySet()) {
					xml.getAttributeValue(key);
					cidclrmap.put(key,
							Color.decode(xml.getAttributeValue(key)));
				}
			} catch (final Exception e) {
				Logger.log.error("Unable to parse settings for plugin", e);
			}
		}
	}

	public Color getRuleColor() {
		Color rc = rulecolour == null ? DEFAULT_RULECOLOUR : rulecolour;
		VPathway vp = getVisualization().getManager().getEngine()
				.getActiveVPathway();
		if (vp != null) {
			rc = new Color(rc.getRGB());
		}
		return rc;
	}

	private Color getNotRuleColor() {
		Color nrc = notrulecolour == null ? DEFAULT_COMPLEX_COLOUR
				: notrulecolour;
		VPathway vp = getVisualization().getManager().getEngine()
				.getActiveVPathway();
		if (vp != null) {
			nrc = new Color(nrc.getRGB());
		}
		return nrc;
	}

	public ColorGradient getGradient() {
		ColorGradient cg = gradient == null ? DEFAULT_GRADIENT : gradient; 
		return cg;
	}

	
	@Override
	public int defaultDrawingOrder() {
		return 3;
	}

	protected void setExpression(String criterion) {
		if (criterion != null) {
			expression = criterion;
			modified();
		}

	}

	protected void setPercentValues(Map<String, Float> complexidpercentmap) {
		cidpercentmap = new HashMap<String, Float>();
		if (complexidpercentmap != null) {
			cidpercentmap = complexidpercentmap;
		}
	}

	private void setDefaultColours() {
		cidset = new HashSet<String>();
		for (PathwayElement pe : se.getEngine().getActivePathway()
				.getDataObjects()) {
			if (pe.getObjectType() == ObjectType.DATANODE) {
				if (pe.getDataNodeType().equalsIgnoreCase("complex")) {
					cidset.add(pe.getElementID());
				}
			}
		}
		
		DEFAULT_GRADIENT = new ColorGradient();
		DEFAULT_GRADIENT.addColorValuePair(new ColorValuePair(Color.RED, 0));
		DEFAULT_GRADIENT.addColorValuePair(new ColorValuePair(Color.GREEN, 100));
		
		cidclrmap = new HashMap<String, Color>();
		for (String key : cidset) {
			cidclrmap.put(key, DEFAULT_COMPLEX_COLOUR);
		}
		modified();
	}

	protected void setComplexColours() {
		cidclrmap = new HashMap<String, Color>();
		for (String key : cidpercentmap.keySet()) {
			Color cc = getColour(cidpercentmap.get(key));
			cidclrmap.put(key, cc);
		}
		modified();
	}

	protected void setRuleColour(Color rc) {
		if (rc != null) {
			rulecolour = rc;
			modified();
		}
	}

	protected void setNotRuleColour(Color nrc) {
		if (nrc != null) {
			notrulecolour = nrc;
			modified();
		}

	}

	protected void setDefaultExpresion() {
		expression = DEFAULT_EXPRESSION;
		modified();
	}

	protected void setGradient(ColorGradient grad) {
		gradient = grad;
//		lp.setGradient(grad);
		modified();
	}

}