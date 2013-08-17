
package org.asteriskjava.iax.protocol;


import org.asteriskjava.iax.audio.javasound.AudioInterface;

import java.io.IOException;

/**
 * Takes captured audio and sends it to the remote IAX
 */
public class AudioSender {

    private Call _call;
    private int _formatBit;
    private boolean _done = false;
    private long _astart;
    private long _cstart;
    private long _nextdue;
    private int _stamp;
    private char _lastminisent;
    private boolean _first;
    private AudioInterface _aif;
    private byte[] _buff;

    /**
     * Constructor for the AudioSender object
     *
     * @param aif The audio interface
     * @param ca  The call object
     */
    AudioSender(AudioInterface aif, Call ca) {
        _call = ca;
        _aif = aif;
        _formatBit = aif.getFormatBit();
        _buff = new byte[_aif.getSampSz()];

        _cstart = this._call.getTimestamp();
        _nextdue = _cstart;
        _lastminisent = 0;
        _first = true;

    }

    /**
     * sending audio, using VoiceFrame and MiniFrame frames.
     */
    public void send() throws IOException {

        if (!_done) {

            long stamp = _aif.readWithTime(_buff);
            long now = stamp - _astart + _cstart;
            _stamp = (int) _nextdue;
            char mstamp = (char) (0xffff & _stamp);
            if (_first || (mstamp < _lastminisent)) {
                _first = false;
                VoiceFrame vf = new VoiceFrame(_call);
                vf._sCall = _call.getLno().charValue();
                vf._dCall = _call.getRno().charValue();
                vf.setTimestampVal(_stamp);
                vf._subclass = _formatBit;
                vf.sendMe(_buff);
                Log.debug("sent Full voice frame");
                // send a full audio frame
            } else {
                // send a miniframe
                MiniFrame mf = new MiniFrame(_call);
                mf._sCall = _call.getLno().charValue();
                mf.setTimestampVal(mstamp); // ?
                mf.sendMe(_buff);
                Log.verb("sent voice mini frame " + (int) (mstamp / 20));
            }
            // eitherway note it down
            _lastminisent = mstamp;
            // now workout how long to wait...
            _nextdue += 20;

        }

    }

}
