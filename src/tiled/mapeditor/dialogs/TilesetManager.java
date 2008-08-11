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
 *  Rainer Deyke <rainerd@eldwood.com>
 */

package tiled.mapeditor.dialogs;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tiled.core.*;
import tiled.io.MapHelper;
import tiled.io.MapWriter;
import tiled.mapeditor.Resources;
import tiled.mapeditor.plugin.PluginClassLoader;
import tiled.mapeditor.util.ConfirmingFileChooser;
import tiled.mapeditor.util.TiledFileFilter;
import tiled.mapeditor.util.TilesetTableModel;

/**
 * @version $Id$
 */
public class TilesetManager extends JDialog implements ActionListener,
       ListSelectionListener
{
    private final Map map;

    private JButton saveButton, saveAsButton, embedButton, removeButton, editButton;
    private JButton moveUpButton, moveDownButton, closeButton;
    private JTable tilesetTable;

    private static final String DIALOG_TITLE = Resources.getString("dialog.tilesetmanager.title");
    private static final String CLOSE_BUTTON = Resources.getString("general.button.close");
    private static final String MOVE_UP_BUTTON = Resources.getString("dialog.tilesetmanager.button.moveup");
    private static final String MOVE_DOWN_BUTTON = Resources.getString("dialog.tilesetmanager.button.movedown");
    private static final String REMOVE_BUTTON = Resources.getString("general.button.remove");
    private static final String EMBED_BUTTON = Resources.getString("dialog.tilesetmanager.embed.button");
    private static final String SAVE_AS_BUTTON = Resources.getString("action.map.saveas.name");
    private static final String EDIT_BUTTON = Resources.getString("dialog.tilesetmanager.edit.button");
    private static final String SAVE_BUTTON = Resources.getString("action.map.save.name");

    private static final Icon REMOVE_BUTTON_ICON = Resources.getIcon("gnome-delete.png");
    private static final Icon EMBED_BUTTON_ICON = Resources.getIcon("insert-object.png");
    private static final Icon SAVE_AS_BUTTON_ICON = Resources.getIcon("document-save-as.png");
    private static final Icon EDIT_BUTTON_ICON = Resources.getIcon("gtk-edit.png");
    private static final Icon SAVE_BUTTON_ICON = Resources.getIcon("document-save.png");

    public TilesetManager(JFrame parent, Map map) {
        super(parent, DIALOG_TITLE, true);
        this.map = map;
        init();
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void init() {
        // Create the tileset table
        tilesetTable = new JTable(new TilesetTableModel(map));
        tilesetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tilesetTable.getSelectionModel().addListSelectionListener(this);
        JScrollPane tilesetScrollPane = new JScrollPane(tilesetTable);
        tilesetScrollPane.setPreferredSize(new Dimension(360, 150));

        // Create the buttons
        saveButton = new JButton(SAVE_BUTTON_ICON);
        editButton = new JButton(EDIT_BUTTON_ICON);
        saveAsButton = new JButton(SAVE_AS_BUTTON_ICON);
        embedButton = new JButton(EMBED_BUTTON_ICON);
        removeButton = new JButton(REMOVE_BUTTON_ICON);
        moveUpButton = new JButton(MOVE_UP_BUTTON);
        moveDownButton = new JButton(MOVE_DOWN_BUTTON);
        closeButton = new JButton(CLOSE_BUTTON);

        saveButton.setActionCommand(SAVE_BUTTON);
        saveAsButton.setActionCommand(SAVE_AS_BUTTON);
        embedButton.setActionCommand(EMBED_BUTTON);
        removeButton.setActionCommand(REMOVE_BUTTON);
        editButton.setActionCommand(EDIT_BUTTON);

        saveButton.setMargin(new Insets(0, 0, 0, 0));
        saveAsButton.setMargin(new Insets(0, 0, 0, 0));
        embedButton.setMargin(new Insets(0, 0, 0, 0));
        removeButton.setMargin(new Insets(0, 0, 0, 0));
        editButton.setMargin(new Insets(0, 0, 0, 0));

        saveButton.setToolTipText(SAVE_BUTTON);
        saveAsButton.setToolTipText(SAVE_AS_BUTTON);
        embedButton.setToolTipText(EMBED_BUTTON);
        removeButton.setToolTipText(REMOVE_BUTTON);
        editButton.setToolTipText(EDIT_BUTTON);

        saveButton.addActionListener(this);
        saveAsButton.addActionListener(this);
        embedButton.addActionListener(this);
        removeButton.addActionListener(this);
        editButton.addActionListener(this);
        moveUpButton.addActionListener(this);
        moveDownButton.addActionListener(this);
        closeButton.addActionListener(this);

        // Create the main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridy = 0;
        c.gridwidth = 3;
        c.gridheight = 6;
        c.weightx = 1;
        c.weighty = 1;
        mainPanel.add(tilesetScrollPane, c);
        c.insets = new Insets(5, 5, 5, 5);
        c.weighty = 0;
        c.weightx = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        mainPanel.add(saveButton, c);
        c.gridy = 1;
        mainPanel.add(saveAsButton, c);
        c.gridy = 2;
        mainPanel.add(embedButton, c);
        c.gridy = 3;
        mainPanel.add(removeButton, c);
        c.gridy = 4;
        mainPanel.add(editButton, c);
        c.gridy = 5;
        c.weighty = 1;
        mainPanel.add(Box.createGlue(), c);
        c.gridy = 6;
        c.insets = new Insets(5, 0, 0, 5);
        c.weighty = 0;
        mainPanel.add(moveUpButton, c);
        mainPanel.add(moveDownButton, c);
        c.weightx = 1;
        c.insets = new Insets(5, 0, 0, 0);
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.SOUTHEAST;
        JPanel temp = new JPanel();
        temp.setLayout(new BoxLayout(temp, BoxLayout.LINE_AXIS));
        temp.add(Box.createGlue());
        temp.add(closeButton);
        mainPanel.add(temp, c);
        getContentPane().add(mainPanel);
        getRootPane().setDefaultButton(closeButton);

        tilesetTable.changeSelection(0, 0, false, false);
    }

    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        int selectedRow = tilesetTable.getSelectedRow();
        Vector tilesets = map.getTilesets();
        TileSet set = null;
        try {
            set = (TileSet)tilesets.get(selectedRow);
        } catch (IndexOutOfBoundsException e) {
        }


        if (command.equals(CLOSE_BUTTON)) {
            dispose();
        } else if (command.equals(EDIT_BUTTON)) {
            if (map != null && selectedRow >= 0) {
                TileDialog tileDialog = new TileDialog(this, set, map);
                tileDialog.setVisible(true);
            }
        } else if (command.equals(REMOVE_BUTTON)) {
            if (checkSetUsage(set) > 0) {
                int ret = JOptionPane.showConfirmDialog(this,
                        Resources.getString("action.tileset.remove.in-use.message"),
                        Resources.getString("action.tileset.remove.in-use.title"),
                        JOptionPane.YES_NO_OPTION);
                if (ret != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            try {
                map.removeTileset(set);
                updateTilesetTable();
            } catch (LayerLockedException e) {
                JOptionPane.showMessageDialog(this,
                        Resources.getString("action.tileset.remove.error.layer-locked.message"),
                        Resources.getString("action.tileset.remove.error.title"),
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (command.equals(SAVE_AS_BUTTON)) {
            JFileChooser ch = new ConfirmingFileChooser(map.getFilename());

            MapWriter[] writers = PluginClassLoader.getInstance().getWriters();
            for (int i = 0; i < writers.length; i++) {
                try {
                    ch.addChoosableFileFilter(new TiledFileFilter(
                                writers[i].getFilter(), writers[i].getName()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            ch.addChoosableFileFilter
              (new TiledFileFilter(TiledFileFilter.FILTER_TSX));
            int ret = ch.showSaveDialog(this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                String filename = ch.getSelectedFile().getAbsolutePath();
                try {
                    MapHelper.saveTileset(set, filename);
                    set.setSource(filename);
                    embedButton.setEnabled(true);
                    saveButton.setEnabled(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (command.equals(SAVE_BUTTON)) {
            try {
                MapHelper.saveTileset(set, set.getSource());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (command.equals(EMBED_BUTTON)) {
            set.setSource(null);
            embedButton.setEnabled(false);
            saveButton.setEnabled(false);
        } else if (command.equals(MOVE_UP_BUTTON)) {
            if (selectedRow > 0) {
		int newRow = selectedRow - 1;
                map.swapTileSets(selectedRow, newRow);
                tilesetTable.getSelectionModel().setSelectionInterval(newRow, newRow);
            }
        } else if (command.equals(MOVE_DOWN_BUTTON)) {
            if (selectedRow > -1 && selectedRow < tilesetTable.getRowCount() - 1) {
		int newRow = selectedRow + 1;
                map.swapTileSets(selectedRow, newRow);
                tilesetTable.getSelectionModel().setSelectionInterval(newRow, newRow);
            }
        }
    }

    private void updateTilesetTable() {
        ((TilesetTableModel)tilesetTable.getModel()).setMap(map);
        tilesetTable.repaint();
    }

    private int checkSetUsage(TileSet tileset) {
        int used = 0;
        Iterator tileIterator = tileset.iterator();

        while (tileIterator.hasNext()) {
            Tile tile = (Tile) tileIterator.next();
            Iterator layerIterator = map.getLayers();

            while (layerIterator.hasNext()) {
                MapLayer ml = (MapLayer) layerIterator.next();
                if (ml instanceof TileLayer) {
                    if (((TileLayer) ml).isUsed(tile)) {
                        used++;
                        break;
                    }
                }
            }
        }

        return used;
    }

    public void valueChanged(ListSelectionEvent event) {
        updateButtons();
    }

    private void updateButtons() {
        int selectedRow = tilesetTable.getSelectedRow();

        moveUpButton.setEnabled(selectedRow > 0);

        moveDownButton.setEnabled(selectedRow > -1 && selectedRow < tilesetTable.getRowCount() - 1);

        Vector tilesets = map.getTilesets();
        TileSet set = null;
        try {
            set = (TileSet)tilesets.get(selectedRow);
        } catch (IndexOutOfBoundsException e) {
        }

        editButton.setEnabled(set != null);
        removeButton.setEnabled(set != null);
        saveButton.setEnabled(set != null && set.getSource() != null);
        saveAsButton.setEnabled(set != null);
        embedButton.setEnabled(set != null && set.getSource() != null);
    }
}
