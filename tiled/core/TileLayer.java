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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;


public class TileLayer extends MapLayer
{
    protected Tile map[][];

    public TileLayer() {
    	super();
    }

    /**
     * @param w width in tiles
     * @param h height in tiles
     */
    public TileLayer(int w, int h) {
        super(w,h);
    }

    public TileLayer(Rectangle r) {
        super(r);
    }

    public TileLayer(TileLayer ml) {
        super(ml);

        map = new Tile[bounds.height][];
        for (int y = 0; y < bounds.height; y++) {
            map[y] = new Tile[bounds.width];
            System.arraycopy(ml.map[y], 0, map[y], 0, bounds.width);
        }
    }

    /**
     * @param m the map this layer is part of
     */
    TileLayer(Map m) {
        super(m);
    }

    /**
     * @param m the map this layer is part of
     * @param w width in tiles
     * @param h height in tiles
     */
    public TileLayer(Map m, int w, int h) {
        super(w, h);
        setMap(m);
    }

    public void rotate(int angle) {
        Tile[][] trans;
        int xtrans = 0, ytrans = 0;

        if (!checkPermission())
            return;

        switch (angle) {
            case ROTATE_90:
                trans = new Tile[bounds.width][bounds.height];
                xtrans = bounds.height - 1;
                break;
            case ROTATE_180:
                trans = new Tile[bounds.height][bounds.width];
                xtrans = bounds.width - 1;
                ytrans = bounds.height - 1;
                break;
            case ROTATE_270:
                trans = new Tile[bounds.width][bounds.height];
                ytrans = bounds.width - 1;
                break;
            default:
                System.out.println("Unsupported rotation (" + angle + ")");
                return;
        }

        double ra = Math.toRadians(angle);
        int cos_angle = (int)Math.round(Math.cos(ra));
        int sin_angle = (int)Math.round(Math.sin(ra));

        for (int y = 0; y < bounds.height; y++) {
            for (int x = 0; x < bounds.width; x++) {
                int xrot = x * cos_angle - y * sin_angle;
                int yrot = x * sin_angle + y * cos_angle;
                trans[yrot + ytrans][xrot + xtrans] = getTileAt(x, y);
            }
        }

        bounds.width = trans[0].length;
        bounds.height = trans.length;
        map = trans;
    }

    /**
     * Performs a mirroring function on the layer data. Two orientations are
     * allowed: vertical and horizontal.
     *
     * Example: <code>layer.mirror(MapLayer.MIRROR_VERTICAL);</code> will
     * mirror the layer data around a horizontal axis.
     *
     * @param dir the axial orientation to mirror around
     */
    public void mirror(int dir) {
        if (!checkPermission())
            return;

        Tile[][] mirror = new Tile[bounds.height][bounds.width];
        for (int y = 0; y < bounds.height; y++) {
            for (int x = 0; x < bounds.width; x++) {
                if (dir == MIRROR_VERTICAL) {
                    mirror[y][x] = map[(bounds.height - 1) - y][x];
                } else {
                    mirror[y][x] = map[y][(bounds.width - 1) - x];
                }
            }
        }
        map = mirror;
    }

    /**
     * Checks to see if the given Tile is used anywhere in the layer
     *
     * @param t a Tile object to check for
     * @return <code>true</code> if the Tile is used at least once,
     *         <code>false</code> otherwise.
     */
    public boolean isUsed(Tile t) {
        for (int y = 0; y < bounds.height; y++) {
            for (int x = 0; x < bounds.width; x++) {
                if (map[y][x] == t) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setWidth(int w) {
    	super.setWidth(w);
    	resize(w, bounds.height, 0, 0);
    }
    
    /**
     * Sets the height of the map in tiles
     */
    public void setHeight(int h) {
    	super.setHeight(h);
    	resize(bounds.width, h, 0, 0);
    }
    
    /**
     * Sets the bounds (in tiles) to the specified Rectangle. <b>Caution:</b>
     * this causes a reallocation of the data array, and all previous data is
     * lost.
     *
     * @param bounds
     * @see MapLayer#setBounds
     */
    public void setBounds(Rectangle bounds) {
        super.setBounds(bounds);
        map = new Tile[bounds.height][bounds.width];
    }

    /**
     * Creates a diff of the two layers, <code>ml</code> is considered the
     * significant difference.
     *
     * @param ml
     * @return A new MapLayer that represents the difference between this
     *         layer, and the argument, or <b>null</b> if no difference exists.
     */
    public MapLayer createDiff(MapLayer ml) {
        if (ml == null) { return null; }

        if(ml instanceof TileLayer) {
        
	        Rectangle r = null;
	
	        for (int y = bounds.y; y < bounds.height + bounds.y; y++) {
	            for (int x = bounds.x; x < bounds.width + bounds.x; x++) {
	                if (((TileLayer)ml).getTileAt(x, y) != getTileAt(x, y)) {
	                    if (r != null) {
	                        r.add(x, y);
	                    } else {
	                        r = new Rectangle(new Point(x, y));
	                    }
	                }
	            }
	        }
	
	        if (r != null) {
	            MapLayer diff = new TileLayer(
	                    new Rectangle(r.x, r.y, r.width + 1, r.height + 1));
	            diff.copyFrom(ml);
	            return diff;
	        } else {
	            return new TileLayer();
	        }
        } else {
        	return null;
        }
        
    }

    /**
     * Removes any occurences of the given tile from this map layer.
     *
     * @param tile the Tile to be removed
     */
    public void removeTile(Tile tile) {
        if (!checkPermission())
            return;

        for (int y = 0; y < bounds.height; y++) {
            for (int x = 0; x < bounds.width; x++) {
                if (map[y][x] == tile) {
                    map[y][x] = myMap.getNullTile();
                }
            }
        }
    }

    /**
     * Sets the tile at the specified position. Does nothing if (tx, ty) falls
     * outside of this layer.
     *
     * @param tx x position of tile
     * @param ty y position of tile
     * @param ti the tile object to place
     */
    public void setTileAt(int tx, int ty, Tile ti) {
        try {
            if (checkPermission())
                map[ty - bounds.y][tx - bounds.x] = ti;
        } catch (ArrayIndexOutOfBoundsException e) {
            // Silently ignore out of bounds exception
        }
    }

    /**
     * Returns the tile at the specified position.
     *
     * @return tile at position (tx, ty) or <code>null</code> when (tx, ty) is
     *         outside this layer
     */
    public Tile getTileAt(int tx, int ty) {
        try {
            return map[ty - bounds.y][tx - bounds.x];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Returns the first occurance (using top down, left to right search) of
     * the given tile.
     *
     * @param t the tile to look for
     */
    public Point locationOf(Tile t) {
        for (int y = bounds.y; y < bounds.height + bounds.y; y++) {
            for (int x = bounds.x; x < bounds.width + bounds.x; x++) {
                if (getTileAt(x, y) == t) {
                    return new Point(x,y);
                }
            }
        }
        return null;
    }

    /**
     * Replaces all occurances of the Tile <code>find</code> with the Tile
     * <code>replace</code> in the entire layer
     *
     * @param find    the tile to replace
     * @param replace the replacement tile
     */
    public void replaceTile(Tile find, Tile replace) {
        if (!checkPermission())
            return;

        for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
            for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
                if(getTileAt(x,y) == find) {
                    setTileAt(x, y, replace);
                }
            }
        }
    }

    /**
     * Merges the tile data of this layer with the specified layer. The calling
     * layer is considered the significant layer, and will overwrite the data
     * of the argument layer. At cells where the calling layer has no data, the
     * argument layer data is preserved.
     *
     * @param other the insignificant layer to merge with
     */
    public void mergeOnto(MapLayer other) {
        for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
            for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
                Tile tile = getTileAt(x, y);
                if (tile != myMap.getNullTile()) {
                	((TileLayer)other).setTileAt(x, y, tile);
                }
            }
        }
    }

    /**
     * Copy data from another layer onto this layer. Unlike mergeOnto,
     * copyFrom() copies the empty cells as well.
     *
     * @see tiled.core.MapLayer#mergeOnto
     * @param other
     */
    public void copyFrom(MapLayer other) {
        if (!checkPermission())
            return;

        for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
            for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
                setTileAt(x, y, ((TileLayer)other).getTileAt(x, y));
            }
        }
    }

    public void maskedCopyFrom(MapLayer other, Area mask) {
        if (!checkPermission())
            return;

        Rectangle boundBox = mask.getBounds();

        for (int y = boundBox.y; y < boundBox.y + boundBox.height; y++) {
            for (int x = boundBox.x; x < boundBox.x + boundBox.width; x++) {
                if (mask.contains(x,y)) {
                    setTileAt(x, y, ((TileLayer)other).getTileAt(x, y));
                }
            }
        }
    }

    /**
     * Unlike mergeOnto, copyTo includes the null tile when merging
     *
     * @see tiled.core.MapLayer#copyFrom
     * @see tiled.core.MapLayer#mergeOnto
     * @param other the layer to copy this layer to
     */
    public void copyTo(MapLayer other) {
        for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
            for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
                ((TileLayer)other).setTileAt(x, y, getTileAt(x, y));
            }
        }
    }

    /**
     * Creates a copy of this layer.
     *
     * @see java.lang.Object#clone
     * @return a clone of this layer, as complete as possible
     * @exception CloneNotSupportedException
     */
    public Object clone() throws CloneNotSupportedException {
        TileLayer clone = null;
        clone = (TileLayer)super.clone();

        // Clone the layer data
        clone.map = new Tile[map.length][];
        for (int i = 0; i < map.length; i++) {
            clone.map[i] = new Tile[map[i].length];
            System.arraycopy(map[i], 0, clone.map[i], 0, map[i].length);
        }

        return clone;
    }

    /**
     * @see MultilayerPlane#resize
     *
     * @param width  the new width of the layer
     * @param height the new height of the layer
     * @param dx     the shift in x direction
     * @param dy     the shift in y direction
     */
    public void resize(int width, int height, int dx, int dy) {
        if (!checkPermission())
            return;

        Tile[][] newMap = new Tile[height][width];

        int maxX = Math.min(width, bounds.width + dx);
        int maxY = Math.min(height, bounds.height + dy);

        for (int x = Math.max(0, dx); x < maxX; x++) {
            for (int y = Math.max(0, dy); y < maxY; y++) {
                newMap[y][x] = getTileAt(x - dx, y - dy);
            }
        }

        map = newMap;
        bounds.width = width;
        bounds.height = height;
    }
}
