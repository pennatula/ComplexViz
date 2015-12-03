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
package org.pathvisio.complexviz.plugins;

import java.awt.Color;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

public class BorderColorTableModel extends AbstractTableModel {
	private String columnNames[] = { "Complex Name", "Border Color" };
	private Object[][] colormapArray;

	protected void convertMap2Array(Map<String, Color> cm) {
		colormapArray = new Object[cm.keySet().size()][2];
		int i = 0;
		for (String key : cm.keySet()) {
			colormapArray[i][0] = key;
			colormapArray[i][1] = cm.get(key);
			i++;
		}
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
		return colormapArray.length;
	}

	@Override
	public Object getValueAt(int row, int column) {
		return colormapArray[row][column];
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return (column != 0);
	}

	@Override
	public void setValueAt(Object value, int row, int column) {
		colormapArray[row][column] = value;
	}
}