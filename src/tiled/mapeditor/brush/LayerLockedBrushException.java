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
public class LayerLockedBrushException extends BrushException {

    public LayerLockedBrushException(MapLayer ml) {
        super(ml);
    }

}
