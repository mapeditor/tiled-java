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
import java.awt.geom.Area;


public class ObjectGroup extends MapLayer {

	/**
	 * @param ret
	 * @param width
	 * @param height
	 */
	public ObjectGroup(Map ret, int width, int height) {
		
	}

	/**
	 * @param rectangle
	 */
	public ObjectGroup(Rectangle rectangle) {
		
	}

	/**
	 * @param group
	 */
	public ObjectGroup(ObjectGroup group) {
		
		// TODO Auto-generated constructor stub
	}

	public void translate(int dx, int dy) {
		
	}

	public void rotate(int angle) {
		
	}

	public void mirror(int dir) {
		
	}

	public void mergeOnto(MapLayer other) {
		
	}

	public void copyFrom(MapLayer other) {
		
	}

	public void maskedCopyFrom(MapLayer other, Area mask) {
		
	}

	public void copyTo(MapLayer other) {
		
	}

	public void resize(int width, int height, int dx, int dy) {
		
	}

	public boolean isUsed(Tile t) {

		return false;
	}

	public MapLayer createDiff(MapLayer ml) {
		
		return null;
	}

}
