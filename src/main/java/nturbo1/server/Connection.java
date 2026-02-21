package nturbo1.server;

import nturbo1.exceptions.http.BadHttpRequestHeaderException;
import nturbo1.exceptions.http.HttpMessageParseException;
import nturbo1.exceptions.http.InvalidHttpMessageHeaderException;
import nturbo1.exceptions.http.UnsupportedHttpVersionException;
import nturbo1.http.HttpRequest;
import nturbo1.http.parser.v1_1.HttpRequestParser;
import nturbo1.log.CustomLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class Connection
{
    private final Socket socket;

    private static final CustomLogger log = CustomLogger.getLogger(Connection.class.getName());

    public Connection(Socket socket) { this.socket = socket; }

    public void handle()
    {
        InputStream iStream = null;
        try {
            iStream = socket.getInputStream();
        } catch (IOException e) {
            log.error("Couldn't get the socket input stream due to: " + e.getMessage());
            close();

            return;
        }

        HttpRequest req = null;
        try {
             req = HttpRequestParser.parseHttpRequest(iStream);
        } catch (HttpMessageParseException e) {
            log.error("Failed to parse the HTTP request because: " + e.getMessage());
            // TODO: SEND A PROPER HTTP RESPONSE!!!
        } catch (InvalidHttpMessageHeaderException e) {
            log.warn("Invalid http message header detected!");
            // TODO: YOU SHOULD PROBABLY SEND 400 BAD REQUEST RESPONSE OR STH
        } catch (UnsupportedHttpVersionException e) {
            log.warn("HANDLE UNSUPPORTED HTTP VERSION IN THE REQUEST CASE!!!!!!");
            // TODO: SEND A PROPER HTTP RESPONSE!!!
        } catch (BadHttpRequestHeaderException e) {
            log.warn("SEND 400 BAD REQUEST RESPONSE!!!");
            // TODO: SEND A PROPER HTTP RESPONSE!!!
        } catch (IOException e) {
            log.error("Failed to parse an HTTP Request due to: " + e.getMessage());
            // TODO: SHOULD YOU SEND A RESPONSE HERE?
            close();

            return;
        }
        log.info("Request: " + req);

        log.warn("HANDLE THE PARSED HTTP REQUEST AND SEND AN APPROPRIATE HTTP RESPONSE!!!");

        close();
    }

    private void close()
    {
        try {
            socket.close();
        } catch (IOException e) {
            log.error("Failed to close the socket connection due to: " + e.getMessage());
        }
    }
}
