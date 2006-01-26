package tiled.mapeditor.util.cutter;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class BasicTileCutter implements TileCutter {

	private int nextX, nextY;
	private BufferedImage image;
	private int tileWidth, tileHeight, frame, offset;
	
	public BasicTileCutter(int width, int height, int frame, int offset) {
		this.tileWidth = width;
		this.tileHeight = height;
		this.frame = frame;
		this.offset = offset;
		
		//do initial setup
		nextX = offset+frame;
		nextY = offset+frame;
	}
	
	public void setImage(Image image) {
		int iw = image.getWidth(null);
		int ih = image.getHeight(null);
		this.image = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_ARGB);
		//FIXME: although faster, the following doesn't seem to handle alpha on some platforms...
        //GraphicsConfiguration config =
        //    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        //Image tilesetImage = config.createCompatibleImage(tileWidth, tileHeight);
        //Graphics tg = tilesetImage.getGraphics();
		Graphics2D tg = this.image.createGraphics();
		
		tg.drawImage(image, 0, 0,
                iw, ih,
                0, 0, iw, ih,
                null);
	}

	public Image getNextTile() throws Exception {
		
		if(nextY + tileHeight < image.getHeight()) {
			BufferedImage tile = image.getSubimage(nextX, nextY, tileWidth, tileHeight);
			nextX += tileWidth+frame;
			
			if(nextX+tileWidth > image.getWidth()) {
				nextX = offset + frame;
				nextY += tileHeight+frame;
			}
			
			return tile;
		}
		
		return null;
	}

	public Dimension getDimensions() {
		return new Dimension(tileWidth, tileHeight);
	}
}
