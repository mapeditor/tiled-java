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
import javax.swing.*;

import tiled.core.*;


public class TileDialog extends JDialog
    implements ActionListener, MouseListener
{
    private Tile currentTile;
    private TileSet currentTileSet;
    private JList tileList = null;
    private JCheckBox tMovable, tPainful, tImpassable, tAnimated, tBottom;
    private JCheckBox tLink, tNoMCross, tLanding, tCost;
    private JTextField tCostField;
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
        tMovable = new JCheckBox("Movable");
        tMovable.setToolTipText("Is this tile pushable/pullable?");
        tMovable.addActionListener(this);
        c.gridx = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        getContentPane().add(tMovable,c);
        tPainful = new JCheckBox("Painful");
        tPainful.setToolTipText("Will this tile cause physical damage when crossed?");
        tPainful.addActionListener(this);
        c.gridx = 3;
        getContentPane().add(tPainful,c);
        tImpassable = new JCheckBox("Impassable");
        tImpassable.setToolTipText("Cannot be crossed i.e. a wall or tree trunk");
        tImpassable.addActionListener(this);
        c.gridx = 2;
        c.gridy = 2;
        getContentPane().add(tImpassable,c);
        tAnimated = new JCheckBox("Animated");
        tAnimated.setToolTipText("Does this tile have more than one state/frame");
        tAnimated.addActionListener(this);
        c.gridx = 3;
        getContentPane().add(tAnimated,c);
        tBottom = new JCheckBox("Bottom");
        tBottom.setToolTipText("Bottom tiles make up the \"ground level\" of the map");
        tBottom.addActionListener(this);
        c.gridx = 2;
        c.gridy = 3;
        getContentPane().add(tBottom,c);
        tLink = new JCheckBox("Link");
        tLink.setToolTipText("When touched, this tile will transport toucher to map id: ");
        tLink.addActionListener(this);
        c.gridx = 3;
        getContentPane().add(tLink,c);
        tNoMCross = new JCheckBox("No Monster Cross");
        tNoMCross.setToolTipText("Monsters cannot cross this tile");
        tNoMCross.addActionListener(this);
        c.gridx = 2;
        c.gridy = 4;
        getContentPane().add(tNoMCross,c);
        tLanding = new JCheckBox("Landing point");
        tLanding.addActionListener(this);
        c.gridx = 3;
        getContentPane().add(tLanding,c);
        tCost = new JCheckBox("Tile has cost");
        tCost.setToolTipText("This is used by the pathfinding code - a lower weight means the AI is more likely to use it");
        tCost.addActionListener(this);
        c.gridx = 2;
        c.gridy = 5;
        getContentPane().add(tCost,c);
        tCostField = new JTextField();
        if (currentTile != null) {
            tCostField.setText("" + currentTile.getCost());
        } else {
            tCostField.setText("0.0");
        }
        tCostField.setEnabled(false);
        c.gridx = 3;
        getContentPane().add(tCostField,c);
        tLinkList = new JComboBox();
        tLinkList.setEnabled(false);
        tLinkList.setEditable(true);
        c.gridx = 4;
        c.gridy = 3;
        getContentPane().add(tLinkList,c);
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

        short flags = currentTile.getFlags();

        tMovable.setSelected((flags & Tile.T_MOVABLE) != 0);
        tPainful.setSelected((flags & Tile.T_PAINFUL) != 0);
        tImpassable.setSelected((flags & Tile.T_IMPASSABLE) != 0);
        tAnimated.setSelected((flags & Tile.T_ANIMATED) != 0);
        tBottom.setSelected((flags & Tile.T_BOTTOM) != 0);

        if ((flags & Tile.T_LINK) != 0) {
            tLink.setSelected(true);
            tLinkList.setEnabled(true);
            tLinkList.addItem(currentTile.getLink());
        } else {
            tLink.setSelected(false);
            tLinkList.setEnabled(false);
        }

        tNoMCross.setSelected((flags & Tile.T_NOMCROSS) != 0);
        tLanding.setSelected((flags & Tile.T_LANDING) != 0);

        if ((flags & Tile.T_COST) == Tile.T_COST) {
            tCost.setSelected(true);
            tCostField.setEnabled(true);
        } else {
            tCost.setSelected(false);
            tCostField.setEnabled(false);
        }

        tCostField.setText("" + currentTile.getCost());
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("OK")) {
            if (tCost.isSelected()) {
                //System.out.print("Setting tile cost to: "+tCostField.getText());
                if (currentTile != null) {
                    currentTile.setCost(Float.parseFloat(tCostField.getText()));
                    if (tLinkList.isEnabled()) {
                        currentTile.setLink((String)tLinkList.getSelectedItem());
                    }
                }
            }
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

        if (e.getActionCommand().equals("Movable")) {
            if (tMovable.isSelected()) {
                currentTile.appendFlag((short)Tile.T_MOVABLE);
            } else {
                currentTile.unsetFlag((short)Tile.T_MOVABLE);
            }
        } else if (e.getActionCommand().equals("Painful")) {
            if (tPainful.isSelected()) {
                currentTile.appendFlag((short)Tile.T_PAINFUL);
            } else {
                currentTile.unsetFlag((short)Tile.T_PAINFUL);
            }

        } else if (e.getActionCommand().equals("Impassable")) {
            if (tImpassable.isSelected()) {
                currentTile.appendFlag((short)Tile.T_IMPASSABLE);
            } else {
                currentTile.unsetFlag((short)Tile.T_IMPASSABLE);
            }

        } else if (e.getActionCommand().equals("Animated")) {
            if (tAnimated.isSelected()) {
                currentTile.appendFlag((short)Tile.T_ANIMATED);
            } else {
                currentTile.unsetFlag((short)Tile.T_ANIMATED);
            }

        } else if (e.getActionCommand().equals("Bottom")) {
            if (tBottom.isSelected()) {
                currentTile.appendFlag((short)Tile.T_BOTTOM);
            } else {
                currentTile.unsetFlag((short)Tile.T_BOTTOM);
            }

        } else if (e.getActionCommand().equals("Link")) {
            if (tLink.isSelected()) {
                currentTile.appendFlag((short)Tile.T_LINK);
                tLinkList.setEnabled(true);
            } else {
                currentTile.unsetFlag((short)Tile.T_LINK);
                tLinkList.setEnabled(false);
            }

        } else if (e.getActionCommand().equals("No Monster Cross")) {
            if (tNoMCross.isSelected()) {
                currentTile.appendFlag((short)Tile.T_NOMCROSS);
            } else {
                currentTile.unsetFlag((short)Tile.T_NOMCROSS);
            }

        } else if (e.getActionCommand().equals("Landing point")) {
            if (tLanding.isSelected()) {
                currentTile.appendFlag((short)Tile.T_LANDING);
            } else {
                currentTile.unsetFlag((short)Tile.T_LANDING);
            }

        } else if (e.getActionCommand().equals("Tile has cost")) {
            if (tCost.isSelected()) {
                currentTile.appendFlag((short)Tile.T_COST);
                tCostField.setEnabled(true);
            } else {
                currentTile.unsetFlag((short)Tile.T_COST);
                tCostField.setEnabled(false);
            }

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
}
