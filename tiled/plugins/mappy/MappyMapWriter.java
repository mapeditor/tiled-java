package mappy;

import java.io.*;

import tiled.io.MapWriter;
import tiled.core.Map;
import tiled.core.TileSet;

public class MappyMapWriter implements MapWriter {

    /**
     * Loads a map from a file.
     *
     * @param filename the filename of the map file
     */
    public void writeMap(Map map, String filename) throws IOException {
	System.out.println("Asked to write map "+filename);
    }
                                                                                
    /**
     * Loads a tileset from a file.
     *
     * @param filename the filename of the tileset file
     */
    public void writeTileset(TileSet set, String filename) throws IOException {
	System.out.println("Asked to write "+filename);
    }

    /**
     * @see tiled.io.MapReader#getFilter()
     */
    public String getFilter() throws Exception {
        return "*.map";
    }
                                                                                
    public String getDescription() {
        return "+---------------------------------------------+\n| A sloppy writer for Mappy FMAP (v0.36) |\n|      (c) Adam Turk 2004               |\n|          aturk@biggeruniverse.com               |\n+-------------------------------------------+";
    }
                                                                                
    public String getPluginPackage() {
        return "Mappy Reader/Writer Plugin";
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

}
