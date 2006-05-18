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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Vector;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tiled.core.Map;
import tiled.core.TileSet;
import tiled.core.TileSet;
import tiled.mapeditor.MapEditor;
import tiled.mapeditor.Resources;
import tiled.mapeditor.util.TileSelectionEvent;
import tiled.mapeditor.util.TileSelectionListener;
import tiled.mapeditor.widget.TilePalettePanel;
import tiled.util.TiledConfiguration;

/**
 * @version $Id$
 */
public class TilePaletteDialog extends JDialog implements
        TileSelectionListener, ListSelectionListener
{
    private final MapEditor editor;
    private TilePalettePanel pc;
    private JList sets;
    private JSplitPane splitPane;

    private static final String DIALOG_TITLE =
            Resources.getString("dialog.tilepalette.title");
    private static final Preferences prefs =
            TiledConfiguration.node("dialog/tilepalette");

    public TilePaletteDialog(MapEditor editor, Map map) {
        super(editor.getAppFrame(), DIALOG_TITLE, false);
        this.editor = editor;
        init();
        setMap(map);
        setSize(new Dimension(prefs.getInt("width", 300),
                              prefs.getInt("height", 200)));
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    private void init() {
        sets = new JList();
        sets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sets.addListSelectionListener(this);

        pc = new TilePalettePanel();
        pc.addTileSelectionListener(this);

        JScrollPane setsScrollPane = new JScrollPane(sets);
        JScrollPane paletteScrollPane = new JScrollPane(pc,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
        splitPane.setBorder(null);
        splitPane.setResizeWeight(0.75);
        splitPane.setLeftComponent(paletteScrollPane);
        splitPane.setRightComponent(setsScrollPane);

        int dividerLocation = prefs.getInt("divider", -1);
        if (dividerLocation >= 0) {
            splitPane.setDividerLocation(dividerLocation);
        }

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1; c.weighty = 1;
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(splitPane, c);

        setContentPane(mainPanel);
    }

    /**
     * Sets the map of which the tilesets should be displayed.
     * @param map
     */
    public void setMap(Map map) {
        if (map != null) {
            Vector tilesets = map.getTilesets();

            // A bit of juggling to maintain the selected index when possible
            int prevIndex = sets.getSelectedIndex();
            sets.setListData(tilesets);
            sets.setSelectedIndex(prevIndex);
            if (sets.getSelectedIndex() == -1) {
                sets.setSelectedIndex(0);
            }
            pc.setTileset((TileSet) sets.getSelectedValue());
        }
    }

    public void tileSelected(TileSelectionEvent event) {
        editor.setCurrentTile(event.getTile());
    }

    public void valueChanged(ListSelectionEvent e) {
        pc.setTileset((TileSet) sets.getSelectedValue());
    }

    public void shutdown() {
        Dimension size = getSize();
        prefs.putInt("width", size.width);
        prefs.putInt("height", size.height);
        prefs.putInt("divider", splitPane.getDividerLocation());
    }
}
