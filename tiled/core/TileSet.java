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
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;


public class TileSet
{
    private Vector tiles;  // TODO: Change to a LinkedList or HashMap...
    private int id;
    private int firstGid;
    private int standardHeight;
    private int standardWidth;
    private Image setImage;
    private String externalSource, tilebmpFile;
    private String name;

    public TileSet() {
        tiles = new Vector();
    }

    /**
     * Creates a tileset from a tile bitmap. This is a tile-cutter.
     *
     * @param imgFilename the filename of the image to be used
     * @param tileWidth   the tile width
     * @param tileHeight  the tile height
     * @param spacing     the amount of spacing between the tiles
     */
    public void importTileBitmap(String imgFilename,
            int tileWidth, int tileHeight, int spacing) throws Exception{
        File imgFile = null;
        try {
            imgFile = new File(imgFilename);
            tilebmpFile = imgFile.getCanonicalPath();
        } catch (IOException e) {
            tilebmpFile = imgFilename;
        }

        BufferedImage tilebmp = null;

        tilebmp = ImageIO.read(imgFile);

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

                    Tile newTile = new Tile();
                    newTile.setImage(tile);
                    addNewTile(newTile);
                }
            }
        }
    }

    /**
     * Sets the standard width of the tiles in this tileset. Tiles in this
     * tileset are not recommended to have any other width.
     */
    public void setStandardWidth(int width) {
        standardWidth = width;
    }

	/**
	 * Sets the standard height of the tiles in this tileset. This is used
	 * to calculate the drawing position of tiles with a height above the 
	 * standard height.
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
	 * Sets the URI path of the external source of this tile set.
	 * By setting this, the set is implied to be external in all 
	 * other operations.
	 * 
	 * @param source
	 */
    public void setSource(String source) {
        externalSource = source;
    }

    /**
     * Sets the first global id used by this tileset.
     * 
     * @param the first global id
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
	 * Adds the tile to the setting the id of the tile only if the 
	 * current value of id is -1.
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
        t.setTileSet(this);
        t.setStandardHeight(standardHeight);
        if (standardWidth < t.getWidth()) {
            standardWidth = t.getWidth();
        }
    }

    /**
     * This method takes a new Tile object as argument, and in addition to 
     * the functionality of <code>addTile()</code>, sets the id of the tile. 
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
     * indices.
     */
    public void removeTile(int i) {
        tiles.set(i, null);
    }

    /**
     *
     * @return the total size of the internal Vector
     */
    public int getTotalTiles() {
        return tiles.size();
    }

    /**
     * Returns the standard width of tiles in this tileset. All tiles in a
     * tileset should be the same width.
     */
    public int getStandardWidth() {
        return standardWidth;
    }

    /**
     * Returns the standard height of tiles in this tileset. Not all tiles in
     * a tileset are required to have the same height.
     * 
     * @return the standard height as previously set with a call to TileSet#setStandardHeight 
     */
    public int getStandardHeight() {
        return standardHeight;
    }

	/**
	 * Iterates through the set an retrieves the larges height value.
	 * 
	 * @return the maximum hieght of any tile
	 */
    public int getTileHeightMax() {
        int maxHeight = 0;
        Iterator itr = tiles.iterator();
        while (itr.hasNext()) {
            Tile t = (Tile)itr.next();
            if(t != null && t.getHeight() > maxHeight) {
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
     * Returns the name of this tileset.
     */
    public String getName() {
        return name;
    }
    
    public String toString() {
    	return name + " ["+getTotalTiles()+"]";
    }
}
