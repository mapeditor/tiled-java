/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiled.mapeditor.selection;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import tiled.core.MapLayer;
import tiled.mapeditor.MapEditor;
import tiled.view.MapView;

/**
 *
 * @author upachler
 */
public class ObjectSelectionToolSemantic extends ToolSemantic{
    
    private boolean selecting;
    private Point selectionStart;
    private Rectangle selectionRubberband;
    private MapLayer selectedLayer;
    
    public ObjectSelectionToolSemantic(MapEditor editor){
        super(editor);
    }

    @Override
    public void activate() {
        super.activate();
        getEditor().getMapView().addMouseListener(mouseListener);
        getEditor().getMapView().addMouseMotionListener(mouseMotionListener);
    }

    @Override
    public void deactivate() {
        getEditor().getMapView().removeMouseListener(mouseListener);
        getEditor().getMapView().removeMouseMotionListener(mouseMotionListener);
        super.deactivate();
    }
    
    
    private void startSelection(int x, int y) {
        if(selecting)
            return;
        selecting = true;

        MapView mapView = getEditor().getMapView();
        selectedLayer = getEditor().getCurrentLayer();
        selectionStart = mapView.screenToPixelCoords(selectedLayer, x, y);
        selectionRubberband = new Rectangle();
        updateSelection(x, y);
    }
    
    private void updateSelection(int screenX, int screenY){
        if(!selecting)
            return;
        
        MapView mapView = getEditor().getMapView();
        Point p = mapView.screenToPixelCoords(selectedLayer, screenX, screenY);
        int x = p.x;
        int y = p.y;
        if(x>=selectionStart.x){
            selectionRubberband.x = selectionStart.x;
            selectionRubberband.width = x - selectionStart.x;
        } else {
            selectionRubberband.x = x;
            selectionRubberband.width = selectionStart.x - x;
        }
        
        if(y>=selectionStart.y){
            selectionRubberband.y = selectionStart.y;
            selectionRubberband.height = y - selectionStart.y;
        } else {
            selectionRubberband.y = y;
            selectionRubberband.height = selectionStart.y - y;
        }
        mapView.setSelectionRubberband(selectedLayer, selectionRubberband);
    }
    
    private void finishSelection(int x, int y){
        if(!selecting)
            return;
        
        selecting = false;
        selectionRubberband = null;
        selectionStart = null;
        getEditor().getMapView().setSelectionRubberband(selectedLayer, null);
    }
    
    private MouseMotionListener mouseMotionListener = new MouseMotionAdapter(){

        public void mouseDragged(MouseEvent e) {
            // FIXME: intersection tests with objects should be implemented here
            // to show the object resize cursor
            
            updateSelection(e.getX(), e.getY());
        }
    };
    
    
    
    private MouseListener mouseListener = new MouseAdapter() {

        public void mousePressed(MouseEvent e) {
            startSelection(e.getX(), e.getY());
        }

        public void mouseReleased(MouseEvent e) {
            finishSelection(e.getX(), e.getY());
        }

        public void mouseExited(MouseEvent e) {
//            finishSelection(e.getX(), e.getY());
        }
    };
}
