/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiled.mapeditor.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import tiled.core.Map;
import tiled.core.MapLayer;
import tiled.mapeditor.MapEditor;
import tiled.mapeditor.Resources;

/**
 *
 * @author upachler
 */
public class DeleteLayerEdit extends AbstractUndoableEdit {
    private MapEditor editor;
    
    private Map map;
    private int index;
    private MapLayer layer = null;
    
    public DeleteLayerEdit(MapEditor editor, Map map, int index) {
        this.editor = editor;
        this.map = map;
        this.index = index;
        this.layer = map.getLayer(index);
    }

    public void undo() throws CannotUndoException {
        assert layer != null;
        super.undo();
        map.insertLayer(index, layer);
        if(editor.getCurrentLayerIndex() >= map.getTotalLayers())
            editor.setCurrentLayerIndex(map.getTotalLayers()-1);
        layer = null;
    }

    public void redo() throws CannotRedoException {
        assert layer == null;
        super.redo();
        layer = map.getLayer(index);
        map.removeLayer(index);
    }

    public String getPresentationName() {
        return Resources.getString("action.layer.delete.name");
    }

    
}
