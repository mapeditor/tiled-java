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

package tiled.mapeditor.plugin;

import java.io.File;
import java.io.FileFilter;
import java.util.Stack;

import tiled.io.MapReader;
import tiled.io.MapWriter;
import tiled.io.PluggableMapIO;

public class TiledPlugin implements PluggableMapIO, FileFilter {

	private MapReader reader;
	private MapWriter writer;
	
	public String getFilter() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see tiled.io.PluggableMapIO#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see tiled.io.PluggableMapIO#getDescription()
	 */
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPluginPackage() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean accept(File pathname) {
		
		return false;
	}

	/* (non-Javadoc)
	 * @see tiled.io.PluggableMapIO#setErrorStack(java.util.Stack)
	 */
	public void setErrorStack(Stack es) {
		// TODO Auto-generated method stub
		
	}
	
}
