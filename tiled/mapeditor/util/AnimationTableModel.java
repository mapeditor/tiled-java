/*
 *  Tiled Map Editor, (c) 2005
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 * 
 *  Rainer Deyke <rainerd@eldwood.com>
 */


package tiled.mapeditor.util;


public class AnimationTableModel extends javax.swing.table.AbstractTableModel
{
    private tiled.core.Tile tile;

    public void setTile(tiled.core.Tile t)
    {
        this.tile = t;
        this.fireTableDataChanged();
    }

    public int getRowCount()
    {
        if (this.tile == null) return 0;
        return this.tile.countAnimationFrames();
    }

    public int getColumnCount()
    {
        return 2;
    }

    public String getColumnName(int n)
    {
        if (n == 0) return "Image";
        return "Duration";
    }

    public Object getValueAt(int row, int col)
    {
        if (col == 0) {
            return new javax.swing.ImageIcon
                (this.tile.getAnimationFrameImage(row));
        } else {
            return new Integer(this.tile.getAnimationFrameDuration(row));
        }
    }

    public boolean isCellEditable(int row, int col)
    {
        return col == 1;
    }

    public void setValueAt(Object value, int row, int col)
    {
        this.tile.setAnimationFrame(row,
            this.tile.getAnimationFrameImageId(row),
            this.tile.getAnimationFrameOrientation(row),
            ((Integer)value).intValue());
        this.fireTableCellUpdated(row, col);
    }

    public Class getColumnClass(int c)
    {
        if (c == 0) {
            return javax.swing.ImageIcon.class;
        } else {
            return Integer.class;
        }
    }

    public void removeAnimationFrame(int n)
    {
        this.tile.removeAnimationFrame(n);
        this.fireTableRowsDeleted(n, n);
    }

}

