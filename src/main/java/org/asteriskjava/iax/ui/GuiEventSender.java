// NAME
//      $RCSfile: GuiEventSender.java,v $
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
package org.asteriskjava.iax.ui;
import org.asteriskjava.iax.protocol.*;

/**
 * Decouples events from the main threads. This class it used by Friend.
 *
 * @author <a href="mailto:thp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision$ $Date$
 * @see Friend
 */

public class GuiEventSender
    implements ProtocolEventListener {

    private final static String version_id =
        "@(#)$Id$ Copyright Mexuar Technologies Ltd";

    private ProtocolEventListener _gui;
    private Call _call;

    /**
     * Constructor for the GuiEventSender object
     *
     * @param gui The protocol event listener
     */
    public GuiEventSender(ProtocolEventListener gui) {
        _gui = gui;
    }

    /**
     * Received a new call.
     * Via invokeLater() this is passed on to the ProtocolEventListener parameter.
     *
     * @param c The call object
     */
    public void newCall(Call c) {
        _call = c;
        Runnable r = new Runnable() {
            public void run() {
                if (_gui != null) {
                    _gui.newCall(_call);
                }
            }
        };
        javax.swing.SwingUtilities.invokeLater(r);
    }

    /**
     * Hung up.
     * Via invokeLater() this is passed on to the ProtocolEventListener parameter.
     *
     * @param c The call object
     */
    public void hungUp(Call c) {
        _call = c;
        Runnable r = new Runnable() {
            public void run() {
                if (_gui != null) {
                    _gui.hungUp(_call);
                }
            }
        };
        javax.swing.SwingUtilities.invokeLater(r);
    }

    /**
     * Ringing.
     * Via invokeLater() this is passed on to the ProtocolEventListener parameter.
     *
     * @param c The call object
     */
    public void ringing(Call c) {
        _call = c;
        Runnable r = new Runnable() {
            public void run() {
                if (_gui != null) {
                    _gui.ringing(_call);
                }
            }
        };
        javax.swing.SwingUtilities.invokeLater(r);
    }

    /**
     * Answered.
     * Via invokeLater() this is passed on to the ProtocolEventListener parameter.
     *
     * @param c The call object
     */
    public void answered(Call c) {
        _call = c;
        Runnable r = new Runnable() {
            public void run() {
                if (_gui != null) {
                    _gui.answered(_call);
                }
            }
        };
        javax.swing.SwingUtilities.invokeLater(r);
    }

    /**
     * registered
     *
     * @param f Friend
     * @param s boolean
     */
    public void registered(Friend f, boolean s) {
        final Friend ff = f;
        final boolean fs = s;
        Runnable r = new Runnable() {
            public void run() {
                if (_gui != null) {
                    _gui.registered(ff, fs);
                }
            }
        };
        javax.swing.SwingUtilities.invokeLater(r);
    }

    /**
     * setHostReachable
     *
     * @param f Friend
     * @param b boolean
     * @param roundtrip int
     */
    public void setHostReachable(Friend f, boolean b, int roundtrip) {
        final Friend ff = f;
        final boolean fb = b;
        final int fr = roundtrip;
        Runnable r = new Runnable() {
            public void run() {
                if (_gui != null) {
                    _gui.setHostReachable(ff, fb, fr);
                }
            }
        };
        javax.swing.SwingUtilities.invokeLater(r);

    }

}
