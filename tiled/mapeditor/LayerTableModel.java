/*
 *  Tiled Map Editor, (c) 2004
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 * 
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjørn Lindeijer <b.lindeijer@xs4all.nl>
 */

package tiled.mapeditor;

import javax.swing.table.AbstractTableModel;

import tiled.core.Map;
import tiled.core.MapLayer;


public class LayerTableModel extends AbstractTableModel
{
    private Map map;
    private String[] columnNames = { "Show", "Layer name" };

    public LayerTableModel(Map map) {
        this.map = map;
    }

    public void setMap(Map map) {
        this.map = map;
        fireTableDataChanged();
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public int getRowCount() {
        if (map != null) {
            return map.getTotalLayers();
        } else {
            return 0;
        }
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Class getColumnClass(int col) {
        return getValueAt(0, col).getClass();
    }

    public Object getValueAt(int row, int col) {
        MapLayer layer = map.getLayer(getRowCount() - row - 1);
        if (layer != null) {
            if (col == 0) {
                return new Boolean(layer.isVisible());
            } else if (col == 1) {
                return layer.getName();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        return true;
    }

    public void setValueAt(Object value, int row, int col) {
        MapLayer layer = map.getLayer(getRowCount() - row - 1);
        if (layer != null) {
            if (col == 0) {
                Boolean bool = (Boolean)value;
                layer.setVisible(bool.booleanValue());
            } else if (col == 1) {
                layer.setName(value.toString());
            }
            fireTableCellUpdated(row, col);
        }
    }
}
