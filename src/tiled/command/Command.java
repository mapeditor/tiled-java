package tiled.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

/**
 * A Command consists of a name that it can be invoked by,
 * an optional argument and an optional list of attributes.
 * A typical command has this syntax:
 * command-name ' ' argument-list
 *
 * where argument-list is
 * [argument{','argument}]
 *
 * and argument is
 * mainArgument | attributeName '=' value
 *
 * So an open command might look like this
 *
 * open myfile.xml
 *
 * or a save command might look like this:
 *
 * save myfile.xml,bakeTextures=true
 *
 */
abstract class Command implements Cloneable {

    private ArgumentRequirement argumentRequirement = null;
    private Vector<String> arguments = new Vector<String>();
    private final String name;
    CommandInterpreter interpreter;

    enum ArgumentRequirement{
        REQUIRES_NONE,
        REQUIRES_ZERO_OR_ONE,
        REQUIRES_ZERO_OR_MORE,
        REQUIRES_ONE,
        REQUIRES_ONE_OR_MORE
    }

    
    protected Command(String name, ArgumentRequirement argumentRequirement, CommandInterpreter interpreter) {
        super();
        this.interpreter = interpreter;
        this.name = name;
        this.argumentRequirement = argumentRequirement;
    }

    abstract int execute();

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    String[] getArguments() {
        return arguments.toArray(new String[arguments.size()]);
    }

    void addArgument(String argument) {
        arguments.add(argument);
    }

    public boolean setAttribute(String name, String value) {
        Method[] methods = getClass().getMethods();
        String setterName = "set" + name;
        Method setter = null;
        for (Method m : methods) {
            if (m.getName().equalsIgnoreCase(setterName) && m.getParameterTypes().length == 1) {
                setter = m;
                break;
            }
        }

        if (setter == null) {
            return false;
        }
        Class<?> ptype = setter.getParameterTypes()[0];
        Object p = null;
        if (ptype.equals(int.class)) {
            p = Integer.parseInt(value);
        } else if (ptype.equals(String.class)) {
            p = value;
        } else if (ptype.equals(float.class)) {
            p = Float.parseFloat(value);
        } else if (ptype.equals(boolean.class)){
            p = Boolean.parseBoolean(value);
        }
        try {
            setter.invoke(this, p);
        } catch (IllegalAccessException ex) {
            return false;
        } catch (IllegalArgumentException ex) {
            return false;
        } catch (InvocationTargetException ex) {
            return false;
        }

        return true;
    }

    public Object getDefaultValueOf(String attributeName) {
        Method[] methods = getClass().getMethods();
        Method defaultValueGetter = null;
        String defaultValueGetterName = "get" + attributeName + "Default";
        for (Method m : methods) {
            if (m.getName().equalsIgnoreCase(defaultValueGetterName) && m.getParameterTypes().length == 0) {
                defaultValueGetter = m;
                break;
            }
        }
        if (defaultValueGetter == null) {
            return null;
        }
        try {
            return defaultValueGetter.invoke(this, (Object)null);
        } catch (IllegalAccessException ex) {
            return null;
        } catch (IllegalArgumentException ex) {
            return null;
        } catch (InvocationTargetException ex) {
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public ArgumentRequirement getArgumentRequirement() {
        return argumentRequirement;
    }

    boolean hasDefaultsForAllAttributes() {
        Method[] methods = getClass().getMethods();
        for (Method m : methods) {
            if (m.getName().startsWith("set") && m.getParameterTypes().length == 1) {
                String attributeName = m.getName().substring("set".length());
                try {
                    Method defaultGetter = getClass().getMethod("get" + attributeName + "Default", (Class<?>)null);
                    try {
                        if (defaultGetter.invoke(this, (Object)null) == null) {
                            return false;
                        }
                    } catch (IllegalAccessException ex) {
                        return false;
                    } catch (IllegalArgumentException ex) {
                        return false;
                    } catch (InvocationTargetException ex) {
                        return false;
                    }
                } catch (NoSuchMethodException nsmx) {
                    return false;
                }
            }
        }
        return true;
    }
}
