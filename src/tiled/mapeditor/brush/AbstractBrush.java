/*
 *  Tiled Map Editor, (c) 2004-2006
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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import tiled.core.MultilayerPlane;
import tiled.view.MapView;


public abstract class AbstractBrush extends MultilayerPlane implements Brush
{
    protected int numLayers = 1;
    protected MultilayerPlane affectedMp;
    protected int sx, sy;
    protected boolean paintingStarted = false;
	protected int initLayer;
    
    public AbstractBrush() {
    }

    public AbstractBrush(AbstractBrush ab) {
        numLayers = ab.numLayers;
    }

    /**
     * This will set the number of layers to affect, the default is 1 - the
     * layer specified in commitPaint.
     *
     * @see Brush#commitPaint(MultilayerPlane, int, int, int)
     * @param num   the number of layers to affect.
     */
    public void setAffectedLayers(int num) {
        numLayers = num;
    }

    public int getAffectedLayers() {
        return numLayers;
    }
    
    public void startPaint(MultilayerPlane mp, int x, int y, int button, int layer) {
    	affectedMp = mp;
		initLayer = layer;
    	paintingStarted = true;
    }
    
    public Rectangle doPaint(int x, int y) throws Exception {
    	if(!paintingStarted) throw new Exception("Attempted to call doPaint() without calling startPaint()!");
    	return null;
    }
    
    public void endPaint() {
    	paintingStarted = false;
    }
    
    public void drawPreview(Graphics2D g2d, int x, int y, MapView mv) {
    	sx = x;
    	sy = y;
    	drawPreview(g2d, mv);
    }

	public abstract Shape getShape();
}
