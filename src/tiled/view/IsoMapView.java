/*
 *  Tiled Map Editor, (c) 2004-2006
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <bjorn@lindeijer.nl>
 *
 *  screenToPixelCoords and paintObjectGroup
 *  implemented by: Alturos <alturos@gmail.com>
 */

package tiled.view;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import javax.swing.SwingConstants;
import java.util.Iterator;
import java.util.Properties;

import tiled.core.*;
import tiled.mapeditor.selection.SelectionLayer;

/**
 * Isometric map view implementation.
 */
public class IsoMapView extends MapView
{
    /**
     * Creates a new isometric map view that displays the specified map.
     *
     * @param map the map to be displayed by this map view
     */
    public IsoMapView(Map map) {
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

    protected void paintLayer(Graphics2D g2d, TileLayer layer) {
        // Turn anti alias on for selection drawing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        Rectangle clipRect = g2d.getClipBounds();
        Dimension tileSize = new Dimension(layer.getTileWidth(), layer.getTileHeight());
        int tileStepY = tileSize.height / 2 == 0 ? 1 : tileSize.height / 2;
        Polygon gridPoly = createGridPolygon(tileSize, 0, -tileSize.height, 0);

        Point rowItr = screenToTileCoords(layer, clipRect.x, clipRect.y);
        rowItr.x--;
        Point offset = calculateParallaxOffsetZoomed(layer);
        Point drawLoc = tileToScreenCoords(offset,tileSize, rowItr.x, rowItr.y);
        drawLoc.x -= tileSize.width / 2;
        drawLoc.y += tileSize.height;

        // Determine area to draw from clipping rectangle
        int columns = clipRect.width / tileSize.width + 3;
        int rows = (clipRect.height + (int)(map.getTileHeightMax() * zoom)) /
            tileStepY + 4;

        // Draw this map layer
        for (int y = 0; y < rows; y++) {
            Point columnItr = new Point(rowItr);

            for (int x = 0; x < columns; x++) {
                Tile tile = layer.getTileAt(columnItr.x, columnItr.y);

                if (tile != null) {
                    if (layer instanceof SelectionLayer) {
                        //Polygon gridPoly = createGridPolygon(
                                //drawLoc.x, drawLoc.y - tileSize.height, 0);
                        gridPoly.translate(drawLoc.x, drawLoc.y);
                        g2d.fillPolygon(gridPoly);
                        gridPoly.translate(-drawLoc.x, -drawLoc.y);
                        //paintEdge(g2d, layer, drawLoc.x, drawLoc.y);
                    } else {
                        tile.draw(g2d, drawLoc.x, drawLoc.y, zoom);
                    }
                }

                // Advance to the next tile
                columnItr.x++;
                columnItr.y--;
                drawLoc.x += tileSize.width;
            }

            // Advance to the next row
            if ((y & 1) > 0) {
                rowItr.x++;
                drawLoc.x += tileSize.width / 2;
            } else {
                rowItr.y++;
                drawLoc.x -= tileSize.width / 2;
            }
            drawLoc.x -= columns * tileSize.width;
            drawLoc.y += tileStepY;
        }
    }

    protected void paintObjectGroup(Graphics2D g2d, ObjectGroup og)
    {
        final Dimension tsize = getTileSize();
        final Rectangle bounds = og.getBounds();

        Iterator<MapObject> itr = og.getObjects();
        g2d.translate(bounds.x * tsize.width, bounds.y * tsize.height);

        while (itr.hasNext()) {
            MapObject mo = itr.next();
            final double oxi = mo.getX() * zoom;
            final double oyi = mo.getY() * zoom;

            Point objTileCoords = pixelToTileCoords((int) oxi, (int) oyi);
            Point objScreenCoords = tileToScreenCoords(og,
                                                       objTileCoords.x,
                                                       objTileCoords.y);
            int ox = objScreenCoords.x;
            int oy = objScreenCoords.y;

            Image objectImage = mo.getImage(zoom);
            if (objectImage != null) {
                g2d.drawImage(objectImage, (int) ox, (int) oy, null);
            }
            if (mo.getWidth() == 0 || mo.getHeight() == 0) {
                g2d.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.black);
                g2d.fillOval(
                        (int) ox + 1, (int) oy + 1,
                        (int) (10 * zoom), (int) (10 * zoom));
                g2d.setColor(Color.orange);
                g2d.fillOval(
                        (int) ox, (int) oy,
                        (int) (10 * zoom), (int) (10 * zoom));
                g2d.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_OFF);
            }
            else {
                g2d.setColor(Color.black);
                objScreenCoords.x += 1;
                objScreenCoords.y += 1;
                g2d.drawPolygon(getObjectPolygon(mo,objScreenCoords));
                objScreenCoords.x -= 1;
                objScreenCoords.y -= 1;
                g2d.setColor(Color.orange);
                g2d.drawPolygon(getObjectPolygon(mo,objScreenCoords));
            }
            if (zoom > 0.0625) {
                final String s = mo.getName() != null ? mo.getName() : "(null)";
                int XOffset = (s.length() / 2) * 6;
                g2d.setColor(Color.black);
                g2d.drawString(s, (int) (ox - XOffset) + 1, (int) (oy - 5) + 1);
                g2d.setColor(Color.white);
                g2d.drawString(s, (int) (ox - XOffset), (int) (oy - 5));
            }
        }
    }

    protected void paintGrid(Graphics2D g2d) {
        MapLayer currentLayer = getCurrentLayer();
        if(currentLayer == null)
            return;
        
        Dimension tileSize = getTileSize();
        Point offset = calculateParallaxOffsetZoomed(currentLayer);
        Rectangle clipRect = g2d.getClipBounds();

        clipRect.x -= tileSize.width / 2;
        clipRect.width += tileSize.width;
        clipRect.height += tileSize.height / 2;

        int startX = Math.max(0, screenToTileCoords(currentLayer,clipRect.x, clipRect.y).x);
        int startY = Math.max(0, screenToTileCoords(
                    currentLayer,clipRect.x + clipRect.width, clipRect.y).y);
        int endX = Math.min(map.getWidth(), screenToTileCoords(
                    currentLayer,clipRect.x + clipRect.width,
                    clipRect.y + clipRect.height).x);
        int endY = Math.min(map.getHeight(), screenToTileCoords(
                    currentLayer,clipRect.x, clipRect.y + clipRect.height).y);

        for (int y = startY; y <= endY; y++) {
            Point start = tileToScreenCoords(offset,tileSize, startX, y);
            Point end = tileToScreenCoords(offset,tileSize, endX, y);
            g2d.drawLine(start.x, start.y, end.x, end.y);
        }
        for (int x = startX; x <= endX; x++) {
            Point start = tileToScreenCoords(offset,tileSize, x, startY);
            Point end = tileToScreenCoords(offset,tileSize, x, endY);
            g2d.drawLine(start.x, start.y, end.x, end.y);
        }
    }

    protected void paintCoordinates(Graphics2D g2d) {
        MapLayer currentLayer = getCurrentLayer();
        if(currentLayer == null)
            return;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Rectangle clipRect = g2d.getClipBounds();
        Dimension tileSize = getTileSize();
        Point offset = calculateParallaxOffsetZoomed(currentLayer);
        int tileStepY = tileSize.height / 2 == 0 ? 1 : tileSize.height / 2;
        Font font = new Font("SansSerif", Font.PLAIN, tileSize.height / 4);
        g2d.setFont(font);
        FontRenderContext fontRenderContext = g2d.getFontRenderContext();
        
        Point rowItr = screenToTileCoords(currentLayer, clipRect.x, clipRect.y);
        rowItr.x--;
        Point drawLoc = tileToScreenCoords(offset,tileSize, rowItr.x, rowItr.y);
        drawLoc.y += tileSize.height / 2;

        // Determine area to draw from clipping rectangle
        int columns = clipRect.width / tileSize.width + 3;
        int rows = clipRect.height / tileStepY + 4;

        // Draw the coordinates
        for (int y = 0; y < rows; y++) {
            Point columnItr = new Point(rowItr);

            for (int x = 0; x < columns; x++) {
                if (map.contains(columnItr.x, columnItr.y)) {
                    String coords =
                        "(" + columnItr.x + "," + columnItr.y + ")";
                    Rectangle2D textSize =
                        font.getStringBounds(coords, fontRenderContext);

                    int fx = drawLoc.x - (int)(textSize.getWidth() / 2);
                    int fy = drawLoc.y + (int)(textSize.getHeight() / 2);

                    g2d.drawString(coords, fx, fy);
                }

                // Advance to the next tile
                columnItr.x++;
                columnItr.y--;
                drawLoc.x += tileSize.width;
            }

            // Advance to the next row
            if ((y & 1) > 0) {
                rowItr.x++;
                drawLoc.x += tileSize.width / 2;
            } else {
                rowItr.y++;
                drawLoc.x -= tileSize.width / 2;
            }
            drawLoc.x -= columns * tileSize.width;
            drawLoc.y += tileStepY;
        }
    }

    protected void paintPropertyFlags(Graphics2D g2d, TileLayer layer) {
        throw new RuntimeException("Not yet implemented");    // todo
    }

    public void repaintRegion(MapLayer layer,Rectangle region) {
        Dimension tileSize = getTileSize();
        Point offset = calculateParallaxOffsetZoomed(layer);
        if(layer == null)
            return;
        int maxExtraHeight =
            (int)(map.getTileHeightMax() * zoom) - tileSize.height;

        int mapX1 = region.x;
        int mapY1 = region.y;
        int mapX2 = mapX1 + region.width;
        int mapY2 = mapY1 + region.height;

        int x1 = tileToScreenCoords(offset,tileSize, mapX1, mapY2).x;
        int y1 = tileToScreenCoords(offset,tileSize, mapX1, mapY1).y - maxExtraHeight;
        int x2 = tileToScreenCoords(offset,tileSize, mapX2, mapY1).x;
        int y2 = tileToScreenCoords(offset,tileSize, mapX2, mapY2).y;

        repaint(new Rectangle(x1, y1, x2 - x1, y2 - y1));
    }

    public Dimension getPreferredSize() {
        Dimension tileSize = getTileSize();
        int border = showGrid ? 1 : 0;
        int mapSides = map.getHeight() + map.getWidth();

        return new Dimension(
                (mapSides * tileSize.width) / 2 + border,
                (mapSides * tileSize.height) / 2 + border);
    }

    /**
     * Returns the coordinates of the tile at the given screen coordinates.
     */
    public Point screenToTileCoords(MapLayer layer,int x, int y) {
        Dimension tileSize = getTileSize();
        double r = getTileRatio();

        // Translate origin to top-center
        x -= map.getHeight() * (tileSize.width / 2);
        int mx = y + (int)(x / r);
        int my = y - (int)(x / r);

        // Calculate map coords and divide by tile size (tiles assumed to
        // be square in normal projection)
        return new Point(
                (mx < 0 ? mx - tileSize.height : mx) / tileSize.height,
                (my < 0 ? my - tileSize.height : my) / tileSize.height);
    }

    /**
     * Returns the coordinates the mouse arrow in MapPixel Coords.
     */
    public Point screenToPixelCoords(int x, int y)
    {
        Dimension tileSize = getTileSize();
        double r = getTileRatio();

        // Translate origin to top-center
        x -= map.getHeight() * (tileSize.width / 2);
        int mx = y + (int) (x / r);
        int my = y - (int) (x / r);

        my /= zoom;
        mx /= zoom;

        // Calculate map coords and divide by tile size (tiles assumed to
        // be square in normal projection)
        return new Point(
                (mx < 0 ? mx - tileSize.height : (int) (mx * r)),
                (my < 0 ? my - tileSize.height : my));
        //Point pos = screenToTileCoords(x, y);
        //pos.x = ((pos.x * map.getTileWidth()));
        //pos.y = ((pos.y * map.getTileHeight()));
        //return pos;
    }

    protected Polygon createGridPolygon(Dimension tileSize, int tx, int ty, int border) {
        tileSize.width -= border * 2;
        tileSize.height -= border * 2;

        Polygon poly = new Polygon();
        poly.addPoint(tx + tileSize.width / 2 + border, ty + border);
        poly.addPoint(tx + tileSize.width, ty + tileSize.height / 2 + border);
        poly.addPoint(tx + tileSize.width / 2 + border,
                ty + tileSize.height + border);
        poly.addPoint(tx + border, ty + tileSize.height / 2 + border);
        return poly;
    }

    protected Dimension getTileSize() {
        return new Dimension(
                (int)(map.getTileWidth() * zoom),
                (int)(map.getTileHeight() * zoom));
    }

    protected double getTileRatio() {
        return (double)map.getTileWidth() / (double)map.getTileHeight();
    }

    protected Point pixelToTileCoords(int x, int y)
    {
        Point TilePos = new Point();
        Dimension tileSize = getTileSize();
        // First, make sure we are at the top left corner

        x = x - (x % tileSize.width);
        y = y - (y % tileSize.height);

        TilePos.x = (int) ((x / tileSize.width));
        TilePos.y = (int) ((y / tileSize.height));
        return TilePos;
    }

    protected Polygon getObjectPolygon(MapObject mo, Point topLeftScr)
    {
        // The number of pixels short of a full tile.
        // This promotes drawing only a full tile worth of outline
        final int xTOffset = (mo.getWidth() % map.getTileWidth());
        final int yTOffset = (mo.getHeight() % map.getTileHeight());

        // The number of tilews wide and deep the object is
        final int xTiles = ((mo.getWidth() + xTOffset) / map.getTileWidth());
        final int yTiles = ((mo.getHeight() + yTOffset) / map.getTileHeight());

        final int halfWidth = (int) (map.getTileWidth() * zoom) / 2;
        final int halfHeight = (int) (map.getTileHeight() * zoom) / 2;

        Polygon poly = new Polygon();
        // Top left
        poly.addPoint(topLeftScr.x, topLeftScr.y);
        // Top Right
        poly.addPoint(
                (topLeftScr.x + (halfWidth * xTiles)),
                (topLeftScr.y + (halfHeight * xTiles)));
        // Bottom Right
        poly.addPoint(
                (topLeftScr.x + ((xTiles - yTiles) * halfWidth)),
                (topLeftScr.y + ((xTiles + yTiles) * halfHeight)));
        // Bottom Left
        poly.addPoint(
                (topLeftScr.x - (yTiles * halfWidth)),
                (topLeftScr.y + (yTiles * halfHeight)));

        return poly;
    }

    /**
     * Returns the location on the screen of the top corner of a tile.
     */
    public Point tileToScreenCoords(Point offset,Dimension tileSize, int x, int y) {
        int originX = (map.getHeight() * tileSize.width) / 2;
        return new Point(
                offset.x + ((x - y) * tileSize.width / 2) + originX,
                offset.y + ((x + y) * tileSize.height / 2));
    }
}
