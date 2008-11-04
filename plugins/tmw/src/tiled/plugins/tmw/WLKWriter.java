/*
 *  The Mana World Plugin for Tiled, (c) 2004-2006
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <bjorn@lindeijer.nl>
 */

package tiled.plugins.tmw;

import java.io.*;

import tiled.core.*;

/**
 * The WLK file writer. The format is very simple:
 *
 * <pre>
 *  short (width)
 *  short (height)
 *  char[] (data)
 * </pre>
 *
 * @version $Id$
 */
public class WLKWriter
{
    private static final int FIRST_BYTE = 0x000000FF;

    public static void writeMap(Map map, OutputStream out) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        MapLayer layer = null;

        // Get the last "collision" layer
        for (MapLayer mapLayer : map) {
            if (mapLayer.getName().equalsIgnoreCase("collision")) {
                layer = mapLayer;
            }
        }

        if (layer != null && layer instanceof TileLayer) {
            int width = layer.getWidth();
            int height = layer.getHeight();

            // Write width and height
            out.write(width       & FIRST_BYTE);
            out.write(width >> 8  & FIRST_BYTE);
            out.write(height      & FIRST_BYTE);
            out.write(height >> 8 & FIRST_BYTE);

            for (int y = 0; y < height; y++) {
                for (int x= 0; x < width; x++) {
                    Tile tile = ((TileLayer) layer).getTileAt(x, y);
                    if (tile != null && tile.getId() > 0) {
                        out.write(1);
                    } else {
                        out.write(0);
                    }
                }
            }

            baos.writeTo(out);
        } else {
            throw new Exception("No collision layer found!");
        }
    }
}
