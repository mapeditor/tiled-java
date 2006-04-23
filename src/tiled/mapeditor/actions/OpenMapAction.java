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

package tiled.mapeditor.actions;

import javax.swing.KeyStroke;

import tiled.mapeditor.MapEditor;
import tiled.mapeditor.Resources;

/**
 * Opens the map open dialog.
 *
 * @version $Id$
 */
public class OpenMapAction extends AbstractFileAction
{
    public OpenMapAction(MapEditor editor, SaveAction saveAction) {
        super(editor, saveAction,
              Resources.getString("action.map.open.name"),
              Resources.getString("action.map.open.tooltip"));

        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control O"));
    }

    protected void doPerformAction() {
        editor.openMap();
    }
}
