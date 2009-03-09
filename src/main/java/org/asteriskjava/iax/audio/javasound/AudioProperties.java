// NAME
//      $RCSfile: AudioProperties.java,v $
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
import java.util.*;
import javax.sound.sampled.*;
import javax.sound.sampled.Mixer.*;

import org.asteriskjava.iax.protocol.*;

/**
 * Description of the Class
 *
 * @author <a href="mailto:thp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision$ $Date$
 */
public class AudioProperties
    extends Properties {

    private final static String version_id =
        "@(#)$Id$ Copyright Mexuar Technologies Ltd";

    public final static String OUTPUTDEVICE = "outputDevice";
    public final static String INPUTDEVICE = "inputDevice";
    private final static String BIGBUFF = "bigbuff";
    private final static String AUDIO_IN = "audioIn";
    private final static String AUDIO_OUT = "audioOut";
    private final static String MONITOR = "monitor";
    private final static String AEC = "aec";
    private final static String STEREOREC = "stereoRec";

    private static Properties __audioProps;

    /**
     * Description of the Method
     *
     * @param fis Description of Parameter
     */
    public static void loadFromFile(String fis) {
        try {
            __audioProps = new Properties();
            FileInputStream inf = new FileInputStream(fis);
            __audioProps.load(inf);
        }
        catch (Exception x) {
            __audioProps = null;
            Log.warn(x.getMessage());
        }
    }

    /**
     * Gets the outputDeviceName attribute of the AudioProperties
     * class
     *
     * @return The outputDeviceName value
     */
    public static String getOutputDeviceName() {
        String ret = get(OUTPUTDEVICE);
        return ret;
    }

    /**
     * Gets the inputDeviceName attribute of the AudioProperties class
     *
     * @return The inputDeviceName value
     */
    public static String getInputDeviceName() {
        String ret = get(INPUTDEVICE);
        return ret;
    }

    /**
     * Description of the Method
     *
     * @param name Description of Parameter
     * @param val Description of Parameter
     */
    private static void set(String name, String val) {
        if (__audioProps == null) {
            __audioProps = new Properties();
        }
        __audioProps.put(name, val);
        Log.debug("set " + name + " to " + val);
    }

    /**
     * Description of the Method
     *
     * @param name Description of Parameter
     * @return Description of the Returned Value
     */
    private static String get(String name) {
        String ret = null;
        if (__audioProps != null) {
            ret = (String) __audioProps.get(name);
        }
        return ret;
    }

    /**
     * Sets the inputDeviceName attribute of the AudioProperties class
     *
     * @param val The new inputDeviceName value
     */
    public static void setInputDeviceName(String val) {
        set(INPUTDEVICE, val);
    }

    /**
     * Sets the outputDeviceName attribute of the AudioProperties
     * class
     *
     * @param val The new outputDeviceName value
     */
    public static void setOutputDeviceName(String val) {
        set(OUTPUTDEVICE, val);
    }

    /**
     * Gets the mixIn attribute of the AudioProperties class
     *
     * @return The mixIn value
     */
    public static String[] getMixIn() {
        String ret[] = null;
        Vector vres = new Vector();
        Log.debug("in getMixIn() ");
        String preferred = getInputDeviceName();
        boolean foundPreferred = false;
        Info[] mixes = AudioSystem.getMixerInfo();
        for (int i = 0; i < mixes.length; i++) {
            Mixer.Info mixi = mixes[i];
            String mixup = mixi.getName().trim();
            Log.debug("mixer input " + mixup);
            Mixer mx = AudioSystem.getMixer(mixi);
            Line.Info[] infos = mx.getTargetLineInfo();
            if ( (infos != null) && (infos.length > 0) &&
                (infos[0] instanceof DataLine.Info)) {
                vres.add(mixup);
                if (mixup.equals(preferred)) {
                    foundPreferred = true;
                }
            }
        }

        // if there is a preferred one, put it on top
        ret = new String[vres.size()];
        if (foundPreferred == true) {
            ret[0] = preferred;
            Log.debug("adding input " + preferred + " (preferred)");
            int j=1;
            for (int i = 0; i < ret.length; i++) {
                String mixup = (String) vres.elementAt(i);
                if (mixup.equals(preferred) == false) {
                    ret[j] = (String) vres.elementAt(i);
                    Log.debug("adding input " + mixup);
                    j++;
                }
            }
        } else {
            for (int i = 0; i < ret.length; i++) {
                String mixup = (String) vres.elementAt(i);
                ret[i] = mixup;
                Log.debug("adding input " + mixup);
            }
        }
        Log.debug("ret.length = " + ret.length);

        return ret;
    }

    /**
     * Gets the mixOut attribute of the AudioProperties class
     *
     * @return The mixOut value
     */
    public static String[] getMixOut() {
        String ret[] = null;
        Vector vres = new Vector();
        Log.debug("in getMixIn() ");
        String preferred = getOutputDeviceName();
        boolean foundPreferred = false;
        Info[] mixes = AudioSystem.getMixerInfo();
        for (int i = 0; i < mixes.length; i++) {
            Mixer.Info mixi = mixes[i];
            String mixup = mixi.getName().trim();
            Mixer mx = AudioSystem.getMixer(mixi);
            Line.Info[] infos = mx.getSourceLineInfo();
            if ( (infos != null) && (infos.length > 0) &&
                (infos[0] instanceof DataLine.Info)) {
                vres.add(mixup);
                if (mixup.equals(preferred)) {
                    foundPreferred = true;
                }
            }
        }

        // if there is a preferred one, put it on top
        ret = new String[vres.size()];
        if (foundPreferred == true) {
            ret[0] = preferred;
            Log.debug("adding output " + preferred + " (preferred)");
            int j=1;
            for (int i = 0; i < ret.length; i++) {
                String mixup = (String) vres.elementAt(i);
                if (mixup.equals(preferred) == false) {
                    ret[j] = (String) vres.elementAt(i);
                    j++;
                    Log.debug("adding output " + mixup);
                }
            }
        } else {
            for (int i = 0; i < ret.length; i++) {
                String mixup = (String) vres.elementAt(i);
                ret[i] = mixup;
                Log.debug("adding output " + mixup);
            }
        }
        Log.debug("ret.length = " + ret.length);

        return ret;
    }

    /**
     * getBigBuff
     *
     * @return Boolean
     */
    public static boolean getBigBuff() {
        boolean ret = false;
        String bb = get(BIGBUFF);
        if (bb != null) {
            ret = ("TRUE".compareToIgnoreCase(bb) == 0);
        }
        else {
            // - we know that jdk1.4 likes bigger buffers
            String jdkversion = System.getProperty("java.version");
            if (jdkversion != null) {
                if (jdkversion.startsWith("1.5")) {
                    ret = false;
                }
                else {
                    ret = true;
                    Log.warn("Pre jdk1.5 in use - Using bigger buffers");
                }
            }

            if (ret == false)
            {
                // - so does Linux 
                String osname = System.getProperty("os.name");
                if (osname != null) {
                    osname = osname.toLowerCase();
                    if (osname.startsWith("linux")) {
                        ret = true;
                        Log.warn("Linux in use - Using bigger buffers");
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Sets the bigBuff attribute of the AudioProperties class
     *
     * @param val The new bigBuff value
     */
    public static void setBigBuff(boolean val) {
        set(BIGBUFF, "" + val);
    }


    /**
     * Returns if we have been able to open an incoming audio device, i.e.
     * a microphone.
     *
     * @return True if we have opened incoming audio device, false if
     * otherwise
     */
    public static boolean isAudioInUsable() {
        boolean ret = false;
        String bb = get(AUDIO_IN);
        if (bb != null) {
            ret = "TRUE".equalsIgnoreCase(bb);
        }
        return ret;
    }

    /**
     * Returns if we have been able to open an outgoing audio device, i.e.
     * a headset or speakers.
     *
     * @return True if we have opened outgoing audio device, false if
     * otherwise
     */
    public static boolean isAudioOutUsable() {
        boolean ret = false;
        String bb = get(AUDIO_OUT);
        if (bb != null) {
            ret = "TRUE".equalsIgnoreCase(bb);
        }
        return ret;
    }

    /**
     * Sets if we have been able to open an incoming audio device, i.e.
     * a microphone.
     *
     * @param b True if we have opened incoming audio device, false if
     * otherwise
     */
    static void setAudioInUsable(boolean b) {
        set(AUDIO_IN, Boolean.toString(b));
    }

    /**
     * Sets if we have been able to open an outgoing audio device, i.e.
     * a headset or speakers.
     *
     * @param b True if we have opened outgoing audio device, false if
     * otherwise
     */
    static void setAudioOutUsable(boolean b) {
        set(AUDIO_OUT, Boolean.toString(b));
    }

    /**
     * Sets local audio recording on or off.
     * @param b boolean
     */
    static void setMonitor(boolean b) {
        set(MONITOR, Boolean.toString(b));
    }

    public static boolean isMonitor() {
        boolean ret = false;
        String bb = get(MONITOR);
        if (bb != null) {
            ret = ("TRUE".compareToIgnoreCase(bb) == 0);
        }
        return ret;
    }
    /**
     * Sets local acoustic echo canceller on or off.
     * @param b boolean
     */
    static void setAEC(boolean b) {
        set(AEC, Boolean.toString(b));
    }

    public static boolean isAEC() {
        boolean ret = false;
        String bb = get(AEC);
        if (bb != null) {
            ret = ("TRUE".compareToIgnoreCase(bb) == 0);
        }
        return ret;
    }
    /**
     * Sets local recording in stereo mode on or off
     * @param b boolean
     */
    static void setStereoRec(boolean b) {
        set(STEREOREC, Boolean.toString(b));
    }

    public static boolean isStereoRec() {
        boolean ret = false;
        String bb = get(STEREOREC);
        if (bb != null) {
            ret = ("TRUE".compareToIgnoreCase(bb) == 0);
        }
        return ret;
    }

    /**
     * Returns if the datalines should be closed when Corraleta quits.
     * Some platforms crash when this happens.
     *
     * @return True if the lines should be closed, false otherwise.
     */
    public static boolean closeDataLine() {
        boolean ret = true;
        try {
            String osname = System.getProperty("os.name");
            if (osname != null) {
                osname = osname.toLowerCase();
                Log.debug("AudioProperties.closeDateLine(): osname=" + osname);
                if (osname.startsWith("mac")) {
                    ret = false;
                }
            }
        }
        catch (Exception exc) {
            Log.warn("AudioProperties.closeDateLine(): " 
                + exc.getClass().getName() + " " + exc.getMessage());
        }
        return ret;
    }

}
