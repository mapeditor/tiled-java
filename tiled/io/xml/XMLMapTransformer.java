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

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.*;
import java.lang.reflect.*;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import tiled.core.*;
import tiled.io.ImageHelper;
import tiled.io.MapReader;
import tiled.util.*;


public class XMLMapTransformer implements MapReader
{
    private Map map = null;
    private Document doc;
    private Component mediaComponent;
    private MediaTracker mediaTracker;
    private String xmlPath = null;

    public XMLMapTransformer() {
        mediaComponent = new Canvas();
        mediaTracker = new MediaTracker(mediaComponent);
    }

    private int reflectFindMethodByName(Class c, String methodName) {
        Method[] methods = c.getMethods();
        //System.out.println("Searching for " + methodName);
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equalsIgnoreCase(methodName)) {
                return i;
            }
        }
        return -1;
    }

    private void reflectInvokeMethod(Object invokeVictim, Method method,
            String[] args) throws InvocationTargetException, Exception {
        Class[] parameterTypes = method.getParameterTypes();
        Object[] conformingArguments = new Object[parameterTypes.length];

        if (args.length < parameterTypes.length) {
            throw new Exception("Insufficient arguments were supplied");
        }

        //TODO: is there a better way to do this?
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].getName().equalsIgnoreCase("int")) {
                conformingArguments[i] = new Integer(args[i]);
            } else if (parameterTypes[i].getName().equalsIgnoreCase("float")) {
                conformingArguments[i] = new Float(args[i]);
            } else if (parameterTypes[i].getName().equalsIgnoreCase("string")) {
                conformingArguments[i] = args[i];
            } else if (parameterTypes[i].getName().equalsIgnoreCase("boolean")) {
                conformingArguments[i] = new Boolean(args[i]);
            } else {
                //TODO: is it necessary to warn anyone that we're defaulting?
                conformingArguments[i] = args[i];
            }
        }

        method.invoke(invokeVictim,conformingArguments);
    }

    private void setOrientation(String o) {
        if (o.equalsIgnoreCase("isometric")) {
            map.setOrientation(Map.MDO_ISO);
        } else if (o.equalsIgnoreCase("orthogonal")) {
            map.setOrientation(Map.MDO_ORTHO);
        } else if (o.equalsIgnoreCase("hexagonal")) {
            map.setOrientation(Map.MDO_HEX);
        } else if (o.equalsIgnoreCase("oblique")) {
            map.setOrientation(Map.MDO_OBLIQUE);
        }
    }

    private String getAttributeValue(Node node, String attribname) {
        NamedNodeMap attributes = node.getAttributes();
        String att = null;
        if (attributes != null) {
            Node attribute = attributes.getNamedItem(attribname);
            if (attribute != null) {
                att = attribute.getNodeValue();
            }
        }
        return att;
    }

    private Node getChildNode(Node n, String name) {
        NodeList children = n.getChildNodes();
		Node child = null;
        for (int i = 0; i < children.getLength(); i++) {
             child = children.item(i);
            if (child.getNodeName().equalsIgnoreCase(name)) {
                break;
            }
        }

        return child;
    }

    private Object unmarshalClass(Class reflector, Node node)
        throws InstantiationException, IllegalAccessException,
               InvocationTargetException {
        Constructor cons = null;
		try {
			cons = reflector.getConstructor(null);
		} catch (SecurityException e1) {			
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {			
			e1.printStackTrace();
			return null;
		}
        Object o = cons.newInstance(null);
        Node n;

        Method[] methods = reflector.getMethods();
        NamedNodeMap nnm = node.getAttributes();

        if (nnm != null) {
            for (int i = 0; i < nnm.getLength(); i++) {
                n = nnm.item(i);

                try {
                    int j = reflectFindMethodByName(reflector,
                            "set" + n.getNodeName());
                    if (j >= 0) {
                        reflectInvokeMethod(o,methods[j],
                                new String [] {n.getNodeValue()});
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return o;
    }

    private Image unmarshalImage(Node t) {
        Image img = null;

        String source = getAttributeValue(t, "source");
        if (source != null) {
            img = Toolkit.getDefaultToolkit().createImage(xmlPath + source);
        } else {
            NodeList nl = t.getChildNodes();

            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n.getNodeName().equals("data")) {
                    Node cdata = n.getChildNodes().item(0);
                    String sdata = cdata.getNodeValue();
                    img = ImageHelper.bytesToImage(
                            Base64.decode(sdata.trim().toCharArray()));

                    break;
                }
            }
        }

        // Wait for the image to be ready
        mediaTracker.addImage(img, 0);
        try {
            mediaTracker.waitForID(0);
        }
        catch (InterruptedException ie) {
            System.err.println(ie);
        }
        mediaTracker.removeImage(img);

        return img;
    }

    private TileSet unmarshalTilesetFile(String filename) throws Exception {
        TileSet set = null;
        Node tsNode;
        Document tsDoc = null;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            tsDoc = builder.parse(new File(filename));
        } catch (FileNotFoundException fnf) {
            // TODO: Have a popup and ask the user to browse to the file...
            throw new Exception("Where else can I look?");
        }

        String xmlPathSave = xmlPath;
        if (filename.indexOf(File.separatorChar) >= 0) {
            xmlPath = filename.substring(0,
                    filename.lastIndexOf(File.separatorChar) + 1);
        }

        NodeList tsNodeList = tsDoc.getElementsByTagName("tileset");

        for (int itr = 0; (tsNode = tsNodeList.item(itr)) != null; itr++) {
            set = unmarshalTileset(tsNode);
            if (set.getSource() != null) {
                throw new Exception(
                        "Recursive external Tilesets are not supported.");
            }
            set.setSource(filename);
            // TODO: This is a deliberate break. multiple tilesets per TSX are
            // not supported yet (maybe never)...
            break;
        }

        xmlPath = xmlPathSave;
        return set;
    }

    private TileSet unmarshalTileset(Node t) throws Exception {
        TileSet set = null;

        try {
            set = (TileSet)unmarshalClass(TileSet.class, t);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String tileWidthStr = getAttributeValue(t, "tilewidth");
        String tileHeightStr = getAttributeValue(t, "tileheight");
        String spacingStr = getAttributeValue(t, "spacing");

        if (set.getSource() != null) {
            TileSet ext = unmarshalTilesetFile(xmlPath+set.getSource());
            ext.setFirstGid(set.getFirstGid());
            return ext;
        } else {
            NodeList children = t.getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeName().equalsIgnoreCase("tile")) {
                    set.addTile(unmarshalTile(child));
                } else if (child.getNodeName().equalsIgnoreCase("image")) {
                    String source = getAttributeValue(child, "source");
                    int tileWidth, tileHeight, spacing = 0;
                    tileWidth = Integer.parseInt(tileWidthStr);
                    tileHeight = Integer.parseInt(tileHeightStr);
                    if (spacingStr != null) {
                        spacing = Integer.parseInt(spacingStr);
                    }

                    File sourceFile = new File(source);
                    String sourcePath;
                    if (sourceFile.getAbsolutePath().equals(source)) {
                        sourcePath = sourceFile.getCanonicalPath();
                    } else {
                        sourcePath =
                            new File(xmlPath + source).getCanonicalPath();
                    }

                    set.importTileBitmap(sourcePath,
                            tileWidth, tileHeight, spacing);

                    // There can be only one image element
                    break;
                }
            }
        }
        return set;
    }

    private Tile unmarshalTile(Node t) throws Exception {
        Tile tile = null;

        try {
            tile = (Tile)unmarshalClass(Tile.class,t);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
		NodeList children = t.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if(child.getNodeName().equalsIgnoreCase("image")) {
				tile.setImage(unmarshalImage(child));
			} else if(child.getNodeName().equalsIgnoreCase("property")) {
				tile.setProperty(getAttributeValue(child,"name"),getAttributeValue(child,"value"));
			} else if(child.getNodeName().equalsIgnoreCase("link")) {
				//TODO: support links
			}
		}

        return tile;
    }

	private MapLayer unmarshalLayer(Node t) throws Exception {
		MapLayer ml = null;
		Rectangle rect = new Rectangle(0,0,map.getWidth(),map.getHeight());
		
		boolean encodedBase64=false;
		try {
            ml = (MapLayer)unmarshalClass(MapLayer.class,t);
        } catch (Exception e) {
            e.printStackTrace();
        }
		
		ml.setBounds(rect);

        NodeList children = t.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equalsIgnoreCase("data")) {
                String encoding = getAttributeValue(child, "encoding");

                if (encoding != null && encoding.equalsIgnoreCase("base64")) {
                    Node cdata = child.getChildNodes().item(0);
                    char[] enc = cdata.getNodeValue().trim().toCharArray();
                    byte[] dec = Base64.decode(enc);
                    ByteArrayInputStream bais = new ByteArrayInputStream(dec);
                    InputStream is;

                    String comp = getAttributeValue(child, "compression");

                    if (comp != null && comp.equalsIgnoreCase("gzip")) {
                        is = new GZIPInputStream(bais);
                    } else {
                        is = bais;
                    }

                    for (int y = 0; y < ml.getHeight(); y++) {
                        for (int x = 0; x < ml.getWidth(); x++) {
                            int tileId = 0;
                            tileId |= is.read();
                            tileId |= is.read() <<  8;
                            tileId |= is.read() << 16;
                            tileId |= is.read() << 24;

                            TileSet ts = map.findTileSetForTileGID(tileId);
                            if (ts != null) {
                            	ml.setTileAt(x, y,
                                        ts.getTile(tileId - ts.getFirstGid()));
                            } else {
                                ml.setTileAt(x, y, map.getNullTile());
                            }
                        }
                    }
                } else {
                    NodeList tilelist = doc.getElementsByTagName("tile");
                    int itr = 0;
                    for (int y = 0; y < ml.getHeight(); y++) {
                        for (int x = 0; x < ml.getWidth(); x++) {
                            Node tileNode = tilelist.item(itr++);
                            Tile tile = unmarshalTile(tileNode);
                            int tileId = tile.getGid();
                            TileSet ts = map.findTileSetForTileGID(tileId);
                            if (ts != null) {
                                ml.setTileAt(x, y,
                                        ts.getTile(tileId - ts.getFirstGid()));
                            } else {
                                ml.setTileAt(x, y, map.getNullTile());
                            }
                        }
                    }
                }

                break;
            }
        }

        return ml;
    }

    private void buildMap(Document doc) throws Exception {
        Node item,mapNode;
        NodeList l,mapNodeList;
        NamedNodeMap nnm;
        Tile tile;
        mapNodeList = doc.getElementsByTagName("map");


        for (int itr = 0; (mapNode = mapNodeList.item(itr)) != null; itr++) {
            // Get the map dimensions and create the map
            l = doc.getElementsByTagName("dimensions");
            for (int i = 0; (item = l.item(i)) != null; i++) {
                if (item.getParentNode() == mapNode) {
                    int mapWidth =
                        Integer.parseInt(getAttributeValue(item, "width"));
                    int mapHeight =
                        Integer.parseInt(getAttributeValue(item, "height"));

                    map = new Map(mapWidth, mapHeight);
                }
            }

            if (map == null) {
                throw new Exception("Couldn't locate map dimensions.");
            }

            // Load other map attributes
            map.setName(getAttributeValue(mapNode,"name"));
            String orientation = getAttributeValue(mapNode, "orientation");
            String tileWidthString = getAttributeValue(mapNode, "tilewidth");
            String tileHeightString = getAttributeValue(mapNode, "tileheight");

            if (tileWidthString != null) {
                int tileWidth = Integer.parseInt(tileWidthString);
                map.setTileWidth(tileWidth);
            }
            if (tileHeightString != null) {
                int tileHeight = Integer.parseInt(tileHeightString);
                map.setTileHeight(tileHeight);
            }

            if (orientation != null) {
                setOrientation(orientation);
            } else {
                throw new Exception("A valid orientation must be given");
            }

            // Load the properties
            l = doc.getElementsByTagName("properties");
            if ((item = l.item(0)) != null && item.getParentNode() == mapNode) {
                NodeList children = item.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    Node prop = children.item(i);
                    map.addProperty(getAttributeValue(prop, "name"),
                            getAttributeValue(prop, "value"));
                }
            }
            // Load the tile sets
            l = doc.getElementsByTagName("tileset");
            for (int i = 0; (item = l.item(i)) != null; i++) {
                if (item.getParentNode() == mapNode) {
                    map.addTileset(unmarshalTileset(item));
                }
            }

            // Load the layers
            l = doc.getElementsByTagName("layer");
            for (int i = 0; (item = l.item(i)) != null; i++) {
                if (item.getParentNode() == mapNode) {
                    map.addLayer(unmarshalLayer(item));
                }
            }

            // TODO: Add object support...
        }
    }

    private Map unmarshal(String xmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(new File(xmlFile));
        } catch (Throwable t) {
            t.printStackTrace();
        }

        // TODO: A problem arrises with xmlPath if the tag sources are absolute
        // path...
        xmlPath = xmlFile.substring(0,
                xmlFile.lastIndexOf(File.separatorChar) + 1);

        buildMap(doc);

        map.setFilename(xmlFile);
        return map;
    }


    // MapReader interface

    public Map readMap(String filename) throws Exception {
        return unmarshal(filename);
    }

    public TileSet readTileset(String filename) throws Exception {
        return unmarshalTilesetFile(filename);
    }
}
