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
 
package tiled.core;

import java.awt.*;

/**
 * @author Adam Turk
 *
 */

public class Sprite extends TiledEntity{

    private KeyFrame keys=null,
    currentKey=null;
    private int totalFrames=0,
    borderWidth=0,
    fpl=0,
    totalKeys=-1,
    transparent=0;

    private float currentFrame=0;
    private Rectangle frameSize;
    private Image sprite=null;
    private boolean bPlaying=true;

    public Sprite() {
        frameSize = new Rectangle();
    }

    public Sprite(Image image,int fpl, int border,int totalFrames) {
        setImage(image);
        this.fpl=fpl;
        borderWidth=border;
        this.totalFrames=totalFrames;

        //given this information, extrapolate the rest...
        frameSize=new Rectangle(0,0,0,0);
        frameSize.width=image.getWidth(null)/(fpl+borderWidth*fpl);
        frameSize.height=(int) (image.getHeight(null)/(Math.ceil(totalFrames/fpl)+Math.ceil(totalFrames/fpl)*borderWidth));

    }

    public void setImage(Image i) {
        sprite = i;
    }

    public Image getImage() {
        return sprite;
    }

    public void setFrameSize(int w, int h) {
        frameSize.width=w;
        frameSize.height=h;
    }

    public void setTotalFrames(int f) {
        totalFrames=f;
    }

    public void setBorderWidth(int b) {
        borderWidth=b;
    }

    public void setFpl(int f) {
        fpl=f;
    }

    public void setCurrentFrame(int c) {
        currentFrame=c;
    }

    public void setTotalKeys(int t) {
        totalKeys=t;
    }

    public void setTransparentColor(int t) {
        transparent=t;
    }

    public int getTransparentColor() {
        return transparent;
    }

    public Rectangle getFrameSize() {
        return(frameSize);
    }

    public int getTotalFrames() {
        return(totalFrames);
    }

    public int getBorderWidth() {
        return(borderWidth);
    }

    public int getCurrentFrame() {
        return((int)currentFrame);
    }

    public KeyFrame getCurrentKey() {
        return currentKey;
    }

    public int getFPL() {
        return fpl;
    }

    public int getTotalKeys() {
        KeyFrame temp=keys;
        totalKeys=0;
        while (temp!=null) {
            totalKeys++;
            temp=temp.next();
        }
        return totalKeys;
    }

    public void setKeyFrameTo(String name) {
        KeyFrame temp=keys;
        while (temp!=null) {
            if (temp.equalsIgnoreCase(name)) {
                setCurrentFrame(temp.getStartFrame());
                currentKey=temp;
                break;
            }
            temp=temp.next();
        }
    }

    private void addKeyInternal(KeyFrame k) {
        KeyFrame temp=keys;
        if (keys==null) {
            keys=k;
            currentKey=k;
            return;
        }
        while (temp.next()!=null) {
            temp=temp.next();
        }

        temp.setNext(k);
    }

    public void addKey(KeyFrame k) {
        KeyFrame temp=keys;
        totalKeys++;
        if (keys==null) {
            keys=k;
            currentKey=k;
            k.setId(0);
            return;
        }
        while (temp.next()!=null) {
            temp=temp.next();
        }

        temp.setNext(k);
        k.setId(temp.getId()+1);
    }

    public void removeKey(String name) {
        KeyFrame temp=keys,prev=keys;
        if (keys!=null&&keys.equalsIgnoreCase(name)) {
            keys=keys.next();
            return;
        }
        if (temp!=null) {
            temp=temp.next();
        }
        while (temp!=null) {
            if (temp.equalsIgnoreCase(name)) {
                prev.setNext(temp.next());
                break;
            }
            prev=temp;
            temp=temp.next();
        }
    }

    public void createKey(String name, int start, int end, long flags) {
        KeyFrame kf = new KeyFrame();
        kf.setName(name);
        kf.setFlags(flags);
        kf.setStartFinish(start,end);
        addKey(kf);
    }

    public void iterateFrame() {

        if (currentKey!=null) {
            if (bPlaying) {
                currentFrame+=currentKey.getFrameRate();
            }

            if ((int)currentFrame>currentKey.getFinishFrame()) {
                if ((currentKey.getFlags()&KeyFrame.KEY_LOOP)==KeyFrame.KEY_LOOP) {
                    currentFrame=currentKey.getStartFrame();
                }else if ((currentKey.getFlags()&KeyFrame.KEY_REVERSE)==KeyFrame.KEY_REVERSE) {
                    currentKey.setFrameRate(-currentKey.getFrameRate());
                }else if ((currentKey.getFlags()&KeyFrame.KEY_AUTO)==KeyFrame.KEY_AUTO) {
                    currentKey=currentKey.next();
                    if (currentKey!=null) {
                        currentFrame = currentKey.getStartFrame();
                    }
                } else {
                    currentFrame=currentKey.getFinishFrame();
                    bPlaying=false;
                }
            }else if ((int)currentFrame<currentKey.getStartFrame()) {
                if ((currentKey.getFlags()&KeyFrame.KEY_LOOP)==KeyFrame.KEY_LOOP) {
                    currentFrame=currentKey.getFinishFrame();
                }else if ((currentKey.getFlags()&KeyFrame.KEY_REVERSE)==KeyFrame.KEY_REVERSE) {
                    currentKey.setFrameRate(-currentKey.getFrameRate());
                } else {
                    bPlaying=false;
                }
            }

        }
    }

    public void keySetFrame(int c) {
        setCurrentFrame(currentKey.getStartFrame()+c);
    }

    public void play() {
        bPlaying = true;
    }

    public void stop() {
        bPlaying=false;
    }

    public void keyStepBack(int amt) {
        if (currentFrame-amt<currentKey.getStartFrame()) {
            setCurrentFrame(currentKey.getStartFrame());
        } else {
            setCurrentFrame((int)(currentFrame-amt));
        }
    }

    public void keyStepForward(int amt) {
        if (currentFrame+amt>currentKey.getFinishFrame()) {
            setCurrentFrame(currentKey.getFinishFrame());
        } else {
            setCurrentFrame((int)(currentFrame+amt));
        }
    }

    public KeyFrame getKey(String keyName) {
        KeyFrame temp=keys;
        while (temp!=null) {
            if (temp.equalsIgnoreCase(keyName)) {
                break;
            }
            temp=temp.next();
        }
        return temp;
    }

    public String[] getKeys() throws Exception{
        KeyFrame temp=keys;
        if (temp==null) {
            throw new Exception("No Keys!");
        }
        String [] s = new String [totalKeys+1];
        int i=0;
        while (temp!=null) {
            s[i++] = temp.getName();
            temp=temp.next();
        }
        return s;
    }

    public void draw(Graphics g) {
        int x=0, y=0;
        y=(((int)currentFrame)/fpl)*(frameSize.height+borderWidth);
        x=(((int)currentFrame)%fpl)*(frameSize.width+borderWidth);

        //System.out.println(""+currentFrame+": ("+x+"x"+y+")->("+frameSize.width+"x"+frameSize.height+")");
        g.drawImage(sprite,0,0,frameSize.width,frameSize.height,x,y,frameSize.width+x,frameSize.height+y,null);
    }

    public void drawAll(Graphics g) {
        g.drawImage(sprite,0,0,null);
    }

    public String toString() {
        String s = null;
        s = "Frame: ("+frameSize.width+"x"+frameSize.height+")\nBorder: "+borderWidth+"\nFPL: "+fpl+"\nTotal Frames: "+totalFrames+"\nTotal keys: "+totalKeys;
        return s;
    }

}

