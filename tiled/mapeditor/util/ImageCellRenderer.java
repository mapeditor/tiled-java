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

package tiled.mapeditor.util;

import java.awt.Component;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.DefaultListCellRenderer;


public class ImageCellRenderer extends DefaultListCellRenderer
{
    public ImageCellRenderer() {
        setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list, Object value,
            int index,  boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);

        Image img = (Image)value;

        setIcon(new ImageIcon(img));
        setText(null);

        return this;
    }
}
