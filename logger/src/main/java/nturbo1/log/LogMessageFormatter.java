package nturbo1.log;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogMessageFormatter extends Formatter
{
    @Override
    public String format(LogRecord record) {
        return String.format(
                "%1$tF %1$tT.%1$tL [%2$s] %3$s - %4$s%n",
                record.getMillis(),              // timestamp
                record.getLevel().getName(),     // log level
                record.getLoggerName(),          // classpath
                record.getMessage()              // message
        );
    }
}
