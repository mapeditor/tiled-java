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

import java.awt.event.*;
import java.awt.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tiled.core.*;
import tiled.mapeditor.util.*;
import tiled.mapeditor.widget.*;


public class TilePaletteDialog extends JDialog implements ActionListener,
    TileSelectionListener, ListSelectionListener
{
    private MapEditor editor;
    private Map currentMap;
    private TilePalettePanel pc;
    private JList sets;
    private Tile currentTile;

    public TilePaletteDialog(MapEditor editor, Map map) {
        super(editor.getAppFrame(), "Palette", false);
        this.editor = editor;
        init();
        setMap(map);
        setSize(new Dimension(300, 200));
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    public void setMap(Map map) {
        Vector tilesets = new Vector();
        currentMap = map;
        if (currentMap != null) {
            tilesets = currentMap.getTilesets();
        }
        pc.setTilesets(tilesets);
        sets.setListData(tilesets);
    }

    private void init() {
        sets = new JList();
        //TODO: the full functionality for multiple sets is not yet available.
        //sets.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        sets.addListSelectionListener(this);
        JScrollPane setsSp = new JScrollPane(sets);

        pc = new TilePalettePanel();
        pc.addTileSelectionListener(this);
        JScrollPane paletteScrollPane = new JScrollPane(pc,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, true);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        splitPane.setResizeWeight(0.75);
        splitPane.setLeftComponent(paletteScrollPane);
        splitPane.setRightComponent(setsSp);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1; c.weighty = 1;
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(splitPane, c);

        getContentPane().add(mainPanel);
    }

    public void actionPerformed(ActionEvent e) {
    }

    public void tileSelected(TileSelectionEvent event) {
        Tile tile = event.getTile();
        if (tile != null) {
            editor.setCurrentTile(tile);
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        Vector add = new Vector();
        Object[] setlist = sets.getSelectedValues();
        for (int i = 0; i < setlist.length; i++) {
            add.add(setlist[i]);
        }
        pc.setTilesets(add);
    }
}
