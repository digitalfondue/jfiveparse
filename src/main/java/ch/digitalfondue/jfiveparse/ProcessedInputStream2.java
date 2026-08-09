package ch.digitalfondue.jfiveparse;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/// goal of this class is
/// - replace the processed input stream
/// - have a single input type (a Reader)
/// - do buffering instead of calling read n times
final class ProcessedInputStream2 {

    private final Reader reader;
    private final CharBuffer charBuffer = CharBuffer.allocate(1024);

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
            charBuffer.limit(Math.max(count, 0));
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
