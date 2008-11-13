/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiled.mapeditor.widget;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 *
 * @author count
 */
public class FloatDocument extends PlainDocument {
     public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
         if (str == null)
             return;
         
         try{
             Float.parseFloat(str);
         }catch(NumberFormatException nfx){
             return;
         }
         super.insertString(offs, str, a);
     }
}
