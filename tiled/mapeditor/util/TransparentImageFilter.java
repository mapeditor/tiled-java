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

package tiled.mapeditor.util;

import java.awt.image.RGBImageFilter;

public class TransparentImageFilter extends RGBImageFilter{
	int trans=0;

	public TransparentImageFilter(int col){
		trans=col;
		//System.out.println(trans);
	}

	public int filterRGB(int x,int y,int rgb){
		//System.out.println("does "+rgb+"("+Integer.toBinaryString(rgb)+")=="+trans+"("+Integer.toBinaryString(trans)+")? "+(rgb&trans));		
		if(rgb==trans){
			return 0;
		} else {
			return rgb;			
		}
	}	
}