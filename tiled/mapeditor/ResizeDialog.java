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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;
import javax.swing.*;

import tiled.core.Map;

public class ResizeDialog extends JDialog implements ActionListener {

	private Map currentMap;

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
		
		/* ORIENTATION PANEL */
		JPanel orient = new JPanel();
		JToggleButton ul = new JToggleButton("_ul");
		ul.addActionListener(this);
		JToggleButton uml = new JToggleButton("_uml");
		uml.addActionListener(this);
		JToggleButton ur = new JToggleButton("_ur");
		ur.addActionListener(this);
		JToggleButton ml = new JToggleButton("_ml");
		ml.addActionListener(this);
		JToggleButton mml = new JToggleButton("_mml");
		mml.addActionListener(this);
		JToggleButton mr = new JToggleButton("_mr");
		mr.addActionListener(this);
		JToggleButton ll = new JToggleButton("_ll");
		ll.addActionListener(this);
		JToggleButton lml = new JToggleButton("_lml");
		lml.addActionListener(this);
		JToggleButton lr = new JToggleButton("_lr");
		lr.addActionListener(this);
		orient.setLayout(new GridLayout(3,3,0,0));
		orient.add(ul);
		orient.add(uml);
		orient.add(ur);
		orient.add(ml);
		orient.add(mml);
		orient.add(mr);
		orient.add(ll);
		orient.add(lml);
		orient.add(lr);
		
		/* BUTTONS PANEL */
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

	}

}
