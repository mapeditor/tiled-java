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

import java.io.File;
import java.io.IOException;


public class Util {
	 public static byte [] convertIntegersToBytes (int [] integers) {

	 	if ( integers !=null ) {
	 		
	 		byte outputBytes[] = new byte[integers.length * 4];

	 		

	 		for(int i=0, k=0; i < integers.length; i++) {
	 				int integerTemp = integers[i];
	 				for(int j=0; j < 4; j++, k++) {
	 					outputBytes[k] = (byte) ( (integerTemp >> (8*j) ) & 0xFF );
	 				}
	 		}
	 		return outputBytes;
	 	} else {
	 		return null;
	 	}

	 }
	 
	 public static boolean checkRoot(String filename) {
	 	File [] roots = File.listRoots();
	 	
	 	for(int i=0;i<roots.length;i++) {
	 		try {
                if(filename.startsWith(roots[i].getCanonicalPath())) {
                	return true;
                }
            } catch (IOException e) {
            	//Do we care?
            }
	 	}
	 	
	 	return false;
	 }
}
