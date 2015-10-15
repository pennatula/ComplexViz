package org.pathvisio.complexviz.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.bridgedb.Xref;
import org.pathvisio.complexviz.ComplexVizPlugin;
import org.pathvisio.core.model.PathwayElement;

/**
 * Called when the (i) button next to a complex component is clicked.
 * 
 * @author anwesha
 */
public class InfoButtonListener implements MouseListener {
private final PathwayElement pwe;
//	private final Xref xref;
	private final ComplexVizPlugin plugin;

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
