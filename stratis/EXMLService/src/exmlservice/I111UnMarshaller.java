package exmlservice;

import exmlservice.I111.StratisDueInsOutCollection;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

@Slf4j
public class I111UnMarshaller {

  private I111UnMarshaller() {
  }

  public static StratisDueInsOutCollection unmarshallDecompressedString(String xmlStream) {
    return unmarshallDecompressedBytes(xmlStream.getBytes());
  }

  private static StratisDueInsOutCollection unmarshallDecompressedBytes(byte[] xmlStream) {
    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(xmlStream);
         BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream)) {
      return unmarshall(bufferedInputStream);
    }
    catch (IOException ioe) {
      throw new RuntimeException("Exception creating InputStreams from decompressed byte array.", ioe);
    }
  }

  public static StratisDueInsOutCollection unmarshall(BufferedInputStream xmlStream) {
    StratisDueInsOutCollection i111Collection = null;
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance("exmlservice.I111");
      XMLInputFactory xif = XMLInputFactory.newFactory();
      xif.setProperty(XMLInputFactory.SUPPORT_DTD, false); // This disables DTDs entirely for that factory
      xif.setProperty("javax.xml.stream.isSupportingExternalEntities", false); // disable external entities
      XMLStreamReader xsr = xif.createXMLStreamReader(xmlStream);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

      i111Collection = (StratisDueInsOutCollection) unmarshaller.unmarshal(xsr);
    }
    catch (Exception e) {
      log.error("Error unmarshalling I111", e);
    }
    return i111Collection;
  }
}
