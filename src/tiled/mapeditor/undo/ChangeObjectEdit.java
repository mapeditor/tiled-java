/*
 *  Tiled Map Editor, (c) 2008
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
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import tiled.core.MapObject;
import tiled.mapeditor.Resources;

/**
 * Changes the attributes and properties of an object.
 *
 * @version $Id$
 */
public class ChangeObjectEdit extends AbstractUndoableEdit
{
    private final MapObject mapObject;

    public ChangeObjectEdit(MapObject mapObject) {
        this.mapObject = mapObject;
    }

    public void undo() throws CannotUndoException {
        super.undo();
    }

    public void redo() throws CannotRedoException {
        super.redo();
    }

    public String getPresentationName() {
        return Resources.getString("action.object.change.name");
    }
}
