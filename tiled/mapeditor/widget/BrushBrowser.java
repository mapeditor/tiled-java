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

import java.awt.*;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JPanel;

import tiled.mapeditor.brush.Brush;
import tiled.mapeditor.brush.ShapeBrush;


public class BrushBrowser extends JPanel
{
    private int maxWidth = 25;
    private Brush selectedBrush;
    private LinkedList brushes;

    public BrushBrowser() {
        super();
        brushes = new LinkedList();
        init();
    }

    public Dimension getPreferredSize() {
        return new Dimension(maxWidth * 5, 150);
    }

    private void init() {
        int iters = 7;
        int n = 0, n1 = 1;

        // Use Fibonacci to create a sampling of brushes...
        for (int i = 0; i < iters; i++) {
            int dim = n + n1;
            int hold = n1;
            n1 = dim;
            n = hold;
            ShapeBrush b = new ShapeBrush();
            b.makeCircleBrush(dim / 2);
            brushes.add(b);
        }

        n = 0; n1 = 1;
        for(int i = 0; i < iters; i++) {
            int dim = n + n1;
            int hold = n1;
            n1 = dim;
            n = hold;
            ShapeBrush b = new ShapeBrush();
            b.makeQuadBrush(new Rectangle(0, 0, dim, dim));
            brushes.add(b);
        }
    }

    public void setSpacing(int w) {
        maxWidth = w;
    }

    public void paint(Graphics g) {
        Rectangle clipRect = g.getClipBounds();
        int x = 0, y = 0;

        g.setColor(Color.white);
        g.fillRect(clipRect.x, clipRect.y, clipRect.width, clipRect.height);

        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                         RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.black);

        if (brushes.indexOf(selectedBrush) > 0) {
            int index = brushes.indexOf(selectedBrush);
            int brushesInRow = getPreferredSize().width / maxWidth;
            int bx = index % brushesInRow;
            int by = index / brushesInRow;
            g.drawRect(bx * maxWidth, by * maxWidth, maxWidth, maxWidth);
        }

        // DRAW THE STANDARD SIZES
        Iterator itr = brushes.iterator();
        while (itr.hasNext()) {
            Brush b = (Brush) itr.next();
            Rectangle bb = b.getBounds();
            b.paint(g,
                    x + ((maxWidth / 2) - bb.width / 2),
                    y + ((maxWidth / 2) - bb.width / 2));
            x += maxWidth;
            if (x + maxWidth > clipRect.width) {
                x = 0;
                y += maxWidth;
            }
        }
    }

    public void setSelectedBrush(Brush b) {
        Iterator itr = brushes.iterator();
        while (itr.hasNext()) {
            Brush br = (Brush) itr.next();
            if (br.equals(b)) {
                selectedBrush = br;
                break;
            }
        }
    }

    public Brush getSelectedBrush() {
        return selectedBrush;
    }

    public void findSelected(int x, int y) {
        int perLine = this.getPreferredSize().width / maxWidth;
        int selectedIndex = (y / maxWidth) * perLine + x / maxWidth;

        try {
            selectedBrush = (Brush)brushes.get(selectedIndex);
            this.firePropertyChange("selectedbrush", null, null);
            repaint();
        } catch (IndexOutOfBoundsException e) {}
    }
}
