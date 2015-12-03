// ComplexViz Plugin for PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2015 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package org.pathvisio.complexviz.plugins;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;

import org.jdom.Element;
import org.pathvisio.complexviz.gui.ComplexStatisticsPanel;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.view.GeneProduct;
import org.pathvisio.core.view.Graphics;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.desktop.gex.GexManager;
import org.pathvisio.desktop.visualization.AbstractVisualizationMethod;
import org.pathvisio.desktop.visualization.ColorGradient;
import org.pathvisio.desktop.visualization.ColorGradient.ColorValuePair;
import org.pathvisio.desktop.visualization.ColorSetManager;
import org.pathvisio.desktop.visualization.VisualizationManager;
import org.pathvisio.gui.SwingEngine;

/**
 * Colours a complex node depending on percentage of components passing
 * criterion
 * 
 * @author anwesha
 */
public class VisualisePercentScores extends AbstractVisualizationMethod {

	private static final Color DEFAULT_RULECOLOUR = Color.ORANGE;
	private static final String DEFAULT_EXPRESSION = "[Percent] > 25";

	private int drawModel;

	private GexManager gexManager;
	private String expression;
	private ColorGradient gradient;
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
	private final String XML_COMPLEXVIZ = "complexviz settings";

	public VisualisePercentScores(SwingEngine swingEngine, GexManager gexManager, ColorSetManager csm, VisualizationManager vsm) {
		se = swingEngine;
		this.gexManager = gexManager;
		// lp = new LegendPanel(vsm);
		drawModel = RULE_MODEL;
		setExpression(DEFAULT_EXPRESSION);
		setDefaultColours();

		setIsConfigurable(true);
		setUseProvidedArea(false);
	}

	@Override
	public int defaultDrawingOrder() {
		return 3;
	}

	private void drawArea(final GeneProduct gp, Graphics2D g2d) {
		Color rgb = cidclrmap.get(gp.getPathwayElement().getElementID());
		g2d.setPaint(rgb);
		g2d.setColor(rgb);
		g2d.fill(gp.getShape());
		g2d.draw(gp.getShape());
	}

	private boolean evaluate(String expression2, Float score) {
		Boolean booleanval = false;
		String[] exprParts;
		Matcher matcher = Pattern.compile("\\d+").matcher(expression2);
		if (!score.isNaN()) {
			if (matcher.find()) {
				Matcher exprmatcher = Pattern.compile(">").matcher(expression2);
				if (exprmatcher.find()) {
					exprmatcher = Pattern.compile("=").matcher(expression2);
					if (exprmatcher.find()) {
						exprParts = expression2.split(">=");
						if (score.intValue() >= (Integer.parseInt(exprParts[1]
								.trim()))) {
							booleanval = true;
						}
					} else {
						exprParts = expression2.split(">");
						if (score.intValue() > (Integer.parseInt(exprParts[1]
								.trim()))) {
							booleanval = true;
						}
					}
				}
				exprmatcher = Pattern.compile("<").matcher(expression2);
				if (exprmatcher.find()) {
					exprmatcher = Pattern.compile("=").matcher(expression2);
					if (exprmatcher.find()) {
						exprParts = expression2.split("<=");
						if (score.intValue() <= (Integer.parseInt(exprParts[1]
								.trim()))) {
							booleanval = true;
						}
					} else {
						exprParts = expression2.split("<");
						if (score.intValue() < (Integer.parseInt(exprParts[1]
								.trim()))) {
							booleanval = true;
						}
					}
				}
				exprmatcher = Pattern.compile("==").matcher(expression2);
				if (exprmatcher.find()) {
					exprParts = expression2.split("==");
					if (score.intValue() == (Integer.parseInt(exprParts[1]
							.trim()))) {
						booleanval = true;
					}
				}
				exprmatcher = Pattern.compile("!=").matcher(expression2);
				if (exprmatcher.find()) {
					exprParts = expression2.split("!=");
					if (score.intValue() != (Integer.parseInt(exprParts[1]
							.trim()))) {
						booleanval = true;
					}
				}
			}
		}
		return booleanval;
	}

	private Color getColour(Float float1) {
		Color clr = DEFAULT_COMPLEX_COLOUR;
		if (RULE_MODEL == drawModel) {
			clr = getColourByRule(float1, clr);
		} else if (GRADIENT_MODEL == drawModel) {
			clr = getColourByGradient(float1, clr);
		}
		return clr;
	}

	private Color getColourByGradient(Float float1, Color clr) {
		ColorGradient gradientused = getGradient();
		if (gradientused == null)
			return clr;
		double[] vals = gradientused.getMinMax();
		double minval = vals[0];
		double maxval = vals[1];

		VPathway vp = getVisualization().getManager().getEngine().getActiveVPathway();
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

		VPathway vp = getVisualization().getManager().getEngine().getActiveVPathway();
		if (vp != null) {
			if (evaluate(expr, float1)) {
				clr = rc;
			} else {
				clr = nrc;
			}
		}
		return clr;
	}

	@Override
	public JPanel getConfigurationPanel() {
		JPanel complexpanel = new ComplexStatisticsPanel(this, se, gexManager);
		return complexpanel;
	}

	@Override
	public String getDescription() {
		return "Change colour of complexes based on component data";
	}

	public ColorGradient getGradient() {
		ColorGradient cg = gradient == null ? DEFAULT_GRADIENT : gradient;
		return cg;
	}

	@Override
	public String getName() {
		return "Percent scores on complexes ";
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

	public Color getRuleColor() {
		Color rc = rulecolour == null ? DEFAULT_RULECOLOUR : rulecolour;
		VPathway vp = getVisualization().getManager().getEngine().getActiveVPathway();
		if (vp != null) {
			rc = new Color(rc.getRGB());
		}
		return rc;
	}

	public String getExpression() {
		String expr = expression == null ? DEFAULT_EXPRESSION : expression;
		return expr;
	}

	@Override
	public final void loadXML(Element xml) {
		super.loadXML(xml);
		for (int i = 0; i < xml.getChildren(XML_COMPLEXVIZ).size(); i++) {
			try {
				for (final String key : cidclrmap.keySet()) {
					xml.getAttributeValue(key);
					cidclrmap
							.put(key, Color.decode(xml.getAttributeValue(key)));
				}
			} catch (final Exception e) {
				Logger.log.error("Unable to parse settings for plugin", e);
			}
		}
	}

	public void setComplexColours() {
		cidclrmap = new HashMap<String, Color>();
		for (final String key : cidpercentmap.keySet()) {
			final Color cc = getColour(cidpercentmap.get(key));
			cidclrmap.put(key, cc);
		}
		modified();
	}

	private void setDefaultColours() {
		cidset = new HashSet<String>();
		for (final PathwayElement pe : se.getEngine().getActivePathway()
				.getDataObjects()) {
			if (pe.getObjectType() == ObjectType.DATANODE) {
				if (pe.getDataNodeType().equalsIgnoreCase("complex")) {
					cidset.add(pe.getElementID());
				}
			}
		}

		DEFAULT_GRADIENT = new ColorGradient();
		DEFAULT_GRADIENT.addColorValuePair(new ColorValuePair(Color.RED, 0));
		DEFAULT_GRADIENT
				.addColorValuePair(new ColorValuePair(Color.GREEN, 100));

		cidclrmap = new HashMap<String, Color>();
		for (final String key : cidset) {
			cidclrmap.put(key, DEFAULT_COMPLEX_COLOUR);
		}
		modified();
	}

	public void setDefaultExpresion() {
		expression = DEFAULT_EXPRESSION;
		modified();
	}

	public void setExpression(String criterion) {
		if (criterion != null) {
			expression = criterion;
		}
		modified();

	}

	public void setGradient(ColorGradient grad) {
		gradient = grad;
		// lp.setGradient(grad);
		modified();
	}

	public void setModel(int model) {
		drawModel = model;
	}

	public void setNotRuleColour(Color nrc) {
		if (nrc != null) {
			notrulecolour = nrc;
		}
		modified();
	}

	public void setPercentValues(Map<String, Float> complexidpercentmap) {
		cidpercentmap = new HashMap<String, Float>();
		if (complexidpercentmap != null) {
			cidpercentmap = complexidpercentmap;
		}
	}

	public void setRuleColour(Color rc) {
		if (rc != null) {
			rulecolour = rc;
		}
		modified();
	}

	@Override
	public final Element toXML() {
		final Element xml = super.toXML();
		return xml;
	}

	@Override
	public void visualizeOnDrawing(Graphics g, Graphics2D g2d) {
		if (g instanceof GeneProduct) {
			if (g.getPathwayElement().getObjectType() == ObjectType.DATANODE) {
				if (g.getPathwayElement().getDataNodeType()
						.equalsIgnoreCase("complex")) {
					// if (useSamples.size() == 0)
					// return; // Nothing to draw
					final GeneProduct gp = (GeneProduct) g;
					drawArea(gp, g2d);
				}
			}
		}
	}

	@Override
	public Component visualizeOnToolTip(Graphics g) {
		return null;
	}

}