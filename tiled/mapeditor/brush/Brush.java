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

public abstract class Brush extends MultilayerPlane {

	public Brush() {
		super();
	}
	
	public abstract void commitPaint(MultilayerPlane mp, int x, int y, int start);
	public abstract Rectangle getCenteredBounds(int x, int y);
}
