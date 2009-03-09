// NAME
//      $RCSfile: Call.java,v $
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

import java.io.*;
import org.asteriskjava.iax.util.*;
import java.util.*;

import org.asteriskjava.iax.audio.*;

/**
 * Call deals with all the packets that are part of a specific call (or
 * registration).
 *
 * @author <a href="mailto:thp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision$ $Date$
 */
/*
   The thing to remember is that a _received_ message
   contains fields with the _senders_ viewpoint
   so _source_ is the far end and _dest_ is us.
   in the reply the oposite is true :
   _source_ is us and _dest_ is them.
 */
public class Call
    implements Runnable {

    private final static String version_id =
        "@(#)$Id$ Copyright Mexuar Technologies Ltd";

    // Represent the 'Source Call Number' and 'Destination Call Number'
    // in the FullFrame header. They uniquely identify the call between
    // two parties.
    // Who's who depends on the directions:
    // outgoing:
    // _lno = Source Call Number
    // _rno = Destination Call Number
    private Character _lno;
    private Character _rno;

    /**
     * Our peer.
     */
    protected Friend _peer;
    /**
     * The queue of incoming frames.
     */
    // JDK 1.5 only: protected Vector<byte[]> _frameQueue;
    protected Vector _frameQueue;

    /**
     * The receiving process thread.
     */
    protected Thread _process;

    private int _oseq = 0;
    private int _iseq = 0;
    private int _ackedTo = 0;
    private boolean _done = false;
    private long _startStamp = 0;
    private String _password;
    private boolean _registered;

    // The CALLED NUMBER, CALLING NUMBER and CALLING NAME for the IE
    private String _farNo = null;
    private String _nearNo = null;
    private String _farName = null;
    private String _nearName = null;

    private boolean _accepted;
    private AudioSender _say;
    private boolean _answered;
    private Thread _retry;
    private String _username;
    private int _refresh = 60;
    private boolean _callIsInbound;
    private FullFrame[] _outbound = new FullFrame[256];
    private boolean _forReg = false;
    private boolean _forUnReg = false;
    private AudioInterface _audio;
    private int _format;
    private boolean _recvdFirstAudioFrame = false;
    private boolean _timedout = false;
    private int _hungupCauseCode;

    private long _stampTopWord = 0;
    private long _oldStamp = 0;

    /**
     * The outbound constructor for Call. We know nothing except where
     * to send it.
     *
     * @param source Our peer
     * @see #startRcv
     */
    // Birgit, is it really only outbound?
    public Call(Friend source) {
        _peer = source;
        _callIsInbound = false;
        _lno = _peer.getFreeCallNo();
        this.startRcv();
    }

    /**
     * The constructor for Call.
     *
     * @param friend Our peer
     * @param forRegister If this is a registration call (true) or not (false)
     * @param forUnregister If this is a unregistration call (true) or not (false)
     */
    public Call(Friend friend, boolean forRegister, boolean forUnregister) {
        this(friend);
        _forReg = forRegister;
        _forUnReg = forUnregister;
    }

    /**
     * Returns if this a registration call.
     *
     * @return True if a registration call, false if not
     */
    public boolean isForReg() {
        return _forReg;
    }

    /**
     * Returns if this a unregistration call.
     *
     * @return True if a unregistration call, false if not
     */
    public boolean isForUnReg() {
        return _forUnReg;
    }

    /**
     * Sets if this is an inbound or outbound call.
     *
     * @param b True if inbound, false if outbound
     */
    public void setIsInbound(boolean b) {
        _callIsInbound = b;
    }

    /**
     * Returns if this is an inbound Call or outbound
     *
     * @return True if inbound, false if outbound
     */
    public boolean getIsInbound() {
        return _callIsInbound;
    }

    /**
     * This method starts the bi-directional datastream. It kicks off
     * the process thread (see run()) for inbound frames and a
     * retry thread for outbound frames.
     *
     * @see #run()
     */
    private synchronized void startRcv() {
        // JDK 1.5 only _frameQueue = new Vector<byte[]>();
        _frameQueue = new Vector();
        _process = new Thread(this, "call-" + (int) _lno.charValue() + "-rcv");
        _process.setPriority(Thread.MAX_PRIORITY - 1);
//        _process.setDaemon(true);

        resetClock();
        // Birgit: Tim mentioned using TimerTask to resend
        Runnable retry = new Runnable() {
            public void run() {
                while (!_done) {
                    resendUnacked();
                    try {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException ex) {
                        ; // dont care
                    }
                }
            }
        };
        _retry = new Thread(retry, "call-" + (int) _lno.charValue() + "-retry");
        _retry.setPriority(Thread.MIN_PRIORITY);
//        _retry.setDaemon(true);
        _process.start();
        _retry.start();
    }

    /**
     * Adds an incoming frame (as bytes) to the frame queue. We are
     * still on the recv thread; this data was received by binder and
     * passed to us via friend. The frames are stored in the queue and
     * we deal with them on our own thread to relief the recv thread.
     * In other words, this is the last thing we do on the recv thread!
     *
     * @param data The frame
     * @see #run()
     */
    public synchronized void addFrame(byte[] data) {
        _frameQueue.addElement(data);
        this.notifyAll();
    }

    /**
     * Manages the incoming frames stored in the frame queue.
     * This thread is started by startRcv() and run separately from the
     * binder's recv thread.
     *
     * @see #startRcv()
     * @todo sort the frames into sequence before we dispose of them
     */
    public void run() {
        while (!_done) {
            Object[] frames = new Object[0];
            synchronized (this) {
                try {
                    this.wait();
                }
                catch (InterruptedException ex) {
                    ; // don't care
                }
                int sz;
                // do some smart stuff here? - limit the take to
                // only 20 frames ?
                // you'd hope that normally we'd get 1 or maybe
                // 2 frames here.
                if ( (sz = _frameQueue.size()) > 0) {
                    frames = new Object[sz];
                    for (int i = 0; i < sz; i++) {
                        frames[i] = _frameQueue.elementAt(i);
                    }
                    _frameQueue.removeAllElements();
                }
            }
            // now released the lock so let's deal with the list.
            // we are now on the thread of the call, any time we waste is
            // our own.
            // should really sort these into sequence before we dispose of them.
            if (frames.length > 1) {
                // sort them ...
            }
            for (int i = 0; i < frames.length; i++) {
                try {
                    frameDeal( (byte[]) frames[i]);
                }
                catch (Throwable ex1) {
                    ex1.printStackTrace();
                }
            }
        }

        // clean up here ....
        Log.debug("In call Clean up");

        if (_audio != null) {
            _audio.stopPlay();
            _audio.stopRec();
            _audio = null;

        }

    }

    /**
     * Deal with newly received frame bytes. This method turns them
     * into a Full or Mini frame, deal with internal counters, sends an
     * acknowledgement and notifies the frame it has arrived.
     *
     * @param bs byte[]
     * @exception IAX2ProtocolException Thrown by Frame.arrived()
     *
     * @see #impliedAck
     * @see Frame#arrived
     */
    void frameDeal(byte[] bs) throws IAX2ProtocolException {
        Frame f = null;
        if (bs[0] < 0) {
            // full frame
            FullFrame ff = FullFrame.create(this, bs);
            impliedAck(ff);
            if (!ff.isAck()) {
                f = addIn(ff);
            }
            else {
                Log.debug("ignoring oseq in ack");
            }
            ff.ack();
        }
        else {
            f = new MiniFrame(this, bs);
            //Log.warn("Mini Frame");
        }
        if (f != null) {
            f.arrived();
        }
    }

    /**
     * Handles the implied Ack.
     * Any full frame constitutes an ack of sorts. The
     * _iseq in an inbound message is (1+ our _oseq) for the last message
     * they got. So we run through the ring from the last highwater
     * mark removing outbound frames.
     *
     * @param ff The last full frame we received
     */
    private synchronized void impliedAck(FullFrame ff) {
        int happyto = ff._iseq;
        if (happyto < _ackedTo) {
            Log.debug("ack wrap " + happyto + " < " + _ackedTo);
            for (int i = _ackedTo; i < 256; i++) {
                ackIt(i, ff);
            }
            for (int i = 0; i < happyto; i++) {
                ackIt(i, ff);
            }
        }
        for (int i = _ackedTo; i < happyto; i++) {
            ackIt(i, ff);
        }
        _ackedTo = happyto;
    }

    /**
     * We received an implied or explicit ack to the frame we send.
     * Commit it and remove from our outbound array.
     *
     * @param i The index of the frame in the outbound array
     * @param ack The frame that is acked
     */
    private void ackIt(int i, FullFrame ack) {
        if (_outbound[i] != null) {
            _outbound[i].commit(ack);
            _outbound[i] = null;
        }
    }

    /**
     * Generates a new outbound stream sequence number, oSeqno.
     *
     * @return A new outbound stream sequence number
     */
    synchronized int getOseqInc() {
        // look at this .....
        int nos = _oseq++;
        nos = nos % 256;
        return nos;
    }

    /**
     * Returns the outbound stream sequence number, oSeqno.
     *
     * @return The outbound stream sequence number
     */
    synchronized int getOseq() {
        return _oseq;
    }

    /**
     * Returns the inbound stream sequence number, iSeqNo.
     *
     * @return The inbound stream sequence number
     */
    int getIseq() {
        return _iseq;
    }

    /**
     * Sets the inbound stream sequence number, iSeqNo.
     *
     * @param next The inbound stream sequence number
     */
    void setIseq(int next) {
        _iseq = next % 256;
    }

    /**
     * Sends a frame to our peer.
     *
     * @param bs The frame (in bytes)
     * @see Friend#send(ByteBuffer)
     */
    public void send(ByteBuffer bs) {
        if (!_done) {
            _peer.send(bs);
        }
    }

    /**
     * Starts a new outbound call.
     *
     * @param username Username (peer or user) for authentication
     * @param password Password for authentication
     * @param calledNo Number/extension to call
     * @param callingNo Number/extension we call from
     * @param callingName Name of the person calling
     *
     */
    public void newCall(String username, String password, String calledNo,
                        String callingNo, String callingName) {
        // it is a _new_ call so reset the clock.
        resetClock();
        ProtocolControlFrameNew cal = new ProtocolControlFrameNew(this);
        _password = password;
        _farNo = calledNo;
        _nearNo = callingNo;
        _nearName = callingName;
        cal.sendNew(_lno, username, calledNo, callingNo, callingName);
    }

    /**
     * Returns the password for authentication
     *
     * @return The password
     */
    String getPassword() {
        return _password;
    }

    /**
     * Returns the timestamp of this call. This is the number of
     * milliseconds since the call started.
     *
     * @return The timestamp
     */
    public int getTimestamp() {
        long now = System.currentTimeMillis();
        return (int) (now - _startStamp);
    }

    /**
     * Removes this call because it has finished or torn down.
     */
    public synchronized void removeSelf() {
        Log.debug("Removing call " + (int) _lno.charValue());
        _peer.removeCall(this);
        _done = true;
        this.notifyAll();
        if (_audio != null) {
            _audio.stopPlay();
            _audio.stopRinging();
            _audio.stopRec();
        }
    }

    /**
     * Sets if this (outbound) call is registered or not. This will be
     * called if REGACK is received from the other end or a REGREJ.
     *
     * @param b True if registered, false if not.
     */
    void setRegistered(boolean b) {
        _registered = b;
        this._peer.setRegistered(b);
        Log.debug("Call registered = " + b);
    }

    /**
     * Sets if this (outbound) call is accepted or not.  This bit of
     * information comes in with a received ACCEPT or REJECT.
     *
     * @param b True if accepted, false it not
     */
    void setAccepted(boolean b) {
        _accepted = b;
    }

    /**
     * Starts sending our audio recording. This method creates a new
     * AudioSender object to do that.
     *
     * @see #setAnswered(boolean)
     * @see AudioSender#AudioSender(AudioInterface, Call)
     */
    private void startAudioRec() {
        _say = new AudioSender(_audio, this);
        _audio.startRec();
        _audio.setAudioSender(_say);
    }

    /**
     * Sets if this call is answered.
     * This can either be when we receive a ANSWER frame from our peer
     * to an outbound call, or when we answer an incoming call ourselves.
     *
     * @param b True is answered, false if not
     */
    void setAnswered(boolean b) {
        if (!_answered && b) {
            _audio.stopRinging();
            startAudioRec();
        }
        _answered = b;
        _peer.setAnswered(this);
    }

    public int getHungupCauseCode() {
        return _hungupCauseCode;
    }

    /**
     * Our peer has hung up. We'll remove ourself.
     * This bit of information comes in with a received HANGUP frame.
     *
     * @see #removeSelf()
     * @see #hangup
     */
    void hungup(int causecode) {
        _hungupCauseCode = causecode;
        removeSelf();
    }

    /**
     * Writes audio to the speaker.
     *
     * @param bs The incoming audio
     * @param ts The timestap
     * @exception IOException Description of Exception
     *
     * @see VoiceFrame#arrived()
     * @see MiniFrame#arrived()
     */
    public void audioWrite(byte[] bs, long ts) throws IOException {
        if (_audio != null) {
            long stamp = _stampTopWord + ts;
            if ( (stamp < _oldStamp) && (50000 < (_oldStamp - stamp))) {
                Log.debug("Wrapped timestamp on rcv");
                _stampTopWord += 0x10000;
                stamp = _stampTopWord + ts;
                Log.debug("New timestamp top bits are " + _stampTopWord);
            }
            _oldStamp = stamp;
            _audio.write(bs, stamp);
        }
    }

    /**
     * Notifies us that a VOICE FullFrame has been received.
     *
     * @see VoiceFrame#arrived()
     */
    public void fullVoiceFrameRcvd(long stamp) {
        _stampTopWord = stamp & 0xffff0000;
        Log.debug("New timestamp top bits are " + _stampTopWord);

        if ( (_audio != null) && (!_recvdFirstAudioFrame)) {
            _recvdFirstAudioFrame = true;
            _audio.stopRinging();
        }
    }

    /**
     * Adds a full frame to the outbound buffer that hasn't been
     * acknowledges yet. This is done so it can be resend when an
     * acknowledgement doesn't arrive (in time).
     *
     * @param outb FullFrame
     */
    synchronized void addUnacked(FullFrame outb) {
        int where = outb._oseq;
        _outbound[where] = outb;
    }

    /**
     * Returns the local call number. Together with the remote call
     * number, they uniquely identify the call between two parties.
     *
     * On an outgoing call this represents 'Source Call Number',
     * on an incoming call this represents 'Destination Call Number'.
     *
     * @return The local call number
     */
    public Character getLno() {
        return this._lno;
    }

    /**
     * Returns the remote call number. Together with the local call
     * number, they uniquely identify the call between two parties.
     *
     * On an outgoing call this represents 'Destination Call Number',
     * on an ingoing call this represents 'Source Call Number'.
     *
     * @return The remote call number
     */
    public Character getRno() {
        return _rno;
    }

    /**
     * Returns the timestamp.
     *
     * @return The timestamp
     */
    public long getStartTimestamp() {
        return this._startStamp;
    }

    /**
     * Sets the remote call number as a character.  This bit of
     * information comes in with a received ACCEPT.
     *
     * @param d Character
     * @see #getRno
     */
    public void setRno(Character d) {
        _rno = d;
    }

    /**
     * Resets the clock. This method sets the start timestamp of a
     * new call.
     *
     * @see ProtocolControlFrameNew#ProtocolControlFrameNew(Call)
     */
    void resetClock() {
        _startStamp = System.currentTimeMillis();
    }

    /**
     * Sets the username and password for authentication
     *
     * @param username The username
     * @param password The password
     */
    public void setUnameNpass(String username, String password) {
        _password = password;
        _username = username;
    }

    /**
     * Sends a register request and let the rest flow from there.
     */
    void register() {
        ProtocolControlFrame regreq = new ProtocolControlFrame(this);
        _refresh = 60;
        regreq.sendRegReq(_username, _refresh);
    }

    /**
     * Sends a unregister request and let the rest flow from there.
     */
    void unregister() {
        ProtocolControlFrame regrel = new ProtocolControlFrame(this);
        regrel.sendRegRel(_username);
    }

    /**
     * Sends a poke message to test connectivity of a remote IAX peer.
     */
    void sendPoke() {
        ProtocolControlFrame pokereq = new ProtocolControlFrame(this);
        resetClock();
        pokereq.sendPoke();
    }

    /**
     * Sets if we received a pong to our poke (or ping).
     *
     * @param b True if we received pong, false otherwise
     */
    void setPong(boolean b) {
        int roundtrip = getTimestamp();
        _peer.setPong(b, roundtrip);
    }

    /**
     * Resends the unacknowledged frames, that haven't expired their
     * maximum number of retries.
     * If an unacknowledged frame cannot be resend anymore (i.e. it has
     * been sent max retries), we'll tear down the connection.
     *
     * <p>
     * Draft - 7. Message Transport: <br/>
     * If no acknowledgment is received after a locally configured
     * number of retries, default 4, the call leg SHOULD be considered
     * unusable and the call MUST be torn down without any further
     * interaction on this call leg.
     * </p>
     */
    void resendUnacked() {
        int osq = this._oseq;
        if (osq < _ackedTo) {
            osq = 256 + osq;
        }
        boolean timedout = false;
        for (int i = this._ackedTo; i <= this._oseq; i++) {
            int n = i % 256;
            FullFrame ff = _outbound[n];
            if (ff != null) {
                if (!ff.resendMe()) {
                    ff.log("outbound has max retries - timing out");
                    _outbound[n] = null;
                    timedout = true;
                    break;
                }
            }
        }
        if (timedout == true) {
            _timedout = true;
            removeSelf();
        }
    }

    /**
     * Returns if this call has timed out. A call times out when it
     * doesn't receive an ack on time.
     *
     * @return True if timed out, false if not timed out.
     */
    boolean isTimedout() {
        return _timedout;
    }

    /**
     * Passed a newly arrived frame. If the frame is the next one we
     * are expecting, then put it in the buffer and adjust our
     * expectations. Return it, so it can be acted upon. If it isn't
     * the next expected then ignore it and return null (Warn).
     *
     * @param ff FullFrame
     * @return FullFrame
     */
    synchronized FullFrame addIn(FullFrame ff) {
        FullFrame ret = null;
        int where = ff._oseq;
        int expected = this.getIseq();
        if (expected == where) {
            setIseq(++where);
            ret = ff;
        }
        return ret;
    }

    /**
     * Our NEW frame is acknowledged. Tell our Friend object.
     *
     * @param ack ProtocolControlFrame
     */
    void gotAckToNew(FullFrame ack) {
        if (this._rno == null) {
            _rno = new Character( (char) (0xffff & ack._sCall));
        }
        Log.debug("Setting rno = " + (int) (_rno.charValue()));
        _peer.gotAckToNew(this);
    }

    /**
     * Sends a DTMF character
     *
     * @param c The DTMF character
     * @see DtmfFrame#DtmfFrame(Call, char)
     */
    public void sendDTMF(char c) {
        DtmfFrame dtmf = new DtmfFrame(this, c);
    }

    /**
     * Returns the status string
     *
     * @return The status
     */
    public String getStatus() {
        String ret = "Call " + (this._callIsInbound ? " from " : " to ") +
            this._farNo;
        ret += _answered ? " connected " : " in progress ";
        return ret;
    }

    /**
     * We hang up this call. Send a HANGUP to our peer.
     *
     * @see #hungup
     */
    public void hangup() {
        ProtocolControlFrame cal = new ProtocolControlFrame(this);
        cal.sendHangup();
    }

    /**
     * Received a new inbound call. Returns whether or not we accept it.
     *
     * @param callingNo The peer's calling number
     * @param calledNo The peer's called number
     * @param callingName The peer's calling name
     * @param username The peer's username for authentication
     *
     * @return True if we will accept, false if we reject
     */
    public Boolean newCallFrom(String callingNo, String calledNo,
                               String callingName,
                               String username) {
        Boolean ret = null;
        Log.debug("Call.newCallFrom: calledNo=" + calledNo
                  + ", callingNo=" + callingNo
                  + ", callingName=" + callingName
                  + ", username=" + username);
        if (_farNo == null) {
            // first time we have seen this one.
            _farNo = callingNo;
            _farName = callingName;
            _nearNo = calledNo;
            this._callIsInbound = true;
            ret = new Boolean(_peer.willAccept(this));
        }
        return ret;
    }

    /**
     * We had an ack to our accept. Make our Friend object tell its gui.
     */
    void acceptedCall() {
        _peer.tellGuiNewCall(this);
    }

    /**
     * Returns if this call has been answered.
     *
     * @return True is answered, false if not
     */
    public boolean isAnswered() {
        return this._answered;
    }

    /**
     * Answers an incoming call.
     */
    public void answer() {
        ControlFrame ans = new ControlFrame(this);
        ans.sendAnswer();
        this.setAnswered(true);
    }

    /**
     * Returns the far number.
     *
     * On an outgoing call this represents 'CALLED NUMBER' IE,
     * on an incoming call this represents 'CALLING NUMBER' IE.
     *
     * @return The far number
     */
    public String getFarNo() {
        return this._farNo;
    }

    /**
     * Returns the near number.
     *
     * On an outgoing call this represents 'CALLING NUMBER' IE,
     * on an incoming call this represents 'CALLED NUMBER' IE.
     *
     * @return The near number
     */
    public String getNearNo() {
        return this._nearNo;
    }

    /**
     * Returns the far name.
     *
     * On an incoming call this represents 'CALLING NAME' IE,
     * on an outgoing call will be null.
     *
     * @return The far name
     */
    public String getFarName() {
        return _farName;
    }

    /**
     * Returns the near name.
     *
     * On an outgoing call this represents 'CALLING NAME' IE,
     * on an incoming call this will be null.
     *
     * @return The near name
     */
    public String getNearName() {
        return _nearName;
    }

    /**
     * Make a ringing noice.
     */
    void setRinging() {
        if (!_recvdFirstAudioFrame) {
            _audio.startRinging();
        }
        _peer.gotRinging(this);
        Log.debug("going to make a remote ringing noise...!");
    }

    /**
     * Sets the accepted audio format. This bit of information comes in
     * with a received ACCEPT.
     *
     * @param format The audio format
     */
    void setAudioFormat(Integer format) {
        AudioInterface base = _peer.getAudioFace();
        _audio = base.getByFormat(format);
        if (_audio != null) {
            _format = _audio.getFormatBit();
            Log.debug("using audio format = " + _format);
        }
        else {
            Log.warn("cant set audio format " + format);
        }
    }

    /**
     * Returns the frame size. This is base on the audio sample size.
     *
     * @return the frame size
     */
    int getFrameSz() {
        int ret = 0;
        if (_audio != null) {
            ret = _audio.getSampSz();
        }
        return ret;
    }

    /**
     * getAudioFace
     *
     * @return AudioInterface
     */
    AudioInterface getAudioFace() {
        return _peer.getAudioFace();
    }

}
