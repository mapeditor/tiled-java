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

package tiled.mapeditor.selection;

import javax.swing.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.*;

public abstract class SelectionHandler implements MouseListener{

    protected Point clickPt,dragPt;

    SelectionHandler() {
        clickPt = new Point(0,0);
        dragPt = new Point(0,0);
    }

    SelectionHandler(int x,int y) {
        clickPt = new Point(x,y);
        dragPt = new Point(0,0);
    }

    SelectionHandler(int x,int y,int w,int h) {
        clickPt = new Point(x,y);
        dragPt = new Point(x+w,y+h);
    }

    public Point getClickPt() {
        return(clickPt);
    }

    public Point getDragPt() {
        return(dragPt);
    }

    public abstract void expand(int amt);
    public abstract void contract(int amt);

    protected void beginDrag() {

    }

    protected void endDrag() {

    }

    protected void stepDrag() {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void mouseClicked(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mousePressed(MouseEvent e) {
        clickPt.setLocation(e.getX(),e.getY());
        beginDrag();
    }

    public void mouseReleased(MouseEvent e) {
        dragPt.setLocation(e.getX(),e.getY());
        endDrag();

    }

    public void mouseDragged(MouseEvent e) {
        dragPt.setLocation(e.getX(),e.getY());
        stepDrag();
    }

    public void draw(Graphics g) {
        g.setColor(new Color(0,0,0));
        g.drawRect(clickPt.x,clickPt.y,dragPt.x-clickPt.x,dragPt.y-clickPt.y);
    }
}
