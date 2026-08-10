package ch.digitalfondue.jfiveparse;

import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.LongBuffer;

/// goal of this class is
/// - replace the processed input stream
/// - have a single input type (a Reader)
/// - do buffering instead of calling read n times
final class ProcessedInputStream2 {

    private final Reader reader;
    private final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024 * Character.BYTES);
    private final CharBuffer charBuffer = byteBuffer.asCharBuffer();
    private final LongBuffer longBuffer = byteBuffer.asLongBuffer();
    //private final CharBuffer charBuffer = CharBuffer.allocate(1024);

    ProcessedInputStream2(Reader reader) {
        this.reader = reader;
        fillBuffer();
    }

    private boolean fillBuffer() {
        try {
            charBuffer.clear();
            var count = reader.read(charBuffer);
            // after reading, handle "CR LF" pair and single "CR"
            // see https://infra.spec.whatwg.org/#normalize-newlines
            // note, we need to take care of the special case where CR is at the end of
            // the buffer, in this case, we replace it with LF, and at the next fill, if the first
            // char is a LF, we advance position by 1 thus skipping it
            charBuffer.position(0);

            var limit = Math.max(count, 0);
            /*
            for (int i = 0; i < limit/4; i++) {
                // TODO: here can do SWAR for CR LF search
                //       and rewrite
                long packed = longBuffer.get(i);
                char c1 = (char) (packed >>> 48);
                char c2 = (char) (packed >>> 32);
                char c3 = (char) (packed >>> 16);
                char c4 = (char) (packed);
                String unpackedString = new String(new char[]{c1, c2, c3, c4});
                System.out.println(unpackedString);
            }
            for (int i = limit - (limit % 4); i < limit; i++) {
                System.out.println(charBuffer.get(i));
            }
            */

            charBuffer.limit(limit);
            return count < 1;
        } catch (IOException e) {
            throw new ParserException(e);
        }
    }

    int read() {
        var remaining = charBuffer.remaining();
        if (remaining > 0) {
            return charBuffer.get();
        } else {
            var isEof = fillBuffer();
            return isEof && charBuffer.remaining() == 0 ? Characters.EOF : charBuffer.get();
        }
    }
}
