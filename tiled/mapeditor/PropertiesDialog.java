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

package tiled.mapeditor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;

import tiled.core.*;


public class PropertiesDialog extends JDialog implements ActionListener
{
    Map currentMap;

    public PropertiesDialog(MapEditor m) {
        currentMap = m.getCurrentMap();
        setSize(550,200);
        setLocation(100,250);
        setTitle("Map Properties");
        setModal(true);
    }

    private void init() {
    }


    public void getProps() {
        init();
        show();
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
    }
}
