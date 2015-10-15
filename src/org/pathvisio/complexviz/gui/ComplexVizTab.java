package org.pathvisio.complexviz.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.bridgedb.Xref;
import org.pathvisio.complexviz.ComplexVizPlugin;
import org.pathvisio.core.model.DataNodeType;
import org.pathvisio.core.model.LineStyle;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.ShapeType;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.gex.BackpageExpression;
import org.pathvisio.gui.BackpagePane;
import org.pathvisio.gui.BackpageTextProvider;
import org.pathvisio.gui.SwingEngine;
import org.pathvisio.gui.BackpageTextProvider.BackpageAttributes;
import org.pathvisio.gui.BackpageTextProvider.BackpageXrefs;
import org.pathvisio.gui.view.VPathwaySwing;

/**
 * The tab in the PathVisio side panel that belongs to the VizPro plugin. It
 * lists the components of the complex that is clicked on
 * 
 * @author anwesha
 */
public class ComplexVizTab extends JSplitPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JPanel linkoutPane = new JPanel();
	private final JPanel pathwayPanel = new JPanel();
	private final JPanel dataPanel = new JPanel();
	private final ComplexVizPlugin plugin;
	// private final ImageIcon icon = createImageIcon("/i.gif",
	// "information icon");
	private final SwingEngine swingEngine;
	private final PvDesktop pvd;
	private String COMPLEX_ID = "complex_id";

	public ComplexVizTab(ComplexVizPlugin plugin) {
		super(JSplitPane.VERTICAL_SPLIT);
		this.plugin = plugin;
		pvd = plugin.getDesktop();
		pathwayPanel.setLayout(new BorderLayout());
		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.PAGE_AXIS));
		swingEngine = plugin.getDesktop().getSwingEngine();
		final JScrollPane pathwayScroll = new JScrollPane(pathwayPanel);
		pathwayScroll.getVerticalScrollBar().setUnitIncrement(20);
		final JScrollPane dataScroll = new JScrollPane(dataPanel);
		dataScroll.getVerticalScrollBar().setUnitIncrement(20);
		setTopComponent(pathwayScroll);
		setBottomComponent(dataScroll);
		setOneTouchExpandable(true);
		setDividerLocation(100);
		}

	private PathwayElement clonepwe(PathwayElement pwe, int x) {
		final PathwayElement pwe2 = PathwayElement.createPathwayElement(pwe
				.getObjectType());
		pwe2.setDataSource(pwe.getDataSource());
		pwe2.setElementID(pwe.getElementID());
		pwe2.setTextLabel(pwe.getTextLabel());
		pwe2.setTransparent(false);
		pwe2.setColor(Color.BLACK);
		pwe2.setShapeType(ShapeType.RECTANGLE);
		pwe2.setLineStyle(LineStyle.SOLID);
		pwe2.setInitialSize();
		pwe2.setMHeight(30);
		pwe2.setMWidth(170);
		pwe2.setMCenterX(100);
		pwe2.setMCenterY((35 * x) + 14);
		return pwe2;
	}

	private Set<String> createComplexIdSet(String elementid) {
		final Set<String> complexidset = new HashSet<String>();
		complexidset.add(elementid);
		return complexidset;
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	protected ImageIcon createImageIcon(String path, String description) {
		final java.net.URL imgURL = getClass().getResource(path);
		if (imgURL != null)
			return new ImageIcon(imgURL, description);
		else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	/**
	 * Creates the list of complex components to be displayed in the side tab
	 * {@link ComplexVizTab}
	 * 
	 */
	private VPathwaySwing createPathway(JScrollPane parent,
			Set<PathwayElement> results) {
		final GridLayout boxl = new GridLayout(results.size() + 1, 1);
		linkoutPane.removeAll();
		linkoutPane.setLayout(boxl);
		JTextField dataLbl = new JTextField("Crossreferences & Data");
		dataLbl.setToolTipText("Click on the buttons below!");
		dataLbl.setEditable(false);
		dataLbl.setHorizontalAlignment(JTextField.CENTER);
		linkoutPane.add(dataLbl);
		final VPathwaySwing vPathwaySwing = new VPathwaySwing(parent);
		final VPathway vPathway = vPathwaySwing.createVPathway();
		vPathway.setEditMode(false);
		final Pathway sourcePw = new Pathway();
		sourcePw.getMappInfo().setMapInfoName("Complex Components");
		int x = 0;
		for (final PathwayElement pwe : results) {
			if (pwe.getTextLabel().length() > 0) {
				// System.out.println(pwe.getTextLabel());
				x++;
				sourcePw.add(clonepwe(pwe, x));
				final JButton linkout = new JButton(pwe.getTextLabel());
				// linkout.setPreferredSize(new Dimension(40, 20));
				linkout.addMouseListener(new InfoButtonListener(pwe, plugin));
				linkoutPane.add(linkout);
				linkoutPane.setVisible(true);
			}
		}
		vPathway.fromModel(sourcePw);
		vPathway.setSelectionEnabled(false);
		vPathway.addVPathwayListener(pvd.getVisualizationManager());
		return vPathwaySwing;
	}

	private Set<PathwayElement> getComponentsByComplex(String comid) {
		final Set<PathwayElement> result = new HashSet<PathwayElement>();
		final Pathway pathway = pvd.getSwingEngine().getEngine()
				.getActivePathway();
		for (final PathwayElement elt : pathway.getDataObjects()) {
			final String id = elt.getDynamicProperty(COMPLEX_ID);
			if (id != null && id.equalsIgnoreCase(comid)) {
				result.add(elt);
			}
		}
		return result;
	}

	public void setPathwayPanelText(String text) {
		pathwayPanel.removeAll();
		final JLabel label = new JLabel(text, SwingConstants.LEFT);
		label.setVerticalAlignment(SwingConstants.TOP);
		pathwayPanel.setLayout(new BorderLayout());
		pathwayPanel.add(label, BorderLayout.CENTER);
		pathwayPanel.revalidate();
	}

	public void setDataPanelText(String text) {
		dataPanel.removeAll();
		final JLabel label = new JLabel(text, SwingConstants.LEFT);
		label.setVerticalAlignment(SwingConstants.TOP);
		pathwayPanel.setLayout(new BorderLayout());
		pathwayPanel.add(label, BorderLayout.CENTER);
		pathwayPanel.revalidate();
	}

	/**
	 * Updates the bottom part of the panel with information about the Xref
	 * selected in the top part of the panel
	 * 
	 */
	public void updateDataPanel(PathwayElement pwe) {
		Xref xref = pwe.getXref();
		dataPanel.removeAll();

		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.PAGE_AXIS));

		final PathwayElement e = PathwayElement
				.createPathwayElement(ObjectType.DATANODE);
		e.setDataNodeType(pwe.getDataNodeType());
		e.setDataSource(xref.getDataSource());
		e.setElementID(xref.getId());

		final BackpageTextProvider bpt = new BackpageTextProvider();

		bpt.addBackpageHook(new BackpageAttributes(swingEngine.getGdbManager()
				.getCurrentGdb()));
		bpt.addBackpageHook(new BackpageXrefs(swingEngine.getGdbManager()
				.getCurrentGdb()));
		bpt.addBackpageHook(new BackpageExpression(plugin.getDesktop()
				.getGexManager()));
		final BackpagePane bpp = new BackpagePane(bpt, swingEngine.getEngine());
		bpp.setInput(e);

		bpp.addHyperlinkListener(new HyperlinkListener() {

			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
					try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (final IOException e1) {
						e1.printStackTrace();
					} catch (final URISyntaxException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		dataPanel.add(bpp);
		dataPanel.revalidate();
		dataPanel.repaint();
	}

	/**
	 * Updates panel with a list of components of the selected Complex
	 * 
	 * @param elemid
	 *            The element id of the selected complex
	 */
	public void updatePathwayPanel(final String elementid) {
		
		dataPanel.removeAll();
		final JLabel loading = new JLabel(
				"<html><br>&nbsp;&nbsp;&nbsp;&nbsp;Loading...</html>",
				SwingConstants.LEFT);
		loading.setVerticalAlignment(SwingConstants.TOP);
		pathwayPanel.removeAll();
		pathwayPanel.setLayout(new BorderLayout());
		pathwayPanel.add(loading, BorderLayout.CENTER);
		pathwayPanel.revalidate();
		pathwayPanel.repaint();
		try {
			final Set<String> idset = createComplexIdSet(elementid);
			for (final String id : idset) {
				final Set<PathwayElement> results = getComponentsByComplex(id);
				if (results.size() > 0) {
					final JScrollPane sourceScroll = new JScrollPane(
							ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
							ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
					sourceScroll.setMinimumSize(new Dimension(50, 50));
					final VPathwaySwing vPathwaySwing = createPathway(
							sourceScroll, results);
					sourceScroll.add(vPathwaySwing);
					final JPanel panel = new JPanel();
					panel.setOpaque(true);
					sourceScroll.revalidate();
					sourceScroll.repaint();
					panel.add(sourceScroll);
					pathwayPanel.removeAll();
					pathwayPanel.setLayout(new BoxLayout(pathwayPanel,
							BoxLayout.X_AXIS));
//					setPathwayPanelText("<html><br>&nbsp;&nbsp;&nbsp;&nbsp;Resize panel if pathway diagram is missing.</html>");
					pathwayPanel.add(panel);
					pathwayPanel.add(linkoutPane);
					pathwayPanel.revalidate();
					pathwayPanel.repaint();
				} else {
					setPathwayPanelText("<html><br>&nbsp;&nbsp;&nbsp;&nbsp;No results found.</html>");
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}