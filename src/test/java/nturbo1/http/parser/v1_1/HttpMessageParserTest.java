package nturbo1.http.parser.v1_1;

import nturbo1.exceptions.http.HttpMessageParseException;
import nturbo1.exceptions.http.InvalidHttpMessageHeaderException;
import nturbo1.http.HttpMethod;
import nturbo1.log.CustomLogger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class HttpMessageParserTest
{
    private static final CustomLogger log = CustomLogger.getLogger(HttpMessageParserTest.class.getName());
    private static class TestParseHttpHeaders
    {
        public byte[] headersBytes;
        public Map<String, List<String>> expectedParsedHeadersMap;

        TestParseHttpHeaders(byte[] headersBytes, Map<String, List<String>> headersMap)
        {
            this.headersBytes = headersBytes;
            this.expectedParsedHeadersMap = headersMap;
        }
    }

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

    @ParameterizedTest
    @MethodSource("validHttpMessageHeadersBytes")
    void givenValidHttpMessageHeaders_whenParsingHttpMessageHeaders_returnMapWithExpectedValues(TestParseHttpHeaders testHeaders)
            throws InvalidHttpMessageHeaderException, IOException
    {
        log.info("Testing header bytes: " + new String(testHeaders.headersBytes));
        InputStream is = new ByteArrayInputStream(testHeaders.headersBytes);
        Map<String, List<String>> parsedHeadersMap = HttpMessageParser.parseHttpMessageHeaders(is);

        for (String headerName : testHeaders.expectedParsedHeadersMap.keySet())
        {
            List<String> headerValues = parsedHeadersMap.get(headerName);
            Assertions.assertThat(headerValues).isNotNull();

            List<String> expectedHeaderValues = testHeaders.expectedParsedHeadersMap.get(headerName);
            for (int i = 0; i < expectedHeaderValues.size(); i++)
            {
                Assertions.assertThat(headerValues.get(i)).isEqualTo(expectedHeaderValues.get(i));
            }
        }

        is.close();
    }

    @ParameterizedTest
    @MethodSource("invalidHttpMessageHeadersBytes")
    void givenInvalidHttpHeaders_whenParsingHttpMessageHeaders_thenThrowException(byte[] headersBytes)
    {
        log.info("Testing headerBytes: " + new String(headersBytes));
        InputStream is = new ByteArrayInputStream(headersBytes);
        Assertions.assertThatThrownBy(() -> HttpMessageParser.parseHttpMessageHeaders(is));
    }

    @Test
    void givenTooManyHeaders_whenParsingHttpMessageHeaders_thenThrowException()
    {
        // TODO: Read too many headers input from a file and test
    }

    static Stream<TestParseHttpHeaders> validHttpMessageHeadersBytes()
    {
        return Stream.of(
                new TestParseHttpHeaders(
                        ("\r\n").getBytes(StandardCharsets.UTF_8),
                        Map.of()
                ),
                new TestParseHttpHeaders(
                        ("Host: example.com\r\n\r\n").getBytes(StandardCharsets.UTF_8),
                        Map.of("host", List.of("example.com"))
                ),
                new TestParseHttpHeaders(
                        ("Host: www.example.com\r\nUser-Agent: Mozilla/5.0\r\nAccept: text/html,application/xhtml+xml\r\n" +
                        "Accept-Language: en-US,en;q=0.9\r\nAccept-Encoding: gzip, deflate\r\n" +
                        "Connection: keep-alive\r\n\r\n")
                                .getBytes(StandardCharsets.UTF_8),
                        Map.of("host", List.of("www.example.com"),
                                "user-agent", List.of("Mozilla/5.0"),
                                "accept", List.of("text/html", "application/xhtml+xml"),
                                "accept-language", List.of("en-US", "en;q=0.9"),
                                "accept-encoding", List.of("gzip", "deflate"),
                                "connection", List.of("keep-alive"))
                ),
                new TestParseHttpHeaders(
                        ("Connection: close\r\nAccept: */*\r\nHost: api.example.com\r\n" +
                        "User-Agent: unit-test-client/1.0\r\n\r\n")
                                .getBytes(StandardCharsets.UTF_8),
                        Map.of("connection", List.of("close"),
                                "accept", List.of("*/*"),
                                "host", List.of("api.example.com"),
                                "user-agent", List.of("unit-test-client/1.0"))
                ),
                new TestParseHttpHeaders(
                        ("Host: example.com\r\nCache-Control: no-cache, no-store, must-revalidate\r\n" +
                        "Accept: application/json;q=0.8, text/plain;q=0.5\r\n" +
                        "Accept-Charset: utf-8, iso-8859-1;q=0.3\r\n\r\n")
                                .getBytes(StandardCharsets.UTF_8),
                        Map.of("host", List.of("example.com"),
                                "cache-control", List.of("no-cache", "no-store", "must-revalidate"),
                                "accept", List.of("application/json;q=0.8", "text/plain;q=0.5"),
                                "accept-charset", List.of("utf-8", "iso-8859-1;q=0.3"))
                ),
                new TestParseHttpHeaders(
                        ("Host: example.com\r\nContent-Disposition: form-data; name=\"field1\"; filename=\"test.txt\"\r\n" +
                        "Content-Type: text/plain; charset=\"utf-8\"\r\n\r\n")
                                .getBytes(StandardCharsets.UTF_8),
                        Map.of("host", List.of("example.com"),
                                "content-disposition", List.of("form-data; name=\"field1\"; filename=\"test.txt\""),
                                "content-type", List.of("text/plain; charset=\"utf-8\""))
                ),
                new TestParseHttpHeaders(
                        ("Host: example.com\r\nContent-Length: 348\r\nDate: Tue, 15 Nov 1994 08:12:31 GMT\r\n\r\n")
                                .getBytes(StandardCharsets.UTF_8),
                        Map.of("host", List.of("example.com"),
                                "content-length", List.of("348"),
                                "date", List.of("Tue, 15 Nov 1994 08:12:31 GMT"))
                ),

                /* CASE-INSENSITIVITY */
                new TestParseHttpHeaders(
                        ("hOsT: Example.COM\r\nUsEr-AgEnT: TestClient\r\n\r\n").getBytes(StandardCharsets.UTF_8),
                        Map.of("host", List.of("Example.COM"), "user-agent", List.of("TestClient"))
                ),
                new TestParseHttpHeaders(
                        ("HOST: example.com\r\nCONNECTION: close\r\n\r\n").getBytes(StandardCharsets.UTF_8),
                        Map.of("host", List.of("example.com"), "connection", List.of("close"))
                ),

                /* WHITESPACE HANDLING */
                new TestParseHttpHeaders(
                        "Host    :    example.com   \r\nConnection :keep-alive\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                        Map.of("host", List.of("example.com"), "connection", List.of("keep-alive"))
                ),
                new TestParseHttpHeaders(
                        "Accept:   text/html  ,  application/json   \r\n\r\n".getBytes(StandardCharsets.UTF_8),
                        Map.of("accept", List.of("text/html", "application/json"))
                ),

                /* COMMA-SEPARATED LIST HEADERS */
                new TestParseHttpHeaders(
                        "Accept: text/html,application/xhtml+xml,application/xml;q=0.9\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                        Map.of("accept", List.of("text/html", "application/xhtml+xml", "application/xml;q=0.9"))
                ),
                new TestParseHttpHeaders(
                        "Accept-Encoding: gzip, deflate, br\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                        Map.of("accept-encoding", List.of("gzip", "deflate", "br"))
                ),
                new TestParseHttpHeaders(
                        "Cache-Control: no-cache, no-store, must-revalidate\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                        Map.of("cache-control", List.of("no-cache", "no-store", "must-revalidate"))
                ),

                /* HEADERS THAT MUST NOT BE SPLIT ON COMMAS */
                new TestParseHttpHeaders(
                        "Date: Tue, 15 Nov 1994 08:12:31\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                        Map.of("date", List.of("Tue, 15 Nov 1994 08:12:31"))
                ),
                new TestParseHttpHeaders(
                        "User-Agent: Mozilla/5.0 (Macintosh, Intel Mac OS X 10_15_7)\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                        Map.of("user-agent", List.of("Mozilla/5.0 (Macintosh, Intel Mac OS X 10_15_7)"))
                ),

                /* SEMICOLON PARAMETERS (SHOULD STAY INTACT) */
                new TestParseHttpHeaders(
                        "Set-Cookie: sessionId=abc123; Path=/; HttpOnly\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                        Map.of("set-cookie", List.of("sessionId=abc123; Path=/; HttpOnly"))
                ),
                new TestParseHttpHeaders(
                        "Content-Type: text/html; charset=UTF-8\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                        Map.of("content-type", List.of("text/html; charset=UTF-8"))
                ),
                new TestParseHttpHeaders(
                        "Content-Disposition: form-data; name=\"file\"; filename=\"test.txt\"\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                        Map.of("content-disposition", List.of("form-data; name=\"file\"; filename=\"test.txt\""))
                ),

                /* REPEATED HEADERS */
                new TestParseHttpHeaders(
                        "Set-Cookie: a=1; Path=/\r\nSet-Cookie: b=2; Path=/\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                        Map.of("set-cookie", List.of("a=1; Path=/", "b=2; Path=/"))
                ),
                new TestParseHttpHeaders(
                        "Accept: text/html\r\nAccept: application/json\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                        Map.of("accept", List.of("text/html", "application/json"))
                ),

                /* HEADERS WITH EMPTY VALUES */
                new TestParseHttpHeaders(
                        "X-Debug:\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                        Map.of("x-debug", List.of())
                ),
                new TestParseHttpHeaders(
                        "X-Optional:    \r\n\r\n".getBytes(StandardCharsets.UTF_8),
                        Map.of("x-optional", List.of())
                ),

                /* NUMERIC HEADER VALUES */
                new TestParseHttpHeaders(
                        "Content-Length: 0\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                        Map.of("content-length", List.of("0"))
                ),
                new TestParseHttpHeaders(
                        "Content-Length: 18446744073709551615\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                        Map.of("content-length", List.of("18446744073709551615"))
                ),

                /* UNKNOWN / EXTENSION HEADERS */
                new TestParseHttpHeaders(
                        "X-Custom-Header: foo, bar, baz\r\nX-Another-One: value\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                        Map.of("x-custom-header", List.of("foo", "bar", "baz"), "x-another-one", List.of("value"))
                ),

                /* LF ONLY LINE TERMINATION */
                new TestParseHttpHeaders(
                        "Host: example.com\n\r\n".getBytes(StandardCharsets.UTF_8),
                        Map.of("host", List.of("example.com"))
                ),
                new TestParseHttpHeaders(
                        "Header: value\nAnother: value\n\n".getBytes(StandardCharsets.UTF_8),
                        Map.of("header", List.of("value"), "another", List.of("value"))
                ),
                new TestParseHttpHeaders(
                        "Host: example.com\r\nUser-Agent: test\nAccept: */*\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                        Map.of("host", List.of("example.com"),
                                "user-agent", List.of("test"),
                                "accept", List.of("*/*")
                        )
                )
        );
    }

    static Stream<byte[]> invalidHttpMessageHeadersBytes()
    {
        return Stream.of(
                /* LINE TERMINATION ERROR */
                // Missing CRLF (LF only)
                // CR only
                "Host: example.com\r".getBytes(StandardCharsets.UTF_8),
                // No final empty line
                "Host: example.com\r\nUser-Agent: test\r\n".getBytes(StandardCharsets.UTF_8),

                /* HEADER NAME SYNTAX VIOLATIONS */
                // Empty header name
                ": value\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                // Whitespace in header name
                "Bad Header: value\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                // Tab in header name
                "Bad\tHeader: value\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                // Control characters in header name
                "Bad\u0001Header: value\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                // Non-token characters
                "Bad@Header: value\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                "Bad/Header: value\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                // Unicode in header name
                "Høst: value\r\n\r\n".getBytes(StandardCharsets.UTF_8),

                /* COLON ERRORS */
                // Missing colon
                "Host example.com\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                // Multiple colons before value
                "Host:: example.com\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                // Colon after whitespace
                "Host : example.com\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                // Colon at end
                "Host:\r\n\r\n".getBytes(StandardCharsets.UTF_8),

                /* HEADER VALUE SYNTAX ERRORS */
                // Leading whitespace without obs-fold context
                " Host: example.com\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                // Value with raw CR
                "Header: value\rmore\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                // Value with raw LF
                "Header: value\nmore\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                // Embedded NULL byte
                "Header: value\u0000test\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                // Unescaped control characters
                "Header: value\u0007\r\n\r\n".getBytes(StandardCharsets.UTF_8),

                /* OBSOLETE LINE FOLDING (OBS-FOLD) ABUSE */
                // Obs-fold without previous header
                " value\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                // Obs-fold starting with tab only
                "Header: value\r\n\tcontinued\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                // Obs-fold containing CRLF inside value
                "Header: value\r\ncontinued\r\n\r\n".getBytes(StandardCharsets.UTF_8),

                /* DUPLICATE AND FORBIDDEN HEADERS */
                // Duplicate Host header
                "Host: example.com\r\nHost: evil.com\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                // Multiple Content-Length headers
                "Content-Length: 10\r\nContent-Length: 20\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                // Conflicting Transfer-Encoding and Content-Length headers
                "Transfer-Encoding: chunked\r\nContent-Length: 5\r\n\r\n".getBytes(StandardCharsets.UTF_8),

                /* HEADER SECTION STRUCTURAL ERRORS */
                // Garbage before headers
                "garbage\r\nHost: example.com\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                // Header after empty line
                "Host: example.com\r\n\r\nUser-Agent: test\r\n".getBytes(StandardCharsets.UTF_8),
                // Multiple empty lines inside headers
                "Host: example.com\r\n\r\n\r\n".getBytes(StandardCharsets.UTF_8),

                /* SIZE AND LENGTH VIOLATIONS */
                // Extremely long header name
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA: value\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                // Extremely long value
                "Header: AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n\r\n".getBytes(StandardCharsets.UTF_8),

                /* INVALID TRANSFER-ENCODING VALUES */
                "Transfer-Encoding: chunked, chunked\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                "Transfer-Encoding: gzip, chunked, invalid\r\n\r\n".getBytes(StandardCharsets.UTF_8),

                /* INVALID CONTENT-LENGTH VALUES */
                // Non-numeric
                "Content-Length: abc\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                // Negative
                "Content-Length: -5\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                // Decimal
                "Content-Length: 5.5\r\n\r\n".getBytes(StandardCharsets.UTF_8),
                // Leading plus
                "Content-Length: +10\r\n\r\n".getBytes(StandardCharsets.UTF_8)
        );
    }
}