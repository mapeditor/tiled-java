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
import java.net.URL;
import java.util.*;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import tiled.util.Util;

import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * <p>TileSet handles operations on tiles as a set, or group. It has several advanced internal
 * functions aimed at reducing unnecessary data replication. A 'tile' is represented internally
 * as three distinct pieces of data. The first and most important is a tiled.core.Tile object, 
 * and these are held in a java.util.Vector.</p>
 * <p>Tile objects contain an id that can be used to look up the second piece of data, the tile
 * image hash. The tile image hash is a unique CRC32 checksum. A checksum is generated for each
 * image that is added to the set. A java.util.Hashtable keeps the key-value pair of id and
 * checksum. A second java.util.Hashtable (the imageCache) maintains a key-value pair with the
 * checksum as key and the actual java.awt.Image as value.</p>
 * 
 * <p>When a new image is added, a checksum is created and checked against the checksums in the cache.
 * If the checksum does not already exist, the image is given an id, and is added to the cache.
 * In this way, tile images are never duplicated, and multiple tiles may reference the image by id.</p>
 * 
 * <p>The TileSet also handles 'cutting' tile images from a tileset image, and can optionally create
 * Tile objects that reference the images.</p>
 */

public class TileSet
{
    private String base;
    private Vector tiles;
    private Hashtable images, imageCache;
    private int id;
    private int firstGid;
    private int standardHeight;
    private int standardWidth;
    private Image setImage;
    private String externalSource, tilebmpFile;
    private String name;

    public TileSet() {
        tiles = new Vector();
        images = new Hashtable();
        imageCache = new Hashtable();
    }

    /**
     * Creates a tileset from a tile bitmap file. This is a tile-cutter.
     *
     * @param imgFilename the filename of the image to be used
     * @param tileWidth   the tile width
     * @param tileHeight  the tile height
     * @param spacing     the amount of spacing between the tiles
     * @see TileSet#importTileBitmap(BufferedImage tilebmp, int tileWidth, int tileHeight, int spacing, boolean createTiles)
     * @exception
     */
    public void importTileBitmap(String imgFilename, int tileWidth,
            int tileHeight, int spacing, boolean createTiles) throws Exception{
        File imgFile = null;
        try {
            imgFile = new File(imgFilename);
            tilebmpFile = imgFile.getCanonicalPath();
        } catch (IOException e) {
            tilebmpFile = imgFilename;
        }

        System.out.println("Importing " + imgFilename + "...");

        importTileBitmap(ImageIO.read(new URL(imgFilename)), tileWidth, tileHeight, spacing, createTiles);
        
    }

	/**
	 * Creates a tileset from a buffered image. This is a linear cutter that goes left to right,
	 * top to bottom when cutting. It can optionally create tiled.core.Tile objects that reference
	 * the images as it is cutting them.
	 *
	 * @param tilebmp     the image to be used
	 * @param tileWidth   the tile width
	 * @param tileHeight  the tile height
	 * @param spacing     the amount of spacing between the tiles
	 * @param createTiles set to <code>true</code> to have the function create Tiles
	 * @exception
	 */
	public void importTileBitmap(BufferedImage tilebmp, int tileWidth,
				int tileHeight, int spacing, boolean createTiles) throws Exception{
					
		if (tilebmp == null) {
			throw new Exception("Failed to load " + tilebmpFile);
		}

		int iw = tilebmp.getWidth();
		int ih = tilebmp.getHeight();

		if (iw > 0 && ih > 0) {
			for (int y = 0; y <= ih - tileHeight; y += tileHeight + spacing) {
				for (int x = 0; x <= iw - tileWidth; x += tileWidth + spacing) {
					BufferedImage tile = new BufferedImage(
							tileWidth, tileHeight,
							BufferedImage.TYPE_INT_ARGB);
					Graphics2D tg = tile.createGraphics();

					tg.drawImage(tilebmp, 0, 0,
							tileWidth, tileHeight,
							x, y, x + tileWidth, y + tileHeight,
							null);
					int newId = addImage(tile);
					if (createTiles) {
						Tile newTile = new Tile();
						newTile.setImage(newId);
						addNewTile(newTile);
					}
				}
			}
		}			
	}

    /**
     * Sets the standard width of the tiles in this tileset. Tiles in this
     * tileset are not recommended to have any other width.
     * 
     * @param width the width in pixels to use as the standard tile width
     */
    public void setStandardWidth(int width) {
        standardWidth = width;
    }

    /**
     * Sets the standard height of the tiles in this tileset. This is used to
     * calculate the drawing position of tiles with a height above the standard
     * height.
     */
    public void setStandardHeight(int s) {
        standardHeight = s;
        Iterator itr = tiles.iterator();
        while (itr.hasNext()) {
            Tile t = (Tile)itr.next();
            if (t != null) {
                t.setStandardHeight(standardHeight);
            }
        }
    }

    public void setTilesetImage(Image i) {
        setImage = i;
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
     * Adds the tile to the setting the id of the tile only if the current
     * value of id is -1.
     *
     * @param t the tile to add
     */
    public void addTile(Tile t) {
        if (t.getId() < 0) {
            t.setId(this.getTotalTiles());
        }

        if (t.getId() >= tiles.size()) {
            tiles.setSize(t.getId() + 1);
        }

        tiles.set(t.getId(), t);
        //System.out.println("adding tile " +t.getId());
        t.setTileSet(this);
        t.setStandardHeight(standardHeight);
        if (standardWidth < t.getWidth()) {
            standardWidth = t.getWidth();
        }
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
        tiles.set(i, null);
    }

    /**
     *
     * @return the total size of the internal Vector
     */
    public int getTotalTiles() {
    	int total = 0;
    	for(int i=0;i<tiles.size();i++) {
    		if(tiles.get(i)!=null) {
    			total++;
    		}
    	}
        return total;
    }

    /**
     * Returns the standard width of tiles in this tileset. All tiles in a
     * tileset should be the same width.
     * 
     * @return the standard width as previously set with a call to
     *         TileSet#setStandardWidth
     */
    public int getStandardWidth() {
        return standardWidth;
    }

    /**
     * Returns the standard height of tiles in this tileset. Not all tiles in
     * a tileset are required to have the same height.
     *
     * @return the standard height as previously set with a call to
     *         TileSet#setStandardHeight
     */
    public int getStandardHeight() {
        return standardHeight;
    }

    /**
     * Iterates through the set an retrieves the largest height value.
     *
     * @return the maximum hieght of any tile
     */
    public int getTileHeightMax() {
        int maxHeight = 0;
        Iterator itr = tiles.iterator();
        while (itr.hasNext()) {
            Tile t = (Tile)itr.next();
            if (t != null && t.getHeight() > maxHeight) {
                maxHeight = t.getHeight();
            }
        }
        return maxHeight;
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
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Returns the first non-null tile in the set.
     *
     * @return The first tile in this tileset, or <code>null</code> if none
     *         exists.
     */
    public Tile getFirstTile() {
        Tile ret = null;
        Iterator itr = tiles.iterator();
        while (itr.hasNext()) {
            if ((ret = (Tile)itr.next()) != null) {
                break;
            }
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
	 * @return the name of the tileset, and the total tiles
	 */
    public String toString() {
        return getName() + " [" + getTotalTiles() + "]";
    }


    // TILE IMAGE CODE

	/**
	 * Provides a CRC32 checksum of a given image <code>i</code>
	 * 
	 * @param i a preloaded Image object
	 * @return a String containing the checksum value
	 */
    private String checksumImage(Image i) {
        PixelGrabber pg = new PixelGrabber(i, 0, 0, -1, -1, false);
        Checksum sum = new CRC32();

        try {
            pg.grabPixels();
            ImageInputStream is;

            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(
                        Util.convertIntegersToBytes((int[])pg.getPixels()));
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
	 * 
	 * @return returns the total number of images in the set
	 */
    public int getTotalImages() {
        return images.size();
    }
    
    /**
     * 
     * @return returns an Enumeration of the image ids
     */
    public Enumeration getImageIds() {
        return images.keys();
    }
    
    /**
     * This function uses the CRC32 checksums to find the cached version of the image
     * supplied.
     * 
     * @param i an Image object
     * @return returns the id of the given image, or -1 if the image is not in the set
     */
    public int getIdByImage(Image i) {
        Iterator itr = imageCache.keySet().iterator();
        int searchId = -1;
        if (i != null) {
            while (itr.hasNext()) {
                Image img = (Image)imageCache.get(itr.next());
                searchId++;
                if (img.equals(i)) {
                    break;
                }
            }
        }
        return searchId;
    }

	/**
	 * 
	 * @param key
	 * @return
	 */
    public Image getImageById(Object key) {
        if (images.get(key) == null) {
            return null;
        }
        return (Image)imageCache.get(images.get(key));
    }

    public void overlayImage(Object key, Image i) {
        String hash = checksumImage(i);
        imageCache.put(hash, i);
        images.put(key, hash);
    }

    /**
     * Returns the dimensions of an image as specified by the id <code>key</code>
     * 
     * @param key
     * @return dimensions of image with referenced by given key
     */
    public Dimension getImageDimensions(Object key) {
        if (images.get(key) != null) {
            Image i = (Image)imageCache.get(images.get(key));
            return new Dimension(i.getWidth(null), i.getHeight(null));
        } else {
            return new Dimension(0, 0);
        }
    }

    /**
     * @param image
     * @return
     */
    public Image queryImage(Image image) {
        String hash = checksumImage(image);
        if (imageCache.get(hash) != null) {
            //System.out.println("Success: " + hash);
        }
        return (Image)imageCache.get(hash);
    }

	/**
	 * Find the id fo the given image in the image cache.
	 * 
	 * @param image the java.awt.Image to find the id for.
	 * @return an java.lang.Object that represents the id of the image
	 */
    public Object queryImageId(Image image) {
        String hash = checksumImage(image);
        if (imageCache.get(hash) != null) {
            Iterator itr = images.keySet().iterator();
            while (itr.hasNext()) {
                Object key = itr.next();
                if (images.get(key).equals(hash)) {
                    //System.out.println("Success: " + key);
                    return key;
                }
            }
        }
        return Integer.toString(-1);
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
        int t;
        if ((t = Integer.parseInt((String)queryImageId(image))) >= 0) {
            return t;
        } else {
            t = images.size();
            String cs = checksumImage(image);
            //System.out.print("addImage(Image): " + t + " ");
            //System.out.println("Checksum: " + cs);
            images.put(Integer.toString(t), cs);
            imageCache.put(cs, image);
            return t;
        }
    }

    public int addImage(Image image, Object key) {
        if (key == null) {
            return addImage(image);
        } else {
            String cs = checksumImage(image);
            //System.out.print("addImage(Image, Object): " + key + " ");
            //System.out.println("Checksum: " + cs);
            images.put(key, cs);
            imageCache.put(cs, image);
            return Integer.parseInt((String)key);
        }
    }

    public void removeImage(Object id) {
        imageCache.remove(images.get(id));
        images.remove(id);
    }

    private boolean isUsed(String hash) {
        Iterator itr = tiles.iterator();
        while (itr.hasNext()) {
            Tile t = (Tile) itr.next();
            if (hash.equals(images.get(""+t.getImageId()))) {
                return true;
            }
        }
        return false;
    }

    public Object generateImageWithOrientation(Image src,
            int orientation) {
        if (orientation == 0) {
            return queryImageId(src);
        } else {
            int w = src.getWidth(null), h = src.getHeight(null);
            int[] old_pixels = new int[w * h];
            PixelGrabber p = new PixelGrabber(src, 0, 0, w, h,
                    old_pixels, 0, w);
            try {
                p.grabPixels();
            } catch (InterruptedException e) {
            }
            int[] new_pixels = new int[w * h];
            int dest_w = ((orientation & 4) != 0) ? h : w;
            int dest_h = ((orientation & 4) != 0) ? w : h;
            for (int dest_y = 0; dest_y < dest_h; ++dest_y) {
                for (int dest_x = 0; dest_x < dest_w; ++dest_x) {
                    int src_x = dest_x, src_y = dest_y;
                    if ((orientation & 4) != 0) {
                        src_y = dest_w - dest_x - 1;
                        src_x = dest_y;
                    }
                    if ((orientation & 1) != 0) {
                        src_x = w - src_x - 1;
                    }
                    if ((orientation & 2) != 0) {
                        src_y = h - src_y - 1;
                    }
                    new_pixels[dest_x + dest_y * dest_w]
                        = old_pixels[src_x + src_y * w];
                }
            }
            old_pixels = null;
            BufferedImage new_img = new BufferedImage(dest_w, dest_h,
                    BufferedImage.TYPE_INT_ARGB);
            new_img.setRGB(0, 0, dest_w, dest_h, new_pixels, 0, dest_w);
            return Integer.toString(addImage(new_img));
        }
    }

    public boolean usesSharedImages() {
        // TODO: Currently only uses shared sets...
        return true;
    }
    
    /**
     * Checks whether each image has a one to one relationship with the tiles.
     * 
     * @return true if each image is associated with one and only one tile, false otherwise.
     */
    public boolean isOneforOne() {
    	Enumeration keys = images.keys();
    	
    	while(keys.hasMoreElements()) {
    		int key = Integer.parseInt((String)keys.nextElement());
    		int relations = 0;
    		Iterator itr = tiles.iterator();
    		
    		while(itr.hasNext()) {
    			Tile t = (Tile)itr.next();
    			if(t.getImageId() == key) {
    				relations++;
    			}
    		}
    		if(relations != 1) {
    			return false;
    		}
    	}
    	
    	return true;
    }
}
