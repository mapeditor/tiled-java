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

import tiled.core.MultilayerPlane;
import tiled.util.MersenneTwister;

public class RandomBrush extends ShapeBrush {

    private MersenneTwister mt;
    private double ratio=0.5;
    
    public RandomBrush() {
        super();
        mt = new MersenneTwister(System.currentTimeMillis());
    }
    
    public void setRatio(double r){
        ratio = r;
    }
    
    /**
     * 
     * @see ShapeBrush#commitPaint
     * @return a Rectangle of the bounds of the area that was modified
     * @param mp The multilayer plane that will be modified
     * @param x  The x-coordinate where the click occurred.
     * @param y  The y-coordinate where the click occurred.
     */
    public Rectangle commitPaint(MultilayerPlane mp, int x, int y, int start) {
        Rectangle bounds = shape.getBounds();
        int centerx = (int)(x - (bounds.width / 2));
        int centery = (int)(y - (bounds.height / 2));
        
        //TODO: use the Mersenne Twister to randomize painting the specified tile
        
        return new Rectangle(centerx, centery, bounds.width, bounds.height);
    }
}
