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
import java.util.HashMap;
import javax.swing.*;

import tiled.core.Tile;
import tiled.core.TileSet;
import tiled.mapeditor.MapEditor;

/**
 * This list renderer is used for rendering a list of tiles associated with a
 * certain map. The list renderer produces {@link ImageIcon} instances on
 * demand and caches them in a {@link HashMap}.
 *
 * todo: check whether assuming all tiles have an image is safe
 *
 * @version $Id$
 */
public class MultisetListRenderer extends DefaultListCellRenderer {
    private Icon setIcon;
    private final double zoom;
    private final HashMap<Integer, Icon> tileImages;

    /**
     * Creates the list renderer for rendering a list of tiles.
     *
     * @param zoom the zoom level at which the tiles will be shown
     */
    public MultisetListRenderer(double zoom) {
        tileImages = new HashMap<Integer, Icon>();
        this.zoom = zoom;

        try {
            // Load the icon to show for tilesets
            Image img = MapEditor.loadImageResource("resources/source.png");
            setIcon = new ImageIcon(img);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected,
                                                  boolean cellHasFocus) {
        // Let the default list cell renderer do most of the work
        super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);

        // Attempt to set an appropriate icon
        if (value instanceof Tile && index >= 0) {
            Tile tile = (Tile) value;
            if (!isSelected || zoom == 1) {
                // Use cached ImageIcon instance
                if (tileImages.containsKey(index)) {
                    setIcon(tileImages.get(index));
                } else {
                    Icon icon = new ImageIcon(tile.getScaledImage(zoom));
                    setIcon(icon);
                    tileImages.put(index, icon);
                }
            } else {
                // Selected entry always uses unscaled image
                setIcon(new ImageIcon(tile.getImage()));
            }
        } else if (value instanceof TileSet) {
            setIcon(setIcon);
        }

        return this;
    }
}
