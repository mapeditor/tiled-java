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
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Properties;


/**
 * A layer of a map.
 *
 * @see tiled.core.Map
 * @see tiled.core.MultilayerPlane
 */
public class MapLayer implements Cloneable
{
    public static final int MIRROR_HORIZONTAL = 1;
    public static final int MIRROR_VERTICAL   = 2;

    public static final int ROTATE_90  = 90;
    public static final int ROTATE_180 = 180;
    public static final int ROTATE_270 = 270;

    protected int id;
    protected Tile map[][];
    private String name;
    private boolean isVisible = true;
    private boolean bLocked = false;
    protected Map myMap;
    protected float opacity = 1.0f;
    protected Rectangle bounds;
    private Properties properties;
    private LinkedList boundObjects;

    public MapLayer() {
        bounds = new Rectangle();
        setMap(null);
        properties = new Properties();
        boundObjects = new LinkedList();
    }

    /**
     * @param w width in tiles
     * @param h height in tiles
     */
    public MapLayer(int w, int h) {
        this(new Rectangle(0, 0, w, h));
    }

    public MapLayer(Rectangle r) {
        this();
        setBounds(r);
    }

    public MapLayer(MapLayer ml) {
        this(ml.getBounds());
        id = ml.id;
        name = ml.getName();

        map = new Tile[bounds.height][];
        for (int y = 0; y < bounds.height; y++) {
            map[y] = new Tile[bounds.width];
            System.arraycopy(ml.map[y], 0, map[y], 0, bounds.width);
        }
    }

    /**
     * @param m the map this layer is part of
     */
    MapLayer(Map m) {
        this();
        setMap(m);
    }

    /**
     * @param m the map this layer is part of
     * @param w width in tiles
     * @param h height in tiles
     */
    public MapLayer(Map m, int w, int h) {
        this(w, h);
        setMap(m);
    }

    /**
     * Performs a linear translation of this layer by (<i>dx, dy</i>).
     *
     * @param dx
     * @param dy
     */
    public void translate(int dx, int dy) {
        bounds.x += dx;
        bounds.y += dy;
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

    /**
     * Sets the bounds (in tiles) to the specified Rectangle. <b>Caution:</b>
     * this causes a reallocation of the data array, and all previous data is
     * lost.
     *
     * @param bounds
     */
    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
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

        Rectangle r = null;

        for (int y = bounds.y; y < bounds.height + bounds.y; y++) {
            for (int x = bounds.x; x < bounds.width + bounds.x; x++) {
                if (ml.getTileAt(x, y) != getTileAt(x, y)) {
                    if (r != null) {
                        r.add(x, y);
                    } else {
                        r = new Rectangle(new Point(x, y));
                    }
                }
            }
        }

        if (r != null) {
            MapLayer diff = new MapLayer(
                    new Rectangle(r.x, r.y, r.width + 1, r.height + 1));
            diff.copyFrom(ml);
            return diff;
        } else {
            return new MapLayer();
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
     * Sets the id for this layer. If this layer doesn't have a name yet, or
     * its name is currently based on its id, a new name is created with the
     * given id.
     *
     * @param id the new id for this layer
     * @deprecated
     */
    public void setId(int id) {
        if (name == null || name.equalsIgnoreCase("layer " + this.id)) {
            setName("Layer " + id);
        }
        this.id = id;
    }

    /**
     * Sets the name of this layer.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
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
     * Sets the map this layer is part of.
     *
     * @param m the Map object
     */
    public void setMap(Map m) {
        myMap = m;
    }

    /**
     * Sets layer opacity. If it is different from the previous value and the
     * layer is visible, a MapChangedEvent is fired.
     *
     * @param opacity the new opacity for this layer
     */
    public void setOpacity(float opacity) {
        if (this.opacity != opacity) {
            this.opacity = opacity;

            if (isVisible() && myMap != null) {
                myMap.fireMapChanged();
            }
        }
    }

    /**
     * Sets the visibility of this map layer. If it changes from its current
     * value, a MapChangedEvent is fired.
     *
     * @param visible <code>true</code> to make the layer visible;
     *                <code>false</code> to make it invisible
     */
    public void setVisible(boolean visible) {
        if (isVisible != visible) {
            isVisible = visible;
            if (myMap != null) {
                myMap.fireMapChanged();
            }
        }
    }

    public void setXOffset(int xOff) {
        bounds.x = xOff;
    }

    public void setYOffset(int yOff) {
        bounds.y = yOff;
    }

    public void setOffset(int xOff, int yOff) {
        bounds.x = xOff;
        bounds.y = yOff;
    }

    /**
     * Returns the id of this layer.
     *
     * @deprecated
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the name of this layer.
     *
     * @return a java.lang.String of the name of the layer
     */
    public String getName() {
        return name;
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
     * Returns layer width in tiles.
     */
    public int getWidth() {
        return bounds.width;
    }

    /**
     * Returns layer height in tiles.
     */
    public int getHeight() {
        return bounds.height;
    }

    /**
     * Returns layer bounds in tiles.
     *
     * @return a java.awt.Rectangle object with the bounds in tiles of the
     *         layer
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * A convenience method to check if a point in tile-space is within
     * the layer boundaries.
     *
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     * @return <code>true</code> if the point (x,y) is within the layer
     *         boundaries, <code>false</code> otherwise.
     */
    public boolean contains(int x, int y) {
        return bounds.contains(x,y);
    }

    /**
     * Returns layer opacity.
     *
     * @return layer opacity, ranging from 0.0 to 1.0
     */
    public float getOpacity() {
        return opacity;
    }

    /**
     * Returns whether this layer is visible.
     *
     * @return <code>true</code> if the layer is visible, <code>false</code>
     * otherwise.
     */
    public boolean isVisible() {
        return isVisible;
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
                    other.setTileAt(x, y, tile);
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
                setTileAt(x, y, other.getTileAt(x, y));
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
                    setTileAt(x, y, other.getTileAt(x, y));
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
                other.setTileAt(x, y, getTileAt(x, y));
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
        MapLayer clone = null;
        clone = (MapLayer)super.clone();

        // Clone the layer data
        clone.map = new Tile[map.length][];
        for (int i = 0; i < map.length; i++) {
            clone.map[i] = new Tile[map[i].length];
            System.arraycopy(map[i], 0, clone.map[i], 0, map[i].length);
        }

        // Create a new bounds object
        clone.bounds = new Rectangle(bounds);

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

    /**
     * Get the locked status of the layer.
     *
     * @see MapLayer#setLocked(boolean)
     */
    public boolean getLocked() {
        return bLocked;
    }

    /**
     * Set the locked status of the layer. A locked layer can't be edited.
     *
     * @param lock <code>true</code> to lock the layer, <code>false</code> to
     *             unlock the layer
     */
    public void setLocked(boolean lock) {
        bLocked = lock;
    }

    private boolean checkPermission() {
        return (!bLocked);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key,value);
    }

    public void setProperties(Properties p) {
        properties = p;
    }

    public Properties getAllProperties() {
        return properties;
    }

    public Enumeration getProperties() {
        return properties.keys();
    }

    public String getPropertyValue(String key) {
        return properties.getProperty(key);
    }

    public void bindObject(MapObject o) {
        boundObjects.add(o);
    }

    public void unbindObject(MapObject o) {
        boundObjects.remove(o);
    }

    public void unbindAll() {
        boundObjects.clear();
    }
}
