package tiled.util;

public class TiledProperty {

	private String value,
					min,
					max,
					type;
	
	public TiledProperty() {
	}
	
	public TiledProperty(String value, String min, String max, String type) {
		set(value, min, max, type);
	}
	
	public void set(String value, String min, String max, String type) {
		this.value = value;
		this.min   = min;
		this.max   = max;
		this.type  = type;
	}
	
}
