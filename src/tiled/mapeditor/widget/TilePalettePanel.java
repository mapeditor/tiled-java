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
import java.util.Vector;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.event.EventListenerList;
import javax.swing.event.MouseInputAdapter;

import tiled.core.Tile;
import tiled.core.TileSet;
import tiled.mapeditor.util.TileSelectionEvent;
import tiled.mapeditor.util.TileSelectionListener;

/**
 * Displays a tileset and allows selecting a specific tile.
 *
 * @version $Id$
 */
public class TilePalettePanel extends JPanel implements Scrollable
{
    private static final int TILES_PER_ROW = 4;
    private TileSet tileset;
    private EventListenerList tileSelectionListeners;
    private Vector tilesetMap;
    private Rectangle selection;

    public TilePalettePanel() {
        tileSelectionListeners = new EventListenerList();

        MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {
            private Point origin;

            public void mousePressed(MouseEvent e) {
                origin = getTileCoordinates(e.getX(), e.getY());
                setSelection(new Rectangle(origin.x, origin.y, 0, 0));
                Tile clickedTile = getTileAt(origin.x, origin.y);
                if (clickedTile != null) {
                    fireTileSelectionEvent(clickedTile);
                }
            }

            public void mouseDragged(MouseEvent e) {
                Point point = getTileCoordinates(e.getX(), e.getY());
                Rectangle select = new Rectangle(origin.x, origin.y, 0, 0);
                select.add(point);
                if (!select.equals(selection)) {
                    setSelection(select);
                }
                // todo: Fire tile region selection event
            }
        };
        addMouseListener(mouseInputAdapter);
        addMouseMotionListener(mouseInputAdapter);
    }

    /**
     * Adds tile selection listener. The listener will be notified when the
     * user selects a tile.
     */
    public void addTileSelectionListener(TileSelectionListener listener) {
        tileSelectionListeners.add(TileSelectionListener.class, listener);
    }

    /**
     * Removes tile selection listener.
     */
    public void removeTileSelectionListener(TileSelectionListener listener) {
        tileSelectionListeners.remove(TileSelectionListener.class, listener);
    }

    protected void fireTileSelectionEvent(Tile selectedTile) {
        Object[] listeners = tileSelectionListeners.getListenerList();
        TileSelectionEvent event = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TileSelectionListener.class) {
                if (event == null) event =
                    new TileSelectionEvent(this, selectedTile);
                ((TileSelectionListener)listeners[i + 1]).tileSelected(event);
            }
        }
    }

    /**
     * Change the tileset displayed by this palette panel.
     *
     * @param tileset
     */
    public void setTileset(TileSet tileset) {
        this.tileset = tileset;
        if (tileset != null) tilesetMap = tileset.generateGaplessVector();
        revalidate();
        repaint();
    }

    /**
     * Converts pixel coordinates to tile coordinates. The returned coordinates
     * are adjusted with respect to the number of tiles per row.
     */
    private Point getTileCoordinates(int x, int y) {
        int twidth = tileset.getTileWidth() + 1;
        int theight = tileset.getTileHeight() + 1;

        int tileX = Math.min(x / twidth, getTilesPerRow() - 1);
        int tileY = y / theight;

        return new Point(tileX, tileY);
    }

    /**
     * Retrieves the tile at the given tile coordinates. It assumes the tile
     * coordinates are adjusted to the number of tiles per row.
     *
     * @return the tile at the given tile coordinates, or <code>null</code>
     *         if the index is out of range
     */
    private Tile getTileAt(int x, int y) {
        int tilesPerRow = getTilesPerRow();
        int tileAt = y * tilesPerRow + x;

        if (tileAt >= tilesetMap.size()) {
            return null;
        } else {
            return (Tile) tilesetMap.get(tileAt);
        }
    }

    /**
     * Returns the number of tiles to display per row. This gets calculated
     * dynamically unless the tileset specifies this value.
     */
    private int getTilesPerRow() {
        // todo: It should be an option to follow the tiles per row given
        // todo: by the tileset.
        if (tileset.getTilesPerRow() == 0) {
            int twidth = tileset.getTileWidth() + 1;
            return Math.max(1, (getWidth() - 1) / twidth);
        } else {
            return tileset.getTilesPerRow();
        }
    }

    private void setSelection(Rectangle rect) {
        repaintSelection();
        selection = rect;
        repaintSelection();
    }

    private void repaintSelection() {
        if (selection != null) {
            int twidth = tileset.getTileWidth() + 1;
            int theight = tileset.getTileHeight() + 1;

            repaint(selection.x * twidth, selection.y * theight,
                    (selection.width + 1) * twidth + 1,
                    (selection.height + 1) * theight + 1);
        }
    }

    public void paint(Graphics g) {
        Rectangle clip = g.getClipBounds();

        paintBackground(g);

        if (tileset != null) {
            // Draw the tiles
            int twidth = tileset.getTileWidth() + 1;
            int theight = tileset.getTileHeight() + 1;
            int tilesPerRow = getTilesPerRow();

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

            // Draw the selection
            if (selection != null) {
                g.setColor(new Color(100, 100, 255));
                g.draw3DRect(
                        selection.x * twidth, selection.y * theight,
                        (selection.width + 1) * twidth,
                        (selection.height + 1) * theight,
                        false);
                ((Graphics2D) g).setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_ATOP, 0.2f));
                g.fillRect(
                        selection.x * twidth + 1, selection.y * theight + 1,
                        (selection.width + 1) * twidth - 1,
                        (selection.height + 1) * theight - 1);
            }
        }
    }

    /**
     * Draws checkerboard background.
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
            int tilesPerRow = getTilesPerRow();
            int rows = tileCount / tilesPerRow +
                    (tileCount % tilesPerRow > 0 ? 1 : 0);

            return new Dimension(tilesPerRow * twidth + 1, rows * theight + 1);
        }
    }


    // Scrollable interface

    public Dimension getPreferredScrollableViewportSize() {
        if (tileset != null) {
            int twidth = tileset.getTileWidth() + 1;
            return new Dimension(TILES_PER_ROW * twidth + 1, 200);
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
        return tileset.getTilesPerRow() == 0;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
