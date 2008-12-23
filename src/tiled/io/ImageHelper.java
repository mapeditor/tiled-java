/*
 *  Tiled Map Editor, (c) 2004-2008
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Adam Turk <aturk@biggeruniverse.com>
 *  Bjorn Lindeijer <bjorn@lindeijer.nl>
 */

package tiled.io;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;


/**
 * This class provides functions to help out with saving/loading images.
 *
 * @version $Id$
 */
public class ImageHelper
{
    /**
     * Converts an image to a PNG stored in a byte array.
     *
     * @param image
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

    public static byte[] imageToRAW(Image image, PixelFormat pixelFormat, boolean bigEndian) {        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        int w = image.getWidth(null);
        int h = image.getHeight(null);
        BufferedImage buffer = new BufferedImage(
                w, h,
                BufferedImage.TYPE_INT_ARGB);

        buffer.createGraphics().drawImage(image, 0, 0, null);

        int[] rgbArray = buffer.getRGB(0, 0, w, h, null, 0, w);

        int arraySize = rgbArray.length;
        switch(pixelFormat)
        {
            case A1R5G5B5:{
                for(int i=0; i<arraySize; ++i){
                    int pixel = rgbArray[i];
                    int a = (pixel & 0xff000000) >>> 24;
                    int r = (pixel & 0x00ff0000) >>> 16;
                    int g = (pixel & 0x0000ff00) >>> 8;
                    int b = (pixel & 0x000000ff) >>> 0;

                    int output = 0;
                    if(a >= 0x80)
                        output |= 0x00008000;
                    output |= (r >> 3) << 10;
                    output |= (g >> 3) << 5;
                    output |= (b >> 3) << 0;

                    if(bigEndian){
                        baos.write(output >> 8);
                        baos.write(output);
                    } else {
                        baos.write(output);
                        baos.write(output >> 8);
                    }
                }
            }    break;
            case A8R8G8B8:
            case R8G8B8A8:{
                for(int i=0; i<arraySize; ++i){
                    int pixel = rgbArray[i];
                    int a = (pixel & 0xff000000) >>> 24;
                    int r = (pixel & 0x00ff0000) >>> 16;
                    int g = (pixel & 0x0000ff00) >>> 8;
                    int b = (pixel & 0x000000ff) >>> 0;

                    if(pixelFormat == PixelFormat.A8R8G8B8){
                        if(bigEndian){
                            baos.write(a);
                            baos.write(r);
                            baos.write(g);
                            baos.write(b);
                        } else {
                            baos.write(b);
                            baos.write(g);
                            baos.write(r);
                            baos.write(a);
                        }
                    }else{    // pixelFormat == PixelFormat.R8G8B8A8){
                        if(bigEndian){
                            baos.write(r);
                            baos.write(g);
                            baos.write(b);
                            baos.write(a);
                        } else {
                            baos.write(a);
                            baos.write(b);
                            baos.write(g);
                            baos.write(r);
                        }
                    }
                }
            }    break;
            default:
                throw new IllegalArgumentException("pixel format '" + pixelFormat.toString() + "' not implemented");
        }
        try {
            baos.close();
        }catch(IOException iox){
            iox.printStackTrace();
        }
        
        return baos.toByteArray();
    }

    /**
     * Converts a byte array into an image. The byte array must include the
     * image header, so that a decision about format can be made.
     *
     * @param imageData The byte array of the data to convert.
     * @return Image The image instance created from the byte array
     * @throws IOException
     * @see java.awt.Toolkit#createImage(byte[] imagedata)
     */
    static public BufferedImage pngToImage(byte[] imageData) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(imageData));
    }

    public static Image rawToImage(byte[] imageData, PixelFormat pixelFormat, boolean bigEndian, int width, int height) {
        int[] iArray = new int[width*height];
        switch(pixelFormat){
            case A8R8G8B8: {
                int max = java.lang.Math.min(imageData.length, iArray.length*4);
                if(bigEndian){
                    for(int i=0, j=0; i<max;){
                        int sample;
                        sample  = (0x0ff & imageData[i++]) << 24;
                        sample |= (0x0ff & imageData[i++]) << 16;
                        sample |= (0x0ff & imageData[i++]) << 8;
                        sample |= (0x0ff & imageData[i++]);
                        iArray[j++] = sample;
                    }
                } else {
                    for(int i=0, j=0; i<max;){
                        int sample;
                        sample  = (0x0ff & imageData[i++]);
                        sample |= (0x0ff & imageData[i++]) << 8;
                        sample |= (0x0ff & imageData[i++]) << 16;
                        sample |= (0x0ff & imageData[i++]) << 24;
                        iArray[j++] = sample;
                    }
                }
            }    break;
            case R8G8B8A8: {
                int max = java.lang.Math.min(imageData.length, iArray.length*4);
                if(bigEndian){
                    for(int i=0, j=0; i<max;){
                        int sample;
                        sample  = (0x0ff & imageData[i++]) << 16;
                        sample |= (0x0ff & imageData[i++]) << 8;
                        sample |= (0x0ff & imageData[i++]);
                        sample |= (0x0ff & imageData[i++]) << 24;
                        iArray[j++] = sample;
                    }
                } else {
                    for(int i=0, j=0; i<max;){
                        int sample;
                        sample  = (0x0ff & imageData[i++]) << 24;
                        sample |= (0x0ff & imageData[i++]);
                        sample |= (0x0ff & imageData[i++]) << 8;
                        sample |= (0x0ff & imageData[i++]) << 16;
                        iArray[j++] = sample;
                    }
                }
            }    break;
            case A1R5G5B5: {
                int max = java.lang.Math.min(imageData.length, iArray.length*2);
                for(int i=0, j=0; i<max;){
                    int ssample;
                    if(bigEndian) {
                        ssample  = (0x0ff & imageData[i++]) << 8;
                        ssample |= (0x0ff & imageData[i++]);
                    } else {
                        ssample  = (0x0ff & imageData[i++]);
                        ssample |= (0x0ff & imageData[i++]) << 8;
                    }
                    int a = (ssample&0x8000)!=0 ? 0xff : 0x00;
                    int r = ((ssample&0x7c00) >> 7) | ((ssample&0x7000) >> 12);
                    int g = ((ssample&0x03e0) >> 2) | ((ssample&0x0380) >> 7);
                    int b = ((ssample&0x001f) << 3) | ((ssample&0x001f) >> 2);

                    int sample = (a<<24) | (r<<16) | (g<<8) | (b);
                    iArray[j++] = sample;
                }
            }
        }
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        img.getRaster().setDataElements(0, 0, width, height, iArray);
        
        return img;
    }

    /**
     * This function loads the image denoted by <code>file</code>. This
     * supports PNG, GIF, JPG, and BMP (in 1.5).
     *
     * @param file
     * @return the (partially) loaded image
     * @throws IOException
     */
    static public BufferedImage loadImageFile(File file) throws IOException {
        return ImageIO.read(file);
    }

    public static enum ImageFormat {
        PNG,
        RAW;
        
        public static ImageFormat valueOf(String s, ImageFormat defaultValue){
            try{
                return ImageFormat.valueOf(ImageFormat.class, s);
            } catch(IllegalArgumentException iax){
                // ignore and return default
            }
            return defaultValue;
        }
    }
    
    /** the format names are borrowed from Direct3D (e.g. D3DFORMAT_A8R8G8B8
     * is A8R8G8B8)
     */
    public static enum PixelFormat {
        A8R8G8B8,
        R8G8B8A8,
        A1R5G5B5;

        public static PixelFormat valueOf(String s, PixelFormat defaultValue){
            try{
                return PixelFormat.valueOf(PixelFormat.class, s);
            } catch(IllegalArgumentException iax){
                // ignore and return default
            }
            return defaultValue;
        }
    }
    
    private static class SynchronizedImageObserver implements ImageObserver{
        int width = -1;
        int height = -1;
        synchronized void setValue(int value, int infoflag){
            switch(infoflag){
                case ImageObserver.WIDTH:
                    width = value;
                    break;
                case ImageObserver.HEIGHT:
                    height = value;
                    break;
            }
            notifyAll();
        }
        
        /**
         * gets an image property value from the image observer, specified
         * by the infoflag parameter.
         * @param infoflag One of the values specified in ImageObserver, e.g.
         * ImageObserver.WIDTH.
         * @return the requirested value. -1 if the value hasn't been set yet.
         */
        synchronized int getValue(int infoflag){
            switch(infoflag){
                case ImageObserver.WIDTH:
                    return width;
                case ImageObserver.HEIGHT:
                    return height;
            }
            return -1;
        }
            
        public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
            if(infoflags == ImageObserver.ALLBITS)
                return false;
            if((infoflags & ImageObserver.WIDTH) != 0)
                setValue(width, ImageObserver.WIDTH);
            if((infoflags & ImageObserver.HEIGHT) != 0)
                setValue(height, ImageObserver.HEIGHT);
            return true;
        }
    }
    
    /**
     * This grabs the width of the specified image. If the width is not
     * available, this method will block until it becomes available
     * @param image
     * @return
     */
    public static int getImageWidth(Image image){
        SynchronizedImageObserver o = new SynchronizedImageObserver();
        
        // we use the image observer as lock
        synchronized(o)
        {
            while(true){
                o.setValue(image.getWidth(o), ImageObserver.WIDTH);
                if(o.getValue(ImageObserver.WIDTH) != -1)
                    break;
                try {
                    o.wait();
                } catch (InterruptedException ex) {
                }
            }
            return o.getValue(ImageObserver.WIDTH);
        }
    }
    /**
     * This grabs the width of the specified image. If the width is not
     * available, this method will block until it becomes available
     * @param image
     * @return
     */
    public static int getImageHeight(Image image){
        SynchronizedImageObserver o = new SynchronizedImageObserver();
        
        // we use the image observer as lock
        synchronized(o)
        {
            while(true){
                o.setValue(image.getHeight(o), ImageObserver.HEIGHT);
                if(o.getValue(ImageObserver.HEIGHT) != -1)
                    break;
                try {
                    o.wait();
                } catch (InterruptedException ex) {
                }
            }
            return o.getValue(ImageObserver.HEIGHT);
        }
    }
}
