/*
 *  Tiled Map Editor, (c) 2004-2006
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <bjorn@lindeijer.nl>
 */

package tiled.core;

import java.util.EventListener;

/**
 * Gets notified about changes made to a map. This includes events relating to
 * changes in the list of layers or tilesets used by this map.
 *
 * @version $Id$
 */
public interface MapChangeListener extends EventListener
{
    public void mapChanged(MapChangedEvent e);
    
    /// called after a layer has been added. the getLayerIndex() method
    /// of the supplied MapChangedEvent will yield the index of the new layer
    /// (where it has been inserted)
    public void layerAdded(MapChangedEvent e);
    
    /// called after a layer has been removed. the getLayerIndex() method
    /// of the supplied MapChangedEvent will yield the index of the layer that
    /// was removed.
    public void layerRemoved(MapChangedEvent e);
    
    /// called after a layer has been moved around in the map's list of
    /// layers. This changes which layer is on top of which other layer. A move
    /// is basically the
    /// same as removing the layer on one position and inserting it at another.
    /// e.getLayerIndex() returns the layer's new index, e.getOldLayerIndex()
    /// returns the layer's old index. Note that the indices calculate the
    /// same way as for a an remove/add operation required to move the layer
    /// around: remove(N) will remove the layer from position N. removal will
    /// cause all subsequent layers to move one position down. After that, 
    /// add(M) will add the layer at position M (counted from the state of the
    /// sequence after remove(N)
    public void layerMoved(MapChangedEvent e);
    
    /// This event is fired every time the name of a layer is changed.
    public void layerChanged(MapChangedEvent e, MapLayerChangeEvent layerChangeEvent);
    
    public void tilesetAdded(MapChangedEvent e, TileSet tileset);

    public void tilesetRemoved(MapChangedEvent e, int index);

    public void tilesetsSwapped(MapChangedEvent e, int index0, int index1);
}
