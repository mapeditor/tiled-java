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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tiled.core.*;


public class TilePaletteDialog extends JDialog implements ActionListener,
    MouseListener, ListSelectionListener
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
        pc.setTileset(tilesets);
        sets.setListData(tilesets);
    }

    private void init() {
        sets = new JList();
        //TODO: the full functionality for multiple sets is not yet available.
        //sets.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        sets.addListSelectionListener(this);

        pc = new TilePalettePanel();
        pc.addMouseListener(this);
        JScrollPane paletteScrollPane = new JScrollPane(pc,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 2;
        c.weighty = 1;
        c.gridx = 1;
        c.gridy = 1;
        getContentPane().add(paletteScrollPane, c);
        c.weightx = 1;
        c.gridx = 3;
        getContentPane().add(sets, c);
    }

    public void actionPerformed(ActionEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        currentTile = pc.getTileAtPoint(e.getX(), e.getY());
        if (currentTile != null) {
            editor.setCurrentTile(currentTile);
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void valueChanged(ListSelectionEvent e) {
        Vector add = new Vector();
        Object[] setlist = sets.getSelectedValues();
        for (int i = 0; i < setlist.length; i++) {
            add.add(setlist[i]);
        }
        pc.setTileset(add);
    }
}
