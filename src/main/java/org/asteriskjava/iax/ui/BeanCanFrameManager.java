
package org.asteriskjava.iax.ui;

import org.asteriskjava.iax.audio.javasound.Audio8k;
import org.asteriskjava.iax.audio.javasound.AudioInterface;
import org.asteriskjava.iax.protocol.*;

import java.awt.event.ActionEvent;
import java.net.SocketException;


public class BeanCanFrameManager extends BeanCanFrame implements ProtocolEventListener, CallManager {

    private Call _ca = null;
    private Friend _peer = null;
    private String _username = "";
    private String _password = "";
    private String _host = "";
    private Binder _bind = null;
    private boolean _isApplet = false;
    private AudioInterface _audioBase = null;

    public BeanCanFrameManager(boolean isApplet, int level, String host) {
        super();
        Log.setLevel(level);
        _isApplet = isApplet;
        _host = host;
    }

    public void start() {
        this.setVisible(true);
        _audioBase = new Audio8k();
        try {
            _bind = new BinderSE(_host, _audioBase);
        } catch (SocketException ex) {
            status.setText(ex.getMessage());
        }

    }

    public void stop() {
        if (_bind != null) {
            _bind.stop();
        }
        this.hide();
        status.setText("Stopped");
        _bind = null;
    }

    public BeanCanFrameManager(String username, String password, String host, boolean isApplet, int level) {
        this(isApplet, level, host);
        _username = username;
        _password = password;
    }

    void register() {
        if (_bind == null) {
            start();
        }
        try {

            _bind.register(_username, _password, this, true);
        } catch (Exception ex) {
            status.setText(ex.getMessage());
        }
    }

    /**
     * newCall
     *
     * @param c Call
     */
    @Override
    public void newCall(Call c) {
        Log.debug("Llamada Entrante ");
        if (_ca == null) {
            _ca = c;
            Log.debug("_ca == null :" + _ca.getStatus());
            this.status.setText(c.getStatus());
            if (_ca.getIsInbound()) {
                act.setText("Atender");
            } else {
                act.setText("Cortar");
            }
        } else {
            Log.debug("_ca != null :" + _ca.getStatus());
            this.status.setText("Ignorando llamada Entrante");
        }
    }

    /**
     * registered
     *
     * @param f Friend
     * @param s boolean
     */
    @Override
    public void registered(Friend f, boolean s) {
        _peer = f;
        this.status.setText(_peer.getStatus());
    }


    @Override
    public boolean accept(Call ca) {
        Log.debug("Aceptada Entrante ");
        boolean ret = true;
        if (_ca != null) {
            ret = false;
        }
        return ret;
    }


    /**
     * hungUp
     *
     * @param c Call
     */
    @Override
    public void hungUp(Call c) {
        _ca = null;
        status.setText("Disponible");
        act.setText("Llamar");
    }

    /**
     * ringing
     *
     * @param c Call
     */
    @Override
    public void ringing(Call c) {
        status.setText("Ringing");
    }

    /**
     * Lets us know that the call we made is answered (or
     * not).
     *
     * @param c Call
     * @see ProtocolEventListener#answered(Call)
     */
    @Override
    public void answered(Call c) {
        status.setText("Antendida " + c.isAnswered());
    }

    /**
     * Called when it is known whether or not friend can reach its host
     * (PBX).
     *
     * @param f         Friend
     * @param b         Whether friend can reach its host
     * @param roundtrip The round trip (ms) of the request
     * @todo implement
     */
    @Override
    public void setHostReachable(Friend f, boolean b, int roundtrip) {
        Log.warn("setHostReachable " + b + ", roundtrip " + roundtrip);
    }

    /**
     */
    @Override
    void dialString_actionPerformed(ActionEvent e) {
        if (_ca == null) {
            if (_peer != null) {
                String num = dialString.getText();
                _peer.newCall(_username, _password, num, null, null);
            }
        } else {
            if (_ca.getIsInbound()) {
                if (_ca.isAnswered()) {
                    _ca.hangup();
                } else {
                    _ca.answer();
                    act.setText("Cortar");
                }
            } else {
                _ca.hangup();
            }
        }
    }


    @Override
    void button_action(ActionEvent e) {
        if (_ca == null) {
            super.button_action(e);
        } else {
            String t = e.getActionCommand();
            _ca.sendDTMF(t.charAt(0));
            status.setText("Enviado dtmf " + t);
        }
    }

    @Override
    void hold() {
        if (_ca != null) {
            _ca.hold();
        }
    }

    @Override
    void clear_actionPerformed(ActionEvent e) {
        dialString.setText("");
    }

    public String get_host() {
        return _host;
    }

    public String get_password() {
        return _password;
    }

    public String get_username() {
        return _username;
    }

    public void set_username(String _username) {
        this._username = _username;
    }

    public void set_password(String _password) {
        this._password = _password;
    }

    public void set_host(String _host) {
        this._host = _host;
    }

    /**
     * set_debug
     *
     * @param debug int
     */
    public void set_debug(int debug) {
        Log.setLevel(debug);
    }

}
