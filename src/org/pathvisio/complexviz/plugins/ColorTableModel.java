package org.pathvisio.complexviz.plugins;

import java.awt.Color;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

public class ColorTableModel extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

//	Object colormapArray[][] = {
//			{ "Network forming tropocollagens", Color.RED },
//			{ "BPAG1e:Plectin", Color.BLUE },
//			{ "Network forming tropocollagens", Color.GREEN },
//			{ "Network forming tropocollagens", Color.RED },
//			{ "BPAG1e:Plectin", Color.BLUE },
//			{ "Network forming tropocollagens", Color.GREEN } };

	String columnNames[] = { "Complex Name", "Border Color" };

	private Object[][] colormapArray;

	
	protected void convertMap2Array(Map<String, Color> cm) {
		System.out.println("size"+cm.keySet().size());
		 colormapArray = new Object[cm.keySet().size()][2];
		int i = 0;
		for (final String key : cm.keySet()) {
			System.out.println("key "+key);
			System.out.println("color"+cm.get(key));
			colormapArray[i][0] = key;
			colormapArray[i][1] = cm.get(key);
			i++;
		}
	}

	@Override
	public Class getColumnClass(int column) {
		return (getValueAt(0, column).getClass());
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public int getRowCount() {
		// return rowData.length;
//		return colormapArray.length;
		return 2;
	}

	@Override
	public Object getValueAt(int row, int column) {
		// return rowData[row][column];
		return colormapArray[row][column];
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return (column != 0);
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		// rowData[row][column] = value;
		colormapArray[row][column] = value;
	}
}