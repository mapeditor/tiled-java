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

import java.awt.Rectangle;


/**
 * A layer of a map.
 *
 * @see Map
 */
public class MapLayer implements Cloneable
{
	
	public static final int MIRROR_HORIZONTAL=1;
	public static final int MIRROR_VERTICAL=2;
	
	public static final int ROTATE_90  = 90;
	public static final int ROTATE_180 = 180;
	public static final int ROTATE_270 = 270;
	
    protected int id, heightInTiles, widthInTiles;
    protected Tile map[][];
    private String name;
    private boolean isVisible = true;
    protected Map myMap;
    protected float opacity = 1.0f;
    protected int xOffset, yOffset;

    public MapLayer() {
        setMap(null);
    }

    /**
     * @param w width in tiles
     * @param h height in tiles
     */
    public MapLayer(int w, int h) {
        map = new Tile[h][w];
        heightInTiles = h;
        widthInTiles = w;
    }

	public MapLayer(Rectangle r) {
		setBounds(r);
	}

    public MapLayer(MapLayer ml) {
        id = ml.id;
        xOffset = ml.xOffset;
        yOffset = ml.yOffset;
        widthInTiles = ml.widthInTiles;
        heightInTiles = ml.heightInTiles;

        map = new Tile[heightInTiles][];
        for (int i = 0; i < heightInTiles; i++) {
            map[i] = new Tile[widthInTiles];
            System.arraycopy(ml.map[i], 0, map[i], 0, widthInTiles);
        }
    }

    /**
     * @param m the map this layer is part of
     */
    MapLayer(Map m) {
        setMap(m);
    }

    /**
     * @param m the map this layer is part of
     * @param w width in tiles
     * @param h height in tiles
     */
    MapLayer(Map m, int w, int h) {
        map = new Tile[h][w];
        setMap(m);
        heightInTiles = h;
        widthInTiles = w;
    }

    public void translate(int deltaX, int deltaY) {
        xOffset += deltaX;
        yOffset += deltaY;
    }

    public void rotate(int angle) {
        Tile[][] trans;
        int xtrans = 0, ytrans = 0;

        switch (angle) {
            case ROTATE_90:
                trans = new Tile[widthInTiles][heightInTiles];
                xtrans = heightInTiles - 1;
                break;
            case ROTATE_180:
                trans = new Tile[heightInTiles][widthInTiles];
                xtrans = widthInTiles - 1;
                ytrans = heightInTiles - 1;
                break;
            case ROTATE_270:
                trans = new Tile[widthInTiles][heightInTiles];
                ytrans = widthInTiles - 1;
                break;
            default:
                System.out.println("Unsupported rotation (" + angle + ")");
                return;
        }

        double ra = Math.toRadians(angle);
        int cos_angle = (int)Math.round(Math.cos(ra));
        int sin_angle = (int)Math.round(Math.sin(ra));

        for (int y = 0; y < heightInTiles; y++) {
            for (int x = 0; x < widthInTiles; x++) {
                int xrot = x * cos_angle - y * sin_angle;
                int yrot = x * sin_angle + y * cos_angle;
                trans[yrot + ytrans][xrot + xtrans] = getTileAt(x, y);
            }
        }

        widthInTiles = trans[0].length;
        heightInTiles = trans.length;
        map = trans;
    }

    public void mirror(int dir) {
        Tile[][] mirror = new Tile[heightInTiles][widthInTiles];
        for (int y = 0; y < heightInTiles; y++) {
            for (int x = 0; x < widthInTiles; x++) {
                if (dir == MIRROR_VERTICAL) {
                    mirror[y][x] = map[(heightInTiles - 1) - y][x];
                } else {
                    mirror[y][x] = map[y][(widthInTiles - 1) - x];
                }
            }
        }
        map = mirror;
    }

    public boolean isUsed(Tile t) {
        for (int y = 0; y < heightInTiles; y++) {
            for (int x = 0; x < widthInTiles; x++) {
                if (map[y][x] == t) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setBounds(Rectangle bounds) {
        xOffset = bounds.x;
        yOffset = bounds.y;
        widthInTiles = bounds.width;
        heightInTiles = bounds.height;
        map = new Tile[heightInTiles][widthInTiles];
    }

    /**
     * Creates a diff of the two layers, <code>ml</code> is considered the significant diiference
     * 
     * @param ml
     * @return A new FiniteMapLayer the represents the difference between this layer, and the argument, or null if no difference exists
     */	
    public MapLayer createDiff(MapLayer ml) {		
        if(ml == null)
            return null;
        Rectangle rect = new Rectangle();
        boolean start=false;
        for (int i = yOffset; i < heightInTiles + yOffset; i++) {
            for (int j = xOffset; j < widthInTiles + xOffset; j++) {
                if (ml.getTileAt(j, i) != getTileAt(j, i)) {
                    if (start) {
                        rect.width = Math.abs(j - rect.x);
                        rect.height = Math.abs(i - rect.y);		
                    } else {
                        start = true;
                        rect.x = j;
                        rect.y = i;
                    }
                }

            }
        }

        MapLayer diff = new MapLayer(rect);
        diff.copyFrom(ml);		
        return diff;
    }

    /**
     * Removes any occurences of the given tile from this map layer.
     *
     * @param tile the tile to be removed
     */
    public void removeTile(Tile tile) {
        for (int i = 0; i < heightInTiles; i++) {
            for (int j = 0; j < widthInTiles; j++) {
                if (map[i][j] == tile) {
                    map[i][j] = myMap.getNullTile();
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
            map[ty - yOffset][tx - xOffset] = ti;
        } catch (ArrayIndexOutOfBoundsException e) {
            // Silently ignore out of bounds exception
        }
    }

    /**
     * Sets the map this layer is part of.
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
        xOffset = xOff;
    }

    public void setYOffset(int yOff) {
        yOffset = yOff;
    }

    public void setOffset(int xOff, int yOff) {
        xOffset = xOff;
        yOffset = yOff;
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
            return map[ty - yOffset][tx - xOffset];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Returns layer width in tiles.
     */
    public int getWidth() {
        return widthInTiles;
    }

    /**
     * Returns layer height in tiles.
     */
    public int getHeight() {
        return heightInTiles;
    }

    /**
     * Returns layer bounds.
     */
    public Rectangle getBounds() {
        return new Rectangle(xOffset, yOffset, widthInTiles, heightInTiles);
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
     * Returns wether this layer is visible.
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Merges this layer onto another layer.
     */
    public void mergeOnto(MapLayer other) {
        for (int i = 0; i < heightInTiles; i++) {
            for (int j = 0; j < widthInTiles; j++) {
                if (map[i][j] != myMap.getNullTile()) {
                    other.setTileAt(j+xOffset, i+yOffset, map[i][j]);
                }
            }
        }
    }

    public void copyFrom(MapLayer other) {
        for (int i = 0; i < heightInTiles; i++) {
            for (int j = 0; j < widthInTiles; j++) {				
                setTileAt(j, i, other.getTileAt(j+xOffset,i+yOffset));				
            }
        }
    }

    /**
     * Unlike mergeOnto, copyTo includes the null tile when merging
     * 
     * @param other
     */
    public void copyTo(MapLayer other) {
        for (int i = 0; i < heightInTiles; i++) {
            for (int j = 0; j < widthInTiles; j++) {				
                other.setTileAt(j+xOffset, i+yOffset, map[i][j]);				
            }
        }
    }

    /**
     * Creates a clone of this layer.
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

        return clone;
    }
    
    /**
     * @see MultilayerPlane#resize
     * 
     * @param width
     * @param height
     * @param dx
     * @param dy
     */
    public void resize(int width, int height, int dx, int dy) {
        Tile[][] newMap = new Tile[height][width];

        int maxX = Math.min(width, widthInTiles + dx);
        int maxY = Math.min(height, heightInTiles + dy);

        for (int x = Math.max(0, dx); x < maxX; x++) {
            for (int y = Math.max(0, dy); y < maxY; y++) {
                newMap[y][x] = getTileAt(x - dx, y - dy);
            }
        }

        map = newMap;
        widthInTiles = width;
        heightInTiles = height;
    }
}
