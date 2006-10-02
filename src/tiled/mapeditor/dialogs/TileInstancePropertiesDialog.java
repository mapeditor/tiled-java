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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Properties;
import javax.swing.*;
import javax.swing.table.TableCellEditor;

import tiled.core.MapLayer;
import tiled.core.Tile;
import tiled.core.TileLayer;
import tiled.mapeditor.MapEditor;
import tiled.mapeditor.Resources;
import tiled.mapeditor.selection.SelectionLayer;
import tiled.mapeditor.util.PropertiesTableModel;
import tiled.mapeditor.widget.VerticalStaticJPanel;

/**
 * @version $Id$
 */
public class TileInstancePropertiesDialog extends JDialog
{
    private JTable propertiesTable;
    private Properties properties = new Properties();
    private PropertiesTableModel tableModel = new PropertiesTableModel();

    private static final String DIALOG_TITLE = "Tile Properties"; // todo: Resource this
    private static final String APPLY_BUTTON = Resources.getString("general.button.apply");
    private static final String APPLY_TOOLTIP = "Apply properties to selected tiles"; // todo: Resource this
    private static final String DELETE_BUTTON = Resources.getString("general.button.delete");

    private final MapEditor editor;
    private LinkedList propertiesList = new LinkedList(); // Holds all currently selected Properties

    public TileInstancePropertiesDialog(MapEditor editor) {
        super(editor.getAppFrame(), DIALOG_TITLE, false);
        this.editor = editor;
        init();
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void init() {
        propertiesTable = new JTable(tableModel);
        JScrollPane propScrollPane = new JScrollPane(propertiesTable);
        propScrollPane.setPreferredSize(new Dimension(200, 150));

        JButton applyButton = new JButton(APPLY_BUTTON);
        applyButton.setToolTipText(APPLY_TOOLTIP);
        JButton deleteButton = new JButton(Resources.getIcon("gnome-delete.png"));
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
        buttons.add(applyButton);

        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(propScrollPane);
        mainPanel.add(user);
        mainPanel.add(buttons);

        getContentPane().add(mainPanel);
        getRootPane().setDefaultButton(applyButton);

        //create actionlisteners
        applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                buildPropertiesAndApply();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                deleteSelected();
            }
        });
    }

    public void setSelection(SelectionLayer selection) {
        // Start off fresh...
        properties.clear();
        propertiesList.clear();

        // Get all properties of all selected tiles...
        MapLayer ml = editor.getCurrentLayer();
        if (ml instanceof TileLayer) {
            TileLayer tl = (TileLayer) ml;
            Rectangle r = selection.getSelectedAreaBounds();
            int maxJ = (int) (r.getY() + r.getHeight());
            int maxI = (int) (r.getX() + r.getWidth());

            for (int j = (int) r.getY(); j < maxJ; j++) {
                for (int i = (int) r.getX(); i < maxI; i++) {
                    Tile t = selection.getTileAt(i, j);
                    if (t != null) {
                        Properties p = tl.getTileInstancePropertiesAt(i, j);
                        if (p != null) propertiesList.add(p);
                    }
                }
            }
        }

        if (!propertiesList.isEmpty()) {
            // Start with properties of first tile instance
            Properties p = (Properties) propertiesList.get(0);

            for (Enumeration e = p.keys(); e.hasMoreElements();) {
                String key = (String) e.nextElement();
                properties.put(key, p.getProperty(key));
            }

            for (int i = 1; i < propertiesList.size(); i++) {
                // Merge the other properties...
                p = (Properties) propertiesList.get(i);

                for (Enumeration e = properties.keys(); e.hasMoreElements();) {
                    // We only care for properties that are already "known"...

                    String key = (String) e.nextElement();
                    String val = properties.getProperty(key);
                    String mval = p.getProperty(key);

                    if (mval == null) {
                        properties.remove(key); // Drop non-common properties
                    } else if (!mval.equals(val)) {
                        properties.setProperty(key, "?"); // Hide non-common values
                    }
                }
            }
        }

        updateInfo(); // Refresh display
    }

    private void updateInfo() {
        tableModel.setProperties(properties);
    }

    public void getProps() {
        updateInfo();
        setVisible(true);
    }

    private void buildPropertiesAndApply() {
        // Make sure there is no active cell editor anymore
        TableCellEditor cellEditor = propertiesTable.getCellEditor();
        if (cellEditor != null) {
            cellEditor.stopCellEditing();
        }

        // Apply possibly changed properties.
        properties.clear();
        properties.putAll(tableModel.getProperties());

        applyPropertiesToTiles();
    }


    private void deleteFromSelectedTiles(String key) {
        for (int i = 0; i < propertiesList.size(); i++) {
            Properties p = (Properties) propertiesList.get(i);
            p.remove(key);
        }
    }

    private void deleteSelected() {
        int total = propertiesTable.getSelectedRowCount();
        Object[] keys = new Object[total];
        int[] selRows = propertiesTable.getSelectedRows();

        for (int i = 0; i < total; i++) {
            keys[i] = propertiesTable.getValueAt(selRows[i], 0);
        }

        for (int i = 0; i < total; i++) {
            if (keys[i] != null) {
                tableModel.remove(keys[i]);
                deleteFromSelectedTiles((String) keys[i]);
            }
        }
    }

    private void applyPropertiesToTiles() {
        for (int i = 0; i < propertiesList.size(); i++) {
            Properties tp = (Properties) propertiesList.get(i);

            for (Enumeration e = properties.keys();
                 e.hasMoreElements();) {

                String key = (String) e.nextElement();
                String val = properties.getProperty(key);
                if (!val.equals("?")) {
                    tp.setProperty(key, val);
                }
            }
        }
    }
}
