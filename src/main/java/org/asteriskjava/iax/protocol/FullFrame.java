// NAME
//      $RCSfile: FullFrame.java,v $
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
 * Represents all FullFrames - understands about Acks etc.
 *
 * <pre>
 *                     1                   2                   3
 * 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |F|     Source Call Number      |R|   Destination Call Number   |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                           time-stamp                          |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |    OSeqno     |    ISeqno     |   Frame Type  |C|  Subclass   |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                                                               |
 * :                             Data                              :
 * |                                                               |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * </pre>
 *
 * @author <a href="mailto:thp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision$ $Date$
 */
public abstract class FullFrame extends Frame {

    private final static String version_id =
            "@(#)$Id$ Copyright Mexuar Technologies Ltd";

    final static int DTMF = 1; // 0-9, A-D, *, #
    final static int VOICE = 2; //   Data Audio Compression Format Raw Voice Data
    final static int VIDEO = 3; //Video Compression Format Raw Video Data
    final static int CONTROL = 4; //See Control Frame Types
    final static int NULL = 5; //
    final static int IAXCONTROL = 6; //IAX Protocol Messages Information Elements
    final static int TEXT = 7; //Raw Text
    final static int IMAGE = 8; // Image Compression Format Raw Image Data
    final static int HTML = 9; //HTML Frame Types Message Specific

    /** The maximum number of retires we'll send */
    private final static int MAXRETRIES = 6;

    /** The retry interval */
    private final static int RETRYINTERVAL = 500;

    /** The latest outgoing message */
    protected ByteBuffer _outGoing;

    /** The destination call number */
    int _dCall;

    /** The outbound stream sequence number */
    int _oseq;

    /** The inbound stream sequence number */
    int _iseq;

    /** The subclass */
    int _subclass;

    /** The frame type */
    int _frametype;

    /** The C bit */
    boolean _cbit;

    /** is retry (or not) */
    boolean _retry;

    /** Indicates whether this message is an ACK */
    boolean _iamanack;

    /** The acknowledgement frame to this frame */
    FullFrame _myAck;

    /** The next retry timestamp */
    private int _nextRetryTime;

    /** The number of retries sent */
    private int _numRetries;


    /**
     * The inbound constructor.
     *
     * @param call The Call object
     * @param bs The incoming message bytes
     * @throws IllegalArgumentException The bytes do not represent a
     * fullframe
     */
    public FullFrame(Call call, byte[] bs)
    throws IllegalArgumentException {
        ByteBuffer buf = ByteBuffer.wrap(bs);
        _sCall = buf.getShort();
        if (_sCall < 0) {
            _sCall = 0x7fff & _sCall;
            _fullBit = true;
        } else {
            _fullBit = false;
            throw new IllegalArgumentException("Not a fullframe, but miniframe.");
        }
        _dCall = buf.getShort();
        if (_dCall < 0) {
            _dCall = 0x7fff & _dCall;
            _retry = true;
        }
        long tst = buf.getInt();
        tst = (tst < 1) ? tst + 0x100000000L : tst;
        setTimestampVal(tst);
        _oseq = tint(buf.get());
        _iseq = tint(buf.get());
        _frametype = buf.get();
        _subclass = buf.get();
        if (_subclass < 0) {
            _subclass = 1 << (_subclass & 0x7f);
            _cbit = true;
        }
        _data = buf.slice();
        _call = call;
    }


    /**
     * The outbound constructor.
     *
     * @param call The Call object
     */
    public FullFrame(Call call) {
        _call = call;
        _fullBit = true;
        Character r = call.getRno();
        if (r != null) {
            _dCall = r.charValue();
        }
        Character l = call.getLno();
        if (l != null) {
            _sCall = l.charValue();
        }
        setTimestampVal(call.getTimestamp());
    }


    /**
     * Creates a new FullFrame of the correct type.
     *
     * @param call Call
     * @param bs byte[]
     * @return a FullFrame
     */
    public static FullFrame create(Call call, byte[] bs) {
        FullFrame ret = null;
        if (bs.length >= 12) {
            int frametype = 0x7f & bs[10];
            switch (frametype) {
                case IAXCONTROL:
                    ret = new ProtocolControlFrame(call, bs);
                    break;
                case CONTROL:
                    ret = new ControlFrame(call, bs);
                    break;
                case VOICE:
                    ret = new VoiceFrame(call, bs);
                    break;
                default:
                    Log.warn("FullFrame type " + frametype);
                    ret =
                        new FullFrame(call, bs) {
                            void ack() {
                                Log.warn("Sending Ack on unimplemented FullFrame Type");
                                sendAck();
                            }
                        };
                    break;
            }
        }
        return ret;
    }


    /**
     * Returns whether or not this is a retry frame.
     *
     * @return true if retry, false otherwise
     */
    public boolean isRetry() {
        return this._retry;
    }


    /**
     * Returns if this is a NEW message. False by default.
     *
     * @return true if NEW, false otherwise
     */
    public boolean isANew() {
        return false;
    }


    /**
     * Returns if this is an ACK message.
     *
     * @return boolean
     */
    public boolean isAck() {
        return this._iamanack;
    }


    /**
     * Returns the source call number as an Character.
     *
     * @return The source call no as Character.
     */
    public Character getScall() {
        return new Character((char) ((0xffff) & this._sCall));
    }


    /**
     * Sends a specified payload. Payload represents the Data field in
     * the frame.
     *
     * @param payload The payload (data)
     */
    public void sendMe(byte[] payload) {
        _oseq = _call.getOseqInc();
        _iseq = _call.getIseq();
        _cbit = false;

        ByteBuffer buff = ByteBuffer.allocate(payload.length + 12);
        buff.putChar((char) (0x8000 | _sCall));
        int rd = _dCall;
        if (_retry) {
            rd |= 0x8000;
        }
        buff.putChar((char) rd);
        long tst = this.getTimestampVal();
        tst =  ((0x100000000L & tst) > 0) ? tst - 0x100000000L : tst;
        buff.putInt((int)tst);
        buff.put((byte) _oseq);
        buff.put((byte) _iseq);
        buff.put((byte) _frametype);
        if (_subclass > 128){
            _cbit = true;
            for (int s=0; s< 31; s++){
                if (((1 << s) & _subclass) != 0) {
                    _subclass = s;
                    break;
                }
            }
        }
        int sc = _subclass;
        if (_cbit) {
            sc += 128;
        }
        buff.put((byte) sc);
        buff.put(payload);
        sendAndStore(buff);
    }


    /**
     * Resends this frame.
     * Returns if the frame has reached it max number of retries or not.
     * We don't actually send the last retry, but use it as a timeout
     * mechanism.
     *
     * @return True if number of retries hasn't rearched its max, False
     * if this was the last retry.
     */
    /* five retries are send, the sixth time round is used to time out.
     */
    public boolean resendMe() {
        int now = _call.getTimestamp();
        // is it time for another resend?
        if (_nextRetryTime < now) {
            // do NOT send the last time!
            if (_numRetries < MAXRETRIES -1) {
                if (!_retry) {
                    setRetryBit();
                }
                sendFromStore();
                Log.warn("time " + now + " resending "
                    + this.getTimestampVal() + " " + _numRetries + "th time.");
            } else {
                Log.warn("time " + now + " NOT resending "
                    + this.getTimestampVal() + " " + _numRetries + "th time.");
            }
            _nextRetryTime = now + (RETRYINTERVAL * ++_numRetries);
        }
        return _numRetries < MAXRETRIES;
    }


    /**
     * Creates an acknowledgement frame. This method is called by
     * sendAck().
     *
     * @param sort The type of acknowledgement frame
     * @return an acknowledgement frame
     * @see #sendAck
     */
    protected ProtocolControlFrame mkAck(int sort) {
        ProtocolControlFrame ack = new ProtocolControlFrame(_call);
        ack._dCall = _sCall;
        ack._sCall = _dCall;
        ack._iseq = _call.getIseq();
        switch (sort) {
            case ProtocolControlFrame.ACK:
                ack._iamanack = true;
                ack.setTimestamp(this.getTimestamp());
                ack._iseq = _call.getIseq();
                ack._oseq = _call.getOseq();
                break;
            case ProtocolControlFrame.PONG:
            case ProtocolControlFrame.LAGRP:
                ack._oseq = _call.getOseqInc();
                ack._iseq = _call.getIseq();
                ack.setTimestamp(this.getTimestamp());
                break;
            default:
                ack._oseq = _call.getOseqInc();
                ack._iseq = _call.getIseq();
                break;
        }

        ack._subclass = sort;
        _myAck = ack;
        return ack;
    }


    /**
     * Sends an acknowledgement frame.
     */
    protected void sendAck() {
        ProtocolControlFrame ack = mkAck(ProtocolControlFrame.ACK);
        Log.debug("Sending Ack");
        ack.sendMe((InfoElement) null);
    }


    /**
     * Sends the latest outgoing message.
     *
     * @see Call#send(ByteBuffer)
     */
    protected void sendFromStore() {
        if (_outGoing != null) {
            _call.send(_outGoing);
        }
    }


    /**
     * Sends a new message. If this message is not ACK, store it and
     * up the nextRetryTime.
     *
     * @param b The bytes to send
     */
    protected void sendAndStore(ByteBuffer b) {
        _outGoing = b;
        if (!_iamanack) {
            _nextRetryTime = _call.getTimestamp() + RETRYINTERVAL;
            _call.addUnacked(this);
        }
        log("sent");
        sendFromStore();
    }


    /**
     * Logs the timestamp and the in- + outbound stream sequence
     * number.
     *
     * @param inoutNtype Text to include
     */
    protected void log(String inoutNtype) {
        StringBuffer bu = new StringBuffer("Time: ");
        bu.append(_call.getTimestamp()).append(", ");
        bu.append(inoutNtype);
        bu.append(", Timestamp: ").append(this.getTimestampVal());
        bu.append(", iseq: ").append(_iseq);
        bu.append(", oseq: ").append(_oseq);
        if (this._retry) {
            bu.append(", retry");
        }
        Log.debug(bu.toString());
    }


    /**
     * Commit this frame. This method is called when a packet we sent
     * has been acked.
     *
     * @param ack The ack frame
     */
    void commit(FullFrame ack) {
    }


    /**
     * Logs this frame.
     */
    void dump() {
        StringBuffer d = new StringBuffer("Frame Dump\n");
        d.append("\n\t Source Call = ").append(_sCall);
        d.append("\n\t Dest Call = ").append(_dCall);
        d.append("\n\t retry  = ").append(_retry);
        d.append("\n\t timestamp Call = ").append(_timestamp);
        d.append("\n\t OSequ no = ").append(_oseq);
        d.append("\n\t ISeqNo = ").append(_iseq);
        d.append("\n\t Sub Class = ").append(_subclass);
        d.append("\n\t Frame Type = ").append(_frametype);
        d.append("\n\t C Bit = ").append(_cbit);
        d.append("\n\t Full Frame = ").append(_fullBit);
        Log.debug("packet dump" + d);
    }


    /**
     * Converts a byte to an int.
     *
     * @param b The byte
     * @return The int
     */
    int tint(byte b) {
        int ret = b;
        if (ret < 0) {
            ret = 128 + ((0x7f) & b);
        }
        return ret;
    }


    /**
     * arrived is called when a packet arrives. This method
     * doesn't do anything more than dumping the frame.
     *
     * @throws IAX2ProtocolException
     * @see #dump()
     */
    void arrived() throws IAX2ProtocolException {
        dump();
    }


    /**
     * Sets the retry bit.
     */
    private void setRetryBit() {
        if (_outGoing != null) {
            char old = _outGoing.getChar(2);
            old |= 0x8000;
            _outGoing.putChar(2, old);
            _retry = true;
        }
    }

}

