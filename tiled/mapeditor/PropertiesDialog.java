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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tiled.mapeditor.util.*;
import tiled.mapeditor.widget.*;

public class PropertiesDialog extends JDialog implements ActionListener,
	ListSelectionListener
 {
	private JTable mapProperties;
	private JButton bOk, bCancel;
	private JButton bDel, bAdd;
	private Properties properties;
	
	public PropertiesDialog(JFrame parent, Properties p) {
		super(parent, "Properties", true);
		properties = p;
		init();
		pack();
		setLocationRelativeTo(getOwner());
	}

	private void init() {
		mapProperties = new JTable(new PropertiesTableModel());
		mapProperties.getSelectionModel().addListSelectionListener(this);
		JScrollPane propScrollPane = new JScrollPane(mapProperties);
		propScrollPane.setPreferredSize(new Dimension(150, 150));

		bOk = new JButton("OK");
		bCancel = new JButton("Cancel");

		try {
			bAdd = new JButton(new ImageIcon(MapEditor.loadImageResource("resources/gnome-new.png")));
			bDel = new JButton(new ImageIcon(MapEditor.loadImageResource("resources/gnome-delete.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		bOk.addActionListener(this);
		bCancel.addActionListener(this);
		bAdd.addActionListener(this);
		bDel.addActionListener(this);
		
		JPanel buttons = new VerticalStaticJPanel();
		buttons.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		buttons.add(Box.createGlue());
		buttons.add(bOk);
		buttons.add(Box.createRigidArea(new Dimension(5, 0)));
		buttons.add(bCancel);

		JPanel user = new VerticalStaticJPanel();
		user.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		user.setLayout(new BoxLayout(user, BoxLayout.X_AXIS));
		user.add(Box.createGlue());
		user.add(bAdd);
		user.add(Box.createRigidArea(new Dimension(5, 0)));
		user.add(bDel);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(propScrollPane);
		mainPanel.add(user);
		mainPanel.add(buttons);

		getContentPane().add(mainPanel);
		getRootPane().setDefaultButton(bOk);

	}

	private void updateInfo() {
		mapProperties.removeAll();

		Enumeration keys = properties.keys();
		Properties props = new Properties();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement(); 
			props.put(key, properties.getProperty(key));
		}
		((PropertiesTableModel)mapProperties.getModel()).update(props);
		mapProperties.repaint();
	}

	public void getProps() {
		updateInfo();
		setVisible(true);
	}

	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();

		if (source == bOk) {
			properties.clear();

			Properties newProps = ((PropertiesTableModel)mapProperties.getModel()).getProperties();
			Enumeration keys = newProps.keys();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement(); 
				properties.put(key, newProps.getProperty(key));
			}

			dispose();
		} else if (source == bCancel) {
			dispose();
		} else if (source == bDel) {
			((PropertiesTableModel)mapProperties.getModel()).remove(mapProperties.getSelectedRow());
			repaint();
		}
	}

	public void valueChanged(ListSelectionEvent e) {
	}
}
