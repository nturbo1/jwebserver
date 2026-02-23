package nturbo1.http;

import java.util.List;
import java.util.Map;

/**
 * Represents a generic HTTP message.
 *
 * generic-message = start-line
 *                   *(message-header CRLF)
 *                   CRLF
 *                   [ message-body ]
 * start-line      = Request-Line | Status-Line
 *
 */
public abstract class HttpMessage
{
    private HttpMethod method;
    private Map<String, List<String>> headers;
    private Object body;

    public HttpMessage() {}

    public HttpMessage(HttpMethod method, Map<String, List<String>> headers, Object body)
    {
        this.method = method;
        this.headers = headers;
        this.body = body;
    }

    // GETTERS
    public HttpMethod getMethod() { return this.method; }
    public Map<String, List<String>> getHeaders() { return this.headers; }
    public Object getBody() { return this.body; }

    // SETTERS
    public void setMethod(HttpMethod method) { this.method = method; }
    public void setHeaders(Map<String, List<String>> headers) { this.headers = headers; }
    public void setBody(Object body) { this.body = body; }
}