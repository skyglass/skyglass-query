package skyglass.data.common.util;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

final class CharSequenceReader extends Reader {

    //******************************************************************************************************************
    // CLASS
    //******************************************************************************************************************

    //******************************************************************************************************************
    // INSTANCE
    //******************************************************************************************************************
    private CharSequence sequence;
    private int index;
    private int mark;

    //------------------------------------------------------------------------------------------------------------------
    CharSequenceReader(CharSequence sequence) {
        this.sequence = Check.nonNull(sequence, "sequence");
    }

    //------------------------------------------------------------------------------------------------------------------
    public int read(CharBuffer buffer)
    throws IOException {
        Check.nonNull(buffer, "buffer");
        int result = -1;
        synchronized (lock) {
            checkOpen();
            int sequenceLength = sequence.length();
            if (index < sequenceLength) {
                int count = Math.min(buffer.remaining(), sequenceLength - index);
                for (int i = 0; i < count; i++) {
                    buffer.put(sequence.charAt(index));
                    index += 1;
                }
                result = count;
            }
        }
        return result;
    }

    //------------------------------------------------------------------------------------------------------------------
    public int read()
    throws IOException {
        int result = -1;
        synchronized (lock) {
            checkOpen();
            if (index < sequence.length()) {
                result = sequence.charAt(index);
                index += 1;
            }
        }
        return result;
    }

    //------------------------------------------------------------------------------------------------------------------
    public int read(char[] buffer)
    throws IOException {
        return read(buffer, 0, buffer.length);
    }

    //------------------------------------------------------------------------------------------------------------------
    public int read(char[] buffer, int offset, int length)
    throws IOException {
        Check.nonNull(buffer, "buffer");
        Check.indexRange(buffer.length, offset, length);
        int result = -1;
        synchronized (lock) {
            checkOpen();
            int sequenceLength = sequence.length();
            if (index < sequenceLength) {
                int count = Math.min(sequenceLength - index, length);
                for (int i = 0; i < count; i++) {
                    buffer[i + offset] = sequence.charAt(index);
                    index += 1;
                }
                result = count;
            }
        }
        return result;
    }

    //------------------------------------------------------------------------------------------------------------------
    public long skip(long count)
    throws IOException {
        int result;
        Check.nonNegative(count, "count");
        synchronized (lock) {
            result = (int) Math.min(count, sequence.length() - index);
            index += result;
        }
        return result;
    }

    //------------------------------------------------------------------------------------------------------------------
    public boolean ready()
    throws IOException {
        checkOpen();
        return true;
    }

    //------------------------------------------------------------------------------------------------------------------
    public boolean markSupported() {
        return true;
    }

    //------------------------------------------------------------------------------------------------------------------
    public void mark(int readAheadLimit)
    throws IOException {
        Check.nonNegative(readAheadLimit, "readAheadLimit");
        synchronized (lock) {
            checkOpen();
            mark = index;
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    public void reset()
    throws IOException {
        synchronized (lock) {
            checkOpen();
            index = mark;
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    public void close()
    throws IOException {
        synchronized (lock) {
            sequence = null;
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    private void checkOpen()
    throws IOException {
        synchronized (lock) {
            if (sequence == null) {
                throw new IOException("Reader closed");
            }
        }
    }
}
