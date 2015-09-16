package org.pathvisio.complexviz.plugins;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.bridgedb.Xref;
import org.pathvisio.complexviz.gui.ComplexLegendPanel;
import org.pathvisio.complexviz.gui.ComplexVizDialog;
import org.pathvisio.complexviz.gui.ComplexVizTab;
import org.pathvisio.core.ApplicationEvent;
import org.pathvisio.core.Engine.ApplicationEventListener;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.view.GeneProduct;
import org.pathvisio.core.view.Graphics;
import org.pathvisio.core.view.SelectionBox.SelectionEvent;
import org.pathvisio.core.view.SelectionBox.SelectionListener;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.core.view.VPathwayElement;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.gex.GexManager.GexManagerEvent;
import org.pathvisio.desktop.gex.GexManager.GexManagerListener;
import org.pathvisio.desktop.plugin.Plugin;
import org.pathvisio.desktop.visualization.VisualizationMethod;
import org.pathvisio.desktop.visualization.VisualizationMethodProvider;
import org.pathvisio.desktop.visualization.VisualizationMethodRegistry;
import org.pathvisio.gui.MainPanel;
import org.pathvisio.gui.PathwayElementMenuListener.PathwayElementMenuHook;

/**
 * Plugin that calculates z-scores for complex and pathway nodes (i.e any node
 * which represents a number of nodes)and colours them using colour gradients or
 * rules set by the user
 * 
 * @author anwesha
 * 
 */

public class ComplexVizPlugin implements Plugin, DocumentListener,
		ApplicationEventListener, SelectionListener, ChangeListener {

	/**
	 * Action / Menu item for opening the VizPro dialog
	 */
	public static class VisualizationAction extends AbstractAction implements
			GexManagerListener {
		private static final long serialVersionUID = 1L;
		MainPanel mainPanel;
		private final PvDesktop ste;

		public VisualizationAction(PvDesktop ste) {
			this.ste = ste;
			putValue(NAME, "ComplexViz options");
			mainPanel = ste.getSwingEngine().getApplicationPanel();
			setEnabled(ste.getGexManager().isConnected());
			ste.getGexManager().addListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			new ComplexVizDialog(ste.getVisualizationManager(), ste
					.getSwingEngine().getFrame(), mainPanel).setVisible(true);
		}

		@Override
		public void gexManagerEvent(GexManagerEvent e) {
			final boolean isConnected = ste.getGexManager().isConnected();
			setEnabled(isConnected);
		}
	}

	private class VizProAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		VizProAction(boolean b) {
			putValue(NAME, "Highlight Complex Components");
		}

		/**
		 * called when the user selects the menu item
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			final VPathway vPathway = desktop.getSwingEngine().getEngine()
					.getActiveVPathway();
			final List<Graphics> selection = vPathway.getSelectedGraphics();
			final Graphics g = selection.get(0);
			if (g instanceof GeneProduct) {
				final GeneProduct gp = (GeneProduct) g;
				if (gp.getPathwayElement().getDataNodeType()
						.equalsIgnoreCase("complex")) {
					final String query = gp.getPathwayElement().getElementID();
					doSearch(query);
				} else {
					JOptionPane.showMessageDialog(desktop.getFrame(),
							"Please select a complex node.", "Not a complex",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	private PvDesktop desktop;
	private ComplexVizPlugin plugin;
	private ComplexVizTab vizprotab;
	private JTabbedPane sidebarTabbedPane;

	private Set<PathwayElement> compwe = new HashSet<PathwayElement>();

	private String selectedElementId;
	private ComplexLegendPanel legendtab;
	private String COMPLEX_ID = "complex_id";

	// private final VizProAction vizproAction = new VizProAction(true);

	@Override
	public void applicationEvent(ApplicationEvent e) {
		switch (e.getType()) {
		case VPATHWAY_OPENED: {
			((VPathway) e.getSource()).addSelectionListener(this);
			for (final PathwayElement o : ((Pathway) e.getSource())
					.getDataObjects()) {
				if (o.getDataNodeType().equalsIgnoreCase("complex")) {
					compwe = getComplexComponents(o.getElementID());
				}
			}
		}
			break;
		case VPATHWAY_CREATED: {
			((VPathway) e.getSource()).addSelectionListener(this);
			for (final PathwayElement o : ((Pathway) e.getSource())
					.getDataObjects()) {
				if (o.getDataNodeType().equalsIgnoreCase("complex")) {
					compwe = getComplexComponents(o.getElementID());
				}
			}
		}
			break;
			
		case VPATHWAY_DISPOSED: {
			((VPathway) e.getSource()).removeSelectionListener(this);
		}
		}
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {
		update();
	}

	private void createSidePanel() {
		/*
		 * Create complex components tab
		 */
		vizprotab = new ComplexVizTab(plugin);
		sidebarTabbedPane = desktop.getSideBarTabbedPane();
		desktop.getSwingEngine().getEngine().addApplicationEventListener(this);
		sidebarTabbedPane.add("Components", vizprotab);
		/*
		 * Create complex legend tab
		 */
//		legendtab = new ComplexLegendPanel(desktop.getVisualizationManager());
//		sidebarTabbedPane.add("Complex Legend", legendtab);
	}

	@Override
	public void done() {
		desktop.getSwingEngine().getEngine()
				.removeApplicationEventListener(this);
	}

	private void doSearch(String q) {
		if (q.length() == 0)
			return; // defensive coding, button should have been disabled anyway
		final String query = q.toLowerCase();
		final Set<PathwayElement> result = new HashSet<PathwayElement>();
		final Pathway pathway = desktop.getSwingEngine().getEngine()
				.getActivePathway();
		if (pathway == null)
			return; // defensive coding, button and text field should have been
					// disabled anyway
		for (final PathwayElement elt : pathway.getDataObjects()) {
			final String parent_complex_id = elt.getTextLabel();
			if (parent_complex_id != null
					&& parent_complex_id.toLowerCase().contains(query)) {
				result.add(elt);
			}

			final String id = elt.getDynamicProperty(COMPLEX_ID );
			if (id != null && id.toLowerCase().contains(query)) {
				result.add(elt);
			}
		}
		Rectangle2D interestingRect = null;
		final VPathway vpwy = desktop.getSwingEngine().getEngine()
				.getActiveVPathway();
		vpwy.resetHighlight();
		for (final PathwayElement elt : result) {
			final VPathwayElement velt = vpwy.getPathwayElementView(elt);
			final Color hc = new Color(128, 0, 128);
			velt.highlight(hc);
			if (interestingRect == null) {
				interestingRect = velt.getVBounds();
			} else {
				interestingRect.add(velt.getVBounds());
			}
		}

		if (interestingRect != null) {
			vpwy.getWrapper().scrollTo(interestingRect.getBounds());
		}
	}

	private Set<PathwayElement> getComplexComponents(String comid) {
		final Set<PathwayElement> result = new HashSet<PathwayElement>();
		final Pathway pathway = desktop.getSwingEngine().getEngine()
				.getActivePathway();
		final String cid = comid;
		for (final PathwayElement elt : pathway.getDataObjects()) {
			final String id = elt.getDynamicProperty("reactome_id");
			if (id != null && id.equalsIgnoreCase(cid)) {
				result.add(elt);
			}
		}
		return result;
	}

	public PvDesktop getDesktop() {
		return desktop;
	}

	@Override
	public void init(PvDesktop aDesktop) {
		plugin = this;
		desktop = aDesktop;

		/**
		 * Register visualization methods
		 */

		final VisualizationMethodRegistry reg = aDesktop
				.getVisualizationManager().getVisualizationMethodRegistry();

		reg.registerComplexMethod(ColourComplexes.class.toString(),
				new VisualizationMethodProvider() {
					@Override
					public VisualizationMethod create() {
						return new ColourComplexes(desktop.getSwingEngine(), desktop
								.getGexManager(), desktop
								.getVisualizationManager().getColorSetManager(), desktop.getVisualizationManager());
					}
				});

		reg.registerComplexMethod(
				ColourComplexComponentBorder.class.toString(),
				new VisualizationMethodProvider() {
					@Override
					public VisualizationMethod create() {
						return new ColourComplexComponentBorder(desktop,
								desktop.getVisualizationManager()
										.getColorSetManager());
					}
				});
		
		reg.registerComplexMethod(
				ComplexLabel.class.toString(),
				new VisualizationMethodProvider() {
					@Override
					public VisualizationMethod create() {
						return new ComplexLabel();
					}
				});
		
		// Register the menu items
		desktop.registerMenuAction("Data", new VisualizationAction(aDesktop));
		final PathwayElementMenuHook vizproHook = new PathwayElementMenuHook() {
			private final VizProAction vizpro_action = new VizProAction(false);

			@Override
			public void pathwayElementMenuHook(VPathwayElement e,
					JPopupMenu menu) {
				menu.add(vizpro_action);
			}
		};
		desktop.addPathwayElementMenuHook(vizproHook);
		desktop.getSwingEngine().getEngine().addApplicationEventListener(this);
		createSidePanel();
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		update();
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		update();
	}

	@Override
	public void selectionEvent(SelectionEvent e) {

		switch (e.type) {
		case SelectionEvent.OBJECT_ADDED:
			if (e.selection.size() == 1) {
				final Iterator<VPathwayElement> it = e.selection.iterator();
				final VPathwayElement o = it.next();

				if (o instanceof GeneProduct) {
					if ((((GeneProduct) o).getPathwayElement()
							.getDataNodeType().equalsIgnoreCase("complex"))) {
						selectedElementId = ((GeneProduct) o)
								.getPathwayElement().getElementID();
						compwe = getComplexComponents(selectedElementId);
						if (selectedElementId.length() > 1) {
							if (sidebarTabbedPane.getSelectedComponent()
									.equals(vizprotab)) {
								vizprotab.updatePathwayPanel(selectedElementId);
								vizprotab.revalidate();
								vizprotab.repaint();
							}
						} else {
							vizprotab
									.setPathwayPanelText("<html><br>&nbsp;&nbsp;&nbsp;&nbsp;DataNode does not have an identifier.</html>");
							vizprotab.revalidate();
							vizprotab.repaint();}
					}else{
					vizprotab
					.setPathwayPanelText("<html><br>&nbsp;&nbsp;&nbsp;&nbsp;Not a Complex Node.</html>");}
					vizprotab.revalidate();
					vizprotab.repaint();}
			}
			break;
		case SelectionEvent.SELECTION_CLEARED:
			selectedElementId = null;
			break;
		}
		vizprotab.revalidate();
		vizprotab.repaint();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (sidebarTabbedPane.getSelectedComponent().equals(vizprotab)) {
			if (selectedElementId.length() > 0) {
				vizprotab.updatePathwayPanel(selectedElementId);
				vizprotab.revalidate();
				vizprotab.repaint();
			}
		}
	}

	public void update() {
		final boolean hasPathway = desktop.getSwingEngine().getEngine()
				.getActivePathway() != null;
	}

	public void updateData(final Xref xref) {
		vizprotab.updateDataPanel(xref);
	}
}
