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

package tiled.view;

import java.awt.*;

import javax.swing.SwingConstants;

import tiled.core.*;
import tiled.mapeditor.selection.SelectionLayer;


public class IsoMapView extends MapView
{
    public IsoMapView(Map m) {
        super(m);
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,
            int orientation, int direction) {
        Dimension tsize = getTileSize(zoom);
        if (orientation == SwingConstants.VERTICAL) {
            return (visibleRect.height / tsize.height) * tsize.height;
        } else {
            return (visibleRect.width / tsize.width) * tsize.width;
        }
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
            int orientation, int direction) {
        Dimension tsize = getTileSize(zoom);
        if (orientation == SwingConstants.VERTICAL) {
            return tsize.height;
        } else {
            return tsize.width;
        }
    }

    protected void paintLayer(Graphics g, TileLayer layer, double zoom) {
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                         RenderingHints.VALUE_ANTIALIAS_ON);

        // Determine tile size and offset
        Dimension tileSize = getTileSize(zoom);
        int tsize_width_delta = tileSize.width / 2;
        int tsize_height_delta = tileSize.height / 2;


        // Determine area to draw from clipping rectangle
        Rectangle clipRect = g.getClipBounds();
        clipRect.height += myMap.getTileHeightMax() * zoom - tileSize.height;
        int logical_row_max =
            (clipRect.y + clipRect.height) / (tsize_height_delta) + 1;
        int originx = (myMap.getHeight() - 1) * (tileSize.width / 2);

        // Draw this map layer
        for (int logical_row = Math.max(clipRect.y / tsize_height_delta - 1, 0); logical_row < logical_row_max; logical_row++) {
            int x = Math.max(logical_row - (myMap.getHeight() - 1), 0);
            int y = Math.min(logical_row, myMap.getHeight() - 1);
            int gx = Math.abs(originx - logical_row * tsize_width_delta);
            int gy = (logical_row * tsize_height_delta) /*- clipRect.y*/;
            Polygon gridPoly = createGridPolygon(gx, gy, 1);

            while (x <= Math.min(logical_row, myMap.getWidth() - 1) &&
                    y >= Math.max(logical_row-(myMap.getWidth() - 1), 0)) {

                Tile t = layer.getTileAt(x, y);
                if (t != null && t != myMap.getNullTile()) {
                    if (SelectionLayer.class.isInstance(layer)) {
                        g.fillPolygon(gridPoly);
                        //paintEdge(g, layer, gx, gy);
                    } else {
                        t.draw(g, gx, gy, zoom);
                    }
                }

                x++;
                y--;
                gx += tileSize.width;
                gridPoly.translate(tileSize.width, 0);
            }
        }
    }	

    protected void paintLayer(Graphics g, ObjectGroup layer, double zoom) {

    }
    
    protected void paintGrid(Graphics g, double zoom) {
        Dimension tileSize = getTileSize(zoom);
        Rectangle clipRect = g.getClipBounds();

        clipRect.x -= tileSize.width / 2;
        clipRect.width += tileSize.width;
        clipRect.height += tileSize.height / 2;

        int startX = Math.max(0, screenToTileCoords(clipRect.x, clipRect.y).x);
        int startY = Math.max(0, screenToTileCoords(
                    clipRect.x + clipRect.width, clipRect.y).y);
        int endX = Math.min(myMap.getWidth(), screenToTileCoords(
                    clipRect.x + clipRect.width,
                    clipRect.y + clipRect.height).x);
        int endY = Math.min(myMap.getHeight(), screenToTileCoords(
                    clipRect.x, clipRect.y + clipRect.height).y);

        for (int y = startY; y <= endY; y++) {
            Point start = tileToScreenCoords(startX, y);
            Point end = tileToScreenCoords(endX, y);
            g.drawLine(start.x, start.y, end.x, end.y);
        }
        for (int x = startX; x <= endX; x++) {
            Point start = tileToScreenCoords(x, startY);
            Point end = tileToScreenCoords(x, endY);
            g.drawLine(start.x, start.y, end.x, end.y);
        }
    }

    protected void paintCoordinates(Graphics g, double zoom) {
        // TODO: Implement paint coordinates for IsoMapView
    }

    public void repaintRegion(Rectangle region) {
        Dimension tileSize = getTileSize(zoom);
        int maxExtraHeight =
            (int)(myMap.getTileHeightMax() * zoom) - tileSize.height;

        int mapX1 = region.x;
        int mapY1 = region.y;
        int mapX2 = mapX1 + region.width;
        int mapY2 = mapY1 + region.height;

        int x1 = tileToScreenCoords(mapX1, mapY2).x;
        int y1 = tileToScreenCoords(mapX1, mapY1).y - maxExtraHeight;
        int x2 = tileToScreenCoords(mapX2, mapY1).x;
        int y2 = tileToScreenCoords(mapX2, mapY2).y;

        repaint(new Rectangle(x1, y1, x2 - x1, y2 - y1));
    }

    public Dimension getPreferredSize() {
        Dimension tileSize = getTileSize(zoom);
        int border = ((modeFlags & PF_GRIDMODE) != 0) ? 1 : 0;
        int mapSides = myMap.getHeight() + myMap.getWidth();

        return new Dimension(
                (mapSides * tileSize.width) / 2 + border,
                (mapSides * tileSize.height) / 2 + border);
    }

    public Point screenToTileCoords(int x, int y) {
        Dimension tileSize = getTileSize(zoom);
        double r = getTileRatio();

        // Translate origin to top-center
        x -= myMap.getHeight() * (tileSize.width / 2);
        int mx = y + (int)(x / r);
        int my = y - (int)(x / r);

        // Calculate map coords and divide by tile size (tiles assumed to
        // be square in normal projection)
        return new Point(
                ((mx < 0) ? mx - tileSize.height : mx) / tileSize.height,
                ((my < 0) ? my - tileSize.height : my) / tileSize.height);
    }

    protected Polygon createGridPolygon(int tx, int ty, int border) {
        Dimension tileSize = getTileSize(zoom);
        tileSize.width -= border * 2;
        tileSize.height -= border * 2;

        Polygon poly = new Polygon();
        poly.addPoint((tx + tileSize.width / 2 + border), ty + border);
        poly.addPoint((tx + tileSize.width), ty + tileSize.height / 2 + border);
        poly.addPoint((tx + tileSize.width / 2 + border), ty + tileSize.height + border);
        poly.addPoint((tx + border), ty + tileSize.height / 2 + border);
        return poly;
    }

    protected Dimension getTileSize(double zoom) {
        return new Dimension(
                (int)(myMap.getTileWidth() * zoom),
                (int)(myMap.getTileHeight() * zoom));
    }

    protected double getTileRatio() {
        return (double)myMap.getTileWidth() / (double)myMap.getTileHeight();
    }

    public Point tileToScreenCoords(double x, double y) {
        Dimension tileSize = getTileSize(zoom);
        int originX = (myMap.getHeight() * tileSize.width) / 2;
        return new Point(
                (int)((x - y) * tileSize.width / 2) + originX,
                (int)((x + y) * tileSize.height / 2));
    }
}
