package nturbo1.http.parser;

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
        }

        throw new HttpMessageParseException("An HTTP message header section MUST end with an empty new line!");
    }

    private static boolean isWhitespace(int ch) {
        return ch == ' ' || ch == '\t';
    }

    private static boolean isAlphanumeric(int ch) {
        return (48 <= ch && ch <= 57) || (65 <= ch && ch <= 90) || (97 <= ch && ch <= 122);
    }

    private static boolean isVAscii(int ch) {
        return 33 <= ch && ch <= 126;
    }

    private static boolean isExtendedAsciiBytes(int ch) {
        return 128 <= ch && ch <= 255;
    }

    private static int skipUntilNonWhitespace(InputStream iStream) throws IOException {
        int ch = iStream.read();
        while (isWhitespace(ch)) {
            ch = iStream.read();
        }

        return ch;
    }
}
