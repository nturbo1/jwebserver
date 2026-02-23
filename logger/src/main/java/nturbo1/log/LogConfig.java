package nturbo1.log;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LogConfig
{
    public static void setup(boolean isDebug)
    {

        LogManager.getLogManager().reset(); // wipe existing config
        Logger root = Logger.getLogger("");

        Level logLevel = isDebug ? DebugLevel.DEBUG : Level.INFO;

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(logLevel);
        consoleHandler.setFormatter(new LogMessageFormatter());

        // TODO: ADD A FILE HANDLER !!!

        root.addHandler(consoleHandler);
        root.setLevel(logLevel);
    }
}
