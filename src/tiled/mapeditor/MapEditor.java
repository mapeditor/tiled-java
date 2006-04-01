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

package tiled.mapeditor;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.io.*;
import java.util.Stack;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.UndoableEditSupport;

import tiled.core.*;
import tiled.view.*;
import tiled.mapeditor.brush.*;
import tiled.mapeditor.dialogs.*;
import tiled.mapeditor.plugin.PluginClassLoader;
import tiled.mapeditor.selection.SelectionLayer;
import tiled.mapeditor.util.*;
import tiled.mapeditor.widget.*;
import tiled.mapeditor.undo.*;
import tiled.util.TileMergeHelper;
import tiled.util.TiledConfiguration;
import tiled.util.Util;
import tiled.io.MapHelper;
import tiled.io.MapReader;
import tiled.io.MapWriter;

/**
 * The main class for the Tiled Map Editor.
 *
 * @version $Id$
 */
public class MapEditor implements ActionListener, MouseListener,
        MouseMotionListener, MapChangeListener, ListSelectionListener,
        ChangeListener, ComponentListener
{
    // Constants and the like
    private static final int PS_POINT   = 0;
    private static final int PS_PAINT   = 1;
    private static final int PS_ERASE   = 2;
    private static final int PS_POUR    = 3;
    private static final int PS_EYED    = 4;
    private static final int PS_MARQUEE = 5;
    private static final int PS_MOVE    = 6;
    private static final int PS_MOVEOBJ = 7;

    private static final int APP_WIDTH = 600;
    private static final int APP_HEIGHT = 400;

    private Cursor curDefault;
    private Cursor curPaint;
    private Cursor curErase;
    private Cursor curPour;
    private Cursor curEyed;
    private Cursor curMarquee;

    /** Current release version */
    public static final String version = "0.6.0";

    private Map currentMap;
    private MapView mapView;
    private final UndoStack undoStack;
    private final UndoableEditSupport undoSupport;
    private final MapEventAdapter mapEventAdapter;
    private final PluginClassLoader pluginLoader;
    private final Preferences prefs = TiledConfiguration.root();

    int currentPointerState;
    Tile currentTile;
    int currentLayer = -1;
    boolean bMouseIsDown;
    SelectionLayer cursorHighlight;
    Point mousePressLocation, mouseInitialPressLocation;
    Point moveDist;
    int mouseButton;
    AbstractBrush currentBrush;
    SelectionLayer marqueeSelection;
    MapLayer clipboardLayer;

    // GUI components
    JMenu       fileMenu, editMenu, selectMenu, viewMenu, helpMenu;
    JMenu       mapMenu, layerMenu, tilesetMenu;
    JPanel      mainPanel;
    JPanel      toolPanel;
    JPanel      dataPanel;
    JPanel      statusBar;
    JMenuBar    menuBar;
    JMenuItem   undoMenuItem, redoMenuItem;
    JMenuItem   copyMenuItem, cutMenuItem, pasteMenuItem;
    JCheckBoxMenuItem gridMenuItem, boundaryMenuItem, cursorMenuItem;
    JCheckBoxMenuItem coordinatesMenuItem;
    JMenuItem   layerAdd, layerClone, layerDel;
    JMenuItem   layerUp, layerDown;
    JMenuItem   layerMerge, layerMergeAll;
    JMenuItem   layerProperties;
    JMenu       recentMenu;
    JScrollPane mapScrollPane;
    JTable      layerTable;
    JList       editHistoryList;
    MiniMapViewer miniMap;

    TileButton  tilePaletteButton;
    JFrame      appFrame;
    JSlider     opacitySlider;
    JLabel      zoomLabel, tileCoordsLabel;

    AbstractButton layerAddButton, layerCloneButton, layerDelButton;
    AbstractButton layerUpButton, layerDownButton;
    AbstractButton paintButton, eraseButton, pourButton;
    AbstractButton eyedButton, marqueeButton, moveButton;
    AbstractButton objectMoveButton, objectAddButton;

    TilePalettePanel tilePalettePanel;
    TilePaletteDialog tilePaletteDialog;
    AboutDialog aboutDialog;
    MapLayerEdit paintEdit;

    /** Available brushes */
    Vector brushes = new Vector();
    Brush eraserBrush;

    // Actions
    final Action zoomInAction, zoomOutAction, zoomNormalAction;
    final Action undoAction, redoAction;
    final Action rot90Action, rot180Action, rot270Action;
    final Action flipHorAction, flipVerAction;
    final Action copyAction, cutAction, pasteAction;
    final Action selectAllAction, inverseAction, cancelSelectionAction;

    public MapEditor() {
        /*eraserBrush = new Eraser();
        brushes.add(eraserBrush());
        setBrush(eraserBrush);*/

        /*
        try {
            Image imgPaintCursor = Resources.getImage("cursor-pencil.png");

            curPaint = Toolkit.getDefaultToolkit().createCustomCursor(
                    imgPaintCursor, new Point(0,0), "paint");
        } catch (Exception e) {
            System.out.println("Error while loading custom cursors!");
            e.printStackTrace();
        }
        */

        curEyed = new Cursor(Cursor.CROSSHAIR_CURSOR);
        curDefault = new Cursor(Cursor.DEFAULT_CURSOR);

        undoStack = new UndoStack();
        undoSupport = new UndoableEditSupport();
        undoSupport.addUndoableEditListener(new UndoAdapter());

        cursorHighlight = new SelectionLayer(1, 1);
        cursorHighlight.select(0, 0);
        cursorHighlight.setVisible(prefs.getBoolean("cursorhighlight", true));

        mapEventAdapter = new MapEventAdapter();

        //Create a default brush
        ShapeBrush sb = new ShapeBrush();
        sb.makeQuadBrush(new Rectangle(0, 0, 1, 1));
        setBrush(sb);

        // Create the actions
        zoomInAction = new ZoomInAction();
        zoomOutAction = new ZoomOutAction();
        zoomNormalAction = new ZoomNormalAction();
        undoAction = new UndoAction();
        redoAction = new RedoAction();
        rot90Action = new LayerTransformAction(MapLayer.ROTATE_90);
        rot180Action = new LayerTransformAction(MapLayer.ROTATE_180);
        rot270Action = new LayerTransformAction(MapLayer.ROTATE_270);
        flipHorAction = new LayerTransformAction(MapLayer.MIRROR_HORIZONTAL);
        flipVerAction = new LayerTransformAction(MapLayer.MIRROR_VERTICAL);
        copyAction = new CopyAction();
        pasteAction = new PasteAction();
        cutAction = new CutAction();
        selectAllAction = new SelectAllAction();
        cancelSelectionAction = new CancelSelectionAction();
        inverseAction = new InverseSelectionAction();

        // Create our frame
        appFrame = new JFrame("Tiled");
        appFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        appFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                exit();
            }
        });
        appFrame.setContentPane(createContentPane());
        createMenuBar();
        appFrame.setJMenuBar(menuBar);
        appFrame.setSize(APP_WIDTH, APP_HEIGHT);
        setCurrentMap(null);
        updateRecent(null);

        appFrame.setVisible(true);

        // Load plugins
        pluginLoader  = PluginClassLoader.getInstance();
        try {
            pluginLoader.readPlugins(null, appFrame);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(appFrame,
                    e.toString(), "Plugin loader",
                    JOptionPane.WARNING_MESSAGE);
        }
        MapHelper.init(pluginLoader);
    }

    private JPanel createContentPane() {
        mapScrollPane = new JScrollPane(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        mapScrollPane.setBorder(null);

        createToolBar();
        createData();
        createStatusBar();

        JSplitPane mainSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, false, mapScrollPane, dataPanel);
        mainSplit.setResizeWeight(1.0);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(toolPanel, BorderLayout.WEST);
        mainPanel.add(mainSplit);
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        return mainPanel;
    }

    private void exit() {
        if (checkSave()) {
            System.exit(0);
        }
    }

    /**
     * Creates all the menus and submenus of the top menu bar. Handles
     * assigning listeners and tooltips as well.
     */
    private void createMenuBar() {
        JMenuItem save = createMenuItem("Save", null, "Save current map",
                "control S");
        JMenuItem saveAs = createMenuItem("Save as...", null,
                "Save current map as new file", "control shift S");
        JMenuItem saveAsImage = createMenuItem("Save as Image...", null,
                "Save current map as an image", "control shift I");
        JMenuItem close = createMenuItem("Close", null, "Close this map",
                "control W");
        JMenuItem print =
            createMenuItem("Print...", null, "Print the map", "control P");

        recentMenu = new JMenu("Open Recent");

        mapEventAdapter.addListener(save);
        mapEventAdapter.addListener(saveAs);
        mapEventAdapter.addListener(saveAsImage);
        mapEventAdapter.addListener(close);
        mapEventAdapter.addListener(print);

        fileMenu = new JMenu("File");
        fileMenu.add(createMenuItem("New...", null, "Start a new map",
                    "control N"));
        fileMenu.add(createMenuItem("Open...", null, "Open a map",
                    "control O"));
        fileMenu.add(recentMenu);
        fileMenu.add(save);
        fileMenu.add(saveAs);
        fileMenu.add(saveAsImage);
        // TODO: Re-add print menuitem when printing is functional
        //fileMenu.addSeparator();
        //fileMenu.add(print);
        //mapEventAdapter.addListener(print);
        fileMenu.addSeparator();
        fileMenu.add(close);
        fileMenu.add(createMenuItem("Exit", null, "Exit the map editor",
                    "control Q"));

        undoMenuItem = new TMenuItem(undoAction);
        redoMenuItem = new TMenuItem(redoAction);
        undoMenuItem.setEnabled(false);
        redoMenuItem.setEnabled(false);

        copyMenuItem = new TMenuItem(copyAction);
        cutMenuItem = new TMenuItem(cutAction);
        pasteMenuItem = new TMenuItem(pasteAction);
        copyMenuItem.setEnabled(false);
        cutMenuItem.setEnabled(false);
        pasteMenuItem.setEnabled(false);

        JMenu transformSub = new JMenu("Transform");
        transformSub.add(new TMenuItem(rot90Action, true));
        transformSub.add(new TMenuItem(rot180Action, true));
        transformSub.add(new TMenuItem(rot270Action, true));
        transformSub.addSeparator();
        transformSub.add(new TMenuItem(flipHorAction, true));
        transformSub.add(new TMenuItem(flipVerAction, true));
        mapEventAdapter.addListener(transformSub);

        editMenu = new JMenu("Edit");
        editMenu.add(undoMenuItem);
        editMenu.add(redoMenuItem);
        editMenu.addSeparator();
        editMenu.add(copyMenuItem);
        editMenu.add(cutMenuItem);
        editMenu.add(pasteMenuItem);
        editMenu.addSeparator();
        editMenu.add(transformSub);
        editMenu.addSeparator();
        editMenu.add(createMenuItem("Preferences...",
                    null, "Configure options of the editor", null));
        editMenu.add(createMenuItem("Brush...", null, "Configure the brush",
                    "control B"));

        mapEventAdapter.addListener(undoMenuItem);
        mapEventAdapter.addListener(redoMenuItem);
        mapEventAdapter.addListener(copyMenuItem);
        mapEventAdapter.addListener(cutMenuItem);
        mapEventAdapter.addListener(pasteMenuItem);


        mapMenu = new JMenu("Map");
        mapMenu.add(createMenuItem("Resize", null, "Modify map dimensions"));
        mapMenu.add(createMenuItem("Search", null,
                    "Search for/Replace tiles"));
        mapMenu.addSeparator();
        mapMenu.add(createMenuItem("Properties", null, "Map properties"));
        mapEventAdapter.addListener(mapMenu);


        layerAdd = createMenuItem("Add Layer", null, "Add a layer");
        layerClone = createMenuItem("Duplicate Layer", null,
                "Duplicate current layer");
        layerDel = createMenuItem("Delete Layer", null,
                "Delete current layer");
        layerUp = createMenuItem("Move Layer Up", null,
                "Move layer up one in layer stack", "shift PAGE_UP");
        layerDown = createMenuItem("Move Layer Down", null,
                "Move layer down one in layer stack", "shift PAGE_DOWN");
        layerMerge = createMenuItem("Merge Down", null,
                "Merge current layer onto next lower", "shift control M");
        layerMergeAll = createMenuItem("Merge All", null, "Merge all layers");
        layerProperties = createMenuItem("Layer Properties", null,
                "Current layer properties");

        mapEventAdapter.addListener(layerAdd);

        layerMenu = new JMenu("Layer");
        layerMenu.add(layerAdd);
        layerMenu.add(layerClone);
        layerMenu.add(layerDel);
        layerMenu.addSeparator();
        layerMenu.add(layerUp);
        layerMenu.add(layerDown);
        layerMenu.addSeparator();
        layerMenu.add(layerMerge);
        layerMenu.add(layerMergeAll);
        layerMenu.addSeparator();
        layerMenu.add(layerProperties);

        tilesetMenu = new JMenu("Tilesets");
        tilesetMenu.add(createMenuItem("New Tileset...", null,
                    "Add a new internal tileset"));
        tilesetMenu.add(createMenuItem("Import Tileset...", null,
                    "Import an external tileset"));
        tilesetMenu.addSeparator();
        tilesetMenu.add(createMenuItem("Tileset Manager", null,
                    "Open the tileset manager"));


        /*
        objectMenu = new JMenu("Objects");
        objectMenu.add(createMenuItem("Add Object", null, "Add an object"));
        mapEventAdapter.addListener(objectMenu);

        JMenu modifySub = new JMenu("Modify");
        modifySub.add(createMenuItem("Expand Selection", null, ""));
        modifySub.add(createMenuItem("Contract Selection", null, ""));
        */

        selectMenu = new JMenu("Select");
        selectMenu.add(new TMenuItem(selectAllAction, true));
        selectMenu.add(new TMenuItem(cancelSelectionAction, true));
        selectMenu.add(new TMenuItem(inverseAction, true));
        //selectMenu.addSeparator();
        //selectMenu.add(modifySub);


        gridMenuItem = new JCheckBoxMenuItem("Show Grid");
        gridMenuItem.addActionListener(this);
        gridMenuItem.setToolTipText("Toggle grid");
        gridMenuItem.setAccelerator(KeyStroke.getKeyStroke("control G"));

        cursorMenuItem = new JCheckBoxMenuItem("Highlight Cursor");
        cursorMenuItem.setSelected(prefs.getBoolean("cursorhighlight", true));
        cursorMenuItem.addActionListener(this);
        cursorMenuItem.setToolTipText(
                "Toggle highlighting on-map cursor position");

        boundaryMenuItem = new JCheckBoxMenuItem("Show Boundaries");
        boundaryMenuItem.addActionListener(this);
        boundaryMenuItem.setToolTipText("Toggle layer boundaries");
        boundaryMenuItem.setAccelerator(KeyStroke.getKeyStroke("control E"));

        coordinatesMenuItem = new JCheckBoxMenuItem("Show Coordinates");
        coordinatesMenuItem.addActionListener(this);
        coordinatesMenuItem.setToolTipText("Toggle tile coordinates");

        viewMenu = new JMenu("View");
        viewMenu.add(new TMenuItem(zoomInAction));
        viewMenu.add(new TMenuItem(zoomOutAction));
        viewMenu.add(new TMenuItem(zoomNormalAction));
        viewMenu.addSeparator();
        viewMenu.add(gridMenuItem);
        viewMenu.add(cursorMenuItem);
        // TODO: Enable when boudary drawing code finished.
        //viewMenu.add(boundaryMenuItem);
        viewMenu.add(coordinatesMenuItem);

        mapEventAdapter.addListener(layerMenu);
        mapEventAdapter.addListener(tilesetMenu);
        mapEventAdapter.addListener(selectMenu);
        mapEventAdapter.addListener(viewMenu);

        helpMenu = new JMenu("Help");
        helpMenu.add(createMenuItem("About Plug-ins", null,
                    "Show plugin window"));
        helpMenu.add(createMenuItem("About Tiled", null, "Show about window"));

        menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(selectMenu);
        menuBar.add(viewMenu);
        menuBar.add(mapMenu);
        menuBar.add(layerMenu);
        menuBar.add(tilesetMenu);
        //menuBar.add(objectMenu);
        menuBar.add(helpMenu);
    }

    /**
     * Creates the tool bar.
     */
    private void createToolBar() {
        Icon iconMove = Resources.getIcon("gimp-tool-move-22.png");
        Icon iconPaint = Resources.getIcon("gimp-tool-pencil-22.png");
        Icon iconErase = Resources.getIcon("gimp-tool-eraser-22.png");
        Icon iconPour = Resources.getIcon("gimp-tool-bucket-fill-22.png");
        Icon iconEyed = Resources.getIcon("gimp-tool-color-picker-22.png");
        Icon iconMarquee = Resources.getIcon("gimp-tool-rect-select-22.png");
        Icon iconMoveObject = Resources.getIcon("gimp-tool-object-move-22.png");

        paintButton = createToggleButton(iconPaint, "paint", "Paint");
        eraseButton = createToggleButton(iconErase, "erase", "Erase");
        pourButton = createToggleButton(iconPour, "pour", "Fill");
        eyedButton = createToggleButton(iconEyed, "eyed", "Eye dropper");
        marqueeButton = createToggleButton(iconMarquee, "marquee", "Select");
        moveButton = createToggleButton(iconMove, "move", "Move layer");
        objectMoveButton = createToggleButton(iconMoveObject, "moveobject", "Move Object");

        mapEventAdapter.addListener(moveButton);
        mapEventAdapter.addListener(paintButton);
        mapEventAdapter.addListener(eraseButton);
        mapEventAdapter.addListener(pourButton);
        mapEventAdapter.addListener(eyedButton);
        mapEventAdapter.addListener(marqueeButton);
        mapEventAdapter.addListener(objectMoveButton);

        JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
        toolBar.setFloatable(true);
        toolBar.add(moveButton);
        toolBar.add(paintButton);
        toolBar.add(eraseButton);
        toolBar.add(pourButton);
        toolBar.add(eyedButton);
        toolBar.add(marqueeButton);
        toolBar.add(Box.createRigidArea(new Dimension(5, 5)));
        //TODO: put this back when working...
        //toolBar.add(objectMoveButton);
        //toolBar.add(Box.createRigidArea(new Dimension(0, 5)));
        toolBar.add(new TButton(zoomInAction));
        toolBar.add(new TButton(zoomOutAction));

        tilePaletteButton = new TileButton(new Dimension(24, 24));
        tilePaletteButton.setActionCommand("palette");
        tilePaletteButton.setMaintainAspect(true);
        mapEventAdapter.addListener(tilePaletteButton);
        tilePaletteButton.addActionListener(this);

        toolPanel = new JPanel(new BorderLayout());
        toolPanel.add(toolBar, BorderLayout.NORTH);
        toolPanel.add(tilePaletteButton, BorderLayout.SOUTH);
    }

    private void createData() {
        JToolBar tabsPanel = new JToolBar();
        JTabbedPane paintPanel = new JTabbedPane();

        dataPanel = new JPanel(new BorderLayout());

        // Try to load the icons
        Icon imgAdd = Resources.getIcon("gnome-new.png");
        Icon imgDel = Resources.getIcon("gnome-delete.png");
        Icon imgDup = Resources.getIcon("gimp-duplicate-16.png");
        Icon imgUp = Resources.getIcon("gnome-up.png");
        Icon imgDown = Resources.getIcon("gnome-down.png");

        //navigation and tool options
        // TODO: the minimap is prohibitively slow, need to speed this up
        // before it can be used
        miniMap = new MiniMapViewer();
        //miniMap.setMainPanel(mapScrollPane);
        JScrollPane miniMapSp = new JScrollPane();
        miniMapSp.getViewport().setView(miniMap);
        miniMapSp.setMinimumSize(new Dimension(0, 120));

        // Layer table
        layerTable = new JTable(new LayerTableModel(currentMap));
        layerTable.getColumnModel().getColumn(0).setPreferredWidth(32);
        layerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        layerTable.getSelectionModel().addListSelectionListener(this);

        // Opacity slider
        opacitySlider = new JSlider(0, 100, 100);
        opacitySlider.addChangeListener(this);
        JLabel opacityLabel = new JLabel("Opacity: ");
        opacityLabel.setLabelFor(opacitySlider);

        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.X_AXIS));
        sliderPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
        sliderPanel.add(opacityLabel);
        sliderPanel.add(opacitySlider);
        sliderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                    sliderPanel.getPreferredSize().height));

        // Layer buttons
        layerAddButton = createButton(imgAdd, "Add Layer", "Add Layer");
        layerDelButton = createButton(imgDel, "Delete Layer", "Delete Layer");
        layerCloneButton = createButton(imgDup, "Duplicate Layer",
                "Duplicate Layer");
        layerUpButton = createButton(imgUp, "Move Layer Up", "Move Layer Up");
        layerDownButton = createButton(imgDown, "Move Layer Down",
                "Move Layer Down");

        mapEventAdapter.addListener(layerAddButton);

        JPanel layerButtons = new JPanel();
        layerButtons.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        layerButtons.add(layerAddButton, c);
        layerButtons.add(layerUpButton, c);
        layerButtons.add(layerDownButton, c);
        layerButtons.add(layerCloneButton, c);
        layerButtons.add(layerDelButton, c);
        layerButtons.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                    layerButtons.getPreferredSize().height));

        // Edit history
        /*
        JScrollPane editSp = new JScrollPane();
        editHistoryList = new JList();
        editSp.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        editSp.getViewport().setView(editHistoryList);
        */

        JPanel layerPanel = new JPanel();
        layerPanel.setLayout(new GridBagLayout());
        layerPanel.setPreferredSize(new Dimension(120, 120));
        c = new GridBagConstraints();
        c.insets = new Insets(3, 0, 0, 0); c.weightx = 1; c.weighty = 0;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0; c.gridy = 0;
        //layerPanel.add(miniMapSp, c);
        c.weighty = 0; c.gridy += 1;
        layerPanel.add(sliderPanel, c);
        c.weighty = 1; c.gridy += 1;
        layerPanel.add(new JScrollPane(layerTable), c);
        c.weighty = 0; c.insets = new Insets(0, 0, 0, 0); c.gridy += 1;
        layerPanel.add(layerButtons, c);
        /*
        c.weighty = 0.25; c.insets = new Insets(3, 0, 0, 0); c.gridy += 1;
        layerPanel.add(editSp, c);
        */

        // Create paint panel
        tilePalettePanel = new TilePalettePanel();

        JPanel brushesPanel = new JPanel();
        paintPanel.add("Palette", tilePalettePanel);
        paintPanel.add("Brushes", brushesPanel);
        paintPanel.setSelectedIndex(1);
        tabsPanel.add(paintPanel);

        dataPanel.add(layerPanel);
    }

    private void createStatusBar() {
        statusBar = new JPanel();
        statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.X_AXIS));

        zoomLabel = new JLabel("100%");
        zoomLabel.setPreferredSize(zoomLabel.getPreferredSize());
        tileCoordsLabel = new JLabel(" ", SwingConstants.CENTER);

        statusBar.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        JPanel largePart = new JPanel();

        statusBar.add(largePart);
        statusBar.add(tileCoordsLabel);
        statusBar.add(Box.createRigidArea(new Dimension(20, 0)));
        statusBar.add(zoomLabel);
    }

    private void updateLayerTable() {
        int cl = currentLayer;
        if (layerTable.isEditing()) {
            layerTable.getCellEditor(layerTable.getEditingRow(),
                    layerTable.getEditingColumn()).cancelCellEditing();
        }
        ((LayerTableModel)layerTable.getModel()).setMap(currentMap);

        if (currentMap != null) {
            if (currentMap.getTotalLayers() > 0 && cl == -1) {
                cl = 0;
            }

            setCurrentLayer(cl);
        }

        updateLayerOperations();
    }

    private void updateLayerOperations() {
        int nrLayers = 0;

        if (currentMap != null) {
            nrLayers = currentMap.getTotalLayers();
        }

        boolean validSelection = currentLayer >= 0;
        boolean notBottom = currentLayer > 0;
        boolean notTop = currentLayer < nrLayers - 1 && validSelection;

        layerClone.setEnabled(validSelection);
        layerDel.setEnabled(validSelection);
        layerUp.setEnabled(notTop);
        layerDown.setEnabled(notBottom);
        layerMerge.setEnabled(notBottom);
        layerMergeAll.setEnabled(nrLayers > 1);

        layerCloneButton.setEnabled(validSelection);
        layerDelButton.setEnabled(validSelection);
        layerUpButton.setEnabled(notTop);
        layerDownButton.setEnabled(notBottom);

        opacitySlider.setEnabled(validSelection);
    }

    private JMenuItem createMenuItem(String name, Icon icon, String tt) {
        JMenuItem menuItem = new JMenuItem(name);
        menuItem.addActionListener(this);
        if (icon != null) {
            menuItem.setIcon(icon);
        }
        if (tt != null) {
            menuItem.setToolTipText(tt);
        }
        return menuItem;
    }

    private JMenuItem createMenuItem(String name, Icon icon, String tt,
            String keyStroke) {
        JMenuItem menuItem = createMenuItem(name, icon, tt);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(keyStroke));
        return menuItem;
    }

    private AbstractButton createToggleButton(Icon icon, String command,
            String tt) {
        return createButton(icon, command, true, tt);
    }

    private AbstractButton createButton(Icon icon, String command, String tt) {
        return createButton(icon, command, false, tt);
    }

    private AbstractButton createButton(Icon icon, String command,
            boolean toggleButton, String tt) {
        AbstractButton button;
        if (toggleButton) {
            button = new JToggleButton("", icon);
        } else {
            button = new JButton("", icon);
        }
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setActionCommand(command);
        button.addActionListener(this);
        if (tt != null) {
            button.setToolTipText(tt);
        }
        return button;
    }

    /**
     * Returns the currently selected tile.
     *
     * @return The currently selected tile.
     */
    public Tile getCurrentTile() {
        return currentTile;
    }

    /**
     * Returns the current map.
     *
     * @return The currently selected map.
     */
    public Map getCurrentMap() {
        return currentMap;
    }

    /**
     * Returns the currently selected layer.
     *
     * @return THe currently selected layer.
     */
    public MapLayer getCurrentLayer() {
        return currentMap.getLayer(currentLayer);
    }

    /**
     * Returns the main application frame.
     *
     * @return The frame of the main application
     */
    public Frame getAppFrame() {
        return appFrame;
    }

    private void updateHistory() {
        //editHistoryList.setListData(undoStack.getEdits());
        undoMenuItem.setText(undoStack.getUndoPresentationName());
        redoMenuItem.setText(undoStack.getRedoPresentationName());
        undoMenuItem.setEnabled(undoStack.canUndo());
        redoMenuItem.setEnabled(undoStack.canRedo());
        updateTitle();
    }

    private void doLayerStateChange(ActionEvent event) {
        if (currentMap == null) {
            return;
        }

        String command = event.getActionCommand();
        Vector layersBefore = new Vector(currentMap.getLayerVector());

        if (command.equals("Add Layer")) {
            currentMap.addLayer();
            setCurrentLayer(currentMap.getTotalLayers() - 1);
        } else if (command.equals("Duplicate Layer")) {
            if (currentLayer >= 0) {
                try {
                    MapLayer clone =
                        (MapLayer)getCurrentLayer().clone();
                    clone.setName(clone.getName() + " copy");
                    currentMap.addLayer(clone);
                } catch (CloneNotSupportedException ex) {
                    ex.printStackTrace();
                }
                setCurrentLayer(currentMap.getTotalLayers() - 1);
            }
        } else if (command.equals("Move Layer Up")) {
            if (currentLayer >= 0) {
                try {
                    currentMap.swapLayerUp(currentLayer);
                    setCurrentLayer(currentLayer + 1);
                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }
            }
        } else if (command.equals("Move Layer Down")) {
            if (currentLayer >= 0) {
                try {
                    currentMap.swapLayerDown(currentLayer);
                    setCurrentLayer(currentLayer - 1);
                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }
            }
        } else if (command.equals("Delete Layer")) {
            if (currentLayer >= 0) {
                currentMap.removeLayer(currentLayer);
                setCurrentLayer(currentLayer < 0 ? 0 : currentLayer);
            }
        } else if (command.equals("Merge Down")) {
            if (currentLayer >= 0) {
                try {
                    currentMap.mergeLayerDown(currentLayer);
                    setCurrentLayer(currentLayer - 1);
                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }
            }
        } else if (command.equals("Merge All")) {
            if (JOptionPane.showConfirmDialog(appFrame,
                    "Do you wish to merge tile images, and create a new tile set?",
                    "Merge Tiles?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION ) {
                TileMergeHelper tmh = new TileMergeHelper(currentMap);
                int len = currentMap.getTotalLayers();
                //TODO: Add a dialog option: "Yes, visible only"
                TileLayer newLayer = tmh.merge(0, len, true);
                currentMap.removeAllLayers();
                currentMap.addLayer(newLayer);
                currentMap.addTileset(tmh.getSet());
            } else {
                while (currentMap.getTotalLayers() > 1) {
                    try {
                        currentMap.mergeLayerDown(
                                currentMap.getTotalLayers() - 1);
                    } catch (Exception ex) {}
                }
            }
            setCurrentLayer(0);
        }

        undoSupport.postEdit(new MapLayerStateEdit(currentMap, layersBefore,
                    new Vector(currentMap.getLayerVector()), command));
    }

    private void doMouse(MouseEvent event) {
        if (currentMap == null || currentLayer < 0) {
            return;
        }

        Point tile = mapView.screenToTileCoords(event.getX(), event.getY());
        MapLayer layer = getCurrentLayer();

        if (layer == null) {
            return;
        } else if (mouseButton == MouseEvent.BUTTON3) {
            if (layer instanceof TileLayer) {
                Tile newTile = ((TileLayer)layer).getTileAt(tile.x, tile.y);
                setCurrentTile(newTile);
            } else if (layer instanceof ObjectGroup) {
                // TODO: Add support for ObjectGroups here
            }
        } else if (mouseButton == MouseEvent.BUTTON1) {
            switch (currentPointerState) {
                case PS_PAINT:
                    paintEdit.setPresentationName("Paint");
                    if (layer instanceof TileLayer) {
                        try {
							mapView.repaintRegion(currentBrush.doPaint(tile.x, tile.y));
						} catch (Exception e) {
							e.printStackTrace();
						}
                    }
                    break;
                case PS_ERASE:
                    paintEdit.setPresentationName("Erase");
                    if (layer instanceof TileLayer) {
                        ((TileLayer) layer).setTileAt(tile.x, tile.y, null);
                    }
                    mapView.repaintRegion(new Rectangle(tile.x, tile.y, 1, 1));
                    break;
                case PS_POUR:  // POUR only works on TileLayers
                    paintEdit = null;
                    if (layer instanceof TileLayer) {
                        Tile oldTile = ((TileLayer)layer).getTileAt(tile.x, tile.y);
                        pour((TileLayer) layer, tile.x, tile.y, currentTile, oldTile);
                        mapView.repaint();
                    }
                    break;
                case PS_EYED:
                    if (layer instanceof TileLayer) {
                        Tile newTile = ((TileLayer)layer).getTileAt(
                                tile.x, tile.y);
                        setCurrentTile(newTile);
                    } else if (layer instanceof ObjectGroup) {
                        // TODO: Add support for ObjectGroups here
                    }
                    break;
                case PS_MOVE:
                    Point translation = new Point(
                            tile.x - mousePressLocation.x,
                            tile.y - mousePressLocation.y);

                    layer.translate(translation.x, translation.y);
                    moveDist.translate(translation.x, translation.y);
                    mapView.repaint();
                    break;
                case PS_MARQUEE:
                    if (marqueeSelection != null) {
                        Point limp = mouseInitialPressLocation;
                        Rectangle oldArea =
                            marqueeSelection.getSelectedAreaBounds();
                        int minx = Math.min(limp.x, tile.x);
                        int miny = Math.min(limp.y, tile.y);

                        Rectangle selRect = new Rectangle(
                                minx, miny,
                                (Math.max(limp.x, tile.x) - minx)+1,
                                (Math.max(limp.y, tile.y) - miny)+1);

                        if (event.isShiftDown()) {
                            marqueeSelection.add(new Area(selRect));
                        } else if (event.isControlDown()) {
                            marqueeSelection.subtract(new Area(selRect));
                        } else {
                            marqueeSelection.selectRegion(selRect);
                        }
                        if (oldArea != null) {
                            oldArea.add(
                                    marqueeSelection.getSelectedAreaBounds());
                            mapView.repaintRegion(oldArea);
                        }
                    }
                    break;
            }
        }
    }

    public void mouseExited(MouseEvent e) {
        tileCoordsLabel.setText(" ");
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    	Point tile = mapView.screenToTileCoords(e.getX(), e.getY());
        mouseButton = e.getButton();
        bMouseIsDown = true;
        mousePressLocation = mapView.screenToTileCoords(e.getX(), e.getY());
        mouseInitialPressLocation = mousePressLocation;

        if (mouseButton == MouseEvent.BUTTON1) {
            switch (currentPointerState) {
                case PS_PAINT:
                	currentBrush.startPaint(currentMap, tile.x, tile.y, mouseButton, currentLayer);
                case PS_ERASE:
                case PS_POUR:
                    MapLayer layer = getCurrentLayer();
                    paintEdit =
                        new MapLayerEdit(layer, createLayerCopy(layer), null);
                    break;
                default:
            }
        }

        if (currentPointerState == PS_MARQUEE) {
            if (marqueeSelection == null) {
                marqueeSelection = new SelectionLayer(
                        currentMap.getWidth(), currentMap.getHeight());
                currentMap.addLayerSpecial(marqueeSelection);
            }
        } else if (currentPointerState == PS_MOVE) {
            // Initialize move distance to (0, 0)
            moveDist = new Point(0, 0);
        }

        doMouse(e);
    }

    public void mouseReleased(MouseEvent event) {
        mouseButton = MouseEvent.NOBUTTON;
        bMouseIsDown = false;
        MapLayer layer = getCurrentLayer();
        Point limp = mouseInitialPressLocation;

       if (currentPointerState == PS_MARQUEE) {
           Point tile = mapView.screenToTileCoords(event.getX(), event.getY());
           if (tile.y - limp.y == 0 && tile.x - limp.x == 0) {
               if (marqueeSelection != null) {
                   currentMap.removeLayerSpecial(marqueeSelection);
                   marqueeSelection = null;
               }
           }
        } else if (currentPointerState == PS_MOVE) {
            if (layer != null && moveDist.x != 0 || moveDist.x != 0) {
                undoSupport.postEdit(new MoveLayerEdit(layer, moveDist));
            }
        } else if (currentPointerState == PS_PAINT) {
        	currentBrush.endPaint();
        }

        if (paintEdit != null) {
            if (layer != null) {
                try {
                    MapLayer endLayer = paintEdit.getStart().createDiff(layer);
                    paintEdit.end(endLayer);
                    undoSupport.postEdit(paintEdit);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            paintEdit = null;
        }
    }

    public void mouseMoved(MouseEvent e) {
        if (bMouseIsDown) {
            doMouse(e);
        }

        Point tile = mapView.screenToTileCoords(e.getX(), e.getY());
        if (currentMap.inBounds(tile.x, tile.y)) {
            tileCoordsLabel.setText(String.valueOf(tile.x) + ", " + tile.y);
        } else {
            tileCoordsLabel.setText(" ");
        }

        updateCursorHighlight(tile);
    }

    public void mouseDragged(MouseEvent e) {
        mousePressLocation = mapView.screenToTileCoords(e.getX(), e.getY());
        Point tile = mapView.screenToTileCoords(e.getX(), e.getY());

        if (mouseButton == MouseEvent.BUTTON3 &&
                getCurrentLayer() instanceof TileLayer &&
                currentPointerState == PS_PAINT)
        {
            Point limp = mouseInitialPressLocation;
            int minx = Math.min(limp.x, tile.x);
            int miny = Math.min(limp.y, tile.y);
            Rectangle oldArea = null;

            if (currentBrush instanceof CustomBrush) {
                oldArea = ((CustomBrush)currentBrush).getBounds();
            }

            Rectangle bounds = new Rectangle(
                    minx, miny,
                    (Math.max(limp.x, tile.x) - minx)+1,
                    (Math.max(limp.y, tile.y) - miny)+1);

            // Right mouse button dragged: create and set custom brush
            MultilayerPlane mlp =
                new MultilayerPlane(bounds.width, bounds.height);
            TileLayer brushLayer = new TileLayer(bounds);
            brushLayer.copyFrom(getCurrentLayer());
            mlp.addLayer(brushLayer);
            setBrush(new CustomBrush(mlp));
        }
        else {
            doMouse(e);
        }

        if (currentMap.inBounds(tile.x, tile.y)) {
            tileCoordsLabel.setText(String.valueOf(tile.x) + ", " + tile.y);
        } else {
            tileCoordsLabel.setText(" ");
        }

        updateCursorHighlight(tile);
    }

    private void updateCursorHighlight(Point tile) {
        if (prefs.getBoolean("cursorhighlight", true)) {
            Rectangle redraw = cursorHighlight.getBounds();

            if (redraw.x != tile.x || redraw.y != tile.y) {
                Rectangle r1 = new Rectangle(tile.x, tile.y, 1, 1);
                Rectangle r2 = new Rectangle(redraw.x, redraw.y, 1, 1);
                cursorHighlight.setOffset(tile.x, tile.y);
                mapView.repaintRegion(r1);
                mapView.repaintRegion(r2);
            }
        }
    }

    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();

        if (command.equals("paint")) {
            setCurrentPointerState(PS_PAINT);
        } else if (command.equals("erase")) {
            setCurrentPointerState(PS_ERASE);
        } else if (command.equals("point")) {
            setCurrentPointerState(PS_POINT);
        } else if (command.equals("pour")) {
            setCurrentPointerState(PS_POUR);
        } else if (command.equals("eyed")) {
            setCurrentPointerState(PS_EYED);
        } else if (command.equals("marquee")) {
            setCurrentPointerState(PS_MARQUEE);
        } else if (command.equals("move")) {
            setCurrentPointerState(PS_MOVE);
        } else if (command.equals("moveobject")) {
            setCurrentPointerState(PS_MOVEOBJ);
        } else if (command.equals("palette")) {
            if (currentMap != null) {
                if (tilePaletteDialog == null) {
                    tilePaletteDialog =
                        new TilePaletteDialog(this, currentMap);
                }
                tilePaletteDialog.setVisible(true);
            }
        } else {
            handleEvent(event);
        }
    }

    private void handleEvent(ActionEvent event) {
        String command = event.getActionCommand();

        if (command.equals("Open...")) {
            if (checkSave()) {
                openMap();
            }
        } else if (command.equals("Exit")) {
            exit();
        } else if (command.equals("Close")) {
            if (checkSave()) {
                setCurrentMap(null);
            }
        } else if (command.equals("New...")) {
            if (checkSave()) {
                newMap();
            }
        } else if (command.equals("Print...")) {
            try {
                MapPrinter mp = new MapPrinter();
                mp.print(mapView);
            } catch (PrinterException e) {
                e.printStackTrace();
            }
        } else if (command.equals("Brush...")) {
            BrushDialog bd = new BrushDialog(this, appFrame, currentBrush);
            bd.setVisible(true);
        } else if (command.equals("Add Layer") ||
                command.equals("Duplicate Layer") ||
                command.equals("Delete Layer") ||
                command.equals("Move Layer Up") ||
                command.equals("Move Layer Down") ||
                command.equals("Merge Down") ||
                command.equals("Merge All")) {
            doLayerStateChange(event);
        } else if (command.equals("New Tileset...")) {
            if (currentMap != null) {
                NewTilesetDialog dialog =
                    new NewTilesetDialog(appFrame, currentMap);
                TileSet newSet = dialog.create();
                if (newSet != null) {
                    currentMap.addTileset(newSet);
                }
            }
        } else if (command.equals("Import Tileset...")) {
            if (currentMap != null) {
                JFileChooser ch = new JFileChooser(currentMap.getFilename());
                MapReader[] readers = pluginLoader.getReaders();
                for (int i = 0; i < readers.length; i++) {
                    try {
                        ch.addChoosableFileFilter(new TiledFileFilter(
                                    readers[i].getFilter(),
                                    readers[i].getName()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                ch.addChoosableFileFilter(
                        new TiledFileFilter(TiledFileFilter.FILTER_TSX));

                int ret = ch.showOpenDialog(appFrame);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    String filename = ch.getSelectedFile().getAbsolutePath();
                    try {
                        TileSet set = MapHelper.loadTileset(filename);
                        currentMap.addTileset(set);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (command.equals("Tileset Manager")) {
            if (currentMap != null) {
                TilesetManager manager = new TilesetManager(appFrame, currentMap);
                manager.setVisible(true);
            }
        } else if (command.equals("Save")) {
            if (currentMap != null) {
                saveMap(currentMap.getFilename(), false);
            }
        } else if (command.equals("Save as...")) {
            if (currentMap != null) {
                saveMap(currentMap.getFilename(), true);
            }
        } else if (command.equals("Save as Image...")) {
            if (currentMap != null) {
                saveMapImage(null);
            }
        } else if (command.equals("Properties")) {
            PropertiesDialog pd = new PropertiesDialog(appFrame,
                    currentMap.getProperties());
            pd.setTitle("Map Properties");
            pd.getProps();
        } else if (command.equals("Layer Properties")) {
            MapLayer layer = getCurrentLayer();
            PropertiesDialog lpd =
                new PropertiesDialog(appFrame, layer.getProperties());
            lpd.setTitle(layer.getName() + " Properties");
            lpd.getProps();
        } else if (command.equals("Show Boundaries") ||
                command.equals("Hide Boundaries")) {
            mapView.toggleMode(MapView.PF_BOUNDARYMODE);
        } else if (command.equals("Show Grid")) {
            // Toggle grid
            mapView.toggleMode(MapView.PF_GRIDMODE);
        } else if (command.equals("Show Coordinates")) {
            // Toggle coordinates
            mapView.toggleMode(MapView.PF_COORDINATES);
            mapView.repaint();
        } else if (command.equals("Highlight Cursor")) {
            prefs.putBoolean("cursorhighlight", cursorMenuItem.isSelected());
            cursorHighlight.setVisible(cursorMenuItem.isSelected());
        } else if (command.equals("Resize")) {
            ResizeDialog rd = new ResizeDialog(appFrame, this);
            rd.setVisible(true);
        }  else if (command.equals("Search")) {
            SearchDialog sd = new SearchDialog(appFrame, currentMap);
            sd.setVisible(true);
        } else if (command.equals("About Tiled")) {
            if (aboutDialog == null) {
                aboutDialog = new AboutDialog(appFrame);
            }
            aboutDialog.setVisible(true);
        } else if (command.equals("About Plug-ins")) {
            PluginDialog pluginDialog =
                new PluginDialog(appFrame, pluginLoader);
            pluginDialog.setVisible(true);
        } else if (command.startsWith("_open")) {
            Preferences recentFiles = prefs.node("recent");
            String file = recentFiles.get("file" + command.substring(5), "");
            if (file.length() > 0) {
                loadMap(file);
            }
        } else if (command.equals("Preferences...")) {
            ConfigurationDialog d = new ConfigurationDialog(appFrame);
            d.configure();
        } else {
            System.out.println(event);
        }
    }

    public void componentHidden(ComponentEvent event) {
    }

    public void componentMoved(ComponentEvent event) {
    }

    public void componentResized(ComponentEvent event) {
        // This can currently only happen when the map changes size
        String s = String.valueOf((int) (mapView.getZoom() * 100)) + "%";
        zoomLabel.setText(s);
    }

    public void componentShown(ComponentEvent event) {
    }

    public void mapChanged(MapChangedEvent e) {
        if (e.getMap() == currentMap) {
            mapScrollPane.setViewportView(mapView);
            updateLayerTable();
            if (tilePaletteDialog != null) {
                tilePaletteDialog.setMap(currentMap);
            }
            mapView.repaint();
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        int selectedRow = layerTable.getSelectedRow();

        // At the moment, this can only be a new layer selection
        if (currentMap != null && selectedRow >= 0) {
            currentLayer = currentMap.getTotalLayers() - selectedRow - 1;

            float opacity = getCurrentLayer().getOpacity();
            opacitySlider.setValue((int)(opacity * 100));
        } else {
            currentLayer = -1;
        }

        updateLayerOperations();
    }

    public void stateChanged(ChangeEvent e) {
        // At the moment, this can only be movement in the opacity slider

        if (currentMap != null && currentLayer >= 0) {
            MapLayer layer = getCurrentLayer();
            layer.setOpacity(opacitySlider.getValue() / 100.0f);

            /*MapLayerStateEdit mlse = new MapLayerStateEdit(currentMap);
            mlse.setPresentationName("Opacity Change");
            undoSupport.postEdit(mlse);*/
        }
    }

    private class UndoAction extends AbstractAction {
        public UndoAction() {
            super("Undo");
            putValue(SHORT_DESCRIPTION, "Undo one action");
            putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke("control Z"));
        }
        public void actionPerformed(ActionEvent evt) {
            undoStack.undo();
            updateHistory();
            mapView.repaint();
        }
    }

    private class RedoAction extends AbstractAction {
        public RedoAction() {
            super("Redo");
            putValue(SHORT_DESCRIPTION, "Redo one action");
            putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke("control Y"));
        }
        public void actionPerformed(ActionEvent evt) {
            undoStack.redo();
            updateHistory();
            mapView.repaint();
        }
    }

    private class LayerTransformAction extends AbstractAction {
        private final int transform;
        public LayerTransformAction(int transform) {
            this.transform = transform;
            switch (transform) {
                case MapLayer.ROTATE_90:
                    putValue(NAME, "Rotate 90 degrees CW");
                    putValue(SHORT_DESCRIPTION,
                            "Rotate layer 90 degrees clockwise");
                    putValue(SMALL_ICON,
                            Resources.getIcon("gimp-rotate-90-16.png"));
                    break;
                case MapLayer.ROTATE_180:
                    putValue(NAME, "Rotate 180 degrees CW");
                    putValue(SHORT_DESCRIPTION,
                            "Rotate layer 180 degrees clockwise");
                    putValue(SMALL_ICON,
                            Resources.getIcon("gimp-rotate-180-16.png"));
                    break;
                case MapLayer.ROTATE_270:
                    putValue(NAME, "Rotate 90 degrees CCW");
                    putValue(SHORT_DESCRIPTION,
                            "Rotate layer 90 degrees counterclockwise");
                    putValue(SMALL_ICON,
                            Resources.getIcon("gimp-rotate-270-16.png"));
                    break;
                case MapLayer.MIRROR_VERTICAL:
                    putValue(NAME, "Flip vertically");
                    putValue(SHORT_DESCRIPTION, "Flip layer vertically");
                    putValue(SMALL_ICON,
                            Resources.getIcon("gimp-flip-vertical-16.png"));
                    break;
                case MapLayer.MIRROR_HORIZONTAL:
                    putValue(NAME, "Flip horizontally");
                    putValue(SHORT_DESCRIPTION, "Flip layer horizontally");
                    putValue(SMALL_ICON,
                            Resources.getIcon("gimp-flip-horizontal-16.png"));
                    break;
            }
        }
        public void actionPerformed(ActionEvent evt) {
            MapLayer currentLayer = getCurrentLayer();
            MapLayer layer = currentLayer;
            MapLayerEdit transEdit;
            transEdit = new MapLayerEdit(
                    currentLayer, createLayerCopy(currentLayer));

            if (marqueeSelection != null) {
                if (currentLayer instanceof TileLayer) {
                    layer = new TileLayer(
                            marqueeSelection.getSelectedAreaBounds());
                } else if (currentLayer instanceof ObjectGroup) {
                    layer = new ObjectGroup(
                            marqueeSelection.getSelectedAreaBounds());
                }
                layer.setMap(currentMap);
                layer.maskedCopyFrom(
                        currentLayer,
                        marqueeSelection.getSelectedArea());
            }

            switch (transform) {
                case MapLayer.ROTATE_90:
                case MapLayer.ROTATE_180:
                case MapLayer.ROTATE_270:
                    transEdit.setPresentationName("Rotate");
                    layer.rotate(transform);
                    //if(marqueeSelection != null) marqueeSelection.rotate(transform);
                    break;
                case MapLayer.MIRROR_VERTICAL:
                    transEdit.setPresentationName("Vertical Flip");
                    layer.mirror(MapLayer.MIRROR_VERTICAL);
                    //if(marqueeSelection != null) marqueeSelection.mirror(transform);
                    break;
                case MapLayer.MIRROR_HORIZONTAL:
                    transEdit.setPresentationName("Horizontal Flip");
                    layer.mirror(MapLayer.MIRROR_HORIZONTAL);
                    //if(marqueeSelection != null) marqueeSelection.mirror(transform);
                    break;
            }

            if (marqueeSelection != null ) {
                layer.mergeOnto(currentLayer);
            }

            transEdit.end(createLayerCopy(currentLayer));
            undoSupport.postEdit(transEdit);
            mapView.repaint();
        }
    }

    private class CancelSelectionAction extends AbstractAction {
        public CancelSelectionAction() {
            super("None");
            putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke("control shift A"));
            putValue(SHORT_DESCRIPTION, "Cancel selection");
        }

        public void actionPerformed(ActionEvent e) {
            if (currentMap != null) {
                if (marqueeSelection != null) {
                    currentMap.removeLayerSpecial(marqueeSelection);
                }

                marqueeSelection = null;
            }
        }
    }

    private class SelectAllAction extends AbstractAction {
        public SelectAllAction() {
            super("All");
            putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke("control A"));
            putValue(SHORT_DESCRIPTION, "Select entire map");
        }

        public void actionPerformed(ActionEvent e) {
            if (currentMap != null) {
                if (marqueeSelection != null) {
                    currentMap.removeLayerSpecial(marqueeSelection);
                }
                marqueeSelection = new SelectionLayer(
                        currentMap.getWidth(), currentMap.getHeight());
                marqueeSelection.selectRegion(marqueeSelection.getBounds());
                currentMap.addLayerSpecial(marqueeSelection);
            }
        }
    }

    private class InverseSelectionAction extends AbstractAction {
        public InverseSelectionAction() {
            super("Invert");
            putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke("control I"));
            putValue(SHORT_DESCRIPTION, "Inverse of the current selection");
        }

        public void actionPerformed(ActionEvent e) {
            if (marqueeSelection != null) {
                marqueeSelection.invert();
                mapView.repaint();
            }
        }
    }

    private class ZoomInAction extends AbstractAction {
        public ZoomInAction() {
            super("Zoom In");
            putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke("control EQUALS"));
            putValue(SHORT_DESCRIPTION, "Zoom in one level");
            putValue(SMALL_ICON, Resources.getIcon("gnome-zoom-in.png"));
        }
        public void actionPerformed(ActionEvent evt) {
            if (currentMap != null) {
                zoomOutAction.setEnabled(true);
                if (!mapView.zoomIn()) {
                    setEnabled(false);
                }
                zoomNormalAction.setEnabled(mapView.getZoomLevel() !=
                        MapView.ZOOM_NORMALSIZE);
            }
        }
    }

    private class ZoomOutAction extends AbstractAction {
        public ZoomOutAction() {
            super("Zoom Out");
            putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke("control MINUS"));
            putValue(SHORT_DESCRIPTION, "Zoom out one level");
            putValue(SMALL_ICON, Resources.getIcon("gnome-zoom-out.png"));
        }
        public void actionPerformed(ActionEvent evt) {
            if (currentMap != null) {
                zoomInAction.setEnabled(true);
                if (!mapView.zoomOut()) {
                    setEnabled(false);
                }
                zoomNormalAction.setEnabled(mapView.getZoomLevel() !=
                        MapView.ZOOM_NORMALSIZE);
            }
        }
    }

    private class ZoomNormalAction extends AbstractAction {
        public ZoomNormalAction() {
            super("Zoom Normalsize");
            putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke("control 0"));
            putValue(SHORT_DESCRIPTION, "Zoom 100%");
        }
        public void actionPerformed(ActionEvent evt) {
            if (currentMap != null) {
                zoomInAction.setEnabled(true);
                zoomOutAction.setEnabled(true);
                setEnabled(false);
                mapView.setZoomLevel(MapView.ZOOM_NORMALSIZE);
            }
        }
    }

    private class CopyAction extends AbstractAction {
        public CopyAction() {
            super("Copy");
            putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke("control C"));
            putValue(SHORT_DESCRIPTION, "Copy");
        }
        public void actionPerformed(ActionEvent evt) {
            if (currentMap != null && marqueeSelection != null) {
                if (getCurrentLayer() instanceof TileLayer) {
                    clipboardLayer = new TileLayer(
                            marqueeSelection.getSelectedAreaBounds());
                } else if (getCurrentLayer() instanceof ObjectGroup) {
                    clipboardLayer = new ObjectGroup(
                            marqueeSelection.getSelectedAreaBounds());
                }
                clipboardLayer.maskedCopyFrom(
                        getCurrentLayer(),
                        marqueeSelection.getSelectedArea());
            }
        }
    }

    private class CutAction extends AbstractAction {
        public CutAction() {
            super("Cut");
            putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke("control X"));
            putValue(SHORT_DESCRIPTION, "Cut");
        }
        public void actionPerformed(ActionEvent evt) {
            if (currentMap != null && marqueeSelection != null) {
                MapLayer ml = getCurrentLayer();

                if (getCurrentLayer() instanceof TileLayer) {
                    clipboardLayer = new TileLayer(
                            marqueeSelection.getSelectedAreaBounds());
                } else if (getCurrentLayer() instanceof ObjectGroup) {
                    clipboardLayer = new ObjectGroup(
                            marqueeSelection.getSelectedAreaBounds());
                }
                clipboardLayer.maskedCopyFrom(
                        ml, marqueeSelection.getSelectedArea());

                Rectangle area = marqueeSelection.getSelectedAreaBounds();
                Area mask = marqueeSelection.getSelectedArea();
                if (ml instanceof TileLayer) {
                    TileLayer tl = (TileLayer)ml;
                    for (int i = area.y; i < area.height+area.y; i++) {
                        for (int j = area.x; j < area.width + area.x; j++){
                            if (mask.contains(j,i)) {
                                tl.setTileAt(j, i, null);
                            }
                        }
                    }
                }
                mapView.repaintRegion(area);
            }
        }
    }

    private class PasteAction extends AbstractAction {
        public PasteAction() {
            super("Paste");
            putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke("control V"));
            putValue(SHORT_DESCRIPTION, "Paste");
        }
        public void actionPerformed(ActionEvent evt) {
            if (currentMap != null && clipboardLayer != null) {
                Vector layersBefore = currentMap.getLayerVector();
                MapLayer ml = createLayerCopy(clipboardLayer);
                ml.setName("Layer " + currentMap.getTotalLayers());
                currentMap.addLayer(ml);
                undoSupport.postEdit(
                        new MapLayerStateEdit(currentMap, layersBefore,
                            new Vector(currentMap.getLayerVector()),
                            "Paste Selection"));
            }
        }
    }

    private class UndoAdapter implements UndoableEditListener {
        public void undoableEditHappened(UndoableEditEvent evt) {
            undoStack.addEdit(evt.getEdit());
            updateHistory();
        }
    }

    private void pour(TileLayer layer, int x, int y,
            Tile newTile, Tile oldTile) {
        if (newTile == oldTile || layer.getLocked()) return;

        Rectangle area;
        TileLayer before = new TileLayer(layer);
        TileLayer after;

        if (marqueeSelection == null) {
            area = new Rectangle(new Point(x, y));
            Stack stack = new Stack();

            stack.push(new Point(x, y));
            while (!stack.empty()) {
                // Remove the next tile from the stack
                Point p = (Point)stack.pop();

                // If the tile it meets the requirements, set it and push its
                // neighbouring tiles on the stack.
                if (layer.contains(p.x, p.y) &&
                        layer.getTileAt(p.x, p.y) == oldTile)
                {
                    layer.setTileAt(p.x, p.y, newTile);
                    area.add(p);

                    stack.push(new Point(p.x, p.y - 1));
                    stack.push(new Point(p.x, p.y + 1));
                    stack.push(new Point(p.x + 1, p.y));
                    stack.push(new Point(p.x - 1, p.y));
                }
            }
        } else {
            if (marqueeSelection.getSelectedArea().contains(x, y)) {
                area = marqueeSelection.getSelectedAreaBounds();
                for (int i = area.y; i < area.height+area.y; i++) {
                    for (int j = area.x;j<area.width+area.x;j++){
                        if (marqueeSelection.getSelectedArea().contains(j, i)){
                            layer.setTileAt(j, i, newTile);
                        }
                    }
                }
            } else {
                return;
            }
        }

        Rectangle bounds = new Rectangle(
                area.x, area.y, area.width + 1, area.height + 1);
        after = new TileLayer(bounds);
        after.copyFrom(layer);

        MapLayerEdit mle = new MapLayerEdit(layer, before, after);
        mle.setPresentationName("Fill");
        undoSupport.postEdit(mle);
    }

    public void setBrush(AbstractBrush b) {
        currentBrush = b;
    }

    private void updateTitle() {
        String title = "Tiled";

        if (currentMap != null) {
            String filename = currentMap.getFilename();
            title += " - ";
            if (filename != null) {
                title += currentMap.getFilename();
            } else {
                title += "Untitled";
            }
            if (unsavedChanges()) {
                title += "*";
            }
        }

        appFrame.setTitle(title);
    }

    private boolean checkSave() {
        if (unsavedChanges()) {
            int ret = JOptionPane.showConfirmDialog(appFrame,
                    "There are unsaved changes for the current map. " +
                    "Save changes?",
                    "Save Changes?", JOptionPane.YES_NO_CANCEL_OPTION);

            if (ret == JOptionPane.YES_OPTION) {
                saveMap(currentMap.getFilename(), true);
            } else if (ret == JOptionPane.CANCEL_OPTION){
                return false;
            }
        }
        return true;
    }

    private boolean unsavedChanges() {
        return currentMap != null && undoStack.canUndo() &&
                !undoStack.isAllSaved();
    }

    /**
     * Loads a map.
     *
     * @param file filename of map to load
     * @return <code>true</code> if the file was loaded, <code>false</code> if
     *         an error occured
     */
    public boolean loadMap(String file) {
        try {
            Map m = MapHelper.loadMap(file);

            if (m != null) {
                setCurrentMap(m);
                updateRecent(file);
                //This is to try and clean up any previously loaded stuffs
            	System.gc();
                return true;
            } else {
                JOptionPane.showMessageDialog(appFrame,
                        "Unsupported map format", "Error while loading map",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(appFrame,
                    "Error while loading " + file + ": " +
                    e.getMessage() + (e.getCause() != null ? "\nCause: " +
                        e.getCause().getMessage() : ""),
                    "Error while loading map",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Saves the current map, optionally with a "Save As" dialog. If
     * <code>filename</code> is <code>null</code> or <code>bSaveAs</code> is
     * passed <code>true</code>, a "Save As" dialog is opened.
     *
     * @see MapHelper#saveMap(Map, String)
     * @param filename Filename to save the current map to.
     * @param bSaveAs  Pass <code>true</code> to ask for a new filename using
     *                 a "Save As" dialog.
     */
    public void saveMap(String filename, boolean bSaveAs) {
    	
    	TiledFileFilter saver = new TiledFileFilter(TiledFileFilter.FILTER_EXT);
    	boolean saveOk = false;
    	JFileChooser ch = null;
    	
    	try {
	    	while(!saveOk) {
		        if (bSaveAs || filename == null) {
		
		        	if(ch == null) {
			            if (filename == null) {
			                ch = new JFileChooser();
			            } else {
			                ch = new JFileChooser(filename);
			            }
			
			            MapWriter[] writers = pluginLoader.getWriters();
			            for(int i = 0; i < writers.length; i++) {
			                try {
			                    ch.addChoosableFileFilter(new TiledFileFilter(writers[i]));
			                } catch (Exception e) {
			                    e.printStackTrace();
			                }
			            }
			
			            ch.addChoosableFileFilter(
			                    new TiledFileFilter(TiledFileFilter.FILTER_TMX));
			
			            ch.addChoosableFileFilter(saver);
		        	}
		        	
		            if (ch.showSaveDialog(appFrame) == JFileChooser.APPROVE_OPTION) {
		                filename = ch.getSelectedFile().getAbsolutePath();
		                saver = (TiledFileFilter) ch.getFileFilter();
		            } else {
		                // User cancelled operation, do nothing
		                return;
		            }
		            
		            // Make sure that the file has an extension. If not, append extension
		            // chosen from dropdown.
		            // NOTE: we can't know anything more than the filename has at least
		            //		 one '.' in it, or at least we won't go to the trouble...
		            if (filename.lastIndexOf('.') == -1) {
		            	if(saver.getType() == TiledFileFilter.FILTER_EXT) {
		            		//impossible to tell
		            		JOptionPane.showMessageDialog(appFrame, "Save failed, unknown type");
		            		continue;
		            	}
		            	filename = filename.concat("."+saver.getFirstExtention());
		            }
		        }
		        
		        
	            // Check if file exists
	            File exist = new File(filename);
	            if (exist.exists() && bSaveAs) {
	                int result = JOptionPane.showConfirmDialog(appFrame,
	                        "The file already exists. Are you sure you want to " +
	                        "overwrite it?", "Overwrite file?",
	                        JOptionPane.YES_NO_OPTION);
	                if (result != JOptionPane.OK_OPTION) {
	                    continue;
	                }
	            }
	            
	            // Do we want to just go by extention?
	            if(saver.getType() == TiledFileFilter.FILTER_EXT) {
	            	MapHelper.saveMap(currentMap, filename);
	            } else {
	                // Check that chosen plugin and extension match.
	            	// If they don't, ask the user if they want to shoot themselves in the foot
	                if(!saver.accept(exist)) {
	                	int result = JOptionPane.showConfirmDialog(appFrame,
	                            "The file extension does not match the plugin."+
	                            " Do you wish to continue?",
	                            "Force save?",
	                            JOptionPane.YES_NO_OPTION);
	                	if (result != JOptionPane.OK_OPTION) {
		                    continue;
		                }
	                }
	                
	                MapHelper.saveMap(currentMap, saver.getPlugin(), filename);
	            }
	            
	            currentMap.setFilename(filename);
	            updateRecent(filename);
	            undoStack.commitSave();
	            updateTitle();
	            saveOk = true;
	    	}
    	} catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(appFrame,
                    "Error while attempting to save " + filename + ": " + e.toString(),
                    "Error while saving map",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Attempts to draw the entire map to an image file
     * of the format of the extension. (filename.ext)
     *
     * @param filename Image filename to save map render to.
     */
    public void saveMapImage(String filename) {
        if (filename == null) {
            JFileChooser ch = new JFileChooser();
            ch.setDialogTitle("Save as image");

            if (ch.showSaveDialog(appFrame) == JFileChooser.APPROVE_OPTION) {
                filename = ch.getSelectedFile().getAbsolutePath();
            }
        }

        if (filename != null) {
            MapView myView = MapView.createViewforMap(currentMap);
            if (mapView.getMode(MapView.PF_GRIDMODE))
                myView.enableMode(MapView.PF_GRIDMODE);
            myView.enableMode(MapView.PF_NOSPECIAL);
            myView.setZoom(mapView.getZoom());
            Dimension d = myView.getPreferredSize();
            BufferedImage i = new BufferedImage(d.width, d.height,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = i.createGraphics();
            g.setClip(0, 0, d.width, d.height);
            myView.paint(g);

            String format = filename.substring(filename.lastIndexOf('.') + 1);

            try {
                ImageIO.write(i, format, new File(filename));
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(appFrame,
                        "Error while saving " + filename + ": " + e.toString(),
                        "Error while saving map image",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openMap() {
        // Start at the location of the most recently loaded map file
        String startLocation = prefs.node("recent").get("file0", "");

        JFileChooser ch = new JFileChooser(startLocation);

        try {
            MapReader[] readers = pluginLoader.getReaders();
            for(int i = 0; i < readers.length; i++) {
                ch.addChoosableFileFilter(new TiledFileFilter(
                            readers[i].getFilter(), readers[i].getName()));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(appFrame,
                    "Error while loading plugins: " + e.getMessage(),
                    "Error while loading map",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        ch.addChoosableFileFilter(
                new TiledFileFilter(TiledFileFilter.FILTER_TMX));

        int ret = ch.showOpenDialog(appFrame);
        if (ret == JFileChooser.APPROVE_OPTION) {
            loadMap(ch.getSelectedFile().getAbsolutePath());
        }
    }

    private void newMap() {
        NewMapDialog nmd = new NewMapDialog(appFrame);
        Map newMap = nmd.create();
        if (newMap != null) {
            setCurrentMap(newMap);
        }
    }

    private static MapLayer createLayerCopy(MapLayer layer) {
        if (layer instanceof TileLayer) {
            return new TileLayer((TileLayer)layer);
        } else if (layer instanceof ObjectGroup) {
            return new ObjectGroup((ObjectGroup)layer);
        }
        return null;
    }

    private void updateRecent(String filename) {
        // If a filename is given, add it to the recent files
        if (filename != null) {
            TiledConfiguration.addToRecentFiles(filename);
        }

        java.util.List files = TiledConfiguration.getRecentFiles();

        recentMenu.removeAll();

        for (int i = 0; i < files.size(); i++) {
            String path = (String) files.get(i);
            String name =
                path.substring(path.lastIndexOf(File.separatorChar) + 1);

            JMenuItem recentOption = createMenuItem(name, null, null);
            recentOption.setActionCommand("_open" + i);
            recentMenu.add(recentOption);
        }
    }

    private void setCurrentMap(Map newMap) {
        currentMap = newMap;
        boolean mapLoaded = currentMap != null;

        if (!mapLoaded) {
            mapEventAdapter.fireEvent(MapEventAdapter.ME_MAPINACTIVE);
            mapView = null;
            mapScrollPane.setViewportView(Box.createRigidArea(
                        new Dimension(0,0)));
            setCurrentPointerState(PS_POINT);
            tileCoordsLabel.setPreferredSize(null);
            tileCoordsLabel.setText(" ");
            zoomLabel.setText(" ");
            tilePalettePanel.setTilesets(null);
            setCurrentTile(null);
            System.gc();
        } else {
            mapEventAdapter.fireEvent(MapEventAdapter.ME_MAPACTIVE);
            mapView = MapView.createViewforMap(currentMap);
            mapView.addMouseListener(this);
            mapView.addMouseMotionListener(this);
            mapView.addComponentListener(this);
            JViewport mapViewport = new JViewport();
            mapViewport.setView(mapView);
            mapScrollPane.setViewport(mapViewport);
            setCurrentPointerState(PS_PAINT);

            currentMap.addMapChangeListener(this);

            gridMenuItem.setState(mapView.getMode(MapView.PF_GRIDMODE));
            coordinatesMenuItem.setState(
                    mapView.getMode(MapView.PF_COORDINATES));

            Vector tilesets = currentMap.getTilesets();
            if (!tilesets.isEmpty()) {
                tilePalettePanel.setTilesets(tilesets);
                TileSet first = (TileSet)tilesets.get(0);
                setCurrentTile(first.getFirstTile());
            } else {
                tilePalettePanel.setTilesets(null);
                setCurrentTile(null);
            }

            tileCoordsLabel.setText(String.valueOf(currentMap.getWidth() - 1)
                    + ", " + (currentMap.getHeight() - 1));
            tileCoordsLabel.setPreferredSize(null);
            Dimension size = tileCoordsLabel.getPreferredSize();
            tileCoordsLabel.setText(" ");
            tileCoordsLabel.setMinimumSize(size);
            tileCoordsLabel.setPreferredSize(size);
            zoomLabel.setText(
                    String.valueOf((int) (mapView.getZoom() * 100)) + "%");
        }

        zoomInAction.setEnabled(mapLoaded);
        zoomOutAction.setEnabled(mapLoaded);
        zoomNormalAction.setEnabled(mapLoaded && mapView.getZoomLevel() !=
                MapView.ZOOM_NORMALSIZE);

        if (tilePaletteDialog != null) {
            tilePaletteDialog.setMap(currentMap);
        }

        /*
        if (miniMap != null && currentMap != null) {
            miniMap.setView(MapView.createViewforMap(currentMap));
        }
        */

        if (currentMap != null) {
            currentMap.addLayerSpecial(cursorHighlight);
        }

        undoStack.discardAllEdits();
        updateLayerTable();
        updateTitle();
        updateHistory();
    }

    private void setCurrentLayer(int index) {
        if (currentMap != null) {
            int totalLayers = currentMap.getTotalLayers();
            if (totalLayers > index && index >= 0) {
                /*
                if (paintEdit != null) {
                    MapLayer layer = getCurrentLayer();
                    try {
                        MapLayer endLayer =
                            paintEdit.getStart().createDiff(layer);
                        if (endLayer != null) {
                            endLayer.setId(layer.getId());
                            endLayer.setOffset(layer.getBounds().x,layer.getBounds().y);
                        }
                        paintEdit.end(endLayer);
                        undoSupport.postEdit(paintEdit);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                */
                currentLayer = index;
                layerTable.changeSelection(totalLayers - currentLayer - 1, 0,
                        false, false);
            }
        }
    }

    /**
     * Changes the currently selected tile.
     *
     * @param tile the new tile to be selected
     */
    public void setCurrentTile(Tile tile) {
        if (currentTile != tile) {
            currentTile = tile;
            if (!(currentBrush instanceof CustomBrush)) {
                ((ShapeBrush)currentBrush).setTile(tile);
            }
            tilePaletteButton.setTile(currentTile);
        }
    }

    private void setCurrentPointerState(int state) {
        /*if(currentPointerState == PS_MARQUEE && state != PS_MARQUEE) {
            // Special logic for selection
            if (marqueeSelection != null) {
                currentMap.removeLayerSpecial(marqueeSelection);
                marqueeSelection = null;
            }
        }*/

        currentPointerState = state;

        // Select the matching button
        paintButton.setSelected(state == PS_PAINT);
        eraseButton.setSelected(state == PS_ERASE);
        pourButton.setSelected(state == PS_POUR);
        eyedButton.setSelected(state == PS_EYED);
        marqueeButton.setSelected(state == PS_MARQUEE);
        moveButton.setSelected(state == PS_MOVE);
        objectMoveButton.setSelected(state == PS_MOVEOBJ);

        // Set the matching cursor
        if (mapView != null) {
            switch (currentPointerState) {
                case PS_PAINT:
                    mapView.setCursor(curPaint);
                    break;
                case PS_ERASE:
                    mapView.setCursor(curErase);
                    break;
                case PS_POINT:
                    mapView.setCursor(curDefault);
                    break;
                case PS_POUR:
                    mapView.setCursor(curPour);
                    break;
                case PS_EYED:
                    mapView.setCursor(curEyed);
                    break;
                case PS_MARQUEE:
                    mapView.setCursor(curMarquee);
                    break;
            }
        }
    }

    /**
     * Starts Tiled.
     *
     * @param args the first argument may be a map file
     */
    public static void main(String[] args) {
        MapEditor editor = new MapEditor();

        if (args.length > 0) {
            String toLoad = args[0];
            if (!Util.checkRoot(toLoad) || toLoad.startsWith(".")) {
                if (toLoad.startsWith(".")) {
                    toLoad = toLoad.substring(1);
                }
                toLoad = System.getProperty("user.dir") +
                    File.separatorChar + toLoad;
            }
            editor.loadMap(toLoad);
        }
    }
}
