package nturbo1.http;

/**
 * entity-header  = Allow
 *                | Content-Encoding
 *                | Content-Language
 *                | Content-Length
 *                | Content-Location
 *                | Content-MD5
 *                | Content-Range
 *                | Content-Type
 *                | Expires
 *                | Last-Modified
 *                | extension-header
 * <p>
 * extension-header = message-header
 */
public enum HttpEntityHeader
{
    ALLOW("Allow"),
    CONTENT_ENCODING("Content-Encoding"),
    CONTENT_LANGUAGE("Content-Language"),
    CONTENT_LENGTH("Content-Length"),
    CONTENT_LOCATION("Content-Location"),
    CONTENT_MD5("Content-MD5"),
    CONTENT_RANGE("Content-Range"),
    CONTENT_TYPE("Content-Type"),
    EXPIRES("Expires"),
    LAST_MODIFIED("Last-Modified");

    private final String name;

    HttpEntityHeader(String name) { this.name = name; }

    public String getName() { return this.name; }
}