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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;

import tiled.mapeditor.util.MultisetListRenderer;
import tiled.mapeditor.widget.*;
import tiled.core.*;
import tiled.core.Map;

public class SearchDialog extends JDialog implements ActionListener {

	private Map myMap;
	private JComboBox searchCBox, replaceCBox;
	private JButton bReplace, bReplaceAll;
	
	public SearchDialog(JFrame parent) {
			this(parent, null);
	}

	public SearchDialog(JFrame parent, Map map) {
			super(parent, "Search/Replace", true);			
			myMap = map;
			init();
			setLocationRelativeTo(parent);
			setModal(false);
	}

	private void init() {
		
		JPanel buttonPanel = new JPanel();
		JPanel searchPanel = new JPanel();
		VerticalStaticJPanel mainPanel = new VerticalStaticJPanel();
		mainPanel.setLayout(new BorderLayout());
		
		JButton bFind = new JButton("Find");
		bFind.addActionListener(this);
		JButton bFindNext = new JButton("Find Next");
		bFindNext.addActionListener(this);
		bReplace = new JButton("Replace");
		bReplace.addActionListener(this);
		bReplace.setEnabled(false);
		bReplaceAll = new JButton("Replace All");
		bReplaceAll.addActionListener(this);
		bReplaceAll.setEnabled(false);
		JButton bCancel = new JButton("Cancel");
		bCancel.addActionListener(this);
		
		/* SEARCH PANEL */
		searchPanel.setLayout(new BorderLayout());
		searchCBox = new JComboBox();
		searchCBox.setRenderer(new MultisetListRenderer(myMap, .5));
		//searchCBox.setSelectedIndex(1);
		searchCBox.setEditable(false);
		searchPanel.add(searchCBox,BorderLayout.WEST);
		JCheckBox cbReplace = new JCheckBox("Replace With");
		cbReplace.addActionListener(this);
		replaceCBox = new JComboBox();
		replaceCBox.setRenderer(new MultisetListRenderer(myMap, .5));
		//searchCBox.setSelectedIndex(1);
		replaceCBox.setEditable(false);
		replaceCBox.setEnabled(false);
		searchPanel.add(replaceCBox,BorderLayout.EAST);
		queryTiles(searchCBox);
		queryTiles(replaceCBox);
		searchPanel.add(cbReplace,BorderLayout.CENTER);
		mainPanel.add(searchPanel, BorderLayout.NORTH);
		
		/* BUTTONS PANEL */
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(Box.createGlue());
		buttonPanel.add(bFind);
		buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		buttonPanel.add(bFindNext);
		buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		buttonPanel.add(bReplace);
		buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		buttonPanel.add(bReplaceAll);
		buttonPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		buttonPanel.add(bCancel);
		mainPanel.add(buttonPanel,BorderLayout.SOUTH);
		
		getContentPane().add(mainPanel);
		getRootPane().setDefaultButton(bFind);
		pack();
	}

	public void showDialog() {
		show();
	}
	
	private void queryTiles(JComboBox b) {
		
		Vector sets = myMap.getTilesets();
		int curSlot = 0;
		Iterator itr = sets.iterator();

		while(itr.hasNext()) {
			TileSet ts = (TileSet) itr.next();
			b.addItem(ts);
			
			for (int i = 0; i < ts.getTotalTiles(); i++) {
				b.addItem(ts.getTile(i));
			}
		}	
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		
		if(command.equalsIgnoreCase("cancel")) {
			//TODO: this must untrigger the drawing condition
			this.dispose();
		} else if(command.equalsIgnoreCase("find")) {
			//TODO: find must trigger a drawing condition to highlight instances of the found tile on the map
			
		} else if(command.equalsIgnoreCase("replace all")) {
			replaceAll((Tile) searchCBox.getSelectedItem(),(Tile) replaceCBox.getSelectedItem());
		} else if(command.equalsIgnoreCase("replace with")) {
			if(((JCheckBox)e.getSource()).isSelected()) {
				bReplace.setEnabled(true);
				bReplaceAll.setEnabled(true);
				replaceCBox.setEnabled(true);
			} else {
				bReplace.setEnabled(false);
				bReplaceAll.setEnabled(false);
				replaceCBox.setEnabled(false);
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
		
	}
}
