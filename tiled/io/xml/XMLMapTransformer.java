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
import java.io.*;
import java.lang.reflect.*;
import java.util.Stack;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;
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
    private Stack warnings;
    
    public XMLMapTransformer() {
        mediaComponent = new Canvas();
        mediaTracker = new MediaTracker(mediaComponent);
        warnings = new Stack();
    }

    private String makeUrl(String filename) throws MalformedURLException {
        String url = "";
        if(filename.indexOf("://") > 0 || filename.startsWith("file:")) {
            url = filename;
        } else {
            url = (new File(filename)).toURL().toString();
        }
        return url;
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

        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].getName().equalsIgnoreCase("int")) {
                conformingArguments[i] = new Integer(args[i]);
            } else if (parameterTypes[i].getName().equalsIgnoreCase("float")) {
                conformingArguments[i] = new Float(args[i]);
            } else if (parameterTypes[i].getName().endsWith("String")) {
                conformingArguments[i] = args[i];
            } else if (parameterTypes[i].getName().equalsIgnoreCase("boolean")) {
                conformingArguments[i] = new Boolean(args[i]);
            } else {
                warnings.push("INFO: Unsupported argument type "+parameterTypes[i].getName()+", defaulting to java.lang.String");
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
        } else {
            warnings.push("WARN: Unknown orientation '"+o+"'");
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
                    } else {
                        warnings.push("WARN: Unsupported attribute '"+n.getNodeName()+"' on <"+node.getNodeName()+"> tag");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return o;
    }

    private Image unmarshalImage(Node t, String baseDir) throws MalformedURLException, IOException {
        Image img = null;

        String source = getAttributeValue(t, "source");
        
        if (source != null) {
            if(Util.checkRoot(source)) {
                source = makeUrl(source);
            } else {
                source = baseDir + source;
            }
            img = ImageIO.read(new URL(source));
        } else {
            NodeList nl = t.getChildNodes();

            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n.getNodeName().equals("data")) {
                    Node cdata = n.getChildNodes().item(0);
                    if (cdata == null) {
                        warnings.push("WARN: image <data> tag enclosed no data. (empty data tag)");
                    } else {
                        String sdata = cdata.getNodeValue();
                        img = ImageHelper.bytesToImage(
                            Base64.decode(sdata.trim().toCharArray()));
                    }
                    break;
                }
            }
        }

        // Wait for the image to be ready
        /*mediaTracker.addImage(img, 0);
        try {
            mediaTracker.waitForID(0);
        }
        catch (InterruptedException ie) {
            System.err.println(ie);
        }
        mediaTracker.removeImage(img);*/

        /*
        if (getAttributeValue(t, "set") != null) {
            TileSet ts = (TileSet)map.getTilesets().get(
                    Integer.parseInt(getAttributeValue(t, "set")));
            if (ts != null) {
                ts.addImage(img);
            }
        }
        */
        
        return img;
    }

    private TileSet unmarshalTilesetFile(InputStream in, String filename) throws Exception {
        TileSet set = null;
        Node tsNode;
        Document tsDoc = null;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            tsDoc = builder.parse(in, ".");
        } catch (FileNotFoundException fnf) {
            // TODO: Have a popup and ask the user to browse to the file...
            warnings.push("ERROR: Could not find external tileset file "+filename);
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
                warnings.push(
                        "WARN: Recursive external Tilesets are not supported.");
            }
            set.setSource(filename);
            // NOTE: This is a deliberate break. multiple tilesets per TSX are
            // not supported yet (maybe never)...
            break;
        }

        xmlPath = xmlPathSave;
        return set;
    }

    private TileSet unmarshalTileset(Node t) throws Exception {
        TileSet set = null;
        boolean hasTileTags = false;

        String tilesetBaseDir = xmlPath;

        try {
            set = (TileSet)unmarshalClass(TileSet.class, t);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int tileWidth = getAttribute(t, "tilewidth", 0);
        int tileHeight = getAttribute(t, "tileheight", 0);
        int tileSpacing = getAttribute(t, "spacing", 0);

        if (set.getBaseDir() != null) {
            tilesetBaseDir = makeUrl(set.getBaseDir()); 
        }

        if (set.getSource() != null) {
            
            String filename = tilesetBaseDir + set.getSource();
            if(Util.checkRoot(set.getSource())) {
                filename = makeUrl(set.getSource());
            }
            
            TileSet ext = null;
            try{
                InputStream in = new URL(filename).openStream();
                ext = unmarshalTilesetFile(in, filename);
            }catch(FileNotFoundException fnf) {
                warnings.push("ERROR: Could not find external tileset file "+filename);
                ext = new TileSet();
            }
            ext.setFirstGid(set.getFirstGid());
            return ext;
        } else {
            NodeList children = t.getChildNodes();

            // Do an initial pass to see if any tile tags are specified
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeName().equalsIgnoreCase("tile")) {
                    hasTileTags = true;
                }
            }

            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeName().equalsIgnoreCase("tile")) {
                    set.addTile(unmarshalTile(child, tilesetBaseDir));
                } else if (child.getNodeName().equalsIgnoreCase("image")) {
                    String source = getAttributeValue(child, "source");

                    if (source != null && getAttributeValue(child, "id") == null) {
                        // Not a shared image, but a entire set in one image
                        // file
                        File sourceFile = new File(source);
                        String sourcePath = tilesetBaseDir + source;
                        if(Util.checkRoot(source)) {
                            sourcePath = makeUrl(source);
                        }

                        set.importTileBitmap(sourcePath, tileWidth, tileHeight,
                                tileSpacing, !hasTileTags);

                    } else {
                        set.addImage(unmarshalImage(child, tilesetBaseDir),
                                getAttributeValue(child, "id"));
                    }
                }
            }
        }
        return set;
    }

    private MapObject unmarshalObject(Node t) throws Exception {
        MapObject obj = null;
        try {
            obj = (MapObject)unmarshalClass(MapObject.class, t);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Properties objProps = obj.getProperties();
        NodeList children = t.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equalsIgnoreCase("property")) {
                objProps.setProperty(getAttributeValue(child, "name"),
                        getAttributeValue(child, "value"));
            }
        }
        return obj;
    }

    private Tile unmarshalTile(Node t, String baseDir) throws Exception {
        Tile tile = null;

        try {
            tile = (Tile)unmarshalClass(Tile.class, t);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Properties tileProps = tile.getProperties();
        NodeList children = t.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equalsIgnoreCase("image")) {
                int id = getAttribute(child, "id", -1);
                if (id < 0) {
                    tile.setImage(unmarshalImage(child, baseDir));
                } else {
                    tile.setImage(id);
                    int rotation = getAttribute(child, "rotation", 0);
                    String flipped_s = getAttributeValue(child, "flipped");
                    boolean flipped = (flipped_s != null
                        && flipped_s.equalsIgnoreCase("true"));
                    int orientation;
                    if (rotation == 90) {
                        orientation = (flipped ? 6 : 4);
                    } else if (rotation == 180) {
                        orientation = (flipped ? 2 : 3);
                    } else if (rotation == 270) {
                        orientation = (flipped ? 5 : 7);
                    } else {
                        orientation = (flipped ? 1 : 0);
                    }
                    tile.setImageOrientation(orientation);
                }
            } else if (child.getNodeName().equalsIgnoreCase("property")) {
                tileProps.setProperty(getAttributeValue(child, "name"),
                        getAttributeValue(child, "value"));
            }
        }

        return tile;
    }

    private MapLayer unmarshalObjectGroup(Node t) throws Exception {
        ObjectGroup og = null;
        try {
            og = (ObjectGroup)unmarshalClass(ObjectGroup.class, t);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        //Read all objects from the group, "...and in the darkness bind them."
        NodeList children = t.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equalsIgnoreCase("object")) {
                og.bindObject(unmarshalObject(child));
            }
        }
        
        return og;
    }
    
    private MapLayer unmarshalLayer(Node t) throws Exception {
        TileLayer ml = null;

        boolean encodedBase64 = false;
        try {
            ml = (TileLayer)unmarshalClass(TileLayer.class, t);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Rectangle r = ml.getBounds();
        if (r.height == 0 && r.width == 0) {
            ml.setBounds(map.getBounds());
            warnings.push("INFO: defaulting layer '"+ml.getName()+"' dimensions to map dimensions");
        }

        Properties mlProps = ml.getProperties();
        NodeList children = t.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equalsIgnoreCase("data")) {
                String encoding = getAttributeValue(child, "encoding");

                if (encoding != null && encoding.equalsIgnoreCase("base64")) {
                    Node cdata = child.getChildNodes().item(0);
                    if (cdata == null) {
                        warnings.push("WARN: layer <data> tag enclosed no data. (empty data tag)");
                    } else {
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
            } else if (child.getNodeName().equalsIgnoreCase("property")) {
                mlProps.setProperty(getAttributeValue(child, "name"),
                        getAttributeValue(child, "value"));
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

            Properties mapProps = map.getProperties();

            // Load the tilesets, properties, layers and objectgroups
            l = mapNode.getChildNodes();
            for (int i = 0; i < l.getLength(); i++) {
                Node sibs = l.item(i);

                if (sibs.getNodeName().equals("tileset")) {
                    map.addTileset(unmarshalTileset(sibs));
                }
                else if (sibs.getNodeName().equals("property")) {
                    mapProps.setProperty(getAttributeValue(sibs, "name"),
                            getAttributeValue(sibs, "value"));
                }
                else if (sibs.getNodeName().equals("layer")) {
                    MapLayer layer = unmarshalLayer(sibs);
                    if (layer != null) {
                        map.addLayer(layer);
                    }
                }
                else if (sibs.getNodeName().equals("objectgroup")) {
                    MapLayer layer = unmarshalObjectGroup(sibs);
                    if (layer != null) {
                        map.addLayer(layer);
                    }
                }
            }
        }
    }

    private Map unmarshal(InputStream in) throws IOException, Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(in, xmlPath);
        } catch (SAXException e) {
            e.printStackTrace();
            throw new Exception("Error while parsing map file: "+e.toString());
        }

        buildMap(doc);
        
        return map;
    }


    // MapReader interface

    public Map readMap(String filename) throws Exception {
        String xmlFile = filename;
        
        xmlPath = filename.substring(0,
                filename.lastIndexOf(File.separatorChar) + 1);
        
        xmlFile = makeUrl(xmlFile);
        xmlPath = makeUrl(xmlPath);
        
        URL url = new URL(xmlFile);
        Map unmarshalledMap = unmarshal(url.openStream());
        unmarshalledMap.setFilename(filename);
        
        
        
        return unmarshalledMap;
    }

    public Map readMap(InputStream in) throws Exception {
        xmlPath = makeUrl(".");
        
        Map unmarshalledMap = unmarshal(in);
        
        //unmarshalledMap.setFilename(xmlFile):w
        //
        return unmarshalledMap;
    }
    
    public TileSet readTileset(String filename) throws Exception {
        String xmlFile = filename;
        
        xmlPath = filename.substring(0,
                filename.lastIndexOf(File.separatorChar) + 1);
        
        xmlFile = makeUrl(xmlFile);
        xmlPath = makeUrl(xmlPath);

        URL url = new URL(xmlFile);
        return unmarshalTilesetFile(url.openStream(), filename);
    }

    public TileSet readTileset(InputStream in) throws Exception {
        // TODO: The MapReader interface should be changed...
        return unmarshalTilesetFile(in, ".");
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
    
    public void setErrorStack(Stack es) {
        warnings = es;
    }
}
