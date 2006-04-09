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

package tiled.mapeditor.dialogs;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.*;

import tiled.core.*;
import tiled.mapeditor.selection.SelectionLayer;
import tiled.mapeditor.util.MultisetListRenderer;
import tiled.mapeditor.widget.VerticalStaticJPanel;

/**
 * @version $Id$
 */
public class SearchDialog extends JDialog implements ActionListener
{
    private final Map map;
    private JComboBox searchCBox, replaceCBox;
    private Point currentMatch;
    private SelectionLayer sl;
    private static final double LIST_TILE_SCALE = 0.5;

    public SearchDialog(JFrame parent) {
        this(parent, null);
    }

    public SearchDialog(JFrame parent, Map map) {
        super(parent, "Search/Replace", false);
        this.map = map;
        init();
        setLocationRelativeTo(parent);
    }

    private void init() {
        final MultisetListRenderer tileListRenderer;
        tileListRenderer = new MultisetListRenderer(LIST_TILE_SCALE);

        /* SEARCH PANEL */
        JPanel searchPanel = new JPanel();
        searchPanel.setBorder(BorderFactory.createEtchedBorder());
        searchPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 2; c.weighty = 1;
        searchPanel.add(new JLabel("Find:"), c);
        c.gridx = 1;
        searchCBox = new JComboBox();
        searchCBox.setRenderer(tileListRenderer);
        //searchCBox.setSelectedIndex(1);
        searchCBox.setEditable(false);
        searchPanel.add(searchCBox, c);
        c.gridy = 1;
        c.gridx = 0;
        searchPanel.add(new JLabel("Replace:"), c);
        c.gridx = 1;
        replaceCBox = new JComboBox();
        replaceCBox.setRenderer(tileListRenderer);
        //searchCBox.setSelectedIndex(1);
        replaceCBox.setEditable(false);
        searchPanel.add(replaceCBox,c);
        queryTiles(searchCBox);
        //replaceCBox.addItem(null);
        queryTiles(replaceCBox);

        /* SCOPE PANEL */
        /*
        JPanel scopePanel = new JPanel();
        scopePanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Scope"),
                    BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        */

        final JButton bFind = new JButton("Find");
        final JButton bFindAll = new JButton("Find All");
        final JButton bReplace = new JButton("Replace");
        final JButton bReplaceAll = new JButton("Replace All");
        final JButton bClose = new JButton("Close");

        bFind.addActionListener(this);
        bFindAll.addActionListener(this);
        bReplace.addActionListener(this);
        bReplaceAll.addActionListener(this);
        bClose.addActionListener(this);


        /* BUTTONS PANEL */
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 2, 5, 5));
        buttonPanel.add(bFind);
        buttonPanel.add(bFindAll);
        buttonPanel.add(bReplace);
        buttonPanel.add(bReplaceAll);

        JPanel closePanel = new VerticalStaticJPanel();
        closePanel.setLayout(new BorderLayout());
        closePanel.add(bClose, BorderLayout.EAST);

        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        //mainPanel.add(scopePanel);
        //mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(closePanel);

        getContentPane().add(mainPanel);
        getRootPane().setDefaultButton(bFind);
        pack();
    }

    private void queryTiles(JComboBox b) {
        final Vector sets = map.getTilesets();
        final Iterator itr = sets.iterator();

        while (itr.hasNext()) {
            TileSet ts = (TileSet)itr.next();
            b.addItem(ts);

            final Iterator tileIterator = ts.iterator();
            while (tileIterator.hasNext()) {
                Tile tile = (Tile)tileIterator.next();
                b.addItem(tile);
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equalsIgnoreCase("close")) {
            map.removeLayerSpecial(sl);
            dispose();
        } else if (command.equalsIgnoreCase("find")) {
            if (searchCBox.getSelectedItem() instanceof Tile) {
                find((Tile)searchCBox.getSelectedItem());
            }
        } else if (command.equalsIgnoreCase("find all")) {
            if (sl != null) {
                map.removeLayerSpecial(sl);
            }

            sl = new SelectionLayer(map.getWidth(), map.getHeight());
            final Iterator itr = map.getLayers();
            while (itr.hasNext()) {
                MapLayer layer = (MapLayer) itr.next();
                if (layer instanceof TileLayer) {
                    Rectangle bounds = layer.getBounds();
                    for (int y = 0; y < bounds.height; y++) {
                        for (int x = 0; x < bounds.width; x++) {
                            if (((TileLayer)layer).getTileAt(x,y) == searchCBox.getSelectedItem()) {
                                sl.select(x,y);
                            }
                        }
                    }
                }
            }
            map.addLayerSpecial(sl);
            map.touch();

        } else if (command.equalsIgnoreCase("replace all")) {
            if (!(searchCBox.getSelectedItem() instanceof TileSet) && !(replaceCBox.getSelectedItem() instanceof TileSet))
                replaceAll((Tile) searchCBox.getSelectedItem(),(Tile) replaceCBox.getSelectedItem());
        } else if (command.equalsIgnoreCase("replace")) {
            if (searchCBox.getSelectedItem() instanceof Tile && replaceCBox.getSelectedItem() instanceof Tile) {
                if (currentMatch == null) {
                    find((Tile)searchCBox.getSelectedItem());
                }

                // run through the layers, look for the first instance of the
                // tile we need to replace
                final Iterator itr = map.getLayers();
                while (itr.hasNext()) {
                    MapLayer layer = (MapLayer) itr.next();
                    if (layer instanceof TileLayer) {
                        if (((TileLayer)layer).getTileAt(currentMatch.x,currentMatch.y) == searchCBox.getSelectedItem()) {
                            ((TileLayer)layer).setTileAt(currentMatch.x,currentMatch.y, (Tile) replaceCBox.getSelectedItem());
                            break;
                        }
                    }
                }
                // find the next instance, effectively stepping forward in our
                // replace
                find((Tile)searchCBox.getSelectedItem());
            }
        }

    }

    private void replaceAll(Tile f, Tile r) {
        // TODO: Allow for "scopes" of one or more layers, rather than all layers
        final Iterator itr = map.getLayers();
        while (itr.hasNext()) {
            MapLayer layer = (MapLayer) itr.next();
            if (layer instanceof TileLayer) {
                ((TileLayer)layer).replaceTile(f,r);
            }
        }
        map.touch();
    }

    private void find(Tile f) {
        boolean bFound = false;

        if (sl != null) {
            map.removeLayerSpecial(sl);
            map.touch();
        }

        sl = new SelectionLayer(map.getWidth(), map.getHeight());


        int startx = currentMatch == null ? 0 : currentMatch.x;
        int starty = currentMatch == null ? 0 : currentMatch.y;

        for (int y = starty; y < map.getHeight() && !bFound; y++) {
            for (int x = startx; x < map.getWidth() && !bFound; x++) {
                final Iterator itr = map.getLayers();
                while (itr.hasNext()) {
                    MapLayer layer = (MapLayer) itr.next();

                    if (layer instanceof TileLayer) {
                        Rectangle bounds = layer.getBounds();

                        if (((TileLayer)layer).getTileAt(x,y) == searchCBox.getSelectedItem()) {
                            if (currentMatch != null) {
                                if (currentMatch.equals(new Point(x,y))) {
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
        }

        if (bFound) {
            map.addLayerSpecial(sl);
            map.touch();
        }
    }
}
