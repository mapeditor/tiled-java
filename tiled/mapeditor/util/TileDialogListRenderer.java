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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.DefaultListCellRenderer;

import tiled.core.*;


public class TileDialogListRenderer extends DefaultListCellRenderer
{
    private double zoom = 1;

    public TileDialogListRenderer() {
        setOpaque(true);
    }

    public TileDialogListRenderer(double zoom) {
        this();
        this.zoom = zoom;
    }

    public Component getListCellRendererComponent(JList list, Object value,
            int index,  boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);

        Tile tile = (Tile)value;

        if (tile != null) {
            setIcon(new ImageIcon(tile.getScaledImage(zoom)));
            setText("Tile " + tile.getId());
        } else {
            setIcon(null);
            setText("No tile?");
        }

        return this;
    }
}
