package nturbo1.http.parser;

import nturbo1.http.HttpMethod;
import nturbo1.http.exceptions.HttpMessageParseException;
import nturbo1.http.exceptions.InvalidHttpHeaderException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

class HttpMsgParser {

    private static final int MAX_HEADER_NAME_SIZE = 8192;
    private static final int MAX_HEADER_VALUE_SIZE = 8192;
    private static final int MAX_HTTP_METHOD_NAME_SIZE = 32;
    private static final int MAX_HTTP_VERSION_BYTES_SIZE = 32; // Doesn't include the prefix, which is 'HTTP/'

    /**
     * Reads byte by byte an HTTP Request Line and parses it.
     * <p>
     * Request Line grammar:
     * <p>
     *     request-line   = method SP request-target SP HTTP-version
     *     method         = token
     *     request-target = origin-form
     *                  / absolute-form
     *                  / authority-form
     *                  / asterisk-form
     *     origin-form    = absolute-path [ "?" query ]
     *     absolute-form  = absolute-URI
     *     authority-form = uri-host ":" port
     *     asterisk-form  = "*"
     * </p>
     * </p>
     *
     * @param iStream the byte source
     * @return a parsed HttpReqLine object
     * @throws IOException in case of an IO error
     */
    static HttpReqLine parseReqLine(InputStream iStream) throws IOException, HttpMessageParseException {
        HttpMethod method = parseHttpMethod(iStream);
        double version = parseHttpVersion(iStream);
        UriInfo targetUri = UriParser.parseOriginForm(iStream);

        return new HttpReqLine(method, targetUri, version);
    }

    static HttpMethod parseHttpMethod(InputStream iStream) throws IOException, HttpMessageParseException {
        int ch = iStream.read();
        ByteBuffer buf = ByteBuffer.allocate(MAX_HTTP_METHOD_NAME_SIZE);
        while (!isWhitespace(ch) && ch != -1) {
            try {
                buf.put((byte) ch);
            } catch (BufferOverflowException bofe) {
                throw new HttpMessageParseException("The HTTP method name in the request line exceeded the size limit.");
            }

            ch = iStream.read();
        }

        if (ch == -1)
            throw new HttpMessageParseException(
                    "Failed to parse the HTTP method in the request line due to the input stream being closed.");
        assert buf.hasArray() && buf.position() > 0 : "HTTP method byte buffer should not be empty!";

        String methodName = new String(buf.array(), 0, buf.position() + 1, StandardCharsets.ISO_8859_1);
        try {
            return HttpMethod.valueOf(methodName);
        } catch (IllegalArgumentException iae) {
            throw new HttpMessageParseException("Invalid/Unsupported HTTP method: " + methodName);
        }
    }

    /**
     * HTTP Version Grammar:
     * <p>
     *     HTTP-version  = HTTP-name "/" DIGIT "." DIGIT
     *     HTTP-name     = %s"HTTP"
     * </p>
     *
     * @param iStream the byte source
     * @return parsed valid HTTP version number
     * @throws IOException in case of an IO error
     * @throws HttpMessageParseException in case of a parsing error
     */
    static double parseHttpVersion(InputStream iStream) throws IOException, HttpMessageParseException {
        byte[] httpPrefix = { 'H', 'T', 'T', 'P' };
        for (int i = 0; i < 4; i++) {
            if (iStream.read() != httpPrefix[i])
                throw new HttpMessageParseException("Invalid HTTP version in the start line.");
        }

        if (iStream.read() != '/')
            throw new HttpMessageParseException("Invalid HTTP version in the start line.");

        int ch = iStream.read();
        ByteBuffer versionBytes = ByteBuffer.allocate(MAX_HTTP_VERSION_BYTES_SIZE);
        while(!isWhitespace(ch)) {
            try {
                versionBytes.put((byte) ch);
            } catch (BufferOverflowException bofe) {
                throw new HttpMessageParseException("HTTP version number bytes exceeds the limit.");
            }

            ch = iStream.read();
        }
        assert versionBytes.hasArray() && versionBytes.position() > 0 : "HTTP Version number bytes buffer shouldn't be empty!";

        String versionStr = new String(versionBytes.array(), 0, versionBytes.position() + 1, StandardCharsets.ISO_8859_1);
        try {
            return Double.parseDouble(versionStr);
        } catch (NumberFormatException nfe) {
            throw new HttpMessageParseException("Invalid HTTP version number: " + versionStr);
        }
    }

    /**
     * Reads byte by byte a header line and parses it.
     * Returns `null` if a new line is read, `\r\n` or `\n`, indicating the end of the header section.
     * <p>
     * Header field line syntax:
     * <p>
     *     field-line       = field-name ":" OWS field-value OWS
     *     field-value      = Visible-ASCII | Space | Horizontal-tab | Extended-bytes
     *     Visible-ASCII    = 0x21 – 0x7E
     *     Space            = 0x20
     *     Horizontal-tab   = 0x09
     *     Extended-bytes   = 0x80 – 0xFF
     * </p>
     * </p>
     *
     * @param iStream the byte source
     * @return parsed HttpHeaderInfo object
     * @throws IOException in case of an IO error
     * @throws HttpMessageParseException in case of a parse error
     */
    static HttpHeaderInfo parseHttpHeaderLine(InputStream iStream)
            throws IOException, HttpMessageParseException, InvalidHttpHeaderException {
        int ch = iStream.read();
        if (ch == -1) {
            throw new HttpMessageParseException("Expected a header line or an empty new line.");
        }

        if (ch == '\r') {
            ch = iStream.read();
            if (ch == '\n') { return null; }
            throw new HttpMessageParseException(String.format("Character %c must be followed by character %c", '\r', '\n'));
        }

        ch = skipUntilNonWhitespace(iStream);

        ByteBuffer headerNameBuf = ByteBuffer.allocate(MAX_HEADER_NAME_SIZE);
        // Parse a header name
        while (ch != ':') {
            if (isWhitespace(ch)) {
                throw new InvalidHttpHeaderException("Header name MUST NOT contain whitespace!");
            } else if (
                    isAlphanumeric(ch) ||
                    ch == '!' || ch == '#' || ch == '$' || ch == '%' || ch == '&' || ch == '\'' || ch == '*' ||
                    ch == '+' || ch == '-' || ch == '.' || ch == '^' || ch == '_' || ch == '`' || ch == '|' || ch == '~')
            {
                try {
                    headerNameBuf.put((byte) ch);
                } catch (BufferOverflowException bofe) {
                    throw new InvalidHttpHeaderException("Header name was too long.");
                }
            } else {
                throw new InvalidHttpHeaderException("Invalid character encountered in the header name: " + ch);
            }

            ch = iStream.read();
        }

        if (!headerNameBuf.hasArray() || headerNameBuf.position() == 0) {
            throw new InvalidHttpHeaderException("Header name can't be empty!");
        }

        String headerName = new String(headerNameBuf.array(), 0, headerNameBuf.position() + 1, StandardCharsets.ISO_8859_1);

        ByteBuffer headerValueBuf = ByteBuffer.allocate(MAX_HEADER_VALUE_SIZE);
        // Parse a header value
        ch = iStream.read();
        while (ch != -1) {
            if (ch == '\r')
            {
                ch = iStream.read();
                if (ch == '\n') {
                    String headerValue;
                    if (headerValueBuf.hasArray() && headerValueBuf.position() > 0) {
                        headerValue = new String(headerValueBuf.array(), 0, headerValueBuf.position() + 1, StandardCharsets.ISO_8859_1);
                    } else {
                        headerValue = "";
                    }

                    return new HttpHeaderInfo(headerName, headerValue);
                }

                throw new HttpMessageParseException(String.format("Character %c must be followed by character %c", '\r', '\n'));
            }
            else if (isVAscii(ch) || isWhitespace(ch) || isExtendedAsciiBytes(ch))
            {
                headerValueBuf.put((byte) ch);
            }
            else
            {
                throw new InvalidHttpHeaderException(String.format("Invalid byte detected in the header field value: %d", ch));
            }

            ch = iStream.read();
        }

        throw new HttpMessageParseException("An HTTP message header section MUST end with an empty new line!");
    }

    static boolean isWhitespace(int ch) {
        return ch == ' ' || ch == '\t';
    }

    static boolean isAlphanumeric(int ch) {
        return (48 <= ch && ch <= 57) || (65 <= ch && ch <= 90) || (97 <= ch && ch <= 122);
    }

    static boolean isVAscii(int ch) {
        return 33 <= ch && ch <= 126;
    }

    static boolean isExtendedAsciiBytes(int ch) {
        return 128 <= ch && ch <= 255;
    }

    static int skipUntilNonWhitespace(InputStream iStream) throws IOException {
        int ch = iStream.read();
        while (isWhitespace(ch)) {
            ch = iStream.read();
        }

        return ch;
    }
}
