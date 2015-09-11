/**
 * 
 */
package org.pathvisio.complexviz.plugins;

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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.bridgedb.Xref;
import org.bridgedb.gui.SimpleFileFilter;
import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.data.DataException;
import org.pathvisio.data.IRow;
import org.pathvisio.desktop.gex.GexManager;
import org.pathvisio.desktop.util.TextFieldUtils;
import org.pathvisio.desktop.visualization.ColorSetManager;
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
	private ButtonGroup colorstyle;
	private JRadioButton gradientbutton;
	private JRadioButton rulebutton;
	private final ColourComplexes method;
	private ColorSetManager csm;
	private JButton vizbtn;
	private JPanel clrPanel;
	private JPanel complexPanel;
	private String COMPLEX_ID = "complex_id";

	/**
	 * Statistics subpanel Pop up the statistics dialog
	 * 
	 * @param complexcolor
	 * @param gexManager
	 * @param swingEngine
	 */
	ComplexStatisticsPanel(ColourComplexes complexcolor,
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
			JOptionPane
					.showMessageDialog(se.getApplicationPanel(),
							"Could not open criterion panel because of a database access error");
		}
		JTextArea info = new JTextArea("A Percentage Score will be calculated for each complex on the pathway. \n" +
		"X = Complex components qualifying the criterion \n"+
				"Y = Total number of components of the complex\n"+
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
		tblResult.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				int row = tblResult.getSelectedRow();
				final ComplexStatisticsResult sr = ((ComplexStatisticsTableModel) (tblResult
						.getModel())).getRow(row);

				// se.openPathway(sr.getFile());
			}
		});
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
		private ComplexResult result;
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
			add(new JLabel("Criterion: "), cc.xy(2, 2));
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

			final JList lstOperators = new JList(Criterion.TOKENS);
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

			final JList lstSamples = new JList(sampleNames.toArray());

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
		// jfc.setCurrentDirectory(PreferenceManager.getCurrent().getFile(StatisticsPreference.STATS_DIR_LAST_USED_RESULTS));
		if (jfc.showDialog(se.getApplicationPanel(), "Save") == JFileChooser.APPROVE_OPTION) {
			File f = jfc.getSelectedFile();
			// PreferenceManager.getCurrent().setFile(StatisticsPreference.STATS_DIR_LAST_USED_RESULTS,
			// jfc.getCurrentDirectory());
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
				Column.R, Column.TOTAL, Column.PERCENT });
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
			// System.out.println(cidset);
			for (String cid : cidset) {
				// System.out.println(cid);
				componentset = new HashSet<Xref>();
				for (PathwayElement component : pathway.getDataObjects()) {
					String componentid = component
							.getDynamicProperty(COMPLEX_ID );
					if (componentid != null
							&& componentid.equalsIgnoreCase(cid)) {
						componentset.add(component.getXref());
					}
				}
				// System.out.println(cid + " : " + componentset);
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
				// dlg.pack();
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
		result.gex = gm.getCachedData();
		result.stm = new ComplexStatisticsTableModel();
		result.stm.setColumns(new Column[] { Column.COMPLEX_NAME,
				Column.COMPLEX_ID, Column.R, Column.TOTAL, Column.PERCENT });
		result.methodDesc = "Percentage calculated";

		for (String cid : cidset) {
			ComplexStatisticsResult spr = calculateComplexPercent(cid);
			complexidpercentmap.put(cid, spr.getZScore());
			// method.setPercentValues(cid, spr.getZScore());
			result.stm.addRow(spr);
		}
		 method.setPercentValues(complexidpercentmap);
		// System.out.println(complexidpercentmap.entrySet());
		result.stm.sort();
		return result;
	}

	private ComplexStatisticsResult calculateComplexPercent(String cid) {
		Set<Xref> componentsRefs = complexidcomponentmap.get(cid);
		float complexComponentPositive = 0;
		float complexComponentTotal = componentsRefs.size();

		for (Xref ref : componentsRefs) {
			System.out.println(ref.getId() + " : data : "
					+ gm.getCachedData().getData(ref));
			if (evaluateRef(ref)) {
				complexComponentPositive++;
			}
		}
		System.out.println("complex : " + cid + " : positive : "
				+ complexComponentPositive + " : total : "
				+ complexComponentTotal);
		System.out.println("ratio:"+complexComponentPositive / complexComponentTotal);
		float percent = (complexComponentPositive / complexComponentTotal) * 100;
		System.out.println("percent : " + percent);
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
		OkCancelDialog optionsDlg = new OkCancelDialog(null,
		 "Percent score visualisation", (Component) e.getSource(), true,
		 false);
		 optionsDlg.setDialogComponent(new ColourComplexesPanel(method, csm));
		 optionsDlg.pack();
		 optionsDlg.setVisible(true);

	}
}