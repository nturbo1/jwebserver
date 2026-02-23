package nturbo1.http.exceptions.cmd;

public class UnknownArgException extends Exception
{
    public UnknownArgException(String argName)
    {
        super("Unknown argument name: " + argName);
    }
}