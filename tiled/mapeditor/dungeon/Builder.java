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

package tiled.mapeditor.dungeon;


abstract public class Builder {

    private int movesPerIteration=0;
    protected int wallTileId=0,
    floorTileId=0,
    doorTileId=0;
    protected int mapx,mapy;

    Builder() {
    }

    Builder(int x,int y) {
        mapx=x;
        mapy=y;
    }

    abstract void iterate();
    abstract Builder spawn();

}
