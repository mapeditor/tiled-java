/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiled.mapeditor.widget;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import tiled.util.TiledConfiguration;

/**
 * This JSplitPane child class observes its children for changes in their
 * visibility and stores/restores it's divider state accordingly. If both
 * children (left/right or top/bottom, depending on splitter orientation)
 * are hidden, the split pane will hide itself as well. If only one of the
 * components is visible, the divider bar is hidden and set so that the
 * split pane shows that component in full size.
 * @author upachler
 */
public class SmartSplitPane extends JSplitPane {
    private final Preferences prefs;
    private int defaultDividerSize = getDividerSize();
    private int lastLocation = Integer.MIN_VALUE;
    
    private void attachComponentListener(Component c) {
        if(c!=null)
            c.addComponentListener(componentListener);
    }

    private void detachComponentListener(Component c) {
        if(c!=null)
            c.removeComponentListener(componentListener);
    }

    private State determineState() {
        State foundState = null;

        Component c0 = getLeftComponent();
        Component c1 = getRightComponent();
        
        boolean leftVisible = c0 != null && c0.isVisible();
        boolean rightVisible = c1 != null && c1.isVisible();
        
        if(leftVisible && rightVisible)
            foundState = State.BOTH_VISIBLE;
        else if(leftVisible)
            foundState = State.LEFT_VISIBLE;
        else if(rightVisible)
            foundState = State.RIGHT_VISIBLE;
        else
            foundState = State.NONE_VISIBLE;
        return foundState;
    }

    @Override
    public void setDividerLocation(int location) {
        if(lastLocation==location)
            return;
        lastLocation = location;
        super.setDividerLocation(location);
    }

    public void restore() {
        final int dividerLocation = prefs.getInt("dividerLocation", -1);
        if(dividerLocation != -1)
            setDividerLocation(dividerLocation);
        else
            resetToPreferredSizes();        
        setDividerSize(defaultDividerSize);
    }

    public void save() {
        if(state == State.BOTH_VISIBLE){    // save only if we're in a correct state
            if(defaultDividerSize == -1)
                defaultDividerSize = getDividerSize();
            int location = getDividerLocation();
            prefs.putInt("dividerLocation", location);
        }
    }
    
    private enum State{
        BOTH_VISIBLE,
        LEFT_VISIBLE,
        RIGHT_VISIBLE,
        NONE_VISIBLE,
    };
    private State state;
    
    private ComponentListener componentListener = new ComponentAdapter(){
        public void componentShown(ComponentEvent e) {
            updateDivider();
        }
        public void componentHidden(ComponentEvent e) {
            updateDivider();
        }
    };
    public SmartSplitPane(int newOrientation, boolean newContinuousLayout, Component newLeftComponent, Component newRightComponent, String preferencesId) {
        super(newOrientation, newContinuousLayout, newLeftComponent, newRightComponent);
        
        prefs = TiledConfiguration.node("dock-splitter/" + preferencesId);
        
        state = determineState();

        attachComponentListener(newLeftComponent);
        attachComponentListener(newRightComponent);
    }
    
    private void updateDivider(){
        
        State newState = determineState();
        
        if(newState.equals(state))
            return;
        
        switch(newState){
            case BOTH_VISIBLE:{
                // Restore the old divider location
                restore();
                setVisible(true);
            }    break;
            case LEFT_VISIBLE:{
                save();
                setDividerLocation(getMaximumDividerLocation());
                setDividerSize(0);
                setVisible(true);
            }    break;
            case RIGHT_VISIBLE:{
                save();
                setDividerLocation(getMinimumDividerLocation());
                setDividerSize(0);
                setVisible(true);
            }    break;
            case NONE_VISIBLE:{
                save();
                setVisible(false);
            }
        }
        
        state = newState;
    }

    @Override
    public void setBottomComponent(Component comp) {
        detachComponentListener(getBottomComponent());
        super.setBottomComponent(comp);
        attachComponentListener(comp);
    }

    @Override
    public void setLeftComponent(Component comp) {
        detachComponentListener(getLeftComponent());
        super.setLeftComponent(comp);
        attachComponentListener(comp);
    }

    @Override
    public void setRightComponent(Component comp) {
        detachComponentListener(getRightComponent());
        super.setRightComponent(comp);
        attachComponentListener(comp);
    }

    @Override
    public void setTopComponent(Component comp) {
        detachComponentListener(getTopComponent());
        super.setTopComponent(comp);
        attachComponentListener(comp);
    }
    
    
}
