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

package tiled.mapeditor.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import tiled.io.PluggableMapIO;
import tiled.util.TiledConfiguration;

public class PluginClassLoader extends URLClassLoader
{
    private Vector plugins;
    private Hashtable readerFormats, writerFormats;

    public PluginClassLoader() {
        super(new URL[0]);
        plugins = new Vector();
    }

    public PluginClassLoader(URL[] urls) {
        super(urls);
        plugins = new Vector();
    }

    public void readPlugins(String base) throws Exception {
        String baseURL = base;
        if (base == null) {
            baseURL = TiledConfiguration.getValue("tiled.plugins.dir");
        }

        File dir = new File(baseURL);
        if (!dir.exists() || !dir.canRead()) {
            throw new Exception(
                    "Could not open directory for reading plugins: " +
                    baseURL);
        }

        File [] files = dir.listFiles();
        for (int i = 0; i < files.length; i++){
            if (files[i].getAbsolutePath().substring(files[i].getAbsolutePath().lastIndexOf('.') + 1).equals("jar")){
                try {
                    JarFile jf = new JarFile(files[i]);
                    //verify that the jar has the necessary files to be a plugin
                    if (jf.getManifest().getMainAttributes().get("readerClass") == null || 
                            jf.getManifest().getMainAttributes().get("writerClass") == null) {
                        continue;
                    }

                    JarEntry reader = jf.getJarEntry((String) jf.getManifest().getMainAttributes().get("readerClass"));
                    JarEntry writer = jf.getJarEntry((String) jf.getManifest().getMainAttributes().get("writerClass"));

                    if (doesImplement(loadFromJar(jf, reader),"tiled.io.MapReader") 
                            && doesImplement(loadFromJar(jf, writer),"tiled.io.MapWriter")) {
                        System.out.println("Added "+files[i].getCanonicalPath());
                        super.addURL(new URL(files[i].getAbsolutePath()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Object getReaderFor(String ext) throws Exception {
        for (int i = 0; i < plugins.size(); i++) {
            Iterator itr = readerFormats.keySet().iterator();
            while(itr.hasNext()){
                String key = (String)itr.next();
                if(key.endsWith(ext)) {
                    return loadClass((String)readerFormats.get(key)).newInstance();
                }
            }
        }
        throw new Exception("No plugin exists for the extension: " + ext);
    }

    public Object getWriterFor(String ext) throws Exception {
        for (int i = 0; i < plugins.size(); i++) {
            Iterator itr = writerFormats.keySet().iterator();
            while (itr.hasNext()) {
                String key = (String)itr.next();
                if (key.endsWith(ext)) {
                    return loadClass((String) writerFormats.get(key)).newInstance();
                }
            }
        }
        throw new Exception("No plugin exists for the extension: "+ext);
    }

    public Class loadFromJar(JarFile jf, JarEntry je) throws IOException {
        byte [] buffer = new byte[(int)je.getSize()+1]; 
        jf.getInputStream(je).read(buffer);
        return defineClass(je.getName(),buffer,0,buffer.length);
    }

    public boolean doesImplement(Class c, String interfaceName) throws Exception {
        Class[] interfaces = c.getInterfaces();
        for (int i = 0; i < interfaces.length; i++){
            String name = interfaces[i].toString(); 
            if (name.substring(name.indexOf(' ') + 1).equals(interfaceName)) {
                _add(c);
                return true;
            }
        }
        return false;
    }

    private boolean isReader(Class c) throws Exception {
        return doesImplement(c,"tiled.io.MapReader");
    }

    private void _add(Class c) throws Exception{
        PluggableMapIO p = (PluggableMapIO) c.newInstance();
        String clname = c.getClass().toString();
        clname = clname.substring(clname.indexOf(' ')+1);
        String filter = p.getFilter();
        String [] ext = filter.split(",");

        if (isReader(c)) {
            for(int i = 0; i < ext.length; i++) {
                readerFormats.put(ext[i], clname);
            }
        } else {
            for (int i = 0; i < ext.length; i++){
                writerFormats.put(ext[i], clname);
            }
        }
    }
}
