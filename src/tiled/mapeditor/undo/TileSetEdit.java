/*
 *  Tiled Map Editor, (c) 2004-2007
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 * 
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <b.lindeijer@xs4all.nl>
 */

package tiled.mapeditor.undo;

import javax.swing.undo.AbstractUndoableEdit;

import tiled.core.TileSet;

/**
 * @version $Id$
 */
public class TileSetEdit extends AbstractUndoableEdit {

    private TileSet editedSet;
    
}
