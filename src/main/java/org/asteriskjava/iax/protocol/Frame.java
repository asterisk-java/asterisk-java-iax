// NAME
//      $RCSfile: Frame.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision$
// CREATED
//      $Date$
// COPYRIGHT
//      Mexuar Technologies Ltd
// TO DO
//
package org.asteriskjava.iax.protocol;

import org.asteriskjava.iax.util.*;
/**
 * Base class for all frames
 *
 * @author <a href="mailto:thp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision$ $Date$
 */
abstract class Frame {

    private final static String version_id =
            "@(#)$Id$ Copyright Mexuar Technologies Ltd";

    final static byte[] EMPTY = new byte[0];

    /** The call object */
    protected Call _call;

    /** The timestamp */
    protected Long _timestamp;

    /** The F bit */
    protected boolean _fullBit;

    /** The source call number */
    protected int _sCall;

    /** The data */
    protected ByteBuffer _data;



    /**
     * Sets the timestamp as int.
     *
     * @param v The timestamp
     * @see #setTimestamp(Long)
     */
    void setTimestampVal(long v) {
        _timestamp = new Long(v);
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

