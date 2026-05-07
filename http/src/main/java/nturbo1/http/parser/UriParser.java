package nturbo1.http.parser;

import nturbo1.http.exceptions.HttpMessageParseException;

import java.io.InputStream;

/**
 * Parser for the Request Target of the start line in an HTTP message.
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
 */
class UriParser {
    static UriInfo parseOriginForm(InputStream iStream) throws HttpMessageParseException {
        throw new HttpMessageParseException("HTTP Request Target origin-form parser is not IMPLEMENTED!!!");
    }
}
