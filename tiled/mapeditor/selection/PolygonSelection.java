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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;

public class PolygonSelection extends SelectionHandler
{
    Polygon sel;

    PolygonSelection() {
        super();
        sel = new Polygon();
    }

    public void or(Polygon r) {

    }


    public void and(Polygon r) {
		
    }

    public void expand(int amt) {
		
    }

    public void contract(int amt) {
		
    }

    public void draw(Graphics g) {
        g.setColor(new Color(0,0,0));
        g.drawPolygon(sel);
    }
}
