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
								height;
	private JToggleButton ur, um, ul, mr, mm, ml, lr, lm, ll;
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
		JLabel lWidth = new JLabel("Width:");
		width = new JTextField();
		
		/* ORIENTATION PANEL */
		JPanel orient = new JPanel();
		ul = new JToggleButton("_ul");
		ul.addActionListener(this);
		um = new JToggleButton("_um");
		um.addActionListener(this);
		ur = new JToggleButton("_ur");
		ur.addActionListener(this);
		ml = new JToggleButton("_ml");
		ml.addActionListener(this);
		mm = new JToggleButton("_mm");
		mm.addActionListener(this);
		mr = new JToggleButton("_mr");
		mr.addActionListener(this);
		ll = new JToggleButton("_ll");
		ll.addActionListener(this);
		lm = new JToggleButton("_lm");
		lm.addActionListener(this);
		lr = new JToggleButton("_lr");
		lr.addActionListener(this);
		orient.setLayout(new GridLayout(3,3,0,0));
		orient.add(ul);
		orient.add(um);
		orient.add(ur);
		orient.add(ml);
		orient.add(mm);
		orient.add(mr);
		orient.add(ll);
		orient.add(lm);
		orient.add(lr);
		
		/* ORIGINAL SIZE PANEL */
		JPanel origin = new VerticalStaticJPanel();
		origin.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Original size"),
					BorderFactory.createEmptyBorder(0, 5, 5, 5)));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.VERTICAL;
		//c.insets = new Insets(5, 0, 0, 0);
		c.weightx = 1;
		c.weighty = 1;
		origin.add(new JLabel("Width: "), c);
		c.gridy = 1;
		origin.add(new JLabel("Height: "), c);
		c.gridx = 1; c.gridy = 0;
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
		mainPanel.add(sizing);
		mainPanel.add(orient);
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
		if(command.equals("_ul")) {
			anchor = ANCHOR_UPPERLEFT;
		} else if(command.equals("_um")) {
			anchor = ANCHOR_UPPERMID;
		} else if(command.equals("_ur")) {
			anchor = ANCHOR_UPPERRIGHT;
		} else if(command.equals("_mr")) {
			anchor = ANCHOR_MIDRIGHT;
		} else if(command.equals("_mm")) {
			anchor = ANCHOR_MIDMID;
		} else if(command.equals("_ml")) {
			anchor = ANCHOR_MIDLEFT;
		} else if(command.equals("_lr")) {
			anchor = ANCHOR_LOWERRIGHT;
		} else if(command.equals("_lm")) {
			anchor = ANCHOR_LOWERMID;
		} else if(command.equals("_ll")) {
			anchor = ANCHOR_LOWERLEFT;
		} else if(command.equalsIgnoreCase("resize")) {
			
			try {
				int nwidth = Integer.parseInt(width.getText());
				int nheight = Integer.parseInt(height.getText());
				int x=0, y=0;
				switch(anchor) {
					
				}
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
		
		ul.setSelected(anchor == ANCHOR_UPPERLEFT);
		um.setSelected(anchor == ANCHOR_UPPERMID);
		ur.setSelected(anchor == ANCHOR_UPPERRIGHT);
		ml.setSelected(anchor == ANCHOR_MIDLEFT);
		mm.setSelected(anchor == ANCHOR_MIDMID);
		mr.setSelected(anchor == ANCHOR_MIDRIGHT);
		ll.setSelected(anchor == ANCHOR_LOWERLEFT);
		lm.setSelected(anchor == ANCHOR_LOWERMID);
		lr.setSelected(anchor == ANCHOR_LOWERRIGHT);
	}

}
