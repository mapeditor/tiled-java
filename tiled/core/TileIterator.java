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

import java.lang.IllegalStateException;
import java.lang.UnsupportedOperationException;
import java.util.Iterator;
import java.util.Vector;
import java.util.NoSuchElementException;


public class TileIterator implements Iterator
{
    private Vector tiles;
    private int pos;

    public TileIterator(Vector tiles) {
        this.tiles = tiles;
        pos = 0;
    }

    public boolean hasNext() {
        while (pos < tiles.size()) {
            if (tiles.get(pos) != null) return true;
            pos++;
        }
        return false;
    }

    public Object next() throws NoSuchElementException {
        while (pos < tiles.size()) {
            Tile t = (Tile)tiles.get(pos);
            pos++;
            if (t != null) return t;
        }
        throw new NoSuchElementException();
    }

    public void remove()
        throws UnsupportedOperationException, IllegalStateException
    {
        throw new UnsupportedOperationException();
    }
}
