
package org.asteriskjava.iax.ui;

import java.applet.Applet;


public class BeanCanApplet extends Applet {


    String host = "192.168.99.254";
    String user = "2001";
    String pass = "1234";


    Integer debug = 0;
    BeanCanFrameManager bcf;


    //Construct the applet
    public BeanCanApplet() {

    }

    //Initialize the applet
    @Override
    public void init() {

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Component initialization
    private void jbInit() throws Exception {
        bcf = new BeanCanFrameManager(true, debug.intValue(), host);
        bcf.validate();


    }

    //Start the applet
    @Override
    public void start() {

        if (bcf != null) {
            bcf.set_host(host);
            bcf.set_username(user);
            bcf.set_password(pass);
            bcf.set_debug(debug.intValue());
            bcf.start();
            bcf.register();


        }
    }


    //Stop the applet
    @Override
    public void stop() {
        if (bcf != null) {
            bcf.stop();
        }
    }

    //Destroy the applet
    @Override
    public void destroy() {

        if (bcf != null) {
            bcf.stop();
        }

    }

    //Get Applet information
    @Override
    public String getAppletInfo() {
        return "Integra CCS";
    }


}
