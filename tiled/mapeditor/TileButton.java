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

package tiled.mapeditor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.EventListenerList;

import tiled.core.*;

public class TileButton extends JComponent implements MouseListener
{
    private String actionCommand;
    private EventListenerList actionListeners;
    private Tile tile;
    private Dimension size;
    private boolean maintainAspect;

    public TileButton( Tile t, Dimension d ) {
        tile = t;
        size = d;
        maintainAspect = false;
        actionListeners = new EventListenerList( );
        addMouseListener( this );
    }

    public TileButton( Dimension d ) {
        this( null, d );
    }

    public TileButton( Tile t ) {
        this( t, null );
    }

    public TileButton( ) {
        this( null, null );
    }

    public void setCurrentTile( Tile t ) {
        tile = t;
        revalidate( );
        repaint( );
    }

    /*
     *  Methods for Size Information 
     */
    private Dimension calculatePreferredSize( ) {
        Insets i = getInsets( );

        if( tile != null ) {
            int w = tile.getWidth( ) + i.left + i.right;
            int h = tile.getHeight( ) + i.top + i.bottom;
            return new Dimension( w, h );
        }

        return null;
    }

    private Dimension calculateInnerSize( ) {
        Insets i = getInsets( );
        int w = getWidth( ) - i.left - i.right;
        int h = getHeight( ) - i.top - i.bottom; 
        return new Dimension( w, h );
    }

    public Dimension getPreferredSize( ) {
        Dimension d;

        if( maintainAspect ) {
            if( ( d = calculatePreferredSize( ) ) != null )	{
                Dimension s = new Dimension( );
                s.width  = getWidth( );
                s.height = (int)
                    (getWidth( ) / ( (double)d.width / (double)d.height) );
                return s;
            }
        }
        else if( size != null ) {
            return size;
        }
        else if( ( d = calculatePreferredSize( ) ) != null ) {
            return d;
        }
        d = super.getPreferredSize( );
        if( d.height < 2 ) {
            d.height = 5;
        }
        if( d.width < 2 ) {
            d.height = 5;
        }
        return d;
    }

    public void setMaintainAspect( boolean v ) {
        maintainAspect = v;
    }

    public boolean isAspectMaintained( ) {
        return maintainAspect;
    }

    /*
     *  Methods for Mouse Events
     */
    public void mouseEntered( MouseEvent e ) { }
    public void mouseExited( MouseEvent e ) { }
    public void mousePressed( MouseEvent e ) { }
    public void mouseReleased( MouseEvent e ) { }

    public void mouseClicked( MouseEvent e ) {
        requestFocusInWindow( );
        fireActionPerformed( new ActionEvent( this, 0, actionCommand ) );
    }

    protected void paintComponent( Graphics t ) {
        Graphics g = t.create( );
        Insets i = getInsets( );

        if( tile != null ) {
            if( maintainAspect ) {
                int w = getWidth( ) - i.left - i.right;
                double per = (double)w / (double)tile.getWidth( );
                tile.drawRaw( g, i.left, i.top, per );
            }
            else {
                tile.drawRaw( g, i.left, i.top, 1.0 );
            }
        }
        else { 
            g.setColor( new Color( 255, 255, 255 ) );
            Dimension s = calculateInnerSize( );
            g.fillRect( i.left, i.top, s.width, s.height );
        }

        g.dispose( );
    }

    /*
     *   Action centric methods.
     */
    public void setActionCommand( String a ) {
        actionCommand = a;
    }
    public String getActionCommand( ) {
        return actionCommand;
    }

    public void addActionListener( ActionListener l ) {
        actionListeners.add( ActionListener.class, l );
    }

    public void removeActionListener( ActionListener l ) {
        actionListeners.remove( ActionListener.class, l );
    }

    protected void fireActionPerformed( ActionEvent e ) {
        Object[] listeners = actionListeners.getListenerList( );

        for( int i = listeners.length - 2; i >= 0; i -= 2 ) {
            if( listeners[i] == ActionListener.class ) {
                ((ActionListener)listeners[i + 1]).actionPerformed( e );
            }
        }
    }
}
