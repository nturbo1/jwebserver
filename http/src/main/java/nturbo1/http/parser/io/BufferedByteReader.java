package nturbo1.http.parser.io;

import java.io.IOException;

public interface BufferedByteReader {
    /**
     * Returns the next byte in the buffer and increments the current position in the buffer.
     *
     * @return the next byte in the buffer
     */
    int next() throws IOException;

    /**
     * Returns the next byte in the buffer but does not update the current position in the buffer, so, calling `next()`
     * immediately right after it will return the same byte in the buffer.
     *
     * @return the next byte in the buffer
     */
    int peek() throws IOException;
}
