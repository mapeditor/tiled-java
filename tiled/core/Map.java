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

import java.util.*;
import javax.swing.event.EventListenerList;

import tiled.mapeditor.util.*;


public class Map extends TiledEntity implements Cloneable
{
    public static final int MDO_ORTHO   = 1;
    public static final int MDO_ISO     = 2;
    public static final int MDO_OBLIQUE = 3;
    public static final int MDO_HEX     = 4;

    private Vector tilesets;
    private MapObject objs;
    private Vector layers;
    int major_rev, minor_rev, id;
    int widthInTiles = 0, heightInTiles = 0;
    int defaultTileWidth, defaultTileHeight;
    int totalObjects = 0;
    int lit = 1;
    int orientation = MDO_ORTHO;
    EventListenerList mapChangeListeners;
    Properties properties;
    String filename;
    String name;

    /**
     * @param width  The map width in tiles.
     * @param height The map height in tiles.
     */
    public Map(int width, int height) {
        init();
        widthInTiles = width;
        heightInTiles = height;
    }

    private void init() {
        mapChangeListeners = new EventListenerList();
        layers = new Vector();
        properties = new Properties();
        tilesets = new Vector();
    }


    /**
     * Adds a change listener. The listener will be notified when the map
     * changes in certain ways.
     *
     * @see MapChangeListener#mapChanged(MapChangedEvent)
     */
    public void addMapChangeListener(MapChangeListener l) {
        mapChangeListeners.add(MapChangeListener.class, l);
    }

    /**
     * Removes a change listener.
     */
    public void removeMapChangeListener(MapChangeListener l) {
        mapChangeListeners.remove(MapChangeListener.class, l);
    }

    /**
     * Notifies all registered map change listeners about a change.
     */
    protected void fireMapChanged() {
        Object[] listeners = mapChangeListeners.getListenerList();
        MapChangedEvent event = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == MapChangeListener.class) {
                if (event == null) event = new MapChangedEvent(this);
                ((MapChangeListener)listeners[i + 1]).mapChanged(event);
            }
        }
    }

    /**
     * Adds a layer to the map.
     */
    public MapLayer addLayer(MapLayer l) {
        layers.add(l);
        l.setMap(this);
        fireMapChanged();
        return l;
    }

	public void addLayerAfter(MapLayer l) {
		//TODO: Implement Map#addLayerAfter
	}

	public void addAllLayers(Collection c) {
		layers.addAll(c);
		fireMapChanged();
	}

    /**
     * Adds a new layer to the map.
     */
    public MapLayer addLayer() {
        MapLayer layer = new MapLayer(this, widthInTiles, heightInTiles);
        layer.setId(layers.size());
        return addLayer(layer);
    }

    /**
     * Adds a Tileset to this Map. If the set is already attached to this map,
     * <code>addTileset</code> simply returns.
     *   
     * @param s	a tileset to add
     */
    public void addTileset(TileSet s) {
        if (s == null || tilesets.indexOf(s) > -1) {
            return;
        }

        Tile t = s.getTile(0);

        if (t != null) {
            int tileWidth = t.getWidth();
            int tileHeight = t.getHeight();
            if (tileWidth != defaultTileWidth) {
                if (defaultTileWidth == 0) {
                    defaultTileWidth = tileWidth;
                    defaultTileHeight = tileHeight;
                }
            }
        }

        s.setStandardHeight(defaultTileHeight);
        s.setStandardWidth(defaultTileWidth);
        tilesets.add(s);
        fireMapChanged();
    }

    public void removeTileset(TileSet s) {
        // Sanity check
        if (tilesets.indexOf(s) == -1) {
            return;
        }

        // Go through the map and remove any instances of the tiles in the set
        for (int i = 0; i < s.getTotalTiles(); i++) {
            ListIterator itr = getLayers();
            Tile tile = s.getTile(i);
            while (itr.hasNext()) {
                MapLayer ml = (MapLayer)itr.next();
                ml.removeTile(tile);
            }
        }

        tilesets.remove(s);
        fireMapChanged();
    }

    public void addObject(MapObject o) {
        MapObject temp = objs;

        if (objs == null) {
            o.setId(getTotalObjects() + 1);
            objs = o;
        } else {
            while (temp.next() != null) {
                temp = temp.next();
            }
            temp.setNext(o);
        }
    }

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

	public Enumeration getProperties() {
		return properties.keys();
	}
	
	public String getPropertyValue(String key) {
		return properties.getProperty(key);
	}

	public void setProperties(Properties prop) {
		properties=prop;
	}

    /**
     * Removes the map layer at the specified index. Layers above this layer
     * will move down to fill the gap.
     *
     * @param index Index of layer to be removed.
     */
    public MapLayer removeLayer(int index) {
        MapLayer layer = (MapLayer)layers.remove(index);
        fireMapChanged();
        return layer;
    }

    public void removeAllLayers() {
	layers.removeAllElements();
	fireMapChanged();
    }

    public void swapLayerUp(int index) throws Exception {
        if (index + 1 == layers.size()) {
            throw new Exception(
                    "Can't swap up when already at the top.");
        }

        MapLayer hold = (MapLayer)layers.get(index + 1);
        layers.set(index + 1, getLayer(index));
        layers.set(index, hold);
        fireMapChanged();
    }

    public void swapLayerDown(int index) throws Exception {
        if (index - 1 < 0) {
            throw new Exception(
                    "Can't swap down when already at the bottom.");
        }

        MapLayer hold = (MapLayer)layers.get(index - 1);
        layers.set(index - 1, getLayer(index));
        layers.set(index, hold);
        fireMapChanged();
    }

    public void mergeLayerDown(int index) throws Exception {
        if (index - 1 < 0) {
            throw new Exception(
                    "Can't merge down bottom layer.");
        }

        getLayer(index).mergeOnto(getLayer(index - 1));
        removeLayer(index);
        fireMapChanged();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setTileWidth(int width) {
        defaultTileWidth = width;
    }

    public void setTileHeight(int height) {
        defaultTileHeight = height;
    }

    public void setTotalLayers(int nr) {
        while (layers.size() < nr) {
            addLayer();
        }
    }

    /**
     * Resizes this map. The (dx, dy) pair determines where the original map
     * should be positioned on the new area.
     *
     * @param width  The new width of the map.
     * @param height The new height of the map.
     * @param dx     The shift in x direction in tiles.
     * @param dy     The shift in y direction in tiles.
     */
    public void resize(int width, int height, int dx, int dy) {
        // TODO: Implement Map#resize
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
        // TODO: fire mapChangedNotification about orientation change
    }

    public String getFilename() {
        return filename;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns a vector with the currently loaded tilesets.
     */
    public Vector getTilesets() {
        return tilesets;
    }

	/**
	* Retrieves the designated "Blank" or "Null" tile
	*
	*@return returns designated Null tile, or null by default
	*/
	public Tile getNullTile() {
		return null;
	}

    /**
     * Get the tile set that matches the given global tile id, only to be used
     * when loading a map.
     */
    public TileSet findTileSetForTileGID(int gid) {
        Iterator itr = tilesets.iterator();
        TileSet has = null;
        while (itr.hasNext()) {
            TileSet ts = (TileSet)itr.next();
            if (ts.getFirstGid() <= gid) {
                has = ts;
            }
        }
        return has;
    }

    public MapLayer getLayerById(int i) {
        MapLayer temp = null;
        Iterator li = layers.iterator();

        while (li.hasNext()) {
            temp = (MapLayer)li.next();
            if (temp.getId() == i) {
                break;
            }
        }
        return temp;
    }

    public MapLayer getLayer(int i) {
    	try {
        	return (MapLayer)layers.get(i);
    	} catch (ArrayIndexOutOfBoundsException e) {
    	}
    	return null;
    }

    public ListIterator getLayers() {
        return layers.listIterator();
    }

    /**
     * Returns width of map in tiles.
     */
    public int getWidth() {
        return widthInTiles;
    }

    /**
     * Returns height of map in tiles.
     */
    public int getHeight() {
        return heightInTiles;
    }

    /**
     * Returns default tile width for this map.
     */
    public int getTileWidth() {
        return defaultTileWidth;
    }

    /**
     * Returns default tile height for this map.
     */
    public int getTileHeight() {
        return defaultTileHeight;
    }

    /**
     * Returns the height of the highest tile in all tilesets.
     */
    public int getTileHeightMax() {
        int maxHeight = 0;
        Iterator itr = tilesets.iterator();

        while (itr.hasNext()) {
            int height = ((TileSet)itr.next()).getTileHeightMax();
            if (height > maxHeight) {
                maxHeight = height;
            }
        }

        return maxHeight;
    }

    /**
     * Returns the sum of the size of each tile set.
     */
    public int getTotalTiles() {
        int totalTiles = 0;
        Iterator itr = tilesets.iterator();

        while (itr.hasNext()) {
            TileSet cur = (TileSet)itr.next();
            totalTiles += cur.getTotalTiles();
        }

        return totalTiles;
    }

    /**
     * Returns the amount of layers on the map.
     */
    public int getTotalLayers() {
        return layers.size();
    }

    /**
     * Returns the amount of objects on the map.
     */
    public int getTotalObjects() {
        return totalObjects;
    }

    /**
     * Creates a MapView instance that will render the map in the right
     * orientation.
     */
    public MapView createView() {
        MapView mapView = null;

        if (orientation == MDO_ISO) {
            mapView = new IsoMapView(this);
        } else if (orientation == MDO_ORTHO) {
            mapView = new OrthoMapView(this);
        } else if (orientation == MDO_HEX) {
            mapView = new HexMapView(this);
        } else if (orientation == MDO_OBLIQUE) {
            mapView = new ObliqueMapView(this);
        }

        return mapView;
    }

    /**
     * Returns the orientation of this map. Orientation will be one of
     * {@link Map#MDO_ISO}, {@link Map#MDO_ORTHO}, {@link Map#MDO_HEX} and
     * {@link Map#MDO_OBLIQUE}.
     */
    public int getOrientation() {
        return orientation;
    }

    public String toString() {
        String sout = new String();
        sout += "Current data: map is v"+major_rev+"."+minor_rev+"\n id: "+id;
        sout += "\ntotal layers: " + layers.size();
        sout += "\nobjects in map: " + totalObjects;
        sout += "\ntile dimensions: " + defaultTileWidth + "x" +
            defaultTileHeight;
        sout += "\nmap dimensions: " + widthInTiles + "x" + heightInTiles;
        return sout;
    }

    public boolean inBounds(int x, int y) {
        return (x >= 0 && y >= 0 &&
                x < this.widthInTiles && y < this.heightInTiles);
    }
}
