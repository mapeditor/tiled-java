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

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.io.*;
import java.util.Stack;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.undo.UndoableEditSupport;

import tiled.core.*;
import tiled.view.*;
import tiled.mapeditor.brush.*;
import tiled.mapeditor.util.*;
import tiled.mapeditor.widget.*;
import tiled.mapeditor.undo.*;
import tiled.util.TiledConfiguration;
import tiled.io.MapReader;
import tiled.io.MapWriter;
import tiled.io.xml.XMLMapTransformer;
import tiled.io.xml.XMLMapWriter;


/**
 * The main class for the Tiled Map Editor.
 */
public class MapEditor implements ActionListener,
    MouseListener, MouseMotionListener, MapChangeListener,
    ListSelectionListener, ChangeListener, ComponentListener
{
    // Constants and the like
    protected static final int PS_POINT   = 0;
    protected static final int PS_PAINT   = 1;
    protected static final int PS_ERASE   = 2;
    protected static final int PS_POUR    = 3;
    protected static final int PS_EYED    = 4;
    protected static final int PS_MARQUEE = 5;
    protected static final int PS_MOVE    = 6;

    private Cursor curDefault = null;
    private Cursor curPaint   = null;
    private Cursor curErase   = null;
    private Cursor curPour    = null;
    private Cursor curEyed    = null;
    private Cursor curMarquee = null;

    public static final String version = "0.4.1 WIP";

    // Basic stuff for the Applet
    Map currentMap;
    MapView mapView;
    UndoStack undoStack;
    UndoableEditSupport undoSupport;
    private MapEventAdapter mapEventAdapter;

    int currentPointerState;
    Tile currentTile;
    int currentLayer = -1;
    boolean bMouseIsDown = false;
    Point mousePressLocation;
    int mouseButton;
    Brush currentBrush;

    // GUI components
    JPanel      mainPanel;
    JPanel      toolPanel;
    JPanel      dataPanel;
    JPanel      statusBar;
    JMenuBar    menuBar;
    JMenuItem   undoMenuItem, redoMenuItem;
    JCheckBoxMenuItem gridMenuItem, boundaryMenuItem;
    JMenuItem   layerAdd, layerClone, layerDel;
    JMenuItem   layerUp, layerDown;
    JMenuItem   layerMerge, layerMergeAll;
    JMenu       recentMenu;
    JScrollPane mapScrollPane;
    JTable      layerTable;
    JList		editHistoryList;

    TileButton  tilePaletteButton;
    JFrame      appFrame;
    JSlider     opacitySlider;
    JLabel      zoomLabel, tileCoordsLabel;

    AbstractButton layerAddButton, layerCloneButton, layerDelButton;
    AbstractButton layerUpButton, layerDownButton;
    AbstractButton paintButton, eraseButton, pourButton;
    AbstractButton eyedButton, marqueeButton, moveButton;

    TilePalettePanel tilePalettePanel;
    TilePaletteDialog tilePaletteDialog;
    AboutDialog aboutDialog;
    MapLayerEdit paintEdit;

    // Actions
    Action zoomInAction, zoomOutAction, zoomNormalAction;
    Action undoAction, redoAction;
    Action rot90Action, rot180Action, rot270Action;
    Action flipHorAction, flipVerAction;


    public MapEditor() {
        /*
        try {
            Image imgPaintCursor = loadImageResource(
                    "resources/cursor-pencil.png");

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

        mapEventAdapter = new MapEventAdapter();
        currentBrush = new ShapeBrush();
        ((ShapeBrush)currentBrush).makeQuadBrush(new Rectangle(0, 0, 1, 1));

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

        // Create our frame
        appFrame = new JFrame("Tiled");
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        appFrame.setContentPane(createContentPane());
        createMenuBar();
        appFrame.setJMenuBar(menuBar);
        appFrame.setSize(600, 400);

        setCurrentMap(null);
        updateRecent(null);

        appFrame.show();
    }

    private JPanel createContentPane() {
        createToolbox();
        createData();
        createStatusBar();

        mapScrollPane = new JScrollPane(
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(toolPanel, BorderLayout.WEST);
        mainPanel.add(mapScrollPane);
        mainPanel.add(dataPanel, BorderLayout.EAST);
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        return mainPanel;
    }

    /**
     * Loads a map.
     *
     * @param file Filename of map to load.
     */
    public void loadMap(String file) {
        try {
            if (file.substring(
                        file.lastIndexOf('.') + 1).equalsIgnoreCase("tmx")) {
                MapReader mr = new XMLMapTransformer();
                setCurrentMap(mr.readMap(file));
                updateRecent(file);
            } else {
                JOptionPane.showMessageDialog(appFrame,
                        "Unsupported map format", "Error while loading map",
                        JOptionPane.ERROR_MESSAGE);
        	}
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(appFrame,
                    "Error while loading " + file + ": " +
                    e.getMessage(), "Error while loading map",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    /**
     * Creates all the menus and submenus of the top menu bar. Handles
     * assigning listeners and tooltips as well.
     */
    private void createMenuBar() {
        JMenu m;

        menuBar = new JMenuBar();

        JMenuItem save = createMenuItem("Save", null, "Save current map",
                "control S");
        JMenuItem saveAs = createMenuItem("Save as...", null,
                "Save current map as new file", "control shift S");
        JMenuItem close = createMenuItem("Close", null, "Close this map",
                "control W");
        JMenuItem print =
            createMenuItem("Print...", null, "Print the map", "control P");
        recentMenu = new JMenu("Open Recent");

        mapEventAdapter.addListener(save);
        mapEventAdapter.addListener(saveAs);
        mapEventAdapter.addListener(close);
        mapEventAdapter.addListener(print);

        m = new JMenu("File");
        m.add(createMenuItem("New", null, "Start a new map", "control N"));
        m.add(createMenuItem("Open...", null, "Open a map", "control O"));
        m.add(recentMenu);
        m.add(save);
        m.add(saveAs);
        // TODO: Re-add print menuitem when printing is functional
        //m.addSeparator();
        //m.add(print);
        //mapEventAdapter.addListener(print);
        m.addSeparator();
        m.add(close);
        m.add(createMenuItem("Exit", null, "Exit the map editor", "control Q"));
        menuBar.add(m);

        undoMenuItem = new TMenuItem(undoAction);
        redoMenuItem = new TMenuItem(redoAction);
        undoMenuItem.setEnabled(false);
        redoMenuItem.setEnabled(false);

        JMenu transformSub = new JMenu("Transform");
        transformSub.add(new TMenuItem(rot90Action, true));
        transformSub.add(new TMenuItem(rot180Action, true));
        transformSub.add(new TMenuItem(rot270Action, true));
        transformSub.addSeparator();
        transformSub.add(new TMenuItem(flipHorAction, true));
        transformSub.add(new TMenuItem(flipVerAction, true));
        mapEventAdapter.addListener(transformSub);

        m = new JMenu("Edit");
        m.add(undoMenuItem);
        m.add(redoMenuItem);
        m.addSeparator();
        m.add(transformSub);
        m.addSeparator();
        m.add(createMenuItem("Preferences...",
                    null, "Configure options of the editor", null));
        mapEventAdapter.addListener(undoMenuItem);
        mapEventAdapter.addListener(redoMenuItem);
        menuBar.add(m);


        m = new JMenu("Map");
        m.add(createMenuItem("Resize", null, "Modify map dimensions"));
        m.add(createMenuItem("Search", null, "Search for/Replace tiles"));
        m.addSeparator();
        m.add(createMenuItem("Properties", null, "Map properties"));
        mapEventAdapter.addListener(m);
        menuBar.add(m);


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

        mapEventAdapter.addListener(layerAdd);

        m = new JMenu("Layer");
        m.add(layerAdd);
        m.add(layerClone);
        m.add(layerDel);
        m.addSeparator();
        m.add(layerUp);
        m.add(layerDown);
        m.addSeparator();
        m.add(layerMerge);
        m.add(layerMergeAll);
        mapEventAdapter.addListener(m);
        menuBar.add(m);

        m = new JMenu("Tilesets");
        m.add(createMenuItem("New tileset...", null,
                    "Add a new internal tileset"));
        m.add(createMenuItem("Import tileset...", null,
                    "Import an external tileset"));
        m.addSeparator();
        m.add(createMenuItem("Tileset Manager", null,
                    "Open the tileset manager"));
        mapEventAdapter.addListener(m);
        menuBar.add(m);

        /*
        m = new JMenu("Objects");
        m.add(createMenuItem("Add Object", null, "Add an object"));
        mapEventAdapter.addListener(m);
        menuBar.add(m);

        JMenu modifySub = new JMenu("Modify");
        modifySub.add(createMenuItem("Expand Selection", null, ""));
        modifySub.add(createMenuItem("Contract Selection", null, ""));

        m = new JMenu("Select");
        m.add(createMenuItem("All", null, "Select entire map"));
        m.add(createMenuItem("Deselect", null, "Cancel Selection"));
        m.add(createMenuItem("Inverse", null, "Invert Selection"));
        m.addSeparator();
        m.add(modifySub);
        mapEventAdapter.addListener(m);
        menuBar.add(m);
        */

        gridMenuItem = new JCheckBoxMenuItem("Show Grid");
        gridMenuItem.addActionListener(this);
        gridMenuItem.setToolTipText("Toggle grid");
        gridMenuItem.setAccelerator(KeyStroke.getKeyStroke("control G"));
        boundaryMenuItem = new JCheckBoxMenuItem("Show Boundaries");
        boundaryMenuItem.addActionListener(this);
        boundaryMenuItem.setToolTipText("Toggle layer boundaries");
        boundaryMenuItem.setAccelerator(KeyStroke.getKeyStroke("control E"));
        m = new JMenu("View");
        m.add(new TMenuItem(zoomInAction));
        m.add(new TMenuItem(zoomOutAction));
        m.add(new TMenuItem(zoomNormalAction));
        m.addSeparator();
        m.add(gridMenuItem);
        // TODO: Enable when boudary drawing code finished.
        //m.add(boundaryMenuItem);
        mapEventAdapter.addListener(m);
        menuBar.add(m);

        m = new JMenu("Help");
        m.add(createMenuItem("About", null, "Show about window"));
        menuBar.add(m);
    }

    /**
     * Creates the left hand main toolbox
     *
     */
    private void createToolbox() {
        Icon iconMove = loadIcon("resources/gimp-tool-move-22.png");
        Icon iconPaint = loadIcon("resources/gimp-tool-pencil-22.png");
        Icon iconErase = loadIcon("resources/gimp-tool-eraser-22.png");
        Icon iconPour = loadIcon("resources/gimp-tool-bucket-fill-22.png");
        Icon iconEyed = loadIcon("resources/gimp-tool-color-picker-22.png");
        Icon iconMarquee = loadIcon("resources/gimp-tool-rect-select-22.png");

        paintButton = createToggleButton(iconPaint, "paint");
        eraseButton = createToggleButton(iconErase, "erase");
        pourButton = createToggleButton(iconPour, "pour");
        eyedButton = createToggleButton(iconEyed, "eyed");
        marqueeButton = createToggleButton(iconMarquee, "marquee");
        moveButton = createToggleButton(iconMove, "move");

        mapEventAdapter.addListener(moveButton);
        mapEventAdapter.addListener(paintButton);
        mapEventAdapter.addListener(eraseButton);
        mapEventAdapter.addListener(pourButton);
        mapEventAdapter.addListener(eyedButton);
        mapEventAdapter.addListener(marqueeButton);

        JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
        toolBar.setFloatable(false);
        toolBar.add(moveButton);
        toolBar.add(paintButton);
        toolBar.add(eraseButton);
        toolBar.add(pourButton);
        toolBar.add(eyedButton);
        // TODO: Re-add marquee button when it can select stuff
        //toolBar.add(marqueeButton);
        toolBar.add(Box.createRigidArea(new Dimension(0, 5)));
        toolBar.add(new TButton(zoomInAction));
        toolBar.add(new TButton(zoomOutAction));

        tilePaletteButton = new TileButton(new Dimension(24, 24));
        tilePaletteButton.setActionCommand("palette");
        tilePaletteButton.setMaintainAspect(true);
        mapEventAdapter.addListener(tilePaletteButton);
        tilePaletteButton.addActionListener(this);

        toolPanel = new JPanel(new BorderLayout());
        toolPanel.add(toolBar, BorderLayout.WEST);
        toolPanel.add(tilePaletteButton, BorderLayout.SOUTH);
    }

    private void createData() {
        JButton b;
        JToolBar tabsPanel = new JToolBar();
        JTabbedPane paintPanel = new JTabbedPane();
        dataPanel = new JPanel(new BorderLayout());

        // Try to load the icons
        Icon imgAdd = loadIcon("resources/gnome-new.png");
        Icon imgDel = loadIcon("resources/gnome-delete.png");
        Icon imgDup = loadIcon("resources/gimp-duplicate-16.png");
        Icon imgUp = loadIcon("resources/gnome-up.png");
        Icon imgDown = loadIcon("resources/gnome-down.png");

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
        layerAddButton = createButton(imgAdd, "Add Layer");
        layerDelButton = createButton(imgDel, "Delete Layer");
        layerCloneButton = createButton(imgDup, "Duplicate Layer");
        layerUpButton = createButton(imgUp, "Move Layer Up");
        layerDownButton = createButton(imgDown, "Move Layer Down");

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
        /*JScrollPane editSp = new JScrollPane();
        editHistoryList = new JList();
        editSp.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        editSp.getViewport().setView(editHistoryList);*/

        JPanel layerPanel = new JPanel();
        layerPanel.setLayout(new GridBagLayout());
        layerPanel.setPreferredSize(new Dimension(120, 120));
        c = new GridBagConstraints();
        c.insets = new Insets(3, 0, 0, 0); c.weightx = 1; c.weighty = 0;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0; c.gridy = 0;
        layerPanel.add(sliderPanel, c);
        c.weighty = 1; c.gridy += 1;
        layerPanel.add(new JScrollPane(layerTable), c);
        c.weighty = 0; c.insets = new Insets(0, 0, 0, 0); c.gridy += 1;
        layerPanel.add(layerButtons, c);
        /*c.weighty = 0.25; c.insets = new Insets(3, 0, 0, 0); c.gridy += 1;
        layerPanel.add(editSp, c);*/

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

    private AbstractButton createToggleButton(Icon icon, String command) {
        return createButton(icon, command, true);
    }

    private AbstractButton createButton(Icon icon, String command) {
        return createButton(icon, command, false);
    }

    private AbstractButton createButton(Icon icon, String command,
            boolean toggleButton) {
        AbstractButton button;
        if (toggleButton) {
            button = new JToggleButton("", icon);
        } else {
            button = new JButton("", icon);
        }
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setActionCommand(command);
        button.addActionListener(this);
        return button;
    }

    /**
     * Returns the currently selected tile.
     */
    public Tile getCurrentTile() {
        return currentTile;
    }

    /**
     * Returns the current map.
     */
    public Map getCurrentMap() {
        return currentMap;
    }

    /**
     * Returns the main application frame.
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
                        (MapLayer)currentMap.getLayer(currentLayer).clone();
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
            while (currentMap.getTotalLayers() > 1) {
                try {
                    currentMap.mergeLayerDown(
                            currentMap.getTotalLayers() - 1);
                } catch (Exception ex) {}
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
        MapLayer layer = currentMap.getLayer(currentLayer);

        if (layer == null) {
            return;
        } else if (mouseButton == MouseEvent.BUTTON3) {
            Tile newTile = layer.getTileAt(tile.x, tile.y);
            if (newTile != currentMap.getNullTile()) {
                setCurrentTile(newTile);
            }
        } else if (mouseButton == MouseEvent.BUTTON1) {
            switch (currentPointerState) {
                case PS_PAINT:
                    paintEdit.setPresentationName("Paint");
                    currentBrush.commitPaint(
                            currentMap, tile.x, tile.y, currentLayer);
                    mapView.repaintRegion(
                            currentBrush.getCenteredBounds(tile.x, tile.y));
                    break;
                case PS_ERASE:
                    paintEdit.setPresentationName("Erase");
                    layer.setTileAt(tile.x, tile.y, currentMap.getNullTile());
                    mapView.repaintRegion(new Rectangle(tile.x, tile.y, 1, 1));
                    break;
                case PS_POUR:
                    paintEdit = null;
                    Tile oldTile = layer.getTileAt(tile.x, tile.y);
                    pour(layer, tile.x, tile.y, currentTile, oldTile);
                    mapView.repaint();
                    break;
                case PS_EYED:
                    Tile newTile = layer.getTileAt(tile.x, tile.y);
                    if (newTile != currentMap.getNullTile()) {
                        setCurrentTile(newTile);
                    }
                    break;
                case PS_MOVE:
                    layer.translate(
                            tile.x - this.mousePressLocation.x,
                            tile.y - mousePressLocation.y);
                    mapView.repaint();
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
        mouseButton = e.getButton();
        bMouseIsDown = true;
        mousePressLocation = mapView.screenToTileCoords(e.getX(), e.getY());
        if (currentPointerState != PS_EYED && currentPointerState != PS_POINT
                && mouseButton == MouseEvent.BUTTON1)
        {
            MapLayer layer = currentMap.getLayer(currentLayer);
            paintEdit =
                new MapLayerEdit(layer, new MapLayer(layer), null);
        }
        doMouse(e);
    }

    public void mouseReleased(MouseEvent event) {
        mouseButton = MouseEvent.NOBUTTON;
        bMouseIsDown = false;
        if (paintEdit != null) {
            MapLayer layer = currentMap.getLayer(currentLayer);
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
            tileCoordsLabel.setText("" + tile.x + ", " + tile.y);
        } else {
            tileCoordsLabel.setText(" ");
        }
    }

    public void mouseDragged(MouseEvent e) {
    	doMouse(e);
		mousePressLocation = mapView.screenToTileCoords(e.getX(), e.getY());
        Point tile = mapView.screenToTileCoords(e.getX(), e.getY());
        if (currentMap.inBounds(tile.x, tile.y)) {
            tileCoordsLabel.setText("" + tile.x + ", " + tile.y);
        } else {
            tileCoordsLabel.setText(" ");
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
        } else if (command.equals("palette")) {
            if (currentMap != null) {
                if (tilePaletteDialog == null) {
                    tilePaletteDialog =
                        new TilePaletteDialog(this, currentMap);
                }
                tilePaletteDialog.show();
            }
        } else {
            handleEvent(event);
        }
    }

    private void handleEvent(ActionEvent event) {
        String command = event.getActionCommand();
        Object src = event.getSource();

        if (command.equals("Open...")) {
            if (checkSave()) {
            	openMap();
            }
        } else if (command.equals("Exit")) {
            if (checkSave()) {
            	try {
                    TiledConfiguration.write("tiled.conf");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        } else if (command.equals("Close")) {
            if (checkSave()) {
            	setCurrentMap(null);
            }
        } else if (command.equals("New")) {
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
        } else if (command.equals("Add Layer") ||
                command.equals("Duplicate Layer") ||
                command.equals("Delete Layer") ||
                command.equals("Move Layer Up") ||
                command.equals("Move Layer Down") ||
                command.equals("Merge Down") ||
                command.equals("Merge All")) {
            doLayerStateChange(event);
        } else if (command.equals("New tileset...")) {
            if (currentMap != null) {
                NewTilesetDialog dialog =
                    new NewTilesetDialog(appFrame, currentMap);
                TileSet newSet = dialog.create();
                if (newSet != null) {
                    currentMap.addTileset(newSet);
                }
            }
        } else if (command.equals("Import tileset...")) {
            if (currentMap != null) {
                JFileChooser ch = new JFileChooser(currentMap.getFilename());

				ch.setFileFilter(new TiledFileFilter());
                int ret = ch.showOpenDialog(appFrame);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    String filename = ch.getSelectedFile().getAbsolutePath();
                    MapReader mr = new XMLMapTransformer();
                    try {
                        TileSet set = mr.readTileset(filename);
                        set.setSource(null);
                        currentMap.addTileset(set);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (command.equals("Tileset Manager")) {
            if (currentMap != null) {
                TilesetManager manager = new TilesetManager(this, currentMap);
                manager.show();
            }
        } else if (command.equals("Save")) {
            if (currentMap != null) {
                saveMap(currentMap.getFilename(), false);
            }
        } else if (command.equals("Save as...")) {
            if (currentMap != null) {
            	saveMap(currentMap.getFilename(), true);
            }
        } else if (command.equals("Properties")) {
            MapPropertiesDialog pd = new MapPropertiesDialog(appFrame, this);
            pd.getProps();
        } else if (command.equals("Show Boundaries") ||
                command.equals("Hide Boundaries")) {
            mapView.toggleMode(MapView.PF_BOUNDARYMODE);
        } else if (command.equals("Show Grid") ||
                command.equals("Hide Grid")) {
            // Toggle grid
            mapView.toggleMode(MapView.PF_GRIDMODE);
        } else if(command.equals("Resize")) {
            ResizeDialog rd = new ResizeDialog(appFrame, this);
            rd.showDialog();
        }  else if(command.equals("Search")) {
			SearchDialog sd = new SearchDialog(appFrame, currentMap);
			sd.showDialog();
		} else if (command.equals("About")) {
            if (aboutDialog == null) {
                aboutDialog = new AboutDialog(appFrame);
            }
            aboutDialog.show();
        } else if (command.substring(0, command.length() < 5 ? command.length() : 5).equals("_open")) {
            try {
                loadMap(TiledConfiguration.getValue(
                            "tmx.recent." + command.substring(5)));
            } catch (Exception e) {
                e.printStackTrace();
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
        zoomLabel.setText("" + (int)(mapView.getZoom() * 100) + "%");
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
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        int selectedRow = layerTable.getSelectedRow();

        // At the moment, this can only be a new layer selection
        if (currentMap != null && selectedRow >= 0) {
            currentLayer = currentMap.getTotalLayers() - selectedRow - 1;

            float opacity = currentMap.getLayer(currentLayer).getOpacity();
            opacitySlider.setValue((int)(opacity * 100));
        } else {
            currentLayer = -1;
        }

        updateLayerOperations();
    }

    public void stateChanged(ChangeEvent e) {
        // At the moment, this can only be movement in the opacity slider

        if (currentMap != null && currentLayer >= 0) {
            MapLayer layer = currentMap.getLayer(currentLayer);
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
        private int transform;
        public LayerTransformAction(int transform) {
            this.transform = transform;
            switch (transform) {
                case MapLayer.ROTATE_90:
                    putValue(NAME, "Rotate 90 degrees CW");
                    putValue(SHORT_DESCRIPTION,
                            "Rotate layer 90 degrees clockwise");
                    putValue(SMALL_ICON,
                            loadIcon("resources/gimp-rotate-90-16.png"));
                    break;
                case MapLayer.ROTATE_180:
                    putValue(NAME, "Rotate 180 degrees CW");
                    putValue(SHORT_DESCRIPTION,
                            "Rotate layer 180 degrees clockwise");
                    putValue(SMALL_ICON,
                            loadIcon("resources/gimp-rotate-180-16.png"));
                    break;
                case MapLayer.ROTATE_270:
                    putValue(NAME, "Rotate 90 degrees CCW");
                    putValue(SHORT_DESCRIPTION,
                            "Rotate layer 90 degrees counterclockwise");
                    putValue(SMALL_ICON,
                            loadIcon("resources/gimp-rotate-270-16.png"));
                    break;
                case MapLayer.MIRROR_VERTICAL:
                    putValue(NAME, "Flip vertically");
                    putValue(SHORT_DESCRIPTION, "Flip layer vertically");
                    putValue(SMALL_ICON,
                            loadIcon("resources/gimp-flip-vertical-16.png"));
                    break;
                case MapLayer.MIRROR_HORIZONTAL:
                    putValue(NAME, "Flip horizontally");
                    putValue(SHORT_DESCRIPTION, "Flip layer horizontalle");
                    putValue(SMALL_ICON,
                            loadIcon("resources/gimp-flip-horizontal-16.png"));
                    break;
            }
        }
        public void actionPerformed(ActionEvent evt) {
            MapLayer layer = currentMap.getLayer(currentLayer);
            MapLayerEdit transEdit;
            transEdit = new MapLayerEdit(layer, new MapLayer(layer));
            switch (transform) {
                case MapLayer.ROTATE_90:
                    transEdit.setPresentationName("Rotate");
                    layer.rotate(MapLayer.ROTATE_90);
                    break;
                case MapLayer.ROTATE_180:
                    transEdit.setPresentationName("Rotate");
                    layer.rotate(MapLayer.ROTATE_180);
                    break;
                case MapLayer.ROTATE_270:
                    transEdit.setPresentationName("Rotate");
                    layer.rotate(MapLayer.ROTATE_270);
                    break;
                case MapLayer.MIRROR_VERTICAL:
                    transEdit.setPresentationName("Vertical Flip");
                    layer.mirror(MapLayer.MIRROR_VERTICAL);
                    break;
                case MapLayer.MIRROR_HORIZONTAL:
                    transEdit.setPresentationName("Horizontal Flip");
                    layer.mirror(MapLayer.MIRROR_HORIZONTAL);
                    break;
            }
            transEdit.end(new MapLayer(layer));
            undoSupport.postEdit(transEdit);
            mapView.repaint();
        }
    }

    private class ZoomInAction extends AbstractAction {
        public ZoomInAction() {
            super("Zoom In");
            putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke("control EQUALS"));
            putValue(SHORT_DESCRIPTION, "Zoom in one level");
            putValue(SMALL_ICON, loadIcon("resources/gnome-zoom-in.png"));
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
            putValue(SMALL_ICON, loadIcon("resources/gnome-zoom-out.png"));
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
                    KeyStroke.getKeyStroke("control 1"));
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

    private class UndoAdapter implements UndoableEditListener {
        public void undoableEditHappened(UndoableEditEvent evt) {
            undoStack.addEdit(evt.getEdit());
            updateHistory();
        }
    }

    private void pour(MapLayer layer, int x, int y,
            Tile newTile, Tile oldTile) {
        if (newTile == oldTile) return;

        Rectangle area = new Rectangle(new Point(x, y));
        Stack stack = new Stack();
        MapLayer before = new MapLayer(layer);
        MapLayer after;

        stack.push(new Point(x, y));
        while (!stack.empty()) {
            // Remove the next tile from the stack
            Point p = (Point)stack.pop();

            // If the tile it meets the requirements, set it and push its
            // neighbouring tiles on the stack.
            if (currentMap.inBounds(p.x, p.y) &&
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

        Rectangle bounds = new Rectangle(
                area.x, area.y, area.width + 1, area.height + 1);
        after = new MapLayer(bounds);
        after.copyFrom(layer);

        MapLayerEdit mle = new MapLayerEdit(layer, before, after);
        mle.setPresentationName("Fill");
        undoSupport.postEdit(mle);
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
        return (currentMap != null && undoStack.canUndo() &&
                !undoStack.isAllSaved());
    }

    /**
     * Saves the current map, optionally with a "Save As" dialog. If
     * <code>filename</code> is <code>null</code> or <code>saveAs</code> is
     * passed <code>true</code>, a "Save As" dialog is opened.
     *
     * @param filename Filename to safe the current map to.
     * @param saveAs   Pass <code>true</code> to ask for a new filename using
     *                 a "Save As" dialog.
     */
    public void saveMap(String filename, boolean saveAs) {
        if (saveAs || filename == null) {
            JFileChooser ch;

            if (filename == null) {
                ch = new JFileChooser();
            } else {
                ch = new JFileChooser(filename);
            }

            ch.setFileFilter(new TiledFileFilter(TiledFileFilter.FILTER_TMX));

            if (ch.showSaveDialog(appFrame) == JFileChooser.APPROVE_OPTION) {
                filename = ch.getSelectedFile().getAbsolutePath();
                TiledConfiguration.addConfigPair("tmx.save.maplocation",
                        filename.substring(0, filename.lastIndexOf(
                                File.separatorChar) + 1));
            }
        }

        try {
            MapWriter writer = new XMLMapWriter();
            writer.writeMap(currentMap, filename);
            currentMap.setFilename(filename);
            updateRecent(filename);
            undoStack.commitSave();
            updateTitle();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(appFrame,
                    "Error while saving " + filename + ": " + e.getMessage(),
                    "Error while saving map",
                    JOptionPane.ERROR_MESSAGE);
        }
        TiledConfiguration.remove("tmx.save.maplocation");
    }

    private void openMap() {
        String startLocation = "";

        if (currentMap != null) {
            startLocation = currentMap.getFilename();
        }

        JFileChooser ch = new JFileChooser(startLocation);
        ch.setFileFilter(new TiledFileFilter(TiledFileFilter.FILTER_TMX));

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

    private void updateRecent(String mapFile) {
        Vector recent = new Vector();
        try {
            recent.add(TiledConfiguration.getValue("tmx.recent.1"));
            recent.add(TiledConfiguration.getValue("tmx.recent.2"));
            recent.add(TiledConfiguration.getValue("tmx.recent.3"));
            recent.add(TiledConfiguration.getValue("tmx.recent.4"));
        } catch (Exception e) {
        }

        // If a map file is given, add it to the recent list
        if (mapFile != null) {
            // Remove any existing entry that is the same
            for (int i = 0; i < recent.size(); i++) {
                String filename = (String)recent.get(i);
                if (filename!=null&&filename.equals(mapFile)) {
                    recent.remove(i);
                    i--;
                }
            }

            recent.add(0, mapFile);

            if (recent.size() > 4) {
                recent.setSize(4);
            }
        }

        recentMenu.removeAll();

        for (int i = 0; i < recent.size(); i++) {
            String file = (String)recent.get(i);
            if (file != null) {
	            String name =
	                file.substring(file.lastIndexOf(File.separatorChar) + 1);

	            TiledConfiguration.addConfigPair("tmx.recent." + (i + 1), file);
	            JMenuItem recentOption = createMenuItem(name, null, null);
	            recentOption.setActionCommand("_open" + (i + 1));
	            recentMenu.add(recentOption);
            }
        }
    }

    private void setCurrentMap(Map newMap) {
        currentMap = newMap;
        boolean mapLoaded = (currentMap != null);

        if (!mapLoaded) {
            mapEventAdapter.fireEvent(MapEventAdapter.ME_MAPINACTIVE);
            mapView = null;
            mapScrollPane.setViewportView(Box.createRigidArea(
                        new Dimension(0,0)));
            setCurrentPointerState(PS_POINT);
            tileCoordsLabel.setPreferredSize(null);
            tileCoordsLabel.setText(" ");
            zoomLabel.setText(" ");
            tilePalettePanel.setTileset(null);
            setCurrentTile(null);
        } else {
            mapEventAdapter.fireEvent(MapEventAdapter.ME_MAPACTIVE);
            mapView = currentMap.createView();
            mapView.addMouseListener(this);
            mapView.addMouseMotionListener(this);
            mapView.addComponentListener(this);
            JViewport mapViewport = new JViewport();
            mapViewport.setView(mapView);
            mapScrollPane.setViewport(mapViewport);
            setCurrentPointerState(PS_PAINT);

            currentMap.addMapChangeListener(this);

            gridMenuItem.setState(mapView.getMode(MapView.PF_GRIDMODE));

            Vector tilesets = currentMap.getTilesets();
            if (tilesets.size() > 0) {
                tilePalettePanel.setTileset(tilesets);
                TileSet first = (TileSet)tilesets.get(0);
                setCurrentTile(first.getFirstTile());
            } else {
                tilePalettePanel.setTileset(null);
                setCurrentTile(null);
            }

            tileCoordsLabel.setText("" + (currentMap.getWidth() - 1) + ", " +
                    (currentMap.getHeight() - 1));
            tileCoordsLabel.setPreferredSize(null);
            Dimension size = tileCoordsLabel.getPreferredSize();
            tileCoordsLabel.setText(" ");
            tileCoordsLabel.setMinimumSize(size);
            tileCoordsLabel.setPreferredSize(size);
            zoomLabel.setText("" + (int)(mapView.getZoom() * 100) + "%");
        }

        zoomInAction.setEnabled(mapLoaded);
        zoomOutAction.setEnabled(mapLoaded);
        zoomNormalAction.setEnabled(mapLoaded && mapView.getZoomLevel() !=
                MapView.ZOOM_NORMALSIZE);

        if (tilePaletteDialog != null) {
            tilePaletteDialog.setMap(currentMap);
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
                    MapLayer layer = currentMap.getLayer(currentLayer);
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
            if (ShapeBrush.class.isInstance(currentBrush)) {
                ((ShapeBrush)currentBrush).setTile(tile);
            }
            tilePaletteButton.setTile(currentTile);
        }
    }

    private void setCurrentPointerState(int state) {
        currentPointerState = state;

        // Select the matching button
        paintButton.setSelected(state == PS_PAINT);
        eraseButton.setSelected(state == PS_ERASE);
        pourButton.setSelected(state == PS_POUR);
        eyedButton.setSelected(state == PS_EYED);
        marqueeButton.setSelected(state == PS_MARQUEE);
		moveButton.setSelected(state == PS_MOVE);

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

    public static BufferedImage loadImageResource(String fname) throws IOException {
        return ImageIO.read(MapEditor.class.getResourceAsStream(fname));
    }

    private ImageIcon loadIcon(String fname) {
        try {
            return new ImageIcon(loadImageResource(fname));
        } catch (IOException e) {
            System.out.println("Failed to load icon: " + fname);
            return null;
        }
    }

    /**
     * Starts Tiled.
     *
     * @param args The first argument may be map file.
     */
    public static void main(String[] args) {
        try {
            TiledConfiguration.populateDefaults();
            TiledConfiguration.parse(
                    new BufferedReader(new FileReader("tiled.conf")));
        } catch (Exception e) {
            /*
            JOptionPane.showMessageDialog(null,
                    "Could not load configuration file - " +
                    "some functions may not work correctly.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            */
        }

        MapEditor editor = new MapEditor();

        if (args.length > 0) {
            editor.loadMap(args[0]);
        }
    }
}
