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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.*;

import tiled.core.MapObject;
import tiled.mapeditor.Resources;
import tiled.mapeditor.widget.IntegerSpinner;
import tiled.mapeditor.widget.VerticalStaticJPanel;

/**
 * @version $Id$
 */
public class ObjectDialog extends PropertiesDialog
{
    private JTextField objectName, objectType;
    private IntegerSpinner objectWidth, objectHeight;
    private MapObject object;

    /* LANGUAGE PACK */
    private static final String DIALOG_TITLE = Resources.getString("dialog.object.title");
    private static final String NAME_LABEL = Resources.getString("dialog.object.name.label");
    private static final String TYPE_LABEL = Resources.getString("dialog.object.type.label");
    private static final String WIDTH_LABEL = Resources.getString("dialog.object.width.label");
    private static final String HEIGHT_LABEL = Resources.getString("dialog.object.height.label");
    private static final String UNTITLED_OBJECT = Resources.getString("general.object.object");

    public ObjectDialog(JFrame parent, MapObject object) {
        super(parent, object.getProperties());
        this.object = object;
        setTitle(DIALOG_TITLE);
        pack();
        setLocationRelativeTo(parent);
    }

    public void init() {
        super.init();
        JLabel nameLabel = new JLabel(NAME_LABEL);
        JLabel typeLabel = new JLabel(TYPE_LABEL);
        JLabel widthLabel = new JLabel(WIDTH_LABEL);
        JLabel heightLabel = new JLabel(HEIGHT_LABEL);

        objectName = new JTextField(UNTITLED_OBJECT);
        objectType = new JTextField();
        objectWidth = new IntegerSpinner(0, 0, 1024);
        objectHeight = new IntegerSpinner(0, 0, 1024);

        JPanel miscPropPanel = new VerticalStaticJPanel();
        miscPropPanel.setLayout(new GridBagLayout());
        miscPropPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(5, 0, 0, 5);
        miscPropPanel.add(nameLabel, c);
        c.gridy = 1;
        miscPropPanel.add(typeLabel, c);
        c.gridy = 2;
        miscPropPanel.add(widthLabel, c);
        c.gridy = 3;
        miscPropPanel.add(heightLabel, c);
        c.insets = new Insets(5, 0, 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        miscPropPanel.add(objectName, c);
        c.gridy = 1;
        miscPropPanel.add(objectType, c);
        c.gridy = 2;
        miscPropPanel.add(objectWidth, c);
        c.gridy = 3;
        miscPropPanel.add(objectHeight, c);

        mainPanel.add(miscPropPanel, 0);
    }

    public void updateInfo() {
        super.updateInfo();
        objectName.setText(object.getName());
        objectType.setText(object.getType());
        objectWidth.setValue(object.getWidth());
        objectHeight.setValue(object.getHeight());
    }

    void buildPropertiesAndDispose() {
        object.setName(objectName.getText());
        object.setType(objectType.getText());
        object.setWidth(objectWidth.intValue());
        object.setHeight(objectHeight.intValue());
        super.buildPropertiesAndDispose();
    }
}
