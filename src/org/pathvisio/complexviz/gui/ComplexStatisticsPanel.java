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
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.bridgedb.Xref;
import org.bridgedb.gui.SimpleFileFilter;
import org.pathvisio.complexviz.plugins.Column;
import org.pathvisio.complexviz.plugins.ComplexResult;
import org.pathvisio.complexviz.plugins.ComplexStatisticsResult;
import org.pathvisio.complexviz.plugins.ComplexStatisticsTableModel;
import org.pathvisio.complexviz.plugins.VisualisePercentScores;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.data.DataException;
import org.pathvisio.data.IRow;
import org.pathvisio.desktop.gex.GexManager;
import org.pathvisio.desktop.util.TextFieldUtils;
import org.pathvisio.desktop.visualization.Criterion;
import org.pathvisio.desktop.visualization.Criterion.CriterionException;
import org.pathvisio.gui.SwingEngine;
import org.pathvisio.gui.dialogs.OkCancelDialog;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Panel to let the user set parameters, start calculation and view results of
 * complex statistics calculation
 * 
 * @author anwesha
 * 
 */
public class ComplexStatisticsPanel extends JPanel implements ActionListener {
	private Map<String, Float> complexidpercentmap;
	private Map<String, Set<Xref>> complexidcomponentmap;
	private Map<String, String> idnamemap;
	private Set<String> cidset;
	private Set<Xref> componentset;
	private CriterionPanel critPanel;
	private JButton btnSave;
	private ComplexResult result;
	private JButton btnCalc;
	private GexManager gm;
	private SwingEngine se;
	private JTable tblResult;
	private JLabel lblResult;
	private final VisualisePercentScores method;
	private JButton vizbtn;
	private JPanel complexPanel;
	private String COMPLEX_ID = "complex_id";

	/**
	 * Statistics subpanel Pop up the statistics dialog
	 * 
	 * @param complexcolor
	 * @param gexManager
	 * @param swingEngine
	 */
	public ComplexStatisticsPanel(VisualisePercentScores complexcolor,
			SwingEngine swingEngine, GexManager gexManager) {
		this.method = complexcolor;
		this.se = swingEngine;
		this.gm = gexManager;
		complexPanel = new JPanel();
		BoxLayout layout = new BoxLayout(complexPanel, BoxLayout.Y_AXIS);
		complexPanel.setLayout(layout);
		add(complexPanel);

		try {
			critPanel = new CriterionPanel(gm.getCurrentGex().getSampleNames());
		} catch (DataException ex) {
			JOptionPane.showMessageDialog(se.getApplicationPanel(), "Could not open criterion panel because of a database access error");
		}
		JTextArea info = new JTextArea("A Percentage Score will be calculated for each complex on the pathway. \n" +
				"X = number of unique components qualifying the criterion\n" +
				"Y = Total number of unique complex components\n" +
		"Percentage Score = X/Y * 100");
		info.setFont(new Font("Serif", Font.BOLD, 11));
		info.setForeground(Color.decode("#00529B"));
		info.setBackground(Color.decode("#BDE5F8"));
		complexPanel.add(info);
		complexPanel.add(critPanel);

		JPanel pnlButtons = new JPanel();
		btnCalc = new JButton("Calculate");
		pnlButtons.add(btnCalc);
		btnSave = new JButton("Save results");
		pnlButtons.add(btnSave);
		btnSave.setEnabled(false);
		vizbtn = new JButton("Visualise Percent");
		pnlButtons.add(vizbtn);
		vizbtn.setEnabled(false);
		vizbtn.addActionListener(this);
		complexPanel.add(pnlButtons);
		
		/*
		 * Label for adding general results after analysis is done
		 */
		lblResult = new JLabel();
		complexPanel.add(lblResult);

		tblResult = new JTable();
		JPanel tblPanel = new JPanel();
		JScrollPane scrollPane = new JScrollPane(tblResult);
		tblPanel.add(scrollPane, BorderLayout.CENTER);
		complexPanel.add(tblPanel);

		/*
		 * Visualization
		 */
		btnCalc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (critPanel.getCriterion().getExpression().trim().equals("")) {
					JOptionPane.showMessageDialog(se.getApplicationPanel(),
							"Please enter an expression to calculate");
					return;
				}
				if (!critPanel.lblError.getText().equals(
						CriterionPanel.CRIT_VALID)) {
					JOptionPane.showMessageDialog(
							se.getApplicationPanel(),
							"Your criterion is invalid! Please correct your criterion",
							"Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				File pwFile = se.getEngine().getActivePathway().getSourceFile();
				btnCalc.setEnabled(false);
				doCalculate(pwFile, critPanel.getCriterion());
			}
		});

		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				doSave();
			}
		});
	}

	/**
	 * the panel for entering an expression, complete with list boxes for
	 * selecting operator and sample. TODO: figure out if this can be re-used in
	 * the color rule panel
	 */
	private static class CriterionPanel extends JPanel {
		private JTextField txtExpr;
		private JLabel lblError;
		private static String CRIT_VALID = "OK";
		private Criterion myCriterion = new Criterion();
		private final List<String> sampleNames;

		public Criterion getCriterion() {
			return myCriterion;
		}

		private void updateCriterion() {
			String error = myCriterion.setExpression(txtExpr.getText(),
					sampleNames);
			if (error != null) {
				lblError.setText(error);
			} else {
				lblError.setText(CRIT_VALID);
			}
		}

		private CriterionPanel(List<String> aSampleNames) {
			super();
			sampleNames = aSampleNames;

			FormLayout layout = new FormLayout(
					"4dlu, min:grow, 4dlu, min:grow, 4dlu",
					"4dlu, pref, 4dlu, pref, 4dlu, [50dlu,min]:grow, 4dlu, pref, 4dlu");
			layout.setColumnGroups(new int[][] { { 2, 4 } });
			setLayout(layout);
			CellConstraints cc = new CellConstraints();
			add(new JLabel("Criterion: e.g. [P.Value] <= 0.05 "), cc.xy(2, 2));
			txtExpr = new JTextField(40);
			txtExpr.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
					updateCriterion();
				}

				public void insertUpdate(DocumentEvent e) {
					updateCriterion();
				}

				public void removeUpdate(DocumentEvent e) {
					updateCriterion();
				}
			});

			add(txtExpr, cc.xyw(2, 4, 3));

			JList lstOperators = new JList(Criterion.TOKENS);
			add(new JScrollPane(lstOperators), cc.xy(2, 6));

			lstOperators.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent me) {
					int selectedIndex = lstOperators.getSelectedIndex();
					if (selectedIndex >= 0) {
						String toInsert = Criterion.TOKENS[selectedIndex];
						TextFieldUtils.insertAtCursorWithSpace(txtExpr,
								toInsert);
					}
					// after clicking on the list, move focus back to text field
					// so
					// user can continue typing
					txtExpr.requestFocusInWindow();
					// on Mac L&F, requesting focus leads to selecting the whole
					// field move caret a bit to work around.
					// Last char is a space anyway.
					txtExpr.setCaretPosition(txtExpr.getDocument().getLength() - 1);
				}
			});

			JList lstSamples = new JList(sampleNames.toArray());

			lstSamples.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent me) {
					int selectedIndex = lstSamples.getSelectedIndex();
					if (selectedIndex >= 0) {
						String toInsert = "[" + sampleNames.get(selectedIndex)
								+ "]";
						TextFieldUtils.insertAtCursorWithSpace(txtExpr,
								toInsert);
					}
					// after clicking on the list, move focus back to text field
					// so
					// user can continue typing
					txtExpr.requestFocusInWindow();
					// on Mac L&F, requesting focus leads to selecting the whole
					// field
					// move caret a bit to work around. Last char is a space
					// anyway.
					txtExpr.setCaretPosition(txtExpr.getDocument().getLength() - 1);
				}
			});

			add(new JScrollPane(lstSamples), cc.xy(4, 6));
			lblError = new JLabel(CRIT_VALID);
			add(lblError, cc.xyw(2, 8, 3));

			txtExpr.requestFocus();
		}
	}

	/**
	 * Save the statistics results to tab delimted text
	 */
	private void doSave() {
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle("Save results");
		jfc.setFileFilter(new SimpleFileFilter("Tab delimited text", "*.txt",
				true));
		jfc.setDialogType(JFileChooser.SAVE_DIALOG);
		if (jfc.showDialog(se.getApplicationPanel(), "Save") == JFileChooser.APPROVE_OPTION) {
			File f = jfc.getSelectedFile();
			if (!f.toString().endsWith(".txt")) {
				f = new File(f + ".txt");
			}
			try {
				result.save(f);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(se.getApplicationPanel(),
						"Could not save results: " + e.getMessage());
				Logger.log.error("Could not save results", e);
			}
		}
	}

	/**
	 * asynchronous statistics calculation function
	 */
	private void doCalculate(final File pwDir, final Criterion crit) {
		btnSave.setEnabled(false);
		/*
		 * temporary model that will be filled with intermediate results.
		 */
		ComplexStatisticsTableModel temp = new ComplexStatisticsTableModel();
		temp.setColumns(new Column[] { Column.COMPLEX_NAME, Column.COMPLEX_ID,
				Column.X, Column.Y, Column.PERCENT });
		tblResult.setModel(temp);
		Pathway pathway = new Pathway();

		try {
			pathway.readFromXml(pwDir, true);
			complexidcomponentmap = new HashMap<String, Set<Xref>>();
			complexidpercentmap = new HashMap<String, Float>();
			idnamemap = new HashMap<String, String>();
			cidset = new HashSet<String>();
			for (PathwayElement pwe : pathway.getDataObjects()) {
				if (pwe.getObjectType() == ObjectType.DATANODE) {
					if (pwe.getDataNodeType().equalsIgnoreCase("complex")) {
						cidset.add(pwe.getElementID());
						idnamemap.put(pwe.getElementID(), pwe.getTextLabel());
					}
				}
			}
			for (String cid : cidset) {
				componentset = new HashSet<Xref>();
				for (PathwayElement component : pathway.getDataObjects()) {
					String componentid = component
							.getDynamicProperty(COMPLEX_ID );
					if (componentid != null
							&& componentid.equalsIgnoreCase(cid)) {
						componentset.add(component.getXref());
					}
				}
				complexidcomponentmap.put(cid, componentset);
			}
			result = calculatePercent();
			if (result.stm.getRowCount() == 0) {
				JOptionPane.showMessageDialog(null,
						"0 results found, did you choose the right directory?");
			} else {
				// replace temp tableModel with definitive one
				tblResult.setModel(result.stm);
				ComplexStatisticsPanel.this.result = result;
			}
		} catch (ConverterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		vizbtn.setEnabled(true);
		btnCalc.setEnabled(true);
		btnSave.setEnabled(true);
	}

	private ComplexResult calculatePercent() {
		complexidpercentmap = new HashMap<String, Float>();
		result = new ComplexResult();
		result.crit = critPanel.getCriterion();
		result.pwFile = se.getEngine().getActivePathway().getSourceFile().getAbsoluteFile();
		result.gdb = se.getGdbManager().getCurrentGdb();
		result.gex = gm.getCachedData();
		result.stm = new ComplexStatisticsTableModel();
		result.stm.setColumns(new Column[] { Column.COMPLEX_NAME,
				Column.COMPLEX_ID, Column.X, Column.Y, Column.PERCENT });
		result.methodDesc = "**************************************************************\n" +
				"Percentage scores calculated for the complexes on the pathway based on the criterion\n" +
				"X = number of unique components qualifying the criterion\n" +
				"Y = Total number of unique complex components\n" +
				"Percentage Score = X/Y * 100";

		for (String cid : cidset) {
			ComplexStatisticsResult spr = calculateComplexPercent(cid);
			complexidpercentmap.put(cid, spr.getZScore());
			result.stm.addRow(spr);
		}
		method.setPercentValues(complexidpercentmap);
		result.stm.sort();
		return result;
	}

	private ComplexStatisticsResult calculateComplexPercent(String cid) {
		Set<Xref> componentsRefs = complexidcomponentmap.get(cid);
		float complexComponentPositive = 0;
		/*
		 * size is -1 as the complex is also member of this list
		 */
		float complexComponentTotal = componentsRefs.size()-1;

		for (Xref ref : componentsRefs) {
			if (evaluateRef(ref)) {
				complexComponentPositive++;
			}
		}
		float percent = (complexComponentPositive / complexComponentTotal) * 100;
		complexidpercentmap.put(cid, percent);
		ComplexStatisticsResult spr = new ComplexStatisticsResult(
				idnamemap.get(cid), cid, complexComponentPositive,
				complexComponentTotal, percent);
		return spr;
	}

	

	/**
	 * Checks if the given ref evaluates positive for the criterion
	 * 
	 * Assumes that ref has already been cached earlier in a call to
	 * result.gex.cacheData(...)
	 */
	private boolean evaluateRef(Xref srcRef) {
		boolean eval = false;
		List<? extends IRow> rows = gm.getCachedData().getData(srcRef);

		if (rows != null) {
			for (IRow row : rows) {

				// if (pk != null && pk.isCancelled()) return false;
				// Use group (line number) to identify a measurement
				try {
					eval = result.crit.evaluate(row.getByName());
				} catch (CriterionException e) {
					Logger.log.error("Unknown error during statistics", e);
				}
			}
		}
		return eval;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		OkCancelDialog optionsDlg = new OkCancelDialog(null, "Percent score visualisation", (Component) e.getSource(), true, false);
		 optionsDlg.setDialogComponent(new ColourComplexesPanel(method));
		 optionsDlg.pack();
		 optionsDlg.setVisible(true);
	}
}