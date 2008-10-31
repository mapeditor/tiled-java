/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiled.mapeditor.brush;

import tiled.core.MapLayer;

/**
 *
 * @author upachler
 */
public class BrushException extends Exception {
    private MapLayer layer;

    public BrushException(MapLayer layer) {
        this.layer = layer;
    }

    public MapLayer getLayer(){
        return layer;
    }
}
