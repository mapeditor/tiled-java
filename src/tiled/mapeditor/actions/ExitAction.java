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

import java.util.prefs.Preferences;
import javax.swing.KeyStroke;

import tiled.mapeditor.MapEditor;
import tiled.mapeditor.Resources;
import tiled.util.TiledConfiguration;

/**
 * Exits the map editor.
 *
 * @version $Id$
 */
public class ExitAction extends AbstractFileAction
{
    public ExitAction(MapEditor editor, SaveAction saveAction) {
        super(editor, saveAction,
              Resources.getString("action.main.exit.name"),
              Resources.getString("action.main.exit.tooltip"));

        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Q"));
    }

    protected void doPerformAction() {
        // Save the extended window state if the window isn't minimized
        Preferences prefs = TiledConfiguration.node("dialog/main");
        int extendedState = editor.getAppFrame().getExtendedState();
        prefs.putInt("state", extendedState);

        System.exit(0);
    }
}
