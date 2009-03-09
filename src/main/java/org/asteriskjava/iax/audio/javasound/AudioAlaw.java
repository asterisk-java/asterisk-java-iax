// NAME
//      $RCSfile: AudioAlaw.java,v $
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
import org.asteriskjava.iax.util.*;
import javax.sound.sampled.*;
import org.asteriskjava.iax.protocol.*;

/**
 * Description of the Class
 *
 * @author <a href="mailto:snmp@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision$ $Date$
 */
public class AudioAlaw extends AbstractAudio {

    private final static String version_id =
            "@(#)$Id$ Copyright Mexuar Technologies Ltd";

    AudioFormat _alawFormat;
    private int _ss;


    /**
     * Constructor for the AudioAlaw object
     */
    public AudioAlaw(Audio8k a8) {
        _a8 = a8;
        _ss = _a8.getSampSz();
        Log.debug("8k buffer size = " + _ss);
        _obuff = new byte[_ss];
        _ibuff = new byte[_ss];

    }

    //////////////////// ALAW ////////////////////////////

    /*
       This source code is a product of Sun Microsystems, Inc. and is provided
       for unrestricted use.  Users may copy or modify this source code without
       charge.
       linear2alaw() - Convert a 16-bit linear PCM value to 8-bit A-law
       linear2alaw() accepts an 16-bit integer and encodes it as A-law data.
       Linear Input Code	Compressed Code
       ------------------------	---------------
       0000000wxyza			000wxyz
       0000001wxyza			001wxyz
       000001wxyzab			010wxyz
       00001wxyzabc			011wxyz
       0001wxyzabcd			100wxyz
       001wxyzabcde			101wxyz
       01wxyzabcdef			110wxyz
       1wxyzabcdefg			111wxyz
       For further information see John C. Bellamy's Digital Telephony, 1982,
       John Wiley & Sons, pps 98-111 and 472-476.
      */
    /* Quantization field mask.  */
    private final static byte QUANT_MASK = 0xf;

    /* Left shift for segment number.  */
    private final static byte SEG_SHIFT = 4;
    private final static short[] seg_end = {
            0xFF, 0x1FF, 0x3FF, 0x7FF, 0xFFF, 0x1FFF, 0x3FFF, 0x7FFF
            };


    /**
     * Description of the Method
     *
     * @param pcm_val Description of Parameter
     * @return Description of the Returned Value
     */
    public static byte linear2alaw(short pcm_val) {
        /* 2's complement (16-bit range) */
        byte mask;
        byte seg = 8;
        byte aval;

        if (pcm_val >= 0) {
            mask = (byte) 0xD5;
            /* sign (7th) bit = 1 */
        } else {
            mask = 0x55;
            /* sign bit = 0 */
            pcm_val = (short) (-pcm_val - 8);
        }

        /*
           Convert the scaled magnitude to segment number.
         */
        for (int i = 0; i < 8; i++) {
            if (pcm_val <= seg_end[i]) {
                seg = (byte) i;
                break;
            }
        }

        /* Combine the sign, segment, and quantization bits.  */
        if (seg >= 8) {
            /* out of range, return maximum value.  */
            return (byte) ((0x7F ^ mask) & 0xFF);
        } else {
            aval = (byte) (seg << SEG_SHIFT);
            if (seg < 2) {
                aval |= (pcm_val >> 4) & QUANT_MASK;
            } else {
                aval |= (pcm_val >> (seg + 3)) & QUANT_MASK;
            }
            return (byte) ((aval ^ mask) & 0xFF);
        }
    }


    private static short[] a2l = {
            -5504, -5248, -6016, -5760, -4480, -4224, -4992, -4736,
            -7552, -7296, -8064, -7808, -6528, -6272, -7040, -6784,
            -2752, -2624, -3008, -2880, -2240, -2112, -2496, -2368,
            -3776, -3648, -4032, -3904, -3264, -3136, -3520, -3392,
            -22016, -20992, -24064, -23040, -17920, -16896, -19968, -18944,
            -30208, -29184, -32256, -31232, -26112, -25088, -28160, -27136,
            -11008, -10496, -12032, -11520, -8960, -8448, -9984, -9472,
            -15104, -14592, -16128, -15616, -13056, -12544, -14080, -13568,
            -344, -328, -376, -360, -280, -264, -312, -296,
            -472, -456, -504, -488, -408, -392, -440, -424,
            -88, -72, -120, -104, -24, -8, -56, -40,
            -216, -200, -248, -232, -152, -136, -184, -168,
            -1376, -1312, -1504, -1440, -1120, -1056, -1248, -1184,
            -1888, -1824, -2016, -1952, -1632, -1568, -1760, -1696,
            -688, -656, -752, -720, -560, -528, -624, -592,
            -944, -912, -1008, -976, -816, -784, -880, -848,
            5504, 5248, 6016, 5760, 4480, 4224, 4992, 4736,
            7552, 7296, 8064, 7808, 6528, 6272, 7040, 6784,
            2752, 2624, 3008, 2880, 2240, 2112, 2496, 2368,
            3776, 3648, 4032, 3904, 3264, 3136, 3520, 3392,
            22016, 20992, 24064, 23040, 17920, 16896, 19968, 18944,
            30208, 29184, 32256, 31232, 26112, 25088, 28160, 27136,
            11008, 10496, 12032, 11520, 8960, 8448, 9984, 9472,
            15104, 14592, 16128, 15616, 13056, 12544, 14080, 13568,
            344, 328, 376, 360, 280, 264, 312, 296,
            472, 456, 504, 488, 408, 392, 440, 424,
            88, 72, 120, 104, 24, 8, 56, 40,
            216, 200, 248, 232, 152, 136, 184, 168,
            1376, 1312, 1504, 1440, 1120, 1056, 1248, 1184,
            1888, 1824, 2016, 1952, 1632, 1568, 1760, 1696,
            688, 656, 752, 720, 560, 528, 624, 592,
            944, 912, 1008, 976, 816, 784, 880, 848
            };


    /**
     * Description of the Method
     *
     * @param ulawbyte Description of Parameter
     * @return Description of the Returned Value
     */
    public static short alaw2linear(byte ulawbyte) {
        return a2l[ulawbyte & 0xFF];
    }


    /**
     * Gets the audioFormat attribute of the AudioAlaw object
     *
     * @return The audioFormat value
     */
    protected AudioFormat getAudioFormat() {
        if (_alawFormat == null) {
            try {
                _alawFormat = new AudioFormat(
                        AudioFormat.Encoding.ALAW,
                        8000.0F, // sampleRate
                8, // sampleSizeInBits
                1, // channels
                1, // frameSize
                8000.0F, // frameRate
                false);// bigEndian

            }
            catch (Exception x) {
                Log.warn(x.getMessage());
            }
        }
        return _alawFormat;
    }



    /**
     * changedProps
     */
    public void changedProps() {
    }


    /**
     * getSampSz
     *
     * @return int
     */
    public int getSampSz() {
        return 160;
    }


    /**
     * Description of the Method
     *
     * @param in Description of Parameter
     * @param out Description of Parameter
     */
    public void convertToLin(byte[] in, byte[] out) {
        convert(in, out);
    }


    /**
     * Description of the Method
     *
     * @param in Description of Parameter
     * @param out Description of Parameter
     */
    public void convertFromLin(byte[] in, byte[] out) {
        ByteBuffer bb = ByteBuffer.wrap(in);
        for (int i = 0; i < in.length / 2; i++) {
            short s = bb.getShort();
            out[i] = linear2alaw(s);
        }
    }


    /**
     * The main program for the AudioAlaw class
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        Log.setLevel(Log.ALL);
        AudioProperties.loadFromFile("audio.properties");
        Audio8k a8 = new Audio8k();
        AudioAlaw alaw = new AudioAlaw(a8);
        try {
            alaw.test();
        }
        catch (IOException ex) {
            Log.debug(ex.getMessage());
        }

    }


    /**
     * convert
     *
     * @param in byte[]
     * @param out byte[]
     */
    public static void convert(byte[] in, byte[] out) {
        ByteBuffer bb = ByteBuffer.wrap(out);
        for (int i = 0; i < in.length; i++) {
            short s = alaw2linear(in[i]);
            bb.putShort(s);
        }
    }


    /**
     * getFormatBit
     *
     * @return int
     */
    public int getFormatBit() {
        return VoiceFrame.ALAW_BIT;
    }

}

