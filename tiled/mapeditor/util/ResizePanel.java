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

import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;

public class ResizePanel extends JPanel implements MouseListener{
	
	private JButton inner;
	private Dimension oldDim, newDim;
	private int edge;
	
	public ResizePanel() {
		super();
		inner = new JButton();
		inner.addMouseListener(this);
		inner.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		add(inner);
		setLayout(new OverlayLayout(this));
		setBorder(BorderFactory.createLoweredBevelBorder());
	}

	public ResizePanel(Dimension size, int edge) {
		this();
		oldDim = newDim = size;
		setSize(size);
	}

	public void setNewDimensions(Dimension n) {
		newDim = n;
		//TODO: recalc the button size...
	}

	public Dimension getPreferredSize() {
		return oldDim;
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		System.out.println("Yay!");
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}
}
