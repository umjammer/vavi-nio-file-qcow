/*
 * https://raw.githubusercontent.com/dleonard0/read-qcow2/master/qcow2.c
 */

package vavi.nio.file.qcow;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Arrays;

import vavi.util.ByteUtil;


/**
 * Open and read from a qcow2 image file.
 * David Leonard, 2020. CC0
 */
public class SimpleQcow {

    static final long _SC_PAGESIZE = 40000;

    /* Cluster kinds, used for the cache */
    static final int KIND_L2 = 0;
    static final int KIND_DATA = 1;
    static final int KIND_MAX = 2;

    /* Context structure for an opened qcow2 image file */
    SeekableByteChannel fd;

    /* extracted from file header */
    long size;
    long[] l1_table;
    int l1_table_size;
    long cluster_size;
    int l1_shift;
    int l2_amask;
    int l2_shift;

    /* cluster cache */
    static class Cluster {
        long[] base;
        long offset;    /* 0 when unused */
    }

    Cluster[] clusters = new Cluster[KIND_MAX];

    /* Maps a cluster from the file into process virtual memory.
     * Each cluster kind is cached independently */
    private long[] load_cluster(long offset, int kind) throws IOException {
        if (offset == 0) {
            throw new IOException("There is no cluster 0");
        }

        Cluster c = clusters[kind];
        if (c.offset == offset)
            return c.base;    /* Cache hit */

        /* Cache miss; try to map */
        ByteBuffer bb = ByteBuffer.allocate((int) this.cluster_size);
        this.fd.position(offset);
        this.fd.read(bb);
        long[] base = bb.asLongBuffer().array();

        c.base = base;
        c.offset = offset;
        return base;
    }

    SimpleQcow(SeekableByteChannel fd) throws IOException {

        /* Check the header */
        ByteBuffer hdr = ByteBuffer.allocate(104);
        fd.position();
        long n = fd.read(hdr);
        if (n == -1)
            throw new IOException("read");
        if (n != hdr.capacity())
            throw new IOException("too short");

        if (!Arrays.equals(Arrays.copyOfRange(hdr.array(), 0, 4), "QFIû".getBytes()))
            throw new IOException("not qcow2");

        int version = ByteUtil.readBeInt(hdr.array(), 4);
        if (version < 2)
            throw new IOException("too old");

        int crypt_method = ByteUtil.readBeInt(hdr.array(), 32);
        if (crypt_method != 0)
            throw new IOException("encrypted");

        if (version >= 3) {
            long incompatible_features = ByteUtil.readBeLong(hdr.array(), 72);
            if (((incompatible_features >> 3) & 1) != 0)
                throw new IOException("compressed");
        }

        this.fd = fd;
        this.size = ByteUtil.readBeLong(hdr.array(), 24);

        int cluster_bits = ByteUtil.readBeInt(hdr.array(), 20);
        this.cluster_size = (long) 1 << cluster_bits;
        if (this.cluster_size == 0)
            throw new IOException("too big");

        /* The cluster size must be at least that of a page,
         * which is the smallest alignment mmap can do */
        long pagesize = _SC_PAGESIZE;
        if (this.cluster_size < pagesize)
            throw new IOException("too fine");

        /* Precompute shifts and masks to extract the L1
         * and L2 indicies from the offset. */
        this.l2_shift = cluster_bits;
        this.l2_amask = (1 << (cluster_bits - 3)) - 1;
        this.l1_shift = cluster_bits + cluster_bits - 3;

        long l1_table_offset = ByteUtil.readBeLong(hdr.array(), 40);
        int l1_size = ByteUtil.readBeInt(hdr.array(), 36);
        this.l1_table_size = l1_size * Long.BYTES;

        ByteBuffer base = ByteBuffer.allocate(this.l1_table_size);
        this.fd.position(l1_table_offset);
        this.fd.read(base);
        this.l1_table = base.asLongBuffer().array();
    }

    long size() {
        return this.size;
    }

    /* Reads virtual image data into memory */
    int read(byte[] dest, long len, long offset) throws IOException {
        int ret = 0;
        int destP = 0;

        /* Ensure read within bounds */
        if (offset >= this.size)
            len = 0;
        else if (len > this.size - offset)
            len = this.size - offset;

        while (len != 0) {
            long l2_index = (offset >> this.l2_shift) & this.l2_amask;
            long l1_index = (offset >> this.l1_shift);
            long l1_val = this.l1_table[(int) l1_index];
            long l2_offset = l1_val & 0x00ff_ffff_ffff_fe00L;
            long data_offset;

            if (l2_offset == 0) {
                data_offset = 0;
            } else {
                final long[] l2_table = load_cluster(l2_offset, KIND_L2);
                if (l2_table == null)
                    return -1;

                long l2_val = l2_table[(int) l2_index];
                if (((l2_val >> 62) & 1) != 0) {
                    throw new IOException("Compressed cluster not supported");
                }
                if ((l2_val & 1) != 0)
                    data_offset = 0;
                else
                    data_offset = l2_val & 0x00ff_ffff_ffff_fe00L;
            }

            /* Be careful not to read beyond the end of the cluster */
            long cluster_offset = offset % this.cluster_size;
            long rlen = len;
            if (rlen + cluster_offset > this.cluster_size)
                rlen = this.cluster_size - cluster_offset;

            if (data_offset == 0) {
                Arrays.fill(dest, 0, (int) rlen, (byte) 0);
            } else {
                this.fd.position(data_offset + cluster_offset);
                ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);
                this.fd.position(rlen);
                this.fd.read(bb);
                long n = bb.asLongBuffer().array()[0];
                if (n == -1)
                    return -1;
                rlen = n;
            }

            if (rlen == 0)
                break; /* EOF */
            ret += rlen;
            len -= rlen;
            destP += rlen;
            offset += rlen;
        }
        return ret;
    }
}
