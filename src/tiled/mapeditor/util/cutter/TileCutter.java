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

/**
 * @version $Id$
 */
public interface TileCutter
{
    public void setImage(Image image);
    public Image getNextTile() throws Exception;
    public Dimension getDimensions();
    public String getName();
}
