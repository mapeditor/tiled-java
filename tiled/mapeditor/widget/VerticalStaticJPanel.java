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

package tiled.mapeditor.widget;

import java.awt.Dimension;
import java.awt.LayoutManager;
import javax.swing.JPanel;


public class VerticalStaticJPanel extends JPanel
{
    public VerticalStaticJPanel() {
    }

    public VerticalStaticJPanel(LayoutManager manager) {
        super(manager);
    }

    public Dimension getMaximumSize() {
        return new Dimension(super.getMaximumSize().width,
                getPreferredSize().height);
    }

    public Dimension getMinimumSize() {
        return new Dimension(super.getMinimumSize().width,
                getPreferredSize().height);
    }
}
