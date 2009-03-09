// NAME
//      $RCSfile: Corraleta.java,v $
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
import java.net.*;
import java.text.*;
import java.util.*;
import com.mexuar.corraleta.faceless.Faceless;

/**
 * A concrete implementation of the Faceless abstract Applet.
 *
 * @author <a href="mailto:ray@westhawk.co.uk">Ray Tran</a>
 * @version $Revision$ $Date$
 */
public class Corraleta extends Faceless {
    private static final String version_id =
            "@(#)$Id$ Copyright Mexuar Technologies Ltd";

    // The variable _host can be replaced during ant target customizejar
    private final static String _host = "risk.westhawk.co.uk";

    // The variable _version will be replaced during ant target customizejar
    private final static String _version = "0.0";

    // The variable _trialExpireDate will be replaced during ant target
    // customizejar
    private final static String _trialExpireDate = "01-Jan-2006 01:00 GMT";

    // The variable _isTrial will be replaced during ant target
    // customizejar, if it's a trial release
    private final static boolean _isTrial = false;

    public final static DateFormat _dateFormat = 
        new SimpleDateFormat("dd-MMM-yyyy HH:mm zzz", Locale.UK);

    public Corraleta() {
        _user = null;
        _pass = null;
    }

    /**
     * Return the host that we connect to. This is used as part of the process
     * tying a signed jar to a particular customer.
     *
     * @return String
     */
    public final String getHost() {
        return _host;
    }

    public void open() throws SocketException {
        openMyHost(_host);
    }

    /**
     * Return the compiled version that we use. 
     *
     * @return String
     */
    public final String getVersion() {
        return _version;
    }

    /**
     *
     */
    public final String getExpireDate() {
        return _trialExpireDate;
    }
    public final DateFormat getDateFormat() {
        return _dateFormat;
    }

    /**
     * Returns how many days are left in the trial.
     */
    public final int trialDaysLeft() {
        // MAX_VALUE gives 2^31 - 1 days
        int daysLeft = Integer.MAX_VALUE; 
        if (_isTrial == true) {
            try {
                Date expiryDate = _dateFormat.parse(_trialExpireDate);
                Date now = new Date();

                // number of milliseconds 
                long expiryDateMilli = expiryDate.getTime();
                long nowMilli = now.getTime();

                double deltaMilli = (double) (expiryDateMilli - nowMilli);

                double daysLeftD = (deltaMilli / (24 * 60 * 60 * 1000));
                daysLeft = (int) Math.ceil(daysLeftD);
            } catch (java.text.ParseException exc) {
                daysLeft = Integer.MIN_VALUE; 
            }
        } 
        return daysLeft;
    }

    /**
     * Returns if this is a trial version.
     */
    public final boolean isTrial() {
        return _isTrial;
    }

    /**
     * Returns if this version is expired.
     */
    public final boolean isExpired() {
        boolean isExpired = false;
        if (_isTrial == true) {
            int daysLeft = trialDaysLeft();
            if (daysLeft <= 0) {
                isExpired = true;
            }
        } 
        return isExpired;
    }
}
