package nturbo1.http.parser;

import nturbo1.http.HttpMethod;

public record HttpReqLine(
        HttpMethod method,
        UriInfo targetUri,
        double version
) {
}
