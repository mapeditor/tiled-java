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

package tiled.view;

import java.awt.*;
import java.util.Iterator;
import javax.swing.Scrollable;
import javax.swing.JPanel;
import java.awt.geom.PathIterator;

import tiled.core.*;
import tiled.mapeditor.selection.SelectionLayer;


/**
 * The base class for map views.
 */
public abstract class MapView extends JPanel implements Scrollable
{
    public static final int PF_GRIDMODE = 0x00000001;
    public static final int PF_BOUNDARYMODE = 0x00000002;
	
    public static int ZOOM_NORMALSIZE = 3;

    protected Map myMap;
    protected int modeFlags = 0;
    protected double zoom = 1.0;
    protected int zoomLevel = 3;
    protected static double[] zoomLevels = {
        0.25, 0.5, 0.75, 1.0, 1.5, 2.0, 3.0, 4.0
    };

    private SmoothZoomer smoothZoomer;


    public MapView(Map m) {
        myMap = m;
        setSize(getPreferredSize());
    }


    public void enableMode(int modeModifier) {
        modeFlags |= modeModifier;
        setSize(getPreferredSize());
    }

    public void disableMode(int modeModifier) {
        modeFlags &= ~modeModifier;
        setSize(getPreferredSize());
    }

    public void toggleMode(int modeModifier) {
        modeFlags ^= modeModifier;
        setSize(getPreferredSize());
    }

    public boolean getMode(int modeModifier) {
        return (modeFlags & modeModifier) != 0;
    }


    // Zooming

    public boolean zoomIn() {
        if (zoomLevel < zoomLevels.length - 1) {
            setZoomLevel(zoomLevel + 1);
        }

        return zoomLevel < zoomLevels.length - 1;
    }

    public boolean zoomOut() {
        if (zoomLevel > 0) {
            setZoomLevel(zoomLevel - 1);
        }

        return zoomLevel > 0;
    }

    public void setZoom(double zoom) {
        if (zoom > 0) {
            this.zoom = zoom;
            setSize(getPreferredSize());
        }
    }

    public void setZoomLevel(int zoomLevel) {
        if (zoomLevel >= 0 && zoomLevel < zoomLevels.length) {
            this.zoomLevel = zoomLevel;
            //setZoomSmooth(zoomLevels[zoomLevel]);
            setZoom(zoomLevels[zoomLevel]);
        }
    }

    public void setZoomSmooth(double zoom) {
        if (zoom > 0) {
            if (smoothZoomer != null) {
                smoothZoomer.stopZooming();
            }
            smoothZoomer = new SmoothZoomer(this, this.zoom, zoom);
            smoothZoomer.start();
        }
    }

    public double getZoom() {
        return zoom;
    }

    public int getZoomLevel() {
        return zoomLevel;
    }


    // Scrolling

    public abstract Dimension getPreferredSize();

    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    public abstract int getScrollableBlockIncrement(Rectangle visibleRect,
            int orientation, int direction);

    public abstract int getScrollableUnitIncrement(Rectangle visibleRect,
            int orientation, int direction);


    // Painting

    /**
     * Draws all the visible layers of the map.
     */
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        Iterator li = myMap.getLayers();
        MapLayer layer;
        double currentZoom = zoom;
        Rectangle clip = g.getClipBounds();

		g2d.setStroke(new BasicStroke(2.0f));		

        // Do an initial fill
        g.setColor(new Color(64, 64, 64));
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

        while (li.hasNext()) {
        	if((layer = (MapLayer)li.next()) != null) {
	            float opacity = layer.getOpacity();
	            if (layer.isVisible() && opacity > 0.0f) {
	                if (opacity < 1.0f) {
	                    g2d.setComposite(AlphaComposite.getInstance(
	                                AlphaComposite.SRC_ATOP, opacity));
	                } else {
	                    g2d.setComposite(AlphaComposite.SrcOver);
	                }
	                paint(g, layer, currentZoom);
	            }
        	}
        }

		li = myMap.getLayersSpecial();

		while(li.hasNext()) {
			layer = (MapLayer) li.next();
			if (layer.getClass() == SelectionLayer.class) {
				((Graphics2D)g).setComposite(AlphaComposite.getInstance(
									   AlphaComposite.SRC_ATOP, 0.3f));
				g.setColor(((SelectionLayer)layer).getHighlightColor());
			}
			paint(g, layer, currentZoom);
		}

        if (getMode(PF_GRIDMODE)) {
			g2d.setStroke(new BasicStroke());
            g2d.setComposite(AlphaComposite.SrcOver);
            paintGrid(g, currentZoom);
        }		
    }

	protected void paintEdge(MapLayer layer, int x, int y, Graphics g) {
		Polygon grid = createGridPolygon(x,y,0);
		PathIterator itr = grid.getPathIterator(null);
		double nextPoint[] = new double[6], prevPoint[],firstPoint[];
	
		Point p = screenToTileCoords(x, y);
		int tx = p.x;
		int ty = p.y;
		
		itr.currentSegment(nextPoint);
		firstPoint = prevPoint=nextPoint;
		
		//NORTH
		itr.next();
		nextPoint = new double[6];
		itr.currentSegment(nextPoint);
		if(layer.getTileAt(tx,ty-1)==null) {
			g.drawLine((int)prevPoint[0],(int)prevPoint[1],(int)nextPoint[0],(int)nextPoint[1]);
		}
		
		//EAST
		itr.next();
		prevPoint = nextPoint;
		nextPoint = new double[6];
		itr.currentSegment(nextPoint);
		if(layer.getTileAt(tx+1,ty)==null) {
			g.drawLine((int)prevPoint[0],(int)prevPoint[1],(int)nextPoint[0],(int)nextPoint[1]);
		}
		
		// SOUTH
		itr.next();
		prevPoint = nextPoint;
		nextPoint = new double[6];
		itr.currentSegment(nextPoint);
		if(layer.getTileAt(tx,ty+1)==null) {
			g.drawLine((int)prevPoint[0],(int)prevPoint[1],(int)nextPoint[0],(int)nextPoint[1]);
		}
		
		// WEST
		if(layer.getTileAt(tx-1,ty)==null) {
			g.drawLine((int)nextPoint[0],(int)nextPoint[1],(int)firstPoint[0],(int)firstPoint[1]);
		}
	}

    /**
     * Tells this view a certain region of the map needs to be repainted.
     * <p>
     * Same as calling repaint() unless implemented more efficiently in a
     * subclass.
     *
     * @param region the region that has changed in tile coordinates
     */
    public void repaintRegion(Rectangle region) {
        repaint();
    }

    /**
     * Draws a layer. Implemented in a subclass.
     *
     * @param layer the layer to be drawn
     * @param zoom  the zoom level to draw the layer on
     */
    protected abstract void paint(Graphics g, MapLayer layer, double zoom);

    /**
     * Draws the map grid.
     */
    protected abstract void paintGrid(Graphics g, double zoom);
    
    /**
     * Returns a Polygon that matches the grid around the specified <b>Map</b>
     * 
     * @param tx
     * @param ty
     * @param border
     * @return
     */
	protected abstract Polygon createGridPolygon(int tx, int ty, int border);
    
    // Conversion functions

    public abstract Point screenToTileCoords(int x, int y);
}


class SmoothZoomer extends Thread
{
    private MapView mapView;
    private double zoomFrom, zoomTo;
    private boolean keepZooming;

    public SmoothZoomer(MapView view, double from, double to) {
        mapView = view;
        zoomFrom = from;
        zoomTo = to;
        keepZooming = true;
    }

    public void stopZooming() {
        keepZooming = false;
    }

    public void run() {
        long currentTime = System.currentTimeMillis();
        long endTime = currentTime + 500;

        while (keepZooming && currentTime < endTime) {
            double p = Math.sin(
                    (1 - (endTime - currentTime) / 500.0) * Math.PI * 0.5);
            mapView.setZoom(zoomFrom * (1.0 - p) + zoomTo * p);
            currentTime = System.currentTimeMillis();
        }

        if (keepZooming) {
            mapView.setZoom(zoomTo);
        }
    }
}
