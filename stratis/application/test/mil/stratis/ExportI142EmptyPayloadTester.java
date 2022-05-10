package mil.stratis;

import exmlservice.EXMLService;
import mil.usmc.mls2.r12.i009.I009ShipmentReceiptsInbound;
import mil.usmc.mls2.r12.i009.I009ShipmentReceiptsInboundProcessCompressedRequest;
import mil.usmc.mls2.r12.i009.I009ShipmentReceiptsInbound_Service;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.ws.BindingProvider;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * class used to test the I142 payload.
 */
public class ExportI142EmptyPayloadTester {

  private String remoteUrl;
  private String truststorePath;
  private String truststorePwd;
  private String keystorePath;
  private String keystorePwd;
  private String keyAlias;
  private String masterSchemaPath;

  private static class JNDICustomUtils {

    private static final String PREFIX = "java:comp/env/";
    private static JNDICustomUtils _instance = null;
    private static InitialContext _ctx = null;

    /**
     * Private Constructor.
     */
    private JNDICustomUtils() throws NamingException {
      _ctx = this.getContext();
    }

    /**
     * Returns an instance of JNDIUtils.class.
     *
     * @return instance of JNDIUtils class
     */
    public static JNDICustomUtils getInstance() throws NamingException {
      if (_instance == null) {
        _instance = new JNDICustomUtils();
      }
      return _instance;
    }

    /**
     * Returns an instance of InitialContext.
     *
     * @return InitialContext
     */
    private InitialContext getContext() {
      InitialContext ctx = null;
      try {
        ctx = new InitialContext();
      }
      catch (NamingException e) {
        System.out.println("Error creating JNDI connection");
        e.printStackTrace();
      }
      return ctx;
    }

    /**
     * Used to retrieve the value of a property in the context realm.
     *
     * @param prop - name of the property requested
     * @return String value
     */
    public String getProperty(final String prop) {
      String envValue = null;
      try {
        envValue = (String) _ctx.lookup(PREFIX + prop);
      }
      catch (NamingException e) {
        System.out.println("Error looking up JNDI property: " + prop + " " + e.getLocalizedMessage());
        e.printStackTrace();
      }
      return envValue;
    }

    //        public void setProperty(final String prop, final String value) {
    //            try {
    //                //_ctx.lookup(PREFIX + prop);
    //                _ctx.bind(PREFIX + prop, value);
    //            } catch (NamingException e) {
    //                System.out.println("Error setting JNDI property: " + prop + " " + e.getLocalizedMessage() );
    //            }
    //        }
  }

  public ExportI142EmptyPayloadTester(String configFilePath) throws Exception {
    TreeMap<String, String> map = getProperties(configFilePath);
    System.out.println(map);

    remoteUrl = map.get("url");
    truststorePath = map.get("truststorePath");
    truststorePwd = map.get("truststorePwd");
    keystorePath = map.get("keystorePath");
    keystorePwd = map.get("keystorePwd");
    keyAlias = map.get("keyAlias");
    masterSchemaPath = map.get("masterSchemaPath");

    // Use Apache Tomcat's Directory
    System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
    System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
    // Standard hook
    InitialContext initialContext = new InitialContext();
    // Create binding
    initialContext.createSubcontext("java:");
    initialContext.createSubcontext("java:comp");
    initialContext.createSubcontext("java:comp/env");

    initialContext.bind("java:comp/env/ws.truststore.path", truststorePath);
    initialContext.bind("java:comp/env/ws.truststore.password", truststorePwd);
    initialContext.bind("java:comp/env/ws.keystore.path", keystorePath);
    initialContext.bind("java:comp/env/ws.keystore.password", keystorePwd);

    initialContext.bind("java:comp/env/ws.keystore.alias", keyAlias);
    initialContext.bind("java:comp/env/ws.disabled", "false");

    initialContext.bind("java:comp/env/masterSchemaPath", masterSchemaPath);
  }

  public static TreeMap<String, String> getProperties(String infile) throws IOException {
    final int lhs = 0;
    final int rhs = 1;

    TreeMap<String, String> map = new TreeMap<String, String>();
    try (BufferedReader bfr = new BufferedReader(new FileReader(new File(infile)))) {
      String line;
      while ((line = bfr.readLine()) != null) {
        if (!line.startsWith("#") && !line.isEmpty()) {
          String[] pair = line.trim().split("=");
          map.put(pair[lhs].trim(), pair[rhs].trim());
        }
      }
    }

    return (map);
  }

  public static void main(String[] args) {
    try {
      if (args.length == 0) {
        System.out.println("Usage: expects one command line argument, which is a path to a config file.\n" +
            "  The config file must contain five entries such as this:\n" +
            "\n" +
            "url=https://remotegcssurl\n" +
            "truststorePath=path/to/truststore\n" +
            "truststorePwd=pwd\n" +  //NOSONAR - this was being flagged as a hard coded credential
            "keystorePath=path/to/keystore\n" +
            "keystorePwd=pwd"); //NOSONAR - this was being flagged as a hard coded credential

        System.exit(1);
      }

      ExportI142EmptyPayloadTester tester = new ExportI142EmptyPayloadTester(args[0]);

      tester.doWork();
    }
    catch (Throwable t) {
      System.out.println("Exception!");
      t.printStackTrace();
    }
  }

  public void doWork() throws Exception {

    final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><shipmentReceiptsInCollection xmlns=\"http://www.usmc.mil/schemas/1/if/stratis\"></shipmentReceiptsInCollection>";

    byte[] output;

    EXMLService eXMLFactory = new EXMLService();
    output = eXMLFactory.compress(xml.toString());

    I009ShipmentReceiptsInbound_Service i009 = new I009ShipmentReceiptsInbound_Service();
    I009ShipmentReceiptsInbound svc = i009.getI009ShipmentReceiptsInboundPort();
    I009ShipmentReceiptsInboundProcessCompressedRequest payload = new I009ShipmentReceiptsInboundProcessCompressedRequest();
    payload.setInput(output);

    BindingProvider bp = (BindingProvider) svc;
    Map<String, Object> reqContext = bp.getRequestContext();
    System.out.println("Setting Spool the ENDPOINT_ADDRESS_PROPERTY: " + remoteUrl);
    reqContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, remoteUrl);
    System.setProperty("javax.net.ssl.trustStore", truststorePath);
    System.setProperty("javax.net.ssl.trustStorePassword", truststorePwd);
    System.setProperty("javax.net.ssl.trustStoreType", "JKS");
    System.setProperty("javax.net.ssl.keyStore", keystorePath);
    System.setProperty("javax.net.ssl.keyStorePassword", keystorePwd);
    System.setProperty("javax.net.ssl.keyStoreType", "JKS");

    System.out.println("Sending spool request");
    svc.initiateCompressed(payload);
    System.out.println("Sending complete");
  }
}
