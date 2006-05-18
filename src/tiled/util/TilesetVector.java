package tiled.util;

import java.util.Vector;

import tiled.core.TileSet;

/**
 * 
 * @version $Id$
 *
 */
public class TilesetVector extends Vector {

    private TileSet tileset;
    
    public TilesetVector(TileSet ts) {
        tileset = ts;
    }

    public Object get(int index) {
        int t=0, i;
        for(i=0;i<=tileset.getMaxTileId() && t<index;i++)
            if(tileset.getTile(i) != null) t++;
        
        return tileset.getTile(i);
    }

}
