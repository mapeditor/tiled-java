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

import tiled.core.*;
import tiled.util.TiledConfiguration;

public class SelectionLayer extends MapLayer {
	
	private Color highlightColor;
	private Tile selTile;
	private Rectangle selRect;
	
	public SelectionLayer() {
		super();
		
		TiledConfiguration conf = TiledConfiguration.getInstance();
		try{
			highlightColor = Color.decode(conf.getValue("tiled.selection.color"));
		}catch(Throwable e) {
			highlightColor = Color.blue;
		}
		
		selTile = new Tile();
	}
	
	public SelectionLayer(int w, int h) {
		super(w,h);
		
		TiledConfiguration conf = TiledConfiguration.getInstance();
		try{
			highlightColor = Color.decode(conf.getValue("tiled.selection.color"));
		}catch(Throwable e) {
			highlightColor = Color.blue;
		}
		
		selTile = new Tile();
	}
	
	public Rectangle getSelectedArea() {
		
		return selRect;
	}
	
	public boolean isSelected(int tx, int ty) {
		return (getTileAt(tx,ty) !=null); 
	}
	
	public void selectRegion(Rectangle region) {
		if(selRect != null)
			fillRegion(selRect, null);
		selRect = region;
		fillRegion(region, selTile);
	}
	
	public void select(int tx, int ty) {
		setTileAt(tx,ty,selTile);
		if(selRect == null) {
			selRect = new Rectangle(tx,ty,1,1);
		}else{
			selRect.add(tx,ty);
		}
	}
	
	public void setHighlightColor(Color c) {
		highlightColor = c;
	}
	
	public Color getHighlightColor() {
		return highlightColor;
	}
	
	private void fillRegion(Rectangle region, Tile fill) {
		for(int i = region.y;i<region.y+region.height;i++) {
			for(int j = region.x;j<region.x+region.width;j++) {
				setTileAt(j,i,fill);
			}
		}
	}
}
