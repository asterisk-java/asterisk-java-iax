
package org.asteriskjava.iax.audio.javasound;

import org.asteriskjava.iax.protocol.AudioSender;

import java.io.IOException;

/**
 * Base class for codecs that can convert to and from SLIN
 * It assumes that Audio8k will find and talk to SLIN hardware
 * It assumes that classes that extend it will implement the
 * abstract methods for their own codecs.
 */
public abstract class AbstractAudio
        implements AudioInterface {


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
    @Override
    public long startRec() {
        return _a8.startRec();
    }

    /**
     * Description of the Method
     */
    @Override
    public void startPlay() {
        _a8.startPlay();
    }

    /**
     * Description of the Method
     */
    @Override
    public void stopRec() {
        _a8.stopRec();
    }

    /**
     * Description of the Method
     *
     * @param in  Description of Parameter
     * @param out Description of Parameter
     */
    public abstract void convertFromLin(byte[] in, byte[] out);

    /**
     * Description of the Method
     *
     * @param in  Description of Parameter
     * @param out Description of Parameter
     */
    public abstract void convertToLin(byte[] in, byte[] out);

    /**
     * Description of the Method
     */
    @Override
    public void stopPlay() {
        _a8.stopPlay();
    }

    /**
     * Description of the Method
     *
     * @param buff Description of Parameter
     * @return Description of the Returned Value
     * @throws IOException Description of Exception
     */
    @Override
    public long readWithTime(byte[] buff) throws IOException {
        long ret = _a8.readWithTime(_ibuff);
        convertFromLin(_ibuff, buff);
        return ret;
    }

    @Override
    public long readDirect(byte[] buff) throws IOException {
        long ret = _a8.readDirect(_ibuff);
        convertFromLin(_ibuff, buff);
        return ret;
    }

    /**
     * Description of the Method
     *
     * @param buff      Description of Parameter
     * @param timestamp Description of Parameter
     * @throws IOException Description of Exception
     */
    @Override
    public void write(byte[] buff, long timestamp) throws IOException {
        convertToLin(buff, _obuff);
        _a8.write(_obuff, timestamp);
    }

    /**
     * Description of the Method
     *
     * @param f Description of Parameter
     */
    @Override
    public void writeDirect(byte[] f) {
        byte[] tf = new byte[2 * f.length];
        convertToLin(f, tf);
        _a8.writeDirect(tf);
    }


    /**
     * startRinging
     */
    @Override
    public void startRinging() {
        _a8.startRinging();
    }

    /**
     * stopRinging
     */
    @Override
    public void stopRinging() {
        _a8.stopRinging();
    }

    /**
     * Gets the formatBit attribute of the AbstractAudio object
     *
     * @return The formatBit value
     */
    @Override
    public abstract int getFormatBit();

    /**
     * Sets the audioSender attribute of the AbstractAudio object
     *
     * @param as The new audioSender value
     */
    @Override
    public void setAudioSender(AudioSender as) {
        _a8.setAudioSender(as);
    }

    /**
     * Play an audio Stream,
     *
     * @param in InputStream
     * @throws IOException
     */
    @Override
    public void playAudioStream(java.io.InputStream in) throws IOException {
        _a8.playAudioStream(in);
    }

    /**
     * produce occasional sample values
     *
     * @param slis SampleListener
     * @throws IOException
     */


    @Override
    public Integer supportedCodecs() {
        return _a8.supportedCodecs();
    }

    @Override
    public String codecPrefString() {
        return _a8.codecPrefString();
    }

    @Override
    public void cleanUp() {
        _a8.cleanUp();
    }

    @Override
    public AudioInterface getByFormat(Integer format) {
        return _a8.getByFormat(format);
    }
}
