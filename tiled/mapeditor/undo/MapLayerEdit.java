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

package tiled.mapeditor.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import tiled.core.*;

public class MapLayerEdit implements UndoableEdit
{
    protected Map myMap;
    MapLayer layerUndo = null, layerRedo = null;
    String name;
    protected boolean inProgress = false;

    public MapLayerEdit(Map m) {
        myMap = m;
    }

    public MapLayerEdit(Map m, MapLayer before) {
        myMap = m;
        start(before);
    }

    public MapLayerEdit(Map m, MapLayer before, MapLayer after) {
        myMap = m;
        start(before);
        end(after);
    }

    public void start(MapLayer fml) {
        layerUndo = fml;
        inProgress = true;
    }

    public void end(MapLayer fml) {
        if (!inProgress) {
            new Exception("end called before start").printStackTrace();
        }
        if (fml != null) {
            layerRedo = fml;
            inProgress = false;
        }
    }

    public MapLayer getStart() {
        return layerUndo;
    }

    /* inherited methods */
    public void undo() throws CannotUndoException {
        MapLayer ml = myMap.getLayer(layerUndo.getId());
        if (ml == null) {
            throw new CannotUndoException();
        }
        layerUndo.copyTo(ml);
        ml.setOffset(layerUndo.getBounds().x,layerUndo.getBounds().y);
    }

    public boolean canUndo() {
        if (layerUndo != null && myMap.getLayer(layerUndo.getId()) != null) {
            return true;
        }
        return false;
    }

    public void redo() throws CannotRedoException {
        MapLayer ml = myMap.getLayer(layerRedo.getId());
        if (ml == null) {
            throw new CannotRedoException();
        }
        layerRedo.copyTo(ml);
        ml.setOffset(layerRedo.getBounds().x,layerRedo.getBounds().y);
    }

    public boolean canRedo() {
        if(layerRedo!=null&&myMap.getLayer(layerRedo.getId())!=null) {
            return true;
        }
        return false;
    }

    public void die() {
        layerUndo = null;
        layerRedo = null;
        inProgress = false;
    }

    public boolean addEdit(UndoableEdit anEdit) {
        if (inProgress && anEdit.getClass() == this.getClass()) {
            //TODO: absorb the edit
            //return true;
        }
        return false;
    }

    public boolean replaceEdit(UndoableEdit anEdit) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isSignificant() {
        // TODO: perhaps a bSignificant var?
        return true;
    }

    public void setPresentationName(String s) {
        name = s;
    }

    public String getPresentationName() {
        return name;
    }

    public String getUndoPresentationName() {
        return getPresentationName();
    }

    public String getRedoPresentationName() {
        return getPresentationName();
    }
}
