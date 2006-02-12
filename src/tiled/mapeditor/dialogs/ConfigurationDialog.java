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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tiled.mapeditor.widget.IntegerSpinner;
import tiled.mapeditor.widget.VerticalStaticJPanel;
import tiled.util.TiledConfiguration;

/**
 * @version $Id$
 */
public class ConfigurationDialog extends JDialog
{
    private IntegerSpinner undoDepth, gridOpacity;
    private JCheckBox cbBinaryEncode, cbCompressLayerData, cbEmbedImages;
    private JCheckBox cbReportIOWarnings;
    private JRadioButton rbEmbedInTiles, rbEmbedInSet;
    private JCheckBox cbGridAA;
    //private JColorChooser gridColor;

    private final Preferences prefs = TiledConfiguration.root();
    private final Preferences savingPrefs = prefs.node("saving");
    private final Preferences ioPrefs = prefs.node("io");

    public ConfigurationDialog(JFrame parent) {
        super(parent, "Preferences", true);
        init();
        setLocationRelativeTo(parent);
    }

    private void init() {
        // Create primitives

        cbBinaryEncode = new JCheckBox("Use binary encoding");
        cbCompressLayerData = new JCheckBox("Compress layer data (gzip)");
        cbEmbedImages = new JCheckBox("Embed images (png)");
        cbReportIOWarnings = new JCheckBox("Report I/O messages");
        rbEmbedInTiles = new JRadioButton("Embed images in tiles");
        rbEmbedInSet = new JRadioButton("Use Tileset (shared) images");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbEmbedInTiles);
        bg.add(rbEmbedInSet);
        undoDepth = new IntegerSpinner();
        cbGridAA = new JCheckBox("Antialiasing");
        gridOpacity = new IntegerSpinner(0, 0, 255);
        //gridColor = new JColorChooser();

        // Set up the layout

        /* LAYER OPTIONS */
        JPanel layerOps = new VerticalStaticJPanel();
        layerOps.setLayout(new GridBagLayout());
        layerOps.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Layer Options"),
                    BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1; c.gridy = 0; c.weightx = 1;
        layerOps.add(cbBinaryEncode, c);
        c.gridy = 1; c.insets = new Insets(0, 10, 0, 0);
        layerOps.add(cbCompressLayerData, c);

        /* GENERAL OPTIONS */
        JPanel generalOps = new VerticalStaticJPanel();
        generalOps.setLayout(new GridBagLayout());
        generalOps.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.NONE;
        generalOps.add(new JLabel("Undo Depth:  "), c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1; c.weightx = 1;
        generalOps.add(undoDepth, c);
        c.gridy = 1;
        c.gridx = 0;
        generalOps.add(cbReportIOWarnings, c);

        /* TILESET OPTIONS */
        JPanel tilesetOps = new VerticalStaticJPanel();
        tilesetOps.setLayout(new GridBagLayout());
        tilesetOps.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Tileset Options"),
                    BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1; c.gridy = 0; c.weightx = 1;
        tilesetOps.add(cbEmbedImages, c);
        c.gridy = 1; c.insets = new Insets(0, 10, 0, 0);
        tilesetOps.add(rbEmbedInTiles, c);
        c.gridy = 2; c.insets = new Insets(0, 10, 0, 0);
        tilesetOps.add(rbEmbedInSet, c);

        /* GRID OPTIONS */
        JPanel gridOps = new VerticalStaticJPanel();
        gridOps.setLayout(new GridBagLayout());
        gridOps.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        c = new GridBagConstraints();
        gridOps.add(new JLabel("Opacity:  "), c);
        c.weightx = 1; c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridOps.add(gridOpacity, c);
        c.gridwidth = 2; c.gridy = 1; c.gridx = 0;
        gridOps.add(cbGridAA, c);
        //c.gridy = 2; c.weightx = 0;
        //gridOps.add(new JLabel("Color: "), c);
        //c.gridx = 1;
        //gridOps.add(gridColor, c);

        JButton bClose = new JButton("Close");
        bClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                dispose();
            }
        });

        /* BUTTONS PANEL */
        JPanel buttons = new VerticalStaticJPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(Box.createGlue());
        buttons.add(bClose);

        JPanel saving = new JPanel();
        saving.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        saving.setLayout(new BoxLayout(saving, BoxLayout.Y_AXIS));
        saving.add(layerOps);
        saving.add(tilesetOps);

        JPanel general = new JPanel();
        general.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        general.setLayout(new BoxLayout(general, BoxLayout.Y_AXIS));
        general.add(generalOps);

        JPanel grid = new JPanel();
        grid.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        grid.setLayout(new BoxLayout(grid, BoxLayout.Y_AXIS));
        grid.add(gridOps);


        // Put together the tabs

        JTabbedPane perfs = new JTabbedPane();
        perfs.addTab("General", general);
        perfs.addTab("Saving", saving);
        perfs.addTab("Grid", grid);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(perfs);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(buttons);

        getContentPane().add(mainPanel);
        getRootPane().setDefaultButton(bClose);
        pack();

        // Associate listeners with the configuration widgets

        cbBinaryEncode.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                final boolean selected = cbBinaryEncode.isSelected();
                savingPrefs.putBoolean("encodeLayerData", selected);
                cbCompressLayerData.setEnabled(selected);
            }
        });

        cbCompressLayerData.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                savingPrefs.putBoolean("layerCompression",
                        cbCompressLayerData.isSelected());
            }
        });

        cbEmbedImages.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                final boolean embed = cbEmbedImages.isSelected();
                savingPrefs.putBoolean("embedImages", embed);
                rbEmbedInTiles.setSelected(embed && rbEmbedInTiles.isSelected());
                rbEmbedInTiles.setEnabled(embed);
                rbEmbedInSet.setSelected(embed && rbEmbedInSet.isSelected());
                rbEmbedInSet.setEnabled(embed);
            }
        });

        cbReportIOWarnings.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                ioPrefs.putBoolean("reportWarnings",
                        cbReportIOWarnings.isSelected());
            }
        });

        cbGridAA.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                prefs.putBoolean("gridAntialias", cbGridAA.isSelected());
            }
        });

        undoDepth.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                prefs.putInt("undoDepth", undoDepth.intValue());
            }
        });

        gridOpacity.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                prefs.putInt("gridOpacity", gridOpacity.intValue());
            }
        });

        //gridColor.addChangeListener(...);

        rbEmbedInTiles.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                savingPrefs.putBoolean("embedImages",
                        rbEmbedInTiles.isSelected());
            }
        });

        rbEmbedInSet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                savingPrefs.putBoolean("tileSetImages",
                        rbEmbedInSet.isSelected());
            }
        });

        rbEmbedInTiles.setEnabled(false);
        rbEmbedInSet.setEnabled(false);

        //gridColor.setName("tiled.grid.color");
    }

    public void configure() {
        updateFromConfiguration();
        setVisible(true);
    }

    private void updateFromConfiguration() {
        undoDepth.setValue(prefs.getInt("undoDepth", 30));
        gridOpacity.setValue(prefs.getInt("gridOpacity", 255));

        if (savingPrefs.getBoolean("embedImages", true)) {
            cbEmbedImages.setSelected(true);
            rbEmbedInTiles.setSelected(true);
        }

        cbBinaryEncode.setSelected(savingPrefs.getBoolean("encodeLayerData", true));
        cbCompressLayerData.setSelected(savingPrefs.getBoolean("layerCompression", true));
        cbGridAA.setSelected(savingPrefs.getBoolean("gridAntialias", true));
        cbReportIOWarnings.setSelected(ioPrefs.getBoolean("reportWarnings", false));

        cbCompressLayerData.setEnabled(cbBinaryEncode.isSelected());
    }
}
