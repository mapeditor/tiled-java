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

package tiled.mapeditor.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import tiled.mapeditor.MapEditor;
import tiled.mapeditor.Resources;
import tiled.mapeditor.util.TiledFileFilter;
import tiled.io.MapWriter;
import tiled.io.MapHelper;
import tiled.util.TiledConfiguration;

/**
 * A save action that always shows a file chooser.
 *
 * @version $Id$
 */
public class SaveAsAction extends AbstractAction
{
    protected MapEditor editor;
    private boolean savingCancelled;

    private static final String ACTION_NAME = Resources.getString("action.map.saveas.name");
    private static final String ACTION_TOOLTIP = Resources.getString("action.map.saveas.tooltip");
    private static final String UNKNOWN_TYPE_MESSAGE = Resources.getString("dialog.saveas.unknown-type.message");
    private static final String CONFIRM_MISMATCH = Resources.getString("dialog.saveas.confirm.mismatch");
    private static final String CONFIRM_MISMATCH_TITLE = Resources.getString("dialog.saveas.confirm.mismatch.title");
    private static final String FILE_EXISTS_MESSAGE = Resources.getString("general.file.exists.message");
    private static final String FILE_EXISTS_TITLE = Resources.getString("general.file.exists.title");
    private static final String SAVEAS_ERROR_MESSAGE = Resources.getString("dialog.saveas.error.message");
    private static final String SAVEAS_ERROR_TITLE = Resources.getString("dialog.saveas.error.title");

    public SaveAsAction(MapEditor editor) {
        super(ACTION_NAME);
        putValue(SHORT_DESCRIPTION, ACTION_TOOLTIP);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control shift S"));
        this.editor = editor;
    }

    public void actionPerformed(ActionEvent e) {
        showFileChooser();
    }

    /**
     * Shows the confirming file chooser and proceeds with saving the map when
     * a filename was approved.
     */
    protected void showFileChooser()
    {
        // Start at the location of the most recently loaded map file
        String startLocation = TiledConfiguration.node("recent").get("file0", "");

        TiledFileFilter byExtensionFilter =
                new TiledFileFilter(TiledFileFilter.FILTER_EXT);
        TiledFileFilter tmxFilter =
                new TiledFileFilter(TiledFileFilter.FILTER_TMX);

        JFileChooser chooser = new ConfirmingFileChooser(startLocation);
        chooser.addChoosableFileFilter(byExtensionFilter);
        chooser.addChoosableFileFilter(tmxFilter);
        chooser.setFileFilter(byExtensionFilter);

        MapWriter[] writers = editor.getPluginLoader().getWriters();
        for (int i = 0; i < writers.length; i++) {
            try {
                chooser.addChoosableFileFilter(new TiledFileFilter(writers[i]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int result = chooser.showSaveDialog(editor.getAppFrame());
        if (result == JFileChooser.APPROVE_OPTION)
        {
            TiledFileFilter saver = (TiledFileFilter) chooser.getFileFilter();
            String selectedFile = chooser.getSelectedFile().getAbsolutePath();
            saveFile(saver, selectedFile);
        }
    }

    /**
     * Actually saves the map.
     *
     * @param saver the file filter selected when the filename was chosen
     * @param filename the filename to save the map to
     */
    protected void saveFile(TiledFileFilter saver, String filename)
    {
        try {
            // Either select the format by extension or use a specific format
            // when selected.
            if (saver.getType() == TiledFileFilter.FILTER_EXT) {
                MapHelper.saveMap(editor.getCurrentMap(), filename);
            } else {
                MapHelper.saveMap(editor.getCurrentMap(), saver.getPlugin(),
                                  filename);
            }

            // The file was saved successfully, update some things.
            // todo: this could probably be done a bit neater
            editor.getCurrentMap().setFilename(filename);
            editor.updateRecent(filename);
            editor.getUndoStack().commitSave();
            editor.updateTitle();
        }
        catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(editor.getAppFrame(),
                                          SAVEAS_ERROR_MESSAGE + " " +
                                                  filename + ": " +
                                                  e.getLocalizedMessage(),
                                          SAVEAS_ERROR_TITLE,
                                          JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSavingCancelled ()
    {
        return savingCancelled;
    }

    public void resetSavingCancelled ()
    {
        savingCancelled = false;
    }

    /**
     * This file chooser extends the {@link JFileChooser} in a number of ways.
     * <ul>
     *   <li>Adds an extention to the filename based on the file filter, when
     *       the user didn't specify any.</li>
     *   <li>If the file to be saved is not accepted by the chosen file filter,
     *       it confirms that the user really wants to do this. This is done
     *       because the same file filter is used to determine with which
     *       plugin to load the file.</li>
     *   <li>Confirms before overwriting an existing file.</li>
     * </ul>
     * This file chooser can only be used with {@link TiledFileFilter}.
     */
    private final class ConfirmingFileChooser extends JFileChooser
    {
        public ConfirmingFileChooser(String currentDirectoryPath) {
            super(currentDirectoryPath);
            setAcceptAllFileFilterUsed(false);
            setDialogTitle(Resources.getString("dialog.saveas.title"));
        }

        public void approveSelection ()
        {
            File file = new File(getSelectedFile().getAbsolutePath());
            TiledFileFilter saver = (TiledFileFilter) getFileFilter();

            // If the file does not have an extention, append the first
            // extension specified by the file filter.
            String filename = file.getName();
            int lastDot = filename.lastIndexOf('.');

            if (lastDot == -1 || lastDot == filename.length() - 1) {
                if (saver.getType() == TiledFileFilter.FILTER_EXT) {
                    // Impossible to determine extension with this filter
                    JOptionPane.showMessageDialog(this,
                                                  UNKNOWN_TYPE_MESSAGE);
                    return;
                }

                String newFilePath = file.getAbsolutePath();

                // Add a dot if it wasn't at the end already
                if (lastDot != filename.length() - 1) {
                    newFilePath = newFilePath + ".";
                }

                file = new File(newFilePath + saver.getFirstExtention());
            }

            // Check that chosen plugin accepts the file. It is a good idea to
            // warn the user when this is not the case, because loading the map
            // becomes a problem.
            if (saver.getType() != TiledFileFilter.FILTER_EXT) {
                if (!saver.accept(file)) {
                    int result = JOptionPane.showConfirmDialog(
                            editor.getAppFrame(),
                            CONFIRM_MISMATCH, CONFIRM_MISMATCH_TITLE,
                            JOptionPane.YES_NO_OPTION);

                    if (result != JOptionPane.OK_OPTION) {
                        return;
                    }
                }
            }

            // Confirm overwrite if the file happens to exist already
            if (file.exists())
            {
                int answer = JOptionPane.showConfirmDialog(
                        editor.getAppFrame(),
                        FILE_EXISTS_MESSAGE, FILE_EXISTS_TITLE,
                        JOptionPane.YES_NO_OPTION);

                if (answer == JOptionPane.YES_OPTION)
                {
                    savingCancelled = false;
                    super.approveSelection();
                }
            }
            else {
                savingCancelled = false;
                super.approveSelection();
            }
        }

        public void cancelSelection ()
        {
            savingCancelled = true;
            super.cancelSelection();
        }
    }
}
