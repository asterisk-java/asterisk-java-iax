package org.asteriskjava.iax.util;

/**
 * base implementation of MD4 family style digest as outlined in
 * "Handbook of Applied Cryptography", pages 344 - 347.
 */
public abstract class GeneralDigest
{
    private byte[]  xBuf = new byte[4];
    private int     xBufOff = 0;

    private long    byteCount;

	/**
	 * Standard constructor
	 */
	protected GeneralDigest()
	{
	}

	/**
	 * Copy constructor.  We are using copy constructors in place
	 * of the Object.clone() interface as this interface is not
	 * supported by J2ME.
	 */
	protected GeneralDigest(GeneralDigest t)
	{
		System.arraycopy(t.xBuf, 0, xBuf, 0, t.xBuf.length);
		xBufOff = t.xBufOff;
		byteCount = t.byteCount;
	}

    public void update(
        byte in)
    {
        xBuf[xBufOff++] = in;

        if (xBufOff == xBuf.length)
        {
            processWord(xBuf, 0);
            xBufOff = 0;
        }

        byteCount++;
    }

    public void update(
        byte[]  in,
        int     inOff,
        int     len)
    {
        //
        // fill the current word
        //
        while ((xBufOff != 0) && (len > 0))
        {
            update(in[inOff]);

            inOff++;
            len--;
        }

        //
        // process whole words.
        //
        while (len > 4)
        {
            processWord(in, inOff);

            inOff += 4;
            len -= 4;
            byteCount += 4;
        }

        //
        // load in the remainder.
        //
        while (len > 0)
        {
            update(in[inOff]);

            inOff++;
            len--;
        }
    }

    public void finish()
    {
        long    bitLength = (byteCount << 3);

        //
        // add the pad bytes.
        //
        update((byte)128);

        while (xBufOff != 0)
        {
            update((byte)0);
        }

        processLength(bitLength);

        processBlock();
    }

    public void reset()
    {
        byteCount = 0;

        xBufOff = 0;
        xBuf[0] = xBuf[1] = xBuf[2] = xBuf[3] = 0;
    }

    protected abstract void processWord(byte[] in, int inOff);

    protected abstract void processLength(long bitLength);

    protected abstract void processBlock();
}
