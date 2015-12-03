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
package org.pathvisio.complexviz;

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

import org.pathvisio.complexviz.gui.ComplexVizDialog;
import org.pathvisio.complexviz.gui.ComplexVizTab;
import org.pathvisio.complexviz.plugins.ColourComplexBorder;
import org.pathvisio.complexviz.plugins.ComplexLabel;
import org.pathvisio.complexviz.plugins.VisualisePercentScores;
import org.pathvisio.core.ApplicationEvent;
import org.pathvisio.core.Engine.ApplicationEventListener;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.view.GeneProduct;
import org.pathvisio.core.view.Graphics;
import org.pathvisio.core.view.GraphicsShape;
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
 * @author mkutmon
 * 
 */

public class ComplexVizPlugin implements Plugin, DocumentListener, ApplicationEventListener, SelectionListener, ChangeListener {

	private PvDesktop desktop;
	private ComplexVizPlugin plugin;
	private ComplexVizTab vizprotab;
	private JTabbedPane sidebarTabbedPane;
	private PathwayElement selectedElementId;
	private String COMPLEX_ID = "complex_id";

	@Override
	public void applicationEvent(ApplicationEvent e) {
		vizprotab.clear();
		switch (e.getType()) {
		case VPATHWAY_OPENED: 
			((VPathway) e.getSource()).addSelectionListener(this);
			break;
		case VPATHWAY_CREATED: 
			((VPathway) e.getSource()).addSelectionListener(this);
			break;
		case VPATHWAY_DISPOSED: 
			((VPathway) e.getSource()).removeSelectionListener(this);
			break;
		default:
			break;
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
	}
	
	private VisualizationAction vizAction;

	@Override
	public void done() {
		desktop.getSwingEngine().getEngine().removeApplicationEventListener(this);
		desktop.unregisterMenuAction("Data", vizAction);
		desktop.getSideBarTabbedPane().remove(vizprotab);

	}

	private void findComponents(String q) {
		if (q.length() == 0)
			return; // defensive coding, button should have been disabled anyway
		String query = q.toLowerCase();
		Set<PathwayElement> result = new HashSet<PathwayElement>();
		Pathway pathway = desktop.getSwingEngine().getEngine().getActivePathway();
		if (pathway == null)
			return; // defensive coding, button and text field should have been
					// disabled anyway
		for (PathwayElement elt : pathway.getDataObjects()) {
			String id = elt.getDynamicProperty(COMPLEX_ID );
			if (id != null && id.toLowerCase().contains(query)) {
				result.add(elt);
			}
		}
		Rectangle2D interestingRect = null;
		VPathway vpwy = desktop.getSwingEngine().getEngine().getActiveVPathway();
		vpwy.resetHighlight();
		for (PathwayElement elt : result) {
			VPathwayElement velt = vpwy.getPathwayElementView(elt);
			Color hc = new Color(128, 0, 128);
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

	private void findParentComplex(String q) {
		if (q.length() == 0)
			return; // defensive coding, button should have been disabled anyway
		String query = q.toLowerCase();
		Set<PathwayElement> result = new HashSet<PathwayElement>();
		Pathway pathway = desktop.getSwingEngine().getEngine().getActivePathway();
		if (pathway == null)
			return; // defensive coding, button and text field should have been
					// disabled anyway
		for (PathwayElement elt : pathway.getDataObjects()) {
			String id = elt.getXref().getId();
			if (id != null && id.toLowerCase().contains(query)) {
				result.add(elt);
			}
		}
		Rectangle2D interestingRect = null;
		VPathway vpwy = desktop.getSwingEngine().getEngine()
				.getActiveVPathway();
		vpwy.resetHighlight();
		for (PathwayElement elt : result) {
			VPathwayElement velt = vpwy.getPathwayElementView(elt);
			Color hc = new Color(128, 0, 128);
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
		VisualizationMethodRegistry reg = aDesktop.getVisualizationManager().getVisualizationMethodRegistry();

		reg.registerComplexMethod(VisualisePercentScores.class.toString(),
				new VisualizationMethodProvider() {
					@Override
					public VisualizationMethod create() {
						return new VisualisePercentScores(desktop.getSwingEngine(), desktop
								.getGexManager(), desktop
								.getVisualizationManager().getColorSetManager(), desktop.getVisualizationManager());
					}
				});

		reg.registerComplexMethod(
				ColourComplexBorder.class.toString(),
				new VisualizationMethodProvider() {
					@Override
					public VisualizationMethod create() {
						return new ColourComplexBorder(desktop,
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
		vizAction = new VisualizationAction(aDesktop);
		desktop.registerMenuAction("Data", vizAction);
		PathwayElementMenuHook vizproHook = new PathwayElementMenuHook() {
			private VizProAction vizpro_action = new VizProAction(false);

			@Override
			public void pathwayElementMenuHook(VPathwayElement e,
					JPopupMenu menu) {
				menu.add(vizpro_action);
			}
		};
		createSidePanel();
		desktop.addPathwayElementMenuHook(vizproHook);
		desktop.getSwingEngine().getEngine().addApplicationEventListener(this);
		
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
			if(e.selection.size() == 1) {
				Iterator<VPathwayElement> it = e.selection.iterator();
				VPathwayElement o = it.next();

				if(o instanceof GraphicsShape) {
					PathwayElement elm = ((GraphicsShape)o).getPathwayElement();
					if (elm.getDataNodeType().equalsIgnoreCase("complex")) {
						
						selectedElementId = elm;
						if (selectedElementId.getElementID().length() > 1) {
								vizprotab.updatePathwayPanel(selectedElementId);
						} else {
							vizprotab.setPathwayPanelText("<html><br>&nbsp;&nbsp;&nbsp;&nbsp;DataNode does not have an identifier.</html>");
						}
					} else {
						vizprotab .setPathwayPanelText("<html><br>&nbsp;&nbsp;&nbsp;&nbsp;Not a Complex Node.</html>");
					}
					vizprotab.revalidate();
					vizprotab.repaint();
				}
			} else {
				// multiple elements selected
				vizprotab.clear();
				selectedElementId = null;
			}
			break;
		default:
			vizprotab.clear();
			selectedElementId = null;
			break;
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(selectedElementId == null) {
			
		}
		if (sidebarTabbedPane.getSelectedComponent().equals(vizprotab)) {
			if (selectedElementId.getElementID().length() > 0) {
				vizprotab.updatePathwayPanel(selectedElementId);
				vizprotab.revalidate();
				vizprotab.repaint();
			}
		}
	}

	public boolean update() {
		boolean hasPathway = false;
		if(desktop.getSwingEngine().getEngine()
				.getActivePathway() != null){
			hasPathway = true;	
		}
		return hasPathway;
	}

	public void updateData(PathwayElement pwe) {
		vizprotab.updateDataPanel(pwe);
	}
	
	/**
	 * Action / Menu item for opening the VizPro dialog
	 */
	public static class VisualizationAction extends AbstractAction implements GexManagerListener {
		private MainPanel mainPanel;
		private PvDesktop ste;

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
			boolean isConnected = ste.getGexManager().isConnected();
			setEnabled(isConnected);
		}
	}

	private class VizProAction extends AbstractAction {
		
		VizProAction(boolean b) {
			putValue(NAME, "Highlight Complex / Components");
		}

		/**
		 * called when the user selects the menu item
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			VPathway vPathway = desktop.getSwingEngine().getEngine()
					.getActiveVPathway();
			List<Graphics> selection = vPathway.getSelectedGraphics();
			Graphics g = selection.get(0);
			if (g instanceof GeneProduct) {
				GeneProduct gp = (GeneProduct) g;
				if (gp.getPathwayElement().getDataNodeType()
						.equalsIgnoreCase("complex")) {
					String query = gp.getPathwayElement().getElementID();
					findComponents(query);
				} else {
					if(!gp.getPathwayElement().getDynamicProperty(COMPLEX_ID).isEmpty()){
						String query = gp.getPathwayElement().getDynamicProperty(COMPLEX_ID);
						findParentComplex(query);
					}else{
						JOptionPane.showMessageDialog(desktop.getFrame(),
								"Please select a complex or complex component node.", "Wrong selection",
								JOptionPane.ERROR_MESSAGE);	
					}
					
				}
			}
		}
	}

}
