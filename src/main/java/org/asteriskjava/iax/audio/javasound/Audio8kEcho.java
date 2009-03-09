package org.asteriskjava.iax.audio.javasound;

import java.io.*;
import org.asteriskjava.iax.audio.*;
import org.asteriskjava.iax.protocol.*;

class Audio8kEcho
    extends Audio8k {
    private RandomAccessFile micRaf = null, aecRaf = null, speakRaf = null;
    private AEC _aec;
    private RandomAccessFile _rawMic;

    void copyRight(byte[] s, byte[] d) {
        // Source is stereo, send right channel
        for (int i = 0; i < d.length / 2; i++) {
            d[i * 2] = s[i * 4 + 2];
            d[i * 2 + 1] = s[i * 4 + 3];
        }
    }

    /**
     * played
     *
     * @param ab ABuffer
     */
     void played(ABuffer ab) {
        // Try to workout the  time the last byte of this buffer
        // will come out of the speaker..... but measured on the _microphone_ clock
        // for the benefit of the echocan....
        long soundPlay = _targetDataLine.getMicrosecondPosition(); // now
        int avail = this._sourceDataLine.available();
        int sz = this._sourceDataLine.getBufferSize();
        int queued = sz - avail;
        long qtime = queued *125;
        soundPlay += qtime;
        ab.setAStamp(soundPlay/1000);
    }

    public long readWithTime(byte[] buff) throws IOException {
        long ret = super.readWithTime(buff);
        boolean doStereoRec = AudioProperties.isStereoRec();
        int micnext = (_micCount - 1) % _rbuffs.length;
        ABuffer ab = _rbuffs[micnext];

        if (_canWrite) {
            if (micRaf != null) {
                try {
                    micRaf.write(buff);
                }
                catch (IOException ioe) {}
            }
            if (_rawMic != null) {
                try {
                    _rawMic.write(ab.getBuff());
                }
                catch (IOException ioe) {}
            }

            if (_aec != null) {
                // We should do AEC here
                // Problem is finding WHICH play buffer to use :(
                byte sbuff[];
                int sb = 0;
                if (doStereoRec) {
                    sbuff = new byte[320];
                    copyRight(ab.getBuff(), sbuff);
                }
                else {
                    // Use _micSpeakOffset for now.
                    sb = _micCount + _micSpeakOffset;
                    ABuffer sab = _pbuffs[sb % _pbuffs.length];
                    sbuff = sab.getBuff();
                }
                long start = System.currentTimeMillis();
                byte[] ebuff = _aec.process(buff, sbuff);
                System.arraycopy(ebuff, 0, buff, 0, ebuff.length);
                long tdiff = (System.currentTimeMillis() - start);
                if (tdiff > 10) {
                    Log.warn("AEC took " + tdiff);
                }
                if (!doStereoRec && (tdiff > 20) && (Log.getLevel() > 5)) {
                    _aec.writeSample(buff, sbuff, sb);
                }

                if (aecRaf != null) {
                    try {
                        aecRaf.write(buff);
                    }
                    catch (IOException ioe) {}
                }
            }
        }
        return ret;
    }

    public void stopRec() {
        super.stopRec();
        if (micRaf != null) {
            try {
                micRaf.close();
            }
            catch (IOException ioe) {}
            micRaf = null;
        }
        if (_rawMic != null) {
            try {
                _rawMic.close();
            }
            catch (IOException ioe) {}
            _rawMic = null;
        }
        if (aecRaf != null) {
            try {
                aecRaf.close();
            }
            catch (IOException ioe) {}
            aecRaf = null;
        }
    }

    public long startRec() {
        long ret = super.startRec();
        boolean doMonitor = AudioProperties.isMonitor();
        boolean doAEC = AudioProperties.isAEC();
        boolean doStereo = AudioProperties.isStereoRec();

        if (doMonitor) {
            try {
                File micFile = new File("mic.snd");
                micFile.delete();
                micRaf = new RandomAccessFile(micFile, "rw");
                micRaf.writeInt(0x2e736e64);
                micRaf.writeInt(0x18);
                micRaf.writeInt(0xffffffff);
                micRaf.writeInt(3);
                micRaf.writeInt(8000);
                micRaf.writeInt(1);
            }
            catch (IOException ioe) {
                micRaf = null;
            }
            if (_targetDataLine.getFormat().getSampleRate() > 8000.0 ||
                doStereo) {
                try {
                    File rawFile = new File("rawmic.snd");
                    rawFile.delete();
                    _rawMic = new RandomAccessFile(rawFile, "rw");
                    _rawMic.writeInt(0x2e736e64);
                    _rawMic.writeInt(0x18);
                    _rawMic.writeInt(0xffffffff);
                    _rawMic.writeInt(3);
                    _rawMic.writeInt( (int) _targetDataLine.getFormat().
                                     getSampleRate());
                    if (doStereo) {
                        _rawMic.writeInt(2);
                    }
                    else {
                        _rawMic.writeInt(1);
                    }
                }
                catch (IOException ioe) {
                    _rawMic = null;
                }

            }
            // We might as well write in stereo...
            if (doAEC) {
                try {
                    File aecFile = new File("aec.snd");
                    aecFile.delete();
                    aecRaf = new RandomAccessFile(aecFile, "rw");
                    aecRaf.writeInt(0x2e736e64);
                    aecRaf.writeInt(0x18);
                    aecRaf.writeInt(0xffffffff);
                    aecRaf.writeInt(3);
                    aecRaf.writeInt(8000);
                    aecRaf.writeInt(1);
                }
                catch (IOException ioe) {
                    aecRaf = null;
                }
            }

        }
        return ret;
    }

    public void startPlay() {
        super.startPlay();
        boolean doAEC = AudioProperties.isAEC();
        boolean doStereo = AudioProperties.isStereoRec();

        if (doAEC) {
            _aec = new AEC();
        }

        boolean doMonitor = AudioProperties.isMonitor();
        if (doMonitor) {
            try {
                File speakFile = new File("speak.snd");
                speakFile.delete();
                speakRaf = new RandomAccessFile(speakFile, "rw");
                // Signature, hdr size, size, type (16bit signed), sample rate, channels
                speakRaf.writeInt(0x2e736e64);
                speakRaf.writeInt(0x18);
                speakRaf.writeInt(0xffffffff);
                speakRaf.writeInt(3);
                speakRaf.writeInt(8000);
                if (doStereo) {
                    speakRaf.writeInt(2);
                }
                else {
                    speakRaf.writeInt(1);
                }
            }
            catch (IOException ioe) {
                speakRaf = null;
            }
        }
    }

    public void stopPlay() {
        super.stopPlay();
        _aec = null;
        if (speakRaf != null) {
            try {
                speakRaf.close();
            }
            catch (IOException ioe) {}
            speakRaf = null;
        }
    }

    void sample(byte[] buff) {
        if (speakRaf != null) {
            try {
                speakRaf.write(buff);
            }
            catch (IOException ioe) {}
        }

    }

}
