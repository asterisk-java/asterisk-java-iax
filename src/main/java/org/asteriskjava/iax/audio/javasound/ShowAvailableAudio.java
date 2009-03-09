package org.asteriskjava.iax.audio.javasound;

import java.io.*;
import javax.sound.sampled.*;

// NAME
//      $RCSfile: ShowAvailableAudio.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision$
// CREATED
//      $Date$
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//
// GPL
// This program is free software, distributed under the terms of
// the GNU General Public License

/**
 * Test program to list available audio mixers
 * @author <a href="mailto:thp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision$ $Date$
 *
 */
public class ShowAvailableAudio {
    private static final String     version_id =
        "@(#)$Id$ Copyright Westhawk Ltd";

  public ShowAvailableAudio() {
  }

  public static void main(String[] args) {
    ShowAvailableAudio showAvailableAudio1 = new ShowAvailableAudio();
    showAvailableAudio1.listMix(System.out);
    System.exit(0);
  }

  /**
   * list
   *
   * @param ps PrintStream
   */
  void listMix(PrintStream ps) {
    Mixer.Info[] mixI = AudioSystem.getMixerInfo();
    for (int i = 0; i < mixI.length; i++) {
      Mixer.Info mi = mixI[i];
      ps.println(mi.getClass().getName() + " " + mi.getName() + " " +
                 mi.getVendor());
      Mixer m = AudioSystem.getMixer(mi);
      listLines(ps, m);
    }
  }

  /**
   * listLines
   *
   * @param ps PrintStream
   * @param m Mixer
   */
  void listLines(PrintStream ps, Mixer m) {
    Line.Info[] infos = m.getSourceLineInfo();
// or:
// Line.Info[] infos = AudioSystem.getTargetLineInfo();
    ps.println("\tSource lines");
    for (int i = 0; i < infos.length; i++) {
      if (infos[i] instanceof DataLine.Info) {
        DataLine.Info dataLineInfo = (DataLine.Info) infos[i];
        AudioFormat[] supportedFormats = dataLineInfo.getFormats();
        showFormats(ps, supportedFormats);
      }
    }
    infos = m.getTargetLineInfo();
    ps.println("\tTarget lines");
    for (int i = 0; i < infos.length; i++) {
      if (infos[i] instanceof DataLine.Info) {
        DataLine.Info dataLineInfo = (DataLine.Info) infos[i];
        AudioFormat[] supportedFormats = dataLineInfo.getFormats();
        showFormats(ps, supportedFormats);
      }
    }

  }

  /**
   * showFormats
   *
   * @param ps PrintStream
   * @param fmts AudioFormat[] supportedFormats
   */
  void showFormats(PrintStream ps, AudioFormat[] fmts) {
    for (int i=0;i<fmts.length;i++){
      AudioFormat af = fmts[i];
      ps.println("\t\t"+af.getEncoding()+" "+af.getSampleRate()+" "+af.getSampleSizeInBits());
    }
  }

}
