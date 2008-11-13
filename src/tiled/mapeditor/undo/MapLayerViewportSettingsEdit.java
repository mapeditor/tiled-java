/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiled.mapeditor.undo;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;
import tiled.core.MapLayer;
import tiled.mapeditor.Resources;

/**
 *
 * @author upachler
 */
public class MapLayerViewportSettingsEdit extends AbstractUndoableEdit  {
    private boolean significant;

    private static class ViewportState implements Cloneable{
        public float viewPlaneDistance;
        public boolean viewPlaneInfinitelyFarAway;
        public void readFrom(MapLayer map){
            viewPlaneDistance = map.getViewPlaneDistance();
            viewPlaneInfinitelyFarAway = map.isViewPlaneInfinitelyFarAway();
        }
        public void writeTo(MapLayer map){
            map.setViewPlaneDistance(viewPlaneDistance);
            map.setViewPlaneInfinitelyFarAway(viewPlaneInfinitelyFarAway);
        }
        public ViewportState duplicate(){
            try {
                return (ViewportState) clone();
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(MapViewportSettingsEdit.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        public boolean equals(Object o){
            try {
                ViewportState rhs = (ViewportState)o;
                return viewPlaneDistance == rhs.viewPlaneDistance
                    &&  viewPlaneInfinitelyFarAway == rhs.viewPlaneInfinitelyFarAway;
            }catch(ClassCastException ccx){
                return false;
            }
            
        }
    }

    private ViewportState backupState;
    private boolean undone = false;
    private MapLayer layer;
    
    
    public MapLayerViewportSettingsEdit(MapLayer layer) {
        this(layer, true);
    }
    
    public MapLayerViewportSettingsEdit(MapLayer layer, boolean significant) {
        backupState = new ViewportState();
        backupState.readFrom(layer);
        this.layer = layer;
        this.significant = significant;
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

    @Override
    public boolean addEdit(UndoableEdit anEdit) {
        assert !undone;
        
        if(!anEdit.getClass().equals(getClass()))
            return super.addEdit(anEdit);
        
        MapLayerViewportSettingsEdit other = (MapLayerViewportSettingsEdit)anEdit;
        
        // edits of different layers can't be merged
        if(layer != other.layer)
            return false;
        
        // edits with the same state are merged
        if(other.backupState.equals(backupState))
            return true;
        
        // inisignificant changes are merged
        return !other.isSignificant();        
    }
    
    public boolean isSignificant(){
        return significant;
    }
    
    private void swapViewportState() {
        ViewportState s = (ViewportState)backupState.duplicate();
        s.readFrom(layer);
        backupState.writeTo(layer);
        backupState = s;
    }

    @Override
    public String getPresentationName() {
        return Resources.getString("edit.change.layer.viewport.name");
    }
    
}
