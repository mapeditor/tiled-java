/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiled.mapeditor.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import tiled.core.MapLayer;
import tiled.mapeditor.MapEditor;
import tiled.mapeditor.Resources;
import tiled.mapeditor.dialogs.LayerPropertiesDialog;
import tiled.mapeditor.dialogs.PropertiesDialog;

/**
 *
 * @author upachler
 */
public class ShowLayerPropertiesAction extends AbstractAction {
    
    private static final String ACTION_NAME = Resources.getString("menu.layer.properties");
    private static final String ACTION_TOOLTIP = Resources.getString("menu.layer.properties.tooltip");
    
    private MapEditor editor;
    
    public ShowLayerPropertiesAction(MapEditor editor){
        super(ACTION_NAME);
        putValue(SHORT_DESCRIPTION, ACTION_TOOLTIP);
        this.editor = editor;        
    }
        
    public void actionPerformed(ActionEvent e) {
            MapLayer layer = editor.getCurrentLayer();
            PropertiesDialog lpd =
                new LayerPropertiesDialog(editor.getAppFrame(), layer, editor.getUndoSupport());
            lpd.setTitle(layer.getName() + " " + Resources.getString("dialog.properties.title"));
            lpd.getProps();
            editor.updateLayerOperations();
            editor.getMapView().repaint();
        }
}
