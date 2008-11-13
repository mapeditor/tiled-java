/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiled.command;

/**
 *
 * @author upachler
 */
class HelpCommand extends Command {
    
    HelpCommand(CommandInterpreter interp){
        super("help", ArgumentRequirement.REQUIRES_NONE, interp);
    }
    
    @Override
    int execute() {
        // FIXME: this should be more detailed
        System.out.println("Supported commands are: ");
        for(Command c : interpreter.getCommandPrototypes()){
            System.out.println("\t'"+c.getName()+'\'');
        }
        System.out.println();
        return 0;
    }
    
}
