
package org.asteriskjava.iax.protocol;


import org.asteriskjava.iax.util.ByteBuffer;

/**
 * Base class for all frames
 */
abstract class Frame {


    final static byte[] EMPTY = new byte[0];

    /**
     * The call object
     */
    protected Call _call;

    /**
     * The timestamp
     */
    protected Long _timestamp;

    /**
     * The F bit
     */
    protected boolean _fullBit;

    /**
     * The source call number
     */
    protected int _sCall;

    /**
     * The data
     */
    protected ByteBuffer _data;


    /**
     * Sets the timestamp as int.
     *
     * @param v The timestamp
     * @see #setTimestamp(Long)
     */
    void setTimestampVal(long v) {
        _timestamp = Long.valueOf(v);
    }


    /**
     * Sets the timestamp as Integer object.
     *
     * @param val The timestamp
     * @see #setTimestampVal(long)
     */
    void setTimestamp(Long val) {
        _timestamp = val;
    }


    /**
     * Returns the timestamp as int
     *
     * @return the timestamp
     * @see #getTimestamp
     */
    long getTimestampVal() {
        long ret = 0;
        if (_timestamp != null) {
            ret = _timestamp.longValue();
        }
        return ret;
    }


    /**
     * Returns the timestamp as Integer object
     *
     * @return the timestamp
     * @see #getTimestampVal
     */
    Long getTimestamp() {
        return _timestamp;
    }


    /**
     * arrived is called when a packet arrives.
     *
     * @throws IAX2ProtocolException
     */
    abstract void arrived() throws IAX2ProtocolException;


    /**
     * ack is called to send any required response.
     */
    abstract void ack();
}

