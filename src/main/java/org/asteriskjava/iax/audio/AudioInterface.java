// NAME
//      $RCSfile: AudioInterface.java,v $
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
package org.asteriskjava.iax.audio;

import java.io.*;

public interface AudioInterface {
  /**
 * Return the minimum sample size for use in creating buffers etc.
 * @return int
 */
public int getSampSz() ;

/**
 * Read from the Microphone, using the buffer provided,
 * but _only_ filling getSampSz() bytes.
 * returns the timestamp of the sample from the audio clock.
 * @param buff byte[]
 * @return long
 */
public long readWithTime(byte[] buff) throws IOException ;
public long readDirect(byte[] buff) throws IOException ;

/**
 * stop the reccorder - but don't throw it away.
 */
public void stopRec() ;

/**
 * start the recorder
  * returning the time...
 */

public long startRec() ;

/**
 * The Audio properties have changed so attempt to
 * re connect to a new device
 */
public void changedProps() ;

/**
 * Start the player
 */
public void startPlay() ;

/**
 * Stop the player
 */
public void stopPlay();

/**
 * play the sample given (getSampSz() bytes)
 * assuming that it's timestamp is long
 * @param buff byte[]
 * @param timestamp long
 */

public void write(byte[] buff, long timestamp) throws IOException;

public void writeDirect(byte[] buff) throws IOException;

  /**
   * startRinging
   */
  public void startRinging();

  /**
   * stopRinging
   */
  public void stopRinging();
  public int getFormatBit();
  public void setAudioSender(org.asteriskjava.iax.protocol.AudioSender as);

public void playAudioStream(java.io.InputStream in) throws IOException;
public void sampleRecord(SampleListener list) throws IOException;
public Integer supportedCodecs();
public String codecPrefString();  
public void cleanUp();
public AudioInterface getByFormat(Integer format);
}
