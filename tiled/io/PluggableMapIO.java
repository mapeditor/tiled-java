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

package tiled.io;

public interface PluggableMapIO {

	
	/**
	 * Check to see if an extension is supported by this I/O plugin
	 * 
	 * @param ext
	 * @return <code>true</code> if it is supported, <code>false</code> otherwise
	 */
	public boolean filter(String ext);
	
	 /**
     * Lists supported file types
     * 
     * @return a comma delimited string of supported file extensions
     * @throws Exception
     */
    public String getFilter() throws Exception;
    
    public String getName();
    
    public String getDescription();
	
}
