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


public final class TiledConfiguration
{
    static HashMap settings = null;
    static boolean newValue = false;

    public TiledConfiguration() {
        settings = new HashMap();
    }

    public TiledConfiguration(String file)
        throws FileNotFoundException, IOException {
        settings = new HashMap();
        parse(new BufferedReader(new FileReader(file)));
    }

    public static void parse(BufferedReader br) throws IOException {
        String line;
        if (settings == null) {
            settings = new HashMap();
        }
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

    private static void _add(String key, String value) {
        if (settings == null) {
            settings = new HashMap();
        }
        settings.put(key, value);
    }

    public static boolean hasOption(String name) {
        return (settings.get(name) != null);
    }

    public static String getValue(String option) {    	
         return (String)settings.get(option);
    }

    public static int getIntValue(String option, int def) {
        String str = getValue(option);
        if (str != null) {
            return Integer.parseInt(str);
        } else {
            return def;
        }
    }

    public static boolean keyHasValue(String option, String comp) {
        String check = getValue(option);
        return (check != null && check.equalsIgnoreCase(comp));
    }

    public static void addConfigPair(String key, String value) {
        _add(key, value);
        newValue = true;
    }

    public static void remove(String key) {
        settings.remove(key);
    }

    public static void write(String filename) throws IOException, Exception {
        BufferedWriter bw;
        Vector inputLines = new Vector();
        HashMap availableKeys = new HashMap();
        String line;

        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            while((line = br.readLine()) != null) {
                inputLines.add(line);
            }

            br.close();
        } catch (IOException ioe) {
            // Although it's nice, it's not necessary to have a config file in
            // existence when we go to write the config
        }

        bw = new BufferedWriter(new FileWriter(filename));

        Iterator lineItr = inputLines.iterator();
        while (lineItr.hasNext()) {
            line = (String)lineItr.next();
            // Make sure it isn't a comment
            if (!line.trim().startsWith("#") && line.trim().length() > 0) {
                String [] keyValue = line.split("[ ]*=[ ]*");
				availableKeys.put(keyValue[0], "Tiled is cool");
                if (hasOption(keyValue[0])) {
                    String newLine = line.substring(0, line.lastIndexOf(
                                keyValue[1])) + getValue(keyValue[0]);
                    bw.write(newLine);
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
        bw.newLine();

        Iterator keyItr = settings.keySet().iterator();
        while (keyItr.hasNext()) {
            String key = (String)keyItr.next();
            try {
                if (availableKeys.get(key) == null){
                    if (settings.get(key) != null) {
                        bw.write(key + " = " + settings.get(key));
                        bw.newLine();
                    }
                }
            } catch (Exception e) {
                if (settings.get(key) != null) {
                    bw.write(key + " = " + settings.get(key));
                    bw.newLine();
                }
            }
        }

        bw.close();
    }
    
    public static void populateDefaults() {
        _add("tmx.save.embedImages", "1");
        _add("tmx.save.tileImagePrefix", "tile");
        _add("tmx.save.layerCompression", "1");
        _add("tmx.save.encodeLayerData", "1");
        _add("tmx.save.tileSetImages", "0");
        _add("tmx.save.embedtileSetImages", "0");
        _add("tmx.undo.depth", "30");
        _add("tiled.plugins.dir",".");
        //TODO: add any new properties here as well with a default value
    }
}
