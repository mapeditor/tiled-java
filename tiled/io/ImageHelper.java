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

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;


/**
 * This class provides functions to help out with saving/loading images.
 */
public class ImageHelper
{
    /**
     * Converts an image to a PNG stored in a byte array.
     *
     * @return a byte array with the PNG data
     */
    static public byte[] imageToPNG(Image image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            BufferedImage buffer = new BufferedImage(
                    image.getWidth(null), image.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB);

            buffer.createGraphics().drawImage(image, 0, 0, null);
            ImageIO.write(buffer, "PNG", baos);
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    /**
     * Converts a byte array into an image.
     *
     * @return the image
     */
    static public Image bytesToImage(byte[] imageData) {
        return Toolkit.getDefaultToolkit().createImage(imageData);
    }
}
