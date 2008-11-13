/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiled.mapeditor.undo;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.undo.AbstractUndoableEdit;
import tiled.core.Map;
import tiled.mapeditor.Resources;

/**
 *
 * @author count
 */
public class MapViewportSettingsEdit extends AbstractUndoableEdit {
    private static class ViewportState implements Cloneable{
        public int viewportWidth;
        public int viewportHeight;
        public float eyeDistance;
        public void readFrom(Map map){
            viewportWidth = map.getViewportWidth();
            viewportHeight = map.getViewportHeight();
            eyeDistance = map.getEyeDistance();
        }
        public void writeTo(Map map){
            map.setViewportWidth(viewportWidth);
            map.setViewportHeight(viewportHeight);
            map.setEyeDistance(eyeDistance);
        }
        public ViewportState duplicate(){
            try {
                return (ViewportState) clone();
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(MapViewportSettingsEdit.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
    }
    private ViewportState backupState;
    private boolean undone = false;
    private Map map;
    
    public MapViewportSettingsEdit(Map map) {
        backupState = new ViewportState();
        backupState.readFrom(map);
        this.map = map;
    }
    
    public void undo(){
        super.undo();
        assert !undone;
        swapViewportState();
        undone = true;
    }
    
    public void redo(){
        super.redo();
        assert undone;
        swapViewportState();
        undone = false;
    }

    private void swapViewportState() {
        ViewportState s = (ViewportState)backupState.duplicate();
        s.readFrom(map);
        backupState.writeTo(map);
        backupState = s;
    }

    @Override
    public String getPresentationName() {
        return Resources.getString("edit.change.map.viewport.name");
    }
    
}
