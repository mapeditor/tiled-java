/*
 * Tiled Map Editor, (c) 2004
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

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import tiled.mapeditor.selection.SelectionLayer;
import tiled.mapeditor.util.MultisetListRenderer;
import tiled.mapeditor.widget.*;
import tiled.core.*;
import tiled.core.Map;


public class SearchDialog extends JDialog implements ActionListener
{
    private Map myMap;
    private JComboBox searchCBox, replaceCBox;
    private JButton bReplace, bReplaceAll;
    private Point currentMatch = null;
    private SelectionLayer sl;

    public SearchDialog(JFrame parent) {
        this(parent, null);
    }

    public SearchDialog(JFrame parent, Map map) {
        super(parent, "Search/Replace", false);
        myMap = map;
        init();
        setLocationRelativeTo(parent);
    }

    private void init() {
		JPanel buttonPanel = new JPanel();
		JPanel closePanel = new JPanel();
		closePanel.setLayout(new BorderLayout());
		JPanel scopePanel = new JPanel();
		JPanel searchPanel = new JPanel();
		VerticalStaticJPanel mainPanel = new VerticalStaticJPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		JButton bFind = new JButton("Find");
		bFind.addActionListener(this);
		JButton bFindNext = new JButton("Find All");
		bFindNext.addActionListener(this);
		bReplace = new JButton("Replace");
		bReplace.addActionListener(this);
		bReplaceAll = new JButton("Replace All");
		bReplaceAll.addActionListener(this);
		JButton bCancel = new JButton("Close");
		bCancel.addActionListener(this);
		
		/* SEARCH PANEL */
		searchPanel.setBorder(BorderFactory.createEtchedBorder());
		searchPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 2; c.weighty = 1;
		searchPanel.add(new JLabel("Find:"),c);
		c.gridx=1;
		searchCBox = new JComboBox();
		searchCBox.setRenderer(new MultisetListRenderer(myMap, .5));
		//searchCBox.setSelectedIndex(1);
		searchCBox.setEditable(false);
		searchPanel.add(searchCBox,c);
		c.gridy=1;
		c.gridx=0;
		searchPanel.add(new JLabel("Replace:"),c);
		c.gridx=1;
		replaceCBox = new JComboBox();
		replaceCBox.setRenderer(new MultisetListRenderer(myMap, .5));
		//searchCBox.setSelectedIndex(1);
		replaceCBox.setEditable(false);
		searchPanel.add(replaceCBox,c);
		queryTiles(searchCBox);
		//replaceCBox.addItem(null);
		queryTiles(replaceCBox);
		mainPanel.add(searchPanel, BorderLayout.NORTH);
		
		/* SCOPE PANEL */
		/*scopePanel.setBorder(BorderFactory.createCompoundBorder(
							BorderFactory.createTitledBorder("Scope"),
							BorderFactory.createEmptyBorder(0, 5, 5, 5)));
		
		mainPanel.add(scopePanel);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));*/
		
		/* BUTTONS PANEL */
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		buttonPanel.setLayout(new GridLayout(2,2,0,0));
		buttonPanel.add(bFind);
		buttonPanel.add(bFindNext);
		buttonPanel.add(bReplace);
		buttonPanel.add(bReplaceAll);
		closePanel.add(bCancel, BorderLayout.EAST);
		mainPanel.add(buttonPanel);
		mainPanel.add(closePanel);
		
		getContentPane().add(mainPanel);
		getRootPane().setDefaultButton(bFind);
		pack();
	}

    public void showDialog() {
        setVisible(true);
    }

	private void queryTiles(JComboBox b) {
		Vector sets = myMap.getTilesets();
		int curSlot = 0;
		Iterator itr = sets.iterator();

		while (itr.hasNext()) {
			TileSet ts = (TileSet) itr.next();
			b.addItem(ts);
			
			for (int i = 0; i < ts.getTotalTiles(); i++) {
				b.addItem(ts.getTile(i));
			}
		}	
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		
		if(command.equalsIgnoreCase("close")) {
			myMap.removeLayerSpecial(sl);
			this.dispose();
		} else if(command.equalsIgnoreCase("find")) {
			if(searchCBox.getSelectedItem() instanceof Tile) {
				find((Tile)searchCBox.getSelectedItem());
			}
		} else if(command.equalsIgnoreCase("find all")) {
			if(sl != null) {
				myMap.removeLayerSpecial(sl);
			}
			
			sl = new SelectionLayer(myMap.getWidth(), myMap.getHeight());
			ListIterator itr = myMap.getLayers();		
			while(itr.hasNext()) {
				MapLayer layer = (MapLayer) itr.next();
				Rectangle bounds = layer.getBounds();
				for (int y = 0; y < bounds.height; y++) {
						for (int x = 0; x < bounds.width; x++) {
							if(layer.getTileAt(x,y) == (Tile) searchCBox.getSelectedItem()) {
								sl.select(x,y);
							}
						}
				}
			}
			myMap.addLayerSpecial(sl);
			myMap.touch();
			
		} else if(command.equalsIgnoreCase("replace all")) {
			if(!(searchCBox.getSelectedItem() instanceof TileSet) && !(replaceCBox.getSelectedItem() instanceof TileSet))
				replaceAll((Tile) searchCBox.getSelectedItem(),(Tile) replaceCBox.getSelectedItem());
		} else if(command.equalsIgnoreCase("replace")) {
			if((searchCBox.getSelectedItem() instanceof Tile) && (replaceCBox.getSelectedItem() instanceof Tile)) {
				if(currentMatch == null) {
					find((Tile)searchCBox.getSelectedItem());
				}
				
				//run through the layers, look for the first instance of the tile we need to replace
				ListIterator itr = myMap.getLayers();		
				while(itr.hasNext()) {
					MapLayer layer = (MapLayer) itr.next();
					if(layer.getTileAt(currentMatch.x,currentMatch.y) == (Tile) searchCBox.getSelectedItem()) {
						layer.setTileAt(currentMatch.x,currentMatch.y, (Tile) replaceCBox.getSelectedItem());
						break;
					}
				}
				//find the next instance, effectively stepping forward in our replace
				find((Tile)searchCBox.getSelectedItem());
			}
		}
		
	}
	
	private void replaceAll(Tile f, Tile r) {
		//TODO: allow for "scopes" of one or more layers, rather than all layers
		ListIterator itr = myMap.getLayers();		
		while(itr.hasNext()) {
			MapLayer layer = (MapLayer) itr.next();
			layer.replaceTile(f,r);
		}
		myMap.touch();
	}
	
	private void find(Tile f) {
		boolean bFound = false;
				
		if(sl != null) {
			myMap.removeLayerSpecial(sl);
			myMap.touch();
		}
		
		sl = new SelectionLayer(myMap.getWidth(), myMap.getHeight());
		

		int startx = currentMatch == null ? 0 : currentMatch.x;
		int starty = currentMatch == null ? 0 : currentMatch.y;
		
		for (int y = starty; y < myMap.getHeight() && !bFound; y++) {
			for (int x = startx; x < myMap.getWidth() && !bFound; x++) {
				ListIterator itr = myMap.getLayers();		
				while(itr.hasNext()) {
					MapLayer layer = (MapLayer) itr.next();
					Rectangle bounds = layer.getBounds();
	
					if(layer.getTileAt(x,y) == (Tile) searchCBox.getSelectedItem()) {
						if(currentMatch != null) {
							if(currentMatch.equals(new Point(x,y))) {
								continue;
							}
						}
						sl.select(x,y);
						bFound = true;
						currentMatch = new Point(x,y);
						break;
					}
				}
			}
		}

		if(bFound) {
			myMap.addLayerSpecial(sl);
			myMap.touch();
		}
	}
}
