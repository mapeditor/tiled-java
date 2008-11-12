/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiled.mapeditor.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;
import tiled.core.Map;
import tiled.mapeditor.Resources;
import tiled.mapeditor.undo.MapViewportSettingsEdit;
import tiled.mapeditor.widget.IntegerSpinner;
import tiled.mapeditor.widget.VerticalStaticJPanel;

/**
 *
 * @author count
 */
public class MapPropertiesDialog extends PropertiesDialog{
    private IntegerSpinner viewportWidthSpinner, viewportHeightSpinner;
    private final Map map;

    private static final String DIALOG_TITLE = Resources.getString("dialog.mapproperites.title");
    private static final String WIDTH_LABEL = Resources.getString("dialog.mapproperites.viewport.width.label");
    private static final String HEIGHT_LABEL = Resources.getString("dialog.mapproperites.viewport.height.label");

    public MapPropertiesDialog(JFrame parent, Map map, UndoableEditSupport undoSupport) {
        super(parent, map.getProperties(), undoSupport, false);
        this.map = map;
        init();
        setTitle(DIALOG_TITLE);
        pack();
        setLocationRelativeTo(parent);
    }

    public void init() {
        super.init();
        JLabel viewportWidthLabel = new JLabel(WIDTH_LABEL);
        JLabel viewportHeightLabel = new JLabel(HEIGHT_LABEL);
        
        viewportWidthSpinner = new IntegerSpinner(0, 0, 4096);
        viewportHeightSpinner = new IntegerSpinner(0, 0, 4096);

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
        miscPropPanel.add(viewportWidthLabel, c);
        c.gridy++;
        miscPropPanel.add(viewportHeightLabel, c);
        c.insets = new Insets(5, 0, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        miscPropPanel.add(viewportWidthSpinner, c);
        c.gridy++;
        miscPropPanel.add(viewportHeightSpinner, c);
        
        mainPanel.add(miscPropPanel, 0);
    }

    public void updateInfo() {
        super.updateInfo();
        viewportWidthSpinner.setValue(map.getViewportWidth());
        viewportHeightSpinner.setValue(map.getViewportHeight());
    }

    protected UndoableEdit commit() {
        // Make sure the changes to the object can be undone
        
        UndoableEdit propertyEdit = super.commit();
        
        boolean viewportDimensionsChanged =
            viewportWidthSpinner.intValue() != map.getViewportWidth()
        ||  viewportHeightSpinner.intValue() != map.getViewportHeight();
        
        if(!viewportDimensionsChanged)
            return propertyEdit;

        CompoundEdit ce = new CompoundEdit();
        if(propertyEdit != null)
            ce.addEdit(propertyEdit);
        
        ce.addEdit(new MapViewportSettingsEdit(map));
        map.setViewportWidth(viewportWidthSpinner.intValue());
        map.setViewportHeight(viewportHeightSpinner.intValue());
        
        ce.end();
        
        return ce;
    }
}
