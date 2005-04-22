/*
 *  Tiled Map Editor, (c) 2004-2005
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <b.lindeijer@xs4all.nl>
 */

package tiled.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import javax.swing.SwingConstants;
import tiled.core.*;
import tiled.mapeditor.selection.SelectionLayer;

public class ShiftedMapView extends MapView
{
    private int horSide;       // Length of horizontal sides
    private int verSide;       // Length of vertical sides

    public ShiftedMapView(Map m) {
        super(m);

        horSide = 16;
        verSide = 0;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,
            int orientation, int direction) {
        int unit =
            getScrollableUnitIncrement(visibleRect, orientation, direction);

        if (orientation == SwingConstants.VERTICAL) {
            return (visibleRect.height / unit) * unit;
        } else {
            return (visibleRect.width / unit) * unit;
        }
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
            int orientation, int direction) {
        Dimension tsize = getTileSize(zoom);
        if (orientation == SwingConstants.VERTICAL) {
            return tsize.height - ((tsize.height - verSide) / 2);
        } else {
            return tsize.width - ((tsize.width - horSide) / 2);
        }
    }

    public Dimension getPreferredSize() {
        Dimension tsize = getTileSize(zoom);
        int border = ((modeFlags & PF_GRIDMODE) != 0) ? 1 : 0;
        int onceX = (tsize.width - horSide) / 2;
        int repeatX = tsize.width - onceX;
        int onceY = (tsize.height - verSide) / 2;
        int repeatY = tsize.height - onceY;

        return new Dimension(
                myMap.getWidth() * repeatX + onceX + border,
                myMap.getHeight() * repeatY + onceY + border);
    }

    protected void paintLayer(Graphics2D g2d, TileLayer layer, double zoom) {
    }

    protected void paintLayer(Graphics2D g, ObjectGroup layer, double zoom) {
    }

    protected void paintGrid(Graphics2D g2d, double zoom) {
    }

    protected void paintCoordinates(Graphics2D g2d, double zoom) {
    }

    public void repaintRegion(Rectangle region) {
    }

    public Point screenToTileCoords(int x, int y) {
        return new Point(0, 0);
    }

    protected Dimension getTileSize(double zoom) {
        return new Dimension(
                (int)(myMap.getTileWidth() * zoom),
                (int)(myMap.getTileHeight() * zoom));
    }

    protected Polygon createGridPolygon(int tx, int ty, int border) {
        return new Polygon();
    }

    public Point tileToScreenCoords(double x, double y) {
        return new Point(0, 0);
    }
}
