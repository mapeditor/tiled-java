/*
 *  Tiled Map Editor, (c) 2004
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
import java.util.HashMap;

public class MapObject extends TiledEntity{

    protected Sprite sprite;
    protected HashMap attributeList;

    protected float map_x, map_y;
    protected Rectangle bounds;
    protected boolean bVisible = true;
    protected String name;
    protected int type,id;

    MapObject() {
        map_x=map_y=0;
        name=null;
        id=-1;
        bounds = new Rectangle();
        attributeList = new HashMap();
    }

    public void addAttribute(String key, String value) {
		attributeList.put(key,value);
    }

    public void setId(int id) {
        this.id=id;
    }

    public void setSprite(Sprite s) {
        sprite=s;
    }

    public int getId() {
        return id;
    }

    public int getTotalAttributes() {
        return attributeList.size();
    }

    public String toString() {
        String s = name+"("+id+"): ("+map_x+","+map_y+")";

        return s;
    }

}
