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
import java.util.Stack;

import tiled.core.*;

public class SelectionLayer extends MapLayer {
	
	private Color highlightColor;
	private Tile selTile;
	
	public SelectionLayer() {
		super();
		highlightColor = Color.blue;
		selTile = new Tile();
	}
	
	public SelectionLayer(int w, int h) {
		super(w,h);
		highlightColor = Color.blue;
		selTile = new Tile();
	}
	
	public Rectangle getSelectedArea() {
		
		Point first = locationOf(selTile);
		
		Rectangle area = new Rectangle(first); 
		Stack stack = new Stack();

		stack.push(new Point(first.x, first.y));
		while (!stack.empty()) {
			// Remove the next tile from the stack
			Point p = (Point)stack.pop();

			// If the tile it meets the requirements, set it and push its
			// neighbouring tiles on the stack.
			if (contains(p.x, p.y) &&
					getTileAt(p.x, p.y) == selTile)
			{
				area.add(p);

				stack.push(new Point(p.x, p.y - 1));
				stack.push(new Point(p.x, p.y + 1));
				stack.push(new Point(p.x + 1, p.y));
				stack.push(new Point(p.x - 1, p.y));
			}
		}
		
		return area;
	}
	
	public boolean isSelected(int tx, int ty) {
		return (getTileAt(tx,ty) !=null); 
	}
	
	public void select(int tx, int ty) {
		setTileAt(tx,ty,selTile);
	}
	
	public void setHighlightColor(Color c) {
		highlightColor = c;
	}
	
	public Color getHighlightColor() {
		return highlightColor;
	}
}
