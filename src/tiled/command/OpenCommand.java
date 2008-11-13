package tiled.command;

import tiled.io.xml.XMLMapTransformer;

class OpenCommand extends Command {

    @Override
    int execute() {
        XMLMapTransformer t = new XMLMapTransformer();
        String filename = getArguments()[0];
        try {
            interpreter.setMap(t.readMap(filename), filename);
        } catch (Exception ex) {
            interpreter.raiseError("could not load file " + filename + "");
            return 1;
        }
        return 0;
    }

    OpenCommand(CommandInterpreter outer) {
        super("open", ArgumentRequirement.REQUIRES_ONE, outer);
        this.interpreter = outer;
    }
}
