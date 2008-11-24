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
import javax.swing.undo.UndoableEdit;
import tiled.core.MapLayer;
import tiled.core.MapObject;
import tiled.core.ObjectGroup;
import tiled.mapeditor.MapEditor;
import tiled.mapeditor.undo.ChangeObjectEdit;
import tiled.view.MapView;

/**
 *
 * @author upachler
 */
public class ObjectSelectionToolSemantic extends ToolSemantic{
    private UndoableEdit undoableEdit = null;;

    private enum Mode {
        IDLE,
        SELECT,
        MOVE_OBJECT,
        RESIZE_OBJECT,
    }
    private enum Corner{
        NONE,
        X0Y0,
        X0Y1,
        X1Y0,
        X1Y1;
            
        public static Corner findCorner(Rectangle r, Rectangle r2)
        {
            Point points[] = new Point[]{
                new Point(r.x, r.y),
                new Point(r.x, r.y+r.height),
                new Point(r.x+r.width, r.y),
                new Point(r.x+r.width, r.y+r.height),
            };
            int i;
            for(i=0; i<4; ++i){
                if(r2.contains(points[i]))
                    break;
            }
            switch(i){
                case 0:
                    return X0Y0;
                case 1:
                    return X0Y1;
                case 2:
                    return X1Y0;
                case 3:
                    return X1Y1;
                default:
                    return NONE;
            }
        }
        
        public static Point getRectCorner(Rectangle r, Corner corner){
            switch(corner){
                case X0Y0:
                    return new Point(r.x, r.y);
                case X0Y1:
                    return new Point(r.x, r.y+r.height);
                case X1Y0:
                    return new Point(r.x+r.width, r.y);
                case X1Y1:
                    return new Point(r.x+r.width, r.y+r.height);
                default:
                    return null;
            }
        }
        
        public static void setRectCorner(Rectangle r, Corner corner, int x, int y) {
            int x0 = r.x;
            int y0 = r.y;
            int x1 = r.x+r.width;
            int y1 = r.y+r.height;
            // horizontal
            switch(corner){
                case X0Y0:
                case X0Y1:
                    x0 = x;
                    if(x0>x1)
                        x0 = x1;
                    break;
                case X1Y0:
                case X1Y1:
                    x1 = x;
                    if(x1<x0)
                        x1 = x0;
                    break;
            }
            // vertical
            switch(corner){
                case X0Y0:
                case X1Y0:
                    y0 = y;
                    if(y0>y1)
                        y0=y1;
                    break;
                case X0Y1:
                case X1Y1:
                    y1 = y;
                    if(y1<y0)
                        y1 = y0;
                    break;
            }
            r.x = x0;
            r.y = y0;
            r.width = x1-x0;
            r.height = y1-y0;
        }
    };
    private Mode mode = Mode.IDLE;
    private Point selectionStart;
    private Rectangle selectionRubberband;
    private MapLayer selectedLayer;
    private Point objectStartPos;
    private MapObject object;
    private Corner corner = Corner.NONE;
    
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
    
    private Corner findObjectCorner(MapObject o, int x, int y) {
        MapView mapView = getEditor().getMapView();
        ObjectGroup og = (ObjectGroup)(getEditor().getCurrentLayer());
        final int margin = 2;   // one pixel margin around selection point
        
        Rectangle r = new Rectangle(x-margin, y-margin, 1+2*margin, 1+2*margin);
        r = mapView.screenToPixelCoords(og, r);
        return Corner.findCorner(o.getBounds(), r);
    }
    
    private Mode determineMode(int x, int y) {
        MapObject o = findObject(x, y);
        if(o == null)
            return Mode.SELECT;
        
        Corner corner = findObjectCorner(o, x, y);
        if(corner == Corner.NONE)
            return Mode.MOVE_OBJECT;
        else
            return Mode.RESIZE_OBJECT;
        
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
    
    private void finishSelection(int x, int y, boolean mergeSelection){
        if(mode != Mode.SELECT)
            return;
        
        ObjectGroup og = (ObjectGroup)selectedLayer;
        MapObject[] objects = og.findObjects(selectionRubberband);
        if(objects.length>0){
            Selection[] selection = new Selection[objects.length];
            for(int i=0; i<objects.length; ++i)
                selection[i] = new ObjectSelection(og, objects[i]);
            SelectionSet ss = getEditor().getSelectionSet();
            if(mergeSelection)
                ss.addSelection(selection);
            else
                ss.setSelection(selection);
        }
            
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
        undoableEdit = new ChangeObjectEdit(object);
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
        getEditor().getUndoSupport().postEdit(undoableEdit);
        undoableEdit = null;
        mode = Mode.IDLE;
    }
    
    private void startResizeObject(int x, int y) {
        MapObject o = findObject(x, y);
        if(mode != Mode.IDLE)
            return;
        mode = Mode.RESIZE_OBJECT;
        this.object = o;
        undoableEdit = new ChangeObjectEdit(object);
        MapView mapView = getEditor().getMapView();
        selectedLayer = getEditor().getCurrentLayer();
        selectionStart = mapView.screenToPixelCoords(selectedLayer, x, y);
        corner = findObjectCorner(o, selectionStart.x, selectionStart.y);
        objectStartPos = new Point(Corner.getRectCorner(o.getBounds(), corner));
        
        updateResizeObject(x, y);
    }

    private void finishResizeObject(int x, int y) {
        if(mode != Mode.RESIZE_OBJECT)
            return;
        mode = Mode.IDLE;
        getEditor().getUndoSupport().postEdit(undoableEdit);
    }
    
    private void updateResizeObject(int x, int y) {
        if(mode != Mode.RESIZE_OBJECT)
            return;
        MapView mapView = getEditor().getMapView();
        Point p = mapView.screenToPixelCoords(selectedLayer, x, y);
        int diffX = p.x - selectionStart.x;
        int diffY = p.y - selectionStart.y;
        Rectangle b = object.getBounds();
        Corner.setRectCorner(b, corner, objectStartPos.x+diffX, objectStartPos.y+diffY);
        
        // FIXME: this is probably a bit too easy
        mapView.repaint();
    }

    private MouseMotionListener mouseMotionListener = new MouseMotionAdapter(){

        @Override
        public void mouseDragged(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            switch(mode){
                case MOVE_OBJECT:
                    updateMoveObject(x, y);
                    break;
                case SELECT:
                    updateSelection(x, y);
                    break;
                case RESIZE_OBJECT:
                    updateResizeObject(x, y);
            }
        }
        
        @Override
        public void mouseMoved(MouseEvent e){
            MapView mapView = getEditor().getMapView();
            Mode m = determineMode(e.getX(), e.getY());
            int x = e.getX();
            int y = e.getY();
            
            switch(m){
                case SELECT:
                    mapView.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    break;
                case MOVE_OBJECT:
                    mapView.setCursor(new Cursor(Cursor.MOVE_CURSOR));
                    break;
                case RESIZE_OBJECT:{
                    Corner c = findObjectCorner(findObject(x, y), x, y);
                    mapView.setCursor(mapResizeCursor(c));
                }   break;
            }
        }

        private Cursor mapResizeCursor(Corner c) {
            switch(c){
                case X0Y0:
                    return new Cursor(Cursor.NW_RESIZE_CURSOR);
                case X0Y1:
                    return new Cursor(Cursor.SW_RESIZE_CURSOR);
                case X1Y0:
                    return new Cursor(Cursor.NE_RESIZE_CURSOR);
                case X1Y1:
                    return new Cursor(Cursor.SE_RESIZE_CURSOR);
                default:
                    return null;
            }
        }
    };
    
    
    
    private MouseListener mouseListener = new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            MapObject o = findObject(x, y);
            ObjectGroup og;
            try{
                og = (ObjectGroup)getEditor().getCurrentLayer();
            }catch(ClassCastException ccx){
                return; // should never happen though, as ObjectSelectionToolSemantic should only ever be active when an ObjectGroup is the current layer...
            }
            SelectionSet ss = getEditor().getSelectionSet();
            final int allMask = MouseEvent.SHIFT_DOWN_MASK;
            final int addSelectionMask = MouseEvent.SHIFT_DOWN_MASK;
            final int selectionClickMask = 0;
            final int modifiers = e.getModifiersEx() & allMask;
            if(o == null){  // o==null if mouse click did not hit a MapObject
                if(modifiers == selectionClickMask)
                    ss.clearSelection();
            } else {
                ObjectSelection newSelection = new ObjectSelection(og, o);
                if(modifiers == addSelectionMask)
                    ss.addSelection(newSelection);
                else
                    ss.setSelection(newSelection);
            }
        }

        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            Mode m = determineMode(x, y);
            switch(m){
                case SELECT:
                    startSelection(x, y);
                    break;
                case MOVE_OBJECT:
                    startMoveObject(x, y);
                    break;
                case RESIZE_OBJECT:
                    startResizeObject(x, y);
                    break;
            }
        }

        public void mouseReleased(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            switch(mode){
                case SELECT:{
                    boolean mergeSelection = (e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0;
                    finishSelection(x, y, mergeSelection);
                }   break;
                case MOVE_OBJECT:
                    finishMoveObject(x, y);
                    break;
                case RESIZE_OBJECT:
                    finishResizeObject(x, y);
                    break;
            }
        }

        public void mouseExited(MouseEvent e) {
//            finishSelection(e.getX(), e.getY());
        }
    };
}
