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

import tiled.core.ObjectGroup;
import tiled.core.MapObject;
import tiled.mapeditor.Resources;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;

/**
 * Removes an object from an object group.
 */
public class RemoveObjectEdit extends AbstractUndoableEdit
{
    private final ObjectGroup objectGroup;
    private final MapObject mapObject;

    public RemoveObjectEdit(ObjectGroup objectGroup, MapObject mapObject) {
        this.objectGroup = objectGroup;
        this.mapObject = mapObject;
    }

    public void undo() throws CannotUndoException {
        super.undo();
        objectGroup.addObject(mapObject);
    }

    public void redo() throws CannotRedoException {
        super.redo();
        objectGroup.removeObject(mapObject);
    }

    public String getPresentationName() {
        return Resources.getString("action.object.remove.name");
    }
}
