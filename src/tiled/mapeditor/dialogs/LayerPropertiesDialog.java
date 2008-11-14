/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiled.mapeditor.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;
import tiled.core.MapLayer;
import tiled.core.TileLayer;
import tiled.mapeditor.Resources;
import tiled.mapeditor.undo.LayerResizeEdit;
import tiled.mapeditor.undo.MapLayerEdit;
import tiled.mapeditor.widget.IntegerSpinner;
import tiled.mapeditor.widget.VerticalStaticJPanel;

/**
 *
 * @author upachler
 */
public class LayerPropertiesDialog extends PropertiesDialog {
    private JTextField layerName;
    private IntegerSpinner layerWidth, layerHeight, layerTileWidth, layerTileHeight;
    private final MapLayer layer;
    private final boolean isTileLayer;
    
    /* LANGUAGE PACK */
    private static final String DIALOG_TITLE = Resources.getString("dialog.layerproperites.title");
    private static final String NAME_LABEL = Resources.getString("dialog.layerproperites.name.label");
    private static final String WIDTH_LABEL = Resources.getString("dialog.layerproperties.width.label");
    private static final String HEIGHT_LABEL = Resources.getString("dialog.layerproperties.height.label");
    private static final String TILE_WIDTH_LABEL = Resources.getString("dialog.layerproperties.tilewidth.label");
    private static final String TILE_HEIGHT_LABEL = Resources.getString("dialog.layerproperties.tileheight.label");
    private static final String UNTITLED_LAYER = "";

    public LayerPropertiesDialog(JFrame parent, MapLayer layer, UndoableEditSupport undoSupport) {
        super(parent, layer.getProperties(), undoSupport, false);
        this.layer = layer;
        this.isTileLayer = TileLayer.class.isAssignableFrom(layer.getClass());
        init();
        setTitle(DIALOG_TITLE);
        pack();
        setLocationRelativeTo(parent);
    }

    public void init() {
        super.init();
        JLabel nameLabel = new JLabel(NAME_LABEL);
        JLabel widthLabel = new JLabel(WIDTH_LABEL);
        JLabel heightLabel = new JLabel(HEIGHT_LABEL);
        JLabel tileWidthLabel = new JLabel(TILE_WIDTH_LABEL);
        JLabel tileHeightLabel = new JLabel(TILE_HEIGHT_LABEL);
        
        layerName = new JTextField(UNTITLED_LAYER);
        
        layerWidth = new IntegerSpinner(0, 0, 1024);
        layerHeight = new IntegerSpinner(0, 0, 1024);
        layerTileWidth = new IntegerSpinner(0, 0, 1024);
        layerTileHeight = new IntegerSpinner(0, 0, 1024);

        JPanel miscPropPanel = new VerticalStaticJPanel();
        miscPropPanel.setLayout(new GridBagLayout());
        miscPropPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(5, 0, 0, 5);
        miscPropPanel.add(nameLabel, c);
        c.gridy++;
        miscPropPanel.add(widthLabel, c);
        c.gridy++;
        miscPropPanel.add(heightLabel, c);
        c.gridy++;
        miscPropPanel.add(tileWidthLabel, c);
        c.gridy++;
        miscPropPanel.add(tileHeightLabel, c);
        c.insets = new Insets(5, 0, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        miscPropPanel.add(layerName, c);
        c.gridy++;
        miscPropPanel.add(layerWidth, c);
        c.gridy++;
        miscPropPanel.add(layerHeight, c);
        c.gridy++;
        miscPropPanel.add(layerTileWidth, c);
        c.gridy++;
        miscPropPanel.add(layerTileHeight, c);
        
        // for layers that are not TileLayer instances, the tile width cannot
        // be set and the controls are therefore disabled
        if(!isTileLayer){
            tileWidthLabel.setEnabled(false);
            tileHeightLabel.setEnabled(false);
            layerTileWidth.setEnabled(false);
            layerTileHeight.setEnabled(false);
        }
        
        mainPanel.add(miscPropPanel, 0);
    }

    public void updateInfo() {
        super.updateInfo();
        layerName.setText(layer.getName());
        layerWidth.setValue(layer.getWidth());
        layerHeight.setValue(layer.getHeight());
        layerTileWidth.setValue(layer.getTileWidth());
        layerTileHeight.setValue(layer.getTileHeight());
    }

    protected UndoableEdit commit() {
        // Make sure the changes to the object can be undone
        
        UndoableEdit propertyEdit = super.commit();
        
        boolean miscLayerSettingsChanged =
            !layer.getName().equals(layerName.getText())
        ||    layer.getWidth() != layerWidth.intValue()
        ||    layer.getHeight() != layerHeight.intValue()
        ;
        if(isTileLayer){
            miscLayerSettingsChanged = miscLayerSettingsChanged
            ||    layer.getTileWidth() != layerTileWidth.intValue()
            ||    layer.getTileHeight() != layerTileHeight.intValue()
            ;
        }
        
        // if nothing changed, here, simply return what the superclass changed
        if(!miscLayerSettingsChanged)
            return propertyEdit;
        
        CompoundEdit ce = new CompoundEdit();
        if(propertyEdit != null)
            ce.addEdit(propertyEdit);
        
        try {
            // determine changes
            boolean layerResized =
                layerHeight.intValue() != layer.getHeight()
            ||  layerWidth.intValue() != layer.getWidth()
            ;

            boolean layerSettingsChanged = 
                !layer.getName().equals(layerName.getText())
            ||  layer.getTileWidth() != layerTileWidth.intValue()
            ||  layer.getTileHeight() != layerTileHeight.intValue()
            ;
            
            // apply changes and record edits for undo
            if(layerResized){
                LayerResizeEdit lre = new LayerResizeEdit(layer, 0, 0, layerWidth.intValue(), layerHeight.intValue());

                layer.resize(layerWidth.intValue(), layerHeight.intValue(), 0, 0);
                
                ce.addEdit(lre);
            }
                    
            
            if(layerSettingsChanged){
                MapLayer before = (MapLayer) layer.clone();

                layer.setName(layerName.getText());            
                if (isTileLayer) {
                    ((TileLayer) layer).setTileDimensions(layerTileWidth.intValue(), layerTileHeight.intValue());
                }
                MapLayer after = (MapLayer) layer.clone();
                MapLayerEdit mle = new MapLayerEdit(layer, before, after);
                mle.setPresentationName(Resources.getString("edit.changelayerdimension.name"));
                ce.addEdit(mle);
            }
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(LayerPropertiesDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        ce.end();
        
        return ce;
    }

}
