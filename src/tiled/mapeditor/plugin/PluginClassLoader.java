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

package tiled.mapeditor.plugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.swing.JFrame;
import javax.swing.ProgressMonitor;

import tiled.io.MapReader;
import tiled.io.MapWriter;
import tiled.io.PluggableMapIO;
import tiled.util.TiledConfiguration;

/**
 * @version $Id$
 */
public final class PluginClassLoader extends URLClassLoader
{
    private Vector plugins;
    private Vector readers, writers;
    private Hashtable readerFormats, writerFormats;
    private static PluginClassLoader instance;
    
    public PluginClassLoader() {
        super(new URL[0]);
        plugins = new Vector();
        readers = new Vector();
        writers = new Vector();
        readerFormats = new Hashtable();
        writerFormats = new Hashtable();
    }

    public PluginClassLoader(URL[] urls) {
        super(urls);
        plugins = new Vector();
        readers = new Vector();
        writers = new Vector();
        readerFormats = new Hashtable();
        writerFormats = new Hashtable();
    }

    public static synchronized PluginClassLoader getInstance() {
        if (instance == null) {
            instance = new PluginClassLoader();
        }
        return instance;
    }
    
    public void readPlugins(String base, JFrame parent) throws Exception {
        String baseURL = base;
        ProgressMonitor monitor;

        if (base == null) {
            baseURL = TiledConfiguration.root().get("pluginsDir", "plugins");
        }

        File dir = new File(baseURL);
        if (!dir.exists() || !dir.canRead()) {
            //FIXME: removed for webstart
            //throw new Exception(
            //        "Could not open directory for reading plugins: " +
            //        baseURL);
            return;
        }

        int total = 0;
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            String aPath = files[i].getAbsolutePath();
            if (aPath.endsWith(".jar")) {
                total++;
            }
        }

        // Start the progress monitor
        monitor = new ProgressMonitor(
                parent, "Loading plugins", "", 0, total - 1);
        monitor.setProgress(0);
        monitor.setMillisToPopup(0);
        monitor.setMillisToDecideToPopup(0);

        for (int i = 0; i < files.length; i++) {
            String aPath = files[i].getAbsolutePath();
            String aName =
                aPath.substring(aPath.lastIndexOf(File.separatorChar) + 1);

            if (aPath.endsWith(".jar")) {
                try {
                    monitor.setNote("Reading " + aName + "...");
                    JarFile jf = new JarFile(files[i]);

                    monitor.setProgress(i);

                    if (jf.getManifest() == null)
                        continue;

                    String readerClassName =
                        jf.getManifest().getMainAttributes().getValue(
                                "Reader-Class");
                    String writerClassName =
                        jf.getManifest().getMainAttributes().getValue(
                                "Writer-Class");

                    Class readerClass = null, writerClass = null;

                    // Verify that the jar has the necessary files to be a
                    // plugin
                    if (readerClassName == null && writerClassName == null) {
                        continue;
                    }

                    monitor.setNote("Loading " + aName + "...");
                    addURL((new File(aPath)).toURL());

                    if (readerClassName != null) {
                        JarEntry reader = jf.getJarEntry(
                                readerClassName.replace('.', '/') + ".class");

                        if (reader != null) {
                            readerClass = loadFromJar(
                                    jf, reader, readerClassName);
                        }else System.err.println("Manifest entry "+readerClassName+" does not match any class in the jar.");
                    }
                    if (writerClassName != null) {
                        JarEntry writer = jf.getJarEntry(
                                writerClassName.replace('.', '/') + ".class");

                        if (writer != null) {
                            writerClass = loadFromJar(
                                    jf, writer, writerClassName);
                        } else System.err.println("Manifest entry "+writerClassName+" does not match any class in the jar.");
                    }

                    boolean bPlugin = false;
                    if (doesImplement(readerClass, "tiled.io.MapReader")) {
                        bPlugin = true;
                    }
                    if (doesImplement(writerClass, "tiled.io.MapWriter")) {
                        bPlugin = true;
                    }

                    if (bPlugin) {
                        if (readerClass != null) _add(readerClass);
                        if (writerClass != null) _add(writerClass);
                        //System.out.println(
                        //        "Added " + files[i].getCanonicalPath());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public MapReader[] getReaders() {
        MapReader[] result = new MapReader[readers.size()];
        for (int i = 0; i < readers.size(); ++i) {
          result[i] = (MapReader)(readers.elementAt(i));
        }
        return result;
    }

    public MapWriter[] getWriters() {
        MapWriter[] result = new MapWriter[writers.size()];
        for (int i = 0; i < writers.size(); ++i) {
          result[i] = (MapWriter)(writers.elementAt(i));
        }
        return result;
    }

    public Object getReaderFor(String file) throws Exception {
        Iterator itr = readerFormats.keySet().iterator();
        while (itr.hasNext()){
            String key = (String)itr.next();
            String ext = key.substring(1);
            if (file.toLowerCase().endsWith(ext)) {
                return loadClass((String)readerFormats.get(key)).newInstance();
            }
        }
        throw new Exception(
                "No reader plugin exists for this file type.");
    }

    public Object getWriterFor(String file) throws Exception {
        Iterator itr = writerFormats.keySet().iterator();
        while (itr.hasNext()) {
            String key = (String)itr.next();
            String ext = key.substring(1);
            if (file.toLowerCase().endsWith(ext)) {
                return loadClass((String)writerFormats.get(key)).newInstance();
            }
        }
        throw new Exception(
                "No writer plugin exists for this file type.");
    }

    public Class loadFromJar(JarFile jf, JarEntry je, String className)
        throws IOException
    {
        byte[] buffer = new byte[(int)je.getSize()];
        int n;

        InputStream in = jf.getInputStream(je);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((n = in.read(buffer)) > 0) {
            baos.write(buffer, 0, n);
        }
        buffer = baos.toByteArray();

        if (buffer.length < je.getSize()) {
            throw new IOException(
                    "Failed to read entire entry! (" + buffer.length + "<" +
                    je.getSize() + ")");
        }

        return defineClass(className, buffer, 0, buffer.length);
    }

    private static boolean doesImplement(Class c, String interfaceName)
        throws Exception
    {
        if (c == null) {
            return false;
        }

        Class[] interfaces = c.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            String name = interfaces[i].toString();
            if (name.substring(name.indexOf(' ') + 1).equals(interfaceName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isReader(Class c) throws Exception {
        return doesImplement(c, "tiled.io.MapReader");
    }

    private void _add(Class c) throws Exception{
        try {
            PluggableMapIO p = (PluggableMapIO) c.newInstance();
            String clname = c.toString();
            clname = clname.substring(clname.indexOf(' ') + 1);
            String filter = p.getFilter();
            String[] ext = filter.split(",");

            if (isReader(c)) {
                for (int i = 0; i < ext.length; i++) {
                    readerFormats.put(ext[i], clname);
                }
                readers.add(p);
            } else {
                for (int i = 0; i < ext.length; i++){
                    writerFormats.put(ext[i], clname);
                }
                writers.add(p);
            }
        } catch (NoClassDefFoundError e) {
            System.err.println("**Failed loading plugin: " + e.toString());
        }
    }
}
