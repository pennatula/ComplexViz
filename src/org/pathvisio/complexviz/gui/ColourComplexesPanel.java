// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
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

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Desktop.Action;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdom.Element;
import org.pathvisio.complexviz.plugins.VisualisePercentScores;
import org.pathvisio.core.util.Resources;
import org.pathvisio.desktop.util.TextFieldUtils;
import org.pathvisio.desktop.visualization.ColorGradient;
import org.pathvisio.desktop.visualization.ColorGradient.ColorValuePair;
import org.pathvisio.desktop.visualization.ColorSetManager;
import org.pathvisio.desktop.visualization.Criterion;
import org.pathvisio.visualization.gui.ColorGradientCombo;
import org.pathvisio.visualization.gui.ColorGradientPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Configuration panel for the VisualisePercentScores visualization method
 * 
 * @author anwesha
 */
public class ColourComplexesPanel extends JPanel implements ActionListener {
	
//	private ComplexLegendPanel lp;

	private class CriterionPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final JTextField txtExpr;
		private final JLabel lblError;
		private static final String CRIT_VALID = "OK";
		private final Criterion myCriterion = new Criterion();
		private final List<String> percentscore = Arrays.asList("Percent");

		private CriterionPanel() {
			super();
			final FormLayout layout = new FormLayout(
					"4dlu, min:grow, 4dlu, min:grow, 4dlu",
					"4dlu, pref, 4dlu, pref, 4dlu, [50dlu,min]:grow, 4dlu, pref, 4dlu");
			layout.setColumnGroups(new int[][] { { 2, 4 } });
			setLayout(layout);
			final CellConstraints cc = new CellConstraints();
			add(new JLabel("Enter an expression (e.g. [Percent] > 25 )"), cc.xy(2, 2));
			txtExpr = new JTextField(40);
//			txtExpr.setText("[Percent] > 25");
			txtExpr.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void changedUpdate(final DocumentEvent e) {
					updateCriterion();
				}

				@Override
				public void insertUpdate(final DocumentEvent e) {
					updateCriterion();
				}

				@Override
				public void removeUpdate(final DocumentEvent e) {
					updateCriterion();
				}
			});

			add(txtExpr, cc.xyw(2, 4, 3));
			final String[] OPERANDS = { "<", ">", "<=", ">=","==", "!=" };
			final JList lstOperators = new JList(OPERANDS);
			add(new JScrollPane(lstOperators), cc.xy(2, 6));

			lstOperators.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(final MouseEvent me) {
					final int selectedIndex = lstOperators.getSelectedIndex();
					if (selectedIndex >= 0) {
						final String toInsert = OPERANDS[selectedIndex];
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

			final JList lstSamples = new JList(percentscore.toArray());

			lstSamples.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(final MouseEvent me) {
					final String toInsert = "[" + percentscore.get(0) + "]";
					TextFieldUtils.insertAtCursorWithSpace(txtExpr, toInsert);
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

		public Criterion getCriterion() {
			return myCriterion;
		}

		private void updateCriterion() {
			method.setExpression(txtExpr.getText());
			method.setComplexColours();
			final String error = myCriterion.setExpression(txtExpr.getText(),
					percentscore);
			if (error != null) {
				lblError.setText(error);
			} else {
				lblError.setText(CRIT_VALID);
			}
		}
	}

	class Gradient extends JPanel implements ActionListener {

		private static final long serialVersionUID = 1L;
		private final ColorGradientCombo gradientCombo;
		private ColorGradient colourgradient;
		private JPanel valuesPanel;
		private final JPanel gradientPanel;

		public Gradient() {
			setModel(GRADIENT_MODEL);

			gradientPanel = new JPanel();
			CellConstraints cc = new CellConstraints();
			add(gradientPanel, cc.xy(1, 1));

			gradientPanel.setLayout(new FormLayout("pref, 3dlu, pref:grow",
					"pref, pref, pref"));

			gradientCombo = new ColorGradientCombo();

			gradientCombo.addActionListener(this);

			gradientPanel.add(gradientCombo, cc.xy(1, 1));

			add(gradientPanel);

			refresh();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			colourgradient = gradientCombo.getSelectedGradient();
			method.setGradient(colourgradient);
			method.setComplexColours();
			refreshValuesPanel();
		}

		private void refresh() {
			// Get default gradients
			List<ColorGradient> gradients = ColorGradient
					.createDefaultGradients();

			// Set percentage defaults
			for (ColorGradient cg : gradients) {
				for (int i = 0; i < cg.getColorValuePairs().size(); i++) {
					double val = cg.getColorValuePairs().get(i).getValue();
					if (val == -1) {
						cg.getColorValuePairs().get(i).setValue(0);
					}
					if (val == 0) {
						cg.getColorValuePairs().get(i).setValue(50);
					}
					if (val == 1) {
						cg.getColorValuePairs().get(i).setValue(100);
					}

				}
			}

			// Set gradients
			if (colourgradient != null) {
				ColorGradient preset = null;
				for (ColorGradient cg : gradients) {
					if (cg.equalsPreset(colourgradient)) {
						preset = cg;
					}
				}
				gradients.remove(preset);
				gradients.add(colourgradient);
			}
			gradientCombo.setGradients(gradients);
			gradientCombo.setSelectedGradient(colourgradient);

			// Refresh colourgradient values
			refreshValuesPanel();

			revalidate();
		}

		private void refreshValuesPanel() {
			if (valuesPanel != null) {
				gradientPanel.remove(valuesPanel);
			}
			if (colourgradient != null) {
				valuesPanel = new ColorGradientPanel(colourgradient);
				gradientPanel.add(valuesPanel, new CellConstraints().xy(1, 2));
			}
			revalidate();
		}

		void setModel(int model) {
			method.setModel(model);
		}
	}

	/** Panel for editing colorByLine */
	class Rule extends JPanel  implements ActionListener{

		private static final long serialVersionUID = 1L;
		private final JPanel rulePanel;
//		private final CriterionPanel critPanel;
		private final JPanel clrPanel;
//		private JButton rulecolourButton;
//		private JButton notrulecolourButton;

		public Rule() {

			rulePanel = new JPanel();
			final BoxLayout bl = new BoxLayout(rulePanel, BoxLayout.Y_AXIS);
			rulePanel.setLayout(bl);

			critPanel = new CriterionPanel();
			rulePanel.add(critPanel);

			clrPanel = new JPanel();
			clrPanel.setLayout(new GridBagLayout());
			final GridBagConstraints cc = new GridBagConstraints();
			final JLabel clr = new JLabel("Colour (rule met)");
			rulecolourButton = new JButton("...");

			final JLabel notclr = new JLabel("Colour (rule not met)");
			notrulecolourButton = new JButton("...");

			/**
			 * Assigns default colours and values
			 */
			rulecolourButton.setOpaque(true);
			rulecolourButton.setForeground(Color.BLUE);
			rulecolourButton.setBackground(Color.BLUE);

			notrulecolourButton.setOpaque(true);
			notrulecolourButton.setForeground(Color.GRAY);
			notrulecolourButton.setBackground(Color.GRAY);

			/**
			 * Action Listeners for buttons, combo boxes and text boxes to set
			 * colours for the visualization
			 */
			rulecolourButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final Color ruleColour = JColorChooser.showDialog(
							rulecolourButton, "Choose", Color.BLUE);
					rulecolourButton.setForeground(ruleColour);
					rulecolourButton.setBackground(ruleColour);
					method.setExpression(critPanel.getCriterion()
							.getExpression());
					method.setRuleColour(ruleColour);
					method.setComplexColours();
				}
			});
			notrulecolourButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final Color notruleColour = JColorChooser.showDialog(
							rulecolourButton, "Choose", Color.GRAY);
					notrulecolourButton.setForeground(notruleColour);
					notrulecolourButton.setBackground(notruleColour);
					method.setExpression(critPanel.getCriterion()
							.getExpression());
					method.setNotRuleColour(notruleColour);
					method.setComplexColours();
				}
			});
			cc.fill = GridBagConstraints.HORIZONTAL;
			cc.weightx = 0.0;
			cc.gridx = 0;
			cc.gridy = 0;
			clrPanel.add(clr, cc);
			cc.fill = GridBagConstraints.HORIZONTAL;
			cc.weightx = 0.5;
			cc.gridx = 1;
			cc.gridy = 0;
			clrPanel.add(rulecolourButton, cc);
			cc.fill = GridBagConstraints.HORIZONTAL;
			cc.weightx = 0.0;
			cc.gridx = 0;
			cc.gridy = 1;
			clrPanel.add(notclr, cc);
			cc.fill = GridBagConstraints.HORIZONTAL;
			cc.weightx = 0.5;
			cc.gridx = 1;
			cc.gridy = 1;
			clrPanel.add(notrulecolourButton, cc);
			rulePanel.add(clrPanel);
			add(rulePanel);
		}

		private void setModel(int model) {
			method.setModel(model);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			method.setExpression(critPanel.getCriterion().getExpression());
			method.setRuleColour(rulecolourButton.getForeground());
			method.setNotRuleColour(notrulecolourButton.getForeground());
			method.setComplexColours();
			}
	}

	private static final long serialVersionUID = 1L;
	static final String ACTION_RULE = "Colour Rule";
	static final String ACTION_GRADIENT = "Colour Gradient";
	static final String ACTION_OPTIONS = "Rule Visualization Options";

	static final String ACTION_COMBO = "Colorset";
	static final int RULE_MODEL = 1;

	static final int GRADIENT_MODEL = 2;
	static final ImageIcon COLOR_PICK_ICON = new ImageIcon(
			Resources.getResourceURL("colorpicker.gif"));
	static final Cursor COLOR_PICK_CURS = Toolkit.getDefaultToolkit()
			.createCustomCursor(COLOR_PICK_ICON.getImage(), new Point(4, 19),
					"Color picker");
	private final VisualisePercentScores method;
	private final Rule rule;
	private final Gradient gradient;
	private final CardLayout cardLayout;
	private final JPanel settings;
	private final ColorSetManager csm;

	private int model;

	JButton options = new JButton("Edit Rule Visualization");

//	private ColorGradientCombo gradientCombo;
	private CriterionPanel critPanel;
	private JButton notrulecolourButton ;
	private JButton rulecolourButton;
	private boolean useGradient = false;
	private boolean useRule = false;
	

	public ColourComplexesPanel(final VisualisePercentScores method, ColorSetManager csm) {
		this.method = method;
		this.csm = csm;
//		lp = new ComplexLegendPanel();
		model = RULE_MODEL;
				
		method.setDefaultExpresion();
		method.setComplexColours();
//		lp.addDefault();
		setLayout(new FormLayout(
				"4dlu, pref, 4dlu, pref, fill:pref:grow, 4dlu",
				"4dlu, pref, 4dlu, fill:pref:grow, 4dlu"));
		
		JPanel btnPanel = new JPanel();
		BoxLayout bl = new BoxLayout(btnPanel, BoxLayout.X_AXIS);
		btnPanel.setLayout(bl);

		final ButtonGroup buttons = new ButtonGroup();
		/*
		 * Colour rule
		 */
		final JRadioButton rbBasic = new JRadioButton(ACTION_RULE);
		rbBasic.setActionCommand(ACTION_RULE);
		rbBasic.addActionListener(this);
		buttons.add(rbBasic);

		/*
		 * Colour gradient
		 */
		final JRadioButton rgBasic = new JRadioButton(ACTION_GRADIENT);
		rgBasic.setActionCommand(ACTION_GRADIENT);
		rgBasic.addActionListener(this);
		buttons.add(rgBasic);
		
//		/*
//		 * Update Legend
//		 */
//		final JButton legendbtn = new JButton("Update Legend");
//		legendbtn.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent ae2) {
//				updateLegend();
//			}
//		});

		final CellConstraints cc = new CellConstraints();
//		add(rbBasic, cc.xy(2, 2));
//		add(rgBasic, cc.xy(4, 2));
//		add(legendbtn, cc.xy(6, 2));

		btnPanel.add(rbBasic);
		btnPanel.add(rgBasic);
//		btnPanel.add(legendbtn);
		
		add(btnPanel,cc.xyw(2, 2, 4));
		
		settings = new JPanel();
		settings.setBorder(BorderFactory.createEtchedBorder());
		cardLayout = new CardLayout();
		settings.setLayout(cardLayout);

		rule = new Rule();
		gradient = new Gradient();

		settings.add(rule, ACTION_RULE);
		settings.add(gradient, ACTION_GRADIENT);

		add(settings, cc.xyw(2, 4, 4));

		rbBasic.doClick();

	}

//	protected void updateLegend() {
//		lp.addColours(useGradient,useRule);
//}

	@Override
	public void actionPerformed(ActionEvent e) {
		final String action = e.getActionCommand();
		if (ACTION_RULE.equals(action)) {
			model = RULE_MODEL;
			rule.setModel(model);
			method.setExpression(critPanel.getCriterion()
					.getExpression());
			method.setRuleColour(rulecolourButton.getForeground());
			method.setNotRuleColour(notrulecolourButton.getForeground());
			method.setComplexColours();
			useRule = true;
			cardLayout.show(settings, action);
			} else if (ACTION_GRADIENT.equals(action)) {
			model = GRADIENT_MODEL;
			gradient.setModel(model);
			method.setComplexColours();
			cardLayout.show(settings, action);
			useGradient = true;
		}
	}
}