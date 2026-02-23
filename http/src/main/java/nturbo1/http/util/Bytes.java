package nturbo1.http.util;

import nturbo1.http.exceptions.InvalidHttpMessageHeaderException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Bytes
{
    /**
     * Reads byte by byte from a given input stream until it detects "\r\n" or '\n' or the end of the input stream.
     *
     * @param iStream the input stream that the bytes are read from.
     * @return an array of bytes read excluding the newline symbols.
     * @throws IOException any IO error happened during a reading process.
     */
    public static byte[] readLine(InputStream iStream) throws IOException
    {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int nextByte;

        while ((nextByte = iStream.read()) != -1)
        {
            if (nextByte == '\n') { break; }

            if (nextByte == '\r')
            {
                int n = iStream.read();
                if (n == '\n') { break; }
            }

            buf.write(nextByte);
        }

        return buf.toByteArray();
    }

    /**
     * Reads byte by byte from a given input stream until it detects "\r\n" or '\n' or the end of the input stream.
     * <p>
     *     If a line ends with neither "\r\n" nor '\n', then throws InvalidHttpMessageHeaderException.
     * </p>
     * <p>
     *     If the stream ends with no empty line, "\r\n" or '\n', then throws InvalidHttpMessageHeaderException.
     * </p>
     *
     * @param iStream the input stream that the bytes are read from.
     * @return an array of bytes read excluding the newline symbols.
     * @throws IOException any IO error happened during a reading process.
     * @throws InvalidHttpMessageHeaderException if an HTTP header line or the whole headers section ends with invalid
     * characters.
     */
    public static byte[] readHttpMessageHeaderLine(InputStream iStream)
            throws IOException, InvalidHttpMessageHeaderException
    {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int nextByte;
        int continuousColonCount = 0; // whitespace is ignored between 2 colons

        readLoop:
        while ((nextByte = iStream.read()) != -1)
        {
            switch (nextByte)
            {
                // control characters in ASCII
                case '\u0000', '\u0001', '\u0007', '\u0008', '\t', '\u0010', '\u0012', '\u0013', 27, 127:
                    throw new InvalidHttpMessageHeaderException(
                            "ASCII Control characters are not allowed in HTTP message header lines");
                case '\n':
                    break readLoop;
                case '\r':
                    int n = iStream.read();
                    if (n == '\n') { break readLoop; }
                    else
                    {
                        throw new InvalidHttpMessageHeaderException("Invalid header line ending with: \r");
                    }
                case ':':
                    continuousColonCount++;
                    if (continuousColonCount == 2)
                    {
                        throw new InvalidHttpMessageHeaderException("Invalid header line containing double colons.");
                    }
                case ' ':
                    break;
                default:
                    continuousColonCount = 0;
            }

            buf.write(nextByte);
        }

        if (nextByte == -1)
        {
            throw new InvalidHttpMessageHeaderException("HTTP message headers section didn't end with empty line");
        }

        return buf.toByteArray();
    }

    public static void read(InputStream iStream, byte[] buf) throws IOException
    {
        for (int i = 0; i < buf.length; i++)
        {
            buf[i] = (byte) iStream.read();
        }
    }
}