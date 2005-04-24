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

package tiled.util;

import java.awt.Graphics;
import java.util.*;

import tiled.core.*;
import tiled.core.Map;

public class TileMergeHelper {

    private Map myMap;

    private TileLayer mergedLayer;

    private Vector cells;

    public TileMergeHelper(Map map) {
        myMap = map;
        cells = new Vector();
        mergedLayer = new TileLayer(map.getBounds().width, map.getBounds().height);
    }

    public void merge(int start, int len) {

    }

    public TileSet buildSet() {
        return null;
    }

    private class Cell {
        private Vector sandwich;

        public Cell(Map map, int posx, int posy, int start, int len) {
            
        }

        public void render(Graphics g) {

        }

        public boolean equals(Cell c) {
            return false;
        }
    };
}
