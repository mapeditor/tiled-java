package mappy;

import java.io.*;

public class Chunk {

    private String headerTag;
    private int chunkSize;
    private ByteArrayInputStream bais;

    public Chunk(InputStream in) throws IOException {
	byte[] header = new byte[4];
	byte[] data;
	int readSize;	

        in.read(header);
	headerTag = new String(header);
	chunkSize = (int)readLongReverse(in);
	if(chunkSize > 0) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		data = new byte[chunkSize];
		readSize = in.read(data, 0, chunkSize);
		if(readSize!=chunkSize)
			throw new IOException("Incomplete read!");
		baos.write(data);
		bais = new ByteArrayInputStream(baos.toByteArray());
	}
    }

    public Chunk(String header) {
	headerTag = header;
    }

    public boolean isGood() {
	return chunkSize > 0;
    }

    public boolean equals(Object o) {
	if(o instanceof String) {
		return o.equals(headerTag);
	}else if(o instanceof Chunk) {
		return ((Chunk)o).headerTag.equals(headerTag);
	}
	return false;
    }

    private long readLongReverse(InputStream in) throws IOException {
        int a = in.read();
        int b = in.read();
        int c = in.read();
        int d = in.read();
                                                                                
        return (long)((a<<24)|(b<<16)|(c<<8)|d);
    }

    private long readLong(InputStream in) throws IOException {
	int a = in.read();
        int b = in.read();
        int c = in.read();
        int d = in.read();
	
	return (long)(a|(b<<8)|(c<<16)|(d<<24));
    }

    public InputStream getInputStream() {
	return bais;
    }
}
