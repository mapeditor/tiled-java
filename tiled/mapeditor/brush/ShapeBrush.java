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

package tiled.mapeditor.brush;

import java.awt.*;
import java.awt.geom.*;

import tiled.core.*;


public class ShapeBrush extends Brush
{
    protected Area shape;
    protected Tile paintTile;

    public ShapeBrush() {
        super();
    }

    public ShapeBrush(Area shape) {
        super();
        this.shape = shape;
    }

    public void makeCircleBrush(double rad) {
        shape = new Area(new Ellipse2D.Double(0, 0, rad * 2, rad * 2));
    }

    public void makeQuadBrush(Rectangle r) {
        shape = new Area(new Rectangle2D.Double(r.x, r.y, r.width, r.height));
    }

    public Rectangle commitPaint(MultilayerPlane mp, int x, int y, int start) {
        Rectangle bounds = shape.getBounds();
        int centerx = (int)(x - (bounds.width / 2));
        int centery = (int)(y - (bounds.height / 2));
        MapLayer ml = mp.getLayer(start);

        // TODO: This loop does not take all edges into account

        for (int i = 0; i <= bounds.height; i++) {
            for (int j = 0; j <= bounds.width; j++) {
                if (shape.contains(j, i)) {
                    ((TileLayer)ml).setTileAt(j + centerx, i + centery, paintTile);
                }
            }
        }

        // Return affected area
        return new Rectangle(centerx, centery, bounds.width, bounds.height);
    }

    public void setTile(Tile t) {
        paintTile = t;
    }

    public void draw(Graphics g) {
        // TODO: Do the drawing
    }
}
