package skyglass.data.common.util;

import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

final public class IO {

    final static private int BUFFER_SIZE =
        Integer.getInteger("skyglass.data.common.util.IO.bufferSize", 8192);
    final static private String WINDOWS_INVALID_FILENAME_CHARS = "\\/:*?\"<>|";

    //----------------------------------------------------------------------------------------------
    final static public OutputStream NULL = new OutputStream() {
        @Override public void close() {}
        @Override public void flush() {}
        @Override public void write(byte[] buffer) {
            Check.nonNull(buffer, "buffer");
        }
        @Override public void write(byte[] buffer, int offset, int length) {
            Check.nonNull(buffer, "buffer");
            Check.indexRange(buffer.length, offset, length);
        }
        @Override public void write(int byteValue) {}
    };

    //----------------------------------------------------------------------------------------------
    final static public Writer CNULL = new Writer() {
        @Override public void close() {}
        @Override public void flush() {}
        @Override public void write(char[] buffer) {
            Check.nonNull(buffer, "buffer");
        }
        @Override public void write(char[] buffer, int offset, int length) {
            Check.nonNull(buffer, "buffer");
            Check.indexRange(buffer.length, offset, length);
        }
        @Override public void write(int byteValue) {}
        @Override public void write(String string) {
            Check.nonNull(string, "string");
        }
        @Override public void write(String string, int offset, int length) {
            Check.nonNull(string, "string");
            Check.indexRange(string.length(), offset, length);
        }
    };

    //----------------------------------------------------------------------------------------------
    final static public InputStream ZERO = new InputStream() {
        @Override public int available() { return Integer.MAX_VALUE; }
        @Override public void close() {}
        @Override public void mark(int readlimit) {}
        @Override public boolean markSupported() { return true; }
        @Override public int read() { return 0; }
        @Override public int read(byte[] buffer) {
            Check.nonNull(buffer, "buffer");
            Arrays.fill(buffer, (byte)0);
            return buffer.length;
        }
        @Override
        public int read(byte[] buffer, int offset, int length) {
            Check.nonNull(buffer, "buffer");
            Check.indexRange(buffer.length, offset, length);
            Arrays.fill(buffer, offset, offset + length, (byte)0);
            return length;
        }
        @Override public void reset() {}
        @Override public long skip(long n) { return n; }
    };

    //----------------------------------------------------------------------------------------------
    /**
     * Returns a {@link Charset} for US-ASCII.
     */
    static public Charset ascii() {
        return AsciiInitializer.ASCII;
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedInputStream buffer(InputStream in) {
        BufferedInputStream result;
        boolean exceptionInProgress = true;
        try {
            result = new BufferedInputStream(in);
            exceptionInProgress = false;
        }
        finally {
            closeOnException(in, exceptionInProgress);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedOutputStream buffer(OutputStream out) {
        BufferedOutputStream result;
        boolean exceptionInProgress = true;
        try {
            result = new BufferedOutputStream(out);
            exceptionInProgress = false;
        }
        finally {
            closeOnException(out, exceptionInProgress);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedReader buffer(Reader in) {
        boolean exceptionInProgress = true;
        BufferedReader result;
        try {
            result = new BufferedReader(in);
            exceptionInProgress = false;
        }
        finally {
            closeOnException(in, exceptionInProgress);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter buffer(Writer out) {
        BufferedWriter result;
        boolean exceptionInProgress = true;
        try {
            result = new BufferedWriter(out);
            exceptionInProgress = false;
        }
        finally {
            closeOnException(out, exceptionInProgress);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Builds a {@link File} object out of path elements. The resulting
     * <code>File</code> will be absolute if the base is absolute and relative
     * if it is relative. It will contain only native path separators. The path
     * elements are appended to the base in order as relative paths. If no path
     * elements are supplied, the result is the base unmodified.
     *
     * @param base
     *        base of the resulting path. Non-<code>null</code>.
     * @param pathElements
     *        paths combined to the base to construct a <code>File</code>. May
     *        not contain <code>null</code>.
     * @return a <code>File</code> instance. Non-<code>null</code>.
     */
    static public File build(File base, String... pathElements) {
        Check.nonNull(base, "base");
        if (pathElements != null) {
            for (String p : pathElements) {
                base = new File(base, p);
            }
        }
        return base;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Builds a {@link File} object out of path elements. The resulting
     * <code>File</code> will be absolute and contain only native path
     * separators. If the first path element is absolute (as determined by
     * {@link File#isAbsolute()}), it will be used as the base. Otherwise, the
     * present working directory is the base. Remaining path elements are
     * appended in order as relative paths. If no path elements are supplied,
     * the result is the present working directory.
     *
     * @param pathElements
     *        paths combined to construct a <code>File</code>. May be
     *        <code>null</code>. May not contain <code>null</code>.
     * @return a <code>File</code> instance. Non-<code>null</code>.
     */
    static public File build(String... pathElements) {
        if (pathElements == null || pathElements.length == 0) {
            return pwd();
        }
        String first = pathElements[0];
        File base = new File(first);
        if (!base.isAbsolute()) {
            base = new File(pwd(), first);
        }
        for (int i = 1; i < pathElements.length; i++) {
            base = new File(base, pathElements[i]);
        }
        return base;
    }

    //----------------------------------------------------------------------------------------------
    static public boolean cleanup(List<File> files) {
        boolean exceptionInProgress = true;
        try {
            for (File file : files) {
                try {
                    delete(file);
                }
                catch (Throwable swallow) {
                }
            }
            exceptionInProgress = false;
        }
        catch (Throwable swallow) {
        }
        return !exceptionInProgress;
    }

    //----------------------------------------------------------------------------------------------
    static public void close(Closeable closeable) {
        closeCloseable(closeable);
    }

    //----------------------------------------------------------------------------------------------
    static public void close(DatagramSocket closeable) {
        if (closeable != null) {
            closeable.close();
        }
    }

    //----------------------------------------------------------------------------------------------
    static public void close(InputStream closeable) {
        closeCloseable(closeable);
    }

    //----------------------------------------------------------------------------------------------
    static public void close(OutputStream closeable) {
        closeCloseable(closeable);
    }

    //----------------------------------------------------------------------------------------------
    static public void close(RandomAccessFile closeable) {
        closeCloseable(closeable);
    }

    //----------------------------------------------------------------------------------------------
    static public void close(Reader closeable) {
        closeCloseable(closeable);
    }

    //----------------------------------------------------------------------------------------------
    static public void close(Writer closeable) {
        closeCloseable(closeable);
    }

    //----------------------------------------------------------------------------------------------
    static public void close(ZipFile closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            }
            catch (IOException swallow) {
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    static public void closeNoThrow(Closeable closeable) {
        closeNoThrowCloseable(closeable);
    }

    //----------------------------------------------------------------------------------------------
    static public void closeNoThrow(DatagramSocket closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            }
            catch (Throwable swallow) {
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    static public void closeNoThrow(InputStream closeable) {
        closeNoThrowCloseable(closeable);
    }

    //----------------------------------------------------------------------------------------------
    static public void closeNoThrow(OutputStream closeable) {
        closeNoThrowCloseable(closeable);
    }

    //----------------------------------------------------------------------------------------------
    static public void closeNoThrow(RandomAccessFile closeable) {
        closeNoThrowCloseable(closeable);
    }

    //----------------------------------------------------------------------------------------------
    static public void closeNoThrow(Reader closeable) {
        closeNoThrowCloseable(closeable);
    }

    //----------------------------------------------------------------------------------------------
    static public void closeNoThrow(ServerSocket closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            }
            catch (Throwable swallow) {
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    static public void closeNoThrow(Socket closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            }
            catch (Throwable swallow) {
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    static public void closeNoThrow(Writer closeable) {
        closeNoThrowCloseable(closeable);
    }

    //----------------------------------------------------------------------------------------------
    static public void closeNoThrow(ZipFile closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            }
            catch (Throwable swallow) {
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Closes a {@link Closeable} without suppressing any existing exception. No
     * action is taken if the <code>Closeable</code> is <code>null</code>. If no
     * exception is in progress (<code>exceptionInProgress == true</code>), any
     * exception produced by {@link Closeable#close()} will be allowed to
     * propagate. Otherwise, the exception is suppressed with the expectation
     * that the exception already thrown is more useful.
     * <p>
     * Allowing <code>Closeable.close()</code> to throw is desirable because it
     * may indicate that a previous IO operation did not complete, but was not
     * signalled by the operating system until close.
     */
    static public void closeSafely(Closeable closeable, boolean exceptionInProgress)
    throws IOException {
        if (closeable != null) {
            if (exceptionInProgress) {
                try {
                    closeable.close();
                }
                catch (Throwable t) {
                }
            }
            else {
                closeable.close();
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Closes a {@link DatagramSocket} without suppressing any existing
     * exception. No action is taken if the <code>DatagramSocket</code> is
     * <code>null</code>. If no exception is in progress (
     * <code>exceptionInProgress == true</code>), any exception produced by
     * {@link DatagramSocket#close()} will be allowed to propagate. Otherwise,
     * the exception is suppressed with the expectation that the exception
     * already thrown is more useful.
     * <p>
     * Allowing <code>DatagramSocket.close()</code> to throw is desirable
     * because it may indicate that a previous IO operation did not complete,
     * but was not signalled by the operating system until close.
     */
    static public void closeSafely(DatagramSocket closeable, boolean exceptionInProgress) {
        if (closeable != null) {
            if (exceptionInProgress) {
                try {
                    closeable.close();
                }
                catch (Throwable t) {
                }
            }
            else {
                closeable.close();
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Closes a {@link ServerSocket} without suppressing any existing exception.
     * No action is taken if the <code>ServerSocket</code> is <code>null</code>.
     * If no exception is in progress (<code>exceptionInProgress == true</code>
     * ), any exception produced by {@link ServerSocket#close()} will be allowed
     * to propagate. Otherwise, the exception is suppressed with the expectation
     * that the exception already thrown is more useful.
     * <p>
     * Allowing <code>ServerSocket.close()</code> to throw is desirable because
     * it may indicate that a previous IO operation did not complete, but was
     * not signalled by the operating system until close.
     */
    static public void closeSafely(ServerSocket closeable, boolean exceptionInProgress)
    throws IOException {
        if (closeable != null) {
            if (exceptionInProgress) {
                try {
                    closeable.close();
                }
                catch (Throwable t) {
                }
            }
            else {
                closeable.close();
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Closes a {@link Socket} without suppressing any existing exception. No
     * action is taken if the <code>Socket</code> is <code>null</code>. If no
     * exception is in progress (<code>exceptionInProgress == true</code>), any
     * exception produced by {@link Socket#close()} will be allowed to
     * propagate. Otherwise, the exception is suppressed with the expectation
     * that the exception already thrown is more useful.
     * <p>
     * Allowing <code>Socket.close()</code> to throw is desirable because it
     * may indicate that a previous IO operation did not complete, but was not
     * signalled by the operating system until close.
     */
    static public void closeSafely(Socket closeable, boolean exceptionInProgress)
    throws IOException {
        if (closeable != null) {
            if (exceptionInProgress) {
                try {
                    closeable.close();
                }
                catch (Throwable t) {
                }
            }
            else {
                closeable.close();
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Closes a {@link ZipFile} without suppressing any existing exception. If
     * no exception is in progress (<code>exceptionInProgress == true</code>),
     * any exception produced by {@link ZipFile#close()} will be allowed to
     * propagate. Otherwise, the exception is suppressed with the expectation
     * that the exception already thrown is more useful.
     * <p>
     * Allowing <code>Closeable.close()</code> to throw is desirable because it
     * may indicate that a previous IO operation did not complete, but was not
     * signalled by the operating system until close.
     */
    static public void closeSafely(ZipFile closeable, boolean exceptionInProgress)
    throws IOException {
        if (closeable != null) {
            if (exceptionInProgress) {
                try {
                    closeable.close();
                }
                catch (Throwable t) {
                }
            }
            else {
                closeable.close();
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    static public void copy(File src, File dst)
    throws IOException {
        if (src.equals(dst)) {
            String fmt = "Copy source (%s) and destination (%s) are the same location";
            throw new IllegalArgumentException(String.format(fmt, src, dst));
        }

        if (src.isDirectory()) {
            mkdirs(dst);
            for (File src2 : listFiles(src)) {
                File dst2 = new File(dst, src2.getName());
                copy(src2, dst2);
            }
        }
        else {
            boolean exceptionInProgress = true;
            InputStream in = new FileInputStream(src);
            try {
                copy(in, dst);
                exceptionInProgress = false;
            }
            finally {
                closeSafely(in, exceptionInProgress);
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    static public void copy(File src, File dst, List<File> newFiles)
    throws IOException {
        if (src.isDirectory()) {
            mkdirs(dst, newFiles);
            for (File src2 : listFiles(src)) {
                File dst2 = new File(dst, src2.getName());
                copy(src2, dst2, newFiles);
            }
        }
        else {
            if (dst.createNewFile()) {
                newFiles.add(dst);
            }
            copy(src, dst);
        }
    }

    //----------------------------------------------------------------------------------------------
    static public void copy(File src, OutputStream out)
    throws IOException {
        boolean exceptionInProgress = true;
        InputStream in = new FileInputStream(src);
        try {
            copy(in, out);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(in, exceptionInProgress);
        }
    }

    //----------------------------------------------------------------------------------------------
    /**
     * @return The number of bytes that are uncopied because of EOF.
     */
    static public long copy(File src, OutputStream out, long count)
    throws IOException {
        long result;
        boolean exceptionInProgress = true;
        InputStream in = new FileInputStream(src);
        try {
            result = copy(in, out, count);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(in, exceptionInProgress);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public void copy(InputStream in, File dst)
    throws IOException {
        boolean exceptionInProgress = true;
        OutputStream out = new FileOutputStream(dst);
        try {
            copy(in, out);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(out, exceptionInProgress);
        }
    }

    //----------------------------------------------------------------------------------------------
    /**
     * @return The number of bytes that are uncopied because of EOF.
     */
    static public long copy(InputStream in, File dst, long count)
    throws IOException {
        long result;
        boolean exceptionInProgress = true;
        OutputStream out = new FileOutputStream(dst);
        try {
            result = copy(in, out, count);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(out, exceptionInProgress);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public void copy(InputStream in, OutputStream out)
    throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int count;
        while ((count = in.read(buffer)) != -1) {
            out.write(buffer, 0, count);
        }
    }

    //----------------------------------------------------------------------------------------------
    /**
     * @return The number of bytes that are uncopied because of EOF.
     */
    static public long copy(InputStream in, OutputStream out, long count)
    throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        int length = (int) Math.min(count, buffer.length);
        while (count > 0 && (read = in.read(buffer, 0, length)) != -1) {
            out.write(buffer, 0, read);
            count -= read;
            length = (int) Math.min(count, buffer.length);
        }
        return count;
    }

    //----------------------------------------------------------------------------------------------
    static public void copy(Reader in, Appendable appendable)
    throws IOException {
        char[] buffer = new char[BUFFER_SIZE];
        CharBuffer charBuffer = CharBuffer.wrap(buffer);
        int count;
        while ((count = in.read(buffer)) != -1) {
            appendable.append(charBuffer, 0, count);
        }
    }

    //----------------------------------------------------------------------------------------------
    /**
     * @return The number of chars that are unread because of EOF.
     */
    static public long copy(Reader in, Appendable appendable, long count)
    throws IOException {
        char[] buffer = new char[BUFFER_SIZE];
        CharBuffer charBuffer = CharBuffer.wrap(buffer);
        int read;
        int length = (int) Math.min(count, buffer.length);
        while ((read = in.read(buffer, 0, length)) != -1) {
            appendable.append(charBuffer, 0, read);
            count -= read;
            length = (int) Math.min(count, buffer.length);
        }
        return count;
    }

    //----------------------------------------------------------------------------------------------
    static public void copy(Reader in, File file)
    throws IOException {
        boolean exceptionInProgress = true;
        Writer out = writer(file);
        try {
            copy(in, out);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(out, exceptionInProgress);
        }
    }

    //----------------------------------------------------------------------------------------------
    static public void copy(Reader in, File file, Charset charset)
    throws IOException {
        CharsetEncoder encoder = charset.newEncoder();
        copy(in, file, encoder);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * @return The number of chars that are unread because of EOF.
     */
    static public long copy(Reader in, File file, Charset charset, long count)
    throws IOException {
        CharsetEncoder encoder = charset.newEncoder();
        return copy(in, file, encoder, count);
    }

    static public void copy(Reader in, File file, CharsetEncoder encoder)
    throws IOException {
        boolean exceptionInProgress = true;
        Writer out = writer(file, encoder);
        try {
            copy(in, out);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(out, exceptionInProgress);
        }
    }

    /**
     * @return The number of chars that are unread because of EOF.
     */
    static public long copy(Reader in, File file, CharsetEncoder encoder, long count)
    throws IOException {
        long result;
        boolean exceptionInProgress = true;
        Writer out = writer(file, encoder);
        try {
            result = copy(in, out, count);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(out, exceptionInProgress);
        }
        return result;
    }

    /**
     * @return The number of chars that are unread because of EOF.
     */
    static public long copy(Reader in, File file, long count)
    throws IOException {
        long result;
        boolean exceptionInProgress = true;
        Writer out = writer(file);
        try {
            result = copy(in, out, count);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(out, exceptionInProgress);
        }
        return result;
    }

    static public void copy(Reader in, File file, String charsetName)
    throws IOException {
        Charset charset = Charset.forName(charsetName);
        copy(in, file, charset);
    }

    /**
     * @return The number of chars that are unread because of EOF.
     */
    static public long copy(Reader in, File file, String charsetName, long count)
    throws IOException {
        Charset charset = Charset.forName(charsetName);
        return copy(in, file, charset, count);
    }

    static public void copy(Reader in, Writer out)
    throws IOException {
        char[] buffer = new char[BUFFER_SIZE];
        int count;
        while ((count = in.read(buffer)) != -1) {
            out.write(buffer, 0, count);
        }
    }

    /**
     * @return The number of chars that are unread because of EOF.
     */
    static public long copy(Reader in, Writer out, long count)
    throws IOException {
        char[] buffer = new char[BUFFER_SIZE];
        int read;
        int length = (int) Math.min(count, buffer.length);
        while ((read = in.read(buffer, 0, length)) != -1) {
            out.write(buffer, 0, read);
            count -= read;
            length = (int) Math.min(count, buffer.length);
        }
        return count;
    }

    static public void copyAndDigest(File src, OutputStream out, MessageDigest... digesters)
    throws IOException {
        if (digesters == null || digesters.length == 0) {
            copy(src, out);
        }
        else {
            boolean exceptionInProgress = true;
            InputStream in = new FileInputStream(src);
            try {
                copyAndDigest(in, out, digesters);
                exceptionInProgress = false;
            }
            finally {
                closeSafely(in, exceptionInProgress);
            }
        }
    }

    /**
     * @return The number of bytes that are uncopied because of EOF.
     */
    static public long copyAndDigest(InputStream in, File dst, long count, MessageDigest... digesters)
    throws IOException {
        long result;
        if (digesters == null || digesters.length == 0) {
            result = copy(in, dst, count);
        }
        else {
            boolean exceptionInProgress = true;
            OutputStream out = new FileOutputStream(dst);
            try {
                result = copyAndDigest(in, out, count, digesters);
                exceptionInProgress = false;
            }
            finally {
                closeSafely(out, exceptionInProgress);
            }
        }
        return result;
    }

    static public void copyAndDigest(InputStream in, File dst, MessageDigest... digesters)
    throws IOException {
        if (digesters == null || digesters.length == 0) {
            copy(in, dst);
        }
        else {
            boolean exceptionInProgress = true;
            OutputStream out = new FileOutputStream(dst);
            try {
                copyAndDigest(in, out, digesters);
                exceptionInProgress = false;
            }
            finally {
                closeSafely(out, exceptionInProgress);
            }
        }
    }

    /**
     * @return The number of bytes that are uncopied because of EOF.
     */
    static public long copyAndDigest(InputStream in, OutputStream out, long count, MessageDigest... digesters)
    throws IOException {
        long result;
        if (digesters == null || digesters.length == 0) {
            result = copy(in, out, count);
        }
        else {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            int length = (int) Math.min(count, buffer.length);
            while (count > 0 && (read = in.read(buffer, 0, length)) != -1) {
                for (MessageDigest digester : digesters) {
                    digester.update(buffer, 0, read);
                }
                out.write(buffer, 0, read);
                count -= read;
                length = (int) Math.min(count, buffer.length);
            }
            result = count;
        }
        return result;
    }

    static public void copyAndDigest(InputStream in, OutputStream out, MessageDigest... digesters)
    throws IOException {
        if (digesters == null || digesters.length == 0) {
            copy(in, out);
        }
        else {
            byte[] buffer = new byte[BUFFER_SIZE];
            int count;
            while ((count = in.read(buffer)) != -1) {
                for (MessageDigest digester : digesters) {
                    digester.update(buffer, 0, count);
                }
                out.write(buffer, 0, count);
            }
        }
    }

    /**
     * @return The number of bytes that are uncopied because of EOF.
     */
    static public long copyAndDigestSync(InputStream in, File dst, long count, MessageDigest... digesters)
    throws IOException {
        long result;
        if (digesters == null || digesters.length == 0) {
            result = copySync(in, dst, count);
        }
        else {
            boolean exceptionInProgress = true;
            OutputStream out = new SyncFileOutputStream(dst);
            try {
                result = copyAndDigest(in, out, count, digesters);
                exceptionInProgress = false;
            }
            finally {
                closeSafely(out, exceptionInProgress);
            }
        }
        return result;
    }

    static public void copyAndDigestSync(InputStream in, File dst, MessageDigest... digesters)
    throws IOException {
        if (digesters == null || digesters.length == 0) {
            copySync(in, dst);
        }
        else {
            boolean exceptionInProgress = true;
            OutputStream out = new SyncFileOutputStream(dst);
            try {
                copyAndDigest(in, out, digesters);
                exceptionInProgress = false;
            }
            finally {
                closeSafely(out, exceptionInProgress);
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    static public void copyAppend(File src, File dst)
    throws IOException {
        if (src.isDirectory()) {
            mkdirs(dst);
            for (File src2 : listFiles(src)) {
                File dst2 = new File(dst, src2.getName());
                copy(src2, dst2);
            }
        }
        else {
            boolean exceptionInProgress = true;
            InputStream in = new FileInputStream(src);
            try {
                copyAppend(in, dst);
                exceptionInProgress = false;
            }
            finally {
                closeSafely(in, exceptionInProgress);
            }
        }
    }

    static public void copyAppend(InputStream in, File dst)
    throws IOException {
        boolean exceptionInProgress = true;
        OutputStream out = new FileOutputStream(dst, true);
        try {
            copy(in, out);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(out, exceptionInProgress);
        }
    }

    //----------------------------------------------------------------------------------------------
    static public void copySync(File src, File dst)
    throws IOException {
        if (src.isDirectory()) {
            mkdirs(dst);
            for (File src2 : listFiles(src)) {
                File dst2 = new File(dst, src2.getName());
                copySync(src2, dst2);
            }
        }
        else {
            boolean exceptionInProgress = true;
            InputStream in = new FileInputStream(src);
            try {
                copySync(in, dst);
                exceptionInProgress = false;
            }
            finally {
                closeSafely(in, exceptionInProgress);
            }
        }
    }

    static public void copySync(File src, File dst, List<File> newFiles)
    throws IOException {
        if (src.isDirectory()) {
            mkdirs(dst, newFiles);
            for (File src2 : listFiles(src)) {
                File dst2 = new File(dst, src2.getName());
                copySync(src2, dst2, newFiles);
            }
        }
        else {
            if (dst.createNewFile()) {
                newFiles.add(dst);
            }
            copySync(src, dst);
        }
    }

    static public void copySync(InputStream in, File dst)
    throws IOException {
        boolean exceptionInProgress = true;
        OutputStream out = new SyncFileOutputStream(dst);
        try {
            copy(in, out);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(out, exceptionInProgress);
        }
    }

    /**
     * @return The number of bytes that are uncopied because of EOF.
     */
    static public long copySync(InputStream in, File dst, long count)
    throws IOException {
        long result;
        boolean exceptionInProgress = true;
        OutputStream out = new SyncFileOutputStream(dst);
        try {
            result = copy(in, out, count);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(out, exceptionInProgress);
        }
        return result;
    }

    static public void copySync(Reader in, File file)
    throws IOException {
        boolean exceptionInProgress = true;
        Writer out = writer(new SyncFileOutputStream(file));
        try {
            copy(in, out);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(out, exceptionInProgress);
        }
    }

    static public void copySync(Reader in, File file, Charset charset)
    throws IOException {
        CharsetEncoder encoder = charset.newEncoder();
        copySync(in, file, encoder);
    }

    static public long copySync(Reader in, File file, Charset charset, long count)
    throws IOException {
        CharsetEncoder encoder = charset.newEncoder();
        return copySync(in, file, encoder, count);
    }

    static public void copySync(Reader in, File file, CharsetEncoder encoder)
    throws IOException {
        boolean exceptionInProgress = true;
        Writer out = writer(new SyncFileOutputStream(file), encoder);
        try {
            copy(in, out);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(out, exceptionInProgress);
        }
    }

    static public long copySync(Reader in, File file, CharsetEncoder encoder, long count)
    throws IOException {
        long result;
        boolean exceptionInProgress = true;
        Writer out = writer(new SyncFileOutputStream(file), encoder);
        try {
            result = copy(in, out, count);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(out, exceptionInProgress);
        }
        return result;
    }

    static public long copySync(Reader in, File file, long count)
    throws IOException {
        long result;
        boolean exceptionInProgress = true;
        Writer out = writer(new SyncFileOutputStream(file));
        try {
            result = copy(in, out, count);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(out, exceptionInProgress);
        }
        return result;
    }

    static public void copySync(Reader in, File file, String charsetName)
    throws IOException {
        Charset charset = Charset.forName(charsetName);
        copySync(in, file, charset);
    }

    static public long copySync(Reader in, File file, String charsetName, long count)
    throws IOException {
        Charset charset = Charset.forName(charsetName);
        return copySync(in, file, charset, count);
    }

    //----------------------------------------------------------------------------------------------
    static public DataInputStream data(InputStream in) {
        DataInputStream result;
        boolean exceptionInProgress = true;
        try {
            result = new DataInputStream(in);
            exceptionInProgress = false;
        }
        finally {
            closeOnException(in, exceptionInProgress);
        }
        return result;
    }

    static public DataOutputStream data(OutputStream out) {
        DataOutputStream result;
        boolean exceptionInProgress = true;
        try {
            result = new DataOutputStream(out);
            exceptionInProgress = false;
        }
        finally {
            closeOnException(out, exceptionInProgress);
        }
        return result;
    }

    /**
     * Decode bytes into a string with the semantics of
     * {@link Charset#decode(ByteBuffer)}.
     */
    static public String decode(Charset cs, byte[] bb) {
        return cs.decode(ByteBuffer.wrap(bb)).toString();
    }

    /**
     * Decode bytes into a string with the semantics of
     * {@link Charset#decode(ByteBuffer)}.
     */
    static public String decode(Charset cs, byte[] b, int offset, int length) {
        ByteBuffer bb = ByteBuffer.wrap(b);
        bb.position(offset);
        bb.limit(offset + length);
        return cs.decode(bb).toString();
    }

    static public void delete(Collection<File> files)
    throws IOException {
        delete((Iterable<File>)files);
    }

    static public void delete(File file)
    throws IOException {
        if (file.isDirectory()) {
            for (File f : listFiles(file)) {
                delete(f);
            }
        }
        deleteNonRecursive(file);
    }

    static public void delete(Iterable<File> files)
    throws IOException {
        for (File file : files) {
            delete(file);
        }
    }

    static public void delete(List<File> files)
    throws IOException {
        delete((Collection<File>)files);
    }

    static public void deleteNonRecursive(Collection<File> files)
    throws IOException {
        deleteNonRecursive((Iterable<File>)files);
    }

    static public void deleteNonRecursive(File file)
    throws IOException {
        if (!file.delete() && file.exists()) {
            throw new IOException("Unable to delete " + file.getAbsolutePath());
        }
    }

    static public void deleteNonRecursive(Iterable<File> files)
    throws IOException {
        for (File file : files) {
            deleteNonRecursive(file);
        }
    }

    //----------------------------------------------------------------------------------------------
    static public Object deserialize(byte[] array)
    throws IOException {
        Object result;
        ByteArrayInputStream ain = new ByteArrayInputStream(array);
        ObjectInputStream in = new ObjectInputStream(ain);
        try {
            result = in.readObject();
        }
        catch (ClassNotFoundException e) {
            throw (IOException) new IOException(e.getMessage()).initCause(e);
        }
        return result;
    }

    static public Object deserializeBase64(String s) throws IOException {
        Object retval = null;
        byte[] bytes = Base64.decodeBase64(s);
        try {
            if (null != bytes && bytes.length != 0) {
                retval = IO.deserialize(bytes);
            }
        } catch (IOException e) {
            if (!(e instanceof ObjectStreamException)) {
                throw e;
            }
        }

        return retval;
    }

    static public void digest(File file, MessageDigest digester)
    throws IOException {
        boolean exceptionInProgress = true;
        InputStream in = digest(new FileInputStream(file), digester);
        try {
            copy(in, NULL);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(in, exceptionInProgress);
        }
    }

    static public DigestInputStream digest(InputStream in, MessageDigest digester) {
        DigestInputStream result;
        boolean exceptionInProgress = true;
        try {
            result = new DigestInputStream(in, digester);
            exceptionInProgress = false;
        }
        finally {
            closeOnException(in, exceptionInProgress);
        }
        return result;
    }

    static public DigestOutputStream digest(OutputStream out, MessageDigest digester) {
        DigestOutputStream result;
        boolean exceptionInProgress = true;
        try {
            result = new DigestOutputStream(out, digester);
            exceptionInProgress = false;
        }
        finally {
            closeOnException(out, exceptionInProgress);
        }
        return result;
    }

    static public void digestNow(InputStream in, MessageDigest digester)
    throws IOException {
        boolean exceptionInProgress = true;
        DigestInputStream din = digest(in, digester);
        try {
            copy(din, NULL);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(din, exceptionInProgress);
        }
    }

    /**
     * Discards stream bytes.
     */
    static public void discard(InputStream in)
    throws IOException {
        IO.copy(in, NULL);
    }

    /**
     * Discards up to count bytes and returns <code>true</code> if end-of-stream
     * reached.
     */
    static public boolean discard(InputStream in, long count)
    throws IOException {
        return IO.copy(in, NULL, count) != 0;
    }

    /**
     * Encode a string into bytes with the semantics of
     * {@link Charset#encode(String)}.
     */
    static public byte[] encode(Charset cs, String s) {
        byte[] result;
        ByteBuffer bb = cs.encode(s);
        result = new byte[bb.limit()];
        bb.get(result);
        return result;
    }

    /**
     * Returns a {@link Charset} for Latin-1 (ISO-8859-1).
     */
    static public Charset latin1() {
        return Latin1Initializer.LATIN1;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Returns a stream that allows reading up to limit bytes from another stream.
     */
    static public InputStream limited(InputStream in, long limit) {
        Check.nonNegative(limit, "limit");
        Check.nonNull(in, "in");
        return new LimitedInputStream(limit, in);
    }

    /**
     * @deprecated This method is poorly named - use {@link #listFilesRecursive(File)}.
     */
    @Deprecated
    static public List<File> list(File file) {
        return listFilesRecursive(file);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * @deprecated This method is poorly named - use {@link #listFilesRecursive(File, List)}.
     */
    @Deprecated
    static public void list(File file, List<File> result) {
        listFilesRecursive(file, result);
    }

    //----------------------------------------------------------------------------------------------
    static public List<File> listFiles(File directory) {
        List<File> result = Collections.emptyList();
        File[] files = directory.listFiles();
        if (files != null) {
            result = Collections.unmodifiableList(Arrays.asList(files));
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public List<File> listFilesEx(File directory)
    throws IOException {
        File[] files = directory.listFiles();
        if (files == null) {
            if (directory.isDirectory()) {
                throw new IOException("Access denied: " + directory.getAbsolutePath());
            }
            else {
                throw new IOException("Not a directory: " + directory.getAbsolutePath());
            }
        }
        return Collections.unmodifiableList(Arrays.asList(files));
    }

    //----------------------------------------------------------------------------------------------
    static public List<File> listFilesRecursive(File file) {
        List<File> result = new ArrayList<File>();
        listFilesRecursive(file, result);
        return Collections.unmodifiableList(result);
    }

    static public void listFilesRecursive(File file, List<File> result) {
        if (file.isFile()) {
            result.add(file);
        }
        else {
            for (File f : listFiles(file)) {
                listFilesRecursive(f, result);
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    static public MessageDigest md2Digester() {
        MessageDigest result;
        try {
            result = MessageDigest.getInstance("MD2");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Standard algorithm MD2 unavailable", e);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public MessageDigest md5Digester() {
        MessageDigest result;
        try {
            result = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Standard algorithm MD5 unavailable", e);
        }
        return result;
    }

    /**
     * Merge the files/structure of the source directory into a destination directory.
     * Contents from the source directory will be moved and overwrite files in the destination.
     *
     * @param source the source directory, this path will not exist after this method completes
     * @param destination the destination directory to combine this into (can be a new path)
     * @throws IOException if any file copy/move/create operation fails
     */
    static public void mergeDirectories(File source, File destination)
    throws IOException {
        Check.nonNull(source, "source path");
        Check.nonNull(destination, "destination path");
        if (!source.exists()) {
            throw new IllegalArgumentException("Source path for moving '"+source.getPath()+"' does not exist.");
        }

        if (source.isDirectory() && destination.isDirectory()) {
            // merge contents of two directories
            for (File src2 : listFiles(source)) {
                File dst2 = new File(destination, src2.getName());
                mergeDirectories(src2, dst2);
            }

            // all child files/directories have been successfully moved/merged, delete the source if present
            deleteNonRecursive(source);
        }
        else {
            // normal move/overwrite behavior is appropriate
            move(source, destination);
        }
    }

    //----------------------------------------------------------------------------------------------
    /**
     * @return <code>true</code> if the directory was created, and
     * <code>false</code> if the directory already existed.
     *
     * @throws IOException if the directory did not exist and could not be created
     */
    static public boolean mkdirs(File dir)
    throws IOException {
        return mkdirs(dir, null);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * @return <code>true</code> if the directory was created, and
     * <code>false</code> if the directory already existed.
     */
    static public boolean mkdirs(File dir, List<File> newFiles)
    throws IOException {
        boolean created;
        if (dir.exists()) {
            created = false;
        }
        else if (mkdir0(dir)) {
            created = true;
            if (newFiles != null) {
                newFiles.add(dir);
            }
        }
        else {
            File canonFile = dir.getCanonicalFile();
            File parent = canonFile.getParentFile();

            if (parent != null) {
                created = mkdirs0(parent, dir, newFiles);
                if (created) {
                    created = canonFile.mkdir();
                    if (created && newFiles != null) {
                        newFiles.add(dir);
                    }
                }
            }
            else {
                created = false;
            }
        }
        checkForDirectory(created, dir, dir);
        return created;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Replaces the destination file with the source file. If the destination
     * file exists, it is overwritten.
     */
    static public void move(File source, File destination)
    throws IOException {
        //
        // Unix can rename over existing files on the same volume. Let's assume
        // that since it's fastest.
        //
        boolean renamed = source.renameTo(destination);
        if (!renamed && destination.exists()) {
            //
            // Maybe we are on Windows, which won't rename onto an existing
            // file. Try renaming the destination first. We don't want to
            // delete the destination immediately so we can restore it if
            // necessary.
            //
            File tempDestination = new File(destination.getParentFile(), UUID.randomUUID().toString());
            renamed = destination.renameTo(tempDestination);
            if (renamed) {
                renamed = source.renameTo(destination);
                if (renamed) {
                    //
                    // The move succeeded, so now it's safe to delete the old
                    // destination.
                    //
                    try {
                        delete(tempDestination);
                    }
                    catch (IOException e) {
                        String message = "Failed to remove temporary destination file " + tempDestination +
                                         " after move";
                        if (e.getMessage() != null) {
                            message = message + ": " + e.getMessage();
                        }
                        throw (IOException)new IOException(message).initCause(e);
                    }
                }
                else {
                    //
                    // The moved failed, so restore the destination. (We'll try a copy next).
                    //
                    if (!tempDestination.renameTo(destination)) {
                        throw new IOException("Failed to restore " + tempDestination + " to " + destination);
                    }
                }
            }
        }
        if (!renamed) {
            //
            // We are probably on different volumes. Do a copy. It might
            // also be that source doesn't exist; a copy will catch that
            // as well.
            //
            try {
                copySync(source, destination);
            }
            catch (FileNotFoundException e) {
                //
                // Don't need to improve the message for this.
                //
                throw e;
            }
            catch (IOException e) {
                String message = "Failed during copy of " + source + " to " + destination;
                if (e.getMessage() != null) {
                    message = message + ": " + e.getMessage();
                }
                throw (IOException)new IOException(message).initCause(e);
            }

            try {
                delete(source);
            }
            catch (IOException e) {
                String message = "Unable to remove source file " + source;
                if (e.getMessage() != null) {
                    message = message + ": " + e.getMessage();
                }
                throw (IOException)new IOException(message).initCause(e);
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    static public OutputStream multiplex(OutputStream first, OutputStream... rest) {
        MultiplexOutputStream result;
        boolean exceptionInProgress = true;
        try {
            result = new MultiplexOutputStream(first, rest);
            exceptionInProgress = false;
        }
        finally {
            closeOnException(first, exceptionInProgress);
            if (exceptionInProgress && rest != null) {
                for (OutputStream stream : rest) {
                    closeNoThrow(stream);
                }
            }
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public OutputStream noClose(OutputStream out) {
        NoCloseOutputStream result;
        boolean exceptionInProgress = true;
        try {
            result = new NoCloseOutputStream(out);
            exceptionInProgress = false;
        }
        finally {
            closeOnException(out, exceptionInProgress);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public PrintStream noClose(PrintStream out) {
        NoClosePrintStream result;
        boolean exceptionInProgress = true;
        try {
            result = new NoClosePrintStream(out);
            exceptionInProgress = false;
        }
        finally {
            closeOnException(out, exceptionInProgress);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public PrintWriter noClose(PrintWriter out) {
        NoClosePrintWriter result;
        boolean exceptionInProgress = true;
        try {
            result = new NoClosePrintWriter(out);
            exceptionInProgress = false;
        }
        finally {
            closeOnException(out, exceptionInProgress);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public Writer noClose(Writer out) {
        Writer result;
        boolean exceptionInProgress = true;
        try {
            result = new NoCloseWriter(out);
            exceptionInProgress = false;
        }
        finally {
            closeOnException(out, exceptionInProgress);
        }
        return result;
    }

    //----------------------------------------------------------------------
    static public LineNumberReader number(Reader in) {
        LineNumberReader result;
        boolean exceptionInProgress = true;
        try {
            result = new LineNumberReader(in);
            exceptionInProgress = false;
        }
        finally {
            closeOnException(in, exceptionInProgress);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedOutputStream openAppend(File file)
    throws FileNotFoundException {
        return buffer(new FileOutputStream(file, true));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedOutputStream openAppend(String path)
    throws FileNotFoundException {
        return openAppend(new File(path));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedOutputStream openAppendSync(File file)
    throws FileNotFoundException {
        return buffer(new SyncFileOutputStream(file, true));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedOutputStream openAppendSync(String path)
    throws FileNotFoundException {
        return openAppendSync(new File(path));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openAppendText(File file)
    throws FileNotFoundException {
        return buffer(writer(new FileOutputStream(file, true)));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openAppendText(File file, Charset charset)
    throws FileNotFoundException {
        return buffer(writer(new FileOutputStream(file, true), charset));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openAppendText(File file, CharsetEncoder encoder)
    throws FileNotFoundException {
        return buffer(writer(new FileOutputStream(file, true), encoder));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openAppendText(File file, String charsetName)
    throws FileNotFoundException {
        return buffer(writer(new FileOutputStream(file, true), charsetName));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openAppendText(String path)
    throws FileNotFoundException {
        return openAppendText(new File(path));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openAppendText(String path, Charset charset)
    throws FileNotFoundException {
        return openAppendText(new File(path), charset);
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openAppendText(String path, CharsetEncoder encoder)
    throws FileNotFoundException {
        return openAppendText(new File(path), encoder);
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openAppendText(String path, String charsetName)
    throws FileNotFoundException {
        return openAppendText(new File(path), charsetName);
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openAppendTextSync(File file)
    throws FileNotFoundException {
        return buffer(writer(new SyncFileOutputStream(file, true)));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openAppendTextSync(File file, Charset charset)
    throws FileNotFoundException {
        return buffer(writer(new SyncFileOutputStream(file, true), charset));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openAppendTextSync(File file, CharsetEncoder encoder)
    throws FileNotFoundException {
        return buffer(writer(new SyncFileOutputStream(file, true), encoder));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openAppendTextSync(File file, String charsetName)
    throws FileNotFoundException {
        return buffer(writer(new SyncFileOutputStream(file, true), charsetName));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openAppendTextSync(String path)
    throws FileNotFoundException {
        return openAppendTextSync(new File(path));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openAppendTextSync(String path, Charset charset)
    throws FileNotFoundException {
        return openAppendTextSync(new File(path), charset);
    }
    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openAppendTextSync(String path, CharsetEncoder encoder)
    throws FileNotFoundException {
        return openAppendTextSync(new File(path), encoder);
    }
    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openAppendTextSync(String path, String charsetName)
    throws FileNotFoundException {
        return openAppendTextSync(new File(path), charsetName);
    }
    //----------------------------------------------------------------------------------------------
    static public BufferedInputStream openInput(File file)
    throws FileNotFoundException {
        return buffer(new FileInputStream(file));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedInputStream openInput(String path)
    throws FileNotFoundException {
        return openInput(new File(path));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedReader openInputText(File file)
    throws FileNotFoundException {
        return buffer(reader(file));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedReader openInputText(File file, Charset charset)
    throws FileNotFoundException {
        return buffer(reader(file, charset));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedReader openInputText(File file, CharsetDecoder decoder)
    throws FileNotFoundException {
        return buffer(reader(file, decoder));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedReader openInputText(File file, String charsetName)
    throws FileNotFoundException {
        return buffer(reader(file, charsetName));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedReader openInputText(String fileName)
    throws FileNotFoundException {
        return buffer(reader(new File(fileName)));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedReader openInputText(String fileName, Charset charset)
    throws FileNotFoundException {
        return buffer(reader(new File(fileName), charset));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedReader openInputText(String fileName, CharsetDecoder decoder)
    throws FileNotFoundException {
        return buffer(reader(new File(fileName), decoder));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedReader openInputText(String fileName, String charsetName)
    throws FileNotFoundException {
        return buffer(reader(new File(fileName), charsetName));
    }

    //----------------------------------------------------------------------------------------------
    static public ZipInputStream openInputZip(File file)
    throws FileNotFoundException {
        return zip(new FileInputStream(file));
    }

    //----------------------------------------------------------------------------------------------
    static public ZipInputStream openInputZip(String path)
    throws FileNotFoundException {
        return openInputZip(new File(path));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedOutputStream openOutput(File file)
    throws FileNotFoundException {
        return buffer(new FileOutputStream(file));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedOutputStream openOutput(String path)
    throws FileNotFoundException {
        return openOutput(new File(path));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedOutputStream openOutputSync(File file)
    throws FileNotFoundException {
        return buffer(new SyncFileOutputStream(file));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedOutputStream openOutputSync(String path)
    throws FileNotFoundException {
        return openOutputSync(new File(path));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openOutputText(File file)
    throws FileNotFoundException {
        return buffer(writer(file));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openOutputText(File file, Charset charset)
    throws FileNotFoundException {
        return buffer(writer(file, charset));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openOutputText(File file, CharsetEncoder encoder)
    throws FileNotFoundException {
        return buffer(writer(file, encoder));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openOutputText(File file, String charsetName)
    throws FileNotFoundException {
        return buffer(writer(file, charsetName));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openOutputText(String path)
    throws FileNotFoundException {
        return openOutputText(new File(path));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openOutputText(String path, Charset charset)
    throws FileNotFoundException {
        return openOutputText(new File(path), charset);
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openOutputText(String path, CharsetEncoder encoder)
    throws FileNotFoundException {
        return openOutputText(new File(path), encoder);
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openOutputText(String path, String charsetName)
    throws FileNotFoundException {
        return openOutputText(new File(path), charsetName);
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openOutputTextSync(File file)
    throws FileNotFoundException {
        return buffer(writer(new SyncFileOutputStream(file)));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openOutputTextSync(File file, Charset charset)
    throws FileNotFoundException {
        return buffer(writer(new SyncFileOutputStream(file), charset));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openOutputTextSync(File file, CharsetEncoder encoder)
    throws FileNotFoundException {
        return buffer(writer(new SyncFileOutputStream(file), encoder));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openOutputTextSync(File file, String charsetName)
    throws FileNotFoundException {
        return buffer(writer(new SyncFileOutputStream(file), charsetName));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openOutputTextSync(String path)
    throws FileNotFoundException {
        return openOutputTextSync(new File(path));
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openOutputTextSync(String path, Charset charset)
    throws FileNotFoundException {
        return openOutputTextSync(new File(path), charset);
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openOutputTextSync(String path, CharsetEncoder encoder)
    throws FileNotFoundException {
        return openOutputTextSync(new File(path), encoder);
    }

    //----------------------------------------------------------------------------------------------
    static public BufferedWriter openOutputTextSync(String path, String charsetName)
    throws FileNotFoundException {
        return openOutputTextSync(new File(path), charsetName);
    }

    //----------------------------------------------------------------------------------------------
    static public ZipOutputStream openOutputZip(File file)
    throws FileNotFoundException {
        return zip(new FileOutputStream(file));
    }

    //----------------------------------------------------------------------------------------------
    static public ZipOutputStream openOutputZip(String path)
    throws FileNotFoundException {
        return openOutputZip(new File(path));
    }

    //----------------------------------------------------------------------------------------------
    static public PrintStream printer(OutputStream out) {
        PrintStream result;
        boolean exceptionInProgress = true;
        try {
            result = new PrintStream(out);
            exceptionInProgress = false;
        }
        finally {
            closeOnException(out, exceptionInProgress);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public PrintStream printer(OutputStream out, boolean autoflush) {
        PrintStream result;
        boolean exceptionInProgress = true;
        try {
            result = new PrintStream(out, autoflush);
            exceptionInProgress = false;
        }
        finally {
            closeOnException(out, exceptionInProgress);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public PrintWriter printer(Writer out) {
        PrintWriter result;
        boolean exceptionInProgress = true;
        try {
            result = new PrintWriter(out);
            exceptionInProgress = false;
        }
        finally {
            closeOnException(out, exceptionInProgress);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public PrintWriter printer(Writer out, boolean autoflush) {
        PrintWriter result;
        boolean exceptionInProgress = true;
        try {
            result = new PrintWriter(out, autoflush);
            exceptionInProgress = false;
        }
        finally {
            closeOnException(out, exceptionInProgress);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Returns the present working directory (PWD). The present working
     * directory never changes.
     *
     * @return a {@link File} instance representing the present working
     *         directory. Non-<code>null</code>.
     */
    static public File pwd() {
        return PwdInitializer.pwd;
    }

    //----------------------------------------------------------------------------------------------
    static public byte[] read(InputStream in)
    throws IOException {
        boolean exceptionInProgress = true;
        ByteArrayOutputStream out;
        try {
            out = new ByteArrayOutputStream();
            copy(in, out);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(in, exceptionInProgress);
        }
        return out.toByteArray();
    }

    //----------------------------------------------------------------------------------------------
    static public char readChar(InputStream in)
    throws IOException {
        return readChar(in, ByteOrder.BIG_ENDIAN);
    }

    //----------------------------------------------------------------------------------------------
    static public char readChar(InputStream in, ByteOrder order)
    throws IOException {
        return (char) readShort(in, order);
    }

    //----------------------------------------------------------------------------------------------
    static public char[] readChars(Reader in)
    throws IOException {
        boolean exceptionInProgress = true;
        CharArrayWriter out;
        try {
            out = new CharArrayWriter();
            copy(in, out);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(in, exceptionInProgress);
        }
        return out.toCharArray();
    }

    //----------------------------------------------------------------------------------------------
    static public char[] readCharsFile(File file)
    throws IOException {
        boolean exceptionInProgress = true;
        Reader in = reader(file);
        CharArrayWriter out;
        try {
            out = new CharArrayWriter();
            copy(in, out);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(in, exceptionInProgress);
        }
        return out.toCharArray();
    }

    //----------------------------------------------------------------------------------------------
    static public char[] readCharsFile(File file, Charset charset)
    throws IOException {
        CharsetDecoder decoder = charset.newDecoder();
        return readCharsFile(file, decoder);
    }

    //----------------------------------------------------------------------------------------------
    static public char[] readCharsFile(File file, CharsetDecoder decoder)
    throws IOException {
        boolean exceptionInProgress = true;
        Reader in = reader(file, decoder);
        CharArrayWriter out;
        try {
            out = new CharArrayWriter();
            copy(in, out);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(in, exceptionInProgress);
        }
        return out.toCharArray();
    }

    //----------------------------------------------------------------------------------------------
    static public double readDouble(InputStream in)
    throws IOException {
        return readDouble(in, ByteOrder.BIG_ENDIAN);
    }

    //----------------------------------------------------------------------------------------------
    static public double readDouble(InputStream in, ByteOrder order)
    throws IOException {
        long d = readLong(in, order);
        return Double.longBitsToDouble(d);
    }

    //----------------------------------------------------------------------------------------------
    static public Reader reader(char[] array) {
        CharBuffer buffer = CharBuffer.wrap(array);
        return reader(buffer);
    }

    //----------------------------------------------------------------------------------------------
    static public Reader reader(CharSequence sequence) {
        return new CharSequenceReader(sequence);
    }

    //----------------------------------------------------------------------------------------------
    static public InputStreamReader reader(File file)
    throws FileNotFoundException {
        return reader(new FileInputStream(file));
    }

    //----------------------------------------------------------------------------------------------
    static public InputStreamReader reader(File file, Charset charset)
    throws FileNotFoundException {
        return reader(new FileInputStream(file), charset);
    }

    //----------------------------------------------------------------------------------------------
    static public InputStreamReader reader(File file, CharsetDecoder decoder)
    throws FileNotFoundException {
        return reader(new FileInputStream(file), decoder);
    }

    //----------------------------------------------------------------------------------------------
    static public InputStreamReader reader(File file, String charsetName)
    throws FileNotFoundException {
        return reader(new FileInputStream(file), charsetName);
    }

    //----------------------------------------------------------------------------------------------
    static public InputStreamReader reader(InputStream in) {
        InputStreamReader result;
        boolean exceptionInProgress = true;
        try {
            result = new InputStreamReader(in);
            exceptionInProgress = false;
        }
        finally {
            closeOnException(in, exceptionInProgress);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public InputStreamReader reader(InputStream in, Charset charset) {
        InputStreamReader result;
        if (charset == null) {
            result = reader(in);
        }
        else {
            CharsetDecoder decoder;
            boolean exceptionInProgress = true;
            try {
                decoder = charset.newDecoder();
                exceptionInProgress = false;
            }
            finally {
                closeOnException(in, exceptionInProgress);
            }
            result = reader(in, decoder);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public InputStreamReader reader(InputStream in, CharsetDecoder decoder) {
        InputStreamReader result;
        if (decoder == null) {
            result = reader(in);
        }
        else {
            boolean exceptionInProgress = true;
            try {
                result = new InputStreamReader(in, decoder);
                exceptionInProgress = false;
            }
            finally {
                closeOnException(in, exceptionInProgress);
            }
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public InputStreamReader reader(InputStream in, String charsetName) {
        InputStreamReader result;
        if (charsetName == null) {
            result = reader(in);
        }
        else {
            boolean exceptionInProgress = true;
            try {
                result = new InputStreamReader(in, Charset.forName(charsetName));
                exceptionInProgress = false;
            }
            finally {
                closeOnException(in, exceptionInProgress);
            }
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public StringReader reader(String string) {
        return new StringReader(string);
    }

    //----------------------------------------------------------------------------------------------
    static public byte[] readFile(File file)
    throws IOException {
        boolean exceptionInProgress = true;
        ByteArrayOutputStream out;
        InputStream in = new FileInputStream(file);
        try {
            out = new ByteArrayOutputStream();
            copy(in, out);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(in, exceptionInProgress);
        }
        return out.toByteArray();
    }

    //----------------------------------------------------------------------------------------------
    static public float readFloat(InputStream in)
    throws IOException {
        return readFloat(in, ByteOrder.BIG_ENDIAN);
    }

    //----------------------------------------------------------------------------------------------
    static public float readFloat(InputStream in, ByteOrder order)
    throws IOException {
        int d = readInt(in, order);
        return Float.intBitsToFloat(d);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * @return The number of bytes that are unread because of EOF.
     */
    static public int readFully(InputStream in, byte[] buffer)
    throws IOException {
        return readFully(in, buffer, 0, buffer.length);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * @return The number of bytes that are unread because of EOF.
     */
    static public int readFully(InputStream in, byte[] buffer, int offset, int length)
    throws IOException {
        int read = 0;
        while (length > 0 && (read = in.read(buffer, offset, length)) != -1) {
            offset += read;
            length -= read;
        }
        return length;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * @return The number of chars that are unread because of EOF.
     */
    static public int readFully(Reader in, char[] buffer)
    throws IOException {
        return readFully(in, buffer, 0, buffer.length);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * @return The number of chars that are unread because of EOF.
     */
    static public int readFully(Reader in, char[] buffer, int offset, int length)
    throws IOException {
        int read = 0;
        while (length > 0 && (read = in.read(buffer, offset, length)) != -1) {
            offset += read;
            length -= read;
        }
        return length;
    }

    //----------------------------------------------------------------------------------------------
    static public int readInt(InputStream in)
    throws IOException {
        return readInt(in, ByteOrder.BIG_ENDIAN);
    }

    //----------------------------------------------------------------------------------------------
    static public int readInt(InputStream in, ByteOrder order)
    throws IOException {
        byte[] buffer = new byte[4];
        if (readFully(in, buffer) != 0) {
            throw new EOFException();
        }
        ByteBuffer bb = ByteBuffer.wrap(buffer);
        bb.order(order);
        return bb.getInt();
    }

    //----------------------------------------------------------------------------------------------
    static public long readLong(InputStream in)
    throws IOException {
        return readLong(in, ByteOrder.BIG_ENDIAN);
    }


    //----------------------------------------------------------------------------------------------
    static public long readLong(InputStream in, ByteOrder order)
    throws IOException {
        byte[] buffer = new byte[8];
        if (readFully(in, buffer) != 0) {
            throw new EOFException();
        }
        ByteBuffer bb = ByteBuffer.wrap(buffer);
        bb.order(order);
        return bb.getLong();
    }

    //----------------------------------------------------------------------------------------------
    static public short readShort(InputStream in)
    throws IOException {
        return readShort(in, ByteOrder.BIG_ENDIAN);
    }

    //----------------------------------------------------------------------------------------------
    static public short readShort(InputStream in, ByteOrder order)
    throws IOException {
        byte[] buffer = new byte[2];
        if (readFully(in, buffer) != 0) {
            throw new EOFException();
        }
        ByteBuffer bb = ByteBuffer.wrap(buffer);
        bb.order(order);
        return bb.getShort();
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Read all the text available from the InputStream to a String and close the InputStream.
     */
    static public String readText(InputStream in)
    throws IOException {
        return readText(reader(in));
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Read all the text available from the InputStream to a String and close the InputStream.
     */
    static public String readText(InputStream in, Charset charset)
    throws IOException {
        return readText(reader(in, charset));
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Read all the text available from the InputStream to a String and close the InputStream.
     */
    static public String readText(InputStream in, CharsetDecoder decoder)
    throws IOException {
        return readText(reader(in, decoder));
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Read all the text available from the InputStream to a String and close the InputStream.
     */
    static public String readText(InputStream in, String charsetName)
    throws IOException {
        return readText(reader(in, charsetName));
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Read all the text available from the Reader to a String and close the Reader.
     */
    static public String readText(Reader in)
    throws IOException {
        boolean exceptionInProgress = true;
        StringBuilder builder = new StringBuilder();
        try {
            copy(in, builder);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(in, exceptionInProgress);
        }
        return builder.toString();
    }

    //----------------------------------------------------------------------------------------------
    static public String readTextFile(File file)
    throws IOException {
        boolean exceptionInProgress = true;
        Writer out;
        Reader in = reader(file);
        try {
            out = new StringWriter();
            copy(in, out);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(in, exceptionInProgress);
        }
        return out.toString();
    }

    //----------------------------------------------------------------------------------------------
    static public String readTextFile(File file, Charset charset)
    throws IOException {
        CharsetDecoder decoder = charset.newDecoder();
        return readTextFile(file, decoder);
    }

    //----------------------------------------------------------------------------------------------
    static public String readTextFile(File file, CharsetDecoder decoder)
    throws IOException {
        boolean exceptionInProgress = true;
        Writer out;
        Reader in = reader(file, decoder);
        try {
            out = new StringWriter();
            copy(in, out);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(in, exceptionInProgress);
        }
        return out.toString();
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Returns an immutable list of file system roots. The returned list will
     * never change, but the resulting value of this method may change over time
     * as file systems are mounted and unmounted. If the file system roots
     * cannot be determined, the list is empty. The list will never be
     * <code>null</code>.
     *
     * @return a list of file system roots. Non-<code>null</code>.
     */
    static public List<File> roots() {
        File[] roots = File.listRoots();
        if (roots == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(Arrays.asList(roots));
    }

    //----------------------------------------------------------------------------------------------
    static public byte[] serialize(Object o)
    throws IOException {
        ByteArrayOutputStream aout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(aout);
        out.writeObject(o);
        out.close();
        return aout.toByteArray();
    }

    //----------------------------------------------------------------------------------------------
    static public MessageDigest sha1Digester() {
        MessageDigest result;
        try {
            result = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Standard algorithm SHA-1 unavailable", e);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public MessageDigest sha256Digester() {
        MessageDigest result;
        try {
            result = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Standard algorithm SHA-256 unavailable", e);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public MessageDigest sha384Digester() {
        MessageDigest result;
        try {
            result = MessageDigest.getInstance("SHA-384");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Standard algorithm SHA-384 unavailable", e);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public MessageDigest sha512Digester() {
        MessageDigest result;
        try {
            result = MessageDigest.getInstance("SHA-512");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Standard algorithm SHA-512 unavailable", e);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * @return The number of bytes that are unskipped because of EOF.
     */
    static public long skipFully(InputStream in, long count)
    throws IOException {
        return copy(in, NULL, count);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * @return The number of chars that are unskipped because of EOF.
     */
    static public long skipFully(Reader in, long count)
    throws IOException {
        return copy(in, CNULL, count);
    }

    //----------------------------------------------------------------------------------------------
    static public void unzip(File src, File dst)
    throws IOException {
        unzipInternal(src, dst, false);
    }

    //----------------------------------------------------------------------------------------------
    static public void unzipSync(File src, File dst)
    throws IOException {
        unzipInternal(src, dst, true);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Returns a {@link Charset} for UTF-16. See <code>Charset</code> for
     * byte-order semantics.
     */
    static public Charset utf16() {
        return Utf16Initializer.UTF16;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Returns a {@link Charset} for UTF-16BE (big-endian byte order).
     */
    static public Charset utf16be() {
        return Utf16beInitializer.UTF16BE;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Returns a {@link Charset} for UTF-16LE (little-endian byte order).
     */
    static public Charset utf16le() {
        return Utf16leInitializer.UTF16LE;
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Returns a {@link Charset} for UTF-8.
     */
    static public Charset utf8() {
        return Utf8Initializer.UTF8;
    }

    //----------------------------------------------------------------------------------------------
    static public void writeChar(OutputStream out, ByteOrder order, char value)
    throws IOException {
        writeShort(out, order, (short) value);
    }

    //----------------------------------------------------------------------------------------------
    static public void writeChar(OutputStream out, char value)
    throws IOException {
        writeChar(out, ByteOrder.BIG_ENDIAN, value);
    }

    //----------------------------------------------------------------------------------------------
    static public void writeDouble(OutputStream out, ByteOrder order, double value)
    throws IOException {
        writeLong(out, order, Double.doubleToLongBits(value));
    }

    //----------------------------------------------------------------------------------------------
    static public void writeDouble(OutputStream out, double value)
    throws IOException {
        writeDouble(out, ByteOrder.BIG_ENDIAN, value);
    }

    //----------------------------------------------------------------------------------------------
    static public void writeFile(File file, byte[] data)
    throws IOException {
        boolean exceptionInProgress = true;
        OutputStream out = new FileOutputStream(file);
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            IO.copy(in, out);
            exceptionInProgress = false;
        }
        finally {
            IO.closeSafely(out, exceptionInProgress);
        }
    }

    //----------------------------------------------------------------------------------------------
    static public void writeFloat(OutputStream out, ByteOrder order, float value)
    throws IOException {
        writeInt(out, order, Float.floatToIntBits(value));
    }

    //----------------------------------------------------------------------------------------------
    static public void writeFloat(OutputStream out, float value)
    throws IOException {
        writeFloat(out, ByteOrder.BIG_ENDIAN, value);
    }

    //----------------------------------------------------------------------------------------------
    static public void writeInt(OutputStream out, ByteOrder order, int value)
    throws IOException {
        byte[] buffer = new byte[4];
        ByteBuffer bb = ByteBuffer.wrap(buffer);
        bb.order(order);
        bb.putInt(value);
        out.write(buffer);
    }

    //----------------------------------------------------------------------------------------------
    static public void writeInt(OutputStream out, int value)
    throws IOException {
        writeInt(out, ByteOrder.BIG_ENDIAN, value);
    }

    //----------------------------------------------------------------------------------------------
    static public void writeLong(OutputStream out, ByteOrder order, long value)
    throws IOException {
        byte[] buffer = new byte[8];
        ByteBuffer bb = ByteBuffer.wrap(buffer);
        bb.order(order);
        bb.putLong(value);
        out.write(buffer);
    }

    //----------------------------------------------------------------------------------------------
    static public void writeLong(OutputStream out, long value)
    throws IOException {
        writeLong(out, ByteOrder.BIG_ENDIAN, value);
    }

    //----------------------------------------------------------------------------------------------
    static public OutputStreamWriter writer(File file)
    throws FileNotFoundException {
        return writer(new FileOutputStream(file));
    }

    //----------------------------------------------------------------------------------------------
    static public OutputStreamWriter writer(File file, Charset charset)
    throws FileNotFoundException {
        return writer(new FileOutputStream(file), charset);
    }

    //----------------------------------------------------------------------------------------------
    static public OutputStreamWriter writer(File file, CharsetEncoder encoder)
    throws FileNotFoundException {
        return writer(new FileOutputStream(file), encoder);
    }

    //----------------------------------------------------------------------------------------------
    static public OutputStreamWriter writer(File file, String charsetName)
    throws FileNotFoundException {
        return writer(new FileOutputStream(file), charsetName);
    }

    //----------------------------------------------------------------------------------------------
    static public OutputStreamWriter writer(OutputStream out) {
        OutputStreamWriter result;
        boolean exceptionInProgress = true;
        try {
            result = new OutputStreamWriter(out);
            exceptionInProgress = false;
        }
        finally {
            closeOnException(out, exceptionInProgress);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public OutputStreamWriter writer(OutputStream out, Charset charset) {
        OutputStreamWriter result;
        if (charset == null) {
            result = writer(out);
        }
        else {
            CharsetEncoder encoder;
            boolean exceptionInProgress = true;
            try {
                encoder = charset.newEncoder();
                exceptionInProgress = false;
            }
            finally {
                closeOnException(out, exceptionInProgress);
            }
            result = writer(out, encoder);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public OutputStreamWriter writer(OutputStream out, CharsetEncoder encoder) {
        OutputStreamWriter result;
        if (encoder == null) {
            result = writer(out);
        }
        else {
            boolean exceptionInProgress = true;
            try {
                result = new OutputStreamWriter(out, encoder);
                exceptionInProgress = false;
            }
            finally {
                closeOnException(out, exceptionInProgress);
            }
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public OutputStreamWriter writer(OutputStream out, String charset) {
        OutputStreamWriter result;
        if (charset == null) {
            result = writer(out);
        }
        else {
            result = writer(out, Charset.forName(charset));
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public void writeShort(OutputStream out, ByteOrder order, short value)
    throws IOException {
        byte[] buffer = new byte[2];
        ByteBuffer bb = ByteBuffer.wrap(buffer);
        bb.order(order);
        bb.putShort(value);
        out.write(buffer);
    }

    //----------------------------------------------------------------------------------------------
    static public void writeShort(OutputStream out, short value)
    throws IOException {
        writeShort(out, ByteOrder.BIG_ENDIAN, value);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Write a CharSequence to the OutputStream.
     */
    static public void writeText(OutputStream out, CharSequence sequence)
    throws IOException {
        OutputStreamWriter writer = writer(out);
        writeText(writer, sequence);
        writer.flush();
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Write a CharSequence to the OutputStream.
     */
    static public void writeText(OutputStream out, CharSequence sequence, Charset charset)
    throws IOException {
        OutputStreamWriter writer = writer(out, charset);
        writeText(writer, sequence);
        writer.flush();
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Write a CharSequence to the OutputStream.
     */
    static public void writeText(OutputStream out, CharSequence sequence, CharsetEncoder encoder)
    throws IOException {
        OutputStreamWriter writer = writer(out, encoder);
        writeText(writer, sequence);
        writer.flush();
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Write a CharSequence to the OutputStream.
     */
    static public void writeText(OutputStream out, CharSequence sequence, String charsetName)
    throws IOException {
        OutputStreamWriter writer = writer(out, charsetName);
        writeText(writer, sequence);
        writer.flush();
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Write a CharSequence to the Writer.
     */
    static public void writeText(Writer out, CharSequence sequence)
    throws IOException {
        copy(reader(sequence), out);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Write a String to the Writer and then close the Writer.
     */
    static public void writeTextAndClose(Writer out, String string)
    throws IOException {
        boolean exceptionInProgress = true;
        try {
            out.write(string);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(out, exceptionInProgress);
        }
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Write a String to a File. This follows the behavior or FileOutputStream with appending disabled.
     */
    static public void writeTextFile(File file, String string)
    throws IOException {
        writeTextAndClose(writer(file), string);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Write a String to a File. This follows the behavior or FileOutputStream with appending disabled.
     */
    static public void writeTextFile(File file, String string, Charset charset)
    throws IOException {
        writeTextAndClose(writer(file, charset), string);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Write a String to a File. This follows the behavior or FileOutputStream with appending disabled.
     */
    static public void writeTextFile(File file, String string, CharsetEncoder encoder)
    throws IOException {
        writeTextAndClose(writer(file, encoder), string);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Write a String to a File. This follows the behavior or FileOutputStream with appending disabled.
     */
    static public void writeTextFile(File file, String string, String charsetName)
    throws IOException {
        writeTextAndClose(writer(file, charsetName), string);
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Write a String to a File. This follows the behavior or FileOutputStream with appending disabled.
     */
    static public void writeTextFileSync(File file, String string)
    throws IOException {
        boolean exceptionInProgress = true;
        Writer out = writer(new SyncFileOutputStream(file));
        try {
            copy(reader(string), out);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(out, exceptionInProgress);
        }
    }

    //----------------------------------------------------------------------------------------------
    static public void writeTextFileSync(File file, String string, Charset charset)
    throws IOException {
        CharsetEncoder encoder = charset.newEncoder();
        writeTextFileSync(file, string, encoder);
    }

    //----------------------------------------------------------------------------------------------
    static public void writeTextFileSync(File file, String string, CharsetEncoder encoder)
    throws IOException {
        boolean exceptionInProgress = true;
        Writer out = writer(new SyncFileOutputStream(file), encoder);
        try {
            copy(reader(string), out);
            exceptionInProgress = false;
        }
        finally {
            closeSafely(out, exceptionInProgress);
        }
    }

    //----------------------------------------------------------------------------------------------
    static public void writeTextFileSync(File file, String string, String charsetName)
    throws IOException {
        Charset charset = Charset.forName(charsetName);
        writeTextFileSync(file, string, charset);
    }

    //----------------------------------------------------------------------------------------------
    static public ZipInputStream zip(InputStream in) {
        ZipInputStream result;
        boolean exceptionInProgress = true;
        try {
            result = new ZipInputStream(buffer(in));
            exceptionInProgress = false;
        }
        finally {
            closeOnException(in, exceptionInProgress);
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static public ZipOutputStream zip(OutputStream out) {
        ZipOutputStream result;
        boolean exceptionInProgress = true;
        try {
            result = new ZipOutputStream(buffer(out));
            exceptionInProgress = false;
        }
        finally {
            closeOnException(out, exceptionInProgress);
        }
        return result;
    }

    public static void unpackZip(ZipInputStream zis, File outputFolder) throws IOException {
        byte[] buffer = new byte[1024];
        //create output directory is not exists

        if (!outputFolder.exists()) {
            outputFolder.mkdir();
        }

        ZipEntry ze = zis.getNextEntry();
        while (ze != null) {
            String fileName = ze.getName();
            File newFile = new File(outputFolder, fileName);
            if (ze.isDirectory()) {
                newFile.mkdirs();
            } else {
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            ze = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }

    //----------------------------------------------------------------------------------------------
    static private void checkForDirectory(boolean created, File currentDir, File targetDir)
    throws IOException {
        String name = currentDir.getName();
        char c;
        if (!created && !currentDir.isDirectory()) {
            if (currentDir.isFile()) {
                if (currentDir == targetDir) {
                    throw new IOException(String.format(
                        "Unable to create directory %s: path is a file",
                        targetDir.getAbsolutePath()));
                }
                throw new IOException(String.format(
                    "Unable to create directory %s: parent %s is a file",
                    targetDir.getAbsolutePath(),
                    currentDir.getAbsolutePath()));
            }
            else if (File.separatorChar == '\\' && (c = findInvalidWindowsFilenameChar(name)) != 0) {
                if (currentDir == targetDir) {
                    throw new IOException(String.format(
                        "Unable to create directory %s: name contains character (%s) that may not valid",
                        targetDir.getAbsolutePath(),
                        c));
                }
                throw new IOException(String.format(
                    "Unable to create directory %s: name of parent %s contains character (%s) that may not valid",
                    targetDir.getAbsolutePath(),
                    c,
                    currentDir.getAbsolutePath()));
            }
            else {
                if (currentDir == targetDir) {
                    throw new IOException(String.format(
                        "Unable to create directory %s",
                        targetDir.getAbsolutePath()));
                }
                throw new IOException(String.format(
                    "Unable to create directory %s: unable to create parent %s",
                    targetDir.getAbsolutePath(),
                    currentDir.getAbsolutePath()));
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Closes a closeable without throwing an {@link IOException}. This should
     * be used directly in backward compatibility methods instead of casting to
     * {@link Closeable} and using {@link #close(Closeable)}; that approach can
     * lead to {@link ClassCastException}s or infinite loops if one is not
     * careful.
     */
    static private void closeCloseable(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            }
            catch (IOException swallow) {
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Closes a closeable without throwing an exception. This should be used
     * directly in backward compatibility methods instead of casting to
     * {@link Closeable} and using {@link #closeNoThrow(Closeable)}; that
     * approach can lead to {@link ClassCastException}s or infinite loops if one
     * is not careful.
     */
    static private void closeNoThrowCloseable(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            }
            catch (Throwable swallow) {
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    static private void closeOnException(Closeable closeable, boolean exceptionInProgress) {
        if (exceptionInProgress) {
            closeNoThrow(closeable);
        }
    }

    //----------------------------------------------------------------------------------------------
    static private char findInvalidWindowsFilenameChar(String name) {
        char result = 0;
        for (int i = 0; i < WINDOWS_INVALID_FILENAME_CHARS.length(); i++) {
            char c = WINDOWS_INVALID_FILENAME_CHARS.charAt(i);
            if (name.indexOf(c) != -1) {
                result = c;
                break;
            }
        }
        return result;
    }

    //----------------------------------------------------------------------------------------------
    static private boolean mkdir0(File dir) {
        // Strip trailing "/." path entries
        String path = dir.getPath();
        path = path.replace('/', File.separatorChar);
        path = path.replace('\\', File.separatorChar);
        dir = new File(path);
        while(dir.getName().equals(".") && dir.getParentFile() != null) {
            dir = dir.getParentFile();
        }
        return dir.mkdir();
    }

    //----------------------------------------------------------------------------------------------
    static private boolean mkdirs0(File dir, File targetDir, List<File> newFiles)
    throws IOException {
        boolean created;
        if (dir.exists()) {
            created = false;
        }
        else if (dir.mkdir()) {
            created = true;
            if (newFiles != null) {
                newFiles.add(dir);
            }
        }
        else {
            File canonFile = dir.getCanonicalFile();
            File parent = canonFile.getParentFile();

            if (parent != null) {
                created = mkdirs0(parent, targetDir, newFiles);
                if (created) {
                    created = canonFile.mkdir();
                    if (created && newFiles != null) {
                        newFiles.add(dir);
                    }
                }
            }
            else {
                created = false;
            }
        }
        checkForDirectory(created, dir, targetDir);
        return created;
    }

    //----------------------------------------------------------------------------------------------
    static private void unzipInternal(File src, File dst, boolean sync)
    throws IOException {
        boolean exceptionInProgress = true;
        ZipFile zip = new ZipFile(src);
        try {
            mkdirs(dst);
            for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
                ZipEntry entry = e.nextElement();
                File file = new File(dst, entry.getName());
                if (entry.isDirectory()) {
                    mkdirs(file);
                }
                else {
                    File parent = file.getParentFile();
                    if (parent != null) {
                        mkdirs(parent);
                    }
                    boolean exceptionInProgress2 = true;
                    InputStream in = zip.getInputStream(entry);
                    try {
                        if (sync) {
                            copySync(in, file);
                        }
                        else {
                            copy(in, file);
                        }
                        exceptionInProgress2 = false;
                    }
                    finally {
                        closeSafely(in, exceptionInProgress2);
                    }
                }
            }
            exceptionInProgress = false;
        }
        finally {
            closeSafely(zip, exceptionInProgress);
        }
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Delays initialization of the ASCII {@link Charset} until the first
     * invocation of {@link #ascii()}.
     */
    final static private class AsciiInitializer {
        final static Charset ASCII = Charset.forName("US-ASCII");
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Delays initialization of the LATIN1 {@link Charset} until the first
     * invocation of {@link #latin1()}.
     */
    final static private class Latin1Initializer {
        final static Charset LATIN1 = Charset.forName("ISO-8859-1");
    }

    //----------------------------------------------------------------------------------------------
    final static private class LimitedInputStream extends InputStream {
        final private long limit;
        final private InputStream delegate;
        private long position;
        private boolean closed;

        //------------------------------------------------------------------------------------------
        private LimitedInputStream(long limit, InputStream delegate) {
            this.limit = limit;
            this.delegate = delegate;
        }

        //------------------------------------------------------------------------------------------
        @Override
        synchronized public void close()
        throws IOException {
            if (!closed) {
                closed = true;
                if (position < limit) {
                    discard(delegate, limit - position);
                }
            }
        }

        //------------------------------------------------------------------------------------------
        @Override
        synchronized public int read()
        throws IOException {
            int result;
            if (closed) {
                throw new IOException("Stream closed");
            }
            if (position >= limit) {
                result = -1;
            }
            else {
                result = delegate.read();
                if (result != -1) {
                    position++;
                }
            }
            return result;
        }

        //------------------------------------------------------------------------------------------
        @Override
        synchronized public int read(byte[] b, int off, int len)
        throws IOException {
            int result;
            if (closed) {
                throw new IOException("Stream closed");
            }
            if (position >= limit) {
                result = -1;
            }
            else {
                if (len > limit - position) {
                    len = (int)(limit - position);
                }
                result = delegate.read(b, off, len);
                if (result != -1) {
                    position += result;
                }
            }
            return result;
        }
    }

    //----------------------------------------------------------------------------------------------
    final static private class MultiplexOutputStream extends OutputStream {

        //------------------------------------------------------------------------------------------
        static private interface IOOperation {
            void run(OutputStream delegate)
            throws IOException;
        }

        final private OutputStream[] delegates;

        //------------------------------------------------------------------------------------------
        public MultiplexOutputStream(OutputStream first, OutputStream... rest) {
            Check.nonNull(first, "first");
            Check.nonNull(rest, "rest");
            rest = rest.clone();
            Check.allNonNull(rest, "rest");
            delegates = new OutputStream[1 + rest.length];
            delegates[0] = first;
            System.arraycopy(rest, 0, delegates, 1, rest.length);
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void close()
        throws IOException {
            boolean exceptionInProgress = true;
            try {
                flush();
                exceptionInProgress = false;
            }
            finally {
                doAll(0, exceptionInProgress, new IOOperation() {
                    public void run(OutputStream delegate)
                    throws IOException {
                        delegate.close();
                    }
                });
            }
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void flush()
        throws IOException {
            doAll(0, false, new IOOperation() {
                public void run(OutputStream delegate)
                throws IOException {
                    delegate.flush();
                }
            });
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void write(final byte[] buffer)
        throws IOException {
            Check.nonNull(buffer);
            doAll(0, false, new IOOperation() {
                public void run(OutputStream delegate)
                throws IOException {
                    delegate.write(buffer);
                }
            });
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void write(final byte[] buffer, final int offset, final int length)
        throws IOException {
            Check.nonNull(buffer);
            Check.indexRange(buffer.length, offset, length);
            doAll(0, false, new IOOperation() {
                public void run(OutputStream delegate)
                throws IOException {
                    delegate.write(buffer, offset, length);
                }
            });
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void write(final int byteValue)
        throws IOException {
            doAll(0, false, new IOOperation() {
                public void run(OutputStream delegate)
                throws IOException {
                    delegate.write(byteValue);
                }
            });
        }

        //------------------------------------------------------------------------------------------
        /**
         * Applies an operation to all delegates starting at index
         * <code>start</code>. If an exception is thrown, the operation is still
         * applied to the remaining delegates, but only the first exception will
         * be propagated.
         */
        private void doAll(int start, boolean exceptionInProgress, IOOperation operation)
        throws IOException {
            if (start < delegates.length) {
                boolean localExceptionInProgress = true;
                try {
                    OutputStream delegate = delegates[start];
                    if (exceptionInProgress) {
                        try {
                            operation.run(delegate);
                        }
                        catch (Throwable swallow) {
                        }
                    }
                    else {
                        operation.run(delegate);
                    }
                    localExceptionInProgress = false;
                }
                finally {
                    // Ensure remaining streams have operation performed even
                    // if the current one threw an exception
                    doAll(start + 1, localExceptionInProgress || exceptionInProgress, operation);
                }
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    final static private class NoCloseOutputStream extends OutputStream {
        final private OutputStream delegate;
        volatile private boolean open = true;

        //------------------------------------------------------------------------------------------
        NoCloseOutputStream(OutputStream delegate) {
            this.delegate = Check.nonNull(delegate, "delegate");
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void close()
        throws IOException {
            open = false;
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void flush()
        throws IOException {
            checkIsOpen();
            delegate.flush();
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void write(byte[] buffer)
        throws IOException {
            checkIsOpen();
            delegate.write(buffer);
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void write(byte[] buffer, int offset, int length)
        throws IOException {
            checkIsOpen();
            delegate.write(buffer, offset, length);
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void write(int byteValue)
        throws IOException {
            checkIsOpen();
            delegate.write(byteValue);
        }

        //------------------------------------------------------------------------------------------
        private void checkIsOpen()
        throws IOException {
            if (!open) {
                throw new IOException("Closed");
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    final static private class NoClosePrintStream extends PrintStream {
        volatile private boolean open = true;

        //------------------------------------------------------------------------------------------
        NoClosePrintStream(PrintStream delegate) {
            super(delegate);
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void close() {
            open = false;
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void flush() {
            checkIsOpen();
            super.flush();
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void write(byte[] buffer)
        throws IOException {
            checkIsOpen();
            super.write(buffer);
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void write(byte[] buffer, int offset, int length) {
            checkIsOpen();
            super.write(buffer, offset, length);
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void write(int byteValue) {
            checkIsOpen();
            super.write(byteValue);
        }

        //------------------------------------------------------------------------------------------
        private void checkIsOpen() {
            if (!open) {
                setError();
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    final static private class NoClosePrintWriter extends PrintWriter {
        volatile private boolean open = true;

        //------------------------------------------------------------------------------------------
        NoClosePrintWriter(Writer delegate) {
            super(delegate);
        }

        //------------------------------------------------------------------------------------------
        @Override
        public PrintWriter append(char charValue) {
            checkIsOpen();
            return super.append(charValue);
        }

        //------------------------------------------------------------------------------------------
        @Override
        public PrintWriter append(CharSequence charSequence) {
            checkIsOpen();
            return super.append(charSequence);
        }

        //------------------------------------------------------------------------------------------
        @Override
        public PrintWriter append(CharSequence charSequence, int start, int end) {
            checkIsOpen();
            return super.append(charSequence, start, end);
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void close() {
            open = false;
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void flush() {
            checkIsOpen();
            super.flush();
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void write(char[] buffer) {
            checkIsOpen();
            super.write(buffer);
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void write(char[] buffer, int offset, int length) {
            checkIsOpen();
            super.write(buffer, offset, length);
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void write(int charValue) {
            checkIsOpen();
            super.write(charValue);
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void write(String str) {
            checkIsOpen();
            super.write(str);
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void write(String str, int off, int len) {
            checkIsOpen();
            super.write(str, off, len);
        }

        //------------------------------------------------------------------------------------------
        private void checkIsOpen() {
            if (!open) {
                setError();
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    final static private class NoCloseWriter extends Writer {
        final private Writer delegate;
        volatile private boolean open = true;

        //------------------------------------------------------------------------------------------
        NoCloseWriter(Writer delegate) {
            this.delegate = Check.nonNull(delegate, "delegate");
        }

        //------------------------------------------------------------------------------------------
        @Override
        public Writer append(char charValue)
        throws IOException {
            checkIsOpen();
            return delegate.append(charValue);
        }

        //------------------------------------------------------------------------------------------
        @Override
        public Writer append(CharSequence charSequence)
        throws IOException {
            checkIsOpen();
            return delegate.append(charSequence);
        }

        //------------------------------------------------------------------------------------------
        @Override
        public Writer append(CharSequence charSequence, int start, int end)
        throws IOException {
            checkIsOpen();
            return delegate.append(charSequence, start, end);
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void close()
        throws IOException {
            open = false;
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void flush()
        throws IOException {
            checkIsOpen();
            delegate.flush();
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void write(char[] buffer)
        throws IOException {
            checkIsOpen();
            delegate.write(buffer);
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void write(char[] buffer, int offset, int length)
        throws IOException {
            checkIsOpen();
            delegate.write(buffer, offset, length);
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void write(int charValue)
        throws IOException {
            checkIsOpen();
            delegate.write(charValue);
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void write(String str)
        throws IOException {
            checkIsOpen();
            delegate.write(str);
        }

        //------------------------------------------------------------------------------------------
        @Override
        public void write(String str, int off, int len)
        throws IOException {
            checkIsOpen();
            delegate.write(str, off, len);
        }

        //------------------------------------------------------------------------------------------
        private void checkIsOpen()
        throws IOException {
            if (!open) {
                throw new IOException("Closed");
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Delays initialization of the PWD until the first invocation of
     * {@link #pwd()}.
     */
    final static private class PwdInitializer {
        final static File pwd;
        static {
            // The most reliable way of referring to PWD is the relative path
            // ".". The system property "user.dir" normally refers to PWD, but
            // may be modified before this class loads. The actual PWD,
            // however, never changes.
            File file = new File(".");

            // Get the path minus the dot.
            String path = file.getAbsolutePath();
            path = path.substring(0, path.length() - 1);

            // Check if the path is a root
            boolean pathIsARoot = false;
            for (File root : roots()) {
                if (root.getPath().equals(path)) {
                    pathIsARoot = true;
                    break;
                }
            }

            // If the path is not a root, remove the separator. Otherwise,
            // we leave the separator so that the path remains valid.
            if (!pathIsARoot) {
                path = path.substring(0, path.length() - 1);
            }

            pwd = new File(path);
        }
    }
    //----------------------------------------------------------------------------------------------
    /**
     * Delays initialization of the UTF16BE {@link Charset} until the first
     * invocation of {@link #utf16be()}.
     */
    final static private class Utf16beInitializer {
        final static Charset UTF16BE = Charset.forName("UTF-16BE");
    }


    //----------------------------------------------------------------------------------------------
    /**
     * Delays initialization of the UTF16 {@link Charset} until the first
     * invocation of {@link #utf16()}.
     */
    final static private class Utf16Initializer {
        final static Charset UTF16 = Charset.forName("UTF-16");
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Delays initialization of the UTF16LE {@link Charset} until the first
     * invocation of {@link #utf16le()}.
     */
    final static private class Utf16leInitializer {
        final static Charset UTF16LE = Charset.forName("UTF-16LE");
    }

    //----------------------------------------------------------------------------------------------
    /**
     * Delays initialization of the UTF8 {@link Charset} until the first
     * invocation of {@link #utf8()}.
     */
    final static private class Utf8Initializer {
        final static Charset UTF8 = Charset.forName("UTF-8");
    }

    private IO() {
        throw new UnsupportedOperationException();
    }
}
