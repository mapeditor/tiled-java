/*
 * Tiled Map Editor, (c) 2004
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

import java.util.ListIterator;
import java.util.Vector;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import tiled.core.*;


public class MapLayerStateEdit extends MapLayerEdit {


	Vector beforeLayerSet, afterLayerSet;

	public MapLayerStateEdit(Map m, ListIterator beforeSet, ListIterator afterSet) {
		super(m);
		start(beforeSet);
		afterLayerSet = new Vector();
	}

	public MapLayerStateEdit(Map m) {
		super(m);
		start(m.getLayers());
	}

	public void start(ListIterator beforeSet) {
		inProgress = true;
		beforeLayerSet = new Vector();
		while(beforeSet.hasNext()) {
			try {
				beforeLayerSet.add(((MapLayer)beforeSet.next()).clone());
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
	}

	public void end(ListIterator afterSet) throws Exception{
		if(!inProgress) {
			throw new Exception("Without a beginning, there can be no end...");
		}
		afterLayerSet = new Vector();
		while(afterSet.hasNext()) {
			afterLayerSet.add(((MapLayer)afterSet.next()).clone());
		}
		inProgress = false;
	}

	/* begin inherited methods */
	public void undo() throws CannotUndoException {
		myMap.removeAllLayers();
		myMap.addAllLayers(beforeLayerSet);
	}

	public boolean canUndo() {
		if(beforeLayerSet != null) {
			return true;
		}
		return false;
	}

	public void redo() throws CannotRedoException {
		myMap.removeAllLayers();
		myMap.addAllLayers(afterLayerSet);
	}

	public boolean canRedo() {
		if(afterLayerSet != null) {
			return true;
		}
		return false;
	}

	public void die() {


	}

	public boolean addEdit(UndoableEdit anEdit) {
		return false;
	}

	public boolean replaceEdit(UndoableEdit anEdit) {
		if(anEdit.getClass() == this.getClass()) {
			beforeLayerSet = ((MapLayerStateEdit)anEdit).beforeLayerSet;
			afterLayerSet = ((MapLayerStateEdit)anEdit).afterLayerSet;
			//TODO: should this include the Presentation name?
			return true;
		}
		return false;
	}

	public boolean isSignificant() {
		return true;
	}

	public String getPresentationName() {
		return super.getPresentationName();
	}

	public String getUndoPresentationName() {
		return getPresentationName();
	}

	public String getRedoPresentationName() {
		return getPresentationName();
	}
}
