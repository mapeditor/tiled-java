package mappy;

import java.io.*;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.*;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Iterator;

import tiled.io.MapReader;
import tiled.core.*;

public class MappyMapReader implements MapReader {

    private LinkedList chunks;
    private Vector blocks;
    private static final int BLKSTR_WIDTH=32;
    private int twidth, theight;

    private static class BlkStr {
	public long bg,fg0,fg1,fg2;
	public long user1, user2;   //user long data
	public int user3, user4;    //user short data
	public int user5, user6, user7;   //user byte data
	public int bits;	    //collision and trigger bits
    };

    /**
     * Loads a map from a file.
     *
     * @param filename the filename of the map file
     */
    public Map readMap(String filename) throws Exception {
	Map ret = null;
	chunks = new LinkedList();
	blocks = new Vector();
	byte [] hdr = new byte[4];
	FileInputStream fis = new FileInputStream(filename);
	
	fis.read(hdr);
	long size = readLongReverse(fis);
	fis.read(hdr);	

	try{
	    Chunk chunk = new Chunk(fis);
	    while(chunk.isGood()) {
		chunks.add(chunk);
		chunk = new Chunk(fis);
	    }
	} catch(IOException ioe){}

	//now build a Tiled map...
	Chunk c = findChunk("MPHD");	
	if(c != null) {
	    ret = readMPHDChunk(c.getInputStream());
	}else{
	    throw new IOException("No MPHD chunk found!");
	}

	c = findChunk("BODY");
	if(c != null) {
            readBODYChunk(ret, c.getInputStream());
        }else{
            throw new IOException("No BODY chunk found!");
        }

	return ret;
    }
                                                                                
    /**
     * Loads a tileset from a file.
     *
     * @param filename the filename of the tileset file
     */
    public TileSet readTileset(String filename) throws Exception {
	System.out.println("Tilesets aren't supported!");
	return null;
    }

    /**
     * @see tiled.io.MapReader#getFilter()
     */
    public String getFilter() throws Exception {
        return "*.fmp";
    }

    public String getPluginPackage() {
	return "Mappy Reader/Writer Plugin";
    }

    public String getDescription() {
        return "+---------------------------------------------+\n| An experimental reader for Mappy FMAP (v0.36) |\n|      (c) Adam Turk 2004               |\n|         aturk@biggeruniverse.com               |\n+-------------------------------------------+";
		
    }
                                                                                
    public String getName() {
        return "Mappy Reader";
    }
                                                                                
    public boolean accept(File pathname) {
        try {
            String path = pathname.getCanonicalPath().toLowerCase();
            if (path.endsWith(".fmp")) {
                return true;
            }
        } catch (IOException e) {}
        return false;
    }

    private long readLongReverse(InputStream in) throws IOException {
	int a = in.read();
	int b = in.read();
	int c = in.read();
	int d = in.read();

	return (long)((a<<24)|(b<<16)|(c<<8)|d);
    }

    private int readShortReverse(InputStream in) throws IOException {
	int a = in.read();
	int b = in.read();
	return (int)((a<<8)|b);
    }

    private int readShort(InputStream in) throws IOException {
	int a = in.read();
        int b = in.read();
        return (int)(a|(b<<8));
    }

    private Chunk findChunk(String header) {
	Iterator itr = chunks.iterator();

	while(itr.hasNext()) {
	    Chunk c = (Chunk)itr.next();
	    if(c.equals(header)) {
		return c;
	    }
	}
	return null;
    }

    private Map readMPHDChunk(InputStream in) throws IOException {
	Map ret = null;
	TileSet set = new TileSet();
	int major, minor;
	major = in.read();
	minor = in.read();
	in.skip(2);	//skip lsb and reserved bytes - always msb
	ret = new Map(readShort(in), readShort(in));
	ret.addProperty("(s)fmap reader","Don't modify properties marked (s) unless you really know what you're doing.");
	ret.addProperty("version",""+major+"."+minor);
	in.skip(4);	//reserved
	twidth = readShort(in);
	theight = readShort(in);
	set.setStandardWidth(twidth);
	set.setStandardHeight(theight);
	ret.setTileWidth(twidth);
	ret.setTileHeight(theight);
	set.setName("Static tiles");
	ret.addTileset(set);
	int depth = readShort(in);
	if(depth<16) {
		throw new IOException("Tile bitdepths less than 16 are not supported!");
	}
	ret.addProperty("(s)depth",""+depth);
	in.skip(2);
	int numBlocks = readShort(in);
	int numBlocksGfx = readShort(in);
	Chunk c = findChunk("BKDT");
	if(c == null) {
		throw new IOException("No BKDT block found!");
	}
	MapLayer ml = new MapLayer(ret, ret.getWidth(),ret.getHeight());
	ml.setName("bg");
	ret.addLayer(ml);
	ml = new MapLayer(ret, ret.getWidth(),ret.getHeight());
	ml.setName("fg 1");
	ret.addLayer(ml);
	ml = new MapLayer(ret, ret.getWidth(),ret.getHeight());
	ml.setName("fg 2");
	ret.addLayer(ml);
	ml = new MapLayer(ret, ret.getWidth(),ret.getHeight());
	ml.setName("fg 3");
	ret.addLayer(ml);
	
	for(int i=1;i<8;i++) {
		ml = new MapLayer(ret, ret.getWidth(),ret.getHeight());
		ml.setName("Layer "+i);
		ret.addLayer(ml);
	}
	readBKDTChunk(ret, c.getInputStream(), numBlocks);

	c = findChunk("BGFX");
	if(c != null) {
            readBGFXChunk(ret, c.getInputStream(), numBlocksGfx);
        }else{
            throw new IOException("No BGFX chunk found!");
        }

	System.out.println(ret.toString());
	return ret;
    }

    private void readATHRChunk(Map m, InputStream in) {
	
    }

    private void readBKDTChunk(Map m, InputStream in, int num) throws IOException {
	System.out.println("Reading "+num+" blocks...");
	for(int i = 0;i<num;i++) {
		blocks.add(readBLKSTR(in));
	}
    }

    private void readBODYChunk(Map m, InputStream in) throws IOException {
	TileSet set = (TileSet)m.getTilesets().get(0);
	MapLayer bg = m.getLayer(0),
		fg0 = m.getLayer(1),
		fg1 = m.getLayer(2),
		fg2 = m.getLayer(3);
	for(int i=0;i<m.getHeight();i++) {
		for(int j=0;j<m.getWidth();j++) {
			int block = (int)((readShort(in)&0x00FF)/BLKSTR_WIDTH);
			//System.out.print(""+block);
			BlkStr blk = (BlkStr)blocks.get(block);
			//System.out.println("bg: "+blk.bg);
			bg.setTileAt(j,i, set.getTile((int)blk.bg));	
			fg0.setTileAt(j,i, set.getTile((int)blk.fg0));	
			fg1.setTileAt(j,i, set.getTile((int)blk.fg1));	
			fg2.setTileAt(j,i, set.getTile((int)blk.fg2));	
		}
		//System.out.println();
	}
    }

    /**
     * BGFX blocks are synonymous with Tiles
     */
    private void readBGFXChunk(Map m, InputStream in, int num) throws IOException {
	TileSet set = (TileSet)m.getTilesets().get(0);
	set.addTile(new Tile()); 
	readRawImage(in);   //skip the null-tile
	for(int i=1;i<num;i++) {
		Tile t = new Tile();
		t.setImage(readRawImage(in));
		set.addTile(t);
	}
    }

    private BlkStr readBLKSTR(InputStream in) throws IOException {
	BlkStr ret = new BlkStr();
	long widthMod = (twidth*theight*2*256);
	ret.bg = readLongReverse(in)/widthMod;
	ret.fg0 = readLongReverse(in)/widthMod;
	ret.fg1 = readLongReverse(in)/widthMod;
	ret.fg2 = readLongReverse(in)/widthMod;

	ret.user1 = readLongReverse(in);
	ret.user2 = readLongReverse(in);
	ret.user3 = readShort(in);
	ret.user4 = readShort(in);
	ret.user5 = in.read();
	ret.user6 = in.read();
	ret.user7 = in.read();

	ret.bits = in.read();
	
	return ret;	
    }

    private Image readRawImage(InputStream in) throws IOException {
 	DirectColorModel cm = new DirectColorModel(16,0xF800,0x07E0,0x001F);

	int [] pixels = new int[twidth*theight+1];
	int i,j;

	for(i=0;i<theight;i++)
		for(j=0;j<twidth;j++) {
			pixels[i*twidth+j]=readShortReverse(in);
			//System.out.println(pixels[i*width+j]);
		}

	MemoryImageSource s = new MemoryImageSource(twidth,theight,cm,pixels,0,twidth);		
		
	return Toolkit.getDefaultToolkit().createImage(s);
	//return(Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(orig.getSource(),new TransImageFilter(cm.getRGB(64305)))));
    }
}
