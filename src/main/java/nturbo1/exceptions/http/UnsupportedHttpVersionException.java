package nturbo1.exceptions.http;

public class UnsupportedHttpVersionException extends Exception
{
    public UnsupportedHttpVersionException(float version) {
        super("HTTP Version " + version + " is not supported!");
    }
}