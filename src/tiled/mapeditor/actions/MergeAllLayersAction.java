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

import javax.swing.JOptionPane;

import tiled.core.Map;
import tiled.core.TileLayer;
import tiled.mapeditor.MapEditor;
import tiled.mapeditor.Resources;
import tiled.util.TileMergeHelper;

/**
 * Merges all layers of the map. Optionally it will create a new tileset with
 * merged tiles.
 *
 * @version $Id$
 */
public class MergeAllLayersAction extends AbstractLayerAction
{
    public MergeAllLayersAction(MapEditor editor) {
        super(editor,
              Resources.getString("action.layer.mergeall.name"),
              Resources.getString("action.layer.mergeall.tooltip"));
    }

    protected void doPerformAction() {
        Map map = editor.getCurrentMap();
        
        // check if all layer's tiles have the same size as the map (otherwise, merging won't work and will be cancelled)
        if(!TileMergeHelper.areTileSizesUniform(map))
        {
            JOptionPane.showMessageDialog(editor.getAppFrame(), "Layer tile sizes inconsistent", "The tile size of some layers is different to the default tile size of the map. Layers can't be merged.", JOptionPane.ERROR_MESSAGE);
            return;
        }
            
        
        int ret = JOptionPane.showConfirmDialog(editor.getAppFrame(),
                "Do you wish to merge tile images, and create a new tile set?",
                "Merge Tiles?", JOptionPane.YES_NO_CANCEL_OPTION);

        if (ret == JOptionPane.YES_OPTION) {
            TileMergeHelper tmh = new TileMergeHelper(map);
            int len = map.getTotalLayers();
            //TODO: Add a dialog option: "Yes, visible only"
            TileLayer newLayer = tmh.merge(0, len, true);
            map.removeAllLayers();
            map.addLayer(newLayer);
            newLayer.setName("Merged Layer");
            map.addTileset(tmh.getSet());
            editor.setCurrentLayerIndex(0);
        }
        else if (ret == JOptionPane.NO_OPTION) {
            while (map.getTotalLayers() > 1) {
                map.mergeLayerDown(editor.getCurrentLayerIndex());
            }
            editor.setCurrentLayerIndex(0);
        }
    }
}
