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

package tiled.mapeditor.widget;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * A floatable panel. The panel has a titlebar which displays the panel title
 * plus a small button which can be used to turn the panel into a frame. When
 * the frame is closed the panel is restored.
 *
 * // todo: Clean up this ugly class
 * // todo: Prettify the user interface
 * // todo: Remember floating panel locations in the preferences
 *
 * @version $Id$
 */
public class FloatablePanel extends JPanel
{
    private final JLabel titleLabel;
    private JDialog frame;
    private final JComponent child;
    private final Frame parent;
    private Dimension lastFrameSize;
    private Point lastFrameLocation;
    //private boolean floating;

    /**
     * Constructs a floatable panel with the given title. When the panel is
     * floated, it is placed in a {@link JDialog} with <code>parent</code> as
     * its parent.
     *
     * @param parent the parent of the child
     * @param child the child component
     * @param title the title of this panel
     */
    public FloatablePanel(Frame parent, JComponent child, String title) {
        this.child = child;
        this.parent = parent;
        titleLabel = new JLabel(title);

        final JButton floatButton = new JButton("Float");
        floatButton.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // Remember the current divider location
                final JSplitPane splitPane =
                        (JSplitPane) FloatablePanel.this.getParent();
                final int dividerLocation = splitPane.getDividerLocation();
                final int dividerSize = splitPane.getDividerSize();

                // Hide this panel and remove our child panel
                setVisible(false);
                remove(FloatablePanel.this.child);
                splitPane.setDividerSize(0);

                FloatablePanel.this.child.setBorder(new EmptyBorder(5, 5, 5, 5));

                // Float the child
                frame = new JDialog(FloatablePanel.this.parent, titleLabel.getText());
                frame.getContentPane().add(FloatablePanel.this.child);
                frame.addWindowListener(new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        // Remember the size and position
                        lastFrameSize = frame.getSize();
                        lastFrameLocation = frame.getLocation();

                        // De-float the child
                        frame.getContentPane().remove(FloatablePanel.this.child);
                        frame.dispose();
                        FloatablePanel.this.child.setBorder(null);
                        add(FloatablePanel.this.child, BorderLayout.CENTER);
                        setVisible(true);

                        // Restore the old divider location
                        splitPane.setDividerSize(dividerSize);
                        splitPane.setDividerLocation(dividerLocation);
                    }
                });

                if (lastFrameSize != null)
                {
                    frame.setSize(lastFrameSize);
                    frame.setLocation(lastFrameLocation);
                }
                else
                {
                    frame.pack();
                    frame.setLocationRelativeTo(FloatablePanel.this.parent);
                }
                frame.setVisible(true);
            }
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(floatButton, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(child, BorderLayout.CENTER);
    }

    /**
     * Sets a new title for this panel.
     * @param title the new title
     */
    public void setTitle(String title) {
        // todo: adapt title of frame when it exists
        titleLabel.setText(title);
    }
}
