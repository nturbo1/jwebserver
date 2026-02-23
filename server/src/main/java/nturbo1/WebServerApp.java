package nturbo1;

import nturbo1.cmd.Argument;
import nturbo1.cmd.CommandLineParser;
import nturbo1.http.exceptions.cmd.UnknownArgException;
import nturbo1.http.exceptions.cmd.WrongArgFormatException;
import nturbo1.log.LogConfig;
import nturbo1.server.HttpServer;

import java.util.Map;

/**
 * The HTTP Server application.
 *
 */
public class WebServerApp
{
    public static void main( String[] args )
    {
        Map<String, String> argsMap = parseArgs(args);
        LogConfig.setup(isDebugMode(argsMap));
        HttpServer httpServer = HttpServer.init(argsMap);

        if (httpServer == null)
        {
            System.out.println("Failed to initialize an http server! Exiting...");
            System.exit(1);
        }

        httpServer.start();
    }

    private static Map<String, String> parseArgs(String[] args)
    {
        try {
            return CommandLineParser.parseArgs(args);
        } catch (WrongArgFormatException | UnknownArgException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        return null;
    }

    private static boolean isDebugMode(Map<String, String> args) { return args.containsKey(Argument.DEBUG); }
}
