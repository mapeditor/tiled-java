package tiled.command;

import tiled.io.xml.XMLMapWriter;
import tiled.util.OverriddenPreferences;

class SaveCommand extends Command {

    private XMLMapWriter mapWriter;
    private OverriddenPreferences oprefs;
    CommandInterpreter outer;

    SaveCommand(CommandInterpreter outer) {
        super("save", ArgumentRequirement.REQUIRES_ZERO_OR_ONE, outer);
        this.outer = outer;
        init();
    }

    private void init() {
        mapWriter = new XMLMapWriter();
        oprefs = new OverriddenPreferences(mapWriter.getPreferences());
        mapWriter.setPreferences(oprefs);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        SaveCommand c = (SaveCommand) super.clone();
        c.init();
        return c;
    }
    
    public void setEmbedImageDataEnabled(boolean enabled){
        oprefs.putBoolean("embedImages", enabled);
    }
    
    @Override
    int execute() {
        String filename = outer.getMapFileName();
        if (getArguments().length > 0) {
            filename = getArguments()[0];
        }
        try {
            mapWriter.writeMap(outer.getMap(), filename);
        } catch (Exception ex) {
            outer.raiseError("could not write map to " + filename + " - " + ex.getMessage());
            return 1;
        }
        return 0;
    }
}
