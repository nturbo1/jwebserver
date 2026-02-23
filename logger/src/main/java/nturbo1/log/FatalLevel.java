package nturbo1.log;

import java.util.logging.Level;

public class FatalLevel extends Level
{
    public static final Level FATAL = new FatalLevel("FATAL", Level.SEVERE.intValue());
    protected FatalLevel(String name, int value) { super(name, value); }
}