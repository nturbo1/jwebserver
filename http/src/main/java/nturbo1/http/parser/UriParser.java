package nturbo1.http.parser;

import nturbo1.http.exceptions.HttpMessageParseException;
import nturbo1.log.CustomLogger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Parser for the Request Target of the start line in an HTTP message.
 */
class UriParser {
    private static final int MAX_TARGET_URI_SIZE = 8192; // The actual size of a whole URI is not quite checked,
                                                         // it's more for the parsing convenience.
    private static final int MAX_QUERY_KEY_OR_VALUE_SIZE = 8192;

    private static final CustomLogger log = CustomLogger.getLogger(UriParser.class.getName());

    /**
     * <p>
     * HTTP Request Target Grammar:
     * <p>
     *     request-target = origin-form
     *                      / absolute-form
     *                      / authority-form
     *                      / asterisk-form
     *         origin-form    = absolute-path [ "?" query ]
     *         absolute-form  = absolute-URI
     *         authority-form = uri-host ":" port
     *         asterisk-form  = "*"
     * </p>
     * </p>
     * @param iStream the byte source
     * @return a parsed UriInfo object
     * @throws IOException in case of an IO error
     * @throws HttpMessageParseException in case of a parsing error
     */
    static UriInfo parseReqTarget(InputStream iStream) throws IOException, HttpMessageParseException {
        log.warn("The HTTP Request Target is parsed only in the 'origin-form'! SHOULD BE FIXED LATER!!!");
        return parseOriginForm(iStream);
    }

    /**
     * A target URI origin-form grammar:
     * <p>
     * origin-form    = absolute-path [ "?" query ]
     * </p>
     *
     * <p>
     * For example, a client wishing to retrieve a representation of the resource identified as
     *
     * <p>
     *     http://www.example.org/where?q=now
     * </p>
     *
     * directly from the origin server would open (or reuse) a TCP connection to port 80 of the host "www.example.org"
     * and send the lines:
     *
     * <p>
     *     GET /where?q=now HTTP/1.1
     *     Host: www.example.org
     * </p>
     *
     * followed by the remainder of the request message.
     * </p>
     *
     * @param iStream the byte source
     * @return parsed UriInfo object
     * @throws IOException in case of an IO error
     * @throws HttpMessageParseException in case of a parsing error
     */
    static UriInfo parseOriginForm(InputStream iStream) throws IOException, HttpMessageParseException {
        Map<String, String> queryParams = null;
        ByteBuffer targetURIBytes = ByteBuffer.allocate(MAX_TARGET_URI_SIZE);

        int ch = iStream.read();
        log.warn("HTTP Request Target URI bytes are not decoded when parsed. SHOULD BE FIXED LATER!!!");
        while(!HttpMsgParser.isWhitespace(ch) && ch != -1) {
            if (!HttpMsgParser.isVAscii(ch))
                throw new HttpMessageParseException("Non visible ASCII characters are not allowed in an HTTP Request Target URI!");

            if (ch == '?') {
                queryParams = parseQueryParams(iStream);
            }

            try {
                targetURIBytes.put((byte) ch);
            } catch (BufferOverflowException bofe) {
                throw new HttpMessageParseException("HTTP Request Target URI bytes size exceeded the limit.");
            }

            ch = iStream.read();
        }

        if (ch == -1)
            throw new HttpMessageParseException("The HTTP message input stream was terminated.");

        if (!targetURIBytes.hasArray() || targetURIBytes.position() == 0)
            throw new HttpMessageParseException("An HTTP Request Target URI cannot be empty!");

        String targetUri = new String(targetURIBytes.array(), 0, targetURIBytes.position(), StandardCharsets.ISO_8859_1);

        return new UriInfo(targetUri, queryParams);
    }

    /**
     * Character bytes allowed in the query part of an HTTP Request Target URI:
     * <p>
     *     Unreserved       A-Z, a-z, 0-9, -, ., _, ~           Leave as is
     *     Sub-delims       !, $, &, ', (, ), *, +, ,, ;, =     Encode if used as data
     *     Others           :, @, /, ?                          Usually safe as data
     *     Prohibited       Spaces,                             MUST percent-encode
     *                      Control characters
     *                      (non-visible ASCII characters),
     *                      Non-ASCII
     * </p>
     * @param iStream the byte source
     * @return a Map object containing parsed query key-value pairs
     * @throws IOException in case of an IO error
     * @throws HttpMessageParseException in case of a parsing error
     */
    private static Map<String, String> parseQueryParams(InputStream iStream) throws IOException, HttpMessageParseException {
        log.warn("HTTP Request Target URI Query part bytes are not decoded when parsed. SHOULD BE FIXED LATER!!!");
        ByteBuffer queryBytes = ByteBuffer.allocate(MAX_QUERY_KEY_OR_VALUE_SIZE);
        boolean readingQueryKey = true;
        String currQueryKey = null;
        Map<String, String> queryParams = new HashMap<>();

        while (true) {
            int ch = iStream.read();
            if (HttpMsgParser.isWhitespace(ch))
                break;

            if (!HttpMsgParser.isVAscii(ch))
                throw new HttpMessageParseException(
                        String.format("Invalid character in the HTTP Request Target URI query section: %d", ch));
            if (ch == -1)
                throw new HttpMessageParseException(
                        "The HTTP message input stream was terminated during HTTP Request Target URI query section parsing.");
            if (ch == '%')
                throw new HttpMessageParseException("HTTP Request Target URI Query section IS NOT PERCENT-DECODED YET!!!");
            if (ch == '=')
            {
                if (!readingQueryKey) // == reading query value bytes == '=' encountered while reading query value bytes
                    throw new HttpMessageParseException("HTTP Request Target URI Query param (non-key) value bytes contain a delimiter: =");
                if (queryBytes.hasArray() && queryBytes.position() > 0) {
                    currQueryKey = new String(queryBytes.array(), 0, queryBytes.position(), StandardCharsets.ISO_8859_1);
                    queryBytes.position(0); // Empty the buffer to be used to store the query param value bytes now
                    readingQueryKey = false; // Reading a query value, not a key, from now on
                    continue;
                } else {
                    throw new HttpMessageParseException("HTTP Request Target URI Query key cannot be empty!");
                }
            }
            else if (ch == '&')
            {
                if (readingQueryKey)
                    throw new HttpMessageParseException(
                            "HTTP Request Target URI delimiter character '&' was encountered" +
                            "before finishing reading the query key bytes.");

                String queryValue = queryBytes.hasArray() && queryBytes.position() > 0 ?
                        new String(queryBytes.array(),0, queryBytes.position(), StandardCharsets.ISO_8859_1) :
                        "";
                if (currQueryKey == null) throw new RuntimeException("currQueryKey SHOT NOT be null here!");
                queryParams.put(currQueryKey, queryValue);
                currQueryKey = null;
                readingQueryKey = true;
                queryBytes.position(0);  // Empty the buffer to be used to store the query param key bytes now
                continue;
            }

            try {
                queryBytes.put((byte) ch);
            } catch (BufferOverflowException bofe) {
                throw new HttpMessageParseException("An HTTP Target URI query component size exceeded the limit.");
            }
        }

        if (queryBytes.hasArray() && queryBytes.position() > 0) // The query section finished while parsing a query param
            throw new HttpMessageParseException(
                    "Encountered a whitespace character before the end of the HTTP Request Target Query section");

        return queryParams;
    }
}
