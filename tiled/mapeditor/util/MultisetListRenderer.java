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

package tiled.mapeditor.util;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import javax.swing.*;

import tiled.core.*;
import tiled.mapeditor.MapEditor;
import tiled.core.Map;

public class MultisetListRenderer extends JLabel implements ListCellRenderer
{
	private Map myMap;
	private ImageIcon[] tileImages;
	private Image setImage = null;
	private int highestTileId = 0;
	private double zoom=1;

	public MultisetListRenderer() {
		setOpaque(true);
		try {
			setImage = MapEditor.loadImageResource("resources/source.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public MultisetListRenderer(Map m){
		this();
		myMap = m;
		buildList();
	}

	public MultisetListRenderer(Map m, double zoom) {
		this();
		myMap = m;
		this.zoom=zoom;
		buildList();
	}

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		

		if(value != null && index>=0) {		
			if(value.getClass().toString().equals(Tile.class.toString())) {		
				Tile tile = (Tile)value;
				if (tile != null) {
					setIcon(tileImages[index]);
					setText("Tile " + tile.getId());
				} else {
					setIcon(null);
					setText("");
				}
			} else {
				//Assume it's a set
				TileSet ts = (TileSet)value;
				if(ts != null) {
					setIcon(new ImageIcon(setImage));
					setText("Tileset " + ts.getName());
				} else {
					setIcon(null);
					setText("");
				}
			}
			
			// Draw the correct colors and font
			if (isSelected) {
				// Set the color and font for a selected item
				setBackground(Color.blue);
				setForeground(Color.black);
				setFont(new Font("Roman", Font.BOLD, 12));
			} else {
				// Set the color and font for an unselected item
				setBackground(Color.white);
				setForeground(Color.black);
				setFont(new Font("Roman", Font.PLAIN, 12 ));
			}
		}
		return this;
	}


	private void buildList() {
		Tile t;
		Vector sets = myMap.getTilesets();
		int curSlot = 0;
		Iterator itr = sets.iterator();
		int totalSlots = sets.size();
		
		itr = sets.iterator();
		while(itr.hasNext()) {
			TileSet ts = (TileSet) itr.next();
			totalSlots+=ts.getTotalTiles();
		}
		tileImages = new ImageIcon[totalSlots];
		
		itr = sets.iterator();
		while(itr.hasNext()) {		
			TileSet ts = (TileSet) itr.next();
			tileImages[curSlot++] = new ImageIcon(setImage);
			for (int i = 0; i < ts.getTotalTiles(); i++) {
				t = ts.getTile(i);
				if (t != null) {
					Image img = t.getScaledImage(zoom);
					if (img != null) {
						tileImages[curSlot] = new ImageIcon(img);
					} else {
						tileImages[curSlot] = null;
					}
					curSlot++;
				}
			}
		}
	}
}
