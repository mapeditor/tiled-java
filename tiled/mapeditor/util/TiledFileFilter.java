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

package tiled.mapeditor.util;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.filechooser.FileFilter;

public class TiledFileFilter extends FileFilter {

	public static final int FILTER_TMX=1;
	public static final int FILTER_TSX=2;
	public static final int FILTER_BOTH=3;
	
	private String desc;
	private LinkedList exts;

	public TiledFileFilter() {
		desc = new String("tmx & tsx files");
		exts = new LinkedList();
		exts.add(new String("tmx"));
		exts.add(new String("tsx"));
	}

	public TiledFileFilter(int filter) {
		desc = new String("tmx/tsx files");
		exts = new LinkedList();
		if((filter & FILTER_TMX) != 0) {
			exts.add(new String("tmx"));
		}
		if((filter & FILTER_TSX) != 0) {
			exts.add(new String("tsx"));
		}
	}

	public void setDescription(String d) {
		desc = d;
	}

	public void addExtention(String e) {
		exts.add(e);
	}

	public boolean accept(File f) {
		
		if(f.isFile()) {
			if(f.getAbsolutePath().lastIndexOf('.') == -1) {
				return false;
			}
			
			String ext = f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf('.')+1);
			
			Iterator itr = exts.iterator();
			while(itr.hasNext()) {
				if(ext.equalsIgnoreCase((String)itr.next())) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	public String getDescription() {
		return desc;
	}

}
