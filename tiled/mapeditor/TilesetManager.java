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
import java.util.ListIterator;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tiled.core.*;
import tiled.io.xml.XMLMapWriter;
import tiled.mapeditor.util.TiledFileFilter;


public class TilesetManager extends JDialog implements ActionListener,
       ListSelectionListener
{
    private MapEditor editor;
    private Map map;
    private Vector tileSets;

    private JButton exportButton, saveButton;
    private JButton removeButton, editButton, closeButton;
    private JTable tilesetTable;

    public TilesetManager(MapEditor editor, Map map) {
        super(editor.getAppFrame(), "Tileset Manager", true);
        this.editor = editor;
        this.map = map;
        init();
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void init() {
        // Create the tileset table
        tilesetTable = new JTable(new TilesetTableModel(map));
        tilesetTable.getSelectionModel().addListSelectionListener(this);
        JScrollPane tilesetScrollPane = new JScrollPane(tilesetTable);
        tilesetScrollPane.setPreferredSize(new Dimension(360, 150));

        // Create the buttons
        saveButton = new JButton("Save");
        editButton = new JButton("Edit...");
        exportButton = new JButton("Export...");
        removeButton = new JButton("Remove");
        closeButton = new JButton("Close");

        exportButton.addActionListener(this);
        saveButton.addActionListener(this);
        removeButton.addActionListener(this);
        editButton.addActionListener(this);
        closeButton.addActionListener(this);

        // Create the main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridy = 0;
        c.gridwidth = 6;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        mainPanel.add(tilesetScrollPane, c);
        c.insets = new Insets(5, 0, 0, 5);
        c.gridy = 1;
        c.weighty = 0;
        c.weightx = 0;
        c.gridwidth = 1;
        mainPanel.add(exportButton, c);
        mainPanel.add(removeButton, c);
        mainPanel.add(editButton, c);
        mainPanel.add(saveButton, c);
        c.weightx = 1;
        mainPanel.add(Box.createGlue(), c);
        c.weightx = 0;
        c.insets = new Insets(5, 0, 0, 0);
        mainPanel.add(closeButton, c);

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


        if (command.equals("Close")) {
            dispose();
        } else if (command.equals("Edit...")) {
            if (map != null && selectedRow >= 0) {
                try {
                    TileDialog tileDialog = new TileDialog(this, set);
                    tileDialog.show();
                } catch (ArrayIndexOutOfBoundsException a) {
                }
            }
        } else if (command.equals("Remove")) {
            try {
                if (checkSetUsage(set) > 0) {
                    int ret = JOptionPane.showConfirmDialog(this,
                            "This tileset is currently in use. " +
                            "Are you sure you wish to remove it?",
                            "Sure?", JOptionPane.YES_NO_CANCEL_OPTION);
                    if (ret == JOptionPane.YES_OPTION) {
                        map.removeTileset(set);
                        updateTilesetTable();
                    }
                } else {
                    map.removeTileset(set);
                    updateTilesetTable();
                }
            } catch (ArrayIndexOutOfBoundsException a) {
            }
        } else if (command.equals("Export...")) {
            JFileChooser ch = new JFileChooser(map.getFilename());

			ch.setFileFilter(new TiledFileFilter(TiledFileFilter.FILTER_TSX));
            int ret = ch.showSaveDialog(this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                String filename = ch.getSelectedFile().getAbsolutePath();
                try {
                    XMLMapWriter mw = new XMLMapWriter();
                    mw.writeTileset(set,filename);
                    set.setSource(filename);
                    exportButton.setEnabled(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (command.equals("Save")) {
			JFileChooser ch = new JFileChooser(map.getFilename());
	
			//TODO: warn vehemently if the set file already exists
			ch.setFileFilter(new TiledFileFilter(TiledFileFilter.FILTER_TSX));
			int ret = ch.showSaveDialog(this);
			if (ret == JFileChooser.APPROVE_OPTION) {
				String filename = ch.getSelectedFile().getAbsolutePath();
				try {
					XMLMapWriter mw = new XMLMapWriter();
					mw.writeTileset(set,filename);
					set.setSource(filename);
					exportButton.setEnabled(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
            System.out.println("Unimplemented command: " + command);
        }
    }

    private void updateTilesetTable() {
        ((TilesetTableModel)tilesetTable.getModel()).setMap(map);
        tilesetTable.repaint();
    }

    private int checkSetUsage(TileSet s) {
        int used = 0;

        for (int i = 0; i < s.getTotalTiles(); i++) {
            ListIterator itr = map.getLayers();
            while (itr.hasNext()) {
                MapLayer ml = (MapLayer)itr.next();
                if (ml.isUsed(s.getTile(i))) {
                    used++;
                    break;
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
        Vector tilesets = map.getTilesets();
        TileSet set = null;
        try {
            set = (TileSet)tilesets.get(selectedRow);
        } catch (IndexOutOfBoundsException e) {
        }

        exportButton.setEnabled(set != null && set.getSource() == null);
        editButton.setEnabled(set != null);
        removeButton.setEnabled(set != null);
        saveButton.setEnabled(set != null && set.getSource() != null);
    }
}
