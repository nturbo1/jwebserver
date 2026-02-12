package nturbo1.http.parser.v1_1;

import nturbo1.http.HttpMethod;
import nturbo1.exceptions.parser.HttpMessageParseException;
    
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.assertj.core.api.Assertions;

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
    void throwsHttpMessageParseExceptionForUnknownMethod()
    {
        String unknownHttpMethod = "asdfasdf";
        Assertions.assertThatThrownBy(() -> HttpMessageParser.parseHttpMethod(unknownHttpMethod));
    }
}
