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
