/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tiled.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author upachler
 */
public class CommandInterpreter {

    void raiseError(String string) {
        System.err.println(string);
    }

    private tiled.core.Map map = null;
    private String mapFileName = null;
    
    Map<String,Command> commandPrototypes;
    
    // initializer
    {
        commandPrototypes = new HashMap<String,Command>();
        Command[] commands = {
            new HelpCommand(this),
            new OpenCommand(this),
            new SaveCommand(this),
        };
        for(Command c : commands)
            commandPrototypes.put(c.getName(), c);
    }
    
    public int interpret(String[] commandLine, int off) {
        
        Vector<Command> commands = new Vector<Command>();
        for(int current=off; current<commandLine.length; ++current){
            String commandName = commandLine[current];
            Command command = commandPrototypes.get(commandName);
            
            if(command == null){
                raiseError("unknown command '" + commandName + '\'');
                return 1;
            }
            
            try {
                command = (Command) command.clone();
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(CommandInterpreter.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            Command.ArgumentRequirement areq = command.getArgumentRequirement();
            boolean requiresParameterList =
                areq == Command.ArgumentRequirement.REQUIRES_ONE
            ||  areq == Command.ArgumentRequirement.REQUIRES_ONE_OR_MORE
            ||  !command.hasDefaultsForAllAttributes();
            
            commands.add(command);
            
            // if an argument list is not required and the next parameterList
            // actually looks like another command, process next command
            if(!requiresParameterList && current+1 < commandLine.length && commandPrototypes.get(commandLine[current+1]) != null)
                continue;
            
            // check if parameters are specified for commands that need them
            if(requiresParameterList && current+1 >= commandLine.length){
                raiseError("command " + command.getName() + " requires parameters");
                return 1;
            }
            
            // move on the next commandLine item; that's where our parameters are
            ++current;
            if(current >= commandLine.length)
                break;
            
            // parse parameters
            String parameterListString = commandLine[current];
            String[] parameterList = parameterListString.split(",");
            for(String parameter : parameterList){
                if(parameter.indexOf('=')!=-1){
                    String[] keyValue = parameter.split("=", 2);
                    
                    if(!command.setAttribute(keyValue[0], keyValue[1])){
                        raiseError("command '"+command.getName()+"' does not support the attribute '"+keyValue[0]+"'");
                        return 1;
                    }
                }else{
                    switch(command.getArgumentRequirement()){
                        case REQUIRES_NONE:
                            raiseError("command "+command.getName()+" requires no arguments");
                            return 1;
                        case REQUIRES_ONE:
                        case REQUIRES_ZERO_OR_ONE:
                            if(command.getArguments().length==1){
                                raiseError("command "+command.getName()+" only takes one argument");
                                return 1;
                            }
                            command.addArgument(parameter);
                            break;
                        case REQUIRES_ZERO_OR_MORE:
                        case REQUIRES_ONE_OR_MORE:
                            command.addArgument(parameter);
                            break;
                    }
                }
            }
        }
        
        // now that we have parsed all commands, execute them one after the other
        for(Command c : commands){
            int returnValue = c.execute();
            if(returnValue != 0)
                return returnValue;
            
        }
        return 0;
    }
    
    public Iterable<Command> getCommandPrototypes(){
        return commandPrototypes.values();
    }
    
    public tiled.core.Map getMap() {
        return map;
    }

    public void setMap(tiled.core.Map map) {
        setMap(map, null);
    }

    public void setMap(tiled.core.Map map, String fileName) {
        this.map = map;
    }

    public String getMapFileName() {
        return mapFileName;
    }
}
