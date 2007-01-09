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
 * Cuts tiles from a tileset image according to a regular rectangular pattern.
 * Supports a variable spacing between tiles and an offset from the origin.
 *
 * @version $Id$
 */
public class BasicTileCutter implements TileCutter
{
    private int nextX, nextY;
    private BufferedImage image;
    private final int tileWidth;
    private final int tileHeight;
    private final int tileSpacing;
    private final int offset;

    public BasicTileCutter(int tileWidth, int tileHeight, int tileSpacing,
                           int offset)
    {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.tileSpacing = tileSpacing;
        this.offset = offset;

        reset();
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
                nextX = offset;
                nextY += tileHeight + tileSpacing;
            }

            return tile.getScaledInstance(-1, -1, Image.SCALE_DEFAULT);
        }

        return null;
    }

    public void reset() {
        nextX = offset;
        nextY = offset;
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

    /**
     * Returns the number of tiles per row in the tileset image.
     * @return the number of tiles per row in the tileset image.
     */
    public int getTilesPerRow() {
        return (image.getWidth() + tileSpacing) / (tileWidth + tileSpacing);
    }
}
