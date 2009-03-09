// NAME
//      $RCSfile: Drift.java,v $
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

import javax.sound.sampled.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

class Drift {
    private final static String version_id =
        "@(#)$Id$ Copyright Mexuar Technologies Ltd";

  public Drift() {
    AudioFormat stereo44k = new AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        44100.0F, 16, 2, 4, 44100.0F, false);
    int bsz = 2 * 4410;
    TargetDataLine targetDataLine = null;

    String pref = "";

    DataLine.Info info = new DataLine.Info(TargetDataLine.class, stereo44k);
    try {
      if (pref != null) {
        Mixer.Info[] mixes = AudioSystem.getMixerInfo();
        for (int i = 0; i < mixes.length; i++) {
          Mixer.Info mixi = mixes[i];
          String mixup = mixi.getName();
          System.out.println("Mix " + i + " " + mixup);
          if (mixup.equals(pref)) {
            Mixer preferedMixer = AudioSystem.getMixer(mixi);
            System.out.println("Found name match for prefered input mixer");
            if (preferedMixer.isLineSupported(info)) {
              targetDataLine = (TargetDataLine) preferedMixer.getLine(info);
              System.out.println("got targetLine");
              break;
            }
            else {
              System.out.println("Recording format not supported");
            }
          }
        }
      }
      else {
        targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
      }
      targetDataLine.open(stereo44k, bsz);
      targetDataLine.start();
    }
    catch (Exception x) {
      x.printStackTrace();
    }
    int i = 0;
    byte buff[] = new byte[bsz];
    long clockThen = System.currentTimeMillis();
    long audThen = targetDataLine.getMicrosecondPosition();
    while (i < 5000) {
      targetDataLine.read(buff, 0, bsz);
      if ( (i % 100) == 0) {
        System.out.print(".");
      }
      i++;
    }
    System.out.println();
    long clockNow = System.currentTimeMillis();
    long audNow = targetDataLine.getMicrosecondPosition();
    long cdiff = clockNow - clockThen;
    long adiff = audNow - audThen;
    adiff = adiff / 1000;
    System.out.println("cdiff = " + cdiff);
    System.out.println("adiff = " + adiff);
    long drift = cdiff - adiff;
    System.out.println("drift =" + drift+ "ms");
    double pct = 100.0 * drift / cdiff;
    System.out.println("Pct =" + pct);
    System.out.println("Over a 60 min call ="+(pct * 60.0*60.0/100.0)+"secs" );
    targetDataLine.stop();
    System.exit(1);
  }

  public static void main(String[] args) {
    Drift drift1 = new Drift();
  }

  /*
              if (pref != null) {
              Mixer.Info[] mixes = AudioSystem.getMixerInfo();
              for (int i = 0; i < mixes.length; i++) {
                Mixer.Info mixi = mixes[i];
                String mixup = mixi.getName();
                Log.debug("Mix " + i + " " + mixup);
                if (mixup.equals(pref)) {
                  Mixer preferedMixer = AudioSystem.getMixer(mixi);
                  Log.debug("Found name match for prefered input mixer");
                  if (preferedMixer.isLineSupported(info)) {
   _targetDataLine = (TargetDataLine) preferedMixer.getLine(info);
                    Log.debug("got targetLine");
                    break;
                  }
                  else {
                    Log.debug("Recording format not supported");
                  }
                }
              }

   */
}
