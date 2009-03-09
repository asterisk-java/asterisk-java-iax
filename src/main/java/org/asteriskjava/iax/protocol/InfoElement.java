// NAME
//      $RCSfile: InfoElement.java,v $
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

import java.util.*;

import org.asteriskjava.iax.util.*;

/**
 * Represents the info elements
 *
 * @author <a href="mailto:thp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision$ $Date$
 */
public class InfoElement {
    private final static String version_id =
            "@(#)$Id$ Copyright Mexuar Technologies Ltd";

    /** 0x01 Number/extension being called */
    final static int CALLEDNO = 1;
    /** 0x02 Calling Number String */
    final static int CALLINGNO = 2;
    /** 0x03 Calling number ANI for billing */
    final static int CALLINGANI = 3;
    /** 0x04 Name of caller */
    final static int CALLINGNAME = 4;
    /** 0x05 Context for number*/
    final static int CALLEDCTX = 5;
    /** 0x06 Username (peer or user) for authentication */
    final static int USERNAME = 6;
    /** 0x07 Password for authentication */
    final static int PASSWORD = 7;
    /** 0x08 Actual codec capability 32-bit unsigned integer */
    final static int CAPABILITY = 8;
    /** 0x09 Desired codec format 32-bit unsigned integer */
    final static int FORMAT = 9;
    /** 0x0a Desired language String */
    final static int LANGUAGE = 10;
    /** 0x0b Protocol version 16-bit unsigned integer */
    final static int VERSION = 11;
    /** 0x0c ADSI CPE Capability 16-bit unsigned integer */
    final static int ADSICPE = 12;
    /** 0x0d DNID (Originally dialed DNID - deprecated) */
    final static int DNID = 13;
    /** 0x0e Authentication method(s) 16-bit unsigned integer */
    final static int AUTHMETHODS = 14;
    /** 0x0f Challenge String for MD5/RSA */
    final static int CHALLENGE = 15;
    /** 0x10 MD5 Result String */
    final static int MD5RESULT = 16;
    /** 0x11 RSA Result String */
    final static int RSARESULT = 17;
    /** 0x12 Apparent Address of peer */
    final static int AAA = 18;
    /** 0x13 When to refresh registration interval 16-bit unsigned integer */
    final static int REFRESH = 19;
    /** 0x14 Dialplan Entry Status 16-bit unsigned integer */
    final static int DPE = 20;
    /** 0x15 Call Number of peer 16-bit unsigned integer */
    final static int CALLNO = 21;
    /** 0x16 Cause String */
    final static int CAUSE = 22;
    /** 0x17 IAX Unknown 8-bit unsigned integer */
    final static int IAXUNKNOWN = 23;
    /** 0x18 Messages Waiting 16-bit unsigned integer */
    final static int MSGCOUNT = 24;
    /** 0x19 Request Auto-Answer */
    final static int AUTOANS = 25;
    /** 0x1a Request Music-on-Hold with QUELCH String (optional) */
    final static int MOH = 26;
    /** 0x1b Transfer Identifier 32-bit unsigned integer */
    final static int TRANSINDIC = 27;
    /** 0x1c Referring DNIS String */
    final static int RDNIS = 28;
    /** 0x1d Provisioning info raw */
    final static int PROVISIONING = 29;
    /** 0x1e AES Provisioning info raw */
    final static int AESPROVISIONING = 30;
    /** 0x1f Date/Time u32 */
    final static int DATETIME = 31;
    /** 0x20 Device Type - string */
    final static int DEVICETYPE = 32;
    /** 0x21 Service Identifier - string */
    final static int SERVICEIDENT = 33;
    /** 0x22 Firmware revision - u16 */
    final static int FIRMWAREVER = 34;
    /** 0x23 Firmware block description - u32 */
    final static int FWBLOCKDESC = 35;
    /** 0x24 Firmware block of data - raw */
    final static int FWBLOCKDATA = 36;
    /** 0x25 Provisioning Version (u32) */
    final static int PROVVER = 37;
    /** 0x26 Calling presentation (u8) */
    final static int CALLINGPRES = 38;
    /** 0x27 Calling type of number (u8) */
    final static int CALLINGTON = 39;
    /** 0x28 Calling transit network select (u16) */
    final static int CALLINGTNS = 40;
    /** 0x29 Supported sampling rates (u16) */
    final static int SAMPLINGRATE = 41;
    /** 0x2a Hangup cause (u8) */
    final static int CAUSECODE = 42;
    /** 0x2b Encryption format (u16) */
    final static int ENCRYPTION = 43;
    /** 0x2c 128-bit AES Encryption key (raw) */
    final static int ENCKEY = 44;
    /** 0x2d Codec Negotiation raw */
    final static int CODEC_PREFS = 45;
    /** 0x2e Received jitter (as in RFC1889) u32 */
    final static int RR_JITTER = 46;
    /** 0x2f Received loss (high byte loss pct, low 24 bits loss count, as in rfc1889 u32 */
    final static int RR_LOSS = 47;
    /** 0x30 Received frames (total frames received) u32 */
    final static int RR_PKTS = 48;
    /** 0x31 Max playout delay for received frames (in ms) u16 */
    final static int RR_DELAY = 49;
    /** 0x32 Dropped frames (presumably by jitterbuf) u32 */
    final static int RR_DROPPED = 50;
    /** 0x33 Frames received Out of Order u32 */
    final static int RR_OOO = 51;
    final static int IAXVARS = 52; // todo - fix


    /** 0x01 Number/extension being called */
    String calledNo;
    /** 0x02 Calling Number String */
    String callingNo;
    /** 0x03 Calling number ANI for billing */
    String callingANI;
    /** 0x04 Name of caller */
    String callingName;
    /** 0x05 Context for number*/
    String calledCtx;
    /** 0x06 Username (peer or user) for authentication */
    String username;
    /** 0x07 Password for authentication */
    String password;
    /** 0x08 Actual codec capability 32-bit unsigned integer */
    Integer capability;
    /** 0x09 Desired codec format 32-bit unsigned integer */
    Integer format;
    /** 0x0a Desired language String */
    String language;
    /** 0x0b Protocol version 16-bit unsigned integer */
    Integer version;
    /** 0x0c ADSI CPE Capability 16-bit unsigned integer */
    Integer adsiCpe;
    /** 0x0d DNID (Originally dialed DNID - deprecated) */
    String dnid;
    /** 0x0e Authentication method(s) 16-bit unsigned integer */
    Integer authmethods;
    /** 0x0f Challenge String for MD5/RSA */
    String challenge;
    /** 0x10 MD5 Result String */
    String md5Result;
    /** 0x11 RSA Result String */
    String rsaResult;
    /** 0x12 Apparent Address of peer */
    byte[] aaa;
    /** 0x13 When to refresh registration interval 16-bit unsigned integer */
    Integer refresh;
    /** 0x14 Dialplan Entry Status 16-bit unsigned integer */
    Integer dpe;
    /** 0x15 Call Number of peer 16-bit unsigned integer */
    Integer callNo;
    /** 0x16 Cause String */
    String cause;
    /** 0x17 IAX Unknown 8-bit unsigned integer */
    Integer iaxunknown;
    /** 0x18 Messages Waiting 16-bit unsigned integer */
    Integer msgCount;
    /** 0x19 Request Auto-Answer */
    Boolean autoAns;
    /** 0x1a Request Music-on-Hold String (optional) */
    String moh;
    /** 0x1b Transfer Identifier 32-bit unsigned integer */
    Integer transIndic;
    /** 0x1c Referring DNIS String */
    String rdnis;
    /** 0x1d Provisioning info raw */
    byte[] provisioning;
    /** 0x1e AES Provisioning info raw */
    byte[] aesprovisioning;
    /** 0x1f Date/Time u32 */
    Integer datetime;
    /** 0x20 Device Type - string */
    String devicetype;
    /** 0x21 Service Identifier - string */
    String serviceident;
    /** 0x22 Firmware revision - u16 */
    Integer firmwarever;
    /** 0x23 Firmware block description - u32 */
    Integer fwblockdesc;
    /** 0x24 Firmware block of data - raw */
    byte[] fwblockdata;
    /** 0x25 Provisioning Version (u32) */
    Integer provver;
    /** 0x26 Calling presentation (u8) */
    Integer callingpres;
    /** 0x27 Calling type of number (u8) */
    Integer callington;
    /** 0x28 Calling transit network select (u16) */
    Integer callingtns;
    /** 0x29 Supported sampling rates (u16) */
    Integer samplingrate;
    /** 0x2a Hangup cause (u8) */
    Integer causecode;
    /** 0x2b Encryption format (u16) */
    Integer encryption;
    /** 0x2c 128-bit AES Encryption key (raw) */
    byte[] enckey;
    /** 0x2d Codec Negotiation raw */
    byte[] codec_prefs;
    /** 0x2e Received jitter (as in RFC1889) u32 */
    Integer rr_jitter;
    /** 0x2f Received loss (high byte loss pct, low 24 bits loss count, as in rfc1889 u 32 */
    Integer rr_loss;
    /** 0x30 Received frames (total frames received) u32 */
    Integer rr_pkts;
    /** 0x31 Max playout delay for received frames (in ms) u16 */
    Integer rr_delay;
    /** 0x32 Dropped frames (presumably by jitterbuf) u32 */
    Integer rr_dropped;
    /** 0x33 Frames received Out of Order u32 */
    Integer rr_ooo;
    
    private Hashtable _iaxvars;

    ByteBuffer _buff;
    int _nelems = 0;





    /**
     * Constructor for the inbound InfoElement object
     *
     * @param bin The bytes
     */
    InfoElement(ByteBuffer bin) {
        _buff = bin;
    }


    /**
     * Constructor for the outbound InfoElement object
     */
    InfoElement() { }


    public String [] listIaxVars(){
        String [] ret;
        if (this._iaxvars == null){
            ret = new String[0];
        } else {
            synchronized(_iaxvars){
                ret = new String[_iaxvars.size()];
                Enumeration e = _iaxvars.keys();
                int i= 0;
                while (e.hasMoreElements()){
                    String k = (String) e.nextElement();
                    String v = (String) _iaxvars.get(k);
                    ret[i++] = k+"="+v;
                }
            }
        }
        return ret;
    }
    
    String getIaxVarVal(String name){
        String ret = null;
        if ((name != null) && (_iaxvars != null)){
            synchronized (_iaxvars){
                ret = (String) _iaxvars.get(name);
            }
        }
        return ret; //todo
    }
    
    void putIaxVar(String name, String value){
        if (name != null){
            if (_iaxvars == null) {
                _iaxvars = new Hashtable(5);
                synchronized(_iaxvars){
                    _iaxvars.put(name,value);
                }
            }
        }
    }
    /**
     * Returns the buffer as an array.
     *
     * @return byte[]
     */
    public byte[] getbuff() {
        return _buff.array();
    }


    /**
     * Parses the incoming IAX Control Frame, filling in this object.
     *
     * @param protocolControlFrame ProtocolControlFrame
     * @exception IAX2ProtocolException Description of Exception
     */
    public void parse(ProtocolControlFrame protocolControlFrame)
        throws IAX2ProtocolException {
        while (_buff.hasRemaining()) {
            nextBit();
            _nelems++;
        }
    }


    /**
     * Returns the number of elements in this IE.
     *
     * @return Description of the Returned Value
     */
    public int numElems() {
        return _nelems;
    }


    /**
     * if the caller has a buffer allocated - use it.
     *
     * @param bb ByteBuffer
     */
    public void update(ByteBuffer bb) {
        _buff = bb;
        update();
    }


    /**
     * Set the members, then call this..
     *
     * @todo Mark the end of an IE?
     */
    public void update() {
        writeElem16(VERSION, version);
        writeElemS(CALLEDNO, calledNo);
        writeElemS(CALLINGNO, callingNo);
        writeElemS(CALLINGANI, callingANI);
        writeElemS(CALLINGNAME, callingName);
        writeElemS(CALLEDCTX, calledCtx);
        writeElemS(USERNAME, username);
        writeElemS(PASSWORD, password);
        writeElem32(CAPABILITY, capability);
        writeElem32(FORMAT, format);
        writeElemS(LANGUAGE, language);
        writeElem16(ADSICPE, adsiCpe);
        writeElemS(DNID, dnid);
        writeElem16(AUTHMETHODS, authmethods);
        writeElemS(CHALLENGE, challenge);
        writeElemS(MD5RESULT, md5Result);
        writeElemS(RSARESULT, rsaResult);
        writeElemRaw(AAA, aaa);
        writeElem16(REFRESH, refresh);
        writeElem16(DPE, dpe);
        writeElem16(CALLNO, callNo);
        writeElemS(CAUSE, cause);
        writeElem8(IAXUNKNOWN, iaxunknown);
        writeElem16(MSGCOUNT, msgCount);
        if (autoAns == Boolean.TRUE)
        {
            writeElem0(AUTOANS);
        }
        writeElemS(MOH, moh);
        writeElem32(TRANSINDIC, transIndic);
        writeElemS(RDNIS, rdnis);
        writeElemRaw(PROVISIONING, provisioning);
        writeElemRaw(AESPROVISIONING, aesprovisioning);
        writeElem32(DATETIME, datetime);
        writeElemS(DEVICETYPE, devicetype);
        writeElemS(SERVICEIDENT, serviceident);
        writeElem16(FIRMWAREVER, firmwarever);
        writeElem32(FWBLOCKDESC, fwblockdesc);
        writeElemRaw(FWBLOCKDATA, fwblockdata);
        writeElem32(PROVVER, provver);
        writeElem8(CALLINGPRES, callingpres);
        writeElem8(CALLINGTON, callington);
        writeElem16(CALLINGTNS, callingtns);
        writeElem16(SAMPLINGRATE, samplingrate);
        writeElem8(CAUSECODE, causecode);
        writeElem16(ENCRYPTION, encryption);
        writeElemRaw(ENCKEY, enckey);
        writeElemRaw(CODEC_PREFS, codec_prefs);
        writeElem32(RR_JITTER, rr_jitter);
        writeElem32(RR_LOSS, rr_loss);
        writeElem32(RR_PKTS, rr_pkts);
        writeElem16(RR_DELAY, rr_delay);
        writeElem32(RR_DROPPED, rr_dropped);
        writeElem32(RR_OOO, rr_ooo);
        writeElemsVars();

        // do something here to mark the end.
        // Birgit; is that really necessary?
    }



    /**
     * Reads a String
     *
     * @return Description of the Returned Value
     */
    String readString() {
        int len = _buff.get();
        if (len < 0) {
            len = 128 + (len & 0x7f);
        }
        byte b[] = new byte[len];
        _buff.get(b);
        String ret = null;
        try {
            ret = new String(b, "UTF-8");
        }
        catch (java.io.UnsupportedEncodingException exc) {
            ret = new String(b);
        }
        return ret;
    }

    /**
     * readVar
     */
    private void readVar() {
        String s = readString();
        int i = s.indexOf("=");
        if (i > 0) {
            String name = s.substring(1,i);
            String val = s.substring(i+1);
            putIaxVar(name,val);
        }
    }


    /**
     * Reads a raw block of bytes
     *
     * @return Description of the Returned Value
     */
    byte[] readRaw() {
        int len = _buff.get();
        if (len < 0) {
            len = 128 + (len & 0x7f);
        }
        byte b[] = new byte[len];
        _buff.get(b);
        return b;
    }


    /**
     * Writes a String
     *
     * @param in Description of Parameter
     */
    void writeString(String in) {
        byte[] b = null;/*
        try {
            b = in.getBytes("UTF-8");
        }
        catch (java.io.UnsupportedEncodingException exc) { */
            b = in.getBytes();
      /*  } */
        byte l = (byte) (0xff & b.length);
        _buff.put(l);
        _buff.put(b);
    }


    /**
     * Writes a raw byte
     *
     * @param b Description of Parameter
     */
    void writeRaw(byte[] b) {
        byte l = (byte) (0xff & b.length);
        _buff.put(l);
        _buff.put(b);
    }


    /**
     * Reads a 8 bit integer
     *
     * @return Description of the Returned Value
     * @exception IAX2ProtocolException Description of Exception
     */
    int read8() throws IAX2ProtocolException {
        byte l = _buff.get();
        if (l != 1) {
            throw new IAX2ProtocolException("Byte count in IE wrong expected 1 got " +
                    (int) l);
        }
        int ret = _buff.get();
        if (ret < 0) {
            ret = 128 + (ret & 0x7f);
        }
        return ret;
    }


    /**
     * Reads an empty IE.
     *
     * @return Description of the Returned Value
     * @exception IAX2ProtocolException Description of Exception
     */
    boolean read0() throws IAX2ProtocolException {
        byte l = _buff.get();
        if (l != 0) {
            throw new IAX2ProtocolException("Byte count in IE wrong expected 0 got " +
                    (int) l);
        }
        return true;
    }


    /**
     * Reads a 16 bit integer
     *
     * @return Description of the Returned Value
     * @exception IAX2ProtocolException Description of Exception
     */
    int read16() throws IAX2ProtocolException {
        byte l = _buff.get();
        if (l != 2) {
            throw new IAX2ProtocolException("Byte count in IE wrong expected 2 got " +
                    (int) l);
        }
        int ret = _buff.getChar();
        return ret;
    }


    /**
     * Writes a 16 bit character
     *
     * @param v Description of Parameter
     */
    void write16(char v) {
        _buff.put((byte) 2);
        _buff.putChar(v);
    }


    /**
     * Reads a 32 bit integer
     *
     * @return Description of the Returned Value
     * @exception IAX2ProtocolException Description of Exception
     */
    int read32() throws IAX2ProtocolException {
        byte l = _buff.get();
        if (l != 4) {
            throw new IAX2ProtocolException("Byte count in IE wrong expected 4 got " +
                    (int) l);
        }
        int ret = _buff.getInt();
        return ret;
    }


    /**
     * Writes a 32 bit integer
     *
     * @param v Description of Parameter
     */
    void write32(int v) {
        _buff.put((byte) 4);
        _buff.putInt(v);
    }


    /**
     * Writes a info element with String data
     *
     * @param key Description of Parameter
     * @param s Description of Parameter
     */
    void writeElemS(int key, String s) {
        if (s != null) {
            _buff.put((byte) key);
            writeString(s);
            _nelems++;
        }
    }
    /**
     * writeElemsVars
     */
    void writeElemsVars() {
        String [] l = listIaxVars();
        for (int i = 0; i < l.length; i++){
            writeElemS(this.IAXVARS,l[i]);
        }
    }


    /**
     * Writes a info element with a raw block of bytes
     *
     * @param key Description of Parameter
     * @param b Description of Parameter
     */
    void writeElemRaw(int key, byte[] b) {
        if (b != null) {
            _buff.put((byte) key);
            writeRaw(b);
            _nelems++;
        }
    }


    /**
     * Writes a info element with a 32 bit integer data
     *
     * @param key Description of Parameter
     * @param i Description of Parameter
     */
    void writeElem32(int key, Integer i) {
        if (i != null) {
            _buff.put((byte) key);
            write32(i.intValue());
            _nelems++;
        }
    }


    /**
     * Writes a info element with a 16 bit integer data
     *
     * @param key Description of Parameter
     * @param c Description of Parameter
     */
    void writeElem16(int key, Integer c) {
        if (c != null) {
            _buff.put((byte) key);
            write16((char) (0xffff & c.intValue()));
            _nelems++;
        }
    }


    /**
     * Writes a info element with a 8 bit integer data
     *
     * @param key Description of Parameter
     * @param b Description of Parameter
     */
    void writeElem8(int key, Integer b) {
        if (b != null) {
            _buff.put((byte) key);
            _buff.put((byte) 1);
            _buff.put((byte) (0xff & b.intValue()));
            _nelems++;
        }
    }


    /**
     * Writes an empty info element, with only the key set
     *
     * @param key Description of Parameter
     */
    void writeElem0(int key) {
        _buff.put((byte) key);
        _buff.put((byte) 0);
        _nelems++;
    }


    /**
     * Gets the next IE and stores it in the appropriate variable.
     *
     * @exception IAX2ProtocolException Description of Exception
     */
    private void nextBit() throws IAX2ProtocolException {

        int k = _buff.get();
        Log.verb("IE type = " + k);
        switch (k) {
            case CALLEDNO:
                calledNo = this.readString();
                break;
            case CALLINGNO:
                callingNo = readString();
                break;
            case CALLINGANI:
                callingANI = readString();
                break;
            case CALLINGNAME:
                callingName = readString();
                break;
            case CALLEDCTX:
                calledCtx = readString();
                break;
            case USERNAME:
                username = readString();
                break;
            case PASSWORD:
                password = readString();
                break;
            case CAPABILITY:
                capability = new Integer(this.read32());
                break;
            case FORMAT:
                format = new Integer(read32());
                break;
            case LANGUAGE:
                language = readString();
                break;
            case VERSION:
                version = new Integer(read16());
                break;
            case ADSICPE:
                adsiCpe = new Integer(read16());
                break;
            case DNID:
                break;
            case AUTHMETHODS:
                authmethods = new Integer(read16());
                break;
            case CHALLENGE:
                challenge = readString();
                break;
            case MD5RESULT:
                md5Result = readString();
                break;
            case RSARESULT:
                rsaResult = readString();
                break;
            case AAA:
                aaa = readRaw();
                break;
            case REFRESH:
                refresh = new Integer(read16());
                break;
            case DPE:
                dpe = new Integer(read16());
                break;
            case CALLNO:
                callNo = new Integer(read16());
                break;
            case CAUSE:
                cause = readString();
                break;
            case IAXUNKNOWN:
                iaxunknown = new Integer(_buff.get());
                break;
            case MSGCOUNT:
                msgCount = new Integer(read16());
                break;
            case AUTOANS:
                autoAns = new Boolean(read0());
                break;
            case MOH:
                moh = readString();
                break;
            case TRANSINDIC:
                transIndic = new Integer(read32());
                break;
            case RDNIS:
                rdnis = readString();
                break;
            case PROVISIONING:
                provisioning = readRaw();
                break;
            case AESPROVISIONING:
                aesprovisioning = readRaw();
                break;
            case DATETIME:
                datetime = new Integer(read32());
                break;
            case DEVICETYPE:
                devicetype = readString();
                break;
            case SERVICEIDENT:
                // Are you sure this is a string?
                serviceident = readString();
                break;
            case FIRMWAREVER:
                firmwarever = new Integer(read16());
                break;
            case FWBLOCKDESC:
                fwblockdesc = new Integer(read32());
                break;
            case FWBLOCKDATA:
                fwblockdata = readRaw();
                break;
            case PROVVER:
                provver = new Integer(read32());
                break;
            case CALLINGPRES:
                callingpres = new Integer(read8());
                break;
            case CALLINGTON:
                callington = new Integer(read8());
                break;
            case CALLINGTNS:
                callingtns = new Integer(read16());
                break;
            case SAMPLINGRATE:
                samplingrate = new Integer(read16());
                break;
            case CAUSECODE:
                causecode = new Integer(read8());
                break;
            case ENCRYPTION:
                encryption = new Integer(read16());
                break;
            case ENCKEY:
                enckey = readRaw();
                break;
            case CODEC_PREFS:
                codec_prefs = readRaw();
                break;
            case RR_JITTER:
                rr_jitter = new Integer(read32());
                break;
            case RR_LOSS:
                rr_loss = new Integer(read32());
                break;
            case RR_PKTS:
                rr_pkts = new Integer(read32());
                break;
            case RR_DELAY:
                rr_delay = new Integer(read16());
                break;
            case RR_DROPPED:
                rr_dropped = new Integer(read32());
                break;
            case RR_OOO:
                rr_ooo = new Integer(read32());
                break;
            case IAXVARS:
                readVar();
                break;

            default:
                Log.warn("Unknown InfoElement Type = " + k);
                readRaw();
                throw (new IAX2ProtocolException("Unknown InfoElement Type = " + k));
        }
    }


}

