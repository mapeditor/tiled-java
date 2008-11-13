/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiled.util;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.String;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

/**
 *
 * @author upachler
 */
public class OverriddenPreferences extends Preferences{
    
    private Preferences shadow;
    private Map<String,String> store = new HashMap<String,String>();
    private Set<String> removed = new HashSet<String>();
    
    public OverriddenPreferences(Preferences shadow){
        this.shadow = shadow;
    }
    
    @Override
    public void put(String key, String value) {
        store.put(key,value);
        removed.remove(key);
    }

    @Override
    public String get(String key, String defaultValue) {
        if(removed.contains(key))
            return defaultValue;
        if(!store.containsKey(key))
            return shadow.get(key, defaultValue);
        return store.get(key);
        
    }

    @Override
    public void remove(String key) {
        store.remove(key);
        removed.add(key);
    }

    @Override
    public String[] keys() throws BackingStoreException {
        Set<String> keySet = new HashSet();
        for(String key : store.keySet())
            keySet.add(key);
        for(String key : shadow.keys())
            keySet.add(key);
        return keySet.toArray(new String[keySet.size()]);
    }

    @Override
    public String[] childrenNames() throws BackingStoreException {
        return shadow.childrenNames();
    }

    @Override
    public Preferences node(String name) {
        return shadow.node(name);
    }

    @Override
    public void clear() throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void putInt(String key, int value) {
        store.put(key, Integer.toString(value));
    }

    @Override
    public int getInt(String key, int def) {
        return Integer.parseInt(get(key, Integer.toString(def)));
    }

    @Override
    public void putLong(String key, long value) {
        put(key, Long.toString(value));
    }

    @Override
    public long getLong(String key, long def) {
        return Long.parseLong(get(key, Long.toString(def)));
    }

    @Override
    public void putBoolean(String key, boolean value) {
        put(key, Boolean.toString(value));
    }

    @Override
    public boolean getBoolean(String key, boolean def) {
        return Boolean.parseBoolean(get(key, Boolean.toString(def)));
    }

    @Override
    public void putFloat(String key, float value) {
        put(key, Float.toString(value));
    }

    @Override
    public float getFloat(String key, float def) {
        return Float.parseFloat(get(key, Float.toString(def)));
    }

    @Override
    public void putDouble(String key, double value) {
        put(key, Double.toString(value));
    }

    @Override
    public double getDouble(String key, double def) {
        return Double.parseDouble(get(key, Double.toString(def)));
    }

    @Override
    public void putByteArray(String key, byte[] value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] getByteArray(String key, byte[] def) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Preferences parent() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean nodeExists(String pathName) throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeNode() throws BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String name() {
        return shadow.name();
    }

    @Override
    public String absolutePath() {
        return shadow.absolutePath();
    }

    @Override
    public boolean isUserNode() {
        return shadow.isUserNode();
    }

    @Override
    public String toString() {
        return shadow.toString() + store.toString();
    }

    @Override
    public void flush() throws BackingStoreException {
        shadow.flush();
    }

    @Override
    public void sync() throws BackingStoreException {
        shadow.sync();
    }

    @Override
    public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addNodeChangeListener(NodeChangeListener ncl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeNodeChangeListener(NodeChangeListener ncl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void exportNode(OutputStream os) throws IOException, BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void exportSubtree(OutputStream os) throws IOException, BackingStoreException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
