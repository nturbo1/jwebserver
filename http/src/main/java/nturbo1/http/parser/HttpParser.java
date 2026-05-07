package nturbo1.http.parser;

import nturbo1.http.GeneralHeader;
import nturbo1.http.HttpEntityHeader;
import nturbo1.http.HttpRequestHeader;

import java.util.List;

public class HttpParser {
    public static final char CARRIAGE_RETURN_CHAR = 13;
    public static final char LINE_FEED_CHAR = 10;
    public static final float HTTP_VERSION_1_1 = 1.1f;

    public static final String HTTP_MESSAGE_HEADER_NAME_REGEX = "[0-9a-zA-Z!#$%&'*+.^_`|~-]+";
    public static final List<String> NON_REPEATABLE_HEADERS = List.of( // Non comma separated headers as well
            GeneralHeader.DATE.getName().toLowerCase(),
            GeneralHeader.TRANSFER_ENCODING.getName().toLowerCase(),
            HttpEntityHeader.CONTENT_LENGTH.getName().toLowerCase(),
            HttpEntityHeader.CONTENT_TYPE.getName().toLowerCase(),
            HttpEntityHeader.EXPIRES.getName().toLowerCase(),
            HttpRequestHeader.HOST.getName().toLowerCase(),
            HttpRequestHeader.USER_AGENT.getName().toLowerCase(),
            HttpRequestHeader.FROM.getName().toLowerCase(),
            HttpRequestHeader.AUTHORIZATION.getName().toLowerCase(),
            HttpRequestHeader.REFERER.getName().toLowerCase(),
            HttpRequestHeader.IF_MATCH.getName().toLowerCase(),
            HttpRequestHeader.IF_NONE_MATCH.getName().toLowerCase(),
            HttpRequestHeader.IF_MODIFIED_SINCE.getName().toLowerCase(),
            HttpRequestHeader.IF_UNMODIFIED_SINCE.getName().toLowerCase(),
            HttpRequestHeader.IF_RANGE.getName().toLowerCase()
    );
}
