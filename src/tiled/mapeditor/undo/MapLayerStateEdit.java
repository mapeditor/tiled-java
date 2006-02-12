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

package tiled.mapeditor.undo;

import java.util.Vector;
import javax.swing.undo.*;

import tiled.core.*;


public class MapLayerStateEdit extends AbstractUndoableEdit
{
    private Map map;
    private Vector layersBefore;
    private Vector layersAfter;
    private String name;

    public MapLayerStateEdit(Map m, Vector before, Vector after, String name) {
        map = m;
        layersBefore = before;
        layersAfter = after;
        this.name = name;
    }

    public void undo() throws CannotUndoException {
        super.undo();
        map.setLayerVector(layersBefore);
    }

    public void redo() throws CannotRedoException {
        super.redo();
        map.setLayerVector(layersAfter);
    }

    public String getPresentationName() {
        return name;
    }
}
