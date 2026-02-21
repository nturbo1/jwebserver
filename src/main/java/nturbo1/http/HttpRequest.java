package nturbo1.http;

import java.util.List;
import java.util.Map;

public class HttpRequest extends HttpMessage
{
    private String URI; // TODO: Create a URI class and implement a parser for that!!!

    public HttpRequest() {}

    public HttpRequest(HttpMethod method, Map<String, List<String>> headers, Object body, String URI)
    {
        super(method, headers, body);
        this.URI = URI;
    }

    public void setURI(String URI) { this.URI = URI; }

    @Override
    public String toString()
    {
        return String.format("{Method: %s, Headers: %s, Body: %s}", this.getMethod(), this.getHeaders(), this.getBody());
    }
}