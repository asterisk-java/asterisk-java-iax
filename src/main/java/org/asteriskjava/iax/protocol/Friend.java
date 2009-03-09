// NAME
//      $RCSfile: Friend.java,v $
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

//import java.net.*;
import org.asteriskjava.iax.util.*;
import java.util.*;

import org.asteriskjava.iax.audio.*;

/**
 * Friend deals with packets to and from a specific IP address, which
 * should be an asterisk server.
 * If you register() with the PBX, then it acts as friend, until then
 * it is a user.
 *
 * It manages calls and distribution of frames to the PBX.
 *
 * @author <a href="mailto:thp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision$ $Date$
 */
public class Friend extends java.util.TimerTask {

    private final static String version_id =
            "@(#)$Id$ Copyright Mexuar Technologies Ltd";

    // JDK 1.5 only: private Hashtable<Character, Call> _scalls = new Hashtable<Character, Call>(20);
    private Hashtable _scalls = new Hashtable(20);

    // JDK 1.5 only: private Hashtable<Character, Call> _awaitingAck = new Hashtable<Character, Call>(3);
    private Hashtable _awaitingAck = new Hashtable(3);

    private String _iad;
    private Binder _binder;
    private char _cno = 0;
    private Timer _regTick;
    private boolean _registered = false;
    private boolean _hostReachable;
    private int _hostReachableRoundTrip;
    private Call _regcall;
    private Call _pokecall;
    private String _username;
    private String _password;
    private ProtocolEventListener _gui;


    /**
     * Constructor for the Friend object
     *
     * @param bi The associated binder object
     * @param them The asterisk host address
     */
    public Friend(Binder bi, String them) {
        _binder = bi;
        long now = System.currentTimeMillis();
        _cno = (char) ((now & 0x7fff00) >> 8);
        _iad = them;
        _regTick = new Timer();
        Log.debug("Friend " + _iad);
    }


    /**
     * Constructor for the Friend object
     *
     * @param bi The associated binder object
     * @param them The asterisk host address
     * @param ear The protocal event listener
     */
    public Friend(Binder bi, String them, ProtocolEventListener ear) {
        this(bi, them);
        _gui = ear;
    }


    /**
     * Find the call object, based on _our_ number.
     * <ul>
     *    <li> for an inbound frame this is dcall</li>
     *    <li> for an outbound frame (why would you care?)</li>
     *    <li> this is scall</li>
     *    <li> an inbound new will have 0</li>
     * </ul>
     *
     * @param theirno Character
     * @return Call
     */
    Call findCall(Character theirno) {
        Call ret = (Call) _scalls.get(theirno);
        return ret;
    }


    /**
     * stop
     */
    public void stop() {
        Call ca[] = {};
        int tot = 0;
        this.cancel();
        synchronized (_scalls) {
            Enumeration calls = _scalls.elements();
            tot = _scalls.size();
            ca = new Call[tot];
            int i = 0;
            while (calls.hasMoreElements()) {
                ca[i++] = (Call) calls.nextElement();
            }
        }
        for (int i = 0; i < tot; i++) {
            ca[i].removeSelf();
        }
        tot = 0;
        synchronized (this._awaitingAck) {
            Enumeration calls = _awaitingAck.elements();
            tot = _awaitingAck.size();
            ca = new Call[tot];
            int i = 0;
            while (calls.hasMoreElements()) {
                ca[i++] = (Call) calls.nextElement();
            }
        }
        for (int i = 0; i < tot; i++) {
            ca[i].removeSelf();
        }
    }


    /*
       Interesting cases are :
       1) inbound NEWs - only have the source call set, dCall=0.
       2) ACKs from outbound NEWs - have a sCall set we don't know,
       but dCall is one we know.
       3) miniframes - only sCall is set.
       So we _have to index on sCall, except in case 2 when we also need to
       look in a dCall list - should really check that it is only
       expecting this.
     */
    /**
     * Handle received data from our binder. Find the associated Call,
     * or if it is a new incoming call, create a new Call.
     *
     * @param data byte[]
     */
     public void recv(byte[] data) {
        /*
           we are still on the recv thread!
         */
        int scnum = (data[0] & 0x7f) << 8;
        scnum += ((0x7f & data[1]));
        scnum += (data[1] < 0 ? 128 : 0);
        Character sc = new Character((char) scnum);

        Call ca = findCall(sc);
        if (ca == null) {
            int dcnum = (data[2] & 0x7f) << 8;
            dcnum += ((0x7f & data[3]));
            dcnum += (data[3] < 0 ? 128 : 0);
            Character dc = new Character((char) dcnum);
            ca = findWaiting(dc);
            if ((ca == null) && (dcnum == 0)) {
                ca = new Call(this);
                ca.setIsInbound(true);
                ca.setRno(sc);
                addCall(ca);
            } else {
                Log.warn("Frame with non-zero dest and non-existing scall");
                Log.warn("source call no = " + (int) sc.charValue());
                Log.warn("dest call no = " + (int) dc.charValue());
            }
        }

        if (ca != null) {
            ca.addFrame(data);
            /*
               try {
                  ca.frameDeal(data);
               }
               catch (IAX2ProtocolException ex) {
                  Log.warn(ex.toString());
               }
             */
        }
    }


    /**
     * Tell our GuiEventSender we've got a new call.
     *
     * @param ca Call
     */
    void tellGuiNewCall(Call ca) {
        if ((ca != null) && (!ca.isForReg())) {
            ProtocolEventListener ges = _binder.getGuiEventSender(_gui);
            ges.newCall(ca);
        }
    }


    /**
     * Find the call that is waiting for an acknowledgement on ourno.
     *
     * @param ourno Our dCall number
     * @return The call
     */
    private Call findWaiting(Character ourno) {
        Call ret = (Call) _awaitingAck.get(ourno);
        return ret;
    }


    /**
     * Sends a frame via our binder to our peer.
     *
     * @param bs byte[]
     * @see Call#send(ByteBuffer)
     * @see Binder#send(String, ByteBuffer)
     */
    protected void send(ByteBuffer bs) {
        _binder.send(this._iad, bs);
    }


    /**
     * Generates a free call number.
     *
     * @return The free number.
     */
    protected synchronized Character getFreeCallNo() {
        if (_cno == 0) {
            _cno++;
        }
        return new Character(_cno++);
    }


    /**
     * Makes a new outgoing call.
     *
     * @param username Username (peer or user) for authentication
     * @param secret Password for authentication
     * @param calledNo Number/extension to call
     * @param callingNo Number/extension we call from
     * @param callingName Name of the person calling
     * @return a new call object
     */
    public Call newCall(String username, String secret, String calledNo,
                        String callingNo, String callingName) {
        Call ncall = new Call(this);
        addNewWaitingAck(ncall);
        ncall.newCall(username, secret, calledNo, callingNo, callingName);
        return ncall;
    }


    /**
     * Adds this call to the list of calls that are awaiting an
     * acknowledgement.
     *
     * @param ncall Call
     * @see #newCall(String, String, String, String, String)
     * @see #run()
     */
    private void addNewWaitingAck(Call ncall) {
        Character lno = ncall.getLno();
        _awaitingAck.put(lno, ncall);
    }


    /**
     * Our NEW frame is acknowledged. Tell our gui.
     *
     * @param ncall Desccom.mexuar.corraleta.util.ription of Parameter
     * @see #tellGuiNewCall(Call)
     */
    protected synchronized void gotAckToNew(Call ncall) {
        Character lno = ncall.getLno();
        addCall(ncall);
        if ((lno != null) && (_awaitingAck.containsKey(lno))) {
            _awaitingAck.remove(lno);
        }
        tellGuiNewCall(ncall);
    }


    /**
     * Adds a new call object to our list of calls.
     *
     * @param ncall Call
     *
     * @see #recv(byte[])
     * @see #gotAckToNew(Call)
     */
    protected synchronized void addCall(Call ncall) {
        Character rno = ncall.getRno();
        _scalls.put(rno, ncall);
    }


    /**
     * Removes this call because it has finished.
     *
     * @param ca The finished call
     */
    protected void removeCall(Call ca) {
        Character torem = null;
        torem = ca.getRno();
        if ((torem != null) && (_scalls.containsKey(torem))) {
            _scalls.remove(torem);
        }
        Character lno = ca.getLno();
        if ((lno != null) && (_awaitingAck.containsKey(lno))) {
            _awaitingAck.remove(lno);
        }
        tellGuiHungup(ca);
    }


    /**
     * Tells our Gui the call is finished or torn up.
     *
     * @param ca The finished call
     * @see #removeCall(Call)
     */
    private void tellGuiHungup(Call ca) {
        if (ca != null) {
            if (ca == _pokecall) {
                if (_pokecall.isTimedout() == true) {
                    // so setPong doesn't call _pokecall.removeSelf()
                    _pokecall = null;
                    setPong(false, -1);
                }
            }
            else if (!ca.isForReg() && !ca.isForUnReg()) {
                ProtocolEventListener ges = _binder.getGuiEventSender(_gui);
                ges.hungUp(ca);
            }
        }
    }


    /**
     * Registers this username and password.
     *
     * @param username The username for authentication
     * @param password The password for authentication
     * @see #setRegistered(boolean)
     */
    public void register(String username, String password) {
        _username = username;
        _password = password;

        if (_regTick != null) {
            _regTick.schedule(this, 20, 60000);
        }
    }


    /**
     * Are we registered?
     *
     */
    public boolean isRegistered() {
        return _registered;
    }


    /**
     * Unregisters this username and password.
     * @see #setRegistered(boolean)
     */
    public void unregister() {
        // start the process off by creating a special Call.
        // reuse the _regcall, so it can be release in the setRegistered
        // method
        if (_regcall == null) {
            if (_regTick != null) {
                _regTick.cancel();
            }

            _regcall = new Call(this, false, true);
            _regcall.setUnameNpass(_username, _password);
            this.addNewWaitingAck(_regcall);

            // now send a regRel
            _regcall.unregister();
        }
    }


    /**
     * Internal task to
     * send a registration call (update) on a regular basis.
     * Called by timer.
     */
    public void run() {
        // start the process off by creating a special Call.
        if (_regcall == null) {
            _regcall = new Call(this, true, false);
            _regcall.setUnameNpass(_username, _password);
            this.addNewWaitingAck(_regcall);
        }

        // now send a regReq - the process flow from that.
        _regcall.register();
    }


    /**
     * Sets if registered or not. This method is called by Call.
     *
     * @param ok True if registered, false if not
     *
     * @see Call#setRegistered(boolean)
     */
    protected void setRegistered(boolean ok) {
        _registered = ok;
        if (_gui != null) {
            ProtocolEventListener ges = this._binder.getGuiEventSender(_gui);
            ges.registered(this,ok);

        }
        if (_regcall != null) {
            _regcall.removeSelf();
            _regcall = null;
        }
        if (_registered == false) {
            _binder.removeFriend(_iad);
        }
    }


    /**
     * Checks the connectivity of the asterisk host by sending a poke
     * message.
     * There should only ever be one poke request in flight.
     *
     * @see #setPong(boolean, int)
     */
    public void checkHostReachable() {
        if (_pokecall == null) {
            _pokecall = new Call(this);
            this.addNewWaitingAck(_pokecall);
            _pokecall.sendPoke();
        }
    }

    /**
     * Sets if we received a pong to our poke (or ping).
     *
     * @param b True if we received pong, false otherwise
     * @param roundtrip The round trip (ms) of the request
     * @see #checkHostReachable()
     */
    protected void setPong(boolean b, int roundtrip) {
        _hostReachable = b;
        _hostReachableRoundTrip = roundtrip;
        Log.debug("setPong " + b);
        if (_gui != null) {
            ProtocolEventListener ges = _binder.getGuiEventSender(_gui);
            ges.setHostReachable(this, _hostReachable, _hostReachableRoundTrip);
        }
        if (_pokecall != null) {
            _pokecall.removeSelf();
            _pokecall = null;
        }
    }


    /**
     * The call is ringing. Tell the gui.
     *
     * @param ca The ringing call
     *
     * @see Call#setRinging()
     */
    void gotRinging(Call ca) {
        if ((ca != null) && (!ca.isForReg())) {
            ProtocolEventListener ges = _binder.getGuiEventSender(_gui);
            ges.ringing(ca);
        }
    }

    /**
     * Sets if this call is answered.
     * This can either be when we receive a ANSWER frame from our peer
     * to an outbound call, or when we answer an incoming call ourselves.
     *
     * @param ca The call that has been answered or not
     */
    void setAnswered(Call ca) {
        if ((ca != null) && (!ca.isForReg())) {
            ProtocolEventListener ges = _binder.getGuiEventSender(_gui);
            ges.answered(ca);
        }
    }



    /**
     * Gets the status attribute of the Friend object
     *
     * @return The status value
     */
    public String getStatus() {
        String ret = this._username + "@" + _iad;
        ret += _registered ? " registered" : " not registered";
        return ret;
    }


    /**
     * Returns if the call will be accepted. We are our gui, and pass
     * the answer back.
     *
     * @param call The call to be accepted
     * @return True if accepted, false it not
     */
    public boolean willAccept(Call call) {
        boolean ret = true;
        if ((_gui != null) && (_gui instanceof CallManager)) {
            ret = ((CallManager) _gui).accept(call);
        }
        return ret;
    }

    /**
     * getAudioFace
     *
     * @return AudioInterface
     */
    AudioInterface getAudioFace() {
        return _binder.getAudioFace();
    }

}

