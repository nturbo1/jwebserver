package nturbo1.log;

import java.util.logging.Logger;

/**
 * A custom logger class that wraps around the java.util.logging.Logger class and contains custom log methods for
 * custom log levels.
 *
 * LOG LEVELS:
 * - FATAL == SEVERE = 1000
 * - ERROR = 950
 * - WARN == WARNING = 900
 * - INFO == INFO = 800
 * - DEBUG = 700
 */
public class CustomLogger
{
    private final Logger logger;
    private CustomLogger(Logger logger) { this.logger = logger; }

    public static CustomLogger getLogger(String name) { return new CustomLogger(Logger.getLogger(name)); }

    public void debug(String msg) { logger.log(DebugLevel.DEBUG, msg); }
    public void info(String msg) { logger.info(msg); }
    public void warn(String msg) { logger.log(WarnLevel.WARN, msg); }
    public void error(String msg) { logger.log(ErrorLevel.ERROR, msg); }
    public void fatal(String msg) { logger.log(FatalLevel.FATAL, msg); }
}
