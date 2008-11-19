/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiled.mapeditor.selection;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import tiled.core.MapLayer;
import tiled.core.MapObject;
import tiled.core.ObjectGroup;
import tiled.mapeditor.MapEditor;
import tiled.view.MapView;

/**
 *
 * @author upachler
 */
public class ObjectSelectionToolSemantic extends ToolSemantic{
    private enum Mode {
        IDLE,
        SELECT,
        MOVE_OBJECT,
        RESIZE_OBJECT,
    }
    private Mode mode = Mode.IDLE;
    private Point selectionStart;
    private Rectangle selectionRubberband;
    private MapLayer selectedLayer;
    private Point objectStartPos;
    private MapObject object;
    
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
    
    
    private MapObject findObject(int x, int y) {
        MapView mapView = getEditor().getMapView();
        ObjectGroup og = (ObjectGroup)(getEditor().getCurrentLayer());
        final int margin = 1;   // one pixel margin around selection point
        Rectangle r = new Rectangle(x-margin, y-margin, 1+2*margin, 1+2*margin);
        r = mapView.screenToPixelCoords(og, r);
        MapObject[] objects = og.findObjectsByOutline(r);
        
        return objects.length != 0 ? objects[0] : null;
    }
    
    private Mode determineMode(int x, int y) {
        MapObject o = findObject(x, y);
        if(o == null)
            return Mode.SELECT;
        return Mode.MOVE_OBJECT;
    }
    
    private void startSelection(int x, int y) {
        if(mode != Mode.IDLE)
            return;
        mode = Mode.SELECT;

        MapView mapView = getEditor().getMapView();
        selectedLayer = getEditor().getCurrentLayer();
        selectionStart = mapView.screenToPixelCoords(selectedLayer, x, y);
        selectionRubberband = new Rectangle();
        updateSelection(x, y);
    }
    
    private void updateSelection(int screenX, int screenY){
        if(mode != Mode.SELECT)
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
        if(mode != Mode.SELECT)
            return;
        
        mode = Mode.IDLE;
        selectionRubberband = null;
        selectionStart = null;
        getEditor().getMapView().setSelectionRubberband(selectedLayer, null);
    }
    
    private void startMoveObject(int x, int y){
        MapObject o = findObject(x, y);
        if(mode != Mode.IDLE)
            return;
        mode = Mode.MOVE_OBJECT;
        this.object = o;
        MapView mapView = getEditor().getMapView();
        selectedLayer = getEditor().getCurrentLayer();
        selectionStart = mapView.screenToPixelCoords(selectedLayer, x, y);
        objectStartPos = new Point(o.getBounds().x, o.getBounds().y);
        
        updateMoveObject(x, y);
    }
    
    private void updateMoveObject(int x, int y){
        if(mode != Mode.MOVE_OBJECT)
            return;
        MapView mapView = getEditor().getMapView();
        Point p = mapView.screenToPixelCoords(selectedLayer, x, y);
        int diffX = p.x - selectionStart.x;
        int diffY = p.y - selectionStart.y;
        Rectangle b = object.getBounds();
        b.x = objectStartPos.x + diffX;
        b.y = objectStartPos.y + diffY;
        
        // FIXME: this is probably a bit too easy
        mapView.repaint();
    }
    
    private void finishMoveObject(int x, int y){
        if(mode != Mode.MOVE_OBJECT)
            return;
        mode = Mode.IDLE;
    }
    
    private MouseMotionListener mouseMotionListener = new MouseMotionAdapter(){

        @Override
        public void mouseDragged(MouseEvent e) {
            // FIXME: intersection tests with objects should be implemented here
            // to show the object resize cursor
            
            switch(mode){
                case MOVE_OBJECT:
                    updateMoveObject(e.getX(), e.getY());
                    break;
                case SELECT:
                    updateSelection(e.getX(), e.getY());
            }
        }
        
        @Override
        public void mouseMoved(MouseEvent e){
            MapView mapView = getEditor().getMapView();
            Mode m = determineMode(e.getX(), e.getY());
            switch(m){
                case SELECT:
                    mapView.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    break;
                case MOVE_OBJECT:
                    mapView.setCursor(new Cursor(Cursor.MOVE_CURSOR));
                    break;
            }
        }
    };
    
    
    
    private MouseListener mouseListener = new MouseAdapter() {

        public void mousePressed(MouseEvent e) {
            Mode m = determineMode(e.getX(), e.getY());
            switch(m){
                case SELECT:
                    startSelection(e.getX(), e.getY());
                    break;
                case MOVE_OBJECT:
                    startMoveObject(e.getX(), e.getY());
                    break;
            }
        }

        public void mouseReleased(MouseEvent e) {
            switch(mode){
                case SELECT:
                    finishSelection(e.getX(), e.getY());
                    break;
                case MOVE_OBJECT:
                    finishMoveObject(e.getX(), e.getY());
                    break;
            }
        }

        public void mouseExited(MouseEvent e) {
//            finishSelection(e.getX(), e.getY());
        }

    };
}
