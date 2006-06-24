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
 *
 *  This class is based on TilesetChooserTabbedPane from Stendhal Map Editor
 *  by Matthias Totz <mtotz@users.sourceforge.net>
 */

package tiled.mapeditor.widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import tiled.core.Map;
import tiled.core.TileSet;
import tiled.mapeditor.MapEditor;
import tiled.mapeditor.util.TileSelectionEvent;
import tiled.mapeditor.util.TileSelectionListener;

/**
 * Shows one tab for each Tileset.
 *
 * todo: Since the width of this widget is no longer arbitrary but related
 * todo: to the width of the map editor, there should be an option to set
 * todo: the number of tiles in a row equal to what it was on the tileset
 * todo: image.
 *
 * @version $Id$
 */
public class TabbedTilesetsPane extends JTabbedPane implements TileSelectionListener
{
    /**
     * List of the tile palette panels (one for each tileset).
     */
    private final List tilePanels = new ArrayList();
    private final MapEditor mapEditor;

    /**
     * Constructor.
     */
    public TabbedTilesetsPane(MapEditor mapEditor) {
        this.mapEditor = mapEditor;
    }

    /**
     * Sets the tiles panes to the the ones from this map.
     */
    public void setMap(Map currentMap) {
        if (currentMap == null) {
            removeAll();
        } else {
            recreateTabs(currentMap.getTilesets());
        }
    }

    /**
     * Creates the panels for the tilesets.
     */
    private void recreateTabs(List tilesets) {
        // Stop listening to the tile palette panels
        for (Iterator it = tilePanels.iterator(); it.hasNext();) {
            TilePalettePanel panel = (TilePalettePanel) it.next();
            panel.removeTileSelectionListener(this);
        }
        tilePanels.clear();

        // Remove all tabs
        removeAll();

        if (tilesets != null) {
            // Add a new tab for each tileset of the map
            for (Iterator it = tilesets.iterator(); it.hasNext();)
            {
                TileSet tileset = (TileSet) it.next();
                if (tileset != null) {
                    TilePalettePanel tilePanel = new TilePalettePanel();
                    tilePanel.setTileset(tileset);
                    tilePanel.addTileSelectionListener(this);
                    JScrollPane paletteScrollPane = new JScrollPane(tilePanel,
                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                    addTab(tileset.getName(), paletteScrollPane);
                }
            }
        }
    }

    /**
     * Informs the editor of the new tile.
     */
    public void tileSelected(TileSelectionEvent e) {
        mapEditor.setCurrentTile(e.getTile());
    }
}
