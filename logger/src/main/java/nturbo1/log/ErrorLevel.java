package nturbo1.log;

import java.util.logging.Level;

public class ErrorLevel extends Level
{
    public static final Level ERROR = new ErrorLevel("ERROR", WarnLevel.WARN.intValue() + 50 /* 950 */);

    protected ErrorLevel(String name, int value) { super(name, value); }
}
