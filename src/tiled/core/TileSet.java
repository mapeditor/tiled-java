/*
 *  Tiled Map Editor, (c) 2004-2006
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
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import javax.imageio.ImageIO;

import tiled.mapeditor.util.cutter.TileCutter;
import tiled.util.NumberedSet;

/**
 * <p>TileSet handles operations on tiles as a set, or group. It has several
 * advanced internal functions aimed at reducing unnecessary data replication.
 * A 'tile' is represented internally as two distinct pieces of data. The
 * first and most important is a {@link Tile} object, and these are held in
 * a {@link java.util.Vector}.</p>
 *
 * <p>The other is the tile image.</p>
 *
 * @version $Id$
 */
public class TileSet
{
    private String base;
    private NumberedSet tiles, images;
    private int firstGid;
    private Rectangle tileDimensions;
    private String externalSource, tilebmpFile;
    private String name;
    private Color transparentColor;
    private Properties defaultTileProperties;
    private Image tileSetImage;

    /**
     * Default constructor
     */
    public TileSet() {
        tiles = new NumberedSet();
        images = new NumberedSet();
        tileDimensions = new Rectangle();
        defaultTileProperties = new Properties();
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
     * Creates a tileset from a buffered image. Tiles are cut by the passed cutter.
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

        if (cutter == null) {
        	throw new Exception("No cutter!");
        }

        tileDimensions = new Rectangle(cutter.getDimensions());
        tileSetImage = tilebmp;

        cutter.setImage(tilebmp);

        try {
            BufferedImage tile;
	        while ((tile = (BufferedImage) cutter.getNextTile()) != null) {
	        	int newId = addImage(tile);
	        	if (createTiles) {
                    Tile newTile = new Tile();
                    newTile.setImage(newId);
                    addNewTile(newTile);
                }
	        }
        } catch (Exception e) {
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
        tileSetImage = i;
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

        if (tileDimensions.height < t.getHeight()) {
        	tileDimensions.height = t.getHeight();
        }

        if (tileDimensions.width < t.getWidth()) {
        	tileDimensions.width = t.getWidth();
        }

        // Add any default properties
        // todo: use parent properties instead?
        t.getProperties().putAll(defaultTileProperties);

        tiles.put(t.getId(), t);
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
        return tileDimensions.width;
    }

    /**
     * Returns the tile height of tiles in this tileset. Not all tiles in a
     * tileset are required to have the same height, but the height should be
     * at least the tile height of the map the tileset is used with.
     *
     * If there are tiles with varying heights in this tileset, the returned
     * height will be the maximum.
     *
     * @return the max height of the tiles in the set
     */
    public int getTileHeight() {
        return tileDimensions.height;
    }

    /**
     * Returns the spacing between the tiles on the tileset image.
     * @return the spacing in pixels between the tiles on the tileset image
     */
    public int getTileSpacing() {
        // todo: make this functional
        return 0;
    }

    /**
     * Gets the tile with <b>local</b> id <code>i</code>.
     *
     * @param i local id of tile
     * @return A tile with local id <code>i</code> or <code>null</code> if no
     *         tile exists with that id
     */
    public Tile getTile(int i) {
    	try {
    		return (Tile)tiles.get(i);
    	} catch (ArrayIndexOutOfBoundsException a) {}
    	return null;
    }

    /**
     * Returns the first non-null tile in the set.
     *
     * @return The first tile in this tileset, or <code>null</code> if none
     *         exists.
     */
    public Tile getFirstTile() {
        Tile ret = null;
        final Iterator itr = iterator();
        if (itr.hasNext()) {
            ret = (Tile)itr.next();
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

    // TILE IMAGE CODE

    /**
     * This function uses the CRC32 checksums to find the cached version of the
     * image supplied.
     *
     * @param i an Image object
     * @return returns the id of the given image, or -1 if the image is not in
     *         the set
     */
    public int getIdByImage(Image i) {
        return images.indexOf(i);
    }

    /**
     * @param id
     * @return the image identified by the key, or <code>null</code> when
     *         there is no such image
     */
    public Image getImageById(int id) {
        return (Image) images.get(id);
    }

    /**
     * Overlays the image in the set referred to by the given key.
     *
     * @param id
     * @param i
     */
    public void overlayImage(int id, Image i) {
        images.put(id, i);
    }

    /**
     * Returns the dimensions of an image as specified by the id
     * <code>key</code>.
     *
     * @param id
     * @param orientation
     * @return dimensions of image with referenced by given key
     */
    public Dimension getImageDimensions(int id, int orientation) {
        Image i = (Image) images.get(id);
        if (i != null) {
            return new Dimension(i.getWidth(null), i.getHeight(null));
        } else {
            return new Dimension(0, 0);
        }
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
        return images.findOrAdd(image);
    }

    public int addImage(Image image, int id) {
        return images.put(id, image);
    }

    public void removeImage(int id) {
        images.remove(id);
    }

    /**
     * Returns whether the tileset is derived from a tileset image.
     *
     * @return tileSetImage != null
     */
    public boolean isSetFromImage() {
        return tileSetImage != null;
    }

    /**
     * Checks whether each image has a one to one relationship with the tiles.
     *
     * @deprecated
     * @return <code>true</code> if each image is associated with one and only
     *         one tile, <code>false</code> otherwise.
     */
    public boolean isOneForOne() {
        Iterator itr = iterator();

        //[ATURK] I don't think that this check makes complete sense...
        /*
        while (itr.hasNext()) {
            Tile t = (Tile)itr.next();
            if (t.countAnimationFrames() != 1 || t.getImageId() != t.getId()
                    || t.getImageOrientation() != 0) {
                return false;
            }
        }
        */

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

	public void setDefaultProperties(Properties defaultSetProperties) {
		defaultTileProperties = defaultSetProperties;
	}
}
