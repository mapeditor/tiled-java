/*
 *  Tiled Map Editor, (c) 2004-2006
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <bjorn@lindeijer.nl>
 */

package tiled.mapeditor.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.undo.UndoableEdit;
import tiled.mapeditor.MapEditor;
import tiled.mapeditor.Resources;
import tiled.core.Map;
import tiled.mapeditor.undo.DeleteLayerEdit;

/**
 * Deletes the selected layer and selects the layer that takes the same index.
 *
 * @version $Id$
 */
public class DeleteLayerAction extends AbstractAction
{
    MapEditor editor;
    
    public DeleteLayerAction(MapEditor editor) {
        super(Resources.getString("action.layer.delete.name"),
              Resources.getIcon("gnome-delete.png"));
        this.editor = editor;
        putValue(SHORT_DESCRIPTION, "action.layer.delete.name");
    }

    public void actionPerformed(ActionEvent e) {
        Map map = editor.getCurrentMap();
        int layerIndex = editor.getCurrentLayerIndex();
        int totalLayers = map.getTotalLayers();
        
        UndoableEdit layerDeleteEdit = new DeleteLayerEdit(editor, map, layerIndex);
        
        if (layerIndex >= 0) {
            map.removeLayer(layerIndex);

            // If the topmost layer was selected, the layer index is invalid
            // after removing that layer. The right thing to do is to reset it
            // to the new topmost layer.
            if (layerIndex == totalLayers - 1) {
                editor.setCurrentLayerIndex(totalLayers - 2);
            }
        }
        
        editor.getUndoSupport().postEdit(layerDeleteEdit);
    }
}
