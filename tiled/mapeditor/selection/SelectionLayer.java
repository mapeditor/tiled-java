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
import java.awt.geom.Rectangle2D;

import tiled.core.*;
import tiled.util.TiledConfiguration;

public class SelectionLayer extends MapLayer {
	
	private Color highlightColor;
	private Tile selTile;
	private Area selection;
	
	public SelectionLayer() {
		super();
		
		TiledConfiguration conf = TiledConfiguration.getInstance();
		try{
			highlightColor = Color.decode(conf.getValue("tiled.selection.color"));
		}catch(Throwable e) {
			highlightColor = Color.blue;
		}
		
		selTile = new Tile();
		selection = new Area();
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
		selection = new Area();
	}
	
	public Area getSelectedArea() {
		
		return selection;
	}
	
	public Rectangle getSelectedAreaBounds() {
		return selection.getBounds();
	}
	
	public boolean isSelected(int tx, int ty) {
		return (getTileAt(tx,ty) !=null); 
	}
	
	public void subtract(Area area) {
		clearRegion(area);
		selection.subtract(area);
	}
	
	public void selectRegion(Rectangle region) {
		clearRegion(selection);
		selection = new Area(region);
		fillRegion(selection, selTile);
	}
	
	public void select(int tx, int ty) {
		setTileAt(tx,ty,selTile);
		if(selection == null) {
			selection = new Area(new Rectangle2D.Double(tx,ty,1,1));
		}else{
			if(!selection.contains(tx,ty)) {
				selection.add(new Area(new Rectangle2D.Double(tx,ty,1,1)));
			}
		}
	}
	
	public void setHighlightColor(Color c) {
		highlightColor = c;
	}
	
	public Color getHighlightColor() {
		return highlightColor;
	}
	
	private boolean contains(double x, double y) {
		return selection.contains(x, y);
	}
	
	private void fillRegion(Area region, Tile fill) {
		
		Rectangle bounded = region.getBounds();
		for(int i = bounded.y;i<bounded.y+bounded.height;i++) {
			for(int j = bounded.x;j<bounded.x+bounded.width;j++) {
				if(region.contains(j,i)) {
					setTileAt(j,i,fill);
				}
			}
		}
	}
	
	private void clearRegion(Area region) {
		Rectangle bounded = region.getBounds();
		for(int i = bounded.y;i<bounded.y+bounded.height;i++) {
			for(int j = bounded.x;j<bounded.x+bounded.width;j++) {
				if(region.contains(j,i)) {
					setTileAt(j,i,null);
				}
			}
		}
	}
	
	public void invert() {
		
		Rectangle bounded = getBounds();
		
		for(int i = bounded.y;i<bounded.y+bounded.height;i++) {
			for(int j = bounded.x;j<bounded.x+bounded.width;j++) {
				if(selection.contains(j,i)) {
					setTileAt(j,i,null);
				}else{
					setTileAt(j,i,selTile);
				}
			}
		}
		
	}
}
