package org.asteriskjava.iax.util;

import junit.framework.TestCase;

import java.io.UnsupportedEncodingException;

public class MD5DigestTest extends TestCase
{
    public void testMd5() throws UnsupportedEncodingException
    {
        MD5Digest digest = new MD5Digest();
        byte[] in = "Hello World".getBytes("UTF-8");
        byte[] out = new byte[digest.getDigestSize()];
        digest.update(in, 0, in.length);
        digest.doFinal(out, 0);
    }
}
