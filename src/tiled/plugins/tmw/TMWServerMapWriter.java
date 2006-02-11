/*
 *  The Mana World Plugin for Tiled, (c) 2004-2006
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 * 
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <b.lindeijer@xs4all.nl>
 */

package tiled.plugins.tmw;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import tiled.io.MapWriter;
import tiled.plugins.mappy.MappyMapReader.BlkStr;
import tiled.core.*;

/**
 * An exporter for TMW server map files, used to determine where a character
 * can walk. The format is very simple:
 *
 * <pre>
 *  short (width)
 *  short (height)
 *  char[] (data)
 * </pre>
 */
public class TMWServerMapWriter implements MapWriter
{
    private static final int FIRST_BYTE = 0x000000FF;

    /**
     * Loads a map from a file.
     *
     * @param filename the filename of the map file
     */
    public void writeMap(Map map, String filename) throws Exception {
        writeMap(map, new FileOutputStream(filename));
    }

    /**
     * Loads a tileset from a file.
     *
     * @param filename the filename of the tileset file
     */
    public void writeTileset(TileSet set, String filename) throws Exception {
        System.out.println("Tilesets are not supported!");
        System.out.println("(asked to write " + filename + ")");
    }

    public void writeMap(Map map, OutputStream out) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        MapLayer layer = map.getLayer(3);
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
                    Tile tile = ((TileLayer)layer).getTileAt(x, y);
                    if (tile != null && tile.getId() > 0) {
                        out.write(1);
                    } else {
                        out.write(0);
                    }
                }
            }

            baos.writeTo(out);
        } else {
            throw new Exception("No collision layer 4 found!");
        }
    }
    
    public void writeTileset(TileSet set, OutputStream out) throws Exception {
        System.out.println("Tilesets are not supported!");
    }
    
    /**
     * @see tiled.io.MapReader#getFilter()
     */
    public String getFilter() throws Exception {
        return "*.wlk";
    }
                                                                                
    public String getDescription() {
        return
            "+---------------------------------------------+\n" +
            "|    An exporter for The Mana World server    |\n" +
            "|                  map files.                 |\n" +
            "|          (c) 2005 Bjorn Lindeijer           |\n" +
            "|              bjorn@lindeijer.nl             |\n" +
            "+---------------------------------------------+";
    }
                                                                                
    public String getPluginPackage() {
        return "The Mana World export plugin";
    }

    public String getName() {
        return "The Mana World exporter";
    }
                                                                                
    public boolean accept(File pathname) {
        try {
            String path = pathname.getCanonicalPath().toLowerCase();
            if (path.endsWith(".wlk")) {
                return true;
            }
        } catch (IOException e) {}
        return false;
    }

    public void setErrorStack(Stack es) {
        // TODO: implement setErrorStack
    }
}
