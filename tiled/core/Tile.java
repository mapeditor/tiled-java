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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class Tile
{
    private Image tileImage, scaledImage = null;
    private int id = -1;
    private int stdHeight;
    private int groundHeight;          // Height above ground
    private Properties properties;
    private TileSet tileset;

    public Tile() {
    	properties = new Properties();
    }

    public Tile(Tile t) {
        properties = (Properties)t.properties.clone();        
        if (t.tileImage != null) {
            tileImage = t.tileImage.getScaledInstance(
                    -1, -1, Image.SCALE_DEFAULT);
        }
        groundHeight = getHeight();
    }

    /**
     * Sets the id of the tile as long as it is at least 0.
     */
    public void setId(int i) {
        if (i >= 0) {
            id = i;
        }
    }

    /**
     * Changes the image of the tile as long as it is not null.
     */
    public void setImage(Image i) {
        tileImage = i;
        groundHeight = getHeight();
    }

    public void setStandardHeight(int i) {
        stdHeight = i;
    }

    public void setTileSet(TileSet set) {
        tileset = set;
    }

	public void setProperty(String key, String value) {
		properties.setProperty(key,value);
	}

	public void setProperties(Properties p) {
		properties = p;
	}

	public Enumeration getProperties() {
		return properties.keys();
	}

	public String getPropertyValue(String key) {
		return properties.getProperty(key);
	}

    public int getId() {
        return id;
    }

    public int getGid() {
        if (tileset != null) {		
            return id + tileset.getFirstGid();
        }
        return id;
    }

    public TileSet getTileSet() {
        return tileset;
    }

    public void drawRaw(Graphics g, int x, int y, double zoom) {
        if (tileImage != null) {
            if (zoom != 1.0) {
                int h = (int)(tileImage.getHeight(null) * zoom);
				if (scaledImage == null || scaledImage.getHeight(null) != h) {
					scaledImage = tileImage.getScaledInstance(
							(int)(getWidth() * zoom), h,
							BufferedImage.SCALE_SMOOTH);
					MediaTracker mediaTracker = new MediaTracker(new Canvas());
					mediaTracker.addImage(scaledImage, 0);
					try {
						mediaTracker.waitForID(0);
					}
					catch (InterruptedException ie) {
						System.err.println(ie);
						return;
					}
					mediaTracker.removeImage(scaledImage);
				}
                g.drawImage(scaledImage, x, y, null);
            } else {
                g.drawImage(tileImage, x, y, null);
            }
        } else {
        	//TODO: drawing ids when there is no tile data should be a config option.
            //g.drawString("" + id, x, y);
        }
    }

    public void draw(Graphics g, int x, int y, double zoom) {
        // Make sure tiles longer than the standard height are drawn with
        // bottom aligned with other tiles.
        int h = (int)(getHeight() * zoom);
        int gnd_h = (int)(groundHeight * zoom);
        int std_h = (int)(stdHeight * zoom);

        if (h > std_h && std_h != 0) {
            y -= h - std_h;
        }

        // Invoke raw draw function
        drawRaw(g, x, y + (h - gnd_h), zoom);
    }

    public int getWidth() {
        if (tileImage != null) {
            return tileImage.getWidth(null);
        } else {
            return 0;
        }
    }

    public int getHeight() {
        if (tileImage != null) {
            return tileImage.getHeight(null);
        } else {
            return 0;
        }
    }

    /**
     * Returns the tile image.
     */
    public Image getImage() {
        return tileImage;
    }

    public int getImageId() {
        return 0;
    }

    /**
     * Returns a scaled instance of the tile image.
     */
    public Image getScaledImage(double zoom) {
        if (tileImage != null) {
            return tileImage.getScaledInstance(
                    (int)(getWidth() * zoom), (int)(getHeight() * zoom),
                    BufferedImage.SCALE_SMOOTH);
        } else {
            return null;
        }
    }

    public String toString() {
        String out = "";
        out += "Tile: " + id + "(" + getWidth() + "x" + getHeight() + ")";
        return out;
    }
}
