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

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Enumeration;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tiled.core.*;


public class TileDialog extends JDialog
    implements ActionListener, MouseListener, ListSelectionListener
{
    private Tile currentTile;
    private TileSet currentTileSet;
    private JList tileList;
    private JTable tileProperties;
    private JComboBox tLinkList;
    private JButton bOk, bNew, bDelete, bChangeI, bDuplicate;

    public TileDialog(Dialog parent, TileSet s) {
        super(parent, "Edit Tileset '" + s.getName() + "'", true);
        init();
        setTileSet(s);
        if (currentTileSet.getSource() != null) {
            bDelete.setEnabled(false);
            bChangeI.setEnabled(false);
            bDuplicate.setEnabled(false);
            bNew.setEnabled(false);
        }
        currentTile = s.getFirstTile();
        updateTileInfo();
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void init() {
        // Create the buttons

        bOk = new JButton("OK");
        bDelete = new JButton("Delete Tile");
        bChangeI = new JButton("Change Image");
        bDuplicate = new JButton("Duplicate Tile");
        bNew = new JButton("Add Tile");

        bOk.addActionListener(this);
        bDelete.addActionListener(this);
        bChangeI.addActionListener(this);
        bDuplicate.addActionListener(this);
        bNew.addActionListener(this);

        tileList = new JList();


        // Tile properties table

        tileProperties = new JTable(new PropertiesTableModel());
        tileProperties.getSelectionModel().addListSelectionListener(this);
        JScrollPane propScrollPane = new JScrollPane(tileProperties);
        propScrollPane.setPreferredSize(new Dimension(150, 150));


        // Tile list

        tileList.addMouseListener(this);
        JScrollPane sp = new JScrollPane();
        sp.getViewport().setView(tileList);


        // The split pane

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, true);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        splitPane.setLeftComponent(sp);
        splitPane.setRightComponent(propScrollPane);


        // The buttons

        JPanel buttons = new VerticalStaticJPanel();
        buttons.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(bNew);
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(bDelete);
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(bChangeI);
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(bDuplicate);
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(Box.createGlue());
        buttons.add(bOk);


        // Putting it all together

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1; c.weighty = 1;
        mainPanel.add(splitPane, c);
        c.weightx = 0; c.weighty = 0; c.gridy = 1;
        mainPanel.add(buttons, c);

        getContentPane().add(mainPanel);
        getRootPane().setDefaultButton(bOk);
    }

    private void changeImage() {
        JFileChooser ch = new JFileChooser();
        ch.setMultiSelectionEnabled(true);
        int ret = ch.showOpenDialog(this);

        try {
            currentTile.setImage(ImageIO.read(ch.getSelectedFile()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void newTile(Tile t) {
        File files[];
        JFileChooser ch = new JFileChooser();
        ch.setMultiSelectionEnabled(true);
        Image image = null;

        if (t == null) {
            int ret = ch.showOpenDialog(this);
            files = ch.getSelectedFiles();

            for (int i = 0; i < files.length; i++) {

                try {
                    image = ImageIO.read(files[i]);
                    //n.setImage(Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(),new TransImageFilter(cm.getRGB(64305)))));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, e.getMessage(),
                            "Error!", JOptionPane.ERROR_MESSAGE);
                    return;
                }


                try {
                    Tile newTile = new Tile();
                    newTile.setImage(image);
                    currentTileSet.addNewTile(newTile);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, e.getMessage(),
                            "Error while loading tiles!",
                            JOptionPane.ERROR_MESSAGE);
                }

            }
        } else {
            Tile n = new Tile(t);
            currentTileSet.addNewTile(n);
        }
        queryTiles();
    }

    public void setTileSet(TileSet s) {
        currentTileSet = s;
        queryTiles();
    }

    public void queryTiles() {
        Tile[] listData;
        int curSlot = 0;

        if (currentTileSet != null && currentTileSet.getTotalTiles() > 0) {
            int totalTiles = currentTileSet.getTotalTiles();
            listData = new Tile [totalTiles];
            for (int i = 0; i < totalTiles; i++) {
                listData[curSlot++] = currentTileSet.getTile(i);
            }

            tileList.setListData(listData);
        }
        if (currentTile != null) {
            tileList.setSelectedIndex(currentTile.getGid() - 1);
            tileList.ensureIndexIsVisible(currentTile.getGid() - 1);
        }
        tileList.setCellRenderer(new TileDialogListRenderer(currentTileSet));
        tileList.repaint();
    }

    public void updateTileInfo() {
        if (currentTile == null) {
            return;
        }

        tileProperties.removeAll();

        Enumeration keys = currentTile.getProperties();
        Properties props = new Properties();
        while(keys.hasMoreElements()) {
            String key = (String) keys.nextElement(); 
            props.put(key, currentTile.getPropertyValue(key));
        }
        ((PropertiesTableModel)tileProperties.getModel()).update(props);
        tileProperties.repaint();
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();

        if (source == bOk) {
            this.dispose();
        } else if (source == bDelete) {
            int answer = JOptionPane.showConfirmDialog(
                    this, "Delete tile?", "Are you sure?",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (answer == JOptionPane.YES_OPTION) {
                Tile tile = (Tile)tileList.getSelectedValue();
                if (tile != null) {
                    currentTileSet.removeTile(tile.getId());
                }
                queryTiles();
            }
        } else if (source == bChangeI) {
            changeImage();
        } else if (source == bNew) {
            newTile(null);
        } else if (source == bDuplicate) {
            newTile(currentTile);
        }

        repaint();
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        Component c = e.getComponent();
        //System.out.println(c.getClass().toString());
        if (c.getClass().toString().equals("class javax.swing.JList")) {
            JList j = (JList)c;

            // Update the old current tile's properties
            if (currentTile != null) {
                currentTile.setProperties(((PropertiesTableModel)tileProperties.getModel()).getProperties());
            }

            // Set the new current tile
            currentTile = (Tile)j.getSelectedValue();

            updateTileInfo();
        } else {
        }
        c.repaint();
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void valueChanged(ListSelectionEvent e) {
    }
}
