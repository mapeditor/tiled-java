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

import java.util.*;

public abstract class TiledEntity
{
    protected String catalogName = null;

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String name) {
        catalogName = name;
    }

    public Iterator getAttributes() {
        return null;
    }

    public abstract String toString();
}
