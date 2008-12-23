/*
 *  Tiled Map Editor, (c) 2004-2006
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <bjorn@lindeijer.nl>
 */

package tiled.mapeditor.dialogs;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.prefs.Preferences;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tiled.io.ImageHelper;
import tiled.mapeditor.widget.IntegerSpinner;
import tiled.mapeditor.widget.VerticalStaticJPanel;
import tiled.mapeditor.Resources;
import tiled.mapeditor.util.ConfirmingFileChooser;
import tiled.mapeditor.util.ConfirmableFileFilter;
import tiled.util.TiledConfiguration;

/**
 * @version $Id$
 */
public class ConfigurationDialog extends JDialog
{
    private IntegerSpinner undoDepth;
    private JSlider gridOpacitySlider;
    private JCheckBox cbBinaryEncode;
    private JCheckBox cbCompressLayerData;
    private JCheckBox cbUsefulComments;
    private JCheckBox cbEmbedImages;
    private JCheckBox cbReportIOWarnings;
    private JCheckBox cbAutoOpenLastFile;
    private JRadioButton rbEmbedInTiles;
    private JRadioButton rbEmbedInSet;
    private JLabel lbImageFormat;
    private JLabel lbPixelFormat;
    private JLabel lbByteOrder;
    private JComboBox coImageFormat;
    private JComboBox coPixelFormat;
    private JComboBox coByteOrder;
    private JCheckBox cbGridAA;
    //private JColorChooser gridColor;

    private static final Preferences prefs = TiledConfiguration.root();
    private static final Preferences savingPrefs = prefs.node("saving");
    private static final Preferences ioPrefs = prefs.node("io");
    private static final Preferences displayPrefs = prefs.node("display");

    private static final String DIALOG_TITLE = Resources.getString("dialog.preferences.title");
    private static final String CLOSE_BUTTON = Resources.getString("general.button.close");
    private static final String OPACITY_LABEL = Resources.getString("dialog.preferences.opacity.label");
    private static final String BINARY_ENCODE_CHECKBOX = Resources.getString("dialog.preferences.binary.encode.checkbox");
    private static final String COMPRESS_LAYER_DATA_CHECKBOX = Resources.getString("dialog.preferences.compress.layer.data.checkbox");
    private static final String USEFUL_COMMENTS_CHECKBOX = Resources.getString("dialog.preferences.useful.comments.checkbox");
    private static final String EMBED_IMAGES_CHECKBOX = Resources.getString("dialog.preferences.embed.images.checkbox");
    private static final String REPORT_IO_WARNINGS_CHECKBOX = Resources.getString("dialog.preferences.report.io.warnings.checkbox");
    private static final String AUTO_OPEN_LAST_FILE_CHECKBOX = Resources.getString("dialog.preferences.report.io.autoopenlast.checkbox");
    private static final String EMBED_IN_TILES_CHECKBOX = Resources.getString("dialog.preferences.embed.in.tiles.checkbox");
    private static final String EMBED_IN_SET_CHECKBOX = Resources.getString("dialog.preferences.embed.in.set.checkbox");
    private static final String IMAGE_FORMAT_LABEL = Resources.getString("dialog.preferences.image.format.combobox");
    private static final String PIXEL_FORMAT_LABEL = Resources.getString("dialog.preferences.pixel.format.combobox");
    private static final String BYTE_ORDER_LABEL = Resources.getString("dialog.preferences.byte.order.combobox");
    
    private static final String ANTIALIASING_CHECKBOX = Resources.getString("dialog.preferences.antialiasing.checkbox");
    private static final String GENERAL_SAVING_OPTIONS_TITLE = Resources.getString("dialog.preferences.general.tab");
    private static final String LAYER_OPTIONS_TITLE = Resources.getString("dialog.preferences.layer.options.title");
    private static final String UNDO_DEPTH_LABEL = Resources.getString("dialog.preferences.undo.depth.label");
    private static final String TILESET_OPTIONS_TITLE = Resources.getString("dialog.preferences.tileset.options.title");
    private static final String GENERAL_TAB = Resources.getString("dialog.preferences.general.tab");
    private static final String SAVING_TAB = Resources.getString("dialog.preferences.saving.tab");
    private static final String GRID_TAB = Resources.getString("dialog.preferences.grid.tab");
    private static final String EXPORT_BUTTON = Resources.getString("dialog.preferences.export.button");
    private static final String IMPORT_BUTTON = Resources.getString("dialog.preferences.import.button");

    private static final ConfirmableFileFilter xmlFileFilter =
            new ConfirmableFileFilter() {
                public String getDefaultExtension() {
                    return "xml";
                }
                public boolean accept(File file) {
                    return file.isDirectory() || file.getPath().endsWith(".xml");
                }
                public String getDescription() {
                    return "XML files (*.xml)";
                }
            };

    public ConfigurationDialog(JFrame parent) {
        super(parent, DIALOG_TITLE, true);
        init();
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void updateUI() {
        cbCompressLayerData.setEnabled(cbBinaryEncode.isSelected());
        
        boolean embed = cbEmbedImages.isSelected();

        coImageFormat.setEnabled(embed);

        boolean rawImageFormat = coImageFormat.getSelectedItem().equals(ImageHelper.ImageFormat.RAW);
        coPixelFormat.setEnabled(embed && rawImageFormat);
        coByteOrder.setEnabled(embed && rawImageFormat);
    }

    private void init() {
        // Create primitives

        cbBinaryEncode = new JCheckBox(BINARY_ENCODE_CHECKBOX);
        cbCompressLayerData = new JCheckBox(COMPRESS_LAYER_DATA_CHECKBOX);
        cbUsefulComments = new JCheckBox(USEFUL_COMMENTS_CHECKBOX);
        cbEmbedImages = new JCheckBox(EMBED_IMAGES_CHECKBOX);
        cbReportIOWarnings = new JCheckBox(REPORT_IO_WARNINGS_CHECKBOX);
        cbAutoOpenLastFile = new JCheckBox(AUTO_OPEN_LAST_FILE_CHECKBOX);
        rbEmbedInTiles = new JRadioButton(EMBED_IN_TILES_CHECKBOX);
        rbEmbedInSet = new JRadioButton(EMBED_IN_SET_CHECKBOX);
        lbImageFormat = new JLabel(IMAGE_FORMAT_LABEL);
        lbPixelFormat = new JLabel(PIXEL_FORMAT_LABEL);
        lbByteOrder = new JLabel(BYTE_ORDER_LABEL);
        coImageFormat = new JComboBox(ImageHelper.ImageFormat.values());
        coPixelFormat = new JComboBox(ImageHelper.PixelFormat.values());
        coByteOrder = new JComboBox(new String[]{"Big Endian", "Little Endian"});
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbEmbedInTiles);
        bg.add(rbEmbedInSet);
        undoDepth = new IntegerSpinner();
        cbGridAA = new JCheckBox(ANTIALIASING_CHECKBOX);
        gridOpacitySlider = new JSlider(0, 255, 255);
        //gridColor = new JColorChooser();

        // Set up the layout

        /* LAYER OPTIONS */
        JPanel layerOps = new VerticalStaticJPanel();
        layerOps.setLayout(new GridBagLayout());
        layerOps.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(LAYER_OPTIONS_TITLE),
                    BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1; c.gridy = 0; c.weightx = 1;
        layerOps.add(cbBinaryEncode, c);
        c.gridy = 2; c.insets = new Insets(0, 10, 0, 0);
        layerOps.add(cbCompressLayerData, c);

        /* GENERAL OPTIONS */
        JPanel generalOps = new VerticalStaticJPanel();
        generalOps.setLayout(new GridBagLayout());
        generalOps.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.NONE;
        generalOps.add(new JLabel(UNDO_DEPTH_LABEL), c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1; c.weightx = 1;
        generalOps.add(undoDepth, c);
        c.gridy = 1;
        c.gridx = 0;
        generalOps.add(cbReportIOWarnings, c);
        c.gridy = 2;
        c.gridx = 0;
        generalOps.add(cbAutoOpenLastFile, c);

        /* GENERAL SAVING OPTIONS */
        JPanel generalSavingOps = new VerticalStaticJPanel();
        generalSavingOps.setLayout(new GridBagLayout());
        generalSavingOps.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(GENERAL_SAVING_OPTIONS_TITLE),
                    BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        c = new GridBagConstraints();
        Insets defaultInsets = c.insets;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1; c.gridy = 0; c.weightx = 1;
        generalSavingOps.add(cbUsefulComments, c);

        /* TILESET OPTIONS */
        JPanel tilesetOps = new VerticalStaticJPanel();
        tilesetOps.setLayout(new GridBagLayout());
        tilesetOps.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(TILESET_OPTIONS_TITLE),
                    BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        c.gridy = 0; c.insets = new Insets(0, 10, 0, 0);
        tilesetOps.add(rbEmbedInTiles, c);
        c.gridy = 1; c.insets = new Insets(0, 10, 0, 0);
        tilesetOps.add(rbEmbedInSet, c);
        c.gridy = 2; c.insets = defaultInsets;
        tilesetOps.add(cbEmbedImages, c);
        c.gridy = 3; c.insets = new Insets(0, 10, 0, 0);
        tilesetOps.add(lbImageFormat, c);
        c.gridy = 4; c.insets = new Insets(0, 10, 0, 0);
        tilesetOps.add(coImageFormat, c);
        c.gridy = 5; c.insets = new Insets(0, 10, 0, 0);
        tilesetOps.add(lbPixelFormat, c);
        c.gridy = 6; c.insets = new Insets(0, 10, 0, 0);
        tilesetOps.add(coPixelFormat, c);
        c.gridy = 7; c.insets = new Insets(0, 10, 0, 0);
        tilesetOps.add(lbByteOrder, c);
        c.gridy = 8; c.insets = new Insets(0, 10, 0, 0);
        tilesetOps.add(coByteOrder, c);

        /* GRID OPTIONS */
        JPanel gridOps = new VerticalStaticJPanel();
        gridOps.setLayout(new GridBagLayout());
        gridOps.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 0, 5);
        gridOps.add(new JLabel(OPACITY_LABEL), c);
        c.insets = new Insets(0, 0, 0, 0);
        c.weightx = 1; c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridOps.add(gridOpacitySlider, c);
        c.gridwidth = 2; c.gridy = 1; c.gridx = 0;
        gridOps.add(cbGridAA, c);
        //c.gridy = 2; c.weightx = 0;
        //gridOps.add(new JLabel("Color: "), c);
        //c.gridx = 1;
        //gridOps.add(gridColor, c);

        JButton exportButton = new JButton(EXPORT_BUTTON);
        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                doExport();
            }
        });

        JButton importButton = new JButton(IMPORT_BUTTON);
        importButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                doImport();
            }
        });

        JButton closeButton = new JButton(CLOSE_BUTTON);
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                dispose();
            }
        });

        /* BUTTONS PANEL */
        JPanel buttons = new VerticalStaticJPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(exportButton);
        buttons.add(Box.createRigidArea(new Dimension(5, 5)));
        buttons.add(importButton);
        buttons.add(Box.createRigidArea(new Dimension(5, 5)));
        buttons.add(Box.createGlue());
        buttons.add(closeButton);

        JPanel saving = new JPanel();
        saving.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        saving.setLayout(new BoxLayout(saving, BoxLayout.Y_AXIS));
        saving.add(generalSavingOps);
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
        perfs.addTab(GENERAL_TAB, general);
        perfs.addTab(SAVING_TAB, saving);
        perfs.addTab(GRID_TAB, grid);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(perfs);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(buttons);

        getContentPane().add(mainPanel);
        getRootPane().setDefaultButton(closeButton);

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

        cbUsefulComments.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                savingPrefs.putBoolean("usefulComments",
                        cbUsefulComments.isSelected());
            }
        });
        
        cbEmbedImages.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                final boolean embed = cbEmbedImages.isSelected();
                savingPrefs.putBoolean("embedImages", embed);
                updateUI();
            }
        });
        
        coImageFormat.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                savingPrefs.put("imageFormat", coImageFormat.getSelectedItem().toString());
                updateUI();
            }
        });
        
        coPixelFormat.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                savingPrefs.put("pixelFormat", coPixelFormat.getSelectedItem().toString());
            }
        });
        
        coByteOrder.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                int index = coByteOrder.getSelectedIndex();
                savingPrefs.putBoolean("imageIsBigEndian", index == 0);
            }
        });
        
        cbReportIOWarnings.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                ioPrefs.putBoolean("reportWarnings",
                        cbReportIOWarnings.isSelected());
            }
        });

        cbAutoOpenLastFile.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                ioPrefs.putBoolean("autoOpenLast",
                        cbAutoOpenLastFile.isSelected());
            }
        });

        cbGridAA.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                displayPrefs.putBoolean("gridAntialias", cbGridAA.isSelected());
            }
        });

        undoDepth.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                prefs.putInt("undoDepth", undoDepth.intValue());
            }
        });

        gridOpacitySlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                displayPrefs.putInt("gridOpacity", gridOpacitySlider.getValue());
            }
        });

        //gridColor.addChangeListener(...);

        rbEmbedInTiles.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                savingPrefs.putBoolean("tileSetImages",
                        !rbEmbedInTiles.isSelected());
            }
        });

        rbEmbedInSet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                savingPrefs.putBoolean("tileSetImages",
                        rbEmbedInSet.isSelected());
            }
        });

//        rbEmbedInTiles.setEnabled(false);
//        rbEmbedInSet.setEnabled(false);

        //gridColor.setName("tiled.grid.color");
    }

    public void configure() {
        updateFromConfiguration();
        setVisible(true);
    }

    private void updateFromConfiguration() {
        undoDepth.setValue(prefs.getInt("undoDepth", 30));
        gridOpacitySlider.setValue(displayPrefs.getInt("gridOpacity", 255));

        boolean embedImages = savingPrefs.getBoolean("embedImages", true);
        cbEmbedImages.setSelected(embedImages);

        if (savingPrefs.getBoolean("tileSetImages", false)) {
            rbEmbedInSet.setSelected(true);
        }
        else {
            rbEmbedInTiles.setSelected(true);
        }
            

        cbUsefulComments.setSelected(savingPrefs.getBoolean("usefulComments", false));
        cbBinaryEncode.setSelected(savingPrefs.getBoolean("encodeLayerData", true));
        cbCompressLayerData.setSelected(savingPrefs.getBoolean("layerCompression", true));
        cbGridAA.setSelected(displayPrefs.getBoolean("gridAntialias", true));
        cbReportIOWarnings.setSelected(ioPrefs.getBoolean("reportWarnings", false));
        cbAutoOpenLastFile.setSelected(ioPrefs.getBoolean("autoOpenLast", false));
        
        coImageFormat.setSelectedItem(ImageHelper.ImageFormat.valueOf(savingPrefs.get("imageFormat", "PNG"), ImageHelper.ImageFormat.PNG));
        coPixelFormat.setSelectedItem(ImageHelper.PixelFormat.valueOf(savingPrefs.get("pixelFormat", "A1R5G5B5"), ImageHelper.PixelFormat.A1R5G5B5));
        boolean imageIsBigEndian = savingPrefs.getBoolean("imageIsBigEndian", true);
        coByteOrder.setSelectedIndex(imageIsBigEndian ? 0 : 1);
        
        updateUI();
    }

    private void doExport() {
        JFileChooser chooser = new ConfirmingFileChooser(null);
        chooser.addChoosableFileFilter(xmlFileFilter);
        int result = chooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION)
        {
            File configFile = chooser.getSelectedFile();

            try {
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(configFile);
                    prefs.exportSubtree(outputStream);
                }
                finally {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            }
            catch (Exception e) {
                System.out.println("Error while exporting configuration:\n" +
                        e.toString());
            }
        }
    }

    private void doImport() {
        JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(xmlFileFilter);
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION)
        {
            File configFile = chooser.getSelectedFile();
            try {
                FileInputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(configFile);
                    Preferences.importPreferences(inputStream);
                }
                finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            } catch (Exception e) {
                System.out.println("Error while importing configuration:\n" +
                        e.toString());
            }
            updateFromConfiguration();
        }
    }
}
