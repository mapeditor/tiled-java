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
import tiled.mapeditor.brush.CustomBrush;
import tiled.mapeditor.util.*;

/**
 * Shows one tab for each Tileset.
 *
 * @version $Id$
 */
public class TabbedTilesetsPane extends JTabbedPane implements TileSelectionListener
{
    /**
     * List of the tile palette panels (one for each tileset).
     */
    private final List tilePanels = new ArrayList();
    private final MapChangeListener listener = new MyMapChangeListener();
    private final MapEditor mapEditor;
    private Map map;

    /**
     * Constructor.
     */
    public TabbedTilesetsPane(MapEditor mapEditor) {
        this.mapEditor = mapEditor;
    }

    /**
     * Sets the tiles panes to the the ones from this map.
     */
    public void setMap(Map map) {
        if (this.map != null) {
            this.map.removeMapChangeListener(listener);
        }

        if (map == null) {
            removeAll();
        } else {
            recreateTabs(map.getTilesets());
            map.addMapChangeListener(listener);
        }

        this.map = map;
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
                    addTabForTileset(tileset);
                }
            }
        }
    }

    /**
     * Adds a tab with a {@link TilePalettePanel} for the given tileset.
     *
     * @param tileset the given tileset
     */
    private void addTabForTileset(TileSet tileset) {
        TilePalettePanel tilePanel = new TilePalettePanel();
        tilePanel.setTileset(tileset);
        tilePanel.addTileSelectionListener(this);
        JScrollPane paletteScrollPane = new JScrollPane(tilePanel,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        addTab(tileset.getName(), paletteScrollPane);
    }

    /**
     * Informs the editor of the new tile.
     */
    public void tileSelected(TileSelectionEvent e) {
        mapEditor.setCurrentTile(e.getTile());
    }

    /**
     * Creates a stamp brush from the region contents and sets this as the
     * current brush.
     */
    public void tileRegionSelected(TileRegionSelectionEvent e) {
        mapEditor.setBrush(new CustomBrush(e.getTileRegion()));
    }

    private class MyMapChangeListener implements MapChangeListener
    {
        public void mapChanged(MapChangedEvent e) {
        }

        public void tilesetAdded(MapChangedEvent e, TileSet tileset)
        {
            addTabForTileset(tileset);
        }

        public void tilesetRemoved(MapChangedEvent e, int index)
        {
            removeTabAt(index);
        }
    }
}
