/*
 *  Tiled Map Editor, (c) 2005
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <b.lindeijer@xs4all.nl>
 */

package tiled.mapeditor.dialogs;

import java.awt.Dialog;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javax.swing.*;

import tiled.core.*;

/**
 * $Id$
 */
public class NewTileDialog extends JDialog implements ActionListener
{
	private TileSet tileset;
	private Tile currentTile;
	private String location;

	private JButton bAddTiles, bCancel;

	public NewTileDialog(Dialog parent, TileSet set) {
		super(parent, "New Tile", true);
		tileset = set;
		location = "";

		init();
		pack();
		setLocationRelativeTo(getOwner());
	}

	private void init() {

	}

	public Tile createTile() {
		setVisible(true);
		return currentTile;
	}

	private void pickImage() {
		if (tileset.isSetFromImage()) {
            TileImageDialog d = new TileImageDialog(this, tileset);
            d.setVisible(true);
            if (d.getImageId() >= 0) {
                currentTile = new Tile(tileset);
                currentTile.setImage(d.getImageId());
            }
            return;
        }

        File[] files;
        JFileChooser ch = new JFileChooser(location);
        ch.setMultiSelectionEnabled(true);
        BufferedImage image = null;

        int ret = ch.showOpenDialog(this);
        if(ret == JFileChooser.APPROVE_OPTION) {
		    files = ch.getSelectedFiles();

		    for (int i = 0; i < files.length; i++) {
		        try {
		            image = ImageIO.read(files[i]);
		            // TODO: Support for a transparent color
		        } catch (IOException e) {
		            JOptionPane.showMessageDialog(this, e.getLocalizedMessage(),
		                    "Error!", JOptionPane.ERROR_MESSAGE);
		            return;
		        }

		        Tile newTile = new Tile(tileset);
		        newTile.setImage(image);
		        tileset.addNewTile(newTile);
		    }

		    if (files.length > 0) {
		        location = files[0].getAbsolutePath();
		    }

        }
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
	}
}
