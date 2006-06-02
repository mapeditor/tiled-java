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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
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
    private JProgressBar memoryBar;

    public AboutDialog(JFrame parent) {
        super(parent, Resources.getString("dialog.main.title") + " v" + MapEditor.version);

        this.parent = parent;

        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new UpdateTimerTask(), 0, 1000);

        setContentPane(createMainPanel());
        setResizable(false);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        addMouseListener(new MouseInputAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                setVisible(false);
            }
        });
        pack();
    }

    private JPanel createMainPanel() {
        JLabel label = new JLabel(Resources.getIcon("logo.png"));
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JSeparator separator = new JSeparator();
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);

        memoryBar = new JProgressBar();
        memoryBar.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
                                               memoryBar.getBorder()));
        memoryBar.setMaximumSize(new Dimension(label.getPreferredSize().width,
                                               Short.MAX_VALUE));
        memoryBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        memoryBar.setStringPainted(true);

        JPanel mainPanel = new JPanel();
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(label);
        mainPanel.add(separator);
        mainPanel.add(memoryBar);
        return mainPanel;
    }

    private void updateMemoryBar() {
        int total = (int) Runtime.getRuntime().totalMemory();
        int used = (int) (total - Runtime.getRuntime().freeMemory());
        memoryBar.setMaximum(total);
        memoryBar.setValue(used);
        memoryBar.setString(used / 1024 + " KB / " + total / 1024 + " KB");
    }

    public void setVisible(boolean visible) {
        if (visible) {
            updateMemoryBar();
            setLocationRelativeTo(parent);
        }
        super.setVisible(visible);
    }

    /**
     * Used for updating the memory bar in intervals.
     */
    private class UpdateTimerTask extends TimerTask
    {
        public void run() {
            if (isVisible()) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        updateMemoryBar();
                    }
                });
            }
        }
    }
}
