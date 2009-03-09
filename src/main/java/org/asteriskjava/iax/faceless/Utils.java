package org.asteriskjava.iax.faceless;

import org.asteriskjava.iax.protocol.*;
import java.util.*;

public class Utils {

    Faceless _face;
    Utils(Faceless f) {
        _face = f;
    }

    /**
     * Returns the value of a cookie, if set.
     *
     * You <strong>must</strong> have the javascript file for handling cookies
     * (cookies.js) in the webpage for this to work.
     *
     * @param name The name of the cookie
     * @return The cookie value, or null if not set
     */
    protected String getCookie(String name) {
        String ret = null;
        Object[] args = new Object[] {
            name};

        Object cookie = _face.call("getCookie", args);
        if (cookie != null) {
            ret = cookie.toString();
        }

        return ret;
    }

    /**
     * Prints to debug if this class is signed or not.
     *
     * @param cl The class to use
     */
    void printSigners(Class cl) {
        Object[] signers = cl.getSigners();
        if (signers != null) {
            int len = signers.length;
            for (int i = 0; i < len; i++) {
                Object o = signers[i];
                // Log.debug("signer " + i + " = " + o.getClass().getName());

                if (o instanceof java.security.cert.X509Certificate) {
                    java.security.cert.X509Certificate cert =
                        (java.security.cert.X509Certificate) o;
                    Log.debug(cl.getName() + ": signer " + i
                              + " = " + cert.getSubjectX500Principal().getName());
                }
                // printClassStructure(o.getClass());
            }
        }
        else {
            Log.debug(cl.getName() + " is not signed (has no signers)");
        }
    }

    /**
     * Test available for javascript to see if the applet or browser
     * allows cookies.
     *
     * You <strong>must</strong> have the javascript file for handling cookies
     * (cookies.js) in the webpage for this to work.
     *
     * @return True if cookies are supported, false otherwise
     * @see #saveCookie(String name, String value, Calendar expires, String path)
     * @see #getCookie(String name)
     * @see #deleteCookie(String name, String path)
     */
    public boolean canReadWriteCookie() {
        boolean ret = false;
        String name = "corraleta_test";
        String value = "wizard";

        try {
            saveCookie(name, value, null, "");
            Log.debug("Cookie saved");
            //sleep for a short while to allow cookie to be written before testing for it
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException ex) { /*nop*/}
            String ret_value = getCookie(name);
            if (ret_value != null && ret_value.equals(value)) {
                ret = true;
                Log.debug("Got cookie = " + ret_value);
                deleteCookie(name, "");
            }
        }
        catch (Exception exc) {
            Log.warn("canReadWriteCookie() " + exc.getClass().getName() + ": " +
                     exc.getMessage());
        }
        return ret;
    }

    /**
     * Saves a cookie that will expire in a year.
     *
     * You <strong>must</strong> have the javascript file for handling cookies
     * (cookies.js) in the webpage for this to work.
     *
     * @param name The name of the cookie
     * @param value The value of the cookie
     * @see #saveCookie(String name, String value, Calendar expires, String path)
     */
    void saveCookie(String name, String value) {
        /* expires in a year */
        java.util.Calendar expires = java.util.Calendar.getInstance();
        expires.add(java.util.Calendar.YEAR, 1);

        saveCookie(name, value, expires, "/");
    }

    /**
     * Saves a cookie with arbitary expiry time.
     *
     * You <strong>must</strong> have the javascript file for handling cookies
     * (cookies.js) in the webpage for this to work.
     *
     * @param name The name of the cookie
     * @param value The value of the cookie
     * @param expires Calendar When the cookie expires. null gives session cookie.
     * @param path String the path to use for the cookie. Null or empty for path of current page
     * @see #saveCookie(String name, String value)
     */
    void saveCookie(String name, String value, Calendar expires, String path) {
        String expireStr = "";
        if (expires != null) {
            expireStr = expires.getTime().toString();
        }

        final Object[] args = new Object[] {
            name,
            value,
            expireStr,
            path
        };

        _face.call("setCookie", args);
    }

    /**
     * Deletes a cookie.
     *
     * You <strong>must</strong> have the javascript file for handling cookies
     * (cookies.js) in the webpage for this to work.
     *
     * @param name The name of the cookie
     */
    protected void deleteCookie(String name, String path) {
        final Object[] args = new Object[] {
            name,
            path
        };

        _face.call("deleteCookie", args);

    }

    /**
     * Prints to debug the class structure. Used for debug purposes.
     *
     * @param cl The class to use
     */
    private void printClassStructure(Class cl) {
        Class so = cl.getSuperclass();
        if (so != null) {
            Log.debug(cl.getName() + ", super = " + so.getName());
            printClassStructure(so);
        }
    }
}
