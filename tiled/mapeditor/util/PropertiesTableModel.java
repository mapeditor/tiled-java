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

package tiled.mapeditor.util;

import java.util.Enumeration;
import java.util.Properties;
import javax.swing.table.AbstractTableModel;

public class PropertiesTableModel extends AbstractTableModel
{
    private Properties properties;

    private String[] columnNames = { "Name", "Value" };

    public PropertiesTableModel() {
        properties = new Properties();
    }

    public int getRowCount() {
        return properties.size() + 1;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public boolean isCellEditable(int row, int col) {
        return true;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Object [] array = properties.keySet().toArray();
        if (rowIndex >= 0 && rowIndex < properties.size()) {
            if (columnIndex == 0) {
                return array[rowIndex];
            } else if(columnIndex == 1) {
                return properties.get(array[rowIndex]);
            }
        }
        return null;
    }

    public void setValueAt(Object value, int row, int col) {
        if (row >= 0) {
            if ((row >= properties.size() && col == 0) || getValueAt(row, 0) == null) {
                properties.put(value, "0");
            } else {

                if (col == 1) {
                    properties.setProperty(
                            (String)getValueAt(row, 0), (String)value);
                } else if (col == 0) {
                    Object val = getValueAt(row, 1);
                    if (getValueAt(row, col) != null) {
                        properties.remove(getValueAt(row, col));
                    }
                    properties.put(value, val);
                }
            }
            fireTableCellUpdated(row, col);
        }

    }

    public void remove(int row) {
    	Enumeration e = properties.elements();
    	for(int i=0;e.hasMoreElements(); i++) {
    		Object key = e.nextElement();
    		if(i==row) {
    			properties.remove(key);
    			break;
    		}
    	}
    }
    
    public void update(Properties props) {
        properties = props;
    }

    public Properties getProperties() {
        return properties;
    }
}
