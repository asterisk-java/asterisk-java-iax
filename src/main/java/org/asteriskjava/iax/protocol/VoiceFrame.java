// NAME
//      $RCSfile: VoiceFrame.java,v $
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

import java.io.*;

/**
 * VoiceFrame - The frame carries voice data.
 *
 * @author <a href="mailto:thp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision$ $Date$
 */
public class VoiceFrame extends FullFrame {

    private final static String version_id =
            "@(#)$Id$ Copyright Mexuar Technologies Ltd";

    /**
     * G.723.1 index
     */
    public final static int G723_NO = 0;
    /**
     * Constant: G.723.1 - 4, 20, and 24 byte frames of 240 samples
     */
    public final static int G723_BIT = 1 << G723_NO;

    /**
     * GSM Full Rate index
     */
    public final static int GSM_NO = 1;
    /**
     * Constant: GSM Full Rate - 33 byte chunks of 160 samples or 65 byte chunks of 320 samples
     */
    public final static int GSM_BIT = 1 << GSM_NO;

    /**
     * G.711 mu-law index
     */
    public final static int ULAW_NO = 2;
    /**
     * Constant: G.711 mu-law - 1 byte per sample
     */
    public final static int ULAW_BIT = 1 << ULAW_NO;

    /**
     * G.711 a-law index
     */
    public final static int ALAW_NO = 3;
    /**
     * Constant: G.711 a-law - 1 byte per sample
     */
    public final static int ALAW_BIT = 1 << ALAW_NO;

    /**
     * G.726 index
     */
    public final static int G726_NO = 4;
    /**
     * Constant: G.726
     */
    public final static int G726_BIT = 1 << G726_NO;

    /**
     * IMA ADPCM index
     */
    public final static int ADPCM_NO = 5;
    /**
     * Constant: IMA ADPCM - 1 byte per 2 samples
     */
    public final static int ADPCM_BIT = 1 << ADPCM_NO;

    /**
     * 16-bit linear little-endian index
     */
    public final static int LIN16_NO = 6;
    /**
     * Constant: 16-bit linear little-endian - 2 bytes per sample
     */
    public final static int LIN16_BIT = 1 << LIN16_NO;

    /**
     * LPC10 index
     */
    public final static int LPC10_NO = 7;
    /**
     * Constant: LPC10 - Variable size frame of 172 samples
     */
    public final static int LPC10_BIT = 1 << LPC10_NO;

    /**
     * G.729 index
     */
    public final static int G729_NO = 8;
    /**
     * Constant: G.729 - 20 bytes chunks of 172 samples
     */
    public final static int G729_BIT = 1 << G729_NO;

    /**
     * Speex index
     */
    public final static int SPEEX_NO = 9;
    /**
     * Constant: Speex - Variable
     */
    public final static int SPEEX_BIT = 1 << SPEEX_NO;

    /**
     * ILBC index
     */
    public final static int ILBC_NO = 10;
    /**
     * Constant: ILBC - 50 bytes per 240 samples
     */
    public final static int ILBC_BIT = 1 << ILBC_NO;

    /**
     * AMR narrowband index - not standardized.
     */
    public final static int AMRN_NO = 14;

    public final static int AMRN_BIT = 1 << AMRN_NO;


    /**
     * JPEG index
     */
    public final static int JPEG_NO = 16;
    /**
     * Constant: JPEG
     */
    public final static int JPEG_BIT = 1 << JPEG_NO;

    /**
     * PNG index
     */
    public final static int PNG_NO = 17;
    /**
     * Constant: PNG
     */
    public final static int PNG_BIT = 1 << PNG_NO;

    /**
     * H261 index
     */
    public final static int H261_NO = 18;
    /**
     * Constant: H261
     */
    public final static int H261_BIT = 1 << H261_NO;

    /**
     * H263 index
     */
    public final static int H263_NO = 19;
    /**
     * Constant: H263
     */
    public final static int H263_BIT = 1 << H263_NO;

    /**
     * H263P index
     */
    public final static int H263P_NO = 20;
    /**
     * Constant: H263P
     */
    public final static int H263P_BIT = 1 << H263P_NO;


    /**
     * The outbound constructor.
     *
     * @param p0 The Call object
     */
    public VoiceFrame(Call p0) {
        super(p0);
        _retry = false;
        _frametype = FullFrame.VOICE;
    }

    /**
     * The inbound constructor.
     *
     * @param p0 The Call object
     * @param p1 The incoming message bytes
     */
    public VoiceFrame(Call p0, byte[] p1) {
        super(p0, p1);
    }


    /**
     * ack is called to send any required response.
     */
    void ack() {
        log("got");
        switch (_subclass) {
            case GSM_BIT:
                Log.warn("Got unwanted Audio format " + _subclass);
                break;
            case ULAW_BIT:
            case ALAW_BIT:
            case LIN16_BIT:
            case AMRN_BIT:
                sendAck();
                break;
            default:
                Log.warn("Got unwanted Audio format " + _subclass);
                break;
        }
    }


    /**
     * Logs this frame.
     *
     * @param inout Additional text to log
     */
    protected void log(String inout) {
        super.log(inout + " voice frame");
    }


    /**
     * arrived is called when a packet arrives.
     *
     * @throws IAX2ProtocolException
     */
    void arrived() throws IAX2ProtocolException {
        int fsz = _call.getFrameSz();
        byte[] bs = new byte[fsz];
        _data.get(bs);
        try {
            _call.fullVoiceFrameRcvd(this.getTimestampVal());
            _call.audioWrite(bs, this.getTimestampVal() & 0xffff );
        }
        catch (IOException ex) {
            Log.warn(ex.getMessage());
        }
    }

}

