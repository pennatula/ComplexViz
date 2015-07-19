package org.pathvisio.complexviz.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.bridgedb.Xref;
import org.pathvisio.complexviz.plugins.ComplexVizPlugin;

/**
 * Called when the (i) button next to a complex component is clicked.
 * 
 * @author anwesha
 */
public class InfoButtonListener implements MouseListener {

	private final Xref xref;
	private final ComplexVizPlugin plugin;

	public InfoButtonListener(Xref xref, ComplexVizPlugin plugin) {
		this.xref = xref;
		this.plugin = plugin;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		plugin.updateData(xref);
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
