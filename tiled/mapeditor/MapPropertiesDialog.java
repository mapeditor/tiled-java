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
import tiled.mapeditor.util.*;
import tiled.mapeditor.widget.*;

public class MapPropertiesDialog extends JDialog implements ActionListener,
       ListSelectionListener
{
    private Map currentMap;
    private JTable mapProperties;
    private JButton bOk, bCancel;

    public MapPropertiesDialog(JFrame parent, MapEditor m) {
        super(parent, "Map Properties", true);
        currentMap = m.getCurrentMap();
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

        bOk.addActionListener(this);
        bCancel.addActionListener(this);

        JPanel buttons = new VerticalStaticJPanel();
        buttons.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(Box.createGlue());
        buttons.add(bOk);
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(bCancel);

        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(propScrollPane);
        mainPanel.add(buttons);

        getContentPane().add(mainPanel);
        getRootPane().setDefaultButton(bOk);

    }

    private void updateInfo() {
        mapProperties.removeAll();

        Enumeration keys = currentMap.getProperties();
        Properties props = new Properties();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement(); 
            props.put(key, currentMap.getPropertyValue(key));
        }
        ((PropertiesTableModel)mapProperties.getModel()).update(props);
        mapProperties.repaint();
    }

    public void getProps() {
        updateInfo();
        show();
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();

        if (source == bOk) {
            currentMap.setProperties(((PropertiesTableModel)mapProperties.getModel()).getProperties());
            dispose();
        } else if (source == bCancel) {
            dispose();
        }
    }

    public void valueChanged(ListSelectionEvent e) {
    }
}
