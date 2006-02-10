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

package tiled.core;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import javax.imageio.ImageIO;

import tiled.mapeditor.util.cutter.TileCutter;
import tiled.util.Util;
import tiled.util.NumberedSet;

import tiled.core.ImageGroup;

/**
 * <p>TileSet handles operations on tiles as a set, or group. It has several
 * advanced internal functions aimed at reducing unnecessary data replication.
 * A 'tile' is represented internally as three distinct pieces of data. The
 * first and most important is a tiled.core.Tile object, and these are held in
 * a java.util.Vector.</p>
 *
 * <p>Tile objects contain an id that can be used to look up the second piece
 * of data, the tile image hash. The tile image hash is a unique CRC32
 * checksum. A checksum is generated for each image that is added to the set.
 * A java.util.Hashtable keeps the key-value pair of id and checksum. A second
 * java.util.Hashtable (the imageCache) maintains a key-value pair with the
 * checksum as key and the actual java.awt.Image as value.</p>
 *
 * <p>When a new image is added, a checksum is created and checked against the
 * checksums in the cache. If the checksum does not already exist, the image
 * is given an id, and is added to the cache. In this way, tile images are
 * never duplicated, and multiple tiles may reference the image by id.</p>
 *
 */
public class TileSet
{
    private String base;
    private NumberedSet tiles, images;
    private int firstGid;
    private int tileHeight;
    private int tileWidth;
    private String externalSource, tilebmpFile;
    private String name;
    private Map map;
    private Color transparentColor;

    /**
     * Default constructor
     */
    public TileSet() {
        tiles = new NumberedSet();
        images = new NumberedSet();
    }

    /**
     * Creates a tileset from a tile bitmap file.
     * 
     * @param imgFilename
     * @param cutter
     * @param createTiles
     * @throws Exception
     * @see TileSet#importTileBitmap(BufferedImage, TileCutter, boolean)
     */
    public void importTileBitmap(String imgFilename, TileCutter cutter, boolean createTiles) throws Exception
    {
        File imgFile = null;
        try {
            imgFile = new File(imgFilename);
            tilebmpFile = imgFile.getCanonicalPath();
        } catch (IOException e) {
            tilebmpFile = imgFilename;
        }

        System.out.println("Importing " + imgFilename + "...");
        
        importTileBitmap(ImageIO.read(imgFile.toURL()), cutter, createTiles);
    }

    /**
     * Creates a tileset from a buffered image. This is a linear cutter that
     * goes left to right, top to bottom when cutting. It can optionally create
     * tiled.core.Tile objects that reference the images as it is cutting them.
     *
     * @param tilebmp     the image to be used
     * @param cutter
     * @param createTiles set to <code>true</code> to have the function create
     *                    Tiles
     * @throws Exception
     */
    public void importTileBitmap(BufferedImage tilebmp, TileCutter cutter, boolean createTiles) throws Exception{

        if (tilebmp == null) {
            throw new Exception("Failed to load " + tilebmpFile);
        }

        if(cutter == null) {
        	throw new Exception("No cutter!");
        }
        
        tileHeight = cutter.getDimensions().height;
        tileWidth = cutter.getDimensions().width;
        
        BufferedImage tile;
        
        cutter.setImage(tilebmp);
        
        try {
	        while((tile = (BufferedImage) cutter.getNextTile()) != null) {
	        	int newId = addImage(tile);
	        	if (createTiles) {
                    Tile newTile = new Tile();
                    newTile.setImage(newId);
                    addNewTile(newTile);
                }
	        }
        } catch(Exception e) {
        	e.printStackTrace();
        }

        //FIXME: although faster, the following doesn't seem to handle alpha on some platforms...
        //GraphicsConfiguration config =
        //    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        //Image tilesetImage = config.createCompatibleImage(tileWidth, tileHeight);
        //Graphics tg = tilesetImage.getGraphics();

        /*if (iw > 0 && ih > 0) {
            for (int y = 0; y <= ih - tileHeight; y += tileHeight + spacing) {
                for (int x = 0; x <= iw - tileWidth; x += tileWidth + spacing) {
                    BufferedImage tile = ((BufferedImage)setImage).getSubimage(
                            x, y, tileWidth, tileHeight);

                    int newId = addImage(tile);
                    if (createTiles) {
                        Tile newTile = new Tile();
                        newTile.setImage(newId);
                        addNewTile(newTile);
                    }
                }
            }
        }*/
    }

    /**
     * @deprecated
     * @param i
     */
    public void setTilesetImage(Image i) {
        //setImage = i;
    }

    /**
     * Sets the URI path of the external source of this tile set. By setting
     * this, the set is implied to be external in all other operations.
     *
     * @param source a URI of the tileset image file
     */
    public void setSource(String source) {
        externalSource = source;
    }

    /**
     * Sets the base directory for the tileset
     *
     * @param base a String containing the native format directory
     */
    public void setBaseDir(String base) {
        this.base = base;
    }

    /**
     * Sets the filename of the tileset image. Doesn't change the tileset in
     * any other way.
     * 
     * @param name
     */
    public void setTilesetImageFilename(String name) {
        tilebmpFile = name;
    }

    /**
     * Sets the first global id used by this tileset.
     *
     * @param f first global id
     */
    public void setFirstGid(int f) {
        firstGid = f;
    }

    /**
     * Sets the name of this tileset.
     *
     * @param name the new name for this tileset
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the map this tileset is part of.
     * 
     * @param map
     * @deprecated
     */
    public void setMap(Map map) {
        this.map = map;
    }

    /**
     * Sets the transparent color in the tileset image.
     * 
     * @param color
     */
    public void setTransparentColor(Color color) {
        transparentColor = color;
    }

    /**
     * Adds the tile to the set, setting the id of the tile only if the current
     * value of id is -1.
     *
     * @param t the tile to add
     * @return int The <b>local</b> id of the tile
     */
    public int addTile(Tile t) {
        if (t.getId() < 0) {
            t.setId(tiles.getMaxId());
        }
        
        if (tileHeight < t.getHeight()) {
            tileHeight = t.getHeight();
        }

        if (tileWidth < t.getWidth()) {
            tileWidth = t.getWidth();
        }
        
        tiles.put(t.getId(), t);
        //System.out.println("adding tile " +t.getId());
        t.setTileSet(this);

        return t.getId();
    }

    /**
     * This method takes a new Tile object as argument, and in addition to
     * the functionality of <code>addTile()</code>, sets the id of the tile
     * to -1.
     *
     * @see TileSet#addTile(Tile)
     * @param t the new tile to add.
     */
    public void addNewTile(Tile t) {
        t.setId(-1);
        addTile(t);
    }

    /**
     * Removes a tile from this tileset. Does not invalidate other tile
     * indices. Removal is simply setting the reference at the specified
     * index to <b>null</b>
     * 
     * @param i the index to remove
     */
    public void removeTile(int i) {
        tiles.remove(i);
    }

    /**
     * Returns the amount of tiles in this tileset.
     *
     * @return the amount of tiles in this tileset
     */
    public int size() {
        return tiles.size();
    }

    /**
     * Returns the maximum tile id.
     *
     * @return the maximum tile id, or -1 when there are no tiles
     */
    public int getMaxTileId() {
        return tiles.getMaxId();
    }

    /**
     * Returns an iterator over the tiles in this tileset.
     *
     * @return an iterator over the tiles in this tileset.
     */
    public Iterator iterator() {
        return tiles.iterator();
    }

    /**
     * Returns the width of tiles in this tileset. All tiles in a tileset
     * should be the same width, and the same as the tile width of the map the
     * tileset is used with.
     * 
     * @return int - The maximum tile width
     */
    public int getTileWidth() {
        return tileWidth;
    }

    /**
     * Returns the tile height of tiles in this tileset. Not all tiles in a
     * tileset are required to have the same height, but the height should be
     * at least the tile height of the map the tileset is used with.
     *
     * If there are tiles with varying heights in this tileset, the returned
     * height will be the maximum.
     * 
     * @return int - The max height of the tiles in the set
     */
    public int getTileHeight() {
        return tileHeight;
    }

    /**
     * @deprecated
     * @return int
     */
    public int getMaxTileHeight() {
    	return getTileHeight();
    }
    
    /**
     * Gets the tile with <b>local</b> id <code>i</code>.
     *
     * @param i local id of tile
     * @return A tile with local id <code>i</code> or <code>null</code> if no
     *         tile exists with that id
     */
    public Tile getTile(int i) {
        return (Tile)tiles.get(i);
    }

    /**
     * Returns the first non-null tile in the set.
     *
     * @return The first tile in this tileset, or <code>null</code> if none
     *         exists.
     */
    public Tile getFirstTile() {
        Tile ret = null;
        Iterator itr = iterator();
        while (itr.hasNext()) {
            ret = (Tile)itr.next();
            break;
        }
        return ret;
    }

    /**
     * Returns the source of this tileset.
     *
     * @return a filename if tileset is external or <code>null</code> if
     *         tileset is internal.
     */
    public String getSource() {
        return externalSource;
    }

    /**
     * Returns the base directory for the tileset
     * 
     * @return a directory in native format as given in the tileset file or tag
     */
    public String getBaseDir() {
        return base;
    }

    /**
     * Returns the filename of the tile bitmap.
     *
     * @return the filename of the tile bitmap, or <code>null</code> if this
     *         tileset doesn't reference a tile bitmap
     */
    public String getTilebmpFile() {
        return tilebmpFile;
    }

    /**
     * Returns the first global id connected to this tileset.
     *
     * @return first global id
     */
    public int getFirstGid() {
        return firstGid;
    }

    /**
     * @return the name of this tileset.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the map this tileset is part of.
     * @deprecated
     * @return int
     */
    public Map getMap() {
        return map;
    }

    /**
     * Returns the transparent color of the tileset image, or <code>null</code>
     * if none is set.
     * 
     * @return Color - The transparent color of the set
     */
    public Color getTransparentColor() {
        return transparentColor;
    }

    /**
     * @return the name of the tileset, and the total tiles
     */
    public String toString() {
        return getName() + " [" + size() + "]";
    }


    // TILE IMAGE CODE

    /**
     * Provides a CRC32 checksum of the given image.
     *
     * @param i a preloaded Image object
     * @return a String containing the checksum value
     */
    private String checksumImage(Image i) {
        PixelGrabber pg = new PixelGrabber(i, 0, 0, -1, -1, false);
        Checksum sum = new CRC32();

        try {
            pg.grabPixels();
            //ImageInputStream is;

            try {
                int psize = pg.getColorModel().getPixelSize();
                ByteArrayInputStream bais = null;

                // Handle different pixel sizes
                if (psize >= 15 ) {
                    bais = new ByteArrayInputStream(
                            Util.convertIntegersToBytes((int[])pg.getPixels()));
                } else {
                    bais = new ByteArrayInputStream((byte[])pg.getPixels());
                }
                byte[] bytes = new byte[1024];
                int len = 0;

                while ((len = bais.read(bytes)) >= 0) {
                    sum.update(bytes, 0, len);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return Long.toHexString(sum.getValue());
    }

    /**
     * Returns the number of images in the set.
     *
     * @return the number of images in the set
     */
    public int getTotalImages() {
        return images.size();
    }

    /**
     * @return an Enumeration of the image ids
     */
    public Enumeration getImageIds() {
        Vector v = new Vector();
        for (int id = 0; id <= images.getMaxId(); ++id) {
            if (images.containsId(id)) v.add(Integer.toString(id));
        }
        return v.elements();
    }

    /**
     * This function uses the CRC32 checksums to find the cached version of the
     * image supplied.
     *
     * @param i an Image object
     * @return returns the id of the given image, or -1 if the image is not in
     *         the set
     */
    public int getIdByImage(Image i) {
        return images.indexOf(new ImageGroup(i));
    }

    /**
     * @param key a key identifying the image to get
     * @return the image identified by the key, or <code>null</code> when
     *         there is no such image
     * @see TileSet#getImageByIdAndOrientation(Object, int)
     */
    public Image getImageById(Object key) {
        return getImageByIdAndOrientation(key, 0);
    }

    /**
     * Returns the image referred to by the given key, and automatically
     * sets it to the given orientation.
     * 
     * @param key
     * @param orientation
     * @return Image
     */
    public Image getImageByIdAndOrientation(Object key, int orientation) {
        int img_id = Integer.parseInt((String)key);
        ImageGroup img = (ImageGroup)images.get(img_id);
        if (img == null) return null;
        return img.getImage(orientation);
    }

    /**
     * Overlays the image in the set referred to by the given key.
     * 
     * @param key
     * @param i
     */
    public void overlayImage(Object key, Image i) {
        int img_id = Integer.parseInt((String)key);
        images.put(img_id, new ImageGroup(i));
    }

    /**
     * Returns the dimensions of an image as specified by the id
     * <code>key</code>.
     *
     * @param key
     * @param orientation
     * @return dimensions of image with referenced by given key
     */
    public Dimension getImageDimensions(Object key, int orientation) {
        Image i = getImageByIdAndOrientation(key, orientation);
        if (i != null) {
            return new Dimension(i.getWidth(null), i.getHeight(null));
        } else {
            return new Dimension(0, 0);
        }
    }

    /**
     * Attempt to retrieve an image matching the given image from the image
     * cache.
     *
     * @param image the image to match
     * @return a matching image from the cache if it exists, <code>null</code>
     *         otherwise
     */
    public Image queryImage(Image image) {
        int id = images.indexOf(new ImageGroup(image));
        ImageGroup img = (ImageGroup)images.get(id);
        return img.getImage(0);
    }

    /*
     * Note: The following function only works for images in default
     * orientation.
     */

    /**
     * Find the id of the given image in the image cache.
     *
     * @param image the java.awt.Image to find the id for.
     * @return an java.lang.Object that represents the id of the image
     */
    public Object queryImageId(Image image) {
        ImageGroup img = new ImageGroup(image);
        return Integer.toString(images.indexOf(img));
    }

    /**
     * Adds the specified image to the image cache. If the image already exists
     * in the cache, returns the id of the existing image. If it does not exist,
     * this function adds the image and returns the new id.
     *
     * @param image the java.awt.Image to add to the image cache
     * @return the id as an <code>int</code> of the image in the cache
     */
    public int addImage(Image image) {
        return images.findOrAdd(new ImageGroup(image));
    }

    public int addImage(Image image, Object key) {
        if (key == null) {
            return addImage(image);
        } else {
            int id = Integer.parseInt((String)key);
            images.put(id, new ImageGroup(image));
            return id;
        }
    }

    public void removeImage(Object key) {
        int id = Integer.parseInt((String)key);
        images.remove(id);
    }

    public boolean usesSharedImages() {
        // TODO: Currently only uses shared sets...
        return true;
    }

    /**
     * Checks whether each image has a one to one relationship with the tiles.
     *
     * @return <code>true</code> if each image is associated with one and only
     *         one tile, <code>false</code> otherwise.
     */
    public boolean isOneForOne() {
        Iterator itr = iterator();

        //[ATURK] I don't think that this check makes complete sense...
        /*while (itr.hasNext()) {
            Tile t = (Tile)itr.next();
            if (t.countAnimationFrames() != 1 || t.getImageId() != t.getId()
                    || t.getImageOrientation() != 0) {
                return false;
            }
        }*/


        for (int id = 0; id <= images.getMaxId(); ++id) {
            int relations = 0;
            itr = iterator();

            while (itr.hasNext()) {
                Tile t = (Tile)itr.next();
                if (t.getImageId() == id) {
                    relations++;
                }
            }
            if (relations != 1) {
                return false;
            }
        }

        return true;
    }

}
