/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiled.mapeditor.undo;

import java.util.Properties;
import javax.swing.undo.AbstractUndoableEdit;
import tiled.mapeditor.Resources;

/**
 *
 * @author upachler
 */
public class ChangePropertiesEdit extends AbstractUndoableEdit {
    private boolean undone = false;
    private Properties backupProperties;
    private Properties properties;
    
    /// creates a new edit that stores a backup of the layer's old Properties
    /// object. Note that this object needs to be a clone of the the layer's
    /// property object in its state before the edit was performed.
    public ChangePropertiesEdit(Properties properties, Properties oldPropertiesCopy) {
        backupProperties = oldPropertiesCopy;
        this.properties = properties;
    }
    
    public void undo(){
        super.undo();
        assert !undone;
        swapProperties();
        undone = true;
    }
    
    public void redo(){
        super.redo();
        assert undone;
        swapProperties();
        undone = false;
    }

    private void swapProperties() {
        Properties newBackupProperties = (Properties) properties.clone();
        properties.clear();
        properties.putAll(backupProperties);
        backupProperties = newBackupProperties;
    }
    
    @Override
    public String getPresentationName() {
        return Resources.getString("edit.changeproperties.name");
    }
}
