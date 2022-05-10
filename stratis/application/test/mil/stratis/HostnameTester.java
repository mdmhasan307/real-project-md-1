package mil.stratis;

import exmlservice.EXMLService;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.net.InetAddress;

/**
 * class used find systems hostname in support of eXML.
 */
public class HostnameTester {

  public HostnameTester() throws Exception {

    // Use Apache Tomcat's Directory
    System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
    System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");

    // Standard hook
    InitialContext initialContext = new InitialContext();
    // Create binding
    initialContext.createSubcontext("java:");
    initialContext.createSubcontext("java:comp");
    initialContext.createSubcontext("java:comp/env");

    initialContext.bind("java:comp/env/masterSchemaPath", "/opt/app/stratis/tomcat/current/webapps/stratis/WEB-INF/RICE_INTF/RICE-Master-Schema.xsd");
  }

  public static void main(String[] args) {
    try {

      HostnameTester tester = new HostnameTester();

      tester.doWork();
    }
    catch (Throwable t) {
      System.out.println("Exception!");
      t.printStackTrace();
    }
  }

  public void doWork() throws Exception {

    InetAddress var1 = InetAddress.getLocalHost();

    if (var1 == null) {
      System.out.println("InetAddress.getLocalHost() is NULL!");
    }
    else {
      System.out.println("InetAddress.getLocalHost().getHostName(): " + var1.getHostName());
    }

    final String thing = "thing";

    EXMLService eXMLFactory = new EXMLService();
    byte[] output = eXMLFactory.compress(thing);

    System.out.println("tried to compress a thing");
  }
}
