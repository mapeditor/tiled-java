/*
 *  Tiled Map Editor, (c) 2004-2006
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <b.lindeijer@xs4all.nl>
 */

package tiled.core;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * @version $Id$
 */
public class ObjectGroup extends MapLayer
{
    private LinkedList boundObjects;

    /**
     * Default constructor.
     */
    public ObjectGroup() {
        boundObjects = new LinkedList();
    }

    /**
     * Creates an object group that is part of the given map and has the given
     * origin.
     *
     * @param map    the map this layer is part of
     * @param origx  the x origin of this layer
     * @param origy  the y origin of this layer
     */
    public ObjectGroup(Map map, int origx, int origy) {
        super(map);
        boundObjects = new LinkedList();
        setBounds(new Rectangle(origx, origy, 0, 0));
    }

    /**
     * Creates an object group with a given area. The size of area is
     * irrelevant, just its origin.
     *
     * @param area the area of the object group
     */
    public ObjectGroup(Rectangle area) {
        super(area);
        boundObjects = new LinkedList();
    }

    /**
     * @see MapLayer#rotate(int)
     */
    public void rotate(int angle) {
    }

    /**
     * @see MapLayer#mirror(int)
     */
    public void mirror(int dir) {
    }

    public void mergeOnto(MapLayer other) {
    }

    public void maskedMergeOnto(MapLayer other, Area mask) {
    }
    
    public void copyFrom(MapLayer other) {
    }

    public void maskedCopyFrom(MapLayer other, Area mask) {
    }

    public void copyTo(MapLayer other) {
    }

    /**
     * @see MapLayer#resize(int,int,int,int)
     */
    public void resize(int width, int height, int dx, int dy) {
        // TODO: Translate contained objects by the change of origin
    }

    /**
     * @deprecated
     */
    public boolean isUsed(Tile t) {
        return false;
    }

    public boolean isEmpty() {
    	return boundObjects.isEmpty();
    }

    public Object clone() throws CloneNotSupportedException {
        ObjectGroup clone = (ObjectGroup) super.clone();
        clone.boundObjects = new LinkedList(boundObjects);
        return clone;
    }

    /**
     * @deprecated
     */
    public MapLayer createDiff(MapLayer ml) {
        return null;
    }

    public void bindObject(MapObject o) {
        boundObjects.add(o);
    }

    public void unbindObject(MapObject o) {
        boundObjects.remove(o);
    }

    public void unbindAll() {
        boundObjects.clear();
    }

    public ListIterator getObjects() {
        return (ListIterator) boundObjects.iterator();
    }

    public MapObject getObjectAt(int x, int y) {
        ListIterator iterator = getObjects();
        while (iterator.hasNext()) {
            MapObject obj = (MapObject) iterator.next();
            Rectangle rect = new Rectangle(obj.getX(), obj.getY(),
                    obj.getWidth(), obj.getHeight());
            if (rect.contains(x, y)) {
                return obj;
            }
        }
        return null;
    }
}
