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
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 * @version $Id$
 */
public class BasicTileCutter implements TileCutter
{
    private int nextX, nextY;
    private BufferedImage image;
    private int tileWidth;
    private int tileHeight;
    private int tileSpacing;
    private int offset;

    public BasicTileCutter(int tileWidth, int tileHeight, int tileSpacing,
                           int offset)
    {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.tileSpacing = tileSpacing;
        this.offset = offset;

        // Do initial setup
        nextX = offset + tileSpacing;
        nextY = offset + tileSpacing;
    }

    public String getName() {
    	return "Basic";
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public Image getNextTile() {
        if (nextY + tileHeight <= image.getHeight()) {
            BufferedImage tile =
                image.getSubimage(nextX, nextY, tileWidth, tileHeight);
            nextX += tileWidth + tileSpacing;

            if (nextX + tileWidth > image.getWidth()) {
                nextX = offset + tileSpacing;
                nextY += tileHeight + tileSpacing;
            }

            return tile;
        }

        return null;
    }

    public Dimension getTileDimensions() {
        return new Dimension(tileWidth, tileHeight);
    }

    /**
     * Returns the spacing between tile images.
     * @return the spacing between tile images.
     */
    public int getTileSpacing() {
        return tileSpacing;
    }
}
