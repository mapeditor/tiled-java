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

package tiled.mapeditor.selection;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import tiled.core.*;
import tiled.util.TiledConfiguration;


/**
 * A layer used to keep track of a selected area.
 */
public class SelectionLayer extends TileLayer
{
    private Color highlightColor;
    private Tile selTile;
    private Area selection;

    public SelectionLayer() {
        super();
        init();
    }

    public SelectionLayer(int w, int h) {
        super(w, h);
        init();
    }

    private void init() {
        TiledConfiguration conf = TiledConfiguration.getInstance();
        try {
            highlightColor = Color.decode(
                    conf.getValue("tiled.selection.color"));
        } catch (Throwable e) {
            highlightColor = Color.blue;
        }

        selTile = new Tile();
        selection = new Area();
    }

    /**
     * Returns the selected area.
     */
    public Area getSelectedArea() {
        return selection;
    }

    /**
     * Returns the bounds of the selected area.
     */
    public Rectangle getSelectedAreaBounds() {
        return selection.getBounds();
    }

    /**
     * Deselects the given area. This substracts the given area from the
     * existing selected area.
     *
     * @param area the area to deselect
     */
    public void subtract(Area area) {
        clearRegion(area);
        selection.subtract(area);
    }

    public void selectRegion(Rectangle region) {
        clearRegion(selection);
        selection = new Area(region);
        fillRegion(selection, selTile);
    }

    public void select(int tx, int ty) {
        setTileAt(tx, ty, selTile);
        if (selection == null) {
            selection = new Area(new Rectangle2D.Double(tx, ty, 1, 1));
        } else {
            if (!selection.contains(tx, ty)) {
                selection.add(new Area(new Rectangle2D.Double(tx, ty, 1, 1)));
            }
        }
    }

    /**
     * Sets the highlight color.
     *
     * @param c the new highlight color to use when drawing this selection
     */
    public void setHighlightColor(Color c) {
        highlightColor = c;
    }

    /**
     * Returns the highlight color.
     */
    public Color getHighlightColor() {
        return highlightColor;
    }

    private void fillRegion(Area region, Tile fill) {
        Rectangle bounded = region.getBounds();
        for (int i = bounded.y; i < bounded.y + bounded.height; i++) {
            for (int j = bounded.x; j < bounded.x + bounded.width; j++) {
                if (region.contains(j, i)) {
                    setTileAt(j, i, fill);
                }
            }
        }
    }

    private void clearRegion(Area region) {
        Rectangle bounded = region.getBounds();
        for (int i = bounded.y; i < bounded.y + bounded.height; i++) {
            for (int j = bounded.x; j < bounded.x + bounded.width; j++) {
                if (region.contains(j, i)) {
                    setTileAt(j, i, null);
                }
            }
        }
    }

    /**
     * Inverts the selected area.
     */
    public void invert() {
        Rectangle bounded = getBounds();
        selection.exclusiveOr(new Area(bounded));

        for (int i = bounded.y; i < bounded.y + bounded.height; i++) {
            for (int j = bounded.x; j < bounded.x + bounded.width; j++) {
                if (selection.contains(j, i)) {
                    setTileAt(j, i, selTile);
                } else {
                    setTileAt(j, i, null);
                }
            }
        }
    }
}
