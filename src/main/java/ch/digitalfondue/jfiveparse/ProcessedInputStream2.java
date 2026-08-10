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
    private final ByteBuffer byteBuffer;
    private final CharBuffer charBuffer;
    private final LongBuffer longBuffer;

    ProcessedInputStream2(Reader reader, int capacity) {
        this.reader = reader;
        this.byteBuffer = ByteBuffer.allocateDirect(capacity * Character.BYTES);
        this.charBuffer = byteBuffer.asCharBuffer();
        this.longBuffer = byteBuffer.asLongBuffer();
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
                //       and rewrite, note the code is quite complex,
                //       maybe we can simply do a CR ad rewrite byte by byte,
                //       most likely CR is not that present anyway.
                //       what we must take care of is if the CR/LF is at the boundary, we will need to check if LF
                //       is present
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
