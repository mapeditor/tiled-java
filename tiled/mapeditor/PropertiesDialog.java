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
    private Properties properties;
    private PropertiesTableModel tableModel;

    public PropertiesDialog(JFrame parent, Properties p) {
        super(parent, "Properties", true);
        properties = p;
        init();
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void init() {
        tableModel = new PropertiesTableModel();
        mapProperties = new JTable(tableModel);
        mapProperties.getSelectionModel().addListSelectionListener(this);
        JScrollPane propScrollPane = new JScrollPane(mapProperties);
        propScrollPane.setPreferredSize(new Dimension(200, 150));

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
        // Make a copy of the properties that will be changed by the
        // properties table model.
        Properties props = new Properties();
        Enumeration keys = properties.keys();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement(); 
            props.put(key, properties.getProperty(key));
        }
        tableModel.update(props);
    }

    public void getProps() {
        updateInfo();
        setVisible(true);
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();

        if (source == bOk) {
            // Copy over the new set of properties from the properties table
            // model.
            properties.clear();

            Properties newProps = tableModel.getProperties();
            Enumeration keys = newProps.keys();
            while (keys.hasMoreElements()) {
                String key = (String)keys.nextElement(); 
                properties.put(key, newProps.getProperty(key));
            }

            dispose();
        } else if (source == bCancel) {
            dispose();
        }
    }

    public void valueChanged(ListSelectionEvent e) {
    }
}
