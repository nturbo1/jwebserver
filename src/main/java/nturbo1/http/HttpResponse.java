package nturbo1.http;

import java.util.List;
import java.util.Map;

public class HttpResponse extends HttpMessage
{
    private final HttpStatus status;

    public HttpResponse(HttpStatus status, HttpMethod method, Map<String, List<String>> headers, Object body)
    {
        super(method, headers, body);
        this.status = status;
    }
}