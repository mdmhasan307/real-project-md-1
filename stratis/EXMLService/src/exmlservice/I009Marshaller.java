package exmlservice;

import exmlservice.I009.ObjectFactory;
import exmlservice.I009.ShipmentReceiptsInCollection;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;

@Slf4j
public class I009Marshaller {

  private I009Marshaller() {
  }

  public static final String ROOT_TAG = "shipmentReceiptsInCollection";

  public static final String NAMESPACE = "http://www.usmc.mil/schemas/1/if/stratis";

  public static String marshal(ShipmentReceiptsInCollection.MRec i009Record) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance("exmlservice.I009");
      Marshaller marshaller = jaxbContext.createMarshaller();
      ByteArrayOutputStream output = new ByteArrayOutputStream();

      ObjectFactory factory = new ObjectFactory();
      //Create the Master collection
      ShipmentReceiptsInCollection root = factory.createShipmentReceiptsInCollection();
      //Add each master record to the Master collection
      java.util.List<ShipmentReceiptsInCollection.MRec> list = root.getMRec();
      list.add(i009Record);
      // fix for redmine ticket #58467 & #64810
      marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new StratisNamespaceMapper());
      marshaller.marshal(root, output);

      return output.toString();
    }
    catch (JAXBException e) {
      log.error("Error unmarshalling I009", e);
    }
    return null;
  }
}
