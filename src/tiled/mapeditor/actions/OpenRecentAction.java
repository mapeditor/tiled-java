package tiled.mapeditor.actions;

import tiled.mapeditor.MapEditor;
import tiled.mapeditor.Resources;
import tiled.util.TiledConfiguration;

/**
 * Opens one of the recently open maps
 * 
 * @version $Id$
 */
public class OpenRecentAction extends AbstractFileAction {
    
    private int index;
    
    public OpenRecentAction(MapEditor editor, SaveAction saveAction, String name, int index) {
        super(editor, saveAction,
              name,
              Resources.getString("action.map.open.tooltip"));

        //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control O"));
        this.index = index;
    }

    protected void doPerformAction() {
        String file = TiledConfiguration.node("recent").get("file"+index, "");

        if (file.length() > 0) {
            editor.loadMap(file);
        }
        
    }

}
