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
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import tiled.util.TiledConfiguration;


public class ConfigurationDialog extends JDialog implements ActionListener,
       ChangeListener
{
    private JButton bOk, bApply, bCancel;
    private JPanel layerOps, generalOps, tilesetOps;
    private IntegerSpinner undoDepth;
    private JCheckBox cbBinaryEncode, cbCompressLayerData, cbEmbedImages;

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
        undoDepth = new IntegerSpinner();
        cbBinaryEncode.addChangeListener(this);
        cbCompressLayerData.addChangeListener(this);
        cbEmbedImages.addChangeListener(this);
        undoDepth.addChangeListener(this);

        cbBinaryEncode.setActionCommand("tmx.save.encodeLayerData");
        cbCompressLayerData.setActionCommand("tmx.save.layerCompression");
        cbEmbedImages.setActionCommand("tmx.save.embedImages");
        undoDepth.setName("tmx.undo.depth");

        bOk = new JButton("OK");
        bApply = new JButton("Apply");
        bCancel = new JButton("Cancel");
        bOk.addActionListener(this);
        bApply.addActionListener(this);
        bCancel.addActionListener(this);
		bApply.setEnabled(false);

        /* LAYER OPTIONS */
        layerOps = new VerticalStaticJPanel();
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
        generalOps = new VerticalStaticJPanel();
        generalOps.setLayout(new GridBagLayout());
        generalOps.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.NONE;
        generalOps.add(new JLabel("Undo Depth: "), c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1; c.weightx = 1;
        generalOps.add(undoDepth, c);

        /* TILESET OPTIONS */
        tilesetOps = new VerticalStaticJPanel();
        tilesetOps.setLayout(new GridBagLayout());
        tilesetOps.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Tileset Options"),
                    BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1; c.gridy = 0; c.weightx = 1;
        tilesetOps.add(cbEmbedImages, c);

        /* BUTTONS PANEL */
        JPanel buttons = new VerticalStaticJPanel();
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(Box.createGlue());
        buttons.add(bOk);        
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(bApply);
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(bCancel);


        JPanel saving = new JPanel();
        saving.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        saving.setLayout(new BoxLayout(saving, BoxLayout.Y_AXIS));
        saving.add(layerOps);
        saving.add(tilesetOps);

        JPanel general = new JPanel();
        general.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        general.setLayout(new BoxLayout(general, BoxLayout.Y_AXIS));
        general.add(generalOps);


        // Put together the tabs

        JTabbedPane perfs = new JTabbedPane();
        perfs.addTab("General", general);
        perfs.addTab("Saving", saving);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(perfs);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(buttons);

        getContentPane().add(mainPanel);
        getRootPane().setDefaultButton(bOk);
        pack();
    }

    public void configure() {
        updateFromConf();
        show();
    }

    private void updateFromConf() {
        undoDepth.setValue(Integer.parseInt(
                    TiledConfiguration.getValue(undoDepth.getName())));

        // Handle checkboxes
        for (int i = 0; i < layerOps.getComponentCount(); i++) {
            try {
                AbstractButton b = (AbstractButton)layerOps.getComponent(i);
                if (b.getClass().equals(JCheckBox.class)) {
                    if (TiledConfiguration.keyHasValue(b.getActionCommand(), "1")) {
                        b.setSelected(true);
                    }
                }
            } catch (ClassCastException e) {
            }
        }
        for (int i = 0; i < tilesetOps.getComponentCount(); i++) {
            try {
                AbstractButton b = (AbstractButton)tilesetOps.getComponent(i);
                if (b.getClass().equals(JCheckBox.class)) {
                    if (TiledConfiguration.keyHasValue(b.getActionCommand(), "1")) {
                        b.setSelected(true);
                    }
                }
            } catch (ClassCastException e) {
            }
        }

        cbCompressLayerData.setEnabled(cbBinaryEncode.isSelected());

        bApply.setEnabled(false);
    }

    private void processOptions() {
        TiledConfiguration.addConfigPair(
                undoDepth.getName(), "" + undoDepth.intValue());

        // Handle checkboxes
        for (int i = 0; i < layerOps.getComponentCount(); i++) {
            try {
                AbstractButton b = (AbstractButton)layerOps.getComponent(i);
                if (b.getClass().equals(JCheckBox.class)) {
                    TiledConfiguration.addConfigPair(
                            b.getActionCommand(), b.isSelected() ? "1" : "0");
                }
            } catch (ClassCastException e) {
            }
        }
        for (int i = 0; i < tilesetOps.getComponentCount(); i++) {
            try {
                AbstractButton b = (AbstractButton)tilesetOps.getComponent(i);
                if (b.getClass().equals(JCheckBox.class)) {
                    TiledConfiguration.addConfigPair(
                            b.getActionCommand(),b.isSelected() ? "1" : "0");
                }
            } catch (ClassCastException e) {
            }
        }

        bApply.setEnabled(false);
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();

        if (source == bOk) {
            processOptions();
            dispose();
        } else if (source == bCancel) {
            dispose();
        } else if (source == bApply) {
            processOptions();
        }
    }

    public void stateChanged(ChangeEvent event) {
        Object source = event.getSource();

        if (source == cbBinaryEncode) {
            cbCompressLayerData.setEnabled(cbBinaryEncode.isSelected());
        }
        bApply.setEnabled(true);
    }
}
