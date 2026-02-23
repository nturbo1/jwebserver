package nturbo1.cmd;

import nturbo1.http.exceptions.cmd.UnknownArgException;
import nturbo1.http.exceptions.cmd.WrongArgFormatException;

import java.util.HashMap;
import java.util.Map;

public class CommandLineParser {
    private CommandLineParser() {}

    public static Map<String, String> parseArgs(String[] args)throws WrongArgFormatException, UnknownArgException 
    {
        Map<String, String> argsMap = new HashMap<>();
        for (String arg : args) {
            String[] argKV = verifyArg(arg);
            argsMap.put(argKV[0], argKV.length > 1 ? argKV[1] : "");
        }

        return argsMap;
    }

    /**
     * Verifies a raw argument string taken from the command line and returns a valid array of length 2 that contains
     * the argument name or key and its valid value.
     *
     * @param arg raw argument string from the command line
     * @return valid argument key value like ["--port", "8080"]
     */
    private static String[] verifyArg(String arg) throws WrongArgFormatException, UnknownArgException
    {
        String[] argKV = arg.split("=");
        if (argKV.length > 2) {
            throw new WrongArgFormatException("Multiple '=' symbols detected in the command line arguments: " + arg);
        }

        switch (argKV[0]) {
            case Argument.PORT:
                verifyArgValuePassed(argKV);
                verifyIntegerFormat(argKV[1]);
                break;
            case Argument.DEBUG:
                break;
            default:
                throw new UnknownArgException(argKV[0]);
        }

        return argKV;
    }

    private static void verifyArgValuePassed(String[] argKV) throws WrongArgFormatException
    {
        if (argKV.length < 2) {
            throw new WrongArgFormatException("No value passed for the command line argument: " + argKV[0]);
        }
    }

    private static void verifyIntegerFormat(String argValue) throws WrongArgFormatException
    {
        try {
            Integer.parseInt(argValue);
        } catch (NumberFormatException e) {
            throw new WrongArgFormatException("Invalid integer format: " + argValue);
        }
    }
}
