/*
 *  Tiled Map Editor, (c) 2004
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 * 
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <b.lindeijer@xs4all.nl>
 */

package tiled.mapeditor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import tiled.core.Map;
import tiled.mapeditor.util.ResizePanel;

public class ResizeDialog extends JDialog implements ActionListener {


	public static final int ANCHOR_UPPERLEFT		= 1;
	public static final int ANCHOR_UPPERMID			= 2;
	public static final int ANCHOR_UPPERRIGHT		= 3;
	public static final int ANCHOR_MIDLEFT				= 4;
	public static final int ANCHOR_MIDMID				= 5;
	public static final int ANCHOR_MIDRIGHT			= 6;
	public static final int ANCHOR_LOWERLEFT		= 7;
	public static final int ANCHOR_LOWERMID			= 8;
	public static final int ANCHOR_LOWERRIGHT		= 9;
	
	private Map currentMap;
	private JTextField width,
						height,
						offsetX,
						offsetY;	
	private int anchor;
	
	public ResizeDialog(MapEditor m) {
		currentMap = m.getCurrentMap();
		setTitle("Resize Map");
		setLocationRelativeTo(getOwner());
		setModal(true);
	}

	private void init() {
		JButton bResize = new JButton("Resize"), bCancel = new JButton("Cancel");
		
		/* SIZING PANEL */
		JPanel sizing = new JPanel();
		JPanel outerSizing = new JPanel();
		width = new JTextField("" + currentMap.getWidth(), 5);
		height = new JTextField("" + currentMap.getHeight(), 5);
		offsetX = new JTextField("0", 5);
		offsetY = new JTextField("0", 5);
		outerSizing.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("New size"),
					BorderFactory.createEmptyBorder(0, 5, 5, 5)));
		outerSizing.setLayout(new BoxLayout(outerSizing, BoxLayout.PAGE_AXIS));
		sizing.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.VERTICAL;
		//c.insets = new Insets(5, 0, 0, 0);
		c.weightx = 1;
		c.weighty = 1;
		sizing.add(new JLabel("Width: "), c);
		c.gridy = 1;
		sizing.add(new JLabel("Height: "), c);
		c.gridx = 1; c.gridy = 0;
		c.weightx = 3;
		sizing.add(width, c);
		c.gridy = 1;
		sizing.add(height, c);
		c.gridx = 0; c.gridy = 3;
		sizing.add(new JLabel("X: "), c);
		c.gridy = 4;
		sizing.add(new JLabel("Y: "), c);
		c.gridx = 1; c.gridy = 3;
		c.weightx = 3;
		sizing.add(offsetX, c);
		c.gridy = 4;
		sizing.add(offsetY, c);
		c.gridx = 0; c.gridy = 5;
		
		/* ORIENTATION PANEL */
		ResizePanel orient = new ResizePanel(new Dimension(100,100),100);
		outerSizing.add(sizing);
		outerSizing.add(Box.createRigidArea(new Dimension(0,5)));
		outerSizing.add(orient);
		
		
		/* ORIGINAL SIZE PANEL */
		JPanel origin = new VerticalStaticJPanel();
		origin.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Original size"),
					BorderFactory.createEmptyBorder(0, 5, 5, 5)));
		origin.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.VERTICAL;
		//c.insets = new Insets(5, 0, 0, 0);
		c.weightx = 1;
		c.weighty = 1;
		origin.add(new JLabel("Width: "), c);
		c.gridy = 1;
		origin.add(new JLabel("Height: "), c);
		c.gridx = 1; c.gridy = 0;
		c.weightx = 3;
		origin.add(new JLabel("" + currentMap.getWidth()), c);
		c.gridy = 1;
		origin.add(new JLabel("" + currentMap.getHeight()), c);
		
		/* BUTTONS PANEL */
		bResize.addActionListener(this);
		bCancel.addActionListener(this);
		JPanel buttons = new VerticalStaticJPanel();
		buttons.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		buttons.add(Box.createGlue());
		buttons.add(bResize);
		buttons.add(Box.createRigidArea(new Dimension(5, 0)));
		buttons.add(bCancel);
				
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		mainPanel.add(origin);
		mainPanel.add(outerSizing);
		//mainPanel.add(orient);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		mainPanel.add(buttons);
		
		getContentPane().add(mainPanel);
		
		pack();
	}

	public void showDialog() {
		init();
		show();
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if(command.equalsIgnoreCase("resize")) {
			
			try {
				int nwidth = Integer.parseInt(width.getText());
				int nheight = Integer.parseInt(height.getText());
				int x=-Integer.parseInt(offsetX.getText());        //math works out in MapLayer#resize
				int y=-Integer.parseInt(offsetY.getText());
				currentMap.resize(nwidth,nheight,x,y);
			} catch (NumberFormatException nfe) {
				JOptionPane.showMessageDialog(this,"One of your dimensions is not a number", "Argh!",JOptionPane.ERROR_MESSAGE,null);
			}
			dispose();
		} else if(command.equalsIgnoreCase("cancel")) {
			dispose();
		} else {
			System.out.println(command);
		}
	}

}
