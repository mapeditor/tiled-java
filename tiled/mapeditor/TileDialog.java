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

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tiled.core.*;


public class TileDialog extends JDialog
    implements ActionListener, MouseListener, ListSelectionListener
{
    private Tile currentTile;
    private TileSet currentTileSet;
    private JList tileList = null;
    private JTable tileProperties;
    private JComboBox tLinkList;
    private JButton bOk, bNew, bDelete, bChangeI, bDuplicate;

    public TileDialog(Dialog parent, TileSet s) {
        super(parent, "Edit Tileset '" + s.getName() + "'", true);
        currentTile = s.getFirstTile();
        setTileSet(s);
        init();
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void init() {
    	tileProperties = new JTable(new PropertiesTableModel());
		tileProperties.getSelectionModel().addListSelectionListener(this);
		JScrollPane propScrollPane = new JScrollPane(tileProperties);
		propScrollPane.setPreferredSize(new Dimension(150, 150));
        // Create the buttons
        bOk = new JButton("OK");
        bDelete = new JButton("Delete Tile");
        bChangeI = new JButton("Change Image");
        bDuplicate = new JButton("Duplicate Tile");
        bNew = new JButton("New Tile");

		if(currentTileSet.getSource() != null) {
			bDelete.setEnabled(false);
			bDuplicate.setEnabled(false);
			bNew.setEnabled(false);
			bChangeI.setEnabled(false);
		}

        bOk.addActionListener(this);
        bDelete.addActionListener(this);
        bChangeI.addActionListener(this);
        bDuplicate.addActionListener(this);
        bNew.addActionListener(this);

        JPanel buttonPanel = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        JScrollPane sp = new JScrollPane();
        c.fill = GridBagConstraints.BOTH;
        getContentPane().setLayout(new GridBagLayout());
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 6;
        c.gridwidth = 2;
        if (tileList != null) {
            tileList.addMouseListener(this);

            sp.getViewport().setView(tileList);
            getContentPane().add(sp,c);
        }
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.gridx = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        getContentPane().add(propScrollPane,c);
        
        c.gridx = 0;
        c.gridwidth = 5;
        c.gridy = 6;
        buttonPanel.add(bNew);
        buttonPanel.add(bDelete);
        buttonPanel.add(bChangeI);
        buttonPanel.add(bDuplicate);
        buttonPanel.add(bOk);
        getContentPane().add(buttonPanel,c);
        updateTileInfo();
    }

    private void changeImage() {
        File file;
        JFileChooser ch = new JFileChooser();
        ch.setMultiSelectionEnabled(true);
        int ret = ch.showOpenDialog(this);
        file = ch.getSelectedFile();

        try {
            currentTile.setImage(Toolkit.getDefaultToolkit().getImage(
                        file.getAbsolutePath()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void newTile(Tile t) {
        String name = null;
        File files[];
        JFileChooser ch = new JFileChooser();
        ch.setMultiSelectionEnabled(true);
        Image image = null;

        if (t == null) {
            int ret = ch.showOpenDialog(this);
            files = ch.getSelectedFiles();

            for (int i = 0; i < files.length; i++) {
                name = files[i].getAbsolutePath();

                try {
                    image = Toolkit.getDefaultToolkit().getImage(name);
                    MediaTracker mediaTracker = new MediaTracker(this);
                    mediaTracker.addImage(image, 0);
                    try {
                        mediaTracker.waitForID(0);
                    }
                    catch (InterruptedException ie) {
                        System.err.println(ie);
                        return;
                    }
                    mediaTracker.removeImage(image);
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

    public Tile choose() {
        show();

        //System.out.println("Returning: "+currentTile);
        return currentTile;
    }

	public void setTileSet(TileSet s) {
		currentTileSet = s;
		queryTiles();
	}

    public void queryTiles() {
        Tile[] listData;
        int curSlot = 0;

        if (tileList == null) {
            tileList = new JList();
        }

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

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("OK")) {
            this.dispose();
        } else if (e.getActionCommand().equals("Delete Tile")) {
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
        } else if (e.getActionCommand().equals("Change Image")) {
            changeImage();
        } else if (e.getActionCommand().equals("New Tile")) {
            newTile(null);
        } else if (e.getActionCommand().equals("Duplicate Tile")) {
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
            
            //update the old current tile's properties
            currentTile.setProperties(((PropertiesTableModel)tileProperties.getModel()).getProperties());
            
            //set the new current tile
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

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		
	}
}
