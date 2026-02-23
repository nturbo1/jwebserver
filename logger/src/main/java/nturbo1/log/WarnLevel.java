package nturbo1.log;

import java.util.logging.Level;

public class WarnLevel extends Level
{
    public static final Level WARN = new WarnLevel("WARN", Level.WARNING.intValue());
    protected WarnLevel(String name, int value) { super(name, value); }
}