// NAME
//      $RCSfile: ProtocolControlFrame.java,v $
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

import org.asteriskjava.iax.audio.*;
import org.asteriskjava.iax.util.*;

/**
 * Representation of the IAX2 protocol control frame, also known as an
 * IAX Frame in the draft.
 *
 * @author <a href="mailto:thp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision$ $Date$
 */
public class ProtocolControlFrame
    extends FullFrame {

    private final static String version_id =
        "@(#)$Id$ Copyright Mexuar Technologies Ltd";

    final static int NEW = 1; //Initiate a new call
    final static int PING = 2; //Ping request
    final static int PONG = 3; //Ping reply
    final static int ACK = 4; //Acknowledgement
    final static int HANGUP = 5; //Initiate call teardown
    final static int REJECT = 6; //Reject
    final static int ACCEPT = 7; //Accepted
    final static int AUTHREQ = 8; //Authentication request
    final static int AUTHREP = 9; // Authentication reply
    final static int INVAL = 10; //Invalid call
    final static int LAGRQ = 11; //Lag request
    final static int LAGRP = 12; //Lag reply
    final static int REGREQ = 13; //Registration request
    final static int REGAUTH = 14; //Registration authenticate
    final static int REGACK = 15; //Registration acknowledgement
    final static int REGREJ = 16; //Registration reject
    final static int REGREL = 17; //Registration release
    final static int VNAK = 18; //Video/Voice retransmit request
    final static int DPREQ = 19; //Dialplan request
    final static int DPREP = 20; //Dialplan response
    final static int DIAL = 21; //Dial
    final static int TXREQ = 22; //Transfer request
    final static int TXCNT = 23; //Transfer connect
    final static int TXACC = 24; //Transfer accept
    final static int TXREADY = 25; //Transfer ready
    final static int TXREL = 26; //Transfer release
    final static int TXREJ = 27; //Transfer reject
    final static int QUELCH = 28; //Halt audio/video transmission
    final static int UNQUELCH = 29; // Resume audio/video transmission
    final static int POKE = 30; //Poke request
    final static int PAGE = 31; //Paging call description
    final static int MWI = 32; //Message waiting indication
    final static int UNSUPPORT = 33; //Unsupported message
    final static int TRANSFER = 34; //Remote transfer request
    final static String controlText[] = {
        "ZERO",
        "Initiate a new call",
        "Ping request",
        "Ping or poke reply",
        "Acknowledgement",
        "Initiate call teardown",
        "Reject",
        "Accepted",
        "Authentication request",
        "Authentication reply",
        "Invalid call",
        "Lag request",
        "Lag reply",
        "Registration request",
        "Registration authenticate",
        "Registration acknowledgement",
        "Registration reject",
        "Registration release",
        "Video/Voice retransmit request",
        "Dialplan request",
        "Dialplan response",
        "Dial",
        "Transfer request",
        "Transfer connect",
        "Transfer accept",
        "Transfer ready",
        "Transfer release",
        "Transfer reject",
        "Halt audio/video transmission",
        "Resume audio/video transmission",
        "Poke request",
        "Paging call description",
        "Message waiting indication",
        "Unsupported message",
        "Remote transfer request"
    };

    /** The information element */
    private InfoElement _iep;

    /**
     * The inbound constructor.
     *
     * @param p0 The Call object
     * @param p1 The incoming message bytes
     */
    public ProtocolControlFrame(Call p0, byte[] p1) {
        super(p0, p1);
        this._iamanack = (this._subclass == ACK);
        _iep = new InfoElement(this._data);
        try {
            _iep.parse(this);
        }
        catch (IAX2ProtocolException ex) {
            Log.warn(ex.getMessage());
        }
    }

    /**
     * The outbound constructor.
     *
     * @param p0 The Call object
     */
    public ProtocolControlFrame(Call p0) {
        super(p0);
        this._frametype = FullFrame.IAXCONTROL;
    }

    /**
     * ack is called to send any required response.
     * Switch on the type of message just received.
     */
    void ack() {
        logInbound();
        switch (this._subclass) {
            case PING:
                sendPong();
                break;
            case PONG:
                Log.debug("Sending Ack frame");
                sendAck();
                _call.setPong(true);
                break;
            case ACK:
                break;
            case AUTHREQ:
                sendAuthRep();
                break;
            case REGAUTH:
                if (_call.isForReg()) {
                    sendRegReq();
                }
                else {
                    sendRegRel();
                }
                break;
            case REGACK:
                Log.debug("Sending Ack frame");
                sendAck();
                if (_call.isForReg()) {
                    _call.setRegistered(true);
                }
                else {
                    _call.setRegistered(false);
                }
                break;
            case REGREJ:
                Log.debug("Sending Ack frame");
                sendAck();
                _call.setRegistered(false);
                break;
            case TXREQ:
                Log.debug("Sending TXREJ frame");
                sendTxrej();
                break;
            case INVAL:
                sendAck();
                _call.removeSelf();
                break;
            case LAGRQ:
                Log.debug("Sending lag reply");
                sendLagReply();
                break;
            case ACCEPT:
                Log.debug("Sending Ack frame");
                sendAck();
                _call.setAudioFormat(_iep.format);
                _call.setAccepted(true);
                _call.setRno(new Character( (char) (0xffff & this._sCall)));

                //set Ringing ?
                break;
            case REJECT:
                if ( (_iep != null) && (_iep.cause != null)) {
                    Log.warn("Cause " + _iep.cause);
                }
                Log.debug("Sending Ack frame");
                sendAck();
                _call.setAccepted(false);
                break;
            case HANGUP:
                Log.debug("Sending Ack frame");
                sendAck();
                int val = 0;
                if (_iep.causecode != null) {
                    val = _iep.causecode.intValue();
                }
                _call.hungup(val);
                break;
            case NEW:
                Log.debug("Got New");
                sendAcceptOrRej();
                break;
            default:
                Log.warn("Unhandled PCF");
                break;
        }
    }

    /**
     * sendTxrej
     */
    private void sendTxrej() {
        ProtocolControlFrame rej = mkAck(this.TXREJ);
        InfoElement nip = new InfoElement();
        nip.causecode = new Integer(29);
        nip.cause = "Facility rejected";
        rej.sendMe(nip);
    }

    /**
     * Sends a hangup (HANGUP).
     */
    public void sendHangup() {
        _sCall = _call.getLno().charValue();
        _dCall = _call.getRno().charValue();
        _iseq = _call.getIseq();
        _oseq = _call.getOseqInc();
        _subclass = this.HANGUP;

        InfoElement ie = new InfoElement();
        ie.causecode = new Integer(16); // Normal call clearing
        Log.warn("Sending Hangup");
        sendMe(ie);
    }

    /**
     * Sends the initial registration request for the specified
     * username with the refresh time (REGREQ).
     *
     * @param username The username
     * @param refresh The number of seconds before the registration
     *      expires
     */
    void sendRegReq(String username, int refresh) {
        _sCall = _call.getLno().charValue();
        _dCall = 0;
        _iseq = _call.getIseq();
        _oseq = _call.getOseqInc();
        _subclass = this.REGREQ;

        InfoElement ie = new InfoElement();
        ie.username = username;
        ie.refresh = new Integer(refresh);
        Log.debug("Sending initial RegRequest");
        sendMe(ie);
    }

    /**
     * Sends the complete registration request with authentication
     * credentials (REGREQ).
     */
    void sendRegReq() {
        ProtocolControlFrame ack = mkAck(this.REGREQ);
        String p = _call.getPassword();
        InfoElement nip = new InfoElement();
        buildAuthInfoElements(_iep, nip, p);
        Log.debug("Sending completed RegRequest");
        ack.sendMe(nip);
    }

    /**
     * Sends the initial registration release for the specified
     * username (REGREL).
     *
     * @param username The username
     */
    void sendRegRel(String username) {
        _sCall = _call.getLno().charValue();
        _dCall = 0;
        _iseq = _call.getIseq();
        _oseq = _call.getOseqInc();
        _subclass = this.REGREL;

        InfoElement ie = new InfoElement();
        ie.username = username;
        Log.debug("Sending initial RegRelease");
        sendMe(ie);
    }

    /**
     * Sends the complete registration release with authentication
     * credentials (REGREL).
     */
    void sendRegRel() {
        ProtocolControlFrame ack = mkAck(this.REGREL);
        String p = _call.getPassword();
        InfoElement nip = new InfoElement();
        buildAuthInfoElements(_iep, nip, p);
        Log.debug("Sending completed RegRelease");
        ack.sendMe(nip);
    }

    /**
     * Sends a POKE message to test connectivity of a remote IAX peer.
     *
     * <p>
     * It MUST be sent when there is no existing call to the remote
     * endpoint. It MAY also be used to "qualify" a user to a remote
     * peer, so that the remote peer can maintain awareness of the state
     * of the user. A POKE MUST have 0 as its destination call number.
     * <br/>
     * <br/>
     * Upon receiving a POKE message, the peer SHOULD respond with a PONG message.
     * </p>
     */
    void sendPoke() {
        _sCall = _call.getLno().charValue();
        _dCall = 0;
        _iseq = _call.getIseq();
        _oseq = _call.getOseqInc();
        _subclass = this.POKE;

        Log.debug("Sending Poke");
        // POKE has no IE
        sendMe( (InfoElement)null);
    }

    /**
     * Logs message.
     *
     * @param inout Additional text to log.
     */
    protected void log(String inout) {
        StringBuffer bu = new StringBuffer();
        bu.append(inout).append(", PCF subClass: ");
        if (_subclass < controlText.length) {
            bu.append(controlText[_subclass]);
        }
        super.log(bu.toString());
    }

    // Birgit: Why not remove this. It does less than its parent class.
    /**
     * arrived is called when a packet arrives. This method is
     * empty.
     *
     * @throws IAX2ProtocolException
     */
    void arrived() throws IAX2ProtocolException {
    }

    /**
     * Sends this object with the specified IE.
     *
     * @param ie The IE
     */
    void sendMe(InfoElement ie) {
        ByteBuffer buff = ByteBuffer.allocate(2048);
        buff.putChar( (char) (0x8000 | _sCall));
        int rd = _dCall;
        if (_retry) {
            rd |= 0x8000;
        }
        buff.putChar( (char) rd);
        long tst = this.getTimestampVal();
        tst = ( (0x100000000L & tst) > 0) ? tst - 0x100000000L : tst;
        buff.putInt( (int) tst);
        buff.put( (byte) _oseq);
        buff.put( (byte) _iseq);
        buff.put( (byte) _frametype);
        int sc = _subclass;
        if (_cbit) {
            sc |= 0x80;
        }
        buff.put( (byte) sc);
        if (ie != null) {
            ie.update(buff);
        }
        sendAndStore(buff);
    }

    /**
     * Commit this frame. This method is called when a packet we sent
     * has been acked.
     *
     * @param ack The ack frame sent to us
     */
    void commit(FullFrame ack) {
        switch (this._subclass) {
            case HANGUP:
            case REJECT:
            case INVAL:

                // we want to drop the call.
                if (_call != null) {
                    _call.removeSelf();
                }
                break;
            case ACCEPT:
                _call.acceptedCall();
                break;
        }
    }

    /**
     * Sends an accept or a reject frame. This method is called by
     * ack().
     *
     * @see #ack()
     */
    private void sendAcceptOrRej() {
        int match = 0;
        Boolean will = null;
        AudioInterface a = _call.getAudioFace();
        int can = a.supportedCodecs().intValue();
        String cause = "Congestion";
        String ourprefs = a.codecPrefString();
        byte[] theirprefs = _iep.codec_prefs;
        if (theirprefs == null) {
            // fake them using capability
            Log.debug("no codec IE");
            Integer cap = _iep.capability;
            if (cap != null) {
                Log.debug("no capability IE");
                String caps = "";
                int cbits = cap.intValue();
                for (int i = 0; i < 20; i++) {
                    if ( (cbits & (1 << i)) > 0) {
                        caps += (char) (56 + i);
                    }
                }
                theirprefs = caps.getBytes();
            }
        }

        if (theirprefs != null) {
            Log.debug("Looking for a matching codec");
            Log.debug("ours = " + ourprefs);
            Log.debug("theirs = " + new String(theirprefs));
            byte[] cpr = theirprefs;
            byte[] want = ourprefs.getBytes();
            for (int j = 0; j < want.length; j++) {
                for (int i = 0; i < cpr.length; i++) {
                    if (want[j] == cpr[i]) {
                        match = 1 << (cpr[i] - 66);
                        Log.debug("found codec match " + (char) want[j] +
                                  " -> " + match);
                        break;
                    }
                    else {
                        Log.verb("codec option " + (char) want[j] + " != " +
                                 (char) cpr[i]);
                    }
                }
                if (match != 0) {
                    break;
                }
            }
        }

        if (match != 0) {
            _call.setAudioFormat(new Integer(match));
            will = _call.newCallFrom(_iep.callingNo, _iep.calledNo,
                                     _iep.callingName,
                                     _iep.username);

        }
        else {
            will = Boolean.FALSE;
            cause = "No compatible codec.";
            Log.debug("codec cap was " + _iep.capability);
            Log.debug("but codec pref was " + new String(_iep.codec_prefs));
        }
        if (will != null) {
            if (will.booleanValue()) {
                ProtocolControlFrame acc = mkAck(this.ACCEPT);
                InfoElement ie = new InfoElement();
                ie.format = new Integer(match);
                acc.sendMe(ie);
                Log.debug("we acc'd call format = " + match);
            }
            else {
                ProtocolControlFrame rej = mkAck(this.REJECT);
                InfoElement ie = new InfoElement();
                ie.cause = cause;
                Log.warn("we rejected call because = " + cause);
                rej.sendMe(ie); // teardown ?
            }
        } // otherwise ignore it - we have seen it before.
    }

    /**
     * Logs the fact we received in incoming frame.
     */
    private void logInbound() {
        log("got");
    }

    /**
     * Builds authentication IE. This method is called by sendAuthRep
     * and sendRegReq.
     *
     * @param iep The original IE
     * @param nip The new IE
     * @param pass The password
     * @see #sendAuthRep
     * @see #sendRegReq
     */
    private void buildAuthInfoElements(InfoElement iep, InfoElement nip,
                                       String pass) {
        nip.username = iep.username;
        nip.refresh = nip.refresh;

        if (_iep.authmethods != null) {

            int model = iep.authmethods.intValue();
            if ( (model & 2) > 0) {

                MD5Digest md = new MD5Digest();
                byte[] by = _iep.challenge.getBytes();
                md.update(by, 0, by.length);
                by = pass.getBytes();
                md.update(by, 0, by.length);
                byte[] resp = new byte[16];
                md.doFinal(resp, 0);
                String p = Binder.enHex(resp, (Character)null);
                nip.md5Result = p;
                // do md5
            }
            else if ( (model & 1) > 0) {
                // do plaintext
                nip.md5Result = pass;
            }
        }
    }

    /**
     * Sends an authentication reply with authentication credentials
     * (AUTHREP).
     *
     * @see #ack()
     */
    private void sendAuthRep() {
        if (_iep != null) {
            String p = _call.getPassword();
            InfoElement nip = new InfoElement();
            buildAuthInfoElements(_iep, nip, p);
            ProtocolControlFrame ack = mkAck(this.AUTHREP);
            Log.debug("Sending complete AUTHREP");
            ack.sendMe(nip);
            //dump();

        }
    }

    /**
     * Sends a lag reply (LAGRP).
     *
     * @see #ack()
     */
    private void sendLagReply() {
        ProtocolControlFrame ack = mkAck(LAGRP);
        Log.debug("Sending LagReply");
        ack.sendMe( (InfoElement)null);

    }

    /**
     * Sends a pong (PONG) as response to a PING.
     *
     * @see #ack()
     */
    private void sendPong() {
        ProtocolControlFrame pong = mkAck(PONG);
        Log.debug("Sending Pong");
        pong.sendMe( (InfoElement)null);
    }

}
