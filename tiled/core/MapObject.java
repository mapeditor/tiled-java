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
    static final int O_IMMOVABLE  = 0x00000001;
    static final int O_HOLDS      = 0x00000002;
    static final int O_UNHOLDABLE = 0x00000004;
    static final int O_HOLDER     = 0x00000008;
    static final int O_IMPASSABLE = 0x00000010;
    static final int O_LINK       = 0x00000020;
    static final int O_RECVSHADOW = 0x00000040;
    static final int O_SHADOW     = 0x00000080;
    static final int O_BOTTOM     = 0x00000100;
    static final int O_EPHEMERAL  = 0x00000400;

    protected Sprite sprite;
    protected HashMap attributeList;

    protected float map_x, map_y;
    protected Rectangle bounds;
    protected boolean bVisible = true;
    protected MapObject next_o;
    protected String name;
    protected int type,id;

    MapObject() {
        map_x=map_y=0;
        next_o=null;
        name=null;
        id=-1;
        bounds = new Rectangle();
        bounds.x=bounds.y=bounds.width=bounds.height=0;
    }

    public void addAttribute(String key, String value) {

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

    public void setNext(MapObject o) {
        next_o = o;
    }

    public MapObject next() {
        return next_o;
    }

    public String toString() {
        String s = name+"("+id+"): ("+map_x+","+map_y+")";

        return s;
    }

}
