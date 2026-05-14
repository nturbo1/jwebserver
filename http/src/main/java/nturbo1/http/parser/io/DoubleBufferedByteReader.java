package nturbo1.http.parser.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class DoubleBufferedByteReader implements BufferedByteReader {
    private final InputStream istream;

    private static final int BUFFER_SIZE = 8192;

    /**
     * Contains 2 buffers:
     * <pre>
     *  Buffer 0              Buffer 1
     * [ | | | ... | | | |   | |   | ... | | |    ]
     *  0 1 2 ........... n-1 n n+1 ......... 2n-1
     * </pre>
     */
    private final ByteBuffer byteBuffer;

    /**
     * End of the current buffer being processed, either `Buffer 0` or `Buffer 1`.
     */
    private int bufferEnd;

    public DoubleBufferedByteReader(InputStream istream) throws IOException {
        if (istream == null)
            throw new IllegalArgumentException(
                    "Null input stream was passed during " + this.getClass().getName() + " initialization.");

        this.istream = istream;
        this.byteBuffer = ByteBuffer.allocate(2 * BUFFER_SIZE);
        this.fillBuffer();
        assert this.byteBuffer.hasArray() : "Byte buffer MUST have an array!";
        assert this.byteBuffer.position() == 0 : "Byte buffer position MUST start at 0!";

        this.bufferEnd = BUFFER_SIZE;
    }

    @Override
    public int next() throws IOException {
        assert byteBuffer.hasArray() : "Byte buffer MUST have an array after DoubleBufferedByteReader is initialized!";

        final int nextPos = (this.byteBuffer.position() + 1) % (2 * BUFFER_SIZE);
        if (nextPos % BUFFER_SIZE == 0) { // `nextPos` is the beginning of one of the buffers
            this.fillBuffer(); // fill the next buffer with the next chunk of bytes
            this.bufferEnd = ((nextPos + BUFFER_SIZE) % (2 * BUFFER_SIZE)); // end of the next buffer
        }

        return byteBuffer.get();
    }

    @Override
    public int peek() throws IOException {
        final int currPos = this.byteBuffer.position();
        final int currByte = this.byteBuffer.get(currPos);
        assert currPos == this.byteBuffer.position() : "The buffer position got changed.";

        return currByte;
    }

    /**
     * Fills the currently being processed buffer with the next chunk of bytes from the input source.
     */
    private void fillBuffer() throws IOException {
        assert this.byteBuffer != null : "byteBuffer has not been initialized!";
        assert this.istream != null : "The buffered reader input stream is null";
        assert this.byteBuffer.hasArray() : "Byte buffer MUST have an array after DoubleBufferedByteReader is initialized!";

        final int currPos = this.byteBuffer.position();

        int n = this.istream.read(this.byteBuffer.array(), currPos, BUFFER_SIZE);
        assert this.byteBuffer.position() == currPos : "The buffer position was updated!";

        if (n < 0) {
            this.byteBuffer.put(currPos, (byte) -1);
        }
    }

    // Used for testing purposes, should NOT be used for nothing else!
    // Returns a copy of the `byteBuffer`.
    ByteBuffer getByteBuffer() {
        byte[] dest;
        int length = 0;
        int pos = 0;

        if (this.byteBuffer.hasArray()) {
            final byte[] src = this.byteBuffer.array();
            length = src.length;
            pos = this.byteBuffer.position();
            dest = Arrays.copyOf(src, length);
        } else {
            dest = new byte[0];
        }

        ByteBuffer copy = ByteBuffer.wrap(dest, 0, length);
        copy.position(pos);

        return copy;
    }
}
