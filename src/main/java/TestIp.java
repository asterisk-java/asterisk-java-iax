import java.net.*;
// NAME
//      $RCSfile: TestIp.java,v $
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision$
// CREATED
//      $Date$
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//
// GPL
// This program is free software, distributed under the terms of
// the GNU General Public License

/**
 * Test to see what we get back for local IP addresses.
 * @author <a href="mailto:thp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision$ $Date$
 * 
 */


public class TestIp {
  private static final String     version_id =
        "@(#)$Id$ Copyright Westhawk Ltd";
  public TestIp() {
    try {
      InetAddress loc = java.net.InetAddress.getLocalHost();
      System.err.println("ipaddress ="+loc.getHostName());
    }
    catch (UnknownHostException ex) {
    }
  }
  public static void main(String[] args) {
    TestIp testIp1 = new TestIp();
  }

}
