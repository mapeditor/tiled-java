/*
 * Tiled Map Editor, (c) 2004
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

import tiled.util.TiledConfiguration;

public class ConfigurationDialog extends JDialog implements ActionListener
{
	private JButton bOk, bApply, bCancel;
	private JPanel layerOps, generalOps, tilesetOps;
	private JTextField tUndoDepth;
	
	private void init() {
		JTabbedPane perfs = new JTabbedPane();
		JCheckBox cbBinaryEncode, cbCompressLayerData, cbEmbedImages;		
		JPanel buttons = new JPanel();
		JPanel saving = new JPanel();
		
		layerOps = new VerticalStaticJPanel();
		generalOps = new VerticalStaticJPanel();
		tilesetOps = new VerticalStaticJPanel();
		
		cbBinaryEncode = new JCheckBox();		
		cbCompressLayerData = new JCheckBox();
		cbEmbedImages = new JCheckBox();
		tUndoDepth = new JTextField();
		
		bOk = new JButton("OK");
		bApply = new JButton("Apply");
		bCancel = new JButton("Cancel");
		
		cbBinaryEncode.setActionCommand("tmx.save.encodeLayerData");
		cbCompressLayerData.setActionCommand("tmx.save.layerCompression");
		cbEmbedImages.setActionCommand("tmx.save.embedImages");
		tUndoDepth.setName("tmx.undo.depth");
		tUndoDepth.setColumns(3);
		
		bOk.addActionListener(this);
		bApply.addActionListener(this);
		bCancel.addActionListener(this);
		
		/* LAYER OPTIONS */
		layerOps.setLayout(new GridBagLayout());
		layerOps.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Layer Options"),
					BorderFactory.createEmptyBorder(0, 5, 5, 5)));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(1, 0, 0, 0);
		layerOps.add(new JLabel("Binary encoding: "), c);
		c.gridy = 1;
		layerOps.add(new JLabel("Compress data: "), c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1; c.gridy = 0; c.weightx = 1;		
		layerOps.add(cbBinaryEncode, c);
		c.gridy = 1;
		layerOps.add(cbCompressLayerData, c);
		
		/* GENERAL OPTIONS */
		generalOps.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(1, 0, 0, 0);
		generalOps.add(new JLabel("Undo Depth: "), c);
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1; c.gridy = 0; c.weightx = 1;		
		generalOps.add(tUndoDepth, c);		
		
		/* TILESET OPTIONS */
		tilesetOps.setLayout(new GridBagLayout());
		tilesetOps.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Tileset Options"),
					BorderFactory.createEmptyBorder(0, 5, 5, 5)));
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		c.insets = new Insets(1, 0, 0, 0);
		tilesetOps.add(new JLabel("Embed Images: "), c);		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1; c.gridy = 0; c.weightx = 1;		
		tilesetOps.add(cbEmbedImages, c);		
		
		/* BUTTONS PANEL */
		buttons.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		buttons.add(Box.createGlue());
		buttons.add(bOk);
		buttons.add(bApply);
		buttons.add(Box.createRigidArea(new Dimension(5, 0)));
		buttons.add(bCancel);
		
		saving.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		saving.setLayout(new BoxLayout(saving, BoxLayout.Y_AXIS));
		saving.add(Box.createGlue());
		
		saving.add(layerOps);
		saving.add(tilesetOps);
		
        perfs.addTab("General", generalOps);
        perfs.addTab("Saving", saving);
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		mainPanel.add(perfs);
		mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		mainPanel.add(Box.createGlue());
		mainPanel.add(buttons);
		
		getContentPane().add(mainPanel);
		getRootPane().setDefaultButton(bCancel);
		pack();
	}

	public ConfigurationDialog(JFrame parent) {
		super(parent, "Preferences");
		init();
        setLocationRelativeTo(parent);
        setModal(true);
	}

	public void configure() {
		updateFromConf();
		show();
	}

	private void updateFromConf() {
		
		tUndoDepth.setText(TiledConfiguration.getValue(tUndoDepth.getName()));
		
		// handle checkboxes...
		for(int i=0;i<layerOps.getComponentCount();i++) {
			try{			
				AbstractButton b = (AbstractButton)layerOps.getComponent(i);
				if(b.getClass().equals(JCheckBox.class)) {			
					if(TiledConfiguration.keyHasValue(b.getActionCommand(),"1")) {
						b.setSelected(true);
					}
				}
			} catch (ClassCastException e) {
			}
		}
		for(int i=0;i<tilesetOps.getComponentCount();i++) {
			try{			
				AbstractButton b = (AbstractButton)tilesetOps.getComponent(i);
				if(b.getClass().equals(JCheckBox.class)) {			
					if(TiledConfiguration.keyHasValue(b.getActionCommand(),"1")) {
						b.setSelected(true);
					}
				}
			} catch (ClassCastException e) {
			}
		}
	}

	private void processOptions() {
		
		TiledConfiguration.addConfigPair(tUndoDepth.getName(),tUndoDepth.getText());
		
		//handle checkboxes
		for(int i=0;i<layerOps.getComponentCount();i++) {
			try{			
				AbstractButton b = (AbstractButton)layerOps.getComponent(i);
				if(b.getClass().equals(JCheckBox.class)) {							
					TiledConfiguration.addConfigPair(b.getActionCommand(),b.isSelected()?"1":"0");
				}
			} catch (ClassCastException e) {
			}
		}
		for(int i=0;i<tilesetOps.getComponentCount();i++) {
			try{			
				AbstractButton b = (AbstractButton)tilesetOps.getComponent(i);
				if(b.getClass().equals(JCheckBox.class)) {							
					TiledConfiguration.addConfigPair(b.getActionCommand(),b.isSelected()?"1":"0");
				}
			} catch (ClassCastException e) {
			}
		}
		
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if(command.equalsIgnoreCase("ok")) {
			processOptions();
			dispose();
		} else if(command.equalsIgnoreCase("cancel")) {
			dispose();
		} else if(command.equalsIgnoreCase("apply")) {
			processOptions();
		}
	}

}
