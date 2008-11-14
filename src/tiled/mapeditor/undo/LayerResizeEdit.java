/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiled.mapeditor.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import tiled.core.MapLayer;
import tiled.core.Tile;
import tiled.core.TileLayer;
import tiled.mapeditor.Resources;

/**
 * Captures layer resizes to make them undoable.
 * @author upachler
 */
public class LayerResizeEdit extends AbstractUndoableEdit{
    private MapLayer layer;
    private Backup backup = null;
    
    private static class Backup{
        public Rect resizeRect;
        public Rect[] rasterRects;
        public TileRaster[] rasters;
    }
    
    private static class Rect{
        int x0;
        int y0;
        int x1;
        int y1;
        
        Rect(int x0, int y0, int x1, int y1)
        {
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
        }
        
        Rect(Rect rhs){
            this.x0 = rhs.x0;
            this.y0 = rhs.y0;
            this.x1 = rhs.x1;
            this.y1 = rhs.y1;
        }
        
        boolean intersects(Rect rect)
		{
			// check if rect is outside
			if(x1 <= rect.x0 || y1 <= rect.y0 || x0 >= rect.x1 || y0 >= rect.y1)
				return false;
			return true;
		}
		
        void extend(int x, int y){
            x0 = Math.min(x0, x);
            x1 = Math.max(x1, x);
            y0 = Math.min(y0, y);
            y1 = Math.min(y1, y);
        }
        
        void extend(Rect rhs){
            extend(rhs.x0, rhs.y0);
            extend(rhs.x1, rhs.y1);
        }
        
		Rect[] difference(Rect rhs)
		{
			// if Rectangles don't intersect, the result is the original
			if(!intersects(rhs))
				return new Rect[]{new Rect(this)};
			
			// otherwise, we might end up with up to four fragments
            Rect[] out = new Rect[4];
            int numRects = 0;
			
			// A
			if(x0 < rhs.x0)
				out[numRects++] = new Rect(x0, y0, rhs.x0, y1);
			
			// B
			if(rhs.x1 < x1)
				out[numRects++] = new Rect(rhs.x1, y0, x1, y1);
			
			// C
			if(y0 < rhs.y0)
				out[numRects++] = new Rect(rhs.x0, y0, rhs.x1, rhs.y0);
			
			// D
			if(rhs.y1 < y1)
				out[numRects++] = new Rect(rhs.x0, rhs.y1, rhs.x1, y1);
            if(numRects==out.length)
                return out;
            Rect[] outShrunk = new Rect[numRects];
            for(int i=0; i<numRects; ++i)
                outShrunk[i] = out[i];
            return outShrunk;
		}        
    }
        
    private static class TileRaster{
        Tile[][] raster;
        private int width;
        private int height;

        private int getHeight() {
            return height;
        }

        private int getWidth() {
            return width;
        }
        
        private Tile[][] makeRaster(int width, int height){
            Tile[][] r = new Tile[width][];
            for(int x=0; x<width; ++x)
                r[x] = new Tile[height];
            return r;
        }
        public TileRaster(int width, int height)
        {
            this.width = width;
            this.height = height;
            raster = makeRaster(width, height);
        }
        
        /**
         * Copies a rectangular area from the source TileRaster to an specified
         * target position in this raster. The source rectangle is defined by sourceX,
         * sourceY, sourceWidth and sourceHeight in source coordinates. targetX
         * and targetY specify the target coordinates in this raster where the
         * rectangle is copied to.
         * @throws ArrayIndexOutOfBoundsException if the source coordinates are
         * outside the range of the source rectangle. target coordinates may
         * assume any value.
         */
        public void copyRect(int targetX, int targetY, TileRaster source, int sourceX, int sourceY, int sourceWidth, int sourceHeight){
            // apply clipping to find out the actual target rectangle witdh
            // and height
            
            // find out source boundaries limited by available space in target rectangle
            
            // if targetX and/or targetY are negative, this will adjust the sourceX/Y
            // coordinates accordingly
            int sourceX0 = sourceX - Math.min(0, targetX);
            int sourceY0 = sourceY - Math.min(0, targetY);
            
            // in case the source rectangle reaches over the size of the target
            // rectangle, limit the source rectangle to what actually needs to
            // be copied into the target
            final int sourceX1 = sourceX + Math.min(getWidth()-targetX, sourceWidth);
            final int sourceY1 = sourceY + Math.min(getHeight()-targetY, sourceHeight);
            
            // check if there's actually something to copy
            if(sourceX0 >= sourceX1 || sourceY0 >= sourceY1)
                return;
            
            final int sh = sourceY1-sourceY0;
            final int sy = sourceY0;
            final int ty = Math.max(0, targetY);
            int tx = Math.max(0, targetX);
            for(int sx=sourceX0; sx<sourceX1; ++sx, ++tx){
                System.arraycopy(source.raster[sx], sy, raster[tx], ty, sh);
            }
        }
        
        public void set(int x, int y, Tile tile){
            raster[x][y] = tile;
        }
        
        public Tile get(int x, int y){
            return raster[x][y];
        }
    }

    @Override
    public String getPresentationName() {
        return Resources.getString("edit.changelayerdimension.name");
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();
        swap();
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        swap();
    }

    private void swap(){
        Rect r = backup.resizeRect;
        Backup b = capture(r.x0, r.y0, r.x1-r.x0, r.y1-r.y0);
        apply(backup);
        backup = b;
    }
    
    /**
     * Creates a new LayerResizeEdit. The edit must be created from the layer
     * <i>before</i> the layer is resized.
     * @param layer the layer that will be resized
     * @param x the new layer extents' x position relative to the current layer extents
     * @param y new new layer extents' y position relative to the current layer extents
     * @param width the new layer width
     * @param width the new layer height
     */ 
    public LayerResizeEdit(MapLayer layer, int x, int y, int width, int height){
        this.layer = layer;
        backup = capture(x, y, width, height);
    }
    
    private Backup capture(int x, int y, int width, int height){
        Backup backup = new Backup();
        
        // store resizeRect that is required to undo the requested resize
        backup.resizeRect = new Rect(-x, -y, layer.getWidth(), layer.getHeight());
        
        // see if this layer is a TileLayer. If it is not, there's nothing more
        // we need to do
        TileLayer tileLayer = null;
        try{
            tileLayer = (TileLayer)layer;
        }catch(ClassCastException ccx){
            return backup;
        }
        
        // FIXME: TileRaster is only parked in this class temporarily.
        // The intention is to make it the storage medium for tiles within
        // a MapLayer, replacing the current implementation within MapLayer.
        //
        // however, for now, the the layer's tile data is simply copied into
        // a new TileRaster, which is then used for all subsequent operations.
        // When MapLayer uses TileRasters for storing tile placement data,
        // the following slow copy code should be replaced by a getTileRaster()
        // call.
        TileRaster r = createTileRasterFromLayer(tileLayer);
        
        // make backup copies of areas that will be truncated by resize
        Rect currentDimensions = new Rect(0, 0, tileLayer.getWidth(), tileLayer.getHeight());
        Rect newDimensions = new Rect(x, y, x+width, y+height);
        backup.rasterRects = currentDimensions.difference(newDimensions);
        backup.rasters = new TileRaster[backup.rasterRects.length];
        for(int i=0; i<backup.rasterRects.length; ++i){
            int backupSourceX = backup.rasterRects[i].x0;
            int backupSourceY = backup.rasterRects[i].y0;
            int backupWidth = backup.rasterRects[i].x1-backup.rasterRects[i].x0;
            int backupHeight = backup.rasterRects[i].y1-backup.rasterRects[i].y0;
            backup.rasters[i] = new TileRaster(backupWidth, backupHeight);
            backup.rasters[i].copyRect(0, 0, r, backupSourceX, backupSourceY, backupWidth, backupHeight);
        }
        
        return backup;
    }
    
    private void apply(Backup backup){
        int newX = backup.resizeRect.x0;
        int newY = backup.resizeRect.y0;
        int newWidth = backup.resizeRect.x1-backup.resizeRect.x0;
        int newHeight = backup.resizeRect.y1-backup.resizeRect.y0;
        layer.resize(newWidth, newHeight, newX, newY);
        
        if(backup.rasters == null)
            return;
        
        TileLayer tlayer = (TileLayer)this.layer;
        
        // FIXME: once TileLayer finally uses TileRaster for storage, this
        // function should be replaced by getLayer
        TileRaster targetRaster = createTileRasterFromLayer(tlayer);
        
        for(int i=0; i<backup.rasterRects.length; ++i){
            TileRaster sRaster = backup.rasters[i];
            Rect sRect = backup.rasterRects[i];
            int tx = sRect.x0-newX;
            int ty = sRect.y0-newY;
            int sw = sRect.x1-sRect.x0;
            int sh = sRect.y1-sRect.y0;
            targetRaster.copyRect(tx, ty, sRaster, 0, 0, sw, sh);
        }
        
        // FIXME: once TileLayer finally uses TileRaster for storage, this
        // function call should be removed, as targetRaster is no longer
        // just a copy
        copyTileRasterToLayer(tlayer, targetRaster);
    }

    private void copyTileRasterToLayer(TileLayer tlayer, TileRaster raster) {
        // FIXME: this is slow. The function should not exist actually;
        // TileRaster should be a part of TileLayer.
        int xoff = tlayer.getBounds().x;
        int yoff = tlayer.getBounds().y;
        int width = tlayer.getWidth();
        int height = tlayer.getHeight();
        for(int x=0; x<width; ++x){
            for(int y=0; y<height; ++y){
                Tile t = raster.get(x, y);
                tlayer.setTileAt(x+xoff, y+yoff, t);
            }
        }
    }

    private TileRaster createTileRasterFromLayer(TileLayer tlayer) {
        // FIXME: this is slow. The function should not exist actually;
        // TileRaster should be a part of TileLayer.
        int xoff = tlayer.getBounds().x;
        int yoff = tlayer.getBounds().y;
        int width = tlayer.getWidth();
        int height = tlayer.getHeight();
        TileRaster raster = new TileRaster(width, height);
        for(int x=0; x<width; ++x){
            for(int y=0; y<height; ++y){
                Tile t = tlayer.getTileAt(x+xoff, y+yoff);
                raster.set(x, y, t);
            }
        }
        return raster;
    }
}
