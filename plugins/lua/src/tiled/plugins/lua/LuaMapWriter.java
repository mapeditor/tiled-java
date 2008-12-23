/*
 *  Lua MapWriter plugin for Tiled.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  William C. Bubel <inmatarian@gmail.com>
 */

package tiled.plugins.lua;

import java.io.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.zip.GZIPOutputStream;

import tiled.io.*;
import tiled.core.*;
import tiled.util.*;
import tiled.mapeditor.selection.SelectionLayer;

/**
 * Exporter for writing maps to a Lua file. Loading maps in Lua could
 * barely be easier!
 */
public class LuaMapWriter implements MapWriter
{
    private static final int LAST_BYTE = 0x000000FF;
    private PluginLogger logger;
    private Writer writer;
    private String indent;

    /**
     * Writes the Indent to file.
     * @throws java.io.IOException
     */
    private void writeIndent() throws IOException {
        writer.write(indent);
    }

    /**
     * Increases the indent by two spaces.
     */
    private void addIndent() {
        indent += "  ";
    }

    /**
     * Decreases the indent by two spaces.
     */
    private void minusIndent() {
        indent = indent.substring(2);
    }

    /**
     * Writes a string to file.
     * @param str the string to be written
     * @throws java.io.IOException
     */
    private void writeString(String str) throws IOException {
        writeIndent();
        writer.write(str);
    }

    /**
     * Writes a string to file, including a newline at its end.
     * @param str the string to be written
     * @throws java.io.IOException
     */
    private void writelnString( String str ) throws IOException {
        writeString( str + "\n" );
    }

    /**
     * Writes a table Key-Value pair.
     * @param hash the key
     * @param value the value
     * @throws java.io.IOException
     */
    private void writelnKeyAndValue(String hash, String value) throws IOException {
        writelnString("[\"" + hash + "\"] = \"" + value + "\";");
    }

    /**
     * Writes a table Key-Value pair.
     * @param hash the key
     * @param value the value
     * @throws java.io.IOException
     */
    private void writelnKeyAndValue(String hash, int value) throws IOException {
        writelnString("[\"" + hash + "\"] = " + String.valueOf(value) + ";");
    }

    /**
     * Writes a table Key-Value pair.
     * @param hash the key
     * @param value the value
     * @throws java.io.IOException
     */
    private void writelnKeyAndValue(String hash, float value) throws IOException {
        writelnString("[\"" + hash + "\"] = " + String.valueOf(value) + ";");
    }

    /**
     * Identified table.
     * @param tablename
     * @throws java.io.IOException
     */
    private void startTable(String tablename) throws IOException {
        writelnString( "[\"" + tablename + "\"] = {");
        addIndent();
    }

    /**
     * Unaliased table
     * @throws java.io.IOException
     */
    private void startTable() throws IOException
    {
        writelnString("{");
        addIndent();
    }

    /**
     * Closes a table.
     * @throws java.io.IOException
     */
    private void endTable() throws IOException
    {
        minusIndent();
        writelnString("};");
    }

    /**
     * Writes a list of properties about a table in a subtable.
     * @param props the list of properties
     * @throws java.io.IOException
     */
    private void writeProperties(Properties props) throws IOException
    {
        if (!props.isEmpty()) {
            startTable( "properties" );
            for (Enumeration<Object> keys = props.keys(); keys.hasMoreElements();) {
                String key = (String)keys.nextElement();
                writelnKeyAndValue( key, props.getProperty(key));
            }
            endTable();
        }
    }

    /**
     * Writes an object.
     * @param m the object to write
     * @throws java.io.IOException
     */
    private void writeObject(MapObject m) throws IOException
    {
        final ObjectGroup o = m.getObjectGroup();
        final Rectangle b = o.getBounds();
        startTable();
        writelnKeyAndValue("label", "object");

        // TODO: The object groups coordinates are in tiles, not pixels
        writelnKeyAndValue("x", m.getX() + b.x);
        writelnKeyAndValue("y", m.getY() + b.y);
        writelnKeyAndValue("type", m.getType());

        if (m.getImageSource().length() > 0) {
            startTable();
            writelnKeyAndValue("label", "image");

            // Relative Path feature put on hold.
            // Works out the Filename part only.
            String imageFilepart = m.getImageSource().substring(
                    m.getImageSource().replace("/","\\").lastIndexOf("\\") + 1);
            writelnKeyAndValue("source", imageFilepart);
            endTable();
        }
        writeProperties(m.getProperties());
        endTable();
    }

    /**
     * Writes an Object Group, feature I haven't used yet.
     * @param o
     * @throws java.io.IOException
     */
    private void writeObjectGroup(ObjectGroup o) throws IOException
    {
        Iterator<MapObject> itr = o.getObjects();
        while (itr.hasNext()) {
            writeObject(itr.next());
        }
    }

    /**
     * Writes a tileset reference.
     * @param set
     * @throws java.io.IOException
     */
    private void writeTilesetReference(TileSet set) throws IOException {
        String source = set.getSource();
        if (source == null) {
            writeTileset(set);
        } else {
            startTable();
            writelnKeyAndValue("label", "tileset");
            try {
                writelnKeyAndValue("firstgid", set.getFirstGid());
                writelnKeyAndValue("source", source.substring(
                            source.lastIndexOf(File.separatorChar) + 1));
                if (set.getBaseDir() != null) {
                    writelnKeyAndValue("basedir", set.getBaseDir());
                }
            } finally {
                endTable();
            }
        }
    }

    /**
     * Writes a tileset, doesn't support embedded tilesets as of yet.
     * @param set
     * @throws java.io.IOException
     */
    private void writeTileset(TileSet set) throws IOException {
        String tilebmpFile = set.getTilebmpFile();
        String name = set.getName();

        startTable();
        writelnKeyAndValue("label", "tileset");

        if (name != null) {
            writelnKeyAndValue("name", name);
        }

        writelnKeyAndValue("firstgid", set.getFirstGid());

        if (tilebmpFile != null) {
            writelnKeyAndValue("tilewidth", set.getTileWidth());
            writelnKeyAndValue("tileheight", set.getTileHeight());

            int tileSpacing = set.getTileSpacing();
            if (tileSpacing != 0) {
                writelnKeyAndValue("spacing", tileSpacing);
            }
        }

        if (set.getBaseDir() != null) {
            writelnKeyAndValue("basedir", set.getBaseDir());
        }

        if (tilebmpFile != null) {
            startTable();
            writelnKeyAndValue("label", "image");

            // Relative Path feature put on hold.
            // Works out the Filename part only.
            String tilebmpFilepart = tilebmpFile.substring( tilebmpFile.replace("/","\\").lastIndexOf("\\") +1 );
            writelnKeyAndValue("source", tilebmpFilepart);

            Color trans = set.getTransparentColor();
            if (trans != null) {
                writelnKeyAndValue("trans", Integer.toHexString(trans.getRGB()).substring(2));
            }
            endTable();

            // Write tile properties when necessary.
            Iterator<Object> tileIterator = set.iterator();

            while (tileIterator.hasNext()) {
                Tile tile = (Tile) tileIterator.next();
                // todo: move the null check back into the iterator?
                if (tile != null && !tile.getProperties().isEmpty()) {
                    startTable();
                    writelnKeyAndValue("label", "tile");
                    writelnKeyAndValue("id", tile.getId());
                    writeProperties(tile.getProperties());
                    endTable();
                }
            }
        }
        else {
            // Embedded tileset
            logger.error("Embedded tilesets are not supported!");
        }
        endTable();
    }

    /**
     * Writes a map layer, support for GZIP included.
     * @param l the map layer
     * @throws java.io.IOException
     */
    private void writeMapLayer(MapLayer l) throws IOException {
        Preferences prefs = TiledConfiguration.node("saving");
        boolean encodeLayerData =
                prefs.getBoolean("encodeLayerData", true);
        boolean compressLayerData =
                prefs.getBoolean("layerCompression", true) &&
                        encodeLayerData;

        Rectangle bounds = l.getBounds();

        startTable(); // Array Indexed table.

        if (l.getClass() == SelectionLayer.class) {
            writelnKeyAndValue("label", "selection");
        } else if (l instanceof ObjectGroup){
            writelnKeyAndValue("label", "objectgroup");
        } else {
            writelnKeyAndValue("label", "layer");
        }

        writelnKeyAndValue("name", l.getName());
        writelnKeyAndValue("width", bounds.width);
        writelnKeyAndValue("height", bounds.height);
        if (bounds.x != 0) {
            writelnKeyAndValue("x", bounds.x);
        }
        if (bounds.y != 0) {
            writelnKeyAndValue("y", bounds.y);
        }

        if (!l.isVisible()) {
            writelnKeyAndValue("visible", "0");
        }
        if (l.getOpacity() < 1.0f) {
            writelnKeyAndValue("opacity", l.getOpacity());
        }

        writeProperties(l.getProperties());

        if (l instanceof ObjectGroup){
            writeObjectGroup((ObjectGroup)l);
        } else {
            startTable("data");
            if (encodeLayerData) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                OutputStream out;

                writelnKeyAndValue("encoding", "base64");

                if (compressLayerData) {
                    writelnKeyAndValue("compression", "gzip");
                    out = new GZIPOutputStream(baos);
                } else {
                    out = baos;
                }

                for (int y = 0; y < l.getHeight(); y++) {
                    for (int x = 0; x < l.getWidth(); x++) {
                        Tile tile = ((TileLayer) l).getTileAt(x + bounds.x,
                                                              y + bounds.y);
                        int gid = 0;

                        if (tile != null) {
                            gid = tile.getGid();
                        }

                        out.write(gid       & LAST_BYTE);
                        out.write(gid >> 8  & LAST_BYTE);
                        out.write(gid >> 16 & LAST_BYTE);
                        out.write(gid >> 24 & LAST_BYTE);
                    }
                }

                if (compressLayerData) {
                    ((GZIPOutputStream)out).finish();
                }

                writelnKeyAndValue("content", new String(Base64.encode(baos.toByteArray())) );
            } else {
                for (int y = 0; y < l.getHeight(); y++) {
                    for (int x = 0; x < l.getWidth(); x++) {
                        Tile tile = ((TileLayer)l).getTileAt(x, y);
                        int gid = 0;

                        if (tile != null) {
                            gid = tile.getGid();
                        }

                        startTable();
                        writelnKeyAndValue("label", "tile");
                        writelnKeyAndValue("gid", gid);
                        endTable();
                    }
                }
            }
            endTable();
        }
        endTable();
    }


    /**
     * Saves a map to a file.
     *
     * @param map the map to be saved
     * @param filename the filename of the map file
     * @throws java.io.IOException
     */
    public void writeMap(Map map, String filename) throws IOException
    {
        writeMap(map, new FileOutputStream(filename));
    }

    /**
     * Writes a map to an already opened stream. Useful
     * for maps which are part of a larger binary dataset
     *
     * @param map the Map to be written
     * @param out the output stream to write to
     * @throws java.io.IOException
     */
    public void writeMap(Map map, OutputStream out) throws IOException {
        writer = new OutputStreamWriter(out);
        indent = "";

        writelnString("-- Generated by Tiled's Lua Exporter Plugin.");
        writeString("map = ");
        startTable();
        writelnKeyAndValue("label", "map");

        // Map version
        writelnKeyAndValue("version", "0.99b");
        writelnKeyAndValue("luaversion", "5.1");

        // Orientation of the map
        switch (map.getOrientation()) {
            case Map.MDO_ORTHO:   writelnKeyAndValue("orientation", "orthogonal"); break;
            case Map.MDO_ISO:     writelnKeyAndValue("orientation", "isometric");  break;
            case Map.MDO_HEX:     writelnKeyAndValue("orientation", "hexagonal");  break;
            case Map.MDO_SHIFTED: writelnKeyAndValue("orientation", "shifted");    break;
        }

        // Basic Map Properties
        writelnKeyAndValue("width",      map.getWidth());
        writelnKeyAndValue("height",     map.getHeight());
        writelnKeyAndValue("tilewidth",  map.getTileWidth());
        writelnKeyAndValue("tileheight", map.getTileHeight());

        writeProperties(map.getProperties());

        startTable("tilesets");
        int firstgid = 1;
        Iterator<TileSet> itr = map.getTilesets().iterator();
        while (itr.hasNext()) {
            TileSet tileset = itr.next();
            tileset.setFirstGid(firstgid);
            writeTilesetReference(tileset);
            firstgid += tileset.getMaxTileId() + 1;
        }
        endTable();

        startTable("layers");
        Iterator<MapLayer> ml = map.getLayers();
        while (ml.hasNext()) {
            MapLayer layer = ml.next();
            writeMapLayer(layer);
        }
        endTable();

        endTable();
        writelnString("-- EOF");

        writer.flush();
        writer = null;
    }

    /**
     * Overload this to write a tileset to an open stream. Tilesets are not
     * supported by this writer.
     *
     * @param set
     * @param out
     * @throws Exception
     */
    public void writeTileset(TileSet set, OutputStream out) throws Exception {
        logger.error("Tilesets are not supported!");
    }

    /**
     * Saves a tileset to a file. Tilesets are not supported by this writer.
     *
     * @param set
     * @param filename the filename of the tileset file
     * @throws Exception
     */
    public void writeTileset(TileSet set, String filename) throws Exception {
        logger.error("Tilesets are not supported!");
        logger.error("(asked to write " + filename + ")");
    }

    /**
     * java.io.FileFilter Interface
     */
    public boolean accept(File pathname) {
        try {
            String path = pathname.getCanonicalPath();
            if (path.endsWith(".lua")) {
                return true;
            }
        } catch (IOException e) {}
        return false;
    }

    /**
     * Lists supported file extensions. This function is used by the editor to
     * find the plugin to use for a specific file extension.
     *
     * @return a comma delimited string of supported file extensions
     * @throws Exception
     */
    public String getFilter() throws Exception {
        return "*.lua";
    }

    /**
     * Returns a short description of the plugin, or the plugin name. This
     * string is displayed in the list of loaded plugins under the Help menu in
     * Tiled.
     *
     * @return a short name or description
     */
    public String getName() {
        return "Tiled Lua exporter";
    }

    /**
     * Returns a long description (no limit) that details the plugin's
     * capabilities, author, contact info, etc.
     *
     * @return a long description of the plugin
     */
    public String getDescription() {
        return
            "Lua Table Export Plugin\n" +
            "(c) 2007 William C. Bubel\n" +
            "inmatarian@gmail.com\n" +
            "Released under the terms of the GPLv2.\n";
    }

    /**
     * Returns the base Java package string for the plugin
     *
     * @return String the base package of the plugin
     */
    public String getPluginPackage() {
        return "Tiled Lua Writer";
    }

    /**
     * The PluginLogger object passed by the editor when the plugin is called to load
     * or save a map can be used by the plugin to notify the user of any
     * problems or messages.
     *
     * @param logger
     */
    public void setLogger(PluginLogger logger) {
        this.logger = logger;
    }
}
