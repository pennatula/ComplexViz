/**
 * 
 */
package org.pathvisio.complexviz.plugins;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
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
import org.pathvisio.complexviz.gui.ColorChooserDialog;
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
public class ColourComplexComponentBorder extends AbstractVisualizationMethod {
	JButton clrbtn;
	JLabel complexlbl;
	PvDesktop pvd;
	private Map<String, JButton> buttonCache;
//	private ComplexLegendPane lp;
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

	private static final String XML_COMPLEX_ID = "complex_id";

	public ColourComplexComponentBorder(PvDesktop desktop, ColorSetManager csm) {
		pvd = desktop;
//		lp = new ComplexLegendPane();
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

	private void drawColoredRectangle(Rectangle r, Graphics2D g2d, Color c,
			int lt) {
		g2d.setPaint(c);
		g2d.setColor(c);
		g2d.setStroke(new BasicStroke(lt));
		g2d.draw(r);
	}

	private void drawSample(Graphics2D g2d) {
		final VPathway vpwy = pvd.getSwingEngine().getEngine()
				.getActiveVPathway();
		for (final String cid : complexIdComponentMap.keySet()) {
			final Set<PathwayElement> componentNodes = complexIdComponentMap.get(cid);
			border_colour = complexIdBorderColorMap.get(cid);
			for (final PathwayElement gp : componentNodes) {
				final VPathwayElement gpview = vpwy.getPathwayElementView(gp);
				final Rectangle r = gpview.getVBounds().getBounds();
				drawColoredRectangle(r, g2d, border_colour, 2);
			}
		}
	}

	@Override
	public JPanel getConfigurationPanel() {
		mapComplexIdName();
		mapComplexComponents();
		setDefaultBorderColors();
		final JPanel panel = new JPanel();
		final BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(layout);		
////		JButton legendbtn = new JButton("Update Legend");
////		legendbtn.addActionListener(new ActionListener() {
////			@Override
////			public void actionPerformed(ActionEvent ae2) {
////				updateLegend();
////			}
////		});
////		panel.add(legendbtn);
		buttonCache = new HashMap<String, JButton>();
		JButton apply = new JButton("Apply");
		for (final String key : complexIdNameMap.keySet()) {
			final String complexname = complexIdNameMap.get(key).getTextLabel();
			final JPanel subpanel = new JPanel();
			final GridLayout sublayout = new GridLayout(0,2);
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
					Color c = JColorChooser.showDialog(null, "Choose a Color",
							button.getForeground());
					button.setForeground(c);
					button.setBackground(c);
					complexIdBorderColorMap.put(key, c);
					modified();
					//					c = null;
				}
			});
			subpanel.add(complexlbl);
			subpanel.add(clrbtn);
			panel.add(subpanel);
		}
//		apply.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent ae2) {
//				modified();
//			}
//		});
//		panel.add(new ComplexBorderPanel(this));
//		panel.add(apply);
//		modified();
		return panel;
	}

protected void refresh(){
	
}
	
//	protected void updateLegend() {
//		lp.addBorders(complexIdNameMap, complexIdBorderColorMap);
//		lp.revalidate();
//		lp.repaint();
////			lp.updateLegend(panel);
//		}

	private Color generateRandomColor(){
		int R = (int)(Math.random()*256);
		int G = (int)(Math.random()*256);
		int B= (int)(Math.random()*256);
		Color color = new Color(R, G, B); //random color, but can be bright or dull
		while(color.equals(random_color)){
			//to get rainbow, pastel colors
			Random random = new Random();
			final float hue = random.nextFloat();
			final float saturation = 0.9f;//1.0 for brilliant, 0.0 for dull
			final float luminance = 1.0f; //1.0 for brighter, 0.0 for black
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
		final Pathway pwy = pvd.getSwingEngine().getEngine().getActivePathway();
		for (final PathwayElement pwe : pwy.getDataObjects()) {
			if (pwe.getObjectType() == ObjectType.DATANODE) {
				if (pwe.getDataNodeType().equalsIgnoreCase("complex")) {
//					complexIdNameMap.put(pwe.getElementID(), pwe.getTextLabel());
					complexIdNameMap.put(pwe.getElementID(), pwe);
				}
			}
		}
		return complexIdNameMap;
	}

	@Override
	public final void loadXML(Element xml) {
		super.loadXML(xml);
		for (int i = 0; i < xml.getChildren(XML_COMPLEX_BORDER).size(); i++) {
			try {
				for (final String key : complexIdNameMap.keySet()) {
					xml.getAttributeValue(key);
					complexIdBorderColorMap.put(key,
							Color.decode(xml.getAttributeValue(key)));
				}
			} catch (final Exception e) {
				Logger.log.error("Unable to parse settings for plugin", e);
			}
		}
	}

	protected Map<String, Set<PathwayElement>> mapComplexComponents() {
		complexIdComponentMap = new HashMap<String, Set<PathwayElement>>();
		
		final Pathway pathway = pvd.getSwingEngine().getEngine()
				.getActivePathway();
		for (final String cid : complexIdNameMap.keySet()) {
			result = new HashSet<PathwayElement>();
			result.add(complexIdNameMap.get(cid));
			for (final PathwayElement elt : pathway.getDataObjects()) {
				final String id = elt.getDynamicProperty(COMPLEX_ID );
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
//		if (bordercolour != null) {
//			border_colour = bordercolour;
//			modified();
//		}
		

	}
	
	private Color getBorderColour() {
		return border_colour;

	}

	@Override
	public Element toXML() {
		final Element xml = super.toXML();
//		final Element elm = new Element(XML_COMPLEX_BORDER);
//		xml.addContent(elm);
//		for (final String key : complexIdBorderColorMap.keySet()) {
//			final Element selm = new Element(XML_COMPLEX_ID);
//			final Color bc = complexIdBorderColorMap.get(key);
//			final String hex = String.format("#%02x%02x%02x", bc.getRed(),
//					bc.getGreen(), bc.getBlue());
//			selm.setAttribute(key, hex);
//			xml.addContent(selm);
//		}
		return xml;
	}

	@Override
	public void visualizeOnDrawing(Graphics g, Graphics2D g2d) {
		if (g instanceof GeneProduct) {
			if (g.getPathwayElement().getDataNodeType()
					.equalsIgnoreCase("complex")) {
				final GeneProduct gp = (GeneProduct) g;
				drawSample(g2d);
//				gp.markDirty();
			}

		}

	}

	@Override
	public Component visualizeOnToolTip(Graphics g) {
		return null;
	}

}