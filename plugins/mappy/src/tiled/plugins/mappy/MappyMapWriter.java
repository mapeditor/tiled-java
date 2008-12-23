/*
 *  Mappy Plugin for Tiled, (c) 2004-2006
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <bjorn@lindeijer.nl>
 */

package tiled.plugins.mappy;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;

import tiled.io.MapWriter;
import tiled.io.PluginLogger;
import tiled.plugins.mappy.MappyMapReader.BlkStr;
import tiled.core.Map;
import tiled.core.TileSet;

/**
 * A writer for the Mappy map format. Unfinished!
 */
public class MappyMapWriter implements MapWriter
{
    private final LinkedList<Chunk> chunks;

    public MappyMapWriter() {
        chunks = new LinkedList<Chunk>();
    }

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
        System.out.println("Asked to write "+filename);
    }

    public void writeMap(Map map, OutputStream out) throws Exception {
        out.write("FORM".getBytes());
        // TODO: write the size of the file minus this header
        out.write("FMAP".getBytes());
        createMPHDChunk(map);

        // TODO: write all the chunks
    }

    public void writeTileset(TileSet set, OutputStream out) throws Exception {
        System.out.println("Tilesets are not supported!");
    }

    /**
     * @see tiled.io.PluggableMapIO#getFilter()
     */
    public String getFilter() throws Exception {
        return "*.map";
    }

    public String getDescription() {
        return
            "+---------------------------------------------+\n" +
            "|    A sloppy writer for Mappy FMAP (v0.36)   |\n" +
            "|             (c) Adam Turk 2004              |\n" +
            "|          aturk@biggeruniverse.com           |\n" +
            "+---------------------------------------------+";
    }

    public String getPluginPackage() {
        return "Mappy output plugin";
    }

    public String getName() {
        return "Mappy Writer";
    }

    public boolean accept(File pathname) {
        try {
            String path = pathname.getCanonicalPath().toLowerCase();
            if (path.endsWith(".fmp")) {
                return true;
            }
        } catch (IOException e) {}
        return false;
    }

    public void setLogger(PluginLogger logger) {
        // TODO: implement setErrorStack
    }


    private void createMPHDChunk(Map map) throws IOException {
        Chunk chunk = new Chunk("MPHD");
        OutputStream out = chunk.getOutputStream();
        String ver = map.getProperties().getProperty("version");
        if (ver == null || ver.length() < 3) {
            ver = "0.3";                            // default the value
        }
        TileSet set = map.getTilesets().get(0);

        //FIXME
        //out.write(Integer.parseInt(ver.substring(0,ver.indexOf('.')-1)));
        //out.write(Integer.parseInt(ver.substring(ver.indexOf('.')+1)));
        out.write(0);
        out.write(3);
        out.write(1); out.write(0);                 // LSB, reserved
        Util.writeShort(map.getWidth(), out);
        Util.writeShort(map.getHeight(), out);
        out.write(0); out.write(0); out.write(0); out.write(0);     // reserved
        Util.writeShort(map.getTileWidth(), out);
        Util.writeShort(map.getTileHeight(), out);
        Util.writeShort(16, out);                   // tile bitdepth
        Util.writeShort(32, out);                   // blkstr bytewidth
        Util.writeShort(findAllBlocks(map).size(), out);
        Util.writeShort(set.getMaxTileId(), out);

        chunks.add(chunk);
    }

    @SuppressWarnings("unused")
    private void createBKDTChunk(Map map) {
        Chunk chunk = new Chunk("BKDT");
        LinkedList<Object> blocks = findAllBlocks(map);
        Iterator<Object> itr = blocks.iterator();
        while(itr.hasNext()) {
            MappyMapReader.BlkStr b = (BlkStr) itr.next();
            // TODO: write the block
        }
        chunks.add(chunk);
    }

    private LinkedList<Object> findAllBlocks(Map map) {
        // TODO: this
        return null;
    }
}
