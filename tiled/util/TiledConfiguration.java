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

package tiled.util;

import java.util.*;
import java.io.*;


/**
 * A singleton class handling configuration options.
 */
public final class TiledConfiguration
{
    private static TiledConfiguration instance = null;
    private HashMap settings;
    private boolean changed;

    private TiledConfiguration() {
        settings = new HashMap();
        populateDefaults();
        changed = false;
    }

    /**
     * Returns the tiled configuration class instance. Will return <code>null
     * </code> when the instance hasn't been created yet.
     */
    public static TiledConfiguration getInstance() {
        return instance;
    }

    /**
     * Returns the tiled configuration class instance. Will create a new
     * instance when it hasn't been created yet.
     */
    public static TiledConfiguration getOrCreateInstance() {
        if (instance == null) {
            instance = new TiledConfiguration();
        }
        return instance;
    }

    /**
     * Reads config settings from the given file.
     *
     * @param filename path of file to read configuration from
     */
    public void parse(String filename)
        throws FileNotFoundException, IOException {
        parse(new BufferedReader(new FileReader(filename)));
    }

    /**
     * Reads config settings from the given buffered reader.
     */
    public void parse(BufferedReader br) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            // Make sure it isn't a comment
            if (!line.trim().startsWith("#") && line.trim().length() > 0){
                String [] keyValue = line.split("[ ]*=[ ]*");
                if (keyValue.length > 1) {
                    settings.put(keyValue[0],keyValue[1]);
                }
            }
        }
    }

    /**
     * Returns wether the option is available in the config file.
     */
    public boolean hasOption(String name) {
        return (settings.get(name) != null);
    }

    /**
     * Returns the value of the given option.
     */
    public String getValue(String option) {    	
         return (String)settings.get(option);
    }

    /**
     * Returns the integer value of the given option, or the given default
     * when the option doesn't exist.
     */
    public int getIntValue(String option, int def) {
        String str = getValue(option);
        if (str != null) {
            return Integer.parseInt(str);
        } else {
            return def;
        }
    }

    /**
     * Returns wether a certain option equals a certain string, ignoring case.
     */
    public boolean keyHasValue(String option, String comp) {
        String check = getValue(option);
        return (check != null && check.equalsIgnoreCase(comp));
    }

    /**
     * Returns wether a certain option equals a certain integer.
     */
    public boolean keyHasValue(String option, int comp) {
        return (hasOption(option) && getIntValue(option, 0) == comp);
    }

    /**
     * Adds a config pair to the configuration.
     */
    public void addConfigPair(String key, String value) {
        String prev = (String)settings.get(key);
        if (prev == null || !prev.equals(value)) {
            settings.put(key, value);
            changed = true;
        }
    }

    /**
     * Removes a config pair from the configuration.
     */
    public void remove(String key) {
        settings.remove(key);
    }

    /**
     * Writes the current configuration to a file. Preserves comments and
     * unknown options.
     *
     * @param filename the file to write the configuration to
     */
    public void write(String filename) throws IOException, Exception {
        BufferedWriter bw;
        Vector inputLines = new Vector();
        HashMap availableKeys = new HashMap();
        String line;

        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            while ((line = br.readLine()) != null) {
                inputLines.add(line);
            }

            br.close();
        } catch (IOException ioe) {
            // Although it's nice, it's not necessary to have a config file in
            // existence when we go to write the config
        }

        bw = new BufferedWriter(new FileWriter(filename));

        // Iterate through all existing lines in the file
        Iterator lineItr = inputLines.iterator();
        while (lineItr.hasNext()) {
            line = (String)lineItr.next();
            // Make sure it isn't a comment
            if (!line.trim().startsWith("#") && line.trim().length() > 0) {
                String[] keyValue = line.split("[ ]*=[ ]*");
                availableKeys.put(keyValue[0], "Tiled is cool");
                if (hasOption(keyValue[0])) {
                    bw.write(keyValue[0] + " = " + getValue(keyValue[0]));
                    bw.newLine();
                } else {
                    bw.write(line);
                    bw.newLine();
                }
            } else {
                bw.write(line);
                bw.newLine();
            }
        }

        // Iterate through configuration options, saving the options that were
        // not yet in the file already.
        Iterator keyItr = settings.keySet().iterator();
        while (keyItr.hasNext()) {
            String key = (String)keyItr.next();
            if (!availableKeys.containsKey(key) && settings.get(key) != null) {
                bw.write(key + " = " + settings.get(key));
                bw.newLine();
            }
        }

        bw.close();
    }
    
    public void populateDefaults() {
        addConfigPair("tmx.save.embedImages", "1");
        addConfigPair("tmx.save.tileImagePrefix", "tile");
        addConfigPair("tmx.save.layerCompression", "1");
        addConfigPair("tmx.save.encodeLayerData", "1");
        addConfigPair("tmx.save.tileSetImages", "0");
        addConfigPair("tmx.save.embedtileSetImages", "0");
        addConfigPair("tmx.undo.depth", "30");
		addConfigPair("tiled.cursorhighlight", "1");
        addConfigPair("tiled.grid.color", "0x000000");
        addConfigPair("tiled.grid.antialias", "1");
        addConfigPair("tiled.grid.opacity", "255");
        addConfigPair("tiled.plugins.dir", ".");
    }
}
