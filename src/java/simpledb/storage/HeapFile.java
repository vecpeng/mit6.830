package simpledb.storage;

import simpledb.common.DbException;
import simpledb.common.Permissions;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 *
 * @author Sam Madden
 * @see HeapPage#HeapPage
 */
public class HeapFile implements DbFile {

    private File file;
    private TupleDesc td;
    private BufferPool bufferPool;

    /**
     * Constructs a heap file backed by the specified file.
     *
     * @param f the file that stores the on-disk backing store for this heap
     *          file.
     */
    public HeapFile(File f, TupleDesc td) {
        this.file = f;
        this.td = td;
        bufferPool = new BufferPool(numPages());
    }

    /**
     * Returns the File backing this HeapFile on disk.
     *
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
       return file;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere to ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     *
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
       return file.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     *
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
       return td;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        try {
            byte[] buff = new byte[BufferPool.getPageSize()];
            int offset = BufferPool.getPageSize() * pid.getPageNumber();
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            randomAccessFile.seek(offset);
            randomAccessFile.read(buff, 0, BufferPool.getPageSize());
            randomAccessFile.close();
            return new HeapPage((HeapPageId) pid, buff);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // TODO: some code goes here
        // not necessary for lab1
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        long fileSize = file.length();
        int pageSize = BufferPool.getPageSize();
        return (int) fileSize / pageSize + ((fileSize % pageSize == 0) ? 0 : 1) ;
    }

    // see DbFile.java for javadocs
    public List<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // TODO: some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public List<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // TODO: some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        return new DbFileIterator() {
            private int curPgNo = -1;
            private Iterator<Tuple> tupleIter;
            @Override
            public void open() throws DbException, TransactionAbortedException {
                if (numPages() > 0) {
                    curPgNo++;
                } else {
                    return;
                }
                PageId curPageId = new HeapPageId(getId(), curPgNo);
                HeapPage curPage = (HeapPage) bufferPool.getPage(tid, curPageId, Permissions.READ_ONLY);
                tupleIter = curPage.iterator();
            }

            @Override
            public boolean hasNext() throws DbException, TransactionAbortedException {
                return (curPgNo + 1) < numPages() || (tupleIter != null && tupleIter.hasNext());
            }

            @Override
            public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                if (tupleIter != null && tupleIter.hasNext()) {
                    return tupleIter.next();
                }
                curPgNo++;
                if (curPgNo < numPages()) {
                    PageId curPageId = new HeapPageId(getId(), curPgNo);
                    HeapPage curPage = (HeapPage) bufferPool.getPage(tid, curPageId, null);
                    tupleIter = curPage.iterator();
                    return tupleIter.next();
                }
                throw new NoSuchElementException();
            }

            @Override
            public void rewind() throws DbException, TransactionAbortedException {
                curPgNo = 0;
                PageId curPageId = new HeapPageId(getId(), 0);
                HeapPage curPage = (HeapPage) bufferPool.getPage(null, curPageId, null);
                tupleIter = curPage.iterator();
            }

            @Override
            public void close() {
                tupleIter = null;
            }
        };
    }

}

