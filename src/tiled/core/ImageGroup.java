/*
 *  Tiled Map Editor, (c) 2004-2006
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Rainer Deyke <rainerd@eldwood.com>
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <b.lindeijer@xs4all.nl>
 */

package tiled.core;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;

/**
 * An ImageGroup store a base image and variations of that base image in a
 * single object.
 *
 * @version $Id$
 */
public class ImageGroup {
    private Image[] images;

    /**
     * Constructs an ImageGroup from a base Image.
     */
    public ImageGroup(Image img) {
        images = new Image[8];
        images[0] = img;
    }

    /**
     * Retrieves the image with the specified orientation.
     *
     * @param orientation
     */
    public Image getImage(int orientation) {
        if (images[orientation] == null) {
            images[orientation] = orientImage(images[0], orientation);
        }
        return images[orientation];
    }

    /**
     * Generates a image that is identical to the source image except that it
     * is rotated and/or flipped.
     */
    public static Image orientImage(Image src, int orientation) {
        if (orientation == 0) {
            return src;
        } else {
            int w = src.getWidth(null), h = src.getHeight(null);
            int[] old_pixels = new int[w * h];
            int[] new_pixels = new int[w * h];

            PixelGrabber p = new PixelGrabber(src, 0, 0, w, h, old_pixels, 0, w);
            try {
                p.grabPixels();
            } catch (InterruptedException e) { }


            int dest_w = ((orientation & 4) != 0) ? h : w;
            int dest_h = ((orientation & 4) != 0) ? w : h;
            for (int dest_y = 0; dest_y < dest_h; ++dest_y) {
                for (int dest_x = 0; dest_x < dest_w; ++dest_x) {
                    int src_x = dest_x, src_y = dest_y;
                    if ((orientation & 4) != 0) {
                        src_y = dest_w - dest_x - 1;
                        src_x = dest_y;
                    }

                    if ((orientation & 1) != 0) {
                        src_x = w - src_x - 1;
                    }
          if ((orientation & 2) != 0) {
            src_y = h - src_y - 1;
          }
          new_pixels[dest_x + dest_y * dest_w] = old_pixels[src_x + src_y * w];
        }
      }

      BufferedImage new_img = new BufferedImage(dest_w, dest_h,
          BufferedImage.TYPE_INT_ARGB);
      new_img.setRGB(0, 0, dest_w, dest_h, new_pixels, 0, dest_w);
      return new_img;
    }
  }

  /**
   * Returns true if the other object is also an ImageGroup with the same
   * base image.
   */
  public boolean equals(Object o)
  {
    try {
      return this.images[0].equals(((ImageGroup)o).images[0]);
    } catch (java.lang.ClassCastException e) {
      return false;
    }
  }
}
