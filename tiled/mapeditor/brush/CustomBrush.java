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

import java.awt.Rectangle;

import tiled.core.MultilayerPlane;

public class CustomBrush extends Brush {

	public CustomBrush() {
		super();
	}

    public void commitPaint(MultilayerPlane mp, int x, int y, int start) {
        // TODO Auto-generated method stub
        
    }

	public Rectangle getCenteredBounds(int x, int y) {
		return null;
	}
}
