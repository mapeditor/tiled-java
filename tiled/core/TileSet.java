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

import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.JOptionPane;


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
     * Creates a tileset from a tile bitmap.
     *
     * @param imgFile     the filename of the image to be used
     * @param tileWidth   the tile width
     * @param tileHeight  the tile height
     * @param spacing     the amount of spacing between the tiles
     */
    public void importTileBitmap(String imgFile,
            int tileWidth, int tileHeight, int spacing) {
        try {
            tilebmpFile = new File(imgFile).getCanonicalPath();
        } catch (IOException e) {
            tilebmpFile = imgFile;
        }
        Image tilebmp;

        try {
            tilebmp = Toolkit.getDefaultToolkit().getImage(tilebmpFile);
            MediaTracker mediaTracker = new MediaTracker(new Canvas());
            mediaTracker.addImage(tilebmp, 0);
            try {
                mediaTracker.waitForID(0);
            }
            catch (InterruptedException ie) {
                System.err.println(ie);
                return;
            }
            mediaTracker.removeImage(tilebmp);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(),
                    "Error!", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int iw = tilebmp.getWidth(null);
        int ih = tilebmp.getHeight(null);

        if (iw > 0 && ih > 0) {
            for (int y = 0; y < ih; y += tileHeight + spacing) {
                for (int x = 0; x < iw; x += tileWidth + spacing) {
                    BufferedImage tile = new BufferedImage(
                            tileWidth, tileHeight,
                            BufferedImage.TYPE_INT_ARGB);
                    Graphics2D tg = tile.createGraphics();

                    tg.drawImage(tilebmp, 0, 0,
                            tileWidth, tileHeight,
                            x, y, x + tileWidth, y + tileHeight,
                            null);

                    Tile newTile = new Tile();
                    try {
                        newTile.setImage(tile);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, e.getMessage(),
                                "Error!", JOptionPane.ERROR_MESSAGE);
                    }
                    addNewTile(newTile);
                }
            }
        }
    }

    /**
     * Sets the standard width of the tiles in this tileset. Tiles in this
     * tileset are not supposed to have any other width.
     */
    public void setStandardWidth(int width) {
        standardWidth = width;
    }

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

    public void setSource(String source) {
        externalSource = source;
    }

    /**
     * Sets the first global id used by this tileset.
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

    public void addNewTile(Tile t) {
        t.setId(tiles.size());
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
     */
    public int getStandardHeight() {
        return standardHeight;
    }

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
     * Returns the tile id.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the source of this tileset.
     *
     * @return a filename if external tileset or <code>null</code> if internal
     *         tileset
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
