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

package tiled.io.xml;

import java.awt.Image;
import java.awt.Rectangle;
import java.io.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import tiled.core.*;
import tiled.io.*;
import tiled.mapeditor.selection.SelectionLayer;
import tiled.util.*;


public class XMLMapWriter implements MapWriter
{
    /**
     * Saves a map to an XML file.
     *
     * @param filename the filename of the map file
     */
    public void writeMap(Map map, String filename) throws IOException {
        FileOutputStream os = new FileOutputStream(filename);
        Writer writer = new OutputStreamWriter(os);
        XMLWriter xmlWriter = new XMLWriter(writer);

        xmlWriter.startDocument();
        writeMap(map, xmlWriter, filename);
        xmlWriter.endDocument();

        writer.flush();
    }

    /**
     * Saves a tileset to an XML file.
     *
     * @param filename the filename of the tileset file
     */
    public void writeTileset(TileSet set, String filename) throws IOException {
        FileOutputStream os = new FileOutputStream(filename);
        Writer writer = new OutputStreamWriter(os);
        XMLWriter xmlWriter = new XMLWriter(writer);

        xmlWriter.startDocument();
        writeTileset(set, xmlWriter, filename);
        xmlWriter.endDocument();

        writer.flush();
    }

    private void writeMap(Map map, XMLWriter w, String wp) throws IOException {
        try {
            w.startElement("map");

            if (map.getName() != null) {
                w.writeAttribute("name", map.getName());
            }
            //w.writeAttribute("version", major_rev + "." + minor_rev);
            //w.writeAttribute("id", "" + mapId);

            switch (map.getOrientation()) {
                case Map.MDO_ORTHO:
                    w.writeAttribute("orientation", "orthogonal"); break;
                case Map.MDO_ISO:
                    w.writeAttribute("orientation", "isometric"); break;
                case Map.MDO_OBLIQUE:
                    w.writeAttribute("orientation", "oblique"); break;
                case Map.MDO_HEX:
                    w.writeAttribute("orientation", "hexagonal"); break;
            }

            w.writeAttribute("width", "" + map.getWidth());
            w.writeAttribute("height", "" + map.getHeight());
            w.writeAttribute("tilewidth", "" + map.getTileWidth());
            w.writeAttribute("tileheight", "" + map.getTileHeight());

            Enumeration keys = map.getProperties();
            while(keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                w.startElement("property");
                w.writeAttribute("name", key);
                w.writeAttribute("value", map.getPropertyValue(key));
                w.endElement();
            }

            int firstgid = 1;
            Iterator itr = map.getTilesets().iterator();
            while (itr.hasNext()) {
                TileSet tileset = (TileSet)itr.next();
                tileset.setFirstGid(firstgid);
                writeTileset(tileset, w, wp);
                firstgid += tileset.getTotalTiles();
            }

            Iterator ml = map.getLayers();
            while (ml.hasNext()) {
                MapLayer layer = (MapLayer)ml.next();
                writeMapLayer(layer, w);
            }

            w.endElement();
        } catch (XMLWriterException e) {
            e.printStackTrace();
        }
    }

    private void writeTileset(TileSet set, XMLWriter w, String wp)
        throws IOException {

        try {
            w.startElement("tileset");
            w.writeAttribute("firstgid", "" + set.getFirstGid());

            String source = set.getSource();
            String tilebmpFile = set.getTilebmpFile();

            if (set.getName() != null && source == null) {
                w.writeAttribute("name", set.getName());
            }

            if (tilebmpFile != null) {
                w.writeAttribute("tilewidth", "" + set.getStandardWidth());
                w.writeAttribute("tileheight", "" + set.getStandardHeight());
                //w.writeAttribute("spacing", "0");
            }

            if (source != null) {
                // External tileset
                w.writeAttribute("source", source.substring(
                            source.lastIndexOf(File.separatorChar) + 1));
            } else if (tilebmpFile != null) {
                // Reference to tile bitmap

                w.startElement("image");
                w.writeAttribute("source", getRelativePath(wp, tilebmpFile));
                w.endElement();
            } else {
                // Embedded tileset
                /*
                if (setImage != null) {
                    w.startElement("image");
                    if (TiledConfiguration.keyHasValue(
                                "tmx.save.tileSetImages", "1") &&
                            TiledConfiguration.keyHasValue(
                                "tmx.save.embedtileSetImages", "1")) {
                        w.writeAttribute("format", "png");
                        w.startElement("data");
                        w.writeAttribute("encoding", "base64");
                        w.writeCDATA(new String(Base64.encode(
                                        ImageHelper.imageToPNG(setImage))));
                        w.endElement();
                    } else {
                        if (externalImageSource == null ) {
                            String source =	TiledConfiguration.getValue(
                                    "tmx.save.tileImagePrefix") + "set.png";
                            w.writeAttribute("source", source);
                            FileOutputStream fw = new FileOutputStream(new File(
                                        TiledConfiguration.getValue(
                                            "tmx.save.maplocation") + source));
                            byte[] data = ImageHelper.imageToPNG(setImage);
                            fw.write(data, 0, data.length);
                            fw.close();
                        } else {
                            w.writeAttribute("source", externalImageSource);
                        }
                    }
                    w.endElement();
                }
                */
                // If not a set bitmap, tiles handle their own images...

                int totalTiles = set.getTotalTiles();

                for (int i = 0; i < totalTiles; i++) {
                    Tile tile = set.getTile(i);
                    if (tile != null) {
                        writeTile(tile, w);
                    }
                }
            }
            w.endElement();
        } catch (XMLWriterException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes this layer to an XMLWriter. This should be done <b>after</b> the
     * first global ids for the tilesets are determined, in order for the right
     * gids to be written to the layer data.
     */
    private void writeMapLayer(MapLayer l, XMLWriter w) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStream out;
            boolean encodeLayerData = TiledConfiguration.keyHasValue(
                    "tmx.save.encodeLayerData", "1");
            boolean compressLayerData = TiledConfiguration.keyHasValue(
                    "tmx.save.layerCompression", "1") && encodeLayerData;


            Rectangle bounds = l.getBounds();

			if(l.getClass() == SelectionLayer.class) {
				w.startElement("selection");
			} else {
            	w.startElement("layer");
			}
            //w.writeAttribute("id", "" + l.getId());
            w.writeAttribute("name", l.getName());
            if (bounds.x != 0) {
                w.writeAttribute("xoffset", "" + bounds.x);
            }
            if (bounds.y != 0) {
                w.writeAttribute("yoffset", "" + bounds.y);
            }

            if (!l.isVisible()) {
                w.writeAttribute("visible", "0");
            }
            if (l.getOpacity() < 1.0f) {
                w.writeAttribute("opacity", "" + l.getOpacity());
            }

			w.startElement("data");
            if (encodeLayerData) {
                w.writeAttribute("encoding", "base64");

                if (compressLayerData) {
                    w.writeAttribute("compression", "gzip");
                    out = new GZIPOutputStream(baos);
                } else {
                    out = baos;
                }

                for (int y = 0; y < l.getHeight(); y++) {
                    for (int x = 0; x < l.getWidth(); x++) {
                        Tile tile = l.getTileAt(x, y);
                        int gid = 0;

                        if (tile != null) {
                            gid = tile.getGid();
                        }

                        out.write((gid      ) & 0x000000FF);
                        out.write((gid >>  8) & 0x000000FF);
                        out.write((gid >> 16) & 0x000000FF);
                        out.write((gid >> 24) & 0x000000FF);
                    }
                }

                if (compressLayerData) {
                    ((GZIPOutputStream)out).finish();
                }

                w.writeCDATA(new String(Base64.encode(baos.toByteArray())));
            } else {
                for (int y = 0; y < l.getHeight(); y++) {
                    for (int x = 0; x < l.getWidth(); x++) {
                        Tile tile = l.getTileAt(x, y);
                        int gid = 0;

                        if (tile != null) {
                            gid = tile.getGid();
                        }

                        w.startElement("tile");
                        w.writeAttribute("gid", ""+gid);
                        w.endElement();
                    }
                }                
            }
			w.endElement();
            w.endElement();
        } catch (XMLWriterException e) {
            e.printStackTrace();
        }
    }

    private void writeTile(Tile tile, XMLWriter w) throws IOException {
        try {
            w.startElement("tile");

            int tileId = tile.getId();

            w.writeAttribute("id", "" + tileId);
            if (tile.getName() != null) {
                w.writeAttribute("name", tile.getName());
            }

            //if (groundHeight != getHeight()) {
            //    w.writeAttribute("groundheight", "" + groundHeight);
            //}

            Enumeration keys = tile.getProperties();
            while(keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                w.startElement("property");
                w.writeAttribute("name", key);
                w.writeAttribute("value", tile.getPropertyValue(key));
                w.endElement();
            }

            Image tileImage = tile.getImage();

            // Write encoded data
            if (tileImage != null && !TiledConfiguration.getValue(
                        "tmx.save.tileSetImages").equals("1")) {		
                if (TiledConfiguration.keyHasValue(
                            "tmx.save.embedImages", "1")) {
                    w.startElement("image");
                    w.writeAttribute("format", "png");
                    w.startElement("data");
                    w.writeAttribute("encoding", "base64");
                    w.writeCDATA(new String(Base64.encode(
                                    ImageHelper.imageToPNG(tileImage))));
                    w.endElement();
                    w.endElement();	
                } else {
                    String prefix = TiledConfiguration.getValue(
                            "tmx.save.tileImagePrefix");
                    String filename = TiledConfiguration.getValue(
                            "tmx.save.maplocation") + prefix + tileId + ".png";
                    w.startElement("image");
                    w.writeAttribute("source", prefix + tileId + ".png");
                    FileOutputStream fw = new FileOutputStream(
                            new File(filename));
                    byte[] data = ImageHelper.imageToPNG(tileImage);
                    fw.write(data, 0, data.length);
                    fw.close();
                    w.endElement();
                }        
                        }

            w.endElement();
        } catch (XMLWriterException e) {
            e.printStackTrace();
        }
    }

    private void writeObject(MapObject m, XMLWriter w) throws IOException {
        try {
            w.startElement("object");
            w.writeAttribute("name", m.getName());
            w.writeAttribute("x", "" + m.getX());
            w.writeAttribute("y", "" + m.getY());
            w.writeAttribute("source", m.getSource());

            Enumeration keys = m.getProperties();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                w.startElement("property");
                w.writeAttribute("name", key);
                w.writeAttribute("value", m.getPropertyValue(key));
                w.endElement();
            }

            w.endElement();
        } catch (XMLWriterException e) {
            e.printStackTrace();
        }
    }

    public static String getRelativePath(String from, String to) {
        // Make the two paths absolute and unique
        try {
            from = new File(from).getCanonicalPath();
            to = new File(to).getCanonicalPath();
        } catch (IOException e) {
        }

        File fromFile = new File(from);
        File toFile = new File(to);
        Vector fromParents = new Vector();
        Vector toParents = new Vector();

        // Iterate to find both parent lists
        while (fromFile != null) {
            fromParents.add(0, fromFile.getName());
            fromFile = fromFile.getParentFile();
        }
        while (toFile != null) {
            toParents.add(0, toFile.getName());
            toFile = toFile.getParentFile();
        }

        // Iterate while parents are the same
        int shared = 0;
        int maxShared = Math.min(fromParents.size(), toParents.size());
        for (shared = 0; shared < maxShared; shared++) {
            String fromParent = (String)fromParents.get(shared);
            String toParent = (String)toParents.get(shared);
            if (!fromParent.equals(toParent)) {
                break;
            }
            shared++;
        }

        // Append .. for each remaining parent in fromParents
        String relPath = "";
        for (int i = shared; i < fromParents.size() - 1; i++) {
            relPath += ".." + File.separator;
        }

        // Add the remaining part in toParents
        for (int i = shared; i < toParents.size() - 1; i++) {
            relPath += toParents.get(i) + File.separator;
        }
        relPath += new File(to).getName();

        return relPath;
    }

	/**
	 * @see tiled.io.MapReader#getFilter()
	 */
	public String getFilter() throws Exception {
		return "*.tmx,*.tsx";
	}

	public String getName() {
		return "Default Tiled XML map writer";
	}

	public String getDescription() {
		return "This is the core Tiled TMX format writer\n\nTiled Map Editor, (c) 2004\nAdam Turk\nBjorn Lindeijer";
	}
	
	public boolean accept(File pathname) {
		try {
			if(pathname.getCanonicalPath().endsWith("tmx")||pathname.getCanonicalPath().endsWith("tsx")){
				return true;
			}
		} catch (IOException e) {}
		return false;
	}
}
