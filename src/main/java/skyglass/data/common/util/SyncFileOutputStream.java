package skyglass.data.common.util;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This subclass of {@link FileOutputStream} ensures data is written out to disk
 * before {@link #close()} returns.
 * <p>
 * Note that a "flush" and a "sync" are not the same. A flush ensures all data
 * in application buffers are flushed to operating system buffers. A sync
 * instructs the operating system to flush data in its buffers to disk. To
 * ensure all data written to this stream is on disk, it is thus necessary to
 * perform a flush followed by a sync. The method {@link #flushAndSync()} does
 * just that. <code>close()</code> also does this immediately before closing the
 * operating system file object.
 * <p>
 * With a conventional <code>FileOutputStream</code>, as the operating system is
 * not instructed to sync, it may flush its buffers at its convenience long
 * after <code>close()</code> has completed. In this interval, system failure
 * can result in lost data or even a corrupt file.
 *
 * @see FileDescriptor#sync()
 */
public class SyncFileOutputStream extends FileOutputStream {

    //**************************************************************************
    //  CLASS
    //**************************************************************************

    //**************************************************************************
    //  INSTANCE
    //**************************************************************************

    //--------------------------------------------------------------------------
    public SyncFileOutputStream(File file, boolean append)
    throws FileNotFoundException {
        super(file, append);
    }

    //--------------------------------------------------------------------------
    public SyncFileOutputStream(File file)
    throws FileNotFoundException {
        super(file);
    }

    //--------------------------------------------------------------------------
    public SyncFileOutputStream(FileDescriptor fdObj) {
        super(fdObj);
    }

    //--------------------------------------------------------------------------
    public SyncFileOutputStream(String name, boolean append)
    throws FileNotFoundException {
        super(name, append);
    }

    //--------------------------------------------------------------------------
    public SyncFileOutputStream(String name)
    throws FileNotFoundException {
        super(name);
    }

    //--------------------------------------------------------------------------
    /**
     * Flushes all written data and syncs it to disk. The stream is still
     * available for writing.
     *
     * @throws IOException
     *         if the flush or sync fails.
     */
    public void flushAndSync()
    throws IOException {
        // Although FileOutputStream currently implements flush() as a no-op, if
        // that changes or flush() is overridden in a subclass, we need to flush
        // to be certain sync() is writing all data written.
        flush();
        getFD().sync();
    }

    //--------------------------------------------------------------------------
    /**
     * Closes the stream. All data is flushed and synced before completing the
     * close.
     *
     * @throws IOException
     *         if the flush or sync fails or an I/O error is reporting during
     *         the close.
     */
    @Override
    public void close()
    throws IOException {
        try {
            flushAndSync();
        }
        finally {
            super.close();
        }
    }
}
