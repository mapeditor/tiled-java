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

import tiled.core.MapObject;
import tiled.mapeditor.Resources;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;
import java.awt.*;

/**
 * Moves an object.
 */
public class MoveObjectEdit extends AbstractUndoableEdit
{
    private final MapObject mapObject;
    private final Point moveDist;

    public MoveObjectEdit(MapObject mapObject, Point moveDist) {
        this.mapObject = mapObject;
        this.moveDist = moveDist;
    }

    public void undo() throws CannotUndoException {
        super.undo();
        mapObject.translate(-moveDist.x, -moveDist.y);
    }

    public void redo() throws CannotRedoException {
        super.redo();
        mapObject.translate(moveDist.x, moveDist.y);
    }

    public String getPresentationName() {
        return Resources.getString("action.object.move.name");
    }
}
