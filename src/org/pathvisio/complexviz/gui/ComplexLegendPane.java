package org.pathvisio.complexviz.gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Complex legend
 * @author anwesha
 */
public class ComplexLegendPane extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JPanel linkoutPane = new JPanel();
	private final JPanel legendPanel = new JPanel();
	
	public ComplexLegendPane() {
		FlowLayout layout = new FlowLayout();
		legendPanel.setLayout(layout);
//		addDefault();
		add(legendPanel);
		}

	public void addColours(boolean useGradient,boolean useRule){
		System.out.println("colors");
		final JPanel panel = new JPanel();
		final FlowLayout layout = new FlowLayout();
		panel.setLayout(layout);
		if(useGradient){
			panel.add(new JLabel("Gradient"));
		}
		if(useRule){
			panel.add(new JLabel("Color rule met"));
			panel.add(new JLabel("Color rule not met"));
		}
		legendPanel.add(panel);
		legendPanel.revalidate();
		legendPanel.repaint();
	}
	
	public void addBorders(Map<String, String> complexIdNameMap, Map<String, Color> complexIdBorderColorMap){
		System.out.println("borders");
		final JPanel panel = new JPanel();
		final FlowLayout layout = new FlowLayout();
		panel.setLayout(layout);		
		for (final String key : complexIdNameMap.keySet()) {
			final String complexname = complexIdNameMap.get(key);
			final JPanel subpanel = new JPanel();
			final GridLayout sublayout = new GridLayout(0,2);
			subpanel.setLayout(sublayout);
			JLabel complexlbl = new JLabel(complexname);
			JButton clrbtn = new JButton("..");
			clrbtn.setForeground(complexIdBorderColorMap.get(key));
			clrbtn.setBackground(complexIdBorderColorMap.get(key));
			clrbtn.setName(complexname);
			clrbtn.setOpaque(true);
			clrbtn.setEnabled(false);
			subpanel.add(complexlbl);
			subpanel.add(clrbtn);
			panel.add(subpanel);
		}
		legendPanel.add(panel);
		legendPanel.revalidate();
		legendPanel.repaint();
	}
	
	
	public void addBorderPanel(Map<String, String> complexIdNameMap, Map<String, Color> complexIdBorderColorMap){
		final JPanel panel = new JPanel();
		final FlowLayout layout = new FlowLayout();
		panel.setLayout(layout);		
		for (final String key : complexIdNameMap.keySet()) {
			final String complexname = complexIdNameMap.get(key);
			final JPanel subpanel = new JPanel();
			final GridLayout sublayout = new GridLayout(0,2);
			subpanel.setLayout(sublayout);
			JLabel complexlbl = new JLabel(complexname);
			JButton clrbtn = new JButton("..");
			clrbtn.setForeground(complexIdBorderColorMap.get(key));
			clrbtn.setBackground(complexIdBorderColorMap.get(key));
			clrbtn.setName(complexname);
			clrbtn.setOpaque(true);
			clrbtn.setEnabled(false);
			subpanel.add(complexlbl);
			subpanel.add(clrbtn);
			panel.add(subpanel);
		}
		legendPanel.add(panel);
		legendPanel.revalidate();
		legendPanel.repaint();
	}
	
	public void addDefault(){
		System.out.println("add");
		
		JPanel defaultPanel = new JPanel();
		defaultPanel.setLayout(new BoxLayout(defaultPanel, BoxLayout.X_AXIS));
		JLabel deflbl = new JLabel("Color rule/criteria not met");
		JButton defclr = new JButton("..");
		defclr.setOpaque(true);
		defclr.setBackground(Color.GRAY);
		defclr.setForeground(Color.GRAY);
		defclr.setEnabled(false);
		defaultPanel.add(defclr);
		defaultPanel.add(deflbl);
		legendPanel.add(defaultPanel);
		legendPanel.revalidate();
		legendPanel.repaint();
		}
	
	/**
	 * Updates the bottom part of the panel with information about the Xref
	 * selected in the top part of the panel
	 * 
	 */
	private void updateLegend(JPanel paneltoadd) {
		legendPanel.add(paneltoadd);
		legendPanel.revalidate();
		legendPanel.repaint();
	}
	
	}