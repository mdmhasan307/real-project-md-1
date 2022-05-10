package exmlservice;

import exmlservice.I112.SalesorderOutCollection;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Slf4j
public class I112UnMarshaller {

  private I112UnMarshaller() {
  }

  public static SalesorderOutCollection unmarshallDecompressedString(String xmlStream) {
    return unmarshallDecompressedBytes(xmlStream.getBytes());
  }

  private static SalesorderOutCollection unmarshallDecompressedBytes(byte[] xmlStream) {
    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(xmlStream);
         BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream)) {
      return unmarshall(bufferedInputStream);
    }
    catch (IOException ioe) {
      throw new IllegalStateException("Exception creating InputStreams from decompressed byte array.", ioe);
    }
  }

  public static SalesorderOutCollection unmarshall(BufferedInputStream xmlStream) {
    SalesorderOutCollection i112Collection = null;
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance("exmlservice.I112");
      XMLInputFactory xif = XMLInputFactory.newFactory();
      xif.setProperty(XMLInputFactory.SUPPORT_DTD, false); // This disables DTDs entirely for that factory
      xif.setProperty("javax.xml.stream.isSupportingExternalEntities", false); // disable external entities
      XMLStreamReader xsr = xif.createXMLStreamReader(xmlStream);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

      i112Collection = (SalesorderOutCollection) unmarshaller.unmarshal(xsr);
    }
    catch (Exception e) {
      log.error("Error unmarshalling I112", e);
    }
    return i112Collection;
  }
}
