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

import java.awt.Canvas;
import java.awt.MediaTracker;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;


public class Tile
{
    public static final int T_MOVABLE    = 0x0001;
    public static final int T_PAINFUL    = 0x0002;
    public static final int T_IMPASSABLE = 0x0004;
    public static final int T_ANIMATED   = 0x0008;
    public static final int T_BOTTOM     = 0x0010;
    public static final int T_LINK       = 0x0020;
    public static final int T_NOMCROSS   = 0x0040;
    public static final int T_LANDING    = 0x0080;

    public static final int T_COST       = 0x0100;

    private Image tileImage, scaledImage = null;
    private int id = -1;
    private int stdHeight;
    private int groundHeight;          // Height above ground
    private String name, link;
    private float cost = 0.0f;
    private short flags = T_COST;
    private TileSet tileset;

    public Tile() {
    }

    public Tile(Tile t) {
        cost = t.cost;
        flags = t.flags;
        tileImage = t.tileImage.getScaledInstance(-1, -1, Image.SCALE_DEFAULT);
        groundHeight = getHeight();
    }

    public void setId(int i) {
        if (i >= 0) {
            id = i;
        }
    }

    public void appendFlag(short f) {
        flags = (short)(flags | f);
    }

    public void unsetFlag(short f) {
        flags = (short)((0xFFFF ^ f) & flags);
    }

    public void setFlags(short f) {
        flags = f;
    }

    public void setCost(float c) {
        cost = c;
    }

    public void setLink(String l) {
        if (l.indexOf('\0') > 0) {
            link = l.substring(0, l.indexOf('\0'));
        }
    }

    public void setImage(Image i) throws Exception {
        if (i != null) {
            tileImage = i;
            groundHeight = getHeight();
            if (getWidth() == -1 || getHeight() == -1) {
                throw new Exception(
                        "There was a problem getting info from the image!");
            }
        }
    }

    public void setStandardHeight(int i) {
        stdHeight = i;
    }

    public void setTileSet(TileSet set) {
        tileset = set;
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

    public short getFlags() {
        return flags;
    }

    public float getCost() {
        return cost;
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
            g.drawString("" + id, x, y);
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

    public Image getImage() {
        return tileImage;
    }

    public String getLink() {
        return link;
    }

    public String toString() {
        String out = "";
        out += "Tile: " + id + "(" + getWidth() + "x" + getHeight() + ")";
        return out;
    }
}
