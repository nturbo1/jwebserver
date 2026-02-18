package nturbo1.http.parser.v1_1;

import nturbo1.exceptions.http.BadHttpRequestHeaderException;
import nturbo1.exceptions.http.HttpMessageParseException;
import nturbo1.exceptions.http.InvalidHttpMessageHeaderException;
import nturbo1.http.HttpMethod;
import nturbo1.http.v1_1.GeneralHeader;
import nturbo1.http.v1_1.HttpEntityHeader;
import nturbo1.http.v1_1.HttpRequestHeader;
import nturbo1.log.CustomLogger;
import nturbo1.util.Bytes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class HttpMessageParser {
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

    private static final CustomLogger log = CustomLogger.getLogger(HttpMessageParser.class.getName());

    public static HttpMethod parseHttpMethod(String method) throws HttpMessageParseException {
        try {
            return HttpMethod.valueOf(method);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            throw new HttpMessageParseException("Unknown HTTP Method: " + method);
        }
    }

    public static float parseHttpVersion(String version) throws HttpMessageParseException {
        if (version.length() < 8) {
            throw new HttpMessageParseException("HTTP Version string is too short: " + version);
        }

        char[] versionChars = version.toCharArray();
        if (
                versionChars[0] != 'H' ||
                        versionChars[1] != 'T' ||
                        versionChars[2] != 'T' ||
                        versionChars[3] != 'P' ||
                        versionChars[4] != '/'
        ) {
            throw new HttpMessageParseException("Invalid HTTP version string format: " + version);
        }

        String versionNum = version.substring(5);
        try {
            return Float.parseFloat(versionNum);
        } catch (NumberFormatException e) {
            log.error(e.getMessage());
            throw new HttpMessageParseException("Invalid HTTP version number: " + versionNum);
        }
    }

    public static Map<String, List<String>> parseHttpMessageHeaders(InputStream iStream)
            throws InvalidHttpMessageHeaderException, IOException {
        log.debug("Parsing the HTTP Message Headers...");
        Map<String, List<String>> headers = new HashMap<>();
        while (true) {
            String line;
            try {
                line = new String(Bytes.readHttpMessageHeaderLine(iStream));
            } catch (IOException e) {
                log.error("Failed to read the next line from buffer because: " + e.getMessage());
                throw e;
            }

            if (line.isEmpty()) { // End of the http message headers section
                break;
            }

            String[] headerKV = parseHttpMessageHeaderLine(line);

            String headerKey = headerKV[0];
            List<String> headerValues = parseAndNormalizeHeaderValue(headerKey, headerKV[1]);

            List<String> headerValueList = headers.get(headerKey);
            if (headerValueList == null)
            {
                headerValueList = new ArrayList<>(headerValues);
            }
            else
            {
                if (NON_REPEATABLE_HEADERS.contains(headerKey.toLowerCase()))
                {
                    throw new InvalidHttpMessageHeaderException("More than one instances of " + headerKey +
                            " header was encountered in the http message headers");
                }
                headerValueList.addAll(headerValues);
            }

            headers.put(headerKey, headerValueList);
        }

        log.debug("Successfully parsed the HTTP Message Headers!");
        return headers;
    }

    public static boolean isHeaderCommaSeparatedList(String headerName)
    {
        return !headerName.equalsIgnoreCase(GeneralHeader.DATE.getName()) &&
                !headerName.equalsIgnoreCase(HttpRequestHeader.USER_AGENT.getName());
    }

    private static List<String> parseAndNormalizeHeaderValue(String headerName, String headerValue)
    {
        List<String> headerVals = new ArrayList<>();
        if (isHeaderCommaSeparatedList(headerName))
        {
            String[] commaSplitVals = headerValue.trim().split(",");
            for (String val : commaSplitVals)
            {
                headerVals.add(val.trim());
            }
        }
        else
        {
            headerVals.add(headerValue.trim());
        }

        return headerVals;
    }

    public static byte[] readMessageBodyBytes(InputStream iStream, Map<String, List<String>> headers)
            throws BadHttpRequestHeaderException, HttpMessageParseException, IOException
    {
        List<String> contentLength = headers.get(HttpEntityHeader.CONTENT_LENGTH.getName().toLowerCase());
        List<String> transferEncoding = headers.get(GeneralHeader.TRANSFER_ENCODING.getName().toLowerCase());

        byte[] messageBodyBytes = null;

        if (contentLength != null && transferEncoding != null) {
            log.warn(GeneralHeader.TRANSFER_ENCODING.name() + " should be preferred over " +
                    HttpEntityHeader.CONTENT_LENGTH + " if both exist");
            throw new BadHttpRequestHeaderException(
                    "Both '" + HttpEntityHeader.CONTENT_LENGTH.getName() + "' and '" +
                            GeneralHeader.TRANSFER_ENCODING.getName() + "' headers are present in the request."
            );
        } else if (contentLength != null && !contentLength.isEmpty()) {
            messageBodyBytes = readHttpMessageBody(iStream, contentLength.getFirst());
        } else if (transferEncoding != null && !transferEncoding.isEmpty()) {
            String lowerCaseTransferEncoding = transferEncoding.getFirst().toLowerCase();
            if (lowerCaseTransferEncoding.equals("chunked")) {
                messageBodyBytes = readChunkedHttpMessageBody(iStream);
            } else {
                log.warn("CHECK THE OTHER TRANSFER-ENCODING HEADER VALUES!!!");
                throw new HttpMessageParseException(
                        "Unsupported '" + GeneralHeader.TRANSFER_ENCODING.getName() + "' header value: " +
                                lowerCaseTransferEncoding
                );
            }
        }

        return messageBodyBytes;
    }

    public static byte[] readHttpMessageBody(InputStream iStream, String contentLengthStr) throws HttpMessageParseException, IOException
    {
        int contentLength;
        try {
            contentLength = Integer.parseInt(contentLengthStr);
        } catch (NumberFormatException e) {
            throw new HttpMessageParseException(
                    "Failed to convert '" + HttpEntityHeader.CONTENT_LENGTH.getName() + "' header value '" +
                            contentLengthStr + "' into an integer value."
            );
        }

        byte[] messageBodyBytes = new byte[contentLength];
        Bytes.read(iStream, messageBodyBytes);

        return messageBodyBytes;
    }

    public static byte[] readChunkedHttpMessageBody(InputStream iStream) throws HttpMessageParseException, IOException
    {
        ByteArrayOutputStream messageBodyBytes = new ByteArrayOutputStream();
        byte[] nextChunk;
        while ((nextChunk = readNextHttpMessageBodyChunk(iStream)) != null)
        {
            messageBodyBytes.writeBytes(nextChunk);
        }

        return messageBodyBytes.toByteArray();
    }

    public static byte[] readNextHttpMessageBodyChunk(InputStream iStream) throws HttpMessageParseException, IOException
    {
        String chunkSizeHex;
        try {
            chunkSizeHex = new String(Bytes.readLine(iStream));
        } catch (IOException e) {
            log.error("Failed to read an HTTP Message Body chunk size line");
            throw e;
        }

        int chunkSize;
        try {
            chunkSize = Integer.parseInt(chunkSizeHex.trim(), 16);
        } catch (NumberFormatException e) {
            throw new HttpMessageParseException("Failed to parse the chunk size hex value '" + chunkSizeHex + "' into an integer value.");
        }

        if (chunkSize == 0) { return null; }

        byte[] chunkBytes = new byte[chunkSize];
        Bytes.read(iStream, chunkBytes);

        return chunkBytes;
    }

    private static void validateHttpMessageHeaderName(String headerName) throws InvalidHttpMessageHeaderException
    {
        if (!Pattern.matches(HTTP_MESSAGE_HEADER_NAME_REGEX, headerName))
        {
            throw new InvalidHttpMessageHeaderException("Invalid HTTP message header name.");
        }
    }

    private static String[] parseHttpMessageHeaderLine(String line) throws InvalidHttpMessageHeaderException
    {
        String[] headerKV = line.split(":", 2);
        if (headerKV.length < 2) {
            throw new InvalidHttpMessageHeaderException("Invalid HTTP Message Header format: " + line);
        }
        headerKV[0] = headerKV[0].trim().toLowerCase();
        validateHttpMessageHeaderName(headerKV[0]);
        headerKV[1] = headerKV[1].trim();

        return headerKV;
    }
}
