// VizPro Plugin for PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2014 BiGCaT Bioinformatics
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

import java.awt.Component;
import java.awt.Frame;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.pathvisio.core.debug.Logger;
import org.pathvisio.desktop.visualization.VisualizationManager;
import org.pathvisio.gui.dialogs.OkCancelDialog;

/**
 * Dialog for editing dataset visualization options for complexes.
 * 
 * @author anwesha
 */
@SuppressWarnings("serial")
public class ComplexVizDialog extends OkCancelDialog {

	ComplexVizPanel vizproPanel;
	VisualizationManager visMgr;

	/**
	 * @param visMgr
	 * @param frame
	 * @param locationComp
	 */
	public ComplexVizDialog(VisualizationManager visMgr, Frame frame,
			Component locationComp) {
		super(frame, "Visualization options for Complexes", locationComp, true,
				false);

		this.visMgr = visMgr;

		vizproPanel = new ComplexVizPanel(visMgr);
		setDialogComponent(new JScrollPane(vizproPanel));

		// // If there is no visualization yet, create one
		// if (visMgr.getVisualizations().size() == 0) {
		// visMgr.addVisualization(new Visualization("untitled"));
		// }
		// if (visMgr.getActiveVisualization() == null) {
		// visMgr.setActiveVisualization(0);
		// }

		pack();
		// we have to do this again after pack():
		setLocationRelativeTo(locationComp);
	}

	@Override
	protected void okPressed() {
		try {
			visMgr.saveXML(); // Save the settings
		} catch (final Exception ex) {
			Logger.log.error("Couldn't save visualization", ex);
			JOptionPane.showMessageDialog(this,
					"Couldn't write modifications to disk.\n" + ex.getMessage()
							+ "\nSee error log for details.",
					"Couldn't save visualization", JOptionPane.ERROR_MESSAGE);
		}
		super.okPressed();
	}
}
