/*
 *  Tiled Map Editor, (c) 2004-2006
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 * 
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <b.lindeijer@xs4all.nl>
 */

package tiled.mapeditor.dialogs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Properties;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tiled.mapeditor.Resources;
import tiled.mapeditor.util.PropertiesTableModel;
import tiled.mapeditor.widget.VerticalStaticJPanel;

public class PropertiesDialog extends JDialog implements ListSelectionListener
{
    private JTable tProperties;
    private JButton okButton, cancelButton, deleteButton;
    private final Properties properties;
    private final PropertiesTableModel tableModel = new PropertiesTableModel();

    private static final String DIALOG_TITLE = Resources.getString("dialog.properties.title");
    private static final String OK_BUTTON = Resources.getString("general.button.ok");
    private static final String DELETE_BUTTON = Resources.getString("general.button.delete");
    private static final String CANCEL_BUTTON = Resources.getString("general.button.cancel");
    

    public PropertiesDialog(JFrame parent, Properties p) {
        super(parent, DIALOG_TITLE, true);
        properties = p;
        init();
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void init() {
        tProperties = new JTable(tableModel);
        tProperties.getSelectionModel().addListSelectionListener(this);
        JScrollPane propScrollPane = new JScrollPane(tProperties);
        propScrollPane.setPreferredSize(new Dimension(200, 150));

        okButton = new JButton(OK_BUTTON);
        cancelButton = new JButton(CANCEL_BUTTON);
        deleteButton = new JButton(Resources.getIcon("gnome-delete.png"));
        deleteButton.setToolTipText(DELETE_BUTTON);
        
        JPanel user = new VerticalStaticJPanel();
        user.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        user.setLayout(new BoxLayout(user, BoxLayout.X_AXIS));
        user.add(Box.createGlue());
        user.add(Box.createRigidArea(new Dimension(5, 0)));
        user.add(deleteButton);
        
        JPanel buttons = new VerticalStaticJPanel();
        buttons.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(Box.createGlue());
        buttons.add(okButton);
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(cancelButton);

        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(propScrollPane);
        mainPanel.add(user);
        mainPanel.add(buttons);

        getContentPane().add(mainPanel);
        getRootPane().setDefaultButton(okButton);
        
        //create actionlisteners
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
            	buildPropertiesAndDispose();
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                dispose();
            }
        });
        
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                deleteSelected();
            }
        });
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

    private void buildPropertiesAndDispose() {
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
    }
    
    private void deleteSelected() {
    	int total = tProperties.getSelectedRowCount();
        Object[] keys = new Object[total];
        int[] selRows = tProperties.getSelectedRows();
        
        for(int i = 0; i < total; i++) {
            keys[i] = tProperties.getValueAt(selRows[i], 0);
        }
        
        for (int i = 0; i < total; i++) {
            if (keys[i] != null) {
                tableModel.remove(keys[i]);
            }
        }
    }
    
    public void valueChanged(ListSelectionEvent e) {
    }
}
