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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tiled.core.Tile;
import tiled.mapeditor.brush.*;
import tiled.mapeditor.widget.BrushBrowser;
import tiled.mapeditor.widget.IntegerSpinner;


public class BrushDialog extends JFrame implements ActionListener,
       ChangeListener, MouseListener, PropertyChangeListener
{
    private JFrame parent;
    private AbstractBrush myBrush;
    private MapEditor me;

    private JCheckBox cbRandomBrush;
    private IntegerSpinner affectLayers, brushSize;
    private JSlider sRandomAmount;
    private JButton bApply;
    private BrushBrowser brushes;

    public BrushDialog(MapEditor me, JFrame parent,
            AbstractBrush currentBrush)
    {
        super("Brush Options");
        this.parent = parent;
        myBrush = currentBrush;
        this.me = me;

        init();
        update();
        pack();
    }

    private JPanel createShapePanel() {
        JPanel shapePanel = new JPanel();
        JPanel opts = new JPanel();
        JLabel size = new JLabel("Brush size: ");

        brushSize = new IntegerSpinner();
        if (myBrush != null)
            brushSize.setValue(myBrush.getBounds().width);
        brushSize.addChangeListener(this);
        brushSize.setToolTipText("Sets the size of the brush in tiles");
        brushes = new BrushBrowser();
        brushes.addMouseListener(this);
        JScrollPane brushScrollPane = new JScrollPane(brushes);
        brushScrollPane.setPreferredSize(new Dimension(150, 150));
        brushes.addPropertyChangeListener(this);

        brushes.setSelectedBrush(myBrush);
        shapePanel.add(brushScrollPane);

        opts.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1; c.weighty = 1;
        opts.add(size, c);
        c.gridx=1;
        opts.add(brushSize,c);

        shapePanel.add(opts);

        return shapePanel;
    }

    private JPanel createOptionsPanel() {
        JPanel optionsPanel = new JPanel();
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        cbRandomBrush = new JCheckBox("Random");
        cbRandomBrush.setToolTipText(
                "Make brush paint randomly within the shape area");
        cbRandomBrush.addChangeListener(this);
        cbRandomBrush.addActionListener(this);
        sRandomAmount = new JSlider();
        sRandomAmount.setToolTipText(
                "The amount of area to fill with randomness");
        sRandomAmount.addChangeListener(this);
        JLabel affected = new JLabel("Affected layers");
        affectLayers = new IntegerSpinner();
        affectLayers.setValue(myBrush.getAffectedLayers());
        affectLayers.addChangeListener(this);

        optionsPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1; c.weighty = 1;
        optionsPanel.add(affected,c);
        c.gridx = 1;
        optionsPanel.add(affectLayers,c);
        c.gridx = 0; c.gridy = 1;
        optionsPanel.add(cbRandomBrush,c);
        c.gridx = 1;
        optionsPanel.add(sRandomAmount,c);

        return optionsPanel;
    }

    private JPanel createCustomPanel() {
        JPanel customPanel = new JPanel();
        
        
        
        return customPanel;
    }

    private void init() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.add("Shape", createShapePanel());
        tabs.add("Options", createOptionsPanel());
        tabs.add("Custom", createCustomPanel());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(tabs);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        bApply = new JButton("Apply");
        bApply.addActionListener(this);
        bApply.setEnabled(false);
        buttonPanel.add(bApply);
        JButton bClose = new JButton("Close");
        bClose.addActionListener(this);
        c.gridx = 1;
        buttonPanel.add(bClose);
        mainPanel.add(buttonPanel);

        getContentPane().add(mainPanel);
        getRootPane().setDefaultButton(bClose);
    }

    public void setVisible(boolean visible) {
        if (visible) {
            setLocationRelativeTo(parent);
        }
        super.setVisible(visible);
    }

    private void createFromOptions() {
        Tile t = null;
        if (myBrush instanceof ShapeBrush)
            t = ((ShapeBrush)myBrush).getTile();

        if (cbRandomBrush.isSelected()) {
            myBrush = new RandomBrush((AbstractBrush)brushes.getSelectedBrush());
            ((RandomBrush)myBrush).setRatio(sRandomAmount.getValue() / (double)sRandomAmount.getMaximum());
            ((ShapeBrush)myBrush).setTile(t);
        } else {
            myBrush = new ShapeBrush((AbstractBrush)brushes.getSelectedBrush());
            ((ShapeBrush)myBrush).setTile(t);
        }

        myBrush.setAffectedLayers(((Integer)affectLayers.getValue()).intValue());

        update();
    }

    private void update() {
        sRandomAmount.setEnabled(false);
        if (myBrush instanceof CustomBrush) {  //CUSTOM BRUSH
            affectLayers.setEnabled(false);
            cbRandomBrush.setEnabled(false);
            sRandomAmount.setEnabled(false);
        } else if (myBrush instanceof RandomBrush) {  //RANDOM BRUSH
            cbRandomBrush.setSelected(true);
            sRandomAmount.setValue((int)(((RandomBrush)myBrush).getRatio()*100));
        }

        //SPECIAL SETTINGS FOR NON-CUSTOM BRUSHES
        if (!(myBrush instanceof CustomBrush)) {
            sRandomAmount.setEnabled(cbRandomBrush.isSelected());
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == bApply) {
            bApply.setEnabled(false);
            createFromOptions();
            me.setBrush(myBrush);
        } else if (e.getActionCommand().equals("Close")) {
            dispose();
        } else if (source == cbRandomBrush) {
            sRandomAmount.setEnabled(cbRandomBrush.isSelected());
        }
    }

    public void stateChanged(ChangeEvent e) {
        bApply.setEnabled(true);
    }

    public void mouseClicked(MouseEvent e) {
        brushes.findSelected(e.getX(), e.getY());
        Brush b = brushes.getSelectedBrush();
        if (b != null)
            brushSize.setValue(b.getBounds().width);
    }

    public void mousePressed(MouseEvent e) {
        brushes.findSelected(e.getX(), e.getY());
        Brush b = brushes.getSelectedBrush();
        if (b != null)
            brushSize.setValue(b.getBounds().width);
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void propertyChange(PropertyChangeEvent evt) {
        bApply.setEnabled(true);
    }
}
