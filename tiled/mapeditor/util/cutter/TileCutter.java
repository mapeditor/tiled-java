package tiled.mapeditor.util.cutter;

import java.awt.Image;

public interface TileCutter {
	public void setImage(Image image);
	public Image getNextTile() throws Exception;
}
