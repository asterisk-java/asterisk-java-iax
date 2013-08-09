
package org.asteriskjava.iax.audio.javasound;

import org.asteriskjava.iax.audio.encodings.gsm.Encoder;
import org.asteriskjava.iax.audio.encodings.gsm.GSMDecoder;
import org.asteriskjava.iax.protocol.Log;
import org.asteriskjava.iax.protocol.VoiceFrame;
import org.asteriskjava.iax.util.ByteBuffer;


/**
 * @author Sebastian
 */
public class AudioGSM extends AbstractAudio {

    private Encoder encoder;

    AudioGSM(Audio8k a8) {
        _a8 = a8;
        _obuff = new byte[320];
        _ibuff = new byte[a8.getSampSz()];
        encoder = new Encoder();
    }

    @Override
    public void convertFromLin(byte[] in, byte[] out) {

        short[] sbuff = new short[160];

        ByteBuffer bb = ByteBuffer.wrap(in);
        for (int i = 0; i < in.length / 2; i++) {
            short s = bb.getShort();
            sbuff[i] = s;
        }

        encoder.encode(sbuff, out);

    }

    @Override
    public void convertToLin(byte[] in, byte[] out) {
        try {

            int[] is = GSMDecoder.decode(in);
            for (int i = 0; i < is.length; i++) {
                _obuff[i * 2] = (byte) ((is[i] >> 8));
                _obuff[1 + i * 2] = (byte) (0xff & (is[i]));
            }

        } catch (Exception ex) {
            Log.warn(ex.getMessage());
        }

    }

    @Override
    public int getFormatBit() {
        return VoiceFrame.GSM_BIT;
    }

    @Override
    public int getSampSz() {
        return 33;
    }

    @Override
    public void changedProps() {

    }


}
