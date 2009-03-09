// NAME
//      $RCSfile: AbstractAudio.java,v $
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
import org.asteriskjava.iax.audio.*;
import org.asteriskjava.iax.protocol.*;

/**
 * Base class for codecs that can convert to and from SLIN
 * It assumes that Audio8k will find and talk to SLIN hardware
 * It assumes that classes that extend it will implement the
 * abstract methods for their own codecs.
 *
 * @version $Revision$ $Date$
 */
public abstract class AbstractAudio
    implements AudioInterface {

    private final static String version_id =
        "@(#)$Id$ Copyright Mexuar Technologies Ltd";

    /**
     * Description of the Field
     */
    protected Audio8k _a8;

    /**
     * Description of the Field
     */
    protected byte[] _obuff;

    /**
     * Description of the Field
     */
    protected byte[] _ibuff;

    /**
     * Description of the Method
     *
     * @return Description of the Returned Value
     */
    public long startRec() {
        return _a8.startRec();
    }

    /**
     * Description of the Method
     */
    public void startPlay() {
        _a8.startPlay();
    }

    /**
     * Description of the Method
     */
    public void stopRec() {
        _a8.stopRec();
    }

    /**
     * Description of the Method
     *
     * @param in Description of Parameter
     * @param out Description of Parameter
     */
    public abstract void convertFromLin(byte[] in, byte[] out);

    /**
     * Description of the Method
     *
     * @param in Description of Parameter
     * @param out Description of Parameter
     */
    public abstract void convertToLin(byte[] in, byte[] out);

    /**
     * Description of the Method
     */
    public void stopPlay() {
        _a8.stopPlay();
    }

    /**
     * Description of the Method
     *
     * @param buff Description of Parameter
     * @return Description of the Returned Value
     * @exception IOException Description of Exception
     */
    public long readWithTime(byte[] buff) throws IOException {
        long ret = _a8.readWithTime(_ibuff);
        convertFromLin(_ibuff, buff);
        return ret;
    }

    public long readDirect(byte[] buff) throws IOException {
        long ret = _a8.readDirect(_ibuff);
        convertFromLin(_ibuff, buff);
        return ret;
    }

    /**
     * Description of the Method
     *
     * @param buff Description of Parameter
     * @param timestamp Description of Parameter
     * @exception IOException Description of Exception
     */
    public void write(byte[] buff, long timestamp) throws IOException {
        convertToLin(buff, _obuff);
        _a8.write(_obuff, timestamp);
    }

    /**
     * Description of the Method
     *
     * @param f Description of Parameter
     */
    public void writeDirect(byte[] f) {
        byte[] tf = new byte[2 * f.length];
        convertToLin(f, tf);
        _a8.writeDirect(tf);
    }

    /**
     * A unit test for JUnit
     *
     * @exception IOException Description of Exception
     */
    protected void test() throws IOException {
        boolean first = true;
        long start = this.startRec();
        long stamp = 0;
        this.startPlay();
        byte buff[] = new byte[this.getSampSz()];
        Log.verb("sample size = " + buff.length);
        for (int i = 0; i < 1000; i++) {
            long ts = this.readWithTime(buff);
            Log.verb("ts= " + ts + " stamp =" + stamp);
            this.write(buff, stamp);
            stamp += 20;
        }
    }

    /**
     * startRinging
     */
    public void startRinging() {
        _a8.startRinging();
    }

    /**
     * stopRinging
     */
    public void stopRinging() {
        _a8.stopRinging();
    }

    /**
     * Gets the formatBit attribute of the AbstractAudio object
     *
     * @return The formatBit value
     */
    public abstract int getFormatBit();

    /**
     * Sets the audioSender attribute of the AbstractAudio object
     *
     * @param as The new audioSender value
     */
    public void setAudioSender(org.asteriskjava.iax.protocol.AudioSender as) {
        _a8.setAudioSender(as);
    }

    /**
     * Play an audio Stream,
     * @param in InputStream
     * @throws IOException
     */
    public void playAudioStream(java.io.InputStream in) throws IOException {
        _a8.playAudioStream(in);
    }

    /**
     * produce occasional sample values
     * @param slis SampleListener
     * @throws IOException
     */
    public void sampleRecord(SampleListener slis) throws IOException {
        _a8.sampleRecord(slis);
    }

    public Integer supportedCodecs() {
        return _a8.supportedCodecs();
    }

    public String codecPrefString() {
        return _a8.codecPrefString();
    }
    public void cleanUp(){
        _a8.cleanUp();
    }
    public  AudioInterface getByFormat(Integer format){
        return _a8.getByFormat(format);
    }
}
