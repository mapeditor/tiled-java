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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

import tiled.core.*;


public class ObliqueMapView extends MapView
{
	public ObliqueMapView(Map m) {
		super(m);
	}
	
	public int getScrollableBlockIncrement(
		Rectangle visibleRect,
		int orientation,
		int direction) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getScrollableUnitIncrement(
		Rectangle visibleRect,
		int orientation,
		int direction) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Dimension getPreferredSize() {
		// TODO Auto-generated method stub
		return new Dimension(0, 0);
	}

	protected void paint(Graphics g, MapLayer layer, double zoom) {
		// TODO Auto-generated method stub
	}
    
    protected void paintGrid(Graphics g, double zoom) {
        // TODO
    }

	public Point screenToTileCoords(int x, int y) {
		// TODO Auto-generated method stub
		return null;
	}

	protected Polygon createGridPolygon(int tx, int ty, int border) {
		// TODO Auto-generated method stub
		return null;
	}

    public Point tileToScreenCoords(double x, double y) {
        // TODO Auto-generated method stub
        return null;
    }
}
