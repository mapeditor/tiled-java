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

public class PolygonSelection extends SelectionHandler
{
    private Area selection;
	private Polygon drawnPoly;
	
    PolygonSelection() {
        super();
        selection = new Area();
    }

    public void or(Area r) {

    }


    public void and(Area r) {
		
    }

    public void expand(int amt) {
		
    }

    public void contract(int amt) {
		
    }

	private void createDrawn() {
		drawnPoly = new Polygon();
		
		drawnPoly.invalidate();
	}

    public void draw(Graphics g) {
        g.setColor(new Color(0,0,0));
        g.drawPolygon(drawnPoly);
    }
}
