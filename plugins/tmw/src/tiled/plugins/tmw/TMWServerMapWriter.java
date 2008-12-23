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

import tiled.io.MapWriter;
import tiled.io.PluginLogger;
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
 *
 * @version $Id$
 */
public class TMWServerMapWriter implements MapWriter
{
    @SuppressWarnings("unused")
    private static final int FIRST_BYTE = 0x000000FF;

    private PluginLogger logger;

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
        logger.error("Tilesets are not supported!");
        logger.error("(asked to write " + filename + ")");
    }

    public void writeMap(Map map, OutputStream out) throws Exception {
        WLKWriter.writeMap(map, out);
    }

    public void writeTileset(TileSet set, OutputStream out) throws Exception {
        System.out.println("Tilesets are not supported!");
    }

    /**
     * @see tiled.io.PluggableMapIO#getFilter()
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

    public void setLogger(PluginLogger logger) {
        this.logger = logger;
    }
}
