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
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;

import tiled.core.*;
import tiled.mapeditor.util.TransparentImageFilter;
import tiled.mapeditor.widget.IntegerSpinner;
import tiled.mapeditor.widget.ColorButton;
import tiled.mapeditor.widget.VerticalStaticJPanel;
import tiled.mapeditor.Resources;

/**
 * A dialog for creating a new tileset.
 *
 * @version $Id$
 */
public class NewTilesetDialog extends JDialog implements ChangeListener
{
    private final Map map;
    private TileSet newTileset;
    private IntegerSpinner tileWidth, tileHeight;
    private IntegerSpinner tileSpacing;
    private JTextField tilesetName;
    private JTextField tilebmpFile;
    private JLabel spacingLabel;
    private JLabel tilebmpFileLabel;
    private JCheckBox tilebmpCheck, tileAutoCheck, transCheck;
    private JButton browseButton;
    private ColorButton colorButton;
    private String path;

    private static final String DIALOG_TITLE = Resources.getString("dialog.newtileset.title");
    private static final String NAME_LABEL = Resources.getString("dialog.newtileset.name.label");
    private static final String TILE_WIDTH_LABEL = Resources.getString("dialog.newtileset.tilewidth.label");
    private static final String TILE_HEIGHT_LABEL = Resources.getString("dialog.newtileset.tileheight.label");
    private static final String TILE_SPACING_LABEL = Resources.getString("dialog.newtileset.tilespacing.label");
    private static final String IMAGE_LABEL = Resources.getString("dialog.newtileset.image.label");
    private static final String UNTITLED_FILE = Resources.getString("general.file.untitled");
    private static final String TILESET_IMG_LABEL = Resources.getString("dialog.newtileset.tilesetimgref.label");
    private static final String AUTO_TILES_LABEL = Resources.getString("dialog.newtileset.autotiles.label");
    private static final String USE_TRANS_COLOR_LABEL = Resources.getString("dialog.newtileset.usetransparentcolor.label");
    private static final String OK_BUTTON = Resources.getString("general.button.ok");
    private static final String CANCEL_BUTTON = Resources.getString("general.button.cancel");
    private static final String BROWSE_BUTTON = Resources.getString("general.button.browse");
    private static final String FROM_TILESET_IMG_TITLE = Resources.getString("dialog.newtileset.fromtilesetimg.title");
    private static final String IMPORT_ERROR_MSG = Resources.getString("dialog.newtileset.import.error.message");
    private static final String IMG_LOAD_ERROR = Resources.getString("dialog.newtileset.imgload.error.message");
    private static final String COLOR_CHOOSE_ERROR_TITLE = Resources.getString("dialog.newtileset.colorchoose.error.title");

    public NewTilesetDialog(JFrame parent, Map map) {
        super(parent, DIALOG_TITLE, true);
        this.map = map;
        path = map.getFilename();
        init();
        pack();
        setLocationRelativeTo(parent);
    }

    private void init() {
        // Create the primitives

        JLabel nameLabel = new JLabel(NAME_LABEL);
        JLabel tileWidthLabel = new JLabel(TILE_WIDTH_LABEL);
        JLabel tileHeightLabel = new JLabel(TILE_HEIGHT_LABEL);
        spacingLabel = new JLabel(TILE_SPACING_LABEL);
        tilebmpFileLabel = new JLabel(IMAGE_LABEL);

        tilesetName = new JTextField(UNTITLED_FILE);
        tileWidth = new IntegerSpinner(map.getTileWidth(), 1, 1024);
        tileHeight = new IntegerSpinner(map.getTileHeight(), 1, 1024);
        tileSpacing = new IntegerSpinner(0, 0);
        tilebmpFile = new JTextField(10);
        tilebmpFile.setEnabled(false);

        nameLabel.setLabelFor(tilesetName);
        tileWidthLabel.setLabelFor(tileWidth);
        tileHeightLabel.setLabelFor(tileHeight);
        spacingLabel.setLabelFor(tileSpacing);
        tilebmpFileLabel.setLabelFor(tilebmpFile);

        tileWidthLabel.setEnabled(false);
        tileWidth.setEnabled(false);

        tilebmpCheck = new JCheckBox(TILESET_IMG_LABEL, false);
        tilebmpCheck.addChangeListener(this);

        tileAutoCheck = new JCheckBox(AUTO_TILES_LABEL, true);
        tileAutoCheck.setEnabled(false);

        transCheck = new JCheckBox(USE_TRANS_COLOR_LABEL);
        transCheck.addChangeListener(this);

        JButton okButton = new JButton(OK_BUTTON);
        JButton cancelButton = new JButton(CANCEL_BUTTON);
        browseButton = new JButton(BROWSE_BUTTON);
        colorButton = new ColorButton(new Color(255, 0, 255));

        // Combine browse button and tile bitmap path text field

        JPanel tilebmpPathPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        tilebmpPathPanel.add(tilebmpFile, c);
        c.gridx = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 5, 0, 0);
        tilebmpPathPanel.add(browseButton, c);

        // Combine transparent color label and button

        JPanel tileColorPanel = new JPanel(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        tileColorPanel.add(transCheck, c);
        c.gridx = 1;
        tileColorPanel.add(colorButton);

        // Create the tile bitmap import setting panel

        JPanel tilebmpPanel = new VerticalStaticJPanel();
        tilebmpPanel.setLayout(new GridBagLayout());
        tilebmpPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(FROM_TILESET_IMG_TITLE),
                BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.insets = new Insets(5, 0, 0, 0);
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        tilebmpPanel.add(tilebmpCheck, c);
        c.gridy = 1;
        tilebmpPanel.add(tileAutoCheck, c);
        c.gridy = 2;
        c.gridwidth = 1;
        c.insets = new Insets(5, 0, 0, 5);
        c.fill = GridBagConstraints.NONE;
        tilebmpPanel.add(tilebmpFileLabel, c);
        c.gridy = 3;
        tilebmpPanel.add(spacingLabel, c);
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 1;
        c.insets = new Insets(5, 0, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        tilebmpPanel.add(tilebmpPathPanel, c);
        c.gridy = 3;
        tilebmpPanel.add(tileSpacing, c);
        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        tilebmpPanel.add(tileColorPanel, c);
        c.gridx = 1;
        c.gridwidth = 1;

        // OK and Cancel buttons

        JPanel buttons = new VerticalStaticJPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(Box.createGlue());
        buttons.add(okButton);
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(cancelButton);

        // Top part of form

        JPanel miscPropPanel = new VerticalStaticJPanel();
        miscPropPanel.setLayout(new GridBagLayout());
        miscPropPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(5, 0, 0, 5);
        miscPropPanel.add(nameLabel, c);
        c.gridy = 1;
        miscPropPanel.add(tileWidthLabel, c);
        c.gridy = 2;
        miscPropPanel.add(tileHeightLabel, c);
        c.insets = new Insets(5, 0, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
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

        // Attach the behaviour

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                createSetAndDispose();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                dispose();
            }
        });

        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser ch = new JFileChooser(path);

                int ret = ch.showOpenDialog(NewTilesetDialog.this);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    path = ch.getSelectedFile().getAbsolutePath();
                    tilebmpFile.setText(path);
                }
            }
        });

        colorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                chooseColorFromImage();
            }
        });
    }

    public TileSet create() {
        setVisible(true);
        return newTileset;
    }

    private void createSetAndDispose() {
        newTileset = new TileSet();
        newTileset.setName(tilesetName.getText());

        if (tilebmpCheck.isSelected()) {
            String file = tilebmpFile.getText();
            int spacing = tileSpacing.intValue();
            int width = tileWidth.intValue();
            int height = tileHeight.intValue();
            try {
                if (!transCheck.isSelected()) {
                    newTileset.importTileBitmap(file, width, height, spacing,
                            tileAutoCheck.isSelected());
                } else {
                    try {
                        Toolkit tk = Toolkit.getDefaultToolkit();
                        Image orig = ImageIO.read(new File(file));
                        Image trans = tk.createImage(
                                new FilteredImageSource(orig.getSource(),
                                        new TransparentImageFilter(
                                                colorButton.getColor().getRGB())));
                        BufferedImage img = new BufferedImage(
                                trans.getWidth(null),
                                trans.getHeight(null),
                                BufferedImage.TYPE_INT_ARGB);

                        img.getGraphics().drawImage(trans, 0, 0, null);

                        newTileset.importTileBitmap(img,
                                width, height, spacing,
                                tileAutoCheck.isSelected());

                        newTileset.setTransparentColor(
                                colorButton.getColor());

                        newTileset.setTilesetImageFilename(file);
                    } catch (IOException e) {
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage(),
                        IMPORT_ERROR_MSG, JOptionPane.ERROR_MESSAGE);
                newTileset = null;
            }
        }

        dispose();
    }

    private void chooseColorFromImage() {
        ImageColorDialog icd;
        try {
            icd = new ImageColorDialog(
                    ImageIO.read(new File(tilebmpFile.getText())));
            Color c = icd.showDialog();
            if (c != null) {
                colorButton.setColor(c);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(getOwner(),
                    IMG_LOAD_ERROR + e.getMessage(),
                    COLOR_CHOOSE_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
        }
    }

    public void stateChanged(ChangeEvent event) {
        Object source = event.getSource();

        if (source == tilebmpCheck) {
            setUseTileBitmap(tilebmpCheck.isSelected());
        }
        else if (source == transCheck) {
            colorButton.setEnabled(tilebmpCheck.isSelected() &&
                    transCheck.isSelected());
        }
    }

    private void setUseTileBitmap(boolean value) {
        tilebmpFile.setEnabled(value);
        tilebmpFileLabel.setEnabled(value);
        browseButton.setEnabled(value);
        tileSpacing.setEnabled(value);
        spacingLabel.setEnabled(value);
        tileAutoCheck.setEnabled(value);
        transCheck.setEnabled(value);
        colorButton.setEnabled(value && transCheck.isSelected());
    }
}
