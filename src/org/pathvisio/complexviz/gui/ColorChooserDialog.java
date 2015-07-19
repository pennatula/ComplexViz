package org.pathvisio.complexviz.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.pathvisio.complexviz.plugins.ColorTableModel;

public class ColorChooserDialog {
//	private Object[][] colormapArray;

	public JPanel colorChooserPanel() {
		final JPanel newpanel = new JPanel();
		final BorderLayout layout = new BorderLayout();
		newpanel.setLayout(layout);
		final TableModel model = new ColorTableModel();
		final JTable table = new JTable(model);
		final TableColumn column = table.getColumnModel().getColumn(1);
		final TableCellEditor editor = new ColorChooserEditor();
		column.setCellEditor(editor);
		final JScrollPane scrollPane = new JScrollPane(table);
		newpanel.add(scrollPane);
		return newpanel;
	}

//	public void convertMap2Array(Map<String, Color> cm) {
//		int i = 0;
//		for (final String key : cm.keySet()) {
//			colormapArray[i][0] = key;
//			colormapArray[i][1] = cm.get(key);
//			i++;
//		}
//		
//	}
}

class ColorChooserEditor extends AbstractCellEditor implements TableCellEditor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final JButton delegate = new JButton();

	Color savedColor;

	public ColorChooserEditor() {
		final ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				final Color color = JColorChooser.showDialog(delegate,
						"Color Chooser", savedColor);
				ColorChooserEditor.this.changeColor(color);
			}
		};
		delegate.addActionListener(actionListener);
	}

	private void changeColor(Color color) {
		if (color != null) {
			savedColor = color;
			delegate.setBackground(color);
		}
	}

	@Override
	public Object getCellEditorValue() {
		return savedColor;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		changeColor((Color) value);
		return delegate;
	}
}

