/*
 *  Tiled Map Editor, (c) 2008
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <b.lindeijer@xs4all.nl>
 */

package tiled.mapeditor.actions;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import tiled.core.Map;
import tiled.mapeditor.MapEditor;
import tiled.mapeditor.Resources;
import tiled.view.MapView;

/**
 * Saves the map to an image.
 *
 * @version $Id$
 */
public class SaveAsImageAction extends AbstractAction
{
    private final MapEditor editor;
    private final Frame appFrame;

    private static final String ACTION_NAME = Resources.getString("action.map.saveasimage.name");
    private static final String ACTION_TOOLTIP = Resources.getString("action.map.saveasimage.tooltip");

    public SaveAsImageAction(MapEditor editor) {
        super(ACTION_NAME);

        putValue(SHORT_DESCRIPTION, ACTION_TOOLTIP);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control shift I"));

        this.editor = editor;
        appFrame = editor.getAppFrame();
    }

    public void actionPerformed(ActionEvent event) {
        if (editor.getCurrentMap() == null)
            return;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save as image");

        if (chooser.showSaveDialog(appFrame) ==
                JFileChooser.APPROVE_OPTION) {
            saveMapImage(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    /**
     * Attempts to draw the entire map to an image file of the format of the
     * extension. (filename.ext)
     *
     * @param filename Image filename to save map render to.
     */
    private void saveMapImage(String filename) {
        final Map currentMap = editor.getCurrentMap();
        final MapView myView = MapView.createViewforMap(currentMap);
        myView.setMode(MapView.PF_NOSPECIAL, true);

        // Take grid and zoom level from the current map view
        final MapView mapView = editor.getMapView();
        myView.setShowGrid(mapView.getShowGrid());
        myView.setZoom(mapView.getZoom());

        final Dimension imgSize = myView.getPreferredSize();

        try {
            BufferedImage img = new BufferedImage(
                    imgSize.width, imgSize.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setClip(0, 0, imgSize.width, imgSize.height);
            myView.paint(g);

            int lastDot = filename.lastIndexOf('.');
            String format = filename.substring(lastDot + 1);

            try {
                ImageIO.write(img, format, new File(filename));
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(appFrame,
                        "Error while saving " + filename + ": " + e.toString(),
                        "Error while saving map image",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (OutOfMemoryError memoryError) {
            JOptionPane.showMessageDialog(appFrame,
                    "Out of memory while creating image. Try increasing\n" +
                            "your maximum heap size or zooming out a bit.",
                    "Out of memory",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
