package org.asteriskjava.iax.util;

public class ByteBuffer {
    private byte[] myStore = null; // My backing store
    private int pos = 0; // Position index
    private int offset = 0; // into backing store to enable slicing

    /**
     * allocate
     *
     * @param i int
     * @return ByteBuffer
     */
    public static ByteBuffer allocate(int i) {
        ByteBuffer bb = new ByteBuffer();
        bb.myStore = new byte[i];
        bb.pos = 0;
        bb.offset = 0;
        return bb;
    }

    /**
     * wrap
     *
     * @param bs byte[]
     * @return ByteBuffer
     */
    public static ByteBuffer wrap(byte[] bs) {
        ByteBuffer bb = new ByteBuffer();
        bb.myStore = bs;
        bb.pos = 0;
        bb.offset = 0;
        return bb;
    }

    /**
     * slice
     *
     * @return ByteBuffer
     */
    public ByteBuffer slice() {
        ByteBuffer bb = new ByteBuffer();
        bb.myStore = myStore;
        bb.pos = 0;
        bb.offset = pos;
        return bb;
    }

    /**
     * array
     *
     * @return byte[]
     */
    public byte[] array() {
        if (offset != 0) {
            throw new java.lang.IllegalStateException();
        }
        return myStore;
    }

    /**
     * position
     *
     * @return int
     */
    public int position() {
        return pos;
    }

    /**
     * getShort
     *
     * @return short
     */
    public short getShort() {
        if (offset + pos + 2 > myStore.length) {
            throw new IndexOutOfBoundsException();
        }
        short s = (short) ( (myStore[offset + pos] << 8) +
                           (myStore[offset + pos + 1] & 0xFF));
        pos += 2;
        return s;
    }

    public short getShort(int of) {
        if (offset + of + 2 > myStore.length) {
            throw new IndexOutOfBoundsException();
        }
        short s = (short) ( (myStore[offset + of] << 8) +
                           (myStore[offset + of + 1] & 0xFF));
        return s;
    }

    /**
     * getInt
     *
     * @return int
     */
    public int getInt() {
        if (offset + pos + 4 > myStore.length) {
            throw new IndexOutOfBoundsException();
        }
        int i = (myStore[offset + pos] << 24)
            + ( (myStore[offset + pos + 1] & 0xFF) << 16)
            + ( (myStore[offset + pos + 2] & 0xFF) << 8)
            + (myStore[offset + pos + 3] & 0xFF);
        pos += 4;
        return i;
    }

    /**
     * get
     *
     * @return byte
     */
    public byte get() {
        if (offset + pos + 1 > myStore.length) {
            throw new IndexOutOfBoundsException();
        }
        return myStore[offset + pos++];
    }

    /**
     * putChar
     *
     * @param c char
     */
    public void putChar(char c) {
        if (offset + pos + 2 > myStore.length) {
            throw new IndexOutOfBoundsException();
        }
        myStore[offset + pos++] = (byte) ( ( (short) c) >> 8);
        myStore[offset + pos++] = (byte) c;
    }

    /**
     * putChar
     *
     * @param i int
     * @param c char
     */
    public void putChar(int i, char c) {
        if (offset + i + 2 > myStore.length) {
            throw new IndexOutOfBoundsException();
        }
        myStore[offset + i] = (byte) ( ( (short) c) >> 8);
        myStore[offset + i + 1] = (byte) c;
    }

    /**
     * putInt
     *
     * @param i int
     */
    public void putInt(int i) {
        if (offset + pos + 4 > myStore.length) {
            throw new IndexOutOfBoundsException();
        }
        myStore[offset + pos++] = (byte) (i >> 24);
        myStore[offset + pos++] = (byte) ( (i >> 16) & 0xff);
        myStore[offset + pos++] = (byte) ( (i >> 8) & 0xff);
        myStore[offset + pos++] = (byte) (i & 0xff);
    }

    /**
     * put
     *
     * @param b byte
     */
    public void put(byte b) {
        if (offset + pos + 1 > myStore.length) {
            throw new IndexOutOfBoundsException();
        }
        myStore[pos] = b;
        pos++;
    }

    /**
     * put
     *
     * @param payload byte[]
     */
    public void put(byte[] payload) {
        if (offset + pos + payload.length > myStore.length) {
            throw new IndexOutOfBoundsException();
        }
        System.arraycopy(payload, 0, myStore, offset + pos, payload.length);
        pos += payload.length;
    }

    /**
     * getChar
     *
     * @return char
     */
    public char getChar() {
        if (offset + pos + 2 > myStore.length) {
            throw new IndexOutOfBoundsException();
        }
        short s = (short) ( (myStore[offset + pos] << 8) +
                           (myStore[offset + pos + 1] & 0xFF));
        pos += 2;
        return (char) s;
    }

    /**
     * getChar
     *
     * @param i int
     * @return char
     */
    public char getChar(int i) {
        if (offset + i + 2 > myStore.length) {
            throw new IndexOutOfBoundsException();
        }
        short s = (short) ( (myStore[offset + i] << 8) +
                           (myStore[offset + i + 1] & 0xFF));
        return (char) s;
    }

    /**
     * get
     *
     * @param b byte[]
     */
    public void get(byte[] b) {
        int l = remaining();
        if (l > b.length) {
            l = b.length;
        }
        System.arraycopy(myStore, offset + pos, b, 0, l);
        pos += l;
    }

    /**
     * remaining
     *
     * @return int
     */
    public int remaining() {
        return myStore.length - offset - pos;
    }

    /**
     * putShort
     *
     * @param s short
     */
    public void putShort(short s) {
        if (offset + pos + 2 > myStore.length) {
            throw new IndexOutOfBoundsException();
        }
        myStore[offset + pos++] = (byte) (s >> 8);
        myStore[offset + pos++] = (byte) (s & 0xff);
    }

    public void putShort(int of, short s) {
        if (offset + of + 2 > myStore.length) {
            throw new IndexOutOfBoundsException();
        }
        myStore[offset + of++] = (byte) (s >> 8);
        myStore[offset + of++] = (byte) (s & 0xff);
    }

    public boolean hasRemaining() {
        return (offset + pos < myStore.length);
    }
}
