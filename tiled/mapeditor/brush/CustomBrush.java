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

package tiled.mapeditor.brush;

import java.awt.Rectangle;
import java.util.ListIterator;

import tiled.core.MultilayerPlane;
import tiled.core.TileLayer;

public class CustomBrush extends Brush
{
    public CustomBrush() {
        super();
    }

    public CustomBrush(MultilayerPlane m) {
    	this();
    	this.addAllLayers(m.getLayerVector());
    }
    
    /**
     * The custom brush will merge its internal layers onto the layers of the 
     * specified MultilayerPlane.
     * 
     * @see TileLayer#mergeOnto(MapLayer)
     * @see Brush#commitPaint(MultilayerPlane, int, int, int)
     * @param mp         The MultilayerPlane to be affected
     * @param x          The x-coordinate where the user initiated the paint
     * @param y          The y-coordinate where the user initiated the paint
     * @param initLayer  The first layer to paint to.
     * @return The rectangular region affected by the painting  
     */
    public Rectangle commitPaint(MultilayerPlane mp, int x, int y, int initLayer) {
        Rectangle bounds = this.getBounds();
        int centerx = (int)(x - (bounds.width / 2));
        int centery = (int)(y - (bounds.height / 2));
        
        ListIterator itr = getLayers();
        while(itr.hasNext()) {
            TileLayer tl = (TileLayer)itr.next();            
            TileLayer tm = (TileLayer) mp.getLayer(initLayer++);
            tl.setOffset(centerx,centery);
            tl.mergeOnto(tm);
        }
        
        return new Rectangle(centerx, centery, bounds.width, bounds.height);
    }
}
