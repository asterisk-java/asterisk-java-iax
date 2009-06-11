// NAME
//      $RCSfile: Audio8k.java,v $
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
package org.asteriskjava.iax.audio.javasound;

import java.io.*;
import javax.sound.sampled.*;

import org.asteriskjava.iax.audio.*;
import org.asteriskjava.iax.protocol.*;
import org.asteriskjava.iax.util.*;

/**
 * This class implements the audio interface for 16 bit
 *  signed linear audio.
 * It also provides support for codecs that can convert
 * to and from SLIN, specifically alaw and ulaw (for now)
 *
 *
 * @author <a href="mailto:thp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision$ $Date$
 */
public class Audio8k
    implements AudioInterface, Runnable {

    private final static String version_id =
        "@(#)$Id$ Copyright Mexuar Technologies Ltd";

    //private static boolean __audioOk = false;
    final static int DEPTH = 10;
    final static int LLBS = 6; // Low level buffer size
    final static int FRAMEINTERVAL = 20;
    private AudioFormat _stereo8k, _mono8k;
    protected TargetDataLine _targetDataLine;
    protected SourceDataLine _sourceDataLine;
    protected ABuffer[] _pbuffs = new ABuffer[DEPTH + DEPTH]; //play buffer
    protected ABuffer[] _rbuffs = new ABuffer[DEPTH]; //record buffer

    private int _ofno;
    private int _nwrite;

    //private static Audio8k __instance;
    protected boolean _canWrite;
    private boolean _providingRingBack = false;
    private AudioSender _audioSender;
    private byte[] _ring;
    private byte[] _silence;
    private Thread _micTh, _tick, _tickp, _ringTh;
    private int _bcount;
    protected int _micCount;
    private long _lastMicTime;
    protected int _micSpeakOffset;
    protected boolean _micSpeakOffsetValid = false;

    private long _odelta = 0;
    private boolean _first = true;
    private long _fudge = 0;
    private long _callLen = 0;
    long _rc = -1;

    private boolean _localMode = false;

    /**
     * Constructor for the Audio8k object
     */
    public Audio8k() {
        _mono8k = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                  8000.0F, 16, 1, 2, 8000.0F, true);
        _stereo8k = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                    8000.0F, 16, 2, 4, 8000.0F, true);
        initRingback();

        Runnable trec = new Runnable() {
            public void run() {
                recTick();
            }
        };

        _tick = new Thread(trec, "Tick-send");
        _tick.setDaemon(true);
        _tick.setPriority(Thread.MAX_PRIORITY - 1);

        this.getAudio();
        if (_targetDataLine != null) {
            _tick.start();
        }

        Runnable tplay = new Runnable() {
            public void run() {
                playTick();
            }
        };
        _tickp = new Thread(tplay, "Tick-play");
        _tickp.setDaemon(true);
        _tickp.setPriority(Thread.MAX_PRIORITY);

        if (_sourceDataLine != null) {
            _tickp.start();
        }

        Runnable ringer = new Runnable() {
            public void run() {
                ringDing();
            }

        };

        _ringTh = new Thread(ringer, "ringer");
        _ringTh.setDaemon(true);
        _ringTh.setPriority(Thread.MIN_PRIORITY);

        if (_sourceDataLine != null) {
            _ringTh.start();
        }

        Log.debug("Created new audio8k");
    }

    /**
     */
    public void cleanUp() {
        cleanMeUp();
    }

    void playTick() {
        while (_tickp != null) {
            try {
                long next = 20;
                if (!_localMode) {
                    next = this.writeBuff();
                }
                else {
                    Log.debug("holding packet " + _ofno);
                    next = 20;
                }
                if (next > 30) {
                    Log.verb("nap = " + next);
                }

                if (next < 1) {
                    next = 20;
                }
                Thread.sleep(next);
                Log.verb("Woke");

            }
            catch (Throwable ex) {
                Log.debug("Would have stopped"+ ex.getMessage());
            }

        }
    }

    void recTick() {
        long set = 0;
        long last, point = 0;
        long delta = 20; // We will recalculate anyways.
        boolean audioTime = false;

        while (_tick != null) {
            //
            point += 20; // This should be current time
            delta = point - set + 20;
            if (_targetDataLine.isActive()) {
                // Take care of "discontinuous time"
                if (!audioTime) {
                    audioTime = true;
                    set = _targetDataLine.getMicrosecondPosition() /
                        1000;
                    last = point = set;
                }
            }
            else {
                point = 0;
                delta = 20; // We are live before TDL
                set = System.currentTimeMillis(); // For ring cadence
                audioTime = false;
            }
            frameTime(set);
            // If we are late, set is larger than last so we sleep less
            // If we are early, set is smaller than last and we sleep longer
            if (delta > 1) { // Only sleep if it is worth it...
                try {
                    Thread.sleep(delta);
                }
                catch (InterruptedException ie) {}
            }
            last = set;
            if (audioTime) {
                set = _targetDataLine.getMicrosecondPosition() / 1000;
            }
            if (point > 0) {
                Log.verb("Ticker slept " + delta + " from " + last +
                         " now " + set);
            }
        }

    }

    /**
     * Description of the Method
     */
    /*
     * Added lines to fix:
     * CORR9 - Clean up resources when applet quits on WinX.
     */
    void cleanMeUp() {
        Thread micThCp = _micTh;
        Thread tickCp = _tick;
        Thread tpcp = _tickp;
        Thread rtcp = _ringTh;
        _tickp = null;
        _ringTh = null;
        _micTh = null;
        _tick = null;

        if (tpcp != null) {
            try {
                tpcp.join();
            }
            catch (InterruptedException ex) {
            }
        }
        if (rtcp != null) {
            try {
                rtcp.join();
            }
            catch (InterruptedException ex1) {
            }
        }

        try {
            if (tickCp != null) {
                tickCp.join();
            }
        }
        catch (java.lang.InterruptedException exc) {}
        finally {
            if (_sourceDataLine != null) {
                if (AudioProperties.closeDataLine()) {
                    _sourceDataLine.close();
                }
                _sourceDataLine = null;
            }
        }

        try {
            if (micThCp != null) {
                micThCp.join();
            }
        }
        catch (java.lang.InterruptedException exc) {}
        finally {
            if (_targetDataLine != null) {
                if (AudioProperties.closeDataLine()) {
                    _targetDataLine.close();
                }
                _targetDataLine = null;
            }
        }

    }

    /**
     * getAudio
     *
     * @return The audio value
     */
    private boolean getAudio() {
        boolean ret = false;
        boolean audioIn = getAudioIn();
        if (audioIn) {
            ret = getAudioOut();
            if (!ret) {
                freeAudioIn();
            }
        }
        AudioProperties.setAudioInUsable(audioIn);
        AudioProperties.setAudioOutUsable(ret);
        return ret;
    }

    /**
     * freeAudioIn
     *
     * @todo Implement this method
     */
    private void freeAudioIn() {
    }

    /**
     * Description of the Method
     */
    private void initRingback() {
        // from indications.conf 440+480/2000,0/4000
        double rat2 = 420.0 / 8000.0;
        double rat1 = 25.0 / 8000.0;
        int num = this.getSampSz();
        ByteBuffer rbb = ByteBuffer.allocate(num);

        for (int i = 0; i < 160; i++) {

            short s = (short) ( (Short.MAX_VALUE / 16)
                               *
                               (
                                   Math.sin(2.0 * Math.PI * rat1 * i)
                                   *
                                   Math.sin(4.0 * Math.PI * rat2 * i)
                               ));
            rbb.putShort(s);
        }
        _ring = rbb.array();
        _silence = new byte[num];
    }

    /**
     * called every 20 ms
     *
     * @param set Description of Parameter
     */
    private void frameTime(long set) {

        if (_audioSender != null) {
            try {
                _audioSender.send();
            }
            catch (IOException x) {
                Log.warn(x.getMessage());
            }
        }
        /*
                 if (!_localMode) {
            writeBuff();
                 }
         */
    }

    private void ringDing() {
        long nap = 0;
        while (_ringTh != null) {

            if (_providingRingBack) {
                nap = 0;
                while (nap < 20) {
                    boolean inRing = ( (_rc++ % 120) < 40);
                    if (inRing) {
                        nap = this.writeDirectIfAvail(_ring);
                    }
                    else {
                        nap = this.writeDirectIfAvail(_silence);
                    }
                }
            }
            else {
                nap = 100;
            }
            try {
                Thread.sleep(nap);
            }
            catch (InterruptedException ex) {
                ; // who cares
            }
        }
    }

    /**
     * getAudioOut
     *
     * @return boolean
     */
    private boolean getAudioOut() {
        boolean ret = false;
        String pref = AudioProperties.getOutputDeviceName();
        AudioFormat af;
        String name;
        if (AudioProperties.isStereoRec()) {
            af = this._stereo8k;
            name = "stereo8k";
        }
        else {
            af = this._mono8k;
            name = "mono8k";
        }

        int buffsz = (int) Math.round(LLBS * af.getFrameSize() *
                                      af.getFrameRate() *
                                      FRAMEINTERVAL / 1000.0);
        // we want to do tricky stuff on the 8k mono stream before
        // play back, so we accept no other sort of line.
        boolean big = AudioProperties.getBigBuff();
        if (big) {
            buffsz *= 2.5;
        }
        SourceDataLine sdl = seekSourceLine(pref, af, name, buffsz);
        if (sdl != null) {
            int obuffSz = (int) (af.getFrameRate() * af.getFrameSize() / 50.0);
            Log.debug("ObuffSz = " + obuffSz);
            for (int i = 0; i < _pbuffs.length; i++) {
                _pbuffs[i] = new ABuffer(obuffSz);
            }
            _sourceDataLine = sdl;
            ret = true;
        }

        return ret;
    }

    /**
     * Seek a dataline of either sort based on the pref string. uses
     * type to determine the sort ie Target or Source debtxt is only
     * used in debug printouts to set the context.
     *
     * @param pref String
     * @param af AudioFormat
     * @param name String
     * @param sbuffsz int
     * @param type Class
     * @param debtxt String
     * @return DataLine
     */
    private DataLine seekLine(String pref, AudioFormat af,
                              String name, int sbuffsz, Class type,
                              String debtxt) {

        DataLine line = null;
        DataLine.Info info = new DataLine.Info(type, af);
        try {
            if (pref != null) {
                Mixer.Info[] mixes = AudioSystem.getMixerInfo();
                for (int i = 0; i < mixes.length; i++) {
                    Mixer.Info mixi = mixes[i];
                    String mixup = mixi.getName().trim();
                    Log.debug("Mix " + i + " " + mixup);
                    if (mixup.equals(pref)) {
                        Mixer preferedMixer = AudioSystem.getMixer(mixi);
                        Log.debug("Found name match for prefered input mixer");
                        if (preferedMixer.isLineSupported(info)) {
                            line = (DataLine) preferedMixer.getLine(info);
                            Log.debug("got " + debtxt + " line");
                            break;
                        }
                        else {
                            Log.debug(debtxt + " format not supported");
                        }
                    }
                }
            }
            else {
                line = (DataLine) AudioSystem.getLine(info);
            }

        }
        catch (Exception e) {
            Log.warn("unable to get a " + debtxt + " line of type: " + name);
            line = null;
        }
        return line;
    }

    /**
     * Description of the Method
     *
     * @param pref Description of Parameter
     * @param af Description of Parameter
     * @param name Description of Parameter
     * @param sbuffsz Description of Parameter
     * @return Description of the Returned Value
     */
    private TargetDataLine seekTargetLine(String pref, AudioFormat af,
                                          String name, int sbuffsz) {
        String debtxt = "recording";
        TargetDataLine line = (TargetDataLine) seekLine(pref, af, name, sbuffsz,
            TargetDataLine.class, debtxt);
        if (line != null) {
            try {
                line.open(af, sbuffsz);
                Log.debug("got a " + debtxt + " line of type: " + name);
                Log.debug(" buffer size= " + line.getBufferSize());
            }
            catch (LineUnavailableException ex) {
                Log.warn("unable to get a " + debtxt + " line of type: " + name);
                line = null;
            }
        }
        return line;
    }

    /**
     * Description of the Method
     *
     * @param pref Description of Parameter
     * @param af Description of Parameter
     * @param name Description of Parameter
     * @param sbuffsz Description of Parameter
     * @return Description of the Returned Value
     */
    private SourceDataLine seekSourceLine(String pref, AudioFormat af,
                                          String name, int sbuffsz) {
        String debtxt = "play";
        SourceDataLine line = (SourceDataLine) seekLine(pref, af, name, sbuffsz,
            SourceDataLine.class, debtxt);
        if (line != null) {
            try {
                line.open(af, sbuffsz);
                Log.debug("got a " + debtxt + " line of type: " + name);
                Log.debug(" buffer size= " + line.getBufferSize());
            }
            catch (LineUnavailableException ex) {
                Log.warn("unable to get a " + debtxt + " line of type: " + name);
                line = null;
            }
        }
        return line;
    }

    /**
     * getAudioIn
     *
     * @return boolean
     */
    private boolean getAudioIn() {
        boolean ret = false;

        String pref = AudioProperties.getInputDeviceName();
        boolean big = AudioProperties.getBigBuff();
        /* first make a list of formats we can live with */
        String names[] = {
            "mono8k", "mono44k"};
        // the javasound formats associated with them

        AudioFormat mono44k = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                              44100.0F, 16, 1, 2, 44100.0F, true);
        AudioFormat[] afsteps = {
            _mono8k, mono44k};

        int[] smallbuff = {
            (int) Math.round(LLBS * _mono8k.getFrameSize() *
                             _mono8k.getFrameRate() * FRAMEINTERVAL / 1000.0),
            (int) Math.round(LLBS * mono44k.getFrameSize() *
                             mono44k.getFrameRate() * FRAMEINTERVAL / 1000.0)};
        if (AudioProperties.isStereoRec()) {
            names[0] = "stereo8k";
            afsteps[0] = _stereo8k;
            smallbuff[0] *= 2;
        }

        // if LLBS > 4 then these can be the same....
        // should tweak based on LLB really.
        int[] bigbuff = smallbuff;

// choose one based on audio properties
        int[] buff = big ? bigbuff : smallbuff;

        int fno = 0;
        // now try and find a device that will do it - and live up to the preferences
        _targetDataLine = null;
        for (; fno < afsteps.length; fno++) {
            _targetDataLine = seekTargetLine(pref, afsteps[fno], names[fno],
                                             buff[fno]);
            if (_targetDataLine != null) {
                break;
            }
        }
        Log.debug("targetDataLine =" + _targetDataLine);
        Log.debug("fno =" + fno);
        if (_targetDataLine != null) {
            // now allocate some buffer space in the raw format
            AudioFormat af = _targetDataLine.getFormat();
            int ibuffSz = (int) (af.getFrameRate() * af.getFrameSize() / 50.0);
            for (int i = 0; i < _rbuffs.length; i++) {
                _rbuffs[i] = new ABuffer(ibuffSz);
            }
            ret = true;
            // _targetDataLine.addLineListener(this);

        }
        else {
            Log.warn("No audio input device available");
        }

        return ret;
    }

    /**
     * Called from micThread once - loops and blocks as needed...
     */
    private void micRead() {
        try {
            int fresh = _bcount % _rbuffs.length;
            ABuffer ab = _rbuffs[fresh];
            byte[] buff = ab.getBuff();
            int got = _targetDataLine.read(buff, 0, buff.length);
            long stamp = _targetDataLine.getMicrosecondPosition() / 1000;
            if (stamp >= _lastMicTime) {
                if (ab.isWritten()) {
                    Log.debug("overrun audio data " + stamp + "/" + got);
                }
                ab.setStamp(stamp);
                ab.setWritten(); // should test for overrun ???
                Log.verb("put audio data into buffer " + fresh + " " +
                         ab.getStamp() + "/" + _bcount);
                _bcount++;
            }
            else {
                // Seen at second and subsequent activations, garbage data
                Log.debug("drop audio data " + stamp);
            }
            _lastMicTime = stamp;
            if (!_micSpeakOffsetValid && _canWrite) {
                // Mic started after player
                _micSpeakOffset = _nwrite - _bcount - LLBS; // Assume SDLB full
                _micSpeakOffsetValid = true;
                Log.debug("Set micSpeakOffset " + _micSpeakOffset);
            }
        }
        catch (Exception x) {
            Log.warn("Mic Reader thread quitting :" + x.getMessage());
            _micTh = null; // stops us ...
        }
    }

    /**
     * Returns the native audioformat of this class. 8k Mono PCM in
     * this case, but extenders will set their own
     *
     * @return AudioFormat
     */
    protected AudioFormat getAudioFormat() {
        return _mono8k;
    }

    /**
     * Return the minimum sample size for use in creating buffers etc.
     *
     * @return int
     */
    public int getSampSz() {
        AudioFormat mine = this.getAudioFormat();
        return (int) (mine.getFrameRate() * mine.getFrameSize() * FRAMEINTERVAL /
                      1000.0);
    }

    /**
     * Read from the Microphone, into the buffer provided, but _only_
     * filling getSampSz() bytes. returns the timestamp of the sample
     * from the audio clock. Does not do buffer prefilling...
     *
     * @param buff byte[]
     * @return long
     * @exception IOException Description of Exception
     */
    public long readDirect(byte[] buff) throws IOException {
        int micnext = _micCount % _rbuffs.length;
        int buffCap = (_bcount - _micCount) % _rbuffs.length;
        long ret = 0;
        _localMode = true; // Get the sample timing machinery a rest...

        while (buffCap <= 0) {
            // This is only for local audio... busy looping seems ok
            try {
                Thread.sleep(20);
            }
            catch (InterruptedException ie) {}
            buffCap = (_bcount - _micCount) % _rbuffs.length;
        }
        Log.verb("getting direct audiodata from buffer " + micnext + "/" +
                 buffCap);

        ABuffer ab = _rbuffs[micnext];
        if (ab.isWritten()) {
            ret = ab.getStamp();
            resample(ab.getBuff(), buff);
            ab.setRead();
            _micCount++;
        }
        else {
            System.arraycopy(this._silence, 0, buff, 0, buff.length);
            Log.debug("No data yet");
            ret = ab.getStamp(); // or should we warn them ??
        }
        return ret;
    }

    /**
     * Read from the Microphone, into the buffer provided, but _only_
     * filling getSampSz() bytes. returns the timestamp of the sample
     * from the audio clock.
     *
     * @param buff byte[]
     * @return long
     * @exception IOException Description of Exception
     */
    public long readWithTime(byte[] buff) throws IOException {
        int micnext = _micCount % _rbuffs.length;
        int buffCap = (_bcount - _micCount) % _rbuffs.length;
        long ret = 0;
        Log.verb("getting audiodata from buffer " + micnext + "/" + buffCap);

        ABuffer ab = _rbuffs[micnext];
        if (ab.isWritten() && (_micCount > 0 || buffCap >= _rbuffs.length / 2)) {
            ret = ab.getStamp();
            resample(ab.getBuff(), buff);
            ab.setRead();

            _micCount++;
        }
        else {
            System.arraycopy(this._silence, 0, buff, 0, buff.length);
            Log.debug("Sending silence");
            ret = ab.getStamp(); // or should we warn them ??
        }
        return ret;
    }

    /**
     * Simple downsampler, takes 44k1 audio and
     * downsamples to 8k.
     * Or, takes 8k and copies it on....
     *
     * @param s Description of Parameter
     * @param d Description of Parameter
     */
    void resample(byte[] s, byte[] d) {
        if (s.length == d.length) {
            System.arraycopy(s, 0, d, 0, s.length);
        }
        else if (s.length / 2 == d.length) {
            // Source is stereo, send left channel
            for (int i = 0; i < d.length / 2; i++) {
                d[i * 2] = s[i * 4];
                d[i * 2 + 1] = s[i * 4 + 1];
            }
        }
        else {
            // we assume that it is 44k1 stereo 16 bit and down sample
            // nothing clever - no anti alias etc....

            ByteBuffer bbs = ByteBuffer.wrap(s);
            ByteBuffer bbd = ByteBuffer.wrap(d);
            // iterate over the values we have,
            // add them to the taget bucket they fall into
            // and count the drops....
            int drange = d.length / 2;
            double v[] = new double[drange];
            double w[] = new double[drange];

            double rat = 8000.0 / 44100.0;
            int top = s.length / 2;
            for (int eo = 0; eo < top; eo++) {
                int samp = (int) Math.floor(eo * rat);
                if (samp >= drange) {
                    samp = drange - 1;
                }
                v[samp] += bbs.getShort(eo * 2);
                w[samp]++;
            }
            // now reweight the samples to ensure
            // no volume quirks
            // and move to short
            short vw = 0;
            for (int ei = 0; ei < drange; ei++) {
                if (w[ei] != 0) {
                    vw = (short) (v[ei] / w[ei]);
                }
                bbd.putShort(ei * 2, vw);
            }
        }
    }

    /**
     * stop the reccorder - but don't throw it away.
     */
    public void stopRec() {
        _targetDataLine.stop();
        Log.debug("recline Stop");
        _micTh = null;
        this._audioSender = null;

    }

    /**
     * start the recorder
     *
     * @return Description of the Returned Value
     */

    public long startRec() {
        if (_targetDataLine.available() > 0) {
            _targetDataLine.flush();
            Log.debug("flushed recorded data");
            _lastMicTime = Long.MAX_VALUE; // Get rid of spurious samples
        }
        else {
            _lastMicTime = 0;
        }
        _targetDataLine.start();
        _micSpeakOffsetValid = false;

        //clean rbuffs pointers & buffers -tron
        _bcount = _micCount = 0;
        _micSpeakOffset = 0;
        for (int i = 0; i < _rbuffs.length; i++) {
            _rbuffs[i].setRead();
        }

        _micTh = new Thread(this, "microphone");
        _micTh.setDaemon(true);
        _micTh.setPriority(Thread.MAX_PRIORITY - 1);
        _micTh.start();

        Log.debug("Starting Microphone thread");

        Log.debug("recline Start");
        return this._targetDataLine.getMicrosecondPosition() / 1000;
    }

    /**
     * The Audio properties have changed so attempt to re connect to a
     * new device
     */
    public void changedProps() {

    }

    /**
     * Start the player
     */
    public void startPlay() {

        //_sourceDataLine.flush();
        _sourceDataLine.start();

        _canWrite = true;
        _localMode = false;


        Log.debug("playline start");

    }

    /**
     * Stop the player
     */
    public void stopPlay() {

        // reset the buffer
        _ofno = 0;
        _nwrite = 0;
        _canWrite = false;

        _sourceDataLine.stop();
        if (_fudge != 0) {
            Log.warn("total sample skew" + _fudge);
            Log.warn("total call Length ms" + _callLen);
            Log.warn("Percentage:" + (100.0 * _fudge / (8 * _callLen)));
            _fudge = 0;
            _callLen = 0;
        }
        Log.debug("playline stopped");
        _sourceDataLine.flush();
        Log.debug("playline drained");

    }

    /**
     * play the sample given (getSampSz() bytes) assuming that it's
     * timestamp is long
     *
     * @param buff byte[]
     * @param timestamp long
     * @exception IOException Description of Exception
     */

    public void write(byte[] buff, long timestamp) throws IOException {

        int fno = (int) (timestamp / (this.FRAMEINTERVAL));

        ABuffer ab = _pbuffs[fno % _pbuffs.length];
        byte nbuff[] = ab.getBuff();
        if (AudioProperties.isStereoRec()) {
            for (int i = 0; i < nbuff.length / 4; i++) {
                nbuff[i * 4] = 0; // Left silent
                nbuff[i * 4 + 1] = 0; // Left silent
                nbuff[i * 4 + 2] = buff[i * 2];
                nbuff[i * 4 + 3] = buff[i * 2 + 1];
            }
        }
        else {
            System.arraycopy(buff, 0, nbuff, 0, nbuff.length);
        }
        ab.setWritten();
        ab.setStamp(timestamp);
        _ofno = fno;
        Log.verb("queued packet " + _ofno);

        // notifyAll();
        // Here or atframeTime() ?
        // writeBuff(_ofno);

    }

    /**
     * Description of the Method
     *
     * @param n Description of Parameter
     */
    void conceal(int n) {
        byte[] target = _pbuffs[n % _pbuffs.length].getBuff();
        byte[] prev = _pbuffs[ (n - 1) % _pbuffs.length].getBuff();
        byte[] next = _pbuffs[ (n + 1) % _pbuffs.length].getBuff();
        conceal(target, prev, next);
    }

    /**
     * make up a packet from 'nowhere'
     * We just average the corresponding bytes in the
     * surrounding packets and hope it sounds better
     * than silence.
     *
     * @param targ output buffer
     * @param before preceeding packet
     * @param after following packet
     */
    void conceal(byte[] targ, byte[] before, byte[] after) {
        for (int i = 0; i < targ.length; i++) {
            // to do... fix for 16 bit etc
            targ[i] = (byte) ( (0xff) & ( (before[i] >> 1) + (after[i] >> 1)));
        }
    }

    /**
     * Description of the Method
     */

    private long writeBuff() {
        int top = _ofno;
        int sz = 320;
        boolean fudgeSynch = true;
        int frameSize = _sourceDataLine.getFormat().getFrameSize();

        if (top - _nwrite > _pbuffs.length) {
            if (_nwrite == 0) {
                _nwrite = top;
            }
            else {
                _nwrite = top - _pbuffs.length / 2;
            }
            Log.debug("skipping to " + _nwrite);
        }
        if (!_canWrite) {
            if (top - _nwrite >= (DEPTH + LLBS) / 2) {
                /* We start when we have half full the buffers,
                 * DEPTH is usable buffer cap, size is twice that to keep history for AEC
                 *
                 * offset is:
                 *    _nwrite (first buffer we are sending now to speaker)
                 *  - _bcount (last buffer we got from mic)
                 *  - 2 (1 to account for next mic buffer, 1 for the time to drain a buffer)
                 */
                if (_lastMicTime > 0 && _lastMicTime != Long.MAX_VALUE) {
                    // I.e. mic is running
                    _micSpeakOffset = _nwrite - _bcount - 2;
                    _micSpeakOffsetValid = true;
                    Log.debug("Set micSpeakOffset " + _micSpeakOffset);
                }
                startPlay();
                _first = true;
            }
            else {
                return 20;
            }
        }
        Log.verb("starting top="+top+" nwrite ="+_nwrite);

        for (; _nwrite <= top; _nwrite++) {
            ABuffer ab = _pbuffs[_nwrite % _pbuffs.length];
            byte[] obuff = ab.getBuff();
            int avail = _sourceDataLine.available() / (obuff.length + 2);
            sz = obuff.length;

            if (avail > 0) {
                if (!ab.isWritten()) {
                    Log.verb("missing packet...." + _nwrite);
                    // when do we decide to conceal it vs
                    // trying again ?
                    // 2 interesting cases -
                    boolean watergate = false;
                    if (avail > LLBS - 2) {
                        Log.debug("Running out of sound " + _nwrite);
                        watergate = true;
                    }
                    if ( (top - _nwrite) >= (_pbuffs.length - 2)) {
                        Log.debug("Running out of buffers " + _nwrite);
                        watergate = true;
                    }
                    if (_nwrite == 0) {
                        Log.debug("no data to conceal with. " + _nwrite);
                        watergate = false;
                    }
                    if (watergate) {
                        Log.debug("concealing missing data for " + _nwrite);
                        conceal(_nwrite);
                    }
                    else {
                        Log.debug("waiting for missing data for " + _nwrite);
                        break;
                    }
                }

                int start = 0;
                int len = obuff.length;
                // We do adjustments only if we have a timing reference from mic
                if (fudgeSynch && _lastMicTime > 0 &&
                    _lastMicTime != Long.MAX_VALUE) {
                    // Only one per writeBuff call cause we depend on _lastMicTime
                    fudgeSynch = false;
                    long delta = ab.getStamp() - _lastMicTime;

                    if (_first) {
                        _odelta = delta;
                        _first = false;
                    }
                    else {
                        // if diff is positive, this means that
                        // the source clock is running faster than the audio clock
                        // so we lop a few bytes off and make a note of the
                        // fudge factor
                        // if diff is negative, this means the audio clock
                        // is faster than the source clock
                        // so we make up a couple of samples.
                        // and note down the fudge factor.
                        int diff = (int) (delta - _odelta);
                        int max = (int) Math.round( (LLBS / 2) * FRAMEINTERVAL); // we expect the output buffer to be fullish
                        if (Math.abs(diff) > FRAMEINTERVAL) {
                            Log.verb("delta = " + delta + " diff =" + diff);
                        }

                        if (diff > max) {
                            start = (diff > (LLBS * FRAMEINTERVAL)) ?
                                frameSize * 2 : frameSize; // panic ?
                            len -= start;
                            Log.verb("snip - " + start / frameSize +
                                     " sample(s)");
                            _fudge -= start / frameSize;
                        }
                        if (diff < -1 * FRAMEINTERVAL) {
                            _sourceDataLine.write(obuff, 0, frameSize);
                            Log.verb("paste - added a sample");
                            _fudge += 1;
                        }
                    }
                }
                _sourceDataLine.write(obuff, start, len);
                played(ab);
                _callLen += 20;
                sample(obuff);

                // Log.verb("Written to " + _sourceDataLine.toString());
                ab.setRead();
                Log.verb("took packet " + _nwrite + " dejitter capacity " +
                         (top - _nwrite));
            }
            else {
                // No place for (more?) data in SDLB
                Log.verb("looping top="+top+" nwrite ="+_nwrite);
                break;
            }
        }
        long ttd = ( (sz * LLBS / 2) - _sourceDataLine.available()) / 8;
        return ttd;
    }

    /**
     * played
     * over ridden iin the echocan  version
     * @param ab ABuffer
     */
    void played(ABuffer ab) {
    }

    /**
     * sample
     *
     * @param obuff byte[]
     */
    private void sample(byte[] obuff) {
    }

    /**
     * A unit test for JUnit
     *
     * @exception IOException Description of Exception
     */
    void test() throws IOException {
        long stamp = 0;
        long astart = 0;
        boolean first = true;
        long diff = 0;
        this.startPlay();
        this.startRec();
        byte buff[] = new byte[this.getSampSz()];
        Log.verb("sample size = " + buff.length);
        for (int i = 0; i < 1000; i++) {
            long ts = this.readWithTime(buff);
            if (first) {
                first = false;
                astart = ts;
            }
            ts -= astart;
            diff = ts - stamp;
            Log.verb("diff = " + diff + " ts= " + ts + " stamp =" + stamp);
            this.write(buff, stamp);
            stamp += 20;
        }
        Log.debug("total diff =" + diff);
        this.stopRec();
        this.stopPlay();
    }

    /**
     * Description of the Method
     *
     * @param argv Description of Parameter
     */
    public static void main(String argv[]) {
        Log.setLevel(Log.ALL);
        AudioProperties.loadFromFile("audio.properties");
        Audio8k a8 = new Audio8k();
        try {
            a8.test();
            try {
                Thread.sleep(20000);
            }
            catch (InterruptedException ex1) {
            }
            a8.test();
        }
        catch (IOException ex) {
            Log.debug(ex.getMessage());
        }
    }

    /**
     * getA8k
     *
     * @return Audio8k

         public static Audio8k getA8k() {
        if (__instance == null) {
            __instance = new Audio8k();
        }
        return __instance;
         }
     */
    /**
     * getA8k
     *
     * @param format Integer
     * @return AudioInterface
     */
    public AudioInterface getByFormat(Integer format) {
        AudioInterface ret = null;
        int f = format.intValue();
        switch (f) {
            case VoiceFrame.ALAW_BIT:
                ret = new AudioAlaw(this);
                break;
            case VoiceFrame.ULAW_BIT:
                ret = new AudioUlaw(this);
                break;
            case VoiceFrame.LIN16_BIT:
                ret = this; // amusing no ?
                break;
            default:
                Log.warn("Invalid format for Audio " + f);
                Log.warn("Forced ulaw ");
                ret = new AudioUlaw(this);
                break;
        }
        Log.debug("Using audio Interface of type : " + ret.getClass().getName());
        return ret;
    }

    /**
     * Tell Asterisk which codecs we support.
     * (note ALAW has a 'crackle' bug so it is not included)
     *
     * @return Description of the Returned Value
     */
    public Integer supportedCodecs() {
        int sup = VoiceFrame.ALAW_BIT | VoiceFrame.ULAW_BIT | VoiceFrame.LIN16_BIT;

        return new Integer(sup);
    }

// really want to get this from Audio.prefs
    /**
     * Indicate to Asterisk our preferences - removed alaw due to bug
     *
     * @return Description of the Returned Value
     */
    public String codecPrefString() {
        char[] prefs = {
            VoiceFrame.ULAW_NO,
            VoiceFrame.LIN16_NO,
            VoiceFrame.ALAW_NO
        };
        String ret = "";
        for (int i = 0; i < prefs.length; i++) {
            ret += (char) (prefs[i] + 66);
        }
        return ret;
    }

    private long writeDirectIfAvail(byte[] buff) {
        boolean ret = (_sourceDataLine.available() > buff.length);
        if (ret) {
            _sourceDataLine.write(buff, 0, buff.length);
        }
        long nap = (buff.length * 2 - _sourceDataLine.available()) / 8;
        return nap;
    }

    /**
     * startRinging
     */
    public void startRinging() {
        _providingRingBack = true;
        _sourceDataLine.flush();
        _sourceDataLine.start();
        _rc = 0;
    }

    /**
     * stopRinging
     */
    public void stopRinging() {
        if (_providingRingBack) {
            _providingRingBack = false;
            _sourceDataLine.stop();
            _sourceDataLine.flush();
            _rc = -1;
        }
    }

    /**
     * getFormatBit
     *
     * @return int
     */
    public int getFormatBit() {
        return org.asteriskjava.iax.protocol.VoiceFrame.LIN16_BIT;
    }

    /**
     * run reads from the microphone when available
     */
    public void run() {
        while (_micTh != null) {
            micRead();
        }
    }

    /**
     * Sets the audioSender attribute of the Audio8k object
     *
     * @param as The new audioSender value
     */
    public void setAudioSender(org.asteriskjava.iax.protocol.AudioSender as) {
        _audioSender = as;
    }

    public void playAudioStream(java.io.InputStream in) throws IOException {
        BufferedInputStream buffIn = new BufferedInputStream(in);
        int len = this.getSampSz();
        byte[] buff = new byte[len];
        this.startPlay();
        int readRet = buffIn.read(buff, 0, len);
        while (readRet > -1) {
            this.writeDirect(buff);
            readRet = buffIn.read(buff, 0, len);
        }
        this.stopPlay();

    }

    public void sampleRecord(SampleListener slis) throws IOException {
        int len = this.getSampSz();
        byte[] buff = new byte[len];
        Log.verb("sample size = " + len);
        int sample = 0;
        long ts = 0;
        long stamp = 0;
        long start = this.startRec();
        this.startPlay();
        Log.debug("sampleRecord: start=" + start);
        // does about 10 seconds
        for (int s = 0; s < 50; s++) {
            sample = 0;
            for (int i = 0; i < 10; i++) {
                ts = this.readDirect(buff);
                this.writeDirect(buff);
            }

            // just take one value as sample
            int v = (buff[0] << 8) + (buff[1] & 0xff);
            sample = Math.abs(v);
            slis.setSampleValue(sample);
        }
        this.stopRec();
        this.stopPlay();
        slis.setSampleValue( -1);

    }

    /**
     * writeDirect
     *
     * @param buff byte[]
     */
    public void writeDirect(byte[] buff) {
        _sourceDataLine.write(buff, 0, buff.length);
    }

}
