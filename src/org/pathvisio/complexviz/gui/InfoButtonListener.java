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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.pathvisio.complexviz.ComplexVizPlugin;
import org.pathvisio.core.model.PathwayElement;

/**
 * Called when the (i) button next to a complex component is clicked.
 * 
 * @author anwesha
 */
public class InfoButtonListener implements MouseListener {
	private PathwayElement pwe;
	private ComplexVizPlugin plugin;

	public InfoButtonListener(PathwayElement pwe, ComplexVizPlugin plugin) {
		this.pwe = pwe;
		this.plugin = plugin;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		plugin.updateData(pwe);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}
}
