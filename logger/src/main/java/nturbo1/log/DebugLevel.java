package nturbo1.log;

import java.util.logging.Level;

public class DebugLevel extends Level
{
    public static final int DEBUG_LEVEL_VALUE = Level.INFO.intValue() - 100;
    public static final Level DEBUG = new DebugLevel("DEBUG", DEBUG_LEVEL_VALUE);

    protected DebugLevel(String name, int value) { super(name, value); }
}
