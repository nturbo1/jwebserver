package nturbo1.http.parser;

import java.util.Map;

public record UriInfo(
        String path,
        Map<String, String> queryParams
) {
}
