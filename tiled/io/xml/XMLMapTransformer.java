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
import org.xml.sax.SAXException;

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

    private int getAttribute(Node node, String attribname, int def) {
        String attr = getAttributeValue(node, attribname);
        if (attr != null) {
            return Integer.parseInt(attr);
        } else {
            return def;
        }
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

        if(getAttributeValue(t, "set") != null) {
        	TileSet ts = (TileSet) map.getTilesets().get(Integer.parseInt(getAttributeValue(t, "set")));
        	if(ts != null) {
        		ts.addImage(img);
        	}
        }
        
        return img;
    }

    private TileSet unmarshalTilesetFile(InputStream in) throws Exception {
        TileSet set = null;
        Node tsNode;
        Document tsDoc = null;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            tsDoc = builder.parse(in, ".");
        } catch (FileNotFoundException fnf) {
            // TODO: Have a popup and ask the user to browse to the file...
            throw new Exception("Where else can I look?");
        }

        String xmlPathSave = xmlPath;
        xmlPath = ".";

        NodeList tsNodeList = tsDoc.getElementsByTagName("tileset");

        for (int itr = 0; (tsNode = tsNodeList.item(itr)) != null; itr++) {
            set = unmarshalTileset(tsNode);
            if (set.getSource() != null) {
                throw new Exception(
                        "Recursive external Tilesets are not supported.");
            }
            //set.setSource(filename);
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

        int tileWidth = getAttribute(t, "tilewidth", 0);
        int tileHeight = getAttribute(t, "tileheight", 0);
        int tileSpacing = getAttribute(t, "spacing", 0);

        if (set.getSource() != null) {
            TileSet ext = unmarshalTilesetFile(new FileInputStream(xmlPath + set.getSource()));
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

                    File sourceFile = new File(source);
                    String sourcePath;
                    if (sourceFile.getAbsolutePath().equals(source)) {
                        sourcePath = sourceFile.getCanonicalPath();
                    } else {
                        sourcePath =
                            new File(xmlPath + source).getCanonicalPath();
                    }

                    set.importTileBitmap(sourcePath,
                            tileWidth, tileHeight, tileSpacing);

                    // There can be only one image element
                    break;
                }
            }
        }
        return set;
    }

    private MapObject unmarshalObject(Node t) throws Exception {
        MapObject obj = null;
        try {
            obj = (MapObject)unmarshalClass(MapObject.class,t);
        } catch (Exception e) {
            e.printStackTrace();
        }

        NodeList children = t.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equalsIgnoreCase("property")) {
                obj.setProperty(getAttributeValue(child, "name"),
                        getAttributeValue(child, "value"));
            }
        }
        return obj;
    }

    private Tile unmarshalTile(Node t) throws Exception {
        Tile tile = null;

        try {
            tile = (Tile)unmarshalClass(Tile.class, t);
        } catch (Exception e) {
            e.printStackTrace();
        }

        NodeList children = t.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equalsIgnoreCase("image")) {
                tile.setImage(unmarshalImage(child));
            } else if (child.getNodeName().equalsIgnoreCase("property")) {
                tile.setProperty(getAttributeValue(child,"name"),
                        getAttributeValue(child, "value"));
            }
        }

        return tile;
    }

    private MapLayer unmarshalLayer(Node t) throws Exception {
        MapLayer ml = null;
        Rectangle rect = new Rectangle(0, 0, map.getWidth(), map.getHeight());

        boolean encodedBase64 = false;
        try {
            ml = (MapLayer)unmarshalClass(MapLayer.class, t);
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
                    int x = 0, y = 0;
                    NodeList dataChilds = child.getChildNodes();
                    NodeList tilelist = doc.getElementsByTagName("tile");
                    for (int j = 0; j < dataChilds.getLength(); j++) {
                        Node dataChild = dataChilds.item(j);
                        if (dataChild.getNodeName().equalsIgnoreCase("tile")) {
                            int tileId = getAttribute(dataChild, "gid", -1);
                            TileSet ts = map.findTileSetForTileGID(tileId);
                            if (ts != null) {
                                ml.setTileAt(x, y,
                                        ts.getTile(tileId - ts.getFirstGid()));
                            } else {
                                ml.setTileAt(x, y, map.getNullTile());
                            }

                            x++;
                            if (x == ml.getWidth()) { x = 0; y++; }
                            if (y == ml.getHeight()) { break; }
                        }
                    }
                }

                break;
            }
        }

        return ml;
    }

    private void buildMap(Document doc) throws Exception {
        Node item, mapNode;
        NodeList l, mapNodeList;
        NamedNodeMap nnm;
        Tile tile;
        mapNodeList = doc.getElementsByTagName("map");


        for (int itr = 0; (mapNode = mapNodeList.item(itr)) != null; itr++) {
            // Get the map dimensions and create the map
            int mapWidth = getAttribute(mapNode, "width", 0);
            int mapHeight = getAttribute(mapNode, "height", 0);

            if (mapWidth > 0 && mapHeight > 0) {
                map = new Map(mapWidth, mapHeight);
            } else {
                // Maybe this map is still using the dimensions element
                l = doc.getElementsByTagName("dimensions");
                for (int i = 0; (item = l.item(i)) != null; i++) {
                    if (item.getParentNode() == mapNode) {
                        mapWidth = getAttribute(item, "width", 0);
                        mapHeight = getAttribute(item, "height", 0);

                        if (mapWidth > 0 && mapHeight > 0) {
                            map = new Map(mapWidth, mapHeight);
                        }
                    }
                }
            }

            if (map == null) {
                throw new Exception("Couldn't locate map dimensions.");
            }

            // Load other map attributes
            String orientation = getAttributeValue(mapNode, "orientation");
            int tileWidth = getAttribute(mapNode, "tilewidth", 0);
            int tileHeight = getAttribute(mapNode, "tileheight", 0);

            if (tileWidth > 0) {
                map.setTileWidth(tileWidth);
            }
            if (tileHeight > 0) {
                map.setTileHeight(tileHeight);
            }

            if (orientation != null) {
                setOrientation(orientation);
            } else {
                setOrientation("orthogonal");
            }

            // Load the properties
            l = doc.getElementsByTagName("property");
            for (int i = 0; (item = l.item(i)) != null; i++) {
                if (item.getParentNode() == mapNode) {
                    map.addProperty(getAttributeValue(item, "name"),
                            getAttributeValue(item, "value"));
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
                    MapLayer layer = unmarshalLayer(item);
                    if (layer != null) {
                        map.addLayer(layer);
                    }
                }
            }

            // Load the objects
            l = doc.getElementsByTagName("object");
            for (int i = 0; (item = l.item(i)) != null; i++) {
                if (item.getParentNode() == mapNode) {
                    map.addObject(unmarshalObject(item));
                }
            }
        }
    }

    private Map unmarshal(String xmlFile) throws IOException, Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(new File(xmlFile));
        } catch (SAXException e) {
            e.printStackTrace();
            throw new Exception("Error while parsing map file.");
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

    public Map readMap(InputStream in) throws Exception {            //TODO: this and the unmarshal function should be joined
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(in, ".");
        } catch (SAXException e) {
            e.printStackTrace();
            throw new Exception("Error while parsing map file.");
        }

        // TODO: A problem arrises with xmlPath if the tag sources are absolute
        // path...
        xmlPath = ".";

        buildMap(doc);

        //map.setFilename(xmlFile);
        return map;
    }
    
    public TileSet readTileset(String filename) throws Exception {
        return unmarshalTilesetFile(new FileInputStream(filename));
    }

    public TileSet readTileset(InputStream in) throws Exception {
    	return unmarshalTilesetFile(in);
    }
    
    /**
     * @see tiled.io.MapReader#getFilter()
     */
    public String getFilter() throws Exception {
        return "*.tmx,*.tsx";
    }

    public String getPluginPackage() {
    	return "Tiled internal TMX reader/writer";
    }
    
    public String getDescription() {
        return "This is the core Tiled TMX format reader\n" +
        			"\n" +
					"Tiled Map Editor, (c) 2004\n" +
					"Adam Turk\n" +
					"Bjorn Lindeijer";
    }

    public String getName() {
    	return "Default Tiled XML map reader";
    }

    public boolean accept(File pathname) {
        try {
            String path = pathname.getCanonicalPath();
            if (path.endsWith(".tmx") || path.endsWith(".tsx")) {
                return true;
            }
        } catch (IOException e) {}
        return false;
    }
}
