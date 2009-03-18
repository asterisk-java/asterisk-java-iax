// NAME
//      $RCSfile: Faceless.java,v $
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

package org.asteriskjava.iax.faceless;

import org.asteriskjava.iax.audio.AudioInterface;
import org.asteriskjava.iax.audio.javasound.Audio8k;
import org.asteriskjava.iax.audio.javasound.AudioProperties;
import org.asteriskjava.iax.protocol.*;
import org.asteriskjava.iax.protocol.netse.BinderSE;

import javax.swing.*;
import java.applet.Applet;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.security.AccessControlException;
import java.security.Permission;

/**
 *
 *
 * @author <a href="mailto:ray@westhawk.co.uk">Ray Tran</a>
 * @version $Revision$ $Date$
 */
public class Faceless
    extends JApplet
    implements CallManager, ProtocolEventListener {

    private static final String version_id =
        "@(#)$Id$ Copyright Mexuar Technologies Ltd";

    private boolean isStandalone = false;
    protected String _user = null;
    protected String _pass = null;
    protected String _host = null;
    private boolean _incoming = false;
    private String _calledNo = "";
    private String _callingNo = null;
    private String _callingName = null;
    private Binder _bind;
    private Friend _peer;
    private Call _call;
    private int _debug = 4;

    private JLabel lab = new JLabel();
    private String[] audioinList = null;
    private String[] audiooutList = null;
    private ProtocolEventListener _pevl = this;
    private AudioInterface _audioBase;

    private Utils _utils;

    //Construct the applet
    public Faceless() {
        _utils = new Utils(this);
    }

    /**
     * Initialises the applet.
     *
     * @see #jbInit()
     */
    public void init() {
        try {
            jbInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the applet. This method gets the javascript window object.
     */
    /*
     * Birgit: ??
     *
     * Status UI, order to test things in:
     * Beforehand:
     * 1. canRecord (do we have permission, to do with signing)
     * 2. audioAvailable (is audio available, to do with machine)
     *
     * After start() of applet:
     * 3. audioUsable (could we open audio, maybe used by other application)
     *
     * After register(incoming = false):
     * 4. checkHostReachable()
     *
     * When not signed, it is still possible to read its own cookies.
     */
    public void start() {

        Class cl = this.getClass();

        Log.setLevel(_debug);
        Log.warn(cl.getName() + ": bind host = " + getHost() 
            + ", version = " + getVersion());
        _utils.printSigners(cl);

        try {
            invokeSetup();
            invokeLoaded();
        }
        catch (AccessControlException ex) {
            // applet probably isn't signed
            Permission perm = ex.getPermission();
            Log.warn("print AccessControlException from new on Binder(): "
                     + "no " + perm.getName() + " permission");
            ex.printStackTrace();
            this.showStatus(ex.getMessage());
        }
        catch (Exception ex) {
            Log.warn("print exception from new on Binder()");
            ex.printStackTrace();
            this.showStatus(ex.getMessage());
        }
    }

    protected String getVersion() {
        // TODO fix version detection
        return "1.0";
    }

    /**
     * Creates the Binder object. It first checks if we've got
     * permission to record.
     *
     * @throws SocketException thrown by Binder object
     */
    protected void open() throws SocketException {
        // No point creating a new Binder object, when we cannot record.
        // It will only throw a SocketException
        if (!canRecord()) {
            show("Can't get access to microphone");
        }
        else {
            // we assume that the audio props have been set by now
            _audioBase = new Audio8k();
            _bind = new BinderSE(_host, _audioBase);

            Log.debug("audioIn usable = " + isAudioInUsable()
                      + ", audioOut usable = " + isAudioOutUsable());
        }
    }

    /**
     * Stops the applet.
     *
     * @todo Wait for the unregister response(s).
     */
    public void stop() {
        Log.debug("applet stop");

        // _peer will be stopped either in registred or via _bind
        if (_peer != null) {
            unregister();
        }
        if (_bind != null) {
            _bind.stop();
            _bind = null;
        }
    }

    /**
     * Unregister from our pbx. This is called in stop().
     */
    private void unregister() {
        if (_peer != null && _bind != null) {
            try {
                Log.debug("unregister() _bind = " + _bind);
                _bind.unregister(this);
            }
            catch (Exception exc) {
                Log.warn("unregister " + exc.getClass().getName() + ": " +
                         exc.getMessage());
            }
        }
    }

    /**
     * Destroys the applet. Does nothing.
     */
    public void destroy() {
        Log.debug("applet destroy");
    }

    /** Get Applet information */
    public String getAppletInfo() {
        return "The Faceless Applet Information";
    }

    /** Get parameter info */
    public String[][] getParameterInfo() {
        return null;
    }

    /**
     * Sets the calling number. This will be used to pass the 'context'
     * info to the recipient.
     * This information is only used when making outgoing calls.
     *
     * This method is called via javascript in the register() method.
     * The value is passed from the webpage via javascript.
     *
     * @param callingNo The calling number
     */
    public void setCallingNumber(String callingNo) {
        _callingNo = callingNo;
    }

    /**
     * Sets the calling name. This will be used to pass the 'context'
     * info to the recipient.
     * This information is only used when making outgoing calls.
     *
     * This method is called via javascript in the register() method.
     * The value is passed from the webpage via javascript.
     *
     * @param callingName The calling name
     */
    public void setCallingName(String callingName) {
        _callingName = callingName;
    }

    /**
     * Sets the password. This is used for authentication at the
     * Asterisk host.
     * This method is called via javascript in the register() method.
     * The value is passed from the webpage via javascript.
     *
     * @param pass The password
     */
    public void setPass(String pass) {
        _pass = pass;
    }

    /**
     * Sets the username. This is used for authentication at the
     * Asterisk host.
     * This method is called via javascript in the register() method.
     * The value is passed from the webpage via javascript.
     *
     * @param user The username
     */
    public void setUser(String user) {
        _user = user;
    }

    public String getHost()
    {
        return _host;
    }

    public void setHost(String host)
    {
        this._host = host;
    }

    public String getAudioIn() {
        return AudioProperties.getInputDeviceName();
    }

    /**
     * Should be called from javascript setup()
     * It is too late after that!
     *
     * @param ain String
     */
    public void setAudioIn(String ain) {
        if (ain != null) {
            AudioProperties.setInputDeviceName(ain);
        }
    }

    public String getAudioOut() {
        return AudioProperties.getOutputDeviceName();
    }

    /**
     * Should be called from javascript setup()
     * It is too late after that!
     *
     * @param aout String
     */
    public void setAudioOut(String aout) {
        if (aout != null) {
            AudioProperties.setOutputDeviceName(aout);
        }
    }

    /**
     * Sets the direction of the communication: outgoing only or both in
     * and outgoing.
     * This method is called via javascript in the register() method.
     * The value is passed from the webpage via javascript.
     * The default is not to have incoming calls, but only outgoing.
     *
     * @param trueorfalse String that says TRUE or FALSE
     */
    public void setWantIncoming(String trueorfalse) {
        if (trueorfalse != null) {
            if (trueorfalse.equalsIgnoreCase("TRUE")) {
                _incoming = true;
            }
            else {
                _incoming = false;
            }
        }
    }

    /**
     * Register with the infrastructure so that calls may be made.
     * This method is called via javascript in the register() method.
     * The value is passed from the webpage via javascript.
     *
     * <p>
     * This method will be called in javascript, which is not trusted
     * (even though the jar itself is signed). For that reason we use
     * the Timer ActionListener, so the swing thread will 'transfer'
     * this method from an untrusted to a trusted environment.
     * Also, it will make sure the method 'registered' isn't called too
     * soon, this might cause an re-entrant problem with javascript.
     * </p>
     *
     * @see #registered
     */
    public void register() {
        if (_peer == null && _user != null && _pass != null) {
            ActionListener ans = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (_bind == null)
                    {
                        try {
                            open();
                            Log.debug("binder = " + _bind);
                        }
                        catch (Exception ex) {
                            Log.warn("register (open) " + ex.getClass().getName() + ": " +
                                     ex.getMessage());
                            show("register (open) " + ex.getMessage());
                        }
                    }

                    try {
                        Log.debug("register() _bind = " + _bind);
                        _bind.register(_user, _pass, _pevl, _incoming);
                    }
                    catch (Exception ex) {
                        Log.warn("register " + ex.getClass().getName() + ": " +
                                 ex.getMessage());
                        show("register " + ex.getMessage());
                    }
                }
            };
            Timer timer = new Timer(100, ans);
            timer.setRepeats(false);
            timer.start();
        }
        else {
            show("can't register");
        }
    }

    /**
     * Dials a number.
     * This method is called via javascript in the dial() method.
     * The value is passed from the webpage via javascript.
     *
     * <p>
     * This method will be called in javascript, which is not trusted
     * (even though the jar itself is signed). For that reason we use
     * the invokeLater Runnable, so the swing thread will 'transfer'
     * this method from an untrusted to a trusted environment.
     * </p>
     *
     * @param no The number to dial
     */
    public void dial(String no) {
        Log.debug("Pressed dial");
        //_calledNo = cleanUp(no);
        _calledNo = no;
        if (_call == null) {
            if (_peer != null) {
                Runnable dr = new Runnable() {
                    public void run() {
                        show("Dialing " + _calledNo);
                        _call = _peer.newCall(_user, _pass, _calledNo,
                                              _callingNo, _callingName);
                    }
                };
                javax.swing.SwingUtilities.invokeLater(dr);
            }
            else {
                // _peer will be null when register isn't called because
                // of isExpired()
                Runnable dr = new Runnable() {
                    public void run() {
                        show("No peer object, initialise first");
                    }
                };
                javax.swing.SwingUtilities.invokeLater(dr);
            }
        }
        else {
            show("No new call, in call already");
        }
    }

    /**
     * Hangs up the current call.
     * This method is called via javascript in the hangup() method.
     *
     * <p>
     * This method will be called in javascript, which is not trusted
     * (even though the jar itself is signed). For that reason we use
     * the invokeLater Runnable, so the swing thread will 'transfer'
     * this method from an untrusted to a trusted environment.
     * </p>
     */
    public void hangup() {
        Log.debug("Pressed hangup");
        if (_call != null) {
            Runnable ans = new Runnable() {
                public void run() {
                    _call.hangup();
                }
            };
            javax.swing.SwingUtilities.invokeLater(ans);
            show("Hangup...");
        }
    }

    /**
     * Answers a call.
     * This method is called via javascript in the answer() method.
     *
     * <p>
     * This method will be called in javascript, which is not trusted
     * (even though the jar itself is signed). For that reason we use
     * the invokeLater Runnable, so the swing thread will 'transfer'
     * this method from an untrusted to a trusted environment.
     * </p>
     */
    public void answer() {
        Log.debug("Pressed answer");
        if (_call != null && _call.getIsInbound()) {
            Runnable ans = new Runnable() {
                public void run() {
                    _call.answer();
                }
            };
            javax.swing.SwingUtilities.invokeLater(ans);
            show("Answering");
        }
    }

    public void sendFirstCharDTMF(String s) {
        if (s != null) {
            sendDTMF(s.charAt(0));
        }
    }

    /**
     * Sends a dtmf digit when in a call.
     * This method is called from javascript.
     *
     * <p>
     * This method will be called in javascript, which is not trusted
     * (even though the jar itself is signed). For that reason we use
     * the invokeLater Runnable, so the swing thread will 'transfer'
     * this method from an untrusted to a trusted environment.
     * </p>
     */

    public void sendDTMF(char d) {
        final char dd = d;
        Log.debug("Pressed " + d);
        if (_call != null) {
            Runnable dig = new Runnable() {
                public void run() {
                    _call.sendDTMF(dd);
                }
            };
            javax.swing.SwingUtilities.invokeLater(dig);
            show("Sending DTMF" + dd);
        }
    }

    /**
     * Checks the connectivity of the asterisk host.
     * This method is called via javascript in the hostreachable() method.
     *
     * <p>
     * This method will be called in javascript, which is not trusted
     * (even though the jar itself is signed). For that reason we use
     * the invokeLater Runnable, so the swing thread will 'transfer'
     * this method from an untrusted to a trusted environment.
     * </p>
     */
    public void checkHostReachable() {
        Log.debug("in checkHostReachable()");
        if (_call == null) {
            if (_peer != null) {
                Runnable dr = new Runnable() {
                    public void run() {
                        _peer.checkHostReachable();
                    }
                };
                javax.swing.SwingUtilities.invokeLater(dr);
            }
            else {
                show("No peer object, initialise first");
            }
        }
        else {
            show("Cannot check whilst in call");
        }
    }

    /**
     * Returns if there are some incoming audio devices available.
     * The fact that it's available, doesn't necessarily mean that we
     * can use it.
     *
     * @return True if there are, false if there aren't
     * @see #getAudioInListLen()
     */
    public boolean isAudioInAvailable() {
        if (audioinList == null) {
            Integer i = getAudioInListLen();
        }
        return (audioinList.length > 0);
    }

    /**
     * Returns if an incoming audio device could be opened.
     * Call after start() is finished.
     */
    public boolean isAudioInUsable() {
        return AudioProperties.isAudioInUsable();
    }

    /**
     * Returns the length of the list of incoming audio devices.
     * This method is called via javascript in the showAudioDevices() method.
     *
     * @return The number of incoming audio devices available
     */
    public Integer getAudioInListLen() {
        Log.debug("in getAudioListLen()");
        audioinList = AudioProperties.getMixIn();
        return new Integer(audioinList.length);
    }

    /**
     * Returns nth incoming audio device.
     * This method is called via javascript in the showAudioDevices() method.
     *
     * @param n The index
     * @return The name of the audio device
     */
    public String getAudioInList(int n) {
        Log.debug("in getAudioList()");
        return audioinList[n];
    }

    /**
     * Returns if there are some outgoing audio devices available.
     * The fact that it's available, doesn't necessarily mean that we
     * can use it.
     *
     * @return True if there are, false if there aren't
     * @see #getAudioOutListLen()
     */
    public boolean isAudioOutAvailable() {
        if (audiooutList == null) {
            Integer i = getAudioOutListLen();
        }
        return (audiooutList.length > 0);
    }

    /**
     * Returns if an outgoing audio device could be opened.
     * Call after start() is finished.
     */
    public boolean isAudioOutUsable() {
        return AudioProperties.isAudioOutUsable();
    }

    /**
     * Returns the length of the list of outgoing audio devices.
     * This method is called via javascript in the showAudioDevices() method.
     *
     * @return The number of outgoing audio devices available
     */
    public Integer getAudioOutListLen() {
        audiooutList = AudioProperties.getMixOut();
        return new Integer(audiooutList.length);
    }

    /**
     * Returns nth outgoing audio device.
     * This method is called via javascript in the showAudioDevices() method.
     *
     * @param n The index
     * @return The name of the audio device
     */
    public String getAudioOutList(int n) {
        return audiooutList[n];
    }

    /**
     * Lets us know that the outgoing call we made via dial() has been
     * accepted or that we have an incoming call.
     * This will invoke the newCall() method in the javascript.
     *
     * @param c Call
     * @see #dial(String)
     * @see ProtocolEventListener#newCall(Call)
     */
    public void newCall(Call c) {
        _call = c;
        show("newCall " + _call.toString());
        invoke("newCall");
    }

    /**
     * Lets us know that the other party has hung up on us, or that the
     * call from some other reason has been torn down.
     * This will invoke the hungUp() method in the javascript.
     *
     * @param c Call
     * @see #dial(String)
     * @see ProtocolEventListener#hungUp(Call)
     */
    public void hungUp(Call c) {
        show("hungup " + c.toString() + ", causecode=" + c.getHungupCauseCode());
        if (_call == c) {
            Object[] args = new Object[1];
            args[0] = "" + _call.getHungupCauseCode();
            call("hungUp", args);
            _call = null;
        }
    }

    /**
     * Lets us know that the call we made via dial() is ringing.
     *
     * @param c Call
     * @see #dial(String)
     * @see ProtocolEventListener#ringing(Call)
     */
    public void ringing(Call c) {
        show("ringing " + c.toString());
        invoke("ringing");
    }

    /**
     * Lets us know that the call we made via dial() is answered (or
     * not).
     *
     * @param c Call
     * @see #dial(String)
     * @see ProtocolEventListener#answered(Call)
     */
    public void answered(Call c) {
        show("answered " + c.isAnswered());
        invoke("answered");
    }

    /**
     * Called when registration or unregistration has succeeded.
     *
     * @param f Friend
     * @param s Whether we are registrated or not.
     *
     * @see #register
     * @see ProtocolEventListener#registered(Friend, boolean)
     */
    public void registered(Friend f, boolean s) {
        Log.warn("registered " + s);
        if (s == true) {
            _peer = f;
        }
        else if (_peer != null) {
            _peer.stop();
            _peer = null;
        }
        Object[] args = new Object[1];
        args[0] = f.getStatus();
        call("registered", args);
        show("registered " + f.getStatus());
    }

    /**
     * Called when it is known whether or not friend can reach its host
     * (PBX).
     * This will invoke the hostreachable() method in the javascript.
     *
     * @param f Friend
     * @param b Whether friend can reach its host
     * @param roundtrip The round trip (ms) of the request
     * @see ProtocolEventListener#setHostReachable(Friend, boolean, int)
     */
    public void setHostReachable(Friend f, boolean b, int roundtrip) {
        Log.warn("setHostReachable " + b);
        Object[] args = new Object[2];
        args[0] = "" + b;
        args[1] = "" + roundtrip;
        call("hostreachable", args);
        show("setHostReachable " + b);
    }

    /**
     * Invokes the loaded() method in the javascript. This is called in
     * start().
     *
     * @see #start()
     */
    private void invokeLoaded() {
        Object[] args = new Object[0];
        call("loaded", args);
    }

    /**
     * Invokes the setup() method in the javascript. This is called in
     * before any real action.
     * start().
     *
     * @see #start()
     */
    private void invokeSetup() {
        try {
            Object[] args = new Object[0];
            call("setup", args);
        }
        catch (Throwable any) {
            Log.warn(any.getMessage());
        }
    }

    /**
     * Invokes a javascript method.
     */
    private String invoke(String target) {
        Object[] args = new Object[5];
        args[0] = "" + _call.getIsInbound();
        args[1] = _call.getFarNo();
        args[2] = _call.getNearNo();
        args[3] = "" + _call.isAnswered();

        if (_call.getIsInbound()) {
            args[4] = _call.getFarName();
        }
        else {
            args[4] = _call.getNearName();
        }
        Object ret = call(target, args);

        return (ret == null) ? "" : ret.toString();
    }

    /**
     * Returns if we accept the call or not. Returns true.
     *
     * @param ca Call
     * @return True if we accept the call
     * @see CallManager#accept(Call)
     */
    public boolean accept(Call ca) {
        return true;
    }

    /**
     * Cleans up the string, by filtering out the characters that are
     * not digits.
     *
     * @param in The number typed in
     * @return The clean number
     * @see #dial(String)
     */
    String cleanUp(String in) {
        StringBuffer out = new StringBuffer(in.length());
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (Character.isDigit(c)) {
                out.append(c);
            }
        }
        return out.toString();
    }

    /**
     * Show this message in the webpage
     */
    private void show(String mess) {
        this.showStatus(mess);
        lab.setText(mess);
        validate();
    }

    public void showStatus(String msg)
    {
        if (_debug > 0) {
            super.showStatus(msg);
        }
    }

    /**
     * Returns if we have permission to record audio. If we don't have
     * permission, it could be because this applet isn't signed.
     *
     * @return True if we can, false if we cannot
     */
    public boolean canRecord() {
        boolean ret = false;
        javax.sound.sampled.AudioPermission ap = new javax.sound.sampled.
            AudioPermission("record");
        try {
            java.security.AccessController.checkPermission(ap);
            ret = true;
            Log.debug("Have permission to access microphone");
        }
        catch (java.security.AccessControlException ace) {
            Log.debug("Do not have permission to access microphone");
            Log.warn(ace.getMessage());
        }
        return ret;
    }

    /**
     * Returns the value of parameter key.
     *
     * @param key The key
     * @param def The default value if parameter key doesn't exists
     */
    public String getParameter(String key, String def) {
        String ret = null;
        if (isStandalone) {
            ret = System.getProperty(key, def);
        }
        else {
            if (getParameter(key) != null) {
                ret = getParameter(key);
            }
            else {
                ret = def;
            }
        }
        return ret;
    }

    /**
     * Component initialization. Called by init().
     * This retrieves and sets the value of debug.
     *
     * @see #init()
     */
    private void jbInit() throws Exception {
        String dS = getParameter("debug", "0");
        try {
            _debug = Integer.parseInt(dS);
        }
        catch (NumberFormatException nfe) {
            _debug = 9;
        }
        String idev = getParameter("audioIn", null);
        if (idev != null) {
            AudioProperties.setInputDeviceName(idev);
        }
        String odev = getParameter("audioOut", null);
        if (odev != null) {
            AudioProperties.setInputDeviceName(odev);
        }

        lab.setText("hi");
        this.getContentPane().add(lab);
    }

    public String getJavaVersion() {
        return System.getProperty("java.version");
    }

    Object call(String name, Object[] args)
    {
        try
        {
            Class jsObjectClass = Thread.currentThread().getContextClassLoader().loadClass("netscape.javascript.JSObject");
            Method getWindowMethod = jsObjectClass.getMethod("getWindow", new Class[] { Applet.class });
            Method callMethod = jsObjectClass.getMethod("call", new Class[] { String.class, Object[].class });

            Object window = getWindowMethod.invoke(null, new Object[] { this });
            return callMethod.invoke(window, new Object[] { name, args });
        }
        catch (Exception e)
        {
            Log.warn("Unable to call JavaScript method '" + name + "': " + e.getMessage());
        }

        return null;
    }
}
