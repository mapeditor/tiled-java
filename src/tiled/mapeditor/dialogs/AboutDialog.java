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

import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.event.MouseInputAdapter;

import tiled.mapeditor.MapEditor;
import tiled.mapeditor.Resources;

/**
 * The about dialog.
 *
 * @version $Id$
 */
public class AboutDialog extends JDialog
{
    private final JFrame parent;

    public AboutDialog(JFrame parent) {
        super(parent, Resources.getString("dialog.main.title") + " v" + MapEditor.version);

        this.parent = parent;

        JLabel label = new JLabel(Resources.getIcon("logo.png"));
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        label.addMouseListener(new MouseInputAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                setVisible(false);
            }
        });

        setContentPane(label);
        setResizable(false);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        pack();
    }

    public void setVisible(boolean visible) {
        if (visible) {
            setLocationRelativeTo(parent);
        }
        super.setVisible(visible);
    }
}
