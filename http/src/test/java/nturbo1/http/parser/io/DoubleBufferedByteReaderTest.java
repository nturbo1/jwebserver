package nturbo1.http.parser.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DoubleBufferedByteReaderTest {
    @Test
    void givenNonEmptyInputStream_whenNext_thenExpectedByteIsReturnedAndBufferPosIsIncremented() throws IOException {
        // GIVEN
        byte[] input = new byte[]{ 'H', 'o', 's', 't' };
        InputStream inputStream = new ByteArrayInputStream(input);
        DoubleBufferedByteReader bufReader = new DoubleBufferedByteReader(inputStream);
        int beforePos = bufReader.getByteBuffer().position();
        Assertions.assertTrue(beforePos >= 0);

        // WHEN
        int nextByte = bufReader.next();

        // THEN
        Assertions.assertEquals(input[0], nextByte);
        int afterPos = bufReader.getByteBuffer().position();
        Assertions.assertEquals(beforePos + 1, afterPos);
    }
    @Test
    void givenNonEmptyInputStream_whenPeek_thenExpectedByteIsReturnedAndBufferPosIsNotChanged() throws IOException {
        // GIVEN
        byte[] input = new byte[]{ 'H', 'o', 's', 't' };
        InputStream inputStream = new ByteArrayInputStream(input);
        DoubleBufferedByteReader bufReader = new DoubleBufferedByteReader(inputStream);
        int beforePos = bufReader.getByteBuffer().position();
        Assertions.assertTrue(beforePos >= 0);

        // WHEN
        int nextByte = bufReader.peek();

        // THEN
        Assertions.assertEquals(input[0], nextByte);
        int afterPos = bufReader.getByteBuffer().position();
        Assertions.assertEquals(beforePos, afterPos);
    }
}
