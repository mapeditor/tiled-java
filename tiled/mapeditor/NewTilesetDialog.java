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
import javax.swing.*;
import javax.swing.event.*;

import tiled.core.*;


public class NewTilesetDialog extends JDialog implements ActionListener,
       ChangeListener
{
    private Map map;
    private TileSet newTileset;
    private JTextField tileWidth, tileHeight, tileSpacing;
    private JTextField tilesetName;
    private JTextField tilebmpFile;
    private JLabel nameLabel, tileWidthLabel, tileHeightLabel, spacingLabel;
    private JLabel tilebmpFileLabel;
    private JCheckBox tilebmpCheck;
    private JRadioButton importRadio;
    private JRadioButton referenceRadio;
    private JButton okButton, cancelButton, browseButton;

    public NewTilesetDialog(JFrame parent, Map map) {
        super(parent, "New tileset");
        this.map = map;
        init();
        pack();
        setLocationRelativeTo(parent);
        setModal(true);
    }

    private void init() {
        // Create the primitives

        nameLabel = new JLabel("Tileset name: ");
        tileWidthLabel = new JLabel("Tile width: ");
        tileHeightLabel = new JLabel("Tile height: ");
        spacingLabel = new JLabel("Tile spacing: ");
        tilebmpFileLabel = new JLabel("Tile bitmap: ");

        tilesetName = new JTextField("Untitled");
        tileWidth = new JTextField("" + map.getTileWidth(), 3);
        tileHeight = new JTextField("" + map.getTileHeight(), 3);
        tileSpacing = new JTextField("0", 3);
        tilebmpFile = new JTextField(10);
        tilebmpFile.setEnabled(false);

        tileWidthLabel.setEnabled(false);
        tileHeightLabel.setEnabled(false);
        tileWidth.setEnabled(false);
        tileHeight.setEnabled(false);

        tilebmpCheck = new JCheckBox("Reference tile bitmap", false);
        tilebmpCheck.addChangeListener(this);

        /*
        importRadio = new JRadioButton("Import tile bitmap");
        referenceRadio = new JRadioButton("Reference tile bitmap");

        ButtonGroup tilebmpRadios = new ButtonGroup();
        tilebmpRadios.add(referenceRadio);
        tilebmpRadios.add(importRadio);
        tilebmpRadios.setSelected(referenceRadio.getModel(), true);
        */

        okButton = new JButton("OK");
        cancelButton = new JButton("Cancel");
        browseButton = new JButton("Browse...");
        okButton.addActionListener(this);
        cancelButton.addActionListener(this);
        browseButton.addActionListener(this);


        // Combine browse button and tile bitmap path text field

        JPanel tilebmpPathPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        tilebmpPathPanel.add(tilebmpFile, c);
        c.gridx = 1; c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 5, 0, 0);
        tilebmpPathPanel.add(browseButton, c);
        

        // Create the tile bitmap import setting panel

        JPanel tilebmpPanel = new VerticalStaticJPanel();
        tilebmpPanel.setLayout(new GridBagLayout());
        tilebmpPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("From tile bitmap"),
                    BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        c.insets = new Insets(5, 0, 0, 0);
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        tilebmpPanel.add(tilebmpCheck, c);
        c.gridy = 1; c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        tilebmpPanel.add(tilebmpFileLabel, c);
        c.gridy = 2;
        tilebmpPanel.add(spacingLabel, c);
        c.gridx = 1; c.gridy = 1; c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        tilebmpPanel.add(tilebmpPathPanel, c);
        c.gridy = 2;
        tilebmpPanel.add(tileSpacing, c);
        /*
        c.gridy = 3; c.gridx = 0; c.gridwidth = 2;
        c.weightx = 0;
        tilebmpPanel.add(referenceRadio, c);
        c.gridy = 4;
        c.insets = new Insets(0, 0, 0, 0);
        tilebmpPanel.add(importRadio, c);
        */
        c.gridwidth = 1;


        // OK and Cancel buttons

        JPanel buttons = new VerticalStaticJPanel();
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(Box.createGlue());
        buttons.add(cancelButton);
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(okButton);


        // Top part of form

        JPanel miscPropPanel = new VerticalStaticJPanel();
        miscPropPanel.setLayout(new GridBagLayout());
        miscPropPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(5, 0, 0, 0);
        miscPropPanel.add(nameLabel, c);
        c.gridy = 1;
        miscPropPanel.add(tileWidthLabel, c);
        c.gridy = 2;
        miscPropPanel.add(tileHeightLabel, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1; c.gridy = 0; c.weightx = 1;
        miscPropPanel.add(tilesetName, c);
        c.gridy = 1;
        miscPropPanel.add(tileWidth, c);
        c.gridy = 2;
        miscPropPanel.add(tileHeight, c);

        // Main panel

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(miscPropPanel);
        mainPanel.add(tilebmpPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(Box.createGlue());
        mainPanel.add(buttons);

        getContentPane().add(mainPanel);
        getRootPane().setDefaultButton(okButton);

        setUseTileBitmap(tilebmpCheck.isSelected());
    }

    public TileSet create() {
        show();
        return newTileset;
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();

        if (source == okButton) {
            newTileset = new TileSet();
            newTileset.setName(tilesetName.getText());

            if (tilebmpCheck.isSelected()) {
                String file = tilebmpFile.getText();
                int spacing = Integer.parseInt(tileSpacing.getText());
                newTileset.importTileBitmap(file,
                        map.getTileWidth(), map.getTileHeight(), spacing);
            }

            dispose();
        } if (source == browseButton) {
            JFileChooser ch = new JFileChooser();

            int ret = ch.showOpenDialog(this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                tilebmpFile.setText(ch.getSelectedFile().getAbsolutePath());
            }
        } else {
            dispose();
        }
    }

    public void stateChanged(ChangeEvent event) {
        Object source = event.getSource();

        if (source == tilebmpCheck) {
            setUseTileBitmap(tilebmpCheck.isSelected());
        }
    }

    private void setUseTileBitmap(boolean value) {
        tilebmpFile.setEnabled(value);
        tilebmpFileLabel.setEnabled(value);
        browseButton.setEnabled(value);
        tileSpacing.setEnabled(value);
        spacingLabel.setEnabled(value);
    }
}
