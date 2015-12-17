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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdom.Element;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.view.GeneProduct;
import org.pathvisio.core.view.Graphics;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.core.view.VPathwayElement;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.visualization.AbstractVisualizationMethod;
import org.pathvisio.desktop.visualization.ColorSetManager;

/**
 * @author anwesha
 * 
 */
public class ColourComplexBorder extends AbstractVisualizationMethod {
	private JButton clrbtn;
	private JLabel complexlbl;
	private PvDesktop pvd;
	private Map<String, JButton> buttonCache;
	private Color random_color = Color.BLACK;
	private Map<String, Color> complexIdBorderColorMap;
	private Map<String, Set<PathwayElement>> complexIdComponentMap;
	private Set<PathwayElement> result;
	private Map<String,PathwayElement> complexIdNameMap;
	private String COMPLEX_ID = "complex_id";
	static final Color DEFAULT_BORDER_COLOUR = Color.BLACK;
	static final int DEFAULT_BORDER_THICKNESS = 1;

	static final String PARENT_COMPLEX_ID = "parent_complex_id";
	static final String BORDER_COLOUR = "border_colour";

	static Color border_colour = Color.BLACK;

	final static String XML_COMPLEX_BORDER = "complex_border_colours";

	public ColourComplexBorder(PvDesktop desktop, ColorSetManager csm) {
		pvd = desktop;
		setIsConfigurable(true);
		setUseProvidedArea(false);
	}

	/*
	 * A high drawing order so it comes on top of opaque methods
	 * (non-Javadoc)
	 * @see org.pathvisio.desktop.visualization.VisualizationMethod#defaultDrawingOrder()
	 */
	@Override
	public int defaultDrawingOrder() {
		return -3;
	}

	private void drawColoredRectangle(Rectangle r, Graphics2D g2d, Color c, int lt) {
		g2d.setPaint(c);
		g2d.setColor(c);
		g2d.setStroke(new BasicStroke(lt));
		g2d.draw(r);
	}

	private void drawSample(Graphics2D g2d) {
		VPathway vpwy = pvd.getSwingEngine().getEngine().getActiveVPathway();
		for (String cid : complexIdComponentMap.keySet()) {
			Set<PathwayElement> componentNodes = complexIdComponentMap.get(cid);
			border_colour = complexIdBorderColorMap.get(cid);
			for (PathwayElement gp : componentNodes) {
				VPathwayElement gpview = vpwy.getPathwayElementView(gp);
				Rectangle r = gpview.getVBounds().getBounds();
				drawColoredRectangle(r, g2d, border_colour, 2);
			}
		}
	}

	@Override
	public JPanel getConfigurationPanel() {
		mapComplexIdName();
		mapComplexComponents();
		setDefaultBorderColors();
		JPanel panel = new JPanel();
		BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(layout);		
		buttonCache = new HashMap<String, JButton>();
		for (String key : complexIdNameMap.keySet()) {
			String complexname = complexIdNameMap.get(key).getTextLabel();
			JPanel subpanel = new JPanel();
			GridLayout sublayout = new GridLayout(0,2);
			subpanel.setLayout(sublayout);
			complexlbl = new JLabel(complexname);
			clrbtn = new JButton("border");
			clrbtn.setActionCommand(complexname);
			buttonCache.put(complexname, clrbtn);
			clrbtn.setForeground(complexIdBorderColorMap.get(key));
			clrbtn.setBackground(complexIdBorderColorMap.get(key));
			clrbtn.setName(complexname);
			clrbtn.setOpaque(true);
			clrbtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String command = ((JButton) e.getSource()).getActionCommand();
					JButton button = buttonCache.get(command);
					Color c = JColorChooser.showDialog(null, "Choose a Color", button.getForeground());
					button.setForeground(c);
					button.setBackground(c);
					complexIdBorderColorMap.put(key, c);
					modified();
				}
			});
			subpanel.add(complexlbl);
			subpanel.add(clrbtn);
			panel.add(subpanel);
		}
		return panel;
	}
	
	private Color generateRandomColor(){
		int R = (int)(Math.random()*256);
		int G = (int)(Math.random()*256);
		int B= (int)(Math.random()*256);
		Color color = new Color(R, G, B); //random color, but can be bright or dull
		while(color.equals(random_color)){
			//to get rainbow, pastel colors
			Random random = new Random();
			float hue = random.nextFloat();
			float saturation = 0.9f;//1.0 for brilliant, 0.0 for dull
			float luminance = 1.0f; //1.0 for brighter, 0.0 for black
			color = Color.getHSBColor(hue, saturation, luminance);
		}
		random_color = color;
		return color;
	}
	
	protected Map<String, Color> setDefaultBorderColors() {
		complexIdBorderColorMap = new HashMap<String, Color>();
		for(String key : complexIdNameMap.keySet()){
			Color bc = generateRandomColor();
			complexIdBorderColorMap.put(key, bc);
		}
		return complexIdBorderColorMap;
		
	}

	@Override
	public String getDescription() {
		return "colour the borders of the complex and it's components";
	}

	@Override
	public String getName() {
		return "Mark Complex Components";
	}

	protected Map<String, PathwayElement> mapComplexIdName() {
		complexIdNameMap = new HashMap<String, PathwayElement>();
		Pathway pwy = pvd.getSwingEngine().getEngine().getActivePathway();
		for (PathwayElement pwe : pwy.getDataObjects()) {
			if (pwe.getObjectType() == ObjectType.DATANODE) {
				if (pwe.getDataNodeType().equalsIgnoreCase("complex")) {
					complexIdNameMap.put(pwe.getElementID(), pwe);
				}
			}
		}
		return complexIdNameMap;
	}

	@Override
	public void loadXML(Element xml) {
		super.loadXML(xml);
		for (int i = 0; i < xml.getChildren(XML_COMPLEX_BORDER).size(); i++) {
			try {
				for (String key : complexIdNameMap.keySet()) {
					xml.getAttributeValue(key);
					complexIdBorderColorMap.put(key, Color.decode(xml.getAttributeValue(key)));
				}
			} catch (Exception e) {
				Logger.log.error("Unable to parse settings for plugin", e);
			}
		}
	}

	protected Map<String, Set<PathwayElement>> mapComplexComponents() {
		complexIdComponentMap = new HashMap<String, Set<PathwayElement>>();
		
		Pathway pathway = pvd.getSwingEngine().getEngine().getActivePathway();
		for (String cid : complexIdNameMap.keySet()) {
			result = new HashSet<PathwayElement>();
			result.add(complexIdNameMap.get(cid));
			for (PathwayElement elt : pathway.getDataObjects()) {
				String id = elt.getDynamicProperty(COMPLEX_ID );
				if (id != null && id.equalsIgnoreCase(cid)) {
					result.add(elt);
				}
			}
			complexIdComponentMap.put(cid, result);
		}
		return complexIdComponentMap;
	}

	protected void setBorderColour(Map<String, Color> cIdBorderColorMap) {
		for(String key : complexIdNameMap.keySet()){
			Color bc = cIdBorderColorMap.get(key);
			complexIdBorderColorMap.put(key, bc);
		}
		modified();
	}

	@Override
	public Element toXML() {
		final Element xml = super.toXML();
		return xml;
	}

	@Override
	public void visualizeOnDrawing(Graphics g, Graphics2D g2d) {
		if (g instanceof GeneProduct) {
			if (g.getPathwayElement().getDataNodeType().equalsIgnoreCase("complex")) {
				drawSample(g2d);
			}
		}
	}

	@Override
	public Component visualizeOnToolTip(Graphics g) {
		return null;
	}

}