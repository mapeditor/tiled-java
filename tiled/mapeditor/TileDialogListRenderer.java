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

package tiled.mapeditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import tiled.core.*;


public class TileDialogListRenderer extends JLabel
    implements ListCellRenderer
{
    private TileSet myTileSet;
    private ImageIcon [] tileImages;
    private int highestTileId = 0;

    public TileDialogListRenderer() {
        setOpaque(true);
    }

    public TileDialogListRenderer(TileSet s) {
        setOpaque(true);
        myTileSet = s;
        loadTilesList();
    }

    public Component getListCellRendererComponent(
            JList list, Object value, int index,  boolean isSelected,
            boolean cellHasFocus) {
        
		Tile tile = (Tile)value;
		
		if(tile != null) {
	        setIcon(new ImageIcon(tile.getImage()));
	        if (value != null) {
	        	
	            setText("Tile" + tile.getId());
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
		} else {
			setIcon(null);
			setText("");
		}
        return this;
    }

    private void loadTilesList() {
        Tile t;
        int curSlot = 0;
        int totalTiles = myTileSet.getTotalTiles();

        if (highestTileId == totalTiles) {
            return;
        }

        highestTileId = totalTiles;
        tileImages = new ImageIcon[highestTileId];

        for (int i = 0; i < totalTiles; i++) {
            t = myTileSet.getTile(i);
            if (t != null) {
                Image img = t.getImage();
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
