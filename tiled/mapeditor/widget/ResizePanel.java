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

package tiled.mapeditor.widget;

import java.awt.Dimension;
import java.awt.event.*;

import javax.swing.*;

import tiled.core.Map;
import tiled.view.MapView;


public class ResizePanel extends JPanel implements MouseListener,
       MouseMotionListener
{
	private MapView inner;
	private Map currentMap;
	private Dimension oldDim, newDim;
	private int offsetX, offsetY;
	private int pressX, pressY;
	private double zoom;
	
	public ResizePanel() {
		super();
		setLayout(new OverlayLayout(this));
		setBorder(BorderFactory.createLoweredBevelBorder());
	}
	
	public ResizePanel(Map map) {
		this();
		inner = map.createView();
		inner.addMouseListener(this);
		inner.addMouseMotionListener(this);
		add(inner);
		zoom = 0.1;
		inner.setZoom(zoom);
		currentMap = map;
		Dimension old = inner.getPreferredSize();
		//TODO: get smaller dimension, zoom based on that...
		
		oldDim = old;
		setSize(old);
	}

	public ResizePanel(Dimension size, Map map) {
		this(map);
		oldDim = newDim = size;
		setSize(size);
	}

	public void moveMap(int x, int y) {
		//snap!
		inner.setLocation((int)(x*(currentMap.getTileWidth()*zoom)), (int) (y*(currentMap.getTileHeight()*zoom)));
	}

	public void setNewDimensions(Dimension n) {
		newDim = n;
		//TODO: recalc the map size...
	}

	public Dimension getPreferredSize() {
		return oldDim;
	}

	public double getZoom() {
		return zoom;
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {		
		int newOffsetX=offsetX + (e.getX() - pressX), newOffsetY=offsetY + (e.getY() - pressY);

		newOffsetX /= (currentMap.getTileWidth()*zoom);
		newOffsetY /= (currentMap.getTileHeight()*zoom);
		
		if(newOffsetX != offsetX) {
			firePropertyChange("offsetX", offsetX, newOffsetX);
			offsetX = newOffsetX;
		}
		
		if(newOffsetY != offsetY) {
			firePropertyChange("offsetY", offsetY, newOffsetY);
			offsetY = newOffsetY;
		}
	}

	public void mousePressed(MouseEvent e) {
		pressX = e.getX();
		pressY = e.getY();
	}

	public void mouseReleased(MouseEvent e) {
		pressX=0;
		pressY=0;
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

    public void mouseMoved(MouseEvent e) {
        
    }
    
}
