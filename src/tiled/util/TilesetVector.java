package tiled.util;

import java.util.Vector;

import tiled.core.TileSet;

/**
 * This 
 * @version $Id$
 */
public class TilesetVector extends Vector {

    private TileSet tileset;
    
    public TilesetVector(TileSet ts) {
        tileset = ts;
    }

    public Object get(int index) {
        int t, i=0;
        
        if(tileset.getTile(0) == null) {
            i=1;
        }
        
        for(t=0;i<tileset.getMaxTileId()-1 && t<index;i++)
            if(tileset.getTile(i) != null) t++;
        
        return tileset.getTile(i);
    }

    public int size() {
        int t, i=0;
        
        if(tileset.getTile(0) == null) {
            i=1;
        }
        
        for(t=0;i<tileset.getMaxTileId()-1;i++)
            if(tileset.getTile(i) != null) t++;
        
        return t;
    }
    
}
