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
import java.util.Enumeration;
import java.util.Properties;
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
        //pack();
        setLocationRelativeTo(getOwner());
        setTitle("Map Properties");
        setModal(true);
    }

    private void init() {
        JPanel buttonPanel = new JPanel();
        mapProperties = new JTable(new PropertiesTableModel());
        mapProperties.getSelectionModel().addListSelectionListener(this);
        JScrollPane propScrollPane = new JScrollPane(mapProperties);
        propScrollPane.setPreferredSize(new Dimension(150, 150));

        JButton bOk = new JButton("OK");
        bOk.addActionListener(this);

        JButton bCancel = new JButton("Cancel");
        bCancel.addActionListener(this);

        buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.X_AXIS));
        buttonPanel.add(bOk);
        buttonPanel.add(bCancel);

        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));

        getContentPane().add(propScrollPane);
        getContentPane().add(buttonPanel);

        updateInfo();

        pack();
    }

    private void updateInfo() {
        mapProperties.removeAll();

        Enumeration keys = currentMap.getProperties();
        Properties props = new Properties();
        while(keys.hasMoreElements()) {
            String key = (String) keys.nextElement(); 
            props.put(key, currentMap.getPropertyValue(key));
        }
        ((PropertiesTableModel)mapProperties.getModel()).update(props);
        mapProperties.repaint();
    }

    public void getProps() {
        init();
        show();
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equalsIgnoreCase("ok")) {
            currentMap.setProperties(((PropertiesTableModel)mapProperties.getModel()).getProperties());
            dispose();
        } else if (e.getActionCommand().equalsIgnoreCase("cancel")) {
            dispose();
        }
    }

    public void valueChanged(ListSelectionEvent e) {
    }
}
