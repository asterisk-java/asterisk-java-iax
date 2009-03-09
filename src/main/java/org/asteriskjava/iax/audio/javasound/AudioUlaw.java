// NAME
//      $RCSfile: AudioUlaw.java,v $
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

public class AudioUlaw
    extends AbstractAudio {
  AudioFormat _ulawFormat;

    private final static String version_id =
        "@(#)$Id$ Copyright Mexuar Technologies Ltd";

  private int _ss;
  private static final boolean ZEROTRAP = true;
  private static final short BIAS = 0x84;
  private static final int CLIP = 32635;
  private static final int exp_lut1[] = {
      0, 0, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3,
      4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
      5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
      5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
      6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
      6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
      6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
      6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
      7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
      7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
      7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
      7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
      7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
      7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
      7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
      7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7
  };

  AudioUlaw(Audio8k a8) {
    _a8 = a8;
    _ss = _a8.getSampSz();
    Log.debug("8k buffer size = " + _ss);
    _obuff = new byte[_ss];
    _ibuff = new byte[_ss];
  }

  /**
   * Converts a linear signed 16bit sample to a uLaw byte.
   * Ported to Java by fb.
   * <br>Originally by:<br>
   * Craig Reese: IDA/Supercomputing Research Center <br>
   * Joe Campbell: Department of Defense <br>
   * 29 September 1989 <br>
   */
  public static byte linear2ulaw(int sample) {
    int sign, exponent, mantissa, ulawbyte;

    if (sample > 32767) {
      sample = 32767;
    }
    else if (sample < -32768) {
      sample = -32768;
      /* Get the sample into sign-magnitude. */
    }
    sign = (sample >> 8) & 0x80; /* set aside the sign */
    if (sign != 0) {
      sample = -sample; /* get magnitude */
    }
    if (sample > CLIP) {
      sample = CLIP; /* clip the magnitude */

      /* Convert from 16 bit linear to ulaw. */
    }
    sample = sample + BIAS;
    exponent = exp_lut1[ (sample >> 7) & 0xFF];
    mantissa = (sample >> (exponent + 3)) & 0x0F;
    ulawbyte = ~ (sign | (exponent << 4) | mantissa);
    if (ZEROTRAP) {
      if (ulawbyte == 0) {
        ulawbyte = 0x02; /* optional CCITT trap */
      }
    }
    return ( (byte) ulawbyte);
  }

  /* u-law to linear conversion table */
  private static short[] u2l = {
      -32124, -31100, -30076, -29052, -28028, -27004, -25980, -24956,
      -23932, -22908, -21884, -20860, -19836, -18812, -17788, -16764,
      -15996, -15484, -14972, -14460, -13948, -13436, -12924, -12412,
      -11900, -11388, -10876, -10364, -9852, -9340, -8828, -8316,
      -7932, -7676, -7420, -7164, -6908, -6652, -6396, -6140,
      -5884, -5628, -5372, -5116, -4860, -4604, -4348, -4092,
      -3900, -3772, -3644, -3516, -3388, -3260, -3132, -3004,
      -2876, -2748, -2620, -2492, -2364, -2236, -2108, -1980,
      -1884, -1820, -1756, -1692, -1628, -1564, -1500, -1436,
      -1372, -1308, -1244, -1180, -1116, -1052, -988, -924,
      -876, -844, -812, -780, -748, -716, -684, -652,
      -620, -588, -556, -524, -492, -460, -428, -396,
      -372, -356, -340, -324, -308, -292, -276, -260,
      -244, -228, -212, -196, -180, -164, -148, -132,
      -120, -112, -104, -96, -88, -80, -72, -64,
      -56, -48, -40, -32, -24, -16, -8, 0,
      32124, 31100, 30076, 29052, 28028, 27004, 25980, 24956,
      23932, 22908, 21884, 20860, 19836, 18812, 17788, 16764,
      15996, 15484, 14972, 14460, 13948, 13436, 12924, 12412,
      11900, 11388, 10876, 10364, 9852, 9340, 8828, 8316,
      7932, 7676, 7420, 7164, 6908, 6652, 6396, 6140,
      5884, 5628, 5372, 5116, 4860, 4604, 4348, 4092,
      3900, 3772, 3644, 3516, 3388, 3260, 3132, 3004,
      2876, 2748, 2620, 2492, 2364, 2236, 2108, 1980,
      1884, 1820, 1756, 1692, 1628, 1564, 1500, 1436,
      1372, 1308, 1244, 1180, 1116, 1052, 988, 924,
      876, 844, 812, 780, 748, 716, 684, 652,
      620, 588, 556, 524, 492, 460, 428, 396,
      372, 356, 340, 324, 308, 292, 276, 260,
      244, 228, 212, 196, 180, 164, 148, 132,
      120, 112, 104, 96, 88, 80, 72, 64,
      56, 48, 40, 32, 24, 16, 8, 0
  };
  public static short ulaw2linear(byte ulawbyte) {
    return u2l[ulawbyte & 0xFF];
  }

  protected AudioFormat getAudioFormat() {
    if (_ulawFormat == null) {
      try {
        _ulawFormat = new AudioFormat(
            AudioFormat.Encoding.ULAW,
            8000.0F, // sampleRate
            8, // sampleSizeInBits
            1, // channels
            1, // frameSize
            8000.0F, // frameRate
            false); // bigEndian

      }
      catch (Exception x) {
        Log.warn(x.getMessage());
      }

    }
    return _ulawFormat;
  }

  public static void main(String[] args) {
    Log.setLevel(Log.ALL);
    AudioProperties.loadFromFile("audio.properties");
    Audio8k a8 = new Audio8k();
    AudioUlaw ulaw = new AudioUlaw(a8);
    try {
      ulaw.test();
    }
    catch (IOException ex) {
      Log.debug(ex.getMessage());
    }

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

  public void convertToLin(byte[] in, byte[] out) {
    convert(in, out);
  }

  public void convertFromLin(byte[] in, byte[] out) {
    ByteBuffer bb = ByteBuffer.wrap(in);
    for (int i = 0; i < out.length; i++) {
      short s = bb.getShort();
      out[i] = linear2ulaw(s);
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
      short s = ulaw2linear(in[i]);
      bb.putShort(s);
    }
  }

  /**
   * getFormatBit
   *
   * @return int
   */
  public int getFormatBit() {
    return org.asteriskjava.iax.protocol.VoiceFrame.ULAW_BIT;
  }
}
