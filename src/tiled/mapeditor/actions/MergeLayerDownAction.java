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
import tiled.core.Map;

/**
 * Merges the current layer with the one below and selects the merged layer.
 *
 * @version $Id$
 */
public class MergeLayerDownAction extends AbstractLayerAction
{
    public MergeLayerDownAction(MapEditor editor) {
        super(editor,
              Resources.getString("action.layer.mergedown.name"),
              Resources.getString("action.layer.mergedown.tooltip"));

        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("shift control M"));
    }

    public void doPerformAction() {
        Map map = editor.getCurrentMap();
        int layerIndex = editor.getCurrentLayerIndex();

        if (layerIndex > 0) {
            // todo: The merged layer should be a copy. Currently the merge is
            // todo: done in-place, which makes undo not work properly.
            map.mergeLayerDown(layerIndex);
            editor.setCurrentLayer(layerIndex - 1);
        }
    }
}
