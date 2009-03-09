// NAME
//      $RCSfile: Log.java,v $
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
 * Dumb substitute for a logger
 *
 * @author <a href="mailto:thp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision$ $Date$
 */
public class Log {

    private final static String version_id =
            "@(#)$Id$ Copyright Mexuar Technologies Ltd";

    /**
     * Log level ALL
     */
    public static int ALL = 9;
    /**
     * Log level PROL(IX)
     */
    public static int PROL = 6;
    /**
     * Log level VERB(OSE)
     */
    public static int VERB = 5;
    /**
     * Log level DEBUG
     */
    public static int DEBUG = 4;
    /**
     * Log level INFO(RMATION)
     */
    public static int INFO = 3;
    /**
     * Log level WARN(ING)
     */
    public static int WARN = 2;
    /**
     * Log level ERROR (default)
     */
    public static int ERROR = 1;
    /**
     * Log level NONE
     */
    public static int NONE = 0;
    private static int _level = 1;


    /**
     * Constructor for the Log object
     */
    public Log() { }


    /**
     * Sets the log level 
     *
     * @param level The new level value
     */
    public static void setLevel(int level) {
        _level = level;
    }


    /**
     * Returns the log level 
     *
     * @return The level value
     */
    public static int getLevel() {
        return _level;
    }


    /**
     * Logs a warning message.
     * The message will only be logged if the log level is greater or
     * equal then the WARN level.
     *
     * @param string The text to log
     */
    public static void warn(String string) {
        if (_level >= WARN) {
            String message = "WARN: " + System.currentTimeMillis() + " " + Thread.currentThread().getName() + "->" + string;
            System.out.println(message);
        }
    }


    /**
     * Logs a debug message.
     * The message will only be logged if the log level is greater or
     * equal then the DEBUG level.
     *
     * @param string The text to log
     */
    public static void debug(String string) {
        if (_level >= DEBUG) {
            String message = "DEBUG:" + System.currentTimeMillis() + " " + Thread.currentThread().getName() + "->" + string;
            System.out.println(message);
        }
    }


    /**
     * Logs a verbose message.
     * The message will only be logged if the log level is greater or
     * equal then the VERB level.
     *
     * @param string The text to log
     */
    public static void verb(String string) {
        if (_level >= VERB) {
            String message = "VERB: " + System.currentTimeMillis() + " " + Thread.currentThread().getName() + "->" + string;
            System.out.println(message);
        }
    }

    /**
     * Logs a (very ?) verbose message.
     * The message will only be logged if the log level is greater or
     * equal then the PROL level.
     *
     * @param string The text to log
     */
    public static void prol(String string) {
        if (_level >= VERB) {
            String message = "PROL: " + System.currentTimeMillis() + " " + Thread.currentThread().getName() + "->" + string;
            System.out.println(message);
        }
    }


    /**
     * Prints where this message was called from, via a stack trace.
     */
    public static void where() {
        Exception x = new Exception("Called From");
        x.printStackTrace();
    }

}

