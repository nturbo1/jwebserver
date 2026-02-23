package nturbo1.http.parser.v1_1;

import nturbo1.http.HttpMethod;
import nturbo1.http.HttpRequest;
import nturbo1.http.exceptions.BadHttpRequestHeaderException;
import nturbo1.http.exceptions.HttpMessageParseException;
import nturbo1.http.exceptions.InvalidHttpMessageHeaderException;
import nturbo1.http.exceptions.UnsupportedHttpVersionException;
import nturbo1.http.util.Bytes;
import nturbo1.log.CustomLogger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Request = Request-Line
 *           *(( general-header
 *            | request-header
 *            | entity-header ) CRLF)
 *           CRLF
 *           [ message-body ]
 * <p>
 * Request-Line = Method SP Request-URI SP HTTP-Version CRLF
 */
public class HttpRequestParser
{
    private static final CustomLogger log = CustomLogger.getLogger(HttpRequestParser.class.getName());

    public static HttpRequest parseHttpRequest(InputStream iStream)
            throws
            BadHttpRequestHeaderException,
            HttpMessageParseException,
            UnsupportedHttpVersionException,
            IOException,
            InvalidHttpMessageHeaderException
    {
        String reqLine;
        try {
            reqLine = new String(Bytes.readLine(iStream));
        } catch (IOException e) {
            log.error("Failed to read a line from the socket input stream reader due to: " + e.getMessage());
            throw e;
        }

        HttpRequest req = parseHttpRequestLine(reqLine, null);
        Map<String, List<String>> headers = HttpMessageParser.parseHttpMessageHeaders(iStream);
        req.setHeaders(headers);
        req.setBody(HttpMessageParser.readMessageBodyBytes(iStream, headers));

        return req;
    }

    public static HttpRequest parseHttpRequestLine(String line, HttpRequest req)
            throws HttpMessageParseException, UnsupportedHttpVersionException
    {
        log.debug("Parsing the HTTP Request Line...");
        String[] words = line.split(" "); // [Method, Request-URI, HTTP-Version]
        if (words.length < 3)
        {
            throw new HttpMessageParseException("Not enough information in the HTTP Request Line: " + line);
        }

        HttpMethod method = HttpMessageParser.parseHttpMethod(words[0]);
        log.warn("IMPLEMENT REQUEST URI PARSER!!!"); // words[1]
        float version = HttpMessageParser.parseHttpVersion(words[2]);
        if (HttpMessageParser.HTTP_VERSION_1_1 != version)
        {
            throw new UnsupportedHttpVersionException(version);
        }

        if (req == null)
        {
            // TODO: Pass the URI here after it's parsed
            log.debug("Successfully parsed the HTTP Request Line!");
            return new HttpRequest(method, null, null, null);
        }

        req.setMethod(method);
        // TODO: Set the URI here  after it's parsed

        log.debug("Successfully parsed the HTTP Request Line!");
        return req;
    }
}
