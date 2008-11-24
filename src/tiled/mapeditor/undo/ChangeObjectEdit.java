/*
 *  Tiled Map Editor, (c) 2008
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <bjorn@lindeijer.nl>
 */

package tiled.mapeditor.undo;

import java.awt.Rectangle;
import java.util.Properties;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import tiled.core.MapObject;
import tiled.mapeditor.Resources;

/**
 * Changes the attributes and properties of an object.
 */
public class ChangeObjectEdit extends AbstractUndoableEdit
{
    private final MapObject mapObject;
    private State state;

    class State{
        private String name;
        private String type;
        private String imageSource;
        private Rectangle bounds;
        private final Properties properties = new Properties();
        
        public void retreive(MapObject o){
            name = mapObject.getName();
            type = mapObject.getType();
            imageSource = mapObject.getImageSource();
            bounds = new Rectangle(mapObject.getBounds());
            properties.clear();
            properties.putAll(mapObject.getProperties());
        }
        
        public void apply(MapObject mapObject){
            mapObject.setName(name);
            mapObject.setType(type);
            mapObject.setImageSource(imageSource);
            mapObject.setBounds(new Rectangle(bounds));
            mapObject.getProperties().clear();
            mapObject.getProperties().putAll(properties);
        }
    };
    
    public ChangeObjectEdit(MapObject mapObject) {
        this.mapObject = mapObject;

        // Store the previous state so we can undo changes
        state = new State();
        state.retreive(mapObject);
    }

    public void undo() throws CannotUndoException {
        super.undo();

        // Store the current state so we can redo changes
        swap();
    }

    public void redo() throws CannotRedoException {
        super.redo();

        swap();
    }

    private void swap() {
        State s = new State();
        s.retreive(mapObject);
        state.apply(mapObject);
        state = s;
    }
    
    public String getPresentationName() {
        return Resources.getString("action.object.change.name");
    }
}
