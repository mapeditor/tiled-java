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
 *  Rainer Deyke <rainerd@eldwood.com>
 */

package tiled.mapeditor.dialogs;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tiled.core.*;
import tiled.mapeditor.Resources;
import tiled.mapeditor.animation.AnimationDialog;
import tiled.mapeditor.util.*;
import tiled.mapeditor.widget.*;

/**
 * @version $Id$
 */
public class TileDialog extends JDialog
    implements ActionListener, ListSelectionListener
{
    private Tile currentTile;
    private TileSet tileset;
    private Map map;
    private JList tileList, imageList;
    private JTable tileProperties;
    private JButton okButton, newTileButton, deleteTileButton, changeImageButton, duplicateTileButton;
    private JButton createTileButton;
    private JButton animationButton;
    private String location;
    private JTextField tilesetNameEntry;
    private JCheckBox externalBitmapCheck;
    private JTabbedPane tabs;
    private int currentImageIndex = -1;

    /* LANGUAGE PACK */
    private static final String DIALOG_TITLE = Resources.getString("dialog.tile.title");
    private static final String OK_BUTTON = Resources.getString("general.button.ok");
    private static final String DELETE_BUTTON = Resources.getString("dialog.tile.button.deletetile");
    private static final String CI_BUTTON = Resources.getString("dialog.tile.button.changeimage");
    private static final String NEW_BUTTON = Resources.getString("dialog.tile.button.newtile");
    private static final String CREATE_BUTTON = Resources.getString("dialog.tile.button.createtile");
    private static final String DUPLICATE_BUTTON = Resources.getString("dialog.tile.button.duptile");
    private static final String ANIMATION_BUTTON = Resources.getString("dialog.tile.button.animation");
    private static final String PREVIEW_TAB = Resources.getString("general.button.preview");
    private static final String TILES_TAB = Resources.getString("general.tile.tiles");
    private static final String TILESET_TAB = Resources.getString("general.tile.tileset");
    private static final String NAME_LABEL = Resources.getString("dialog.newtileset.name.label");
    
    /* -- */
    
    public TileDialog(Dialog parent, TileSet s, Map m) {
        super(parent, DIALOG_TITLE + " '" + s.getName() + "'", true);
        location = "";
        tileset = s;    //unofficial
        map = m;        //also unofficial
        init();
        setTileset(s);
        setCurrentTile(null);
        pack();
        setLocationRelativeTo(getOwner());
    }

    private JPanel createTilePanel() {
        // Create the buttons

        deleteTileButton = new JButton(DELETE_BUTTON);
        changeImageButton = new JButton(CI_BUTTON);
        duplicateTileButton = new JButton(DUPLICATE_BUTTON);
        newTileButton = new JButton(NEW_BUTTON);
        animationButton = new JButton(ANIMATION_BUTTON);
        
        deleteTileButton.addActionListener(this);
        changeImageButton.addActionListener(this);
        duplicateTileButton.addActionListener(this);
        newTileButton.addActionListener(this);
        animationButton.addActionListener(this);
        
        tileList = new JList();
        tileList.setCellRenderer(new TileDialogListRenderer());


        // Tile properties table

        tileProperties = new JTable(new PropertiesTableModel());
        tileProperties.getSelectionModel().addListSelectionListener(this);
        JScrollPane propScrollPane = new JScrollPane(tileProperties);
        propScrollPane.setPreferredSize(new Dimension(150, 150));


        // Tile list

        tileList.addListSelectionListener(this);
        JScrollPane sp = new JScrollPane();
        sp.getViewport().setView(tileList);
        sp.setPreferredSize(new Dimension(150, 150));

        // The split pane

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, true);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        splitPane.setResizeWeight(0.25);
        splitPane.setLeftComponent(sp);
        splitPane.setRightComponent(propScrollPane);


        // The buttons

        JPanel buttons = new VerticalStaticJPanel();
        buttons.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(newTileButton);
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(deleteTileButton);
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(changeImageButton);
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(duplicateTileButton);
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(animationButton);
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(Box.createGlue());


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
        
        return mainPanel;
    }

    private JPanel createTilesetPanel()
    {
        JLabel name_label = new JLabel(NAME_LABEL+" ");
        tilesetNameEntry = new JTextField(32);
        //sharedImagesCheck = new JCheckBox("Use shared images");
        externalBitmapCheck = new JCheckBox("Use external bitmap");
        //sharedImagesCheck.addActionListener(this);
        externalBitmapCheck.addActionListener(this);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0;
        mainPanel.add(name_label, c);
        c.gridx = 1; c.gridy = 0;
        mainPanel.add(tilesetNameEntry);
        c.gridx = 0; c.gridy = 1; c.gridwidth = 2;
        //mainPanel.add(sharedImagesCheck, c);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
        mainPanel.add(externalBitmapCheck, c);

        return mainPanel;
    }

    private JPanel createImagePanel()
    {
        imageList = new JList();
        imageList.setCellRenderer(new ImageCellRenderer());
        imageList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        imageList.addListSelectionListener(this);
        JScrollPane sp = new JScrollPane();
        sp.getViewport().setView(imageList);
        sp.setPreferredSize(new Dimension(150, 150));

        // Buttons
        createTileButton = new JButton(CREATE_BUTTON);
        createTileButton.addActionListener(this);
        JPanel buttons = new VerticalStaticJPanel();
        buttons.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(createTileButton);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1; c.weighty = 1;
        mainPanel.add(sp, c);
        c.weightx = 0; c.weighty = 0; c.gridy = 1;
        mainPanel.add(buttons, c);
        return mainPanel;
    }

    private void init() {
        tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.addTab(TILESET_TAB, createTilesetPanel());
        tabs.addTab(TILES_TAB, createTilePanel());
        tabs.addTab(PREVIEW_TAB, createImagePanel());

        okButton = new JButton(OK_BUTTON);

        JPanel buttons = new VerticalStaticJPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(Box.createGlue());
        buttons.add(okButton);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(tabs);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(buttons);

        getContentPane().add(mainPanel);
        getRootPane().setDefaultButton(okButton);
        
        //create actionlisteners
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
            	tileset.setName(tilesetNameEntry.getText());
                dispose();
            }
        });
    }

    private void changeImage() {
        if (currentTile == null) {
            return;
        }

        if (tileset.isSetFromImage()) {
            TileImageDialog d = new TileImageDialog(this, tileset,
                currentTile.getImageId());
            d.setVisible(true);
            if (d.getImageId() >= 0) {
                currentTile.setImage(d.getImageId());
            }
        } else {
            Image img = loadImage();
            if (img != null) {
                currentTile.setImage(img);
            }
        }
    }

    private Image loadImage() {
        JFileChooser ch = new JFileChooser(location);
        int ret = ch.showOpenDialog(this);

        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = ch.getSelectedFile();
            try {
                BufferedImage image = ImageIO.read(file);
                if (image != null) {
                    location = file.getAbsolutePath();
                    return image;
                } else {
                    JOptionPane.showMessageDialog(this, "Error loading image",
                            "Error loading image", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, e.getLocalizedMessage(),
                        "Error loading image", JOptionPane.ERROR_MESSAGE);
            }
        }

        return null;
    }

    private void newTile() {
    	NewTileDialog d = new NewTileDialog(this, tileset);
    	if (d.createTile() != null) queryTiles();
    }

    public void setTileset(TileSet s) {
        tileset = s;

        if (tileset != null) {
            // Find new tile images at the location of the tileset
            if (tileset.getSource() != null) {
                location = tileset.getSource();
            } else if (map != null) {
                location = map.getFilename();
            }
            tilesetNameEntry.setText(tileset.getName());
            //sharedImagesCheck.setSelected(tileset.usesSharedImages());
            externalBitmapCheck.setSelected(tileset.getTilebmpFile() != null);
        }

        queryTiles();
        queryImages();
        updateEnabledState();
    }

    public void queryTiles() {
        Vector listData;

        if (tileset != null && tileset.size() > 0) {
            listData = new Vector();
            Iterator tileIterator = tileset.iterator();

            while (tileIterator.hasNext()) {
                Tile tile = (Tile)tileIterator.next();
                listData.add(tile);
            }

            tileList.setListData(listData);
        }

        if (currentTile != null) {
            tileList.setSelectedIndex(currentTile.getId() - 1);
            tileList.ensureIndexIsVisible(currentTile.getId() - 1);
        }
    }

    public void queryImages() {
        Vector listData = new Vector();

        Enumeration ids = tileset.getImageIds();
        while(ids.hasMoreElements()) {
        	Image img = tileset.getImageById(Integer.parseInt((String) ids.nextElement()));
        	if(img != null)
        		listData.add(img);
        }

        imageList.setListData(listData);
        if (currentImageIndex != -1) {
            imageList.setSelectedIndex(currentImageIndex);
            imageList.ensureIndexIsVisible(currentImageIndex);
        }
    }

    private void setCurrentTile(Tile tile) {
        // Update the old current tile's properties
        // (happens automatically as properties are changed in place now)
        /*
        if (currentTile != null) {
            PropertiesTableModel model =
                (PropertiesTableModel)tileProperties.getModel();
            currentTile.setProperties(model.getProperties());
        }
        */

        currentTile = tile;
        updateTileInfo();
        updateEnabledState();
    }

    private void setImageIndex(int i) {
        currentImageIndex = i;
        updateEnabledState();
    }

    private void updateEnabledState() {
        // boolean internal = (tileset.getSource() == null);
        boolean tilebmp = tileset.getTilebmpFile() != null;
        boolean tileSelected = currentTile != null;
        boolean setImages = tileset.isSetFromImage();
        boolean atLeastOneSharedImage = setImages
          && tileset.getTotalImages() >= 1;

        newTileButton.setEnabled(atLeastOneSharedImage || !tilebmp);
        deleteTileButton.setEnabled((setImages || !tilebmp) && tileSelected);
        changeImageButton.setEnabled((atLeastOneSharedImage || !tilebmp)
            && tileSelected);
        duplicateTileButton.setEnabled((setImages || !tilebmp) && tileSelected);
        animationButton.setEnabled((setImages || !tilebmp) && tileSelected &&
        		currentTile instanceof AnimatedTile);
        tileProperties.setEnabled((setImages || !tilebmp) && tileSelected);
        externalBitmapCheck.setEnabled(tilebmp); // Can't turn this off yet
        //setImagesCheck.setEnabled(!tilebmp || !setImages
        //    || tileset.safeToDisablesetImages());
        tabs.setEnabledAt(2, setImages);
    }

    /**
     * Updates the properties table with the properties of the current tile.
     */
    private void updateTileInfo() {
        if (currentTile == null) {
            return;
        }

        Properties tileProps = currentTile.getProperties();

        // (disabled making a copy, as properties are changed in place now)
        /*
        Properties editProps = new Properties();
        for (Enumeration keys = tileProps.keys(); keys.hasMoreElements();) {
            String key = (String)keys.nextElement();
            editProps.put(key, tileProps.getProperty(key));
        }
        */

        ((PropertiesTableModel)tileProperties.getModel()).update(tileProps);
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();

        if (source == okButton) {
            tileset.setName(tilesetNameEntry.getText());
            dispose();
        } else if (source == deleteTileButton) {
            int answer = JOptionPane.showConfirmDialog(
                    this, "Delete tile?", "Are you sure?",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (answer == JOptionPane.YES_OPTION) {
                Tile tile = (Tile)tileList.getSelectedValue();
                if (tile != null) {
                    tileset.removeTile(tile.getId());
                }
                queryTiles();
            }
        } else if (source == changeImageButton) {
            changeImage();
        } else if (source == newTileButton) {
            newTile();
        } else if (source == duplicateTileButton) {
            Tile n = new Tile(currentTile);
            tileset.addNewTile(n);
            queryTiles();
            // Select the last (cloned) tile
            tileList.setSelectedIndex(tileset.size() - 1);
            tileList.ensureIndexIsVisible(tileset.size() - 1);
        } else if (source == externalBitmapCheck) {
            if (!externalBitmapCheck.isSelected()) {
                int answer = JOptionPane.showConfirmDialog(
                        this,
                        "Warning: this operation cannot currently be reversed.\n" +
                        "Disable the use of an external bitmap?",
                       "Are you sure?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (answer == JOptionPane.YES_OPTION) {
                    tileset.setTilesetImageFilename(null);
                    updateEnabledState();
                } else {
                    externalBitmapCheck.setSelected(true);
                }
            }
        } else if (source == animationButton) {
        	AnimationDialog ad = new AnimationDialog(this, ((AnimatedTile)currentTile).getSprite());
        	ad.setVisible(true);
        }
        /*
        else if (source == setImagesCheck) {
            if (setImagesCheck.isSelected()) {
                tileset.enablesetImages();
                updateEnabledState();
            } else {
                int answer = JOptionPane.YES_OPTION;
                if (!tileset.safeToDisablesetImages()) {
                    answer = JOptionPane.showConfirmDialog(
                        this, "This tileset uses features that require the "
                        + "use of shared images.  Disable the use of shared "
                        + "images?",
                        "Are you sure?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                }
                if (answer == JOptionPane.YES_OPTION) {
                    tileset.disablesetImages();
                    updateEnabledState();
                } else {
                    setImagesCheck.setSelected(true);
                }
            }
        }
        */
        else if (source == createTileButton) {
        	Image img = (Image)imageList.getSelectedValue();
        	Tile n = new Tile(tileset);
        	
        	n.setImage(tileset.getIdByImage(img));
        	tileset.addNewTile(n);
        	queryTiles();
            // Select the last (cloned) tile
            tileList.setSelectedIndex(tileset.size() - 1);
            tileList.ensureIndexIsVisible(tileset.size() - 1);
            JOptionPane.showMessageDialog(this, 
            		"Tile created with id "+n.getId(),
            		"Created Tile",
            		JOptionPane.INFORMATION_MESSAGE);
        }

        repaint();
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == tileList) {
            setCurrentTile((Tile)tileList.getSelectedValue());
        } else if (e.getSource() == imageList) {
            setImageIndex(imageList.getSelectedIndex());
        }
    }
}
