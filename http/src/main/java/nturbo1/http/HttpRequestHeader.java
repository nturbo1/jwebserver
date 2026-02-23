package nturbo1.http;

/**
 * request-header = Accept
 *                | Accept-Charset
 *                | Accept-Encoding
 *                | Accept-Language
 *                | Authorization
 *                | Expect
 *                | From
 *                | Host
 *                | If-Match
 *                | If-Modified-Since
 *                | If-None-Match
 *                | If-Range
 *                | If-Unmodified-Since
 *                | Max-Forwards
 *                | Proxy-Authorization
 *                | Range
 *                | Referer
 *                | TE
 *                | User-Agent
 */
public enum HttpRequestHeader
{
    ACCEPT("ACCEPT"),
    ACCEPT_CHARSET("Accept-Charset"),
    ACCEPT_ENCODING("Accept-Encoding"),
    ACCEPT_LANGUAGE("Accept-Language"),
    AUTHORIZATION("Authorization"),
    EXPECT("Expect"),
    FROM("From"),
    HOST("Host"),
    IF_MATCH("If-Match"),
    IF_MODIFIED_SINCE("If-Modified-Since"),
    IF_NONE_MATCH("If-None-Match"),
    IF_RANGE("If-Range"),
    IF_UNMODIFIED_SINCE("If-Unmodified-Since"),
    MAX_FORWARDS("Max-Forwards"),
    PROXY_AUTHORIZATION("Proxy-Authorization"),
    RANGE("Range"),
    REFERER("Referer"),
    TE("TE"),
    USER_AGENT("User-Agent");

    private final String name;

    HttpRequestHeader(String name) { this.name = name; }

    public final String getName() { return this.name; }
}