// NAME
//      $RCSfile: AEC.java,v $
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

import org.asteriskjava.iax.protocol.*;
import java.io.FileOutputStream;
import java.io.*;

/**
 * Acoustic Echo Cancellation functions
 * Based on draft-avt-aec-01 by Andre Adrian, DFS Deutsche Flugsicherung
 * <a href="mailto:Andre.Adrian@dfs.de">
 *
 * @author <a href="mailto:tron@huapi.ba.ar">Carlos Mendioroz</a>
 * @version $Revision$
 */
public class AEC {
    private final static String version_id =
        "@(#)$Id$ Copyright Mexuar Technologies Ltd";

    /* dB Values */
    public static final double M0dB = 1.00;
    public static final double M3dB = 0.71;
    public static final double M6dB = 0.50;
    public static final double M9dB = 0.35;
    public static final double M12dB = 0.25;
    public static final double M18dB = 0.125;
    public static final double M24dB = 0.063;

    /* dB values for 16bit PCM */
    public static final double M10dB_PCM = 10362.0;
    public static final double M20dB_PCM = 3277.0;
    public static final double M25dB_PCM = 1843.0;
    public static final double M30dB_PCM = 1026.0;
    public static final double M35dB_PCM = 583.0;
    public static final double M40dB_PCM = 328.0;
    public static final double M45dB_PCM = 184.0;
    public static final double M50dB_PCM = 104.0;
    public static final double M55dB_PCM = 58.0;
    public static final double M60dB_PCM = 33.0;
    public static final double M65dB_PCM = 18.0;
    public static final double M70dB_PCM = 10.0;
    public static final double M75dB_PCM = 6.0;
    public static final double M80dB_PCM = 3.0;
    public static final double M85dB_PCM = 2.0;
    public static final double M90dB_PCM = 1.0;

    public static final double MAXPCM = 32767.0;

    /* Design constants (Change to fine tune the algorithms */

    /* NLMS filter length in taps (samples). A longer filter length gives
     * better Echo Cancellation, but maybe slower convergence speed and
     * needs more CPU power (Order of NLMS is linear) */
    public static final int NLMS_LEN = (100*8);

    /* minimum energy in xf. Range: M70dB_PCM to M50dB_PCM. Should be equal
     * to microphone ambient Noise level */
    public static final double NoiseFloor = M55dB_PCM;

    /* Initial MIC Gain, 1 = direct */
    public static final double MICGAIN = 1.0;

    /* Leaky hangover in taps.
     */
    public static final int Thold = 60 * 8;

    public static final double PreWhiteTransferFreq = 2000.0;
    public static final double Rate = 8000.0;

    // Adrian soft decision DTD
    // left point. X is ratio, Y is stepsize
    public static final double STEPX1 = 1.0, STEPY1 = 1.0;
    // right point. STEPX2=2.0 is good double talk, 3.0 is good single talk.
    public static final double STEPX2 = 2.5, STEPY2 = 0;
    public static final double ALPHAFAST = 1.0 / 100.0;
    public static final double ALPHASLOW = 1.0 / 20000.0;

    /* Ageing multiplier for LMS memory vector w */
    public static final double Leaky = 0.9999;

    /* Double Talk Detector Speaker/Microphone Threshold. Range <=1
     * Large value (M0dB) is good for Single-Talk Echo cancellation,
     * small value (M12dB) is good for Doulbe-Talk AEC */
    public static final double GeigelThreshold = M6dB;
    public static final double UpdateThreshold = M50dB_PCM;

    /* for Non Linear Processor. Range >0 to 1. Large value (M0dB) is good
     * for Double-Talk, small value (M12dB) is good for Single-Talk */
    public static final double NLPAttenuation = M12dB;

    /* Below this line there are no more design constants */

    /* Exponential Smoothing or IIR Infinite Impulse Response Filter */
    public class IIR {
        double lowpassf;
        static final double ALPHADC = 0.01;  /* controls Transfer Frequency */

        IIR() {
            lowpassf = 0.0;
        }

        double highpass(double in) {
            lowpassf += ALPHADC*(in - lowpassf);
            return in - lowpassf;
          }
    }

    class IIR6 {
        static final int POL =  6;      /* -6dB attenuation per octave per Pol */
        static final double AlphaHp = 0.075; /* controls Transfer Frequence */
        static final double AlphaLp = 0.15; /* controls Transfer Frequence */
        static final double Gain6   = 1.45;  /* gain to undo filter attenuation */

        double lowpassf[];
        double highpassf[];

        IIR6() {
            lowpassf = new double[2*POL+1];
            highpassf = new double[2*POL+1];
            for (int i = 0; i < 2*POL+1; i++) {
                lowpassf[i] = 0;
                highpassf[i] = 0;
            }
        }

        public double highpass(double in) {
            /* Highpass = Signal - Lowpass. Lowpass = Exponential Smoothing */
            highpassf[0] = in;
            for (int i = 0; i < 2*POL; ++i) {
                lowpassf[i+1] += AlphaHp*(highpassf[i] - lowpassf[i+1]);
                highpassf[i+1] = highpassf[i] - lowpassf[i+1];
            }
            return Gain6*highpassf[2*POL];
        }

        public double lowpass(double in) {
            /* Lowpass = Exponential Smoothing */
            lowpassf[0] = in;
            for (int i = 0; i < 2*POL; ++i) {
                lowpassf[i+1] += AlphaLp*(lowpassf[i] - lowpassf[i+1]);
            }
            return lowpassf[2*POL];
        }
    }

    /* Recursive single pole IIR Infinite Impulse response High-pass filter
     *
     * Reference: The Scientist and Engineer's Guide to Digital Processing
     *
     *  output[N] = A0 * input[N] + A1 * input[N-1] + B1 * output[N-1]
     *
     *      X  = exp(-2.0 * pi * Fc)
     *      A0 = (1 + X) / 2
     *      A1 = -(1 + X) / 2
     *      B1 = X
     *      Fc = cutoff freq / sample rate
     */
    class IIR1 {
        double a0, a1, b1;
        double last_in, last_out;
        IIR1(double freq) {
          double x = (double)Math.exp(-2.0 * Math.PI * freq/Rate);

          a0 = (1.0 + x) / 2.0;
          a1 = -(1.0 + x) / 2.0;
          b1 = x;
          last_in = 0.0;
          last_out = 0.0;
        }
        double highpass(double in)  {
            double out = a0 * in + a1 * last_in + b1 * last_out;
            last_in = in;
            last_out = out;
            return out;
         }
    }

    /* Recursive two pole IIR Infinite Impulse Response filter
     * Coefficients calculated with
     * http://www.dsptutor.freeuk.com/IIRFilterDesign/IIRFiltDes102.html
     */
    class IIR2 {
        final double  a[] = { 0.29289323, -0.58578646, 0.29289323 };
        final double  b[] = { 1.3007072E-16, 0.17157288 };
        double x[], y[];
        IIR2() {
            x = new double[2];
            y = new double[2];
        }

      double highpass(double in) {
        // Butterworth IIR filter, Filter type: HP
        // Passband: 2000 - 4000.0 Hz, Order: 2
        double out =
          a[0] * in + a[1] * x[0] + a[2] * x[1] - b[0] * y[0] - b[1] * y[1];

        x[1] = x[0];
        x[0] = in;
        y[1] = y[0];
        y[0] = out;
        return out;
      }
};


    /* 17 taps FIR Finite Impulse Response filter
     * Coefficients calculated with
     * www.dsptutor.freeuk.com/KaiserFilterDesign/KaiserFilterDesign.html
     */
    class FIR_HP_300Hz {
        final double a[] = {
            // Kaiser Window FIR Filter, Filter type: High pass
            // Passband: 300.0 - 4000.0 Hz, Order: 16
            // Transition band: 75.0 Hz, Stopband attenuation: 10.0 dB
            -0.034870606, -0.039650206, -0.044063766, -0.04800318,
            -0.051370874, -0.054082647, -0.056070227, -0.057283327,
            0.8214126, -0.057283327, -0.056070227, -0.054082647,
            -0.051370874, -0.04800318, -0.044063766, -0.039650206,
            -0.034870606, 0.0
        };
        double z[];

        FIR_HP_300Hz() {
            z = new double[18];
        }

        double highpass(double in) {
            System.arraycopy(z, 0, z, 1, 17);
            z[0] = in;
            double sum0 = 0.0, sum1 = 0.0;
            int j;

            for (j = 0; j < 18; j += 2) {
              // optimize: partial loop unrolling
              sum0 += a[j] * z[j];
              sum1 += a[j + 1] * z[j + 1];
            }
            return sum0 + sum1;
        }
    }

    /* Vector Dot Product */
    public static double dotp(double a[], int oa, double b[], int ob, int l) {
      double sum0 = 0.0, sum1 = 0.0;

      for (int i = 0; i < l; i+= 2) {
        // optimize: partial loop unrolling
        sum0 += a[i+oa] * b[i+ob];
        sum1 += a[i+oa+1] * b[i+ob+1];
      }
      return sum0+sum1;
    }

    static final int NLMS_EXT = (10*8);     // Extention in taps to optimize mem copies
    static final int DTD_LEN = 16;          // block size in taps to optimize DTD calculation

    // Time domain Filters
    IIR dc0 = new IIR();    // DC-level running average (IIR highpass)
    IIR dc1 = new IIR();
    FIR_HP_300Hz hp0 = new FIR_HP_300Hz();      // 300Hz cut-off Highpass
    IIR1 Fx = new IIR1(PreWhiteTransferFreq);   // pre-whitening Filter for x
    IIR1 Fe = new IIR1(PreWhiteTransferFreq);   // pre-whitening Filter for e
    double gain = MICGAIN;                      // Mic signal amplify

    // Adrian soft decision DTD (Double Talk Detector)
    double dfast = M75dB_PCM, xfast = M75dB_PCM;
    double dslow = M80dB_PCM, xslow = M80dB_PCM;

    // Geigel DTD (Double Talk Detector)
    double max_max_x = 0.0;                     // max(|x[0]|, .. |x[L-1]|)
    int hangover = 0;
    double max_x[] = new double[NLMS_LEN/DTD_LEN];// optimize: less calculations for max()
    int dtdCnt = 0;
    int dtdNdx = 0;

    // NLMS-pw
    double x[] = new double[NLMS_LEN+NLMS_EXT]; // tap delayed loudspeaker signal
    double xf[] = new double[NLMS_LEN+NLMS_EXT];// pre-whitening tap delayed signal
    double w[] = new double[NLMS_LEN];          // tap weights
    int j = NLMS_EXT;                           // optimize: less memory copies
    double dotp_xf_xf = 0.0;                    // optimize: iterative dotp(x,x)
    double delta = 0.0;                         // noise floor to stabilize NLMS

    void setambient(double Min_xf) {
        dotp_xf_xf -= delta;
        delta = (NLMS_LEN-1) * Min_xf * Min_xf;
        dotp_xf_xf += delta;  // add new delta
    };

    void setgain(double gain) {
        this.gain = gain;
    };

    /* Normalized Least Mean Square Algorithm pre-whitening (NLMS-pw)
     * The LMS algorithm was developed by Bernard Widrow
     * book: Widrow/Stearns, Adaptive Signal Processing, Prentice-Hall, 1985
     * book: Haykin, Adaptive Filter Theory, 4. edition, Prentice Hall, 2002
     *
     * in mic: microphone sample (PCM as floating point value)
     * in spk: loudspeaker sample (PCM as floating point value)
     * in stepsize: NLMS adaptation variable
     * return: echo cancelled microphone sample
     */
    public double nlms_pw(double mic, double spk, double stepsize)
    {
        double d = mic;                 // desired signal

        x[j] = spk;
        xf[j] = Fx.highpass(spk);       // pre-whitening of x

        // calculate error value (mic signal - estimated mic signal from spk signal)
        double e = d;
        if (hangover > 0) {
             e -= dotp(w, 0, x, j, NLMS_LEN);
        }

        double ef = Fe.highpass(e);    // pre-whitening of e

        dotp_xf_xf += (xf[j] * xf[j] - xf[j+NLMS_LEN-1] * xf[j+NLMS_LEN-1]);

        if (stepsize > 0.0) {
            // calculate variable step size
            double mikro_ef = stepsize * ef /dotp_xf_xf;

            // update tap weights (filter learning)
            for (int i = 0; i < NLMS_LEN; i+=2) {
                // optimize: partial loop unrolling
                w[i] += mikro_ef * xf[i+j];
                w[i+1] += mikro_ef * xf[i+j+1];
            }
        }

        if (--j < 0) {
            // optimize: decrease number of memory copies
            j = NLMS_EXT;
            System.arraycopy(x, 0, x, j+1, NLMS_LEN-1);
            System.arraycopy(xf, 0, xf, j+1, NLMS_LEN-1);
        }

        // Saturation
        if (e > MAXPCM) {
            return MAXPCM;
        } else if (e < -MAXPCM) {
            return -MAXPCM;
        } else {
            return e;
        }
    }

    /* Geigel Double-Talk Detector
     *
     * in d: microphone sample (PCM as doubleing point value)
     * in x: loudspeaker sample (PCM as doubleing point value)
     * return: false for no talking, true for talking
     */
    public boolean gdtd(double d, double x)
    {
      // optimized implementation of max(|x[0]|, |x[1]|, .., |x[L-1]|):
      // calculate max of block (DTD_LEN values)
      x = Math.abs(x);
      if (x > max_x[dtdNdx]) {
        max_x[dtdNdx] = x;
        if (x > max_max_x) {
          max_max_x = x;
        }
      }
      if (++dtdCnt >= DTD_LEN) {
        dtdCnt = 0;
        // calculate max of max
        max_max_x = 0.0;
        for (int i = 0; i < NLMS_LEN/DTD_LEN; ++i) {
          if (max_x[i] > max_max_x) {
            max_max_x = max_x[i];
          }
        }
        // rotate Ndx
        if (++dtdNdx >= NLMS_LEN/DTD_LEN) dtdNdx = 0;
        max_x[dtdNdx] = 0.0;
      }

      // The Geigel DTD algorithm with Hangover timer Thold
      if (Math.abs(d) >= GeigelThreshold * max_max_x) {
        hangover = Thold;
      }

      if (hangover > 0) --hangover;

      if (max_max_x < UpdateThreshold) {
        // avoid update with silence
        return true;
      } else {
        return (hangover > 0);
      }
    }

    /*
     * Adrian soft decision DTD
     * (Dual Average Near-End to Far-End signal Ratio DTD)
     * This algorithm uses exponential smoothing with differnt
     * ageing parameters to get fast and slow near-end and far-end
     * signal averages. The ratio of NFRs term
     * (dfast / xfast) / (dslow / xslow) is used to compute the stepsize
     * A ratio value of 2.5 is mapped to stepsize 0, a ratio of 0 is
     * mapped to 1.0 with a limited linear function.
     */
    double adtd(double d, double x)
    {
      double stepsize;

        // fast near-end and far-end average
        dfast += ALPHAFAST * (Math.abs(d) - dfast);
        xfast += ALPHAFAST * (Math.abs(x) - xfast);

        // slow near-end and far-end average
        dslow += ALPHASLOW * (Math.abs(d) - dslow);
        xslow += ALPHASLOW * (Math.abs(x) - xslow);

        if (xfast < M70dB_PCM) {
        return 0.0;   // no Spk signal
        }

        if (dfast < M70dB_PCM) {
        return 0.0;   // no Mic signal
        }

        // ratio of NFRs
        double ratio = (dfast * xslow) / (dslow * xfast);

        // begrenzte lineare Kennlinie
        final double M = (STEPY2 - STEPY1) / (STEPX2 - STEPX1);
        if (ratio < STEPX1) {
            stepsize = STEPY1;
        } else if (ratio > STEPX2) {
            stepsize = STEPY2;
        } else {
            // Punktrichtungsform einer Geraden
            stepsize = M * (ratio - STEPX1) + STEPY1;
        }

        return stepsize;
    }


    // The xfast signal is used to charge the hangover timer to Thold.
    // When hangover expires (no Spk signal for some time) the vector w
    // is erased. This is Adrian implementation of Leaky NLMS.
    void leaky()
    {
        if (xfast >= M70dB_PCM) {
            // vector w is valid for hangover Thold time
            hangover = Thold;
        } else {
            if (hangover > 1) {
                --hangover;
            } else if (1 == hangover) {
                --hangover;
                // My Leaky NLMS is to erase vector w when hangover expires
                w = new double[NLMS_LEN];
            }
        }
    }

    /* Acoustic Echo Cancellation and Suppression of one sample
     * in   s0: microphone signal with echo
     * in   s1: loudspeaker signal
     * return:  echo cancelled microphone signal
     */
    public int doAEC(int y, int x)
    {
      double s0 = (double)y;
      double s1 = (double)x;

      // Mic and Spk signal remove DC (IIR highpass filter)
      s0 = dc0.highpass(s0);
      s1 = dc1.highpass(s1);

      // Mic Highpass Filter - telephone users are used to 300Hz cut-off
      s0 = hp0.highpass(s0);

      // Amplify, for e.g. Soundcards with -6dB max. volume
      s0 *= gain;

      // Double Talk Detector
      double stepsize = adtd(s0, s1);

      // Leaky (ageing of vector w)
      leaky();

      // Acoustic Echo Cancellation
      s0 = nlms_pw(s0, s1, stepsize);

      return (int)s0;
    }

    /* Acoustic Echo Cancellation and Suppression of frame
     * in/out   ybuff: microphone signal with echo
     * in       xbuff: loudspeaker signal
     * return:  echo cancelled microphone signal
     */

    public byte [] process(byte ybuff[], byte xbuff[]) {
        byte ebuff[] = new byte[xbuff.length];

        /*
        ByteBuffer xbb = ByteBuffer.wrap(xbuff);
        ByteBuffer ybb = ByteBuffer.wrap(ybuff);
        ByteBuffer o = ByteBuffer.allocate(ybuff.length);
        for (int i = 0; i< ybuff.length; i+=2){
            int y = ybb.getShort(i);
            int x = xbb.getShort(i);
            int e = doAEC(y,x);
            o.putShort(i,(short)e);
        }
       return o.array();
        */
        for (int i = 0; i < ybuff.length; i+=2) {
            // If there's any way of doing this faster/better...

            int y = (ybuff[i] << 8) +  (ybuff[i+1] & 0xFF);
            int x = (xbuff[i] << 8) +  (xbuff[i+1] & 0xFF);
            int e = doAEC(y, x);
            ebuff[i] = (byte)(e >> 8);
            ebuff[i+1] = (byte)(e & 0xff);
        }
        return ebuff;
    }

    /**
     * writeSample
     *
     * @param buff byte[]
     */
    public void writeSample(byte[] buff,byte[] buff2, int sno) {
        String fname = ""+sno+".raw";
        try {
            FileOutputStream s = new FileOutputStream(fname);
            s.write(buff);
            s.write(buff2);
            s.close();
        }
        catch (IOException ex) {
            Log.warn(ex.getMessage());
        }
    }

    static public void main(String argv[]){
        AEC aec = new AEC();
        int i = 0;
        byte head[] = new byte[24];
        byte buffx[] = new byte[320];
        byte buffy[] = new byte[320];
        byte buffe[];
        Log.setLevel(Log.PROL);
        try {
            FileInputStream fx = new FileInputStream(argv[0]);
            FileInputStream fy = new FileInputStream(argv[1]);
            FileOutputStream fe = new FileOutputStream(argv[2]);
            // Skip over au header, and pass it to output.
            fx.read(head);
            fy.read(head);
            fe.write(head);

            long then = System.currentTimeMillis();
            while (fx.read(buffx) == 320){
                fy.read(buffy);
                buffe = aec.process(buffy, buffx);
                fe.write(buffe);
                i++;
            }
            long now = System.currentTimeMillis();
            Log.warn("AEC took " + (now - then) + " for " + i + " samples.");

            fx.close();
            fy.close();
            fe.close();

        }
        catch (IOException ex) {
            Log.warn(ex.getMessage());
        }

    }
}


