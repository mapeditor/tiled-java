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

package tiled.mapeditor.util.cutter;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 * @version $Id$
 */
public class BasicTileCutter implements TileCutter
{
    private int nextX, nextY;
    private BufferedImage image;
    private int tileWidth, tileHeight, frame, offset;

    public BasicTileCutter(int tileWidth, int tileHeight, int frame,
                           int offset)
    {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.frame = frame;
        this.offset = offset;

        // Do initial setup
        nextX = offset + frame;
        nextY = offset + frame;
    }

    public String getName() {
    	return "Basic";
    }

    public void setImage(Image image) {
        int iw = image.getWidth(null);
        int ih = image.getHeight(null);
        this.image = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
        //FIXME: although faster, the following doesn't seem to handle alpha on some platforms...
        //GraphicsConfiguration config =
        //    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        //Image tilesetImage = config.createCompatibleImage(tileWidth, tileHeight);
        //Graphics tg = tilesetImage.getGraphics();
        Graphics2D tg = this.image.createGraphics();

        tg.drawImage(image,
                0, 0, iw, ih,
                0, 0, iw, ih,
                null);
    }

    public Image getNextTile() throws Exception {
        if (nextY + tileHeight <= image.getHeight()) {
            BufferedImage tile =
                image.getSubimage(nextX, nextY, tileWidth, tileHeight);
            nextX += tileWidth + frame;

            if (nextX + tileWidth > image.getWidth()) {
                nextX = offset + frame;
                nextY += tileHeight + frame;
            }

            return tile;
        }

        return null;
    }

    public Dimension getDimensions() {
        return new Dimension(tileWidth, tileHeight);
    }
}
