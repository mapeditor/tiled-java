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
import java.util.Enumeration;
import java.util.Properties;

public class MapObject {

    private Sprite sprite;
    private Properties properties;

    protected float map_x, map_y;
    protected Rectangle bounds;
    protected boolean bVisible = true;
    protected String name, source;
    protected int id;

    MapObject() {
        bounds = new Rectangle();
        properties = new Properties();
    }

    public void setProperty(String key, String value) {
		properties.put(key,value);
    }

    public void setId(int id) {
        this.id=id;
    }

	public void setX(int x) {
		map_x=x;
	}

	public void setY(int y) {
		map_y=y;
	}

	public void setName(String s) {
		name = s;
	}

	public void setSource(String s) {
		source = s;
	}

    public void setSprite(Sprite s) {
        sprite=s;
    }

    public int getId() {
        return id;
    }

	public int getX() {
		return (int)map_x;
	}
	
	public int getY() {
		return (int)map_y;
	}

	public String getName() {
		return name;
	}

	public String getSource() {
		return source;
	}

	public Enumeration getProperties() {
		return properties.keys();
	}

	public String getPropertyValue(String key) {
		return properties.getProperty(key);
	}

    public int getTotalAttributes() {
        return properties.size();
    }

    public String toString() {
        String s = name+"("+id+"): ("+map_x+","+map_y+")";

        return s;
    }

}
