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

package tiled.mapeditor.widget;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.event.MouseInputAdapter;

import tiled.core.*;
import tiled.mapeditor.util.TileSelectionEvent;
import tiled.mapeditor.util.TileSelectionListener;

/**
 * Works very much like TilePalettePanel, but is more specialized, in that it 
 * always displays all tiles in one row, and stamps cannot be made from it.
 *
 * @version $Id: TilePaletteQuickPanel.java 701 2006-10-14 20:23:44Z bjorn $
 */
public class TilePaletteQuickPanel extends JPanel implements Scrollable,
       TilesetChangeListener
{
    private TileSet tileset;
    private List tileSelectionListeners;
    private Vector tilesetMap;
    
    public TilePaletteQuickPanel() {
        tileSelectionListeners = new LinkedList();

        MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {
            private Point origin;

            public void mousePressed(MouseEvent e) {
                origin = new Point(e.getX() / (tileset.getTileWidth() + 1), 0);
                Tile clickedTile = getTileAt(origin.x, origin.y);
                if (clickedTile != null) {
                    fireTileSelectionEvent(clickedTile);
                }
            }
        };
        addMouseListener(mouseInputAdapter);
        addMouseMotionListener(mouseInputAdapter);
    }
    
    /**
     * Adds tile selection listener. The listener will be notified when the
     * user selects a tile.
     *
     * @param listener the listener to add
     */
    public void addTileSelectionListener(TileSelectionListener listener) {
        tileSelectionListeners.add(listener);
    }

    /**
     * Removes tile selection listener.
     *
     * @param listener the listener to remove
     */
    public void removeTileSelectionListener(TileSelectionListener listener) {
        tileSelectionListeners.remove(listener);
    }

    private void fireTileSelectionEvent(Tile selectedTile) {
        TileSelectionEvent event = new TileSelectionEvent(this, selectedTile);
        Iterator iterator = tileSelectionListeners.iterator();

        while (iterator.hasNext()) {
            ((TileSelectionListener) iterator.next()).tileSelected(event);
        }
    }
    
    /**
     * Draws checkerboard background.
     *
     * @param g the {@link Graphics} instance to draw on
     */
    private static void paintBackground(Graphics g) {
        Rectangle clip = g.getClipBounds();
        int side = 10;

        int startX = clip.x / side;
        int startY = clip.y / side;
        int endX = (clip.x + clip.width) / side + 1;
        int endY = (clip.y + clip.height) / side + 1;

        // Fill with white background
        g.setColor(Color.WHITE);
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

        // Draw darker squares
        g.setColor(Color.LIGHT_GRAY);
        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                if ((y + x) % 2 == 1) {
                    g.fillRect(x * side, y * side, side, side);
                }
            }
        }
    }

    public Dimension getPreferredSize() {
        if (tileset == null) {
            return new Dimension(0, 0);
        }
        else {
            int twidth = tileset.getTileWidth() + 1;
            int theight = tileset.getTileHeight() + 1;
            int tileCount = tilesetMap.size();

            return new Dimension(tileCount * (twidth + 1), theight + 1);
        }
    }


    // Scrollable interface

    public Dimension getPreferredScrollableViewportSize() {
        if (tileset != null) {
            int twidth = tileset.getTileWidth() + 1;
            int theight = tileset.getTileHeight() + 1;
            int tileCount = tilesetMap.size();

            return new Dimension(Math.max(1, (getWidth() - 1)), theight);
        } else {
            return new Dimension(0, 0);
        }
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
            int orientation, int direction) {
        if (tileset != null) {
            return tileset.getTileWidth();
        } else {
            return 0;
        }
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,
            int orientation, int direction) {
        if (tileset != null) {
            return tileset.getTileWidth();
        } else {
            return 0;
        }
    }

    public boolean getScrollableTracksViewportWidth() {
        // todo: Update when this has become an option
        return false;
    }

    public boolean getScrollableTracksViewportHeight() {
        return tileset == null || tileset.getTilesPerRow() == 0;
    }

    public void paint(Graphics g) {
        Rectangle clip = g.getClipBounds();

        paintBackground(g);

        if (tileset != null) {
            // Draw the tiles
            int twidth = tileset.getTileWidth() + 1;
            int theight = tileset.getTileHeight() + 1;
            int tilesPerRow = Math.max(1, (getWidth() - 1) / twidth);

            int startY = clip.y / theight;
            int endY = (clip.y + clip.height) / theight + 1;
            int tileAt = tilesPerRow * startY;
            int gx;
            int gy = startY * theight;

            for (int y = startY; y < endY; y++) {
                gx = 1;

                for (int x = 0;
                     x < tilesPerRow && tileAt < tilesetMap.size();
                     x++, tileAt++)
                {
                    Tile tile = (Tile) tilesetMap.get(tileAt);

                    if (tile != null) {
                        tile.drawRaw(g, gx, gy + theight, 1.0);
                    }
                    gx += twidth;
                }
                gy += theight;
            }
        }
    }
    
    /**
     * Retrieves the tile at the given tile coordinates. It assumes the tile
     * coordinates are adjusted to the number of tiles per row.
     *
     * @param x x tile coordinate
     * @param y y tile coordinate
     * @return the tile at the given tile coordinates, or <code>null</code>
     *         if the index is out of range
     */
    private Tile getTileAt(int x, int y) {
        //we don't care about y, but it's here for convention

        if (x >= tilesetMap.size()) {
            return null;
        } else {
            return (Tile) tilesetMap.get(x);
        }
    }
    
    /**
     * Change the tileset displayed by this palette panel.
     *
     * @param tileset the tileset to be displayed by this palette panel
     */
    public void setTileset(TileSet tileset) {
        // Remove any existing listener
        if (this.tileset != null) {
            this.tileset.removeTilesetChangeListener(this);
        }

        this.tileset = tileset;

        // Listen to changes in the new tileset
        if (this.tileset != null) {
            this.tileset.addTilesetChangeListener(this);
        }

        if (tileset != null) tilesetMap = tileset.generateGaplessVector();
        revalidate();
        repaint();
    }

    public void tilesetChanged(TilesetChangedEvent event) {
        tilesetMap = tileset.generateGaplessVector();
        revalidate();
        repaint();
    }

}
