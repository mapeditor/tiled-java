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

package tiled.mapeditor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import tiled.core.Map;
import tiled.mapeditor.util.ResizePanel;

public class ResizeDialog extends JDialog implements ActionListener
{
    public static final int ANCHOR_UPPERLEFT  = 1;
    public static final int ANCHOR_UPPERMID   = 2;
    public static final int ANCHOR_UPPERRIGHT = 3;
    public static final int ANCHOR_MIDLEFT    = 4;
    public static final int ANCHOR_MIDMID     = 5;
    public static final int ANCHOR_MIDRIGHT   = 6;
    public static final int ANCHOR_LOWERLEFT  = 7;
    public static final int ANCHOR_LOWERMID   = 8;
    public static final int ANCHOR_LOWERRIGHT = 9;

    private Map currentMap;
    private JSpinner width, height, offsetX, offsetY;
    private int anchor;
    private JButton bOk, bCancel;

    public ResizeDialog(JFrame parent, MapEditor m) {
        super(parent, "Resize Map", true);
        currentMap = m.getCurrentMap();
        init();
        setLocationRelativeTo(getOwner());
    }

    private void init() {
        // Create the primitives

        bOk = new JButton("OK");
        bCancel = new JButton("Cancel");

        width = new JSpinner(new SpinnerNumberModel(currentMap.getWidth(),
                    1, Integer.MAX_VALUE, 1));
        height = new JSpinner(new SpinnerNumberModel(currentMap.getHeight(),
                    1, Integer.MAX_VALUE, 1));
        offsetX = new JSpinner(new SpinnerNumberModel());
        offsetY = new JSpinner(new SpinnerNumberModel());

        // Set a nicer preferred width for these spinners
        width.setPreferredSize(
                new Dimension(60, width.getPreferredSize().height));
        height.setPreferredSize(
                new Dimension(60, height.getPreferredSize().height));
        offsetX.setPreferredSize(
                new Dimension(60, offsetX.getPreferredSize().height));
        offsetX.setPreferredSize(
                new Dimension(60, offsetX.getPreferredSize().height));

        ResizePanel orient = new ResizePanel(new Dimension(100, 100), 100);

        // Offset panel
        JPanel offsetPanel = new VerticalStaticJPanel();
        offsetPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Offset"),
                    BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        offsetPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH; c.weighty = 1;
        c.insets = new Insets(5, 0, 0, 0);
        offsetPanel.add(new JLabel("X: "), c);
        c.gridy = 1;
        offsetPanel.add(new JLabel("Y: "), c);
        c.gridx = 1; c.gridy = 0;
        offsetPanel.add(offsetX, c);
        c.gridy = 1;
        offsetPanel.add(offsetY, c);
        c.gridx = 2; c.gridy = 0; c.gridheight = 2; c.weightx = 1;
        offsetPanel.add(new JPanel(), c);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 3; c.gridheight = 1;
        offsetPanel.add(orient, c);


        // New size panel
        JPanel newSizePanel = new VerticalStaticJPanel(new GridBagLayout());
        newSizePanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("New size"),
                    BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH; c.weighty = 1;
        c.insets = new Insets(5, 0, 0, 0);
        newSizePanel.add(new JLabel("Width: "), c);
        c.gridy = 1;
        newSizePanel.add(new JLabel("Height: "), c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1; c.gridy = 0; c.weightx = 1;
        newSizePanel.add(width, c);
        c.gridy = 1;
        newSizePanel.add(height, c);

        // Original size panel
        JPanel origSizePanel = new VerticalStaticJPanel(new GridBagLayout());
        origSizePanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Current size"),
                    BorderFactory.createEmptyBorder(0, 5, 5, 5)));
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.BOTH; c.weighty = 1; c.weightx = 1;
        c.insets = new Insets(5, 0, 0, 0);
        origSizePanel.add(new JLabel("Width: "), c);
        c.gridy = 1;
        origSizePanel.add(new JLabel("Height: "), c);
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(5, 10, 0, 0);
        c.gridx = 1; c.gridy = 0;
        origSizePanel.add(new JLabel("" + currentMap.getWidth()), c);
        c.gridy = 1;
        origSizePanel.add(new JLabel("" + currentMap.getHeight()), c);

        // Putting two size panels next to eachoter
        JPanel sizePanels = new VerticalStaticJPanel(new GridBagLayout());
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0; c.gridy = 0; c.weighty = 1; c.weightx = 0;
        sizePanels.add(origSizePanel, c);
        c.gridx = 1; c.weightx = 1;
        sizePanels.add(newSizePanel, c);

        // Buttons panel
        bOk.addActionListener(this);
        bCancel.addActionListener(this);
        JPanel buttons = new VerticalStaticJPanel();
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(Box.createGlue());
        buttons.add(bOk);
        buttons.add(Box.createRigidArea(new Dimension(5, 0)));
        buttons.add(bCancel);

        // Putting the main panel together
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(sizePanels);
        mainPanel.add(offsetPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(Box.createGlue());
        mainPanel.add(buttons);

        getContentPane().add(mainPanel);
        getRootPane().setDefaultButton(bOk);
        pack();
    }

    public void showDialog() {
        show();
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == bOk) {
            int nwidth = ((Number)width.getValue()).intValue();
            int nheight = ((Number)height.getValue()).intValue();
            // Math works out in MapLayer#resize
            int dx = ((Number)offsetX.getValue()).intValue();
            int dy = ((Number)offsetY.getValue()).intValue();
            currentMap.resize(nwidth, nheight, dx, dy);
            dispose();
        } else if (src == bCancel) {
            dispose();
        } else {
            System.out.println(e.getActionCommand());
        }
    }
}
