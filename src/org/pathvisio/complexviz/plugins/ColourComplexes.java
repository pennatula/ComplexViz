package org.pathvisio.complexviz.plugins;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JPanel;

import org.jdom.Element;
import org.pathvisio.complexviz.gui.ComplexLegendPane;
import org.pathvisio.complexviz.plugins.ColourComplexesPanel.Gradient;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.view.GeneProduct;
import org.pathvisio.core.view.Graphics;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.desktop.gex.GexManager;
import org.pathvisio.desktop.visualization.AbstractVisualizationMethod;
import org.pathvisio.desktop.visualization.ColorGradient;
import org.pathvisio.desktop.visualization.ColorSetManager;
import org.pathvisio.desktop.visualization.Criterion;
import org.pathvisio.desktop.visualization.ColorGradient.ColorValuePair;
import org.pathvisio.gui.SwingEngine;

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
	private final ColorSetManager csm;
	private String expression;
	ColorGradient gradient;
	private SwingEngine se;
	private Map<String, Double> cidpercentmap;
	private Map<String, Color> cidclrmap;
	private HashSet<String> cidset;
	private int RULE_MODEL = 1;
	private int GRADIENT_MODEL = 2;
	private Color DEFAULT_COMPLEX_COLOUR = Color.GRAY;
	private Color notrulecolour = Color.GRAY;
	private Color rulecolour = Color.BLUE;
	private ColorGradient DEFAULT_GRADIENT;

	GexManager getGexManager() {
		return gexManager;
	}

	public ColourComplexes(SwingEngine swingEngine, GexManager gexManager,
			ColorSetManager csm) {
		this.se = swingEngine;
		this.gexManager = gexManager;
		this.csm = csm;
		
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

	private Color getColour(Double value) {
		Color clr = DEFAULT_COMPLEX_COLOUR;
		if (RULE_MODEL == drawModel) {
			clr = getColourByRule(value, clr);
		} else if (GRADIENT_MODEL == drawModel) {
			System.out.println("gradient used");
			clr = getColourByGradient(value, clr);
		}
		return clr;
	}

	private Color getColourByGradient(Double value, Color clr) {
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
			if (value >= minval && value <= maxval) {
				clr = gradientused.getColor(value);
			}
		}
		return clr;
	}

	private Color getColourByRule(Double value, Color clr) {
		Color rc = getRuleColor();
		Color nrc = getNotRuleColor();
		String expr = getExpression();
		// System.out.println("rule color set: " + rc.getRGB());

//		String expr = expression == null ? DEFAULT_EXPRESSION : expression;
		VPathway vp = getVisualization().getManager().getEngine()
				.getActiveVPathway();
		if (vp != null) {
			if (evaluate(expr, value)) {
				clr = rc;
			} else {
				clr = nrc;
			}

		}
		// System.out.println("rule color set: " + clr.getRGB());
		return clr;
	}

	private boolean evaluate(String expression2, Double value) {
		Boolean booleanval = false;
		String[] exprParts;

		if (expression2.contains("<")) {
			if (expression2.contains("=")) {
				exprParts = expression2.split("<=");
				if (value <= Double.parseDouble(exprParts[1])) {
					booleanval = true;
				}
			} else {
				if (expression2.contains(">")) {
					exprParts = expression2.split("[!=]");
					if (!(value == Double.parseDouble(exprParts[1]))) {
						booleanval = true;
					}
				}
				exprParts = expression2.split("<");
				if (value < Double.parseDouble(exprParts[1])) {
					booleanval = true;
				}
			}
		}

		if (expression2.contains(">")) {
			if (expression2.contains("=")) {
				exprParts = expression2.split(">=");
				if (value > Double.parseDouble(exprParts[1])) {
					booleanval = true;
				}
			} else {
				exprParts = expression2.split(">");
				if (value > Double.parseDouble(exprParts[1])) {
					booleanval = true;
				}
			}

		}

		if (expression2.contains("=")) {
			exprParts = expression2.split("=");
			if (value == Double.parseDouble(exprParts[1])) {
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
		Element xml = super.toXML();
		for (String cid : cidclrmap.keySet()) {
			Element xmlchild = new Element(cid, getColorHex(cidclrmap.get(cid)));
			xml.addContent(xmlchild);
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

	private String getExpression() {
		String expr = expression == null ? DEFAULT_EXPRESSION : expression;
		return expr;
	}

	@Override
	public final void loadXML(Element xml) {
		super.loadXML(xml);
		for (Object o : xml.getChildren(ColourComplexes.XML_ELEMENT)) {
			// setComplexColour();
		}
	}

	private Color getRuleColor() {
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

	private ColorGradient getGradient() {
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

	protected void setPercentValues(Map<String, Double> complexidpercentmap) {
		cidpercentmap = new HashMap<String, Double>();
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
		modified();
	}

	
}