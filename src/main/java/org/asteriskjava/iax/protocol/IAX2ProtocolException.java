// NAME
//      $RCSfile: IAX2ProtocolException.java,v $
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
package org.asteriskjava.iax.protocol;

import java.io.IOException;

/**
 * Generic protocol exception for IAX2 Protocol
 * @author <a href="mailto:thp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision$ $Date$
 *
 */
public class IAX2ProtocolException extends IOException {
    private static final String version_id =
            "@(#)$Id$ Copyright Mexuar Technologies Ltd";


  public IAX2ProtocolException() {
  }

  public IAX2ProtocolException(String p0) {
    super(p0);
  }
}
