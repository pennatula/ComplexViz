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

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.pathvisio.desktop.visualization.Visualization;
import org.pathvisio.desktop.visualization.VisualizationManager;
import org.pathvisio.visualization.gui.VisualizationDialog;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Main component of the {@link VisualizationDialog} has a subpanel for all
 * available Visualization Methods
 * 
 * @author anwesha
 */
public class ComplexVizPanel extends JPanel {
	private VisualizationManager visMgr;
	private JPanel methods;

	/**
	 * @param visMgr
	 */
	public ComplexVizPanel(VisualizationManager visMgr) {
		this.visMgr = visMgr;
		FormLayout layout = new FormLayout(
				"pref, 4dlu, 100dlu:grow, 4dlu, left:pref",
				"pref, 4dlu, pref, 4dlu, fill:max(250dlu;pref):grow");
		setLayout(layout);
		visMgr.getActiveVisualization();
		String vname = visMgr.getActiveVisualization().getName();
		methods = new JPanel();
		CellConstraints cc = new CellConstraints();
		add(new JLabel(vname), cc.xy(1, 1));
		add(methods, cc.xyw(1, 5, 5));

		refresh();
	}

	private void refresh() {
		methods.removeAll();
		if (visMgr != null) {
			Visualization v = visMgr.getActiveVisualization();

			// Refresh methods panel
			if (v != null) {
				FormLayout layout = new FormLayout("fill:pref:grow");
				DefaultFormBuilder builder = new DefaultFormBuilder(
						layout, methods);
				for (String name : visMgr
						.getVisualizationMethodRegistry()
						.getRegisteredComplexMethods()) {
					ComplexVizMethodPanel mp = new ComplexVizMethodPanel(v, name);
					builder.append(mp);
					builder.nextLine();
				}
			}
		}
		revalidate();
		repaint();
	}
}
