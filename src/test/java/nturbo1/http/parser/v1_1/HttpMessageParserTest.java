package nturbo1.http.parser.v1_1;

import nturbo1.exceptions.parser.HttpMessageParseException;
import nturbo1.http.HttpMethod;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class HttpMessageParserTest
{
    @ParameterizedTest
    @ValueSource(strings = { "OPTIONS", "GET", "HEAD", "POST", "PUT", "DELETE", "TRACE", "CONNECT"})
    void returnCorrectHttpMethod(String method) throws HttpMessageParseException
    {
        HttpMethod parsedMethod = HttpMessageParser.parseHttpMethod(method);
        Assertions.assertThat(parsedMethod.name()).isEqualTo(method);
    }

    @Test
    void throwHttpMessageParseExceptionForUnknownMethod()
    {
        String unknownHttpMethod = "asdfasdf";
        Assertions.assertThatThrownBy(() -> HttpMessageParser.parseHttpMethod(unknownHttpMethod));
    }

    @ParameterizedTest
    @CsvSource({
            "HTTP/0.9, 0.9",
            "HTTP/1.0, 1.0",
            "HTTP/1.1, 1.1",
            "HTTP/2.0, 2.0",
            "HTTP/3.0, 3.0"
    })
    void returnValidHttpVersion(String versionTxt, float expectedVersion) throws HttpMessageParseException
    {
        float parsedVersion = HttpMessageParser.parseHttpVersion(versionTxt);
        Assertions.assertThat(parsedVersion).isEqualTo(expectedVersion);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "", "h", "t", "p", "http", "http/1.1", "http/", "http1.1", "asdfasdfasd", "Http", "HTtp",
            "HTTp", "HTTP", "HTTP/", "HTTP/1", "HTTP/1.", "HTTP1.1", "/", "/1.1", "HTTP/asdf",
            "HTTP/1.a", "HTTP/a.4", "HTTP/0xFA"
    })
    void throwHttpMessageParseExceptionForInvalidVersion(String invalidVersionTxt)
    {
        Assertions.assertThatThrownBy(() -> HttpMessageParser.parseHttpVersion(invalidVersionTxt));
    }
}