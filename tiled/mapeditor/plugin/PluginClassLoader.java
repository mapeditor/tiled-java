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


public final class PluginClassLoader extends URLClassLoader
{
    private Vector plugins;
    private Hashtable readerFormats, writerFormats;
    private static PluginClassLoader instance;
    
    public PluginClassLoader() {
        super(new URL[0]);
        plugins = new Vector();
        readerFormats = new Hashtable();
        writerFormats = new Hashtable();
    }

    public PluginClassLoader(URL[] urls) {
        super(urls);
        plugins = new Vector();
        readerFormats = new Hashtable();
        writerFormats = new Hashtable();
    }

    public static PluginClassLoader getInstance() {
        if (instance == null) {
            instance = new PluginClassLoader();
        }
        return instance;
    }
    
    public void readPlugins(String base, JFrame parent) throws Exception {
        String baseURL = base;
        ProgressMonitor monitor;

        if (base == null) {
            baseURL = TiledConfiguration.getInstance().getValue(
                    "tiled.plugins.dir");
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

        for (int i = 0, j = 0; i < files.length; i++) {
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

                    JarEntry reader = jf.getJarEntry(
                            readerClassName.replace('.', '/') + ".class");
                    JarEntry writer = jf.getJarEntry(
                            writerClassName.replace('.', '/') + ".class");

                    if (readerClassName != null && reader != null) {
                        readerClass = loadFromJar(jf, reader, readerClassName);
                    }
                    if (writerClassName != null && writer != null) {
                        writerClass = loadFromJar(jf, writer, writerClassName);
                    }

                    boolean bPlugin = false;
                    if (doesImplement(readerClass, "tiled.io.MapReader")) {
                        bPlugin = true;
                    }

                    if (doesImplement(writerClass, "tiled.io.MapWriter")) {
                        bPlugin = true;
                    }

                    if (bPlugin) {
                        monitor.setNote("Loading " + aName + "...");
                        //System.out.println(
                        //        "Added " + files[i].getCanonicalPath());
                        super.addURL((new File(aPath)).toURL());
                        _add(readerClass);
                        _add(writerClass);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public MapReader[] getReaders() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        MapReader[] readers = new MapReader[readerFormats.size()];
        Iterator itr = readerFormats.keySet().iterator();
        int i = 0;
        while (itr.hasNext()) {
            Object key = itr.next();
            readers[i++] = (MapReader)loadClass(
                    (String)readerFormats.get(key)).newInstance();
        }

        return readers;
    }

    public MapWriter[] getWriters() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        MapWriter[] writers = new MapWriter[writerFormats.size()];
        Iterator itr = writerFormats.keySet().iterator();
        int i = 0;
        while (itr.hasNext()) {
            Object key = itr.next();
            writers[i++] = (MapWriter)loadClass(
                    (String)writerFormats.get(key)).newInstance();
        }

        return writers;
    }

    public Object getReaderFor(String ext) throws Exception {
        Iterator itr = readerFormats.keySet().iterator();
        while (itr.hasNext()){
            String key = (String)itr.next();
            if (key.endsWith(ext.toLowerCase())) {
                return loadClass((String)readerFormats.get(key)).newInstance();
            }
        }
        throw new Exception(
                "No reader plugin exists for the extension: " + ext);
    }

    public Object getWriterFor(String ext) throws Exception {
        Iterator itr = writerFormats.keySet().iterator();
        while (itr.hasNext()) {
            String key = (String)itr.next();
            if (key.endsWith(ext.toLowerCase())) {
                return loadClass((String)writerFormats.get(key)).newInstance();
            }
        }
        throw new Exception(
                "No writer plugin exists for the extension: " + ext);
    }

    public Class loadFromJar(JarFile jf, JarEntry je, String className) throws IOException {
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

    public boolean doesImplement(Class c, String interfaceName)
        throws Exception {
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

    private boolean isReader(Class c) throws Exception {
        return doesImplement(c,"tiled.io.MapReader");
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
            } else {
                for (int i = 0; i < ext.length; i++){
                    writerFormats.put(ext[i], clname);
                }
            }
        } catch (NoClassDefFoundError e) {
            System.err.println("**Failed loading plugin: " + e.toString());
        }
    }
}
