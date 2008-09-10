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
import java.util.Properties;

/**
 * @version $Id$
 */
public class MapObject implements Cloneable
{
    private Properties properties = new Properties();

    protected Rectangle bounds = new Rectangle();
    protected boolean bVisible = true;
    protected String name = "Object";
    protected String source;
    protected String type = "";

    public MapObject(int x, int y, int width, int height) {
        bounds = new Rectangle(x, y, width, height);
    }

    public Object clone() throws CloneNotSupportedException {
        MapObject clone = (MapObject) super.clone();
        clone.bounds = new Rectangle(bounds);
        clone.properties = (Properties) properties.clone();
        return clone;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    public int getX() {
        return bounds.x;
    }

    public void setX(int x) {
        bounds.setLocation(x, getY());
    }

    public int getY() {
        return bounds.y;
    }

    public void setY(int y) {
        bounds.setLocation(getX(), y);
    }

    public String getName() {
        return name;
    }

    public void setName(String s) {
        name = s;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String s) {
        source = s;
    }

    public String getType() {
        return type;
    }

     public void setType(String s) {
        type = s;
    }

     public int getWidth() {
        return bounds.width;
    }

    public void setWidth(int w) {
        bounds.setSize(w, getHeight());
    }
 
    public void setHeight(int h) {
        bounds.setSize(getWidth(), h);
    }

    public int getHeight() {
        return bounds.height;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties p) {
        properties = p;
    }

    public void translate(int dx, int dy) {
        bounds.translate(dx, dy);
    }

    public String toString() {
        return type + " (" + getX() + "," + getY() + ")";
    }
}
