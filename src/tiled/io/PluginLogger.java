package tiled.io;

import java.util.LinkedList;



public class PluginLogger {

	private LinkedList messages;
	
	public void error(Object message) {
		
	}
	
	public void warn(Object message) {
		
	}
	
	public void info(Object message) {
		
	}
	
	public void debug(Object message) {
		
	}
	
	public boolean isEmpty() {
		return messages.isEmpty();
	}
	
	public class PluginMessage {
		private int type;
		private Object message;
	};
}
