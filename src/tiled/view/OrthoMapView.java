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

/**
 * @version $Id$
 */
public class OrthoMapView extends MapView
{
    /**
     * Creates a new orthographic map view that displays the specified map.
     *
     * @param map the map to be displayed by this map view
     */
    public OrthoMapView(Map map) {
        super(map);
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,
            int orientation, int direction) {
        Dimension tsize = getTileSize();

        if (orientation == SwingConstants.VERTICAL) {
            return (visibleRect.height / tsize.height) * tsize.height;
        } else {
            return (visibleRect.width / tsize.width) * tsize.width;
        }
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
            int orientation, int direction) {
        Dimension tsize = getTileSize();
        if (orientation == SwingConstants.VERTICAL) {
            return tsize.height;
        } else {
            return tsize.width;
        }
    }

    public Dimension getPreferredSize() {
        Dimension tsize = getTileSize();

        return new Dimension(
                map.getWidth() * tsize.width,
                map.getHeight() * tsize.height);
    }

    protected void paintLayer(Graphics2D g2d, TileLayer layer) {
        // Determine tile size and offset
        Dimension tsize = getTileSize();
        if (tsize.width <= 0 || tsize.height <= 0) return;
        Polygon gridPoly = createGridPolygon(0, -tsize.height, 0);

        // Determine area to draw from clipping rectangle
        Rectangle clipRect = g2d.getClipBounds();
        int startX = clipRect.x / tsize.width;
        int startY = clipRect.y / tsize.height;
        int endX = (clipRect.x + clipRect.width) / tsize.width + 1;
        int endY = (clipRect.y + clipRect.height) / tsize.height + 3;
        // (endY +2 for high tiles, could be done more properly)

        // Draw this map layer
        for (int y = startY, gy = (startY + 1) * tsize.height;
                y < endY; y++, gy += tsize.height) {
            for (int x = startX, gx = startX * tsize.width;
                    x < endX; x++, gx += tsize.width) {
                Tile tile = layer.getTileAt(x, y);

                if (tile != null) {
                    if (layer instanceof SelectionLayer) {
                        gridPoly.translate(gx, gy);
                        g2d.fillPolygon(gridPoly);
                        gridPoly.translate(-gx, -gy);
                        //paintEdge(g, layer, gx, gy);
                    } else {
                        tile.draw(g2d, gx, gy, zoom);
                    }
                }
            }
        }
    }

    protected void paintObjectGroup(Graphics2D g, ObjectGroup og) {
        Iterator itr = og.getObjects();

        while (itr.hasNext()) {
            MapObject mo = (MapObject) itr.next();
            double ox = mo.getX() * zoom;
            double oy = mo.getY() * zoom;

            g.setColor(Color.black);
            g.fillOval((int)ox, (int)oy, (int)(10 * zoom), (int)(10 * zoom));
            if (zoom > 0.0625) {
                g.setColor(Color.white);
                g.drawString(mo.getType(), (int)(ox - 12), (int)(oy - 5));
            }
        }
    }

    protected void paintGrid(Graphics2D g2d) {
        // Determine tile size
        Dimension tsize = getTileSize();
        if (tsize.width <= 0 || tsize.height <= 0) return;

        // Determine lines to draw from clipping rectangle
        Rectangle clipRect = g2d.getClipBounds();
        int startX = clipRect.x / tsize.width * tsize.width;
        int startY = clipRect.y / tsize.height * tsize.height;
        int endX = clipRect.x + clipRect.width;
        int endY = clipRect.y + clipRect.height;

        for (int x = startX; x < endX; x += tsize.width) {
            g2d.drawLine(x, clipRect.y, x, clipRect.y + clipRect.height - 1);
        }
        for (int y = startY; y < endY; y += tsize.height) {
            g2d.drawLine(clipRect.x, y, clipRect.x + clipRect.width - 1, y);
        }
    }

    protected void paintCoordinates(Graphics2D g2d) {
        Dimension tsize = getTileSize();
        if (tsize.width <= 0 || tsize.height <= 0) return;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Determine tile size and offset
        Font font = new Font("SansSerif", Font.PLAIN, tsize.height / 4);
        g2d.setFont(font);
        FontRenderContext fontRenderContext = g2d.getFontRenderContext();

        // Determine area to draw from clipping rectangle
        Rectangle clipRect = g2d.getClipBounds();
        int startX = clipRect.x / tsize.width;
        int startY = clipRect.y / tsize.height;
        int endX = (clipRect.x + clipRect.width) / tsize.width + 1;
        int endY = (clipRect.y + clipRect.height) / tsize.height + 1;

        // Draw the coordinates
        int gy = startY * tsize.height;
        for (int y = startY; y < endY; y++) {
            int gx = startX * tsize.width;
            for (int x = startX; x < endX; x++) {
                String coords = "(" + x + "," + y + ")";
                Rectangle2D textSize =
                    font.getStringBounds(coords, fontRenderContext);

                int fx = gx + (int)((tsize.width - textSize.getWidth()) / 2);
                int fy = gy + (int)((tsize.height + textSize.getHeight()) / 2);

                g2d.drawString(coords, fx, fy);
                gx += tsize.width;
            }
            gy += tsize.height;
        }
    }

    public void repaintRegion(Rectangle region) {
        Dimension tsize = getTileSize();
        if (tsize.width <= 0 || tsize.height <= 0) return;
        int maxExtraHeight =
                (int)(map.getTileHeightMax() * zoom - tsize.height);

        // Calculate the visible corners of the region
        int startX = region.x * tsize.width;
        int startY = region.y * tsize.height - maxExtraHeight;
        int endX = (region.x + region.width) * tsize.width;
        int endY = (region.y + region.height) * tsize.height;

        Rectangle dirty =
            new Rectangle(startX, startY, endX - startX, endY - startY);

        repaint(dirty);
    }

    public Point screenToTileCoords(int x, int y) {
        Dimension tsize = getTileSize();
        return new Point(x / tsize.width, y / tsize.height);
    }

    protected Dimension getTileSize() {
        return new Dimension(
                (int)(map.getTileWidth() * zoom),
                (int)(map.getTileHeight() * zoom));
    }

    protected Polygon createGridPolygon(int tx, int ty, int border) {
        Dimension tsize = getTileSize();

        Polygon poly = new Polygon();
        poly.addPoint(tx - border, ty - border);
        poly.addPoint(tx + tsize.width + border, ty - border);
        poly.addPoint(tx + tsize.width + border, ty + tsize.height + border);
        poly.addPoint(tx - border, ty + tsize.height + border);

        return poly;
    }

    public Point tileToScreenCoords(double x, double y) {
        Dimension tsize = getTileSize();
        return new Point((int)x * tsize.width, (int)y * tsize.height);
    }
}
