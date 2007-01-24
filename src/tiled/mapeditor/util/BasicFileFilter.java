package tiled.mapeditor.util;

import java.io.File;

public class BasicFileFilter extends ConfirmableFileFilter {

    private String ext, desc;
    
    public BasicFileFilter(String desc, String ext) {
        this.desc = desc;
        this.ext = ext;
    }
    
    public String getDefaultExtension() {
        return ext;
    }

    public boolean accept(File f) {
        String fileName = f.getPath().toLowerCase();
        return fileName.endsWith("."+ext);
    }

    public String getDescription() {
        return desc+" (*."+ext+")";
    }

}
