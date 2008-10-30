/*
 *  Tiled Map Editor, (c) 2004-2006
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  JSON map writer contributed by
 *  Nader Akhres <nader.akhres@laposte.net>
 */

package tiled.plugins.json;

import java.text.ParseException;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Contains utilities to extend JSON capabilities.
 */
public class JSONUtils
{
    /**
     * Constructor.
     */
    public JSONUtils() {
        //nothing
    }

    /**
     * This method takes an object that is JSONArray already or JSONObject and
     * convert it into JSONArray this is useful because JSON turn an xml entry
     * into array or object according to number of entries using this method
     * will avoid to treat as special cases whether you have one entrie or
     * several.
     *
     * @param obj
     * @return JSONArray
     * @throws ParseException
     */
    public static JSONArray getAsJSONArray(Object obj) throws ParseException {
        // We always convert to a json array: json object become a json array
        // with one item
        JSONArray result = null;

        if (obj instanceof JSONArray) {
            result = (JSONArray) obj;
        } else if (obj instanceof JSONObject) {
            result = new JSONArray();
            result.put(obj);
        } else {
            throw new ParseException("problem while interpreting "+obj,0);
        }

        return result;
    }

    /**
     * Unit test.
     *
     * @param args
     */
    /*
    public static void main(String[] args) {
        //test
    }
    */
}
