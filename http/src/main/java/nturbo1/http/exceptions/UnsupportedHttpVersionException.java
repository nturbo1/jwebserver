package nturbo1.http.exceptions;

public class UnsupportedHttpVersionException extends Exception
{
    public UnsupportedHttpVersionException(float version) {
        super("HTTP Version " + version + " is not supported!");
    }
}