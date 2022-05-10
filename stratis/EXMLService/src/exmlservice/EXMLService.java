package exmlservice;

import com.agiledelta.efx.EFXException;
import com.agiledelta.efx.EFXFactory;
import com.agiledelta.efx.text.Transcoder;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class EXMLService {

  private static final long EXCEPTION_THROTTLE_THRESHOLD = 30 * 60 * 1000L;
  private static long lastExceptionOccurrence = 0L;
  private final File schema;

  /**
   * Constructor method
   * Uses the masterSchemaPath environment variable which is stored
   * in the web.xml
   * <p>
   * NOTE: This is constructor is only utilized by the gcss.war.  It should not be used by anything for the stratis.war
   * stratis.war always should pass in the file into the other constructor using the ExmlServiceManager class.
   */
  public EXMLService() throws NamingException {
    Context env = (Context) new InitialContext().lookup("java:comp/env");
    String url = (String) env.lookup("masterSchemaPath");

    schema = new File(url);
  }

  public EXMLService(File file) {
    schema = file;
  }

  /**
   * Compress XML message prior to sending to GCSS-MC.
   * STRATIS Outbound Interfaces include I009
   * Returns null.
   */
  public byte[] compress(String xmldoc) throws Exception {
    byte[] output = null;

    try (ByteArrayInputStream is = new ByteArrayInputStream(xmldoc.getBytes(StandardCharsets.UTF_8))) {
      // Encode XML stream to Efficient XML stream
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      EFXFactory factory = EFXFactory.newInstance();
      factory.setSchema(schema);

      SAXParserFactory spf = SAXParserFactory.newInstance();
      spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      spf.setNamespaceAware(true);
      ContentHandler contentHandler = factory.createSAXWriter(baos);
      org.xml.sax.XMLReader xr = spf.newSAXParser().getXMLReader();
      xr.setContentHandler(contentHandler);
      InputSource inputSource = new InputSource(is);
      xr.parse(inputSource);

      output = baos.toByteArray();
      // free up resources
    }
    catch (FileNotFoundException f) {
      log.error("ERROR: RICE-Master-Schema.xsd path is incorrect; check masterSchemaPath. Transactions not sending.", f);
    }
    catch (EFXException e) {
      if (shouldLogException()) {
        log.error("Invalid Efficient XML license... ensure the [efx.jar] is signed with the correct product key.", e);
      }
    }
    catch (Exception e) {
      log.error("Exception thrown", e);
      throw e;
    }

    return output;
  }

  /**
   * Decompress XML message received from GCSS-MC.
   * STRATIS Inbound Interfaces include I033, I112, I111, I136, I137
   * Returns null.
   *
   * @return String
   */
  public String decompress(byte[] xmlbin) {
    String output = null;

    try {
      log.debug("***decompress method ****");
      log.debug("originalDataLength = {}", xmlbin.length);

      // Decode encoded stream to XML stream
      ByteArrayInputStream bais = new ByteArrayInputStream(xmlbin);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      EFXFactory factory = EFXFactory.newInstance();
      factory.setSchema(schema);

      Transcoder trans = factory.newTranscoder();
      trans.decode(bais, baos);

      byte[] result = baos.toByteArray();

      baos.close();
      bais.close();

      int resultLength = result.length;
      log.debug("unCompressedDataLength = {}", resultLength);

      // Decode the bytes into a String
      output = new String(result, 0, resultLength, StandardCharsets.UTF_8);
    }
    catch (FileNotFoundException f) {
      log.error("ERROR: RICE-Master-Schema.xsd path is incorrect; check masterSchemaPath. Transactions not sending.", f);
    }
    catch (EFXException e) {
      if (shouldLogException()) {
        log.error("Invalid Efficient XML license... ensure the [efx.jar] is signed with the correct product key.", e);
      }
    }
    catch (Exception e) {
      log.error("Exception thrown", e);
    }

    return output;
  }

  /**
   * If the Efficient XML jar is not signed properly it spams the logs with redundant exceptions.
   * This method throttles those exceptions to a constant threshold.
   *
   * @return boolean of whether the exception should be logged.
   */
  private static boolean shouldLogException() {
    long now = System.currentTimeMillis();
    if (now - lastExceptionOccurrence > EXCEPTION_THROTTLE_THRESHOLD) {
      lastExceptionOccurrence = now;
      return true;
    }
    else {
      return false;
    }
  }
}
