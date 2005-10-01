/*
 *  Tiled Map Editor, (c) 2004, 2005
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <b.lindeijer@xs4all.nl>
 *  Rainer Deyke <rainerd@eldwood.com>
 */

package tiled.util;

import java.lang.IllegalStateException;
import java.lang.UnsupportedOperationException;
import java.util.Iterator;
import java.util.Vector;
import java.util.NoSuchElementException;


public class NumberedSetIterator implements Iterator
{
    private NumberedSet set;
    private int id;

    public NumberedSetIterator(NumberedSet set) {
        this.set = set;
        this.id = 0;
    }

    public boolean hasNext() {
        return this.id <= this.set.getMaxId();
    }

    public Object next() throws NoSuchElementException {
        while (this.id <= this.set.getMaxId()) {
            Object o = this.set.get(id);
            ++this.id;
            if (o != null) return o;
        }
        throw new NoSuchElementException();
    }

    public void remove()
        throws UnsupportedOperationException, IllegalStateException
    {
        throw new UnsupportedOperationException();
    }
}
