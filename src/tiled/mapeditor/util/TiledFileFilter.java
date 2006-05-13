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

package tiled.mapeditor.util;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.filechooser.FileFilter;

import tiled.io.PluggableMapIO;
import tiled.io.xml.XMLMapWriter;

/**
 * @version $Id$
 */
public class TiledFileFilter extends FileFilter
{
    public static final int FILTER_EXT  = 0;
    public static final int FILTER_TMX  = 1;
    public static final int FILTER_TSX  = 2;
    public static final int FILTER_BOTH = 3;
    public static final int FILTER_PLUG = 4;

    private String desc;
    private LinkedList exts;
    private PluggableMapIO pmio;
    private int type = FILTER_EXT;

    public TiledFileFilter() {
        desc = "Tiled files";
        exts = new LinkedList();
        exts.add("tmx");
        exts.add("tmx.gz");
        exts.add("tsx");
        pmio = new XMLMapWriter();
    }

    public TiledFileFilter(int filter) {
        exts = new LinkedList();
        desc = "";
        type = filter;

        if ((filter & FILTER_TMX) != 0) {
            desc = "Tiled Maps files ";
            exts.add("tmx");
            exts.add("tmx.gz");
            pmio = new XMLMapWriter();
        }

        if ((filter & FILTER_TSX) != 0) {
            desc += "Tiled Tileset files";
            exts.add("tsx");
            if (pmio == null) {
                pmio = new XMLMapWriter();
            }
        }

        if (filter == FILTER_EXT) {
            desc = "By Extension";
        }
    }

    public TiledFileFilter(PluggableMapIO p) throws Exception {
        exts = new LinkedList();
        pmio = p;
        buildFilter(p.getFilter(), p.getName());
    }

    public TiledFileFilter(String filter, String desc) {
        exts = new LinkedList();
        buildFilter(filter, desc);
    }

    private void buildFilter(String filter, String desc) {
        this.desc = desc;
        String[] ext = filter.split(",");
        for (int i = 0; i < ext.length; i++) {
            exts.add(ext[i].substring(ext[i].indexOf('.') + 1));
        }
    }

    public void setDescription(String d) {
        desc = d;
    }

    public void addExtention(String e) {
        exts.add(e);
    }

    public PluggableMapIO getPlugin() {
        return pmio;
    }

    public String getFirstExtention() {
        return (String) exts.getFirst();
    }

    public int getType() {
        return type;
    }

    public boolean accept(File f) {
        // todo: Verify that the "!f.exists()" check is rather weird.
        if (type != FILTER_EXT && (f.isFile() || !f.exists())) {
            String fileName = f.getPath().toLowerCase();

            Iterator itr = exts.iterator();
            while (itr.hasNext()) {
                String ext = (String) itr.next();
                if (fileName.endsWith("." + ext)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public String getDescription() {
        String filter = "";

        if (!exts.isEmpty()) {
            filter += " (";
            Iterator itr = exts.iterator();
            while (itr.hasNext()) {
                String ext = (String) itr.next();
                filter = filter + "*." + ext;
                if (itr.hasNext()) {
                    filter += ",";
                }
            }

            filter += ")";
        }

        return desc + filter;
    }
}
