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

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tiled.core.*;


public class PropertiesDialog extends JDialog implements ActionListener, ListSelectionListener
{
    private Map currentMap;
	private JTable mapProperties;
	
    public PropertiesDialog(MapEditor m) {
        currentMap = m.getCurrentMap();
        setSize(150,200);
		setLocationRelativeTo(getOwner());
        setTitle("Map Properties");
        setModal(true);
    }

    private void init() {
		mapProperties = new JTable(new PropertiesTableModel());
		mapProperties.getSelectionModel().addListSelectionListener(this);
		JScrollPane propScrollPane = new JScrollPane(mapProperties);
		propScrollPane.setPreferredSize(new Dimension(150, 150));
		
		pack();
    }


    public void getProps() {
        init();
        show();
    }

    public void actionPerformed(ActionEvent e) {
    }

	public void valueChanged(ListSelectionEvent e) {
		
	}
}
