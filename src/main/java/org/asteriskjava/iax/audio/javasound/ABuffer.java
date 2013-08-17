
package org.asteriskjava.iax.audio.javasound;

/**
 * class to encapsulate the concept of an audio buffer and it's state
 */
public class ABuffer {


    private byte[] _buff;
    private boolean _written;
    private long _stamp;
    private long _astamp;

    public ABuffer(int sz) {
        _buff = new byte[sz];
    }

    public byte[] getBuff() {
        return _buff;
    }

    public boolean isWritten() {
        return _written;
    }

    public void setWritten() {
        _written = true;
    }

    public void setRead() {
        _written = false;
    }

    public long getStamp() {
        return _stamp;
    }

    public void setStamp(long stamp) {
        _stamp = stamp;
    }

    public long getAStamp() {
        return _astamp;
    }

    public void setAStamp(long as) {
        _astamp = as;
    }
}
