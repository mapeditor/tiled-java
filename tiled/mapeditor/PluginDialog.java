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
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import tiled.io.MapReader;
import tiled.mapeditor.plugin.PluginClassLoader;
import tiled.mapeditor.widget.VerticalStaticJPanel;

public class PluginDialog extends JFrame implements ActionListener {

	private JFrame parent;
	private PluginClassLoader pluginLoader;
	private JList pluginList = null;
	
	public PluginDialog(JFrame parent, PluginClassLoader pluginLoader) {
		super("Available Plugins");
		this.pluginLoader = pluginLoader;
		
		JPanel buttonPanel;
		JButton closeButton, infoButton, removeButton;
		VerticalStaticJPanel mainPanel = new VerticalStaticJPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		/* LIST PANEL */		
		MapReader readers[];
		
        try {
            readers = (MapReader[]) pluginLoader.getReaders();
            String [] plugins = new String[readers.length];
			for(int i=0;i<readers.length;i++) {
				plugins[i] = readers[i].getPluginPackage();
				  
				//TODO: check for a writer as well, and designate the
				//		plugins that have both
			}
			pluginList = new JList(plugins);			
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
		JScrollPane pluginScrollPane = new JScrollPane(pluginList);
		pluginScrollPane.setAutoscrolls(true);
		pluginScrollPane.setPreferredSize(new Dimension(200, 150));
		
		mainPanel.add(pluginScrollPane);
		
		/* BUTTON PANEL */
		buttonPanel = new JPanel();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		buttonPanel.setLayout(new GridLayout(2,2,0,0));
		infoButton = new JButton("Info");
		infoButton.addActionListener(this);
		buttonPanel.add(infoButton);
		removeButton = new JButton("Remove");
		removeButton.addActionListener(this);
		buttonPanel.add(removeButton);
		closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton);
		
		mainPanel.add(buttonPanel);
		setContentPane(mainPanel);
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

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        if(command.equalsIgnoreCase("close")) {
        	this.dispose();
        }else if(command.equalsIgnoreCase("remove")) {
        	
        }else if(command.equalsIgnoreCase("info")) {
        	JDialog info = new JDialog(this);
        	JTextArea ta = new JTextArea(25,30);
        	int index = pluginList.getSelectedIndex();
        	
        	MapReader[] readers;
        	try{
				readers = (MapReader[]) pluginLoader.getReaders();
				ta.setText(readers[index].getDescription());
        	}catch (Throwable t) {
        		t.printStackTrace();
        	}
        	ta.setEditable(false);
        	ta.setFont(new Font("SanSerif", Font.PLAIN, 11));
        	info.getContentPane().add(ta);
        	info.setLocationRelativeTo(this);
        	info.pack();
        	info.setVisible(true);
		}
    }
}
