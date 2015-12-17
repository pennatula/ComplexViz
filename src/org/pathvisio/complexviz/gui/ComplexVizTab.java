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
package org.pathvisio.complexviz.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.bridgedb.Xref;
import org.pathvisio.complexviz.ComplexVizPlugin;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.view.VPathway;
import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.gex.BackpageExpression;
import org.pathvisio.gui.BackpagePane;
import org.pathvisio.gui.BackpageTextProvider;
import org.pathvisio.gui.BackpageTextProvider.BackpageAttributes;
import org.pathvisio.gui.BackpageTextProvider.BackpageXrefs;
import org.pathvisio.gui.SwingEngine;
import org.pathvisio.gui.view.VPathwaySwing;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The tab in the PathVisio side panel that belongs to the VizPro plugin. It
 * lists the components of the complex that is clicked on
 * 
 * @author anwesha
 * @author mkutmon
 */
public class ComplexVizTab extends JSplitPane {

	// pathway panel (top panel in the tab)
	private JPanel pathwayPanel;
	private JScrollPane pathwayScrollPane;
	
	// data panel (bottom panel in the tab)
	private JPanel dataPanel;
	
	// linkout panel (links pathway elements to data panel)
	private JPanel linkoutPane = new JPanel();
	
	private ComplexVizPlugin plugin;
	private SwingEngine swingEngine;
	private PvDesktop pvd;
	private final static String COMPLEX_ID = "complex_id";

	public ComplexVizTab(ComplexVizPlugin plugin) {
		super(JSplitPane.VERTICAL_SPLIT);
		this.plugin = plugin;
		pvd = plugin.getDesktop();
		swingEngine = plugin.getDesktop().getSwingEngine();
		
		// pathway panel
		pathwayPanel = new JPanel();
		pathwayPanel.setLayout(new BorderLayout());
		pathwayScrollPane = new JScrollPane(pathwayPanel);
		pathwayScrollPane.getVerticalScrollBar().setUnitIncrement(20);
		
		// data panel
		dataPanel = new JPanel();
		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.PAGE_AXIS));
		JScrollPane dataScroll = new JScrollPane(dataPanel);
		dataScroll.getVerticalScrollBar().setUnitIncrement(20);
		
		// split pane layout
		setTopComponent(pathwayScrollPane);
		setBottomComponent(dataScroll);
		setOneTouchExpandable(true);
		setDividerLocation(400);
	}

	private Set<String> createComplexIdSet(String elementid) {
		Set<String> complexidset = new HashSet<String>();
		complexidset.add(elementid);
		return complexidset;
	}
	
	private Set<PathwayElement> getComponentsByComplex(String comid) {
		Set<PathwayElement> result = new HashSet<PathwayElement>();
		Pathway pathway = pvd.getSwingEngine().getEngine()
				.getActivePathway();
		for (PathwayElement elt : pathway.getDataObjects()) {
			String id = elt.getDynamicProperty(COMPLEX_ID);
			if (id != null && id.equalsIgnoreCase(comid)) {
				result.add(elt);
			}
		}
		return result;
	}

	public void setPathwayPanelText(String text) {
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
		dataPanel.removeAll();
		Xref xref = pwe.getXref();

		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.PAGE_AXIS));

		PathwayElement e = PathwayElement.createPathwayElement(ObjectType.DATANODE);
		e.setDataNodeType(pwe.getDataNodeType());
		e.setDataSource(xref.getDataSource());
		e.setElementID(xref.getId());

		BackpageTextProvider bpt = new BackpageTextProvider();
		bpt.addBackpageHook(new BackpageAttributes(swingEngine.getGdbManager().getCurrentGdb()));
		bpt.addBackpageHook(new BackpageXrefs(swingEngine.getGdbManager().getCurrentGdb()));
		bpt.addBackpageHook(new BackpageExpression(plugin.getDesktop().getGexManager()));
		BackpagePane bpp = new BackpagePane(bpt, swingEngine.getEngine());
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
	public void updatePathwayPanel(PathwayElement elm) {		
		Set<String> idset = createComplexIdSet(elm.getElementID());
		
		for(String id : idset) {
			Set<PathwayElement> results = getComponentsByComplex(id);
			if(results.isEmpty()) {
				setPathwayPanelText("<html><br>&nbsp;&nbsp;&nbsp;&nbsp;No results found.</html>");
			} else {
				JLabel loading = new JLabel("<html><br>&nbsp;&nbsp;&nbsp;&nbsp;Loading...</html>", JLabel.LEFT);
				loading.setVerticalAlignment(JLabel.TOP);
				pathwayPanel.removeAll();
				pathwayPanel.setLayout(new BorderLayout());
				pathwayPanel.add(loading, BorderLayout.CENTER);
				pathwayPanel.revalidate();
				
				linkoutPane.setLayout(new BoxLayout(linkoutPane, BoxLayout.Y_AXIS));
				
				linkoutPane.add(new JLabel("<html><br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<B>Identifier and Data:</B></html>"));
				FormLayout layout = new FormLayout("5dlu, pref, 10dlu, pref, 5dlu", "5dlu, pref, 5dlu");
				PanelBuilder builder = new PanelBuilder(layout);
				builder.setDefaultDialogBorder();
				CellConstraints cc = new CellConstraints();
				
				JScrollPane sourceScroll = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				sourceScroll.setMinimumSize(new Dimension(50, 50));
				VPathwaySwing vPathwaySwing = new VPathwaySwing(sourceScroll);
				VPathway vPathway = vPathwaySwing.createVPathway();
				vPathway.setEditMode(false);
				Pathway sourcePw = new Pathway();
				sourcePw.getMappInfo().setMapInfoName("Complex Components");
				int x = 0;
				for (PathwayElement pwe : results) {
					if (pwe.getTextLabel().length() > 0) {
						x++;
						PathwayElement elemNew = pwe.copy();
						elemNew.setMHeight(30);
						elemNew.setMWidth(170);
						elemNew.setMCenterX(100);
						elemNew.setMCenterY((35 * x) + 14);
						sourcePw.add(elemNew);
						JButton linkout = new JButton(pwe.getTextLabel());
						linkout.addMouseListener(new InfoButtonListener(pwe, plugin));
						linkoutPane.add(linkout, Component.CENTER_ALIGNMENT);
						linkoutPane.setVisible(true);
					}
				}
				vPathway.fromModel(sourcePw);
				vPathway.setSelectionEnabled(false);
				vPathway.addVPathwayListener(pvd.getVisualizationManager());
				sourceScroll.add(vPathwaySwing);
				builder.add(sourceScroll, cc.xy(2,2));
				builder.add(linkoutPane, cc.xy(4, 2));
				pathwayPanel.removeAll();
				JLabel selectedLabel = new JLabel("<html><br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<B>Selected: " + elm.getDataSource() + ":\t" + elm.getElementID() + "</B></html>");
				pathwayPanel.setLayout(new BorderLayout());
				pathwayPanel.add(selectedLabel, BorderLayout.NORTH);
				pathwayPanel.add(builder.getPanel(), BorderLayout.CENTER);
				pathwayPanel.revalidate();
				pathwayPanel.repaint();
			}
		}
	}
	
	public void clear() {
		pathwayPanel.removeAll();
		dataPanel.removeAll();
		linkoutPane.removeAll();
		pathwayPanel.repaint();
		dataPanel.repaint();
		linkoutPane.repaint();
	}
}