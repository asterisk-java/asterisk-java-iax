// NAME
//      $RCSfile: BinderSE.java,v $
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
package org.asteriskjava.iax.protocol.netse;

import java.io.*;
import java.net.*;
import org.asteriskjava.iax.util.*;
import org.asteriskjava.iax.protocol.*;
import java.util.*;
import org.asteriskjava.iax.audio.AudioInterface;

/**
 * Main program - binds the udp port.
 * Each Friend object (representing an Astrix host) is associated with
 * one (or the) Binder object.
 *
 * @author <a href="mailto:thp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision$ $Date$
 */
public class BinderSE extends Binder implements Runnable  {

    private final static String version_id =
            "@(#)$Id$ Copyright Mexuar Technologies Ltd";

    /** The default IAX2 port (4569) */
    final static int IAX2SOC = 4569;

    /** The default buffer size (4096) */
    final static int BUFFSZ = 4096;

    private DatagramSocket _lsoc;
    private boolean _done = false;
    // JDK 1.5 only: private Hashtable<InetAddress, Friend> _friends = new Hashtable<InetAddress, Friend>(5);
    private Hashtable _friends = new Hashtable(5);
    private Thread _listener;
    private String _host;
    private AudioInterface _a8;


    /**
     * Constructor for the Binder object
     *
     * @param host The asterisk host which will be our PBX
     * @exception SocketException Thrown if the datagram (udp) socket
     * cannot be created
     */
    public BinderSE(String host, AudioInterface a8) throws SocketException {
        try {
            String jversion = System.getProperty("java.version");
            Log.debug("Java version = " + jversion);
            Log.debug("Binder version: " + version_id);
        }
        catch (Throwable t) {
            ;// don't care much
        }
        _host = host;
        // _lsoc = new DatagramSocket(IAX2SOC);
        _lsoc = new DatagramSocket();
        //_lsoc.setTrafficClass(0x10);
        _listener = new Thread(this, "Binder Listen");
        _listener.setPriority(Thread.MAX_PRIORITY-1);
        _listener.start();
        _a8 = a8;
    }


    /**
     * The main test program for the Binder class
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        try {
            Log.setLevel(Log.ALL);
            String host = "lef.westhawk.co.uk";
            BinderSE binder1 = new BinderSE(host,null);
            Thread.sleep(1000);
            try {
                InetAddress iad = InetAddress.getByName(host);
                Friend f = new Friend(binder1, host);
                binder1._friends.put(iad, f);
                f.checkHostReachable();

                /*
                binder1.register(host, "ijax2", "moofoo", null, true);
                String num = args.length > 0 ? args[0] : null;
                if (num != null) {
                    f.newCall("ijax2", "moofoo", num);
                }
                 */
            }
            catch (UnknownHostException ex1) {
                ex1.printStackTrace();
            }
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
    }/**
     * Set up the infrastructure to make (and optionally receive)
     * calls. If we register for incoming calls gui.registered(Friend,
     * boolean) will be called when the asterisk registration
     * succeeds.
     *
     * @param username A username to use
     * @param password The password for the user
     * @param gui A listener which needs to be told about the registration result
     * @param wantIncoming Whether we are waiting for incoming calls
     * @throws UnknownHostException
     */
    public void register(String username, String password,
            ProtocolEventListener gui, boolean wantIncoming)
        throws Exception {
        InetAddress iad = InetAddress.getByName(_host);

        Log.debug("registering with " + _host);
        Friend f = findFriend(iad);
        if (f == null) {
            f = new Friend(this, _host, gui);
            _friends.put(iad, f);
        }

        if (wantIncoming) {
            f.register(username, password);
        } else {
            //We return true so that the gui knows that the Friend is setup
            //correctly. If there was a problem we'd have bailed before now!
            if (gui != null) {
                gui.registered(f, true);
            }
        }
    }


    /**
     * Unregisters from the asterisk pbx. This will only happen if we
     * are registered indeed.
     *
     * @param gui A listener which needs to be told about the unregistration result
     * @throws UnknownHostException
     */
    public void unregister(ProtocolEventListener gui)
        throws Exception {
        InetAddress iad = InetAddress.getByName(_host);

        Log.debug("unregistering with " + _host);
        Friend f = findFriend(iad);
        if (f != null && f.isRegistered() == true) {
            f.unregister();
        } else {
            //We return false so that the gui knows that the Friend is
            //unregistered. If there was a problem we'd have bailed before now!
            if (gui != null) {
                gui.registered(f, false);
            }
        }
    }


    /**
     * Stop listening, if we were listening.
     */
    public void stop() {
        _done = true;
        if (_listener != null) {
            _lsoc.close();
            Log.debug("Closed Socket");
            try {
                _listener.join();
                Log.debug("Joined binder thread");
            }
            catch (InterruptedException ex) {
                Log.warn(ex.getMessage());
            }
            _listener = null;
        }
        // Birgit: is this the best place for it??
        _a8.cleanUp();
    }


    /**
     * Keeps listening for incoming data.
     * It will pass the data to our friend.
     *
     * @see Friend#recv(byte[])
     */
    public void run() {
        byte buff[] = new byte[BUFFSZ];
        while (!_done) {
            DatagramPacket dgp = new DatagramPacket(buff, BUFFSZ);
            try {
                _lsoc.receive(dgp);
                InetAddress iad = dgp.getAddress();
                int len = dgp.getLength();
                byte data[] = new byte[len];
                System.arraycopy(buff, 0, data, 0, len);
                //packetDump(data, len, iad, dgp.getPort(), true);
                Friend f = findFriend(iad);
                if (f != null) {
                    f.recv(data);
                } else {
                    //throw new IAX2ProtocolException("no friends ?");
                }
            }
            catch (IOException ex) {
                if (!_done) {
                    Log.warn(ex.getMessage());
                }
            }
        }
        // cleanup
        Enumeration fall = _friends.elements();
        while (fall.hasMoreElements()) {
            Friend f = (Friend) fall.nextElement();
            f.stop();
        }
    }


    /**
     * Sends a frame (as bytes) to a specified address
     *
     * @param h The asterisk host address
     * @param b The frame (in bytes)
     * @see Friend#send(ByteBuffer)
     */
    public void send(String h, ByteBuffer b) {
        InetAddress a = null;
        try {
           a = InetAddress.getByName(h);
           DatagramPacket p = new DatagramPacket(b.array(), b.position(), a,
        this.IAX2SOC);
// packetDump(b.array(), b.position(), a, this.IAX2SOC, false);
                this._lsoc.send(p);

        }
        catch (Exception ex1) {
                        ex1.printStackTrace();
        }
    }/**
     * Find the friend that belongs to the specified address.
     *
     * @param iad The asterisk host address
     * @return The found Friend
     */
    private Friend findFriend(InetAddress iad) {
        Friend ret = null;
        ret = (Friend) _friends.get(iad);
        return ret;
    }


    /**
     * Removes a friend. This will be called by friend when it has been
     * succesfully unregistered.
     *
     * @param host The asterisk host address
     * @return The found Friend
     */
    public Friend removeFriend(String host) {
        InetAddress iad = null;
        Friend ret = null;
        try {
            iad = InetAddress.getByName(host);
            ret = (Friend) _friends.remove(iad);
        }
        catch (UnknownHostException ex) {
            Log.warn(ex.getMessage());
        }
        return ret;
    }

    public AudioInterface getAudioFace(){
        return _a8;
    }
    /**
     * Dumps information of a frame (in bytes) to standard error.
     *
     * @param bs The in- or outgoing frame (in bytes)
     * @param i The package size
     * @param a The asterisk host address
     * @param i1 The port number
     * @param in Is it in (true) or outgoing (false)
     */
    protected void packetDump(byte[] bs, int i, InetAddress a, int i1,
                              boolean in) {
        StringBuffer os = new StringBuffer(500);
        if (in) {
            os.append("Packet got from ").append(a.getHostAddress()).append(":").
                append(i1).append('\n');
        }
        else {
            os.append("Packet sent to ").append(a.getHostAddress()).append(":").
                append(i1).append('\n');
        }
        os.append("Packet size = ").append(i).append('\n');
        byte[] bf = new byte[i];
        System.arraycopy(bs, 0, bf, 0, i);
        os.append(enHex(bf, new Character(' ')));
        os.append('\n');
        System.err.print(os);
    }

    /**
     * getGuiEventSender
     *
     * @param _gui ProtocolEventListener
     * @return ProtocolEventListener
     */
    public ProtocolEventListener getGuiEventSender(ProtocolEventListener _gui) {
        return new org.asteriskjava.iax.ui.GuiEventSender(_gui);
    }

}

