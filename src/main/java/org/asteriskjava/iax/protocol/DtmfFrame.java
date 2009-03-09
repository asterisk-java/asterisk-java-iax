// NAME
//      $RCSfile: DtmfFrame.java,v $
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

/**
 * Represents an IAX DTMF FRAME
 *
 * @author <a href="mailto:thp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision$ $Date$
 */
class DtmfFrame extends FullFrame {
    private final static String version_id =
            "@(#)$Id$ Copyright Mexuar Technologies Ltd";


    /**
     * The outbound constructor.
     *
     * @param ca The Call object
     * @param c The outgoing DTMF character: 0-9, A-D, *, # 
     */
    DtmfFrame(Call ca, char c) {
        super(ca);
        _retry = false;
        _cbit = false;
        _frametype = FullFrame.DTMF;
        _subclass = 0x7f & c;
        byte buf[] = new byte[0];
        sendMe(buf);
        Log.debug("Sent DTMF " + c);
        this.dump();
    }


    /**
     * ack is called to send any required response. This method is empty
     * (for the moment?).
     */
    void ack() {
        // inbound - ignore it for now....
    }

}

