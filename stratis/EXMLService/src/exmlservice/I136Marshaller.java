package exmlservice;

import exmlservice.I136.ObjectFactory;
import exmlservice.I136.StratisItemMasterRequest;
import lombok.extern.slf4j.Slf4j;
import oracle.xml.jaxb.JaxbContextImpl;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.HashMap;

@Slf4j
public class I136Marshaller {

  public static final String ROOT_TAG = "stratisItemMasterRequest";

  public static final String NAMESPACE = "http://www.usmc.mil/schemas/1/if/stratis";

  private I136Marshaller() {
  }

  public static ByteArrayOutputStream createXMLString(String buildCompleteXML,
                                                      HashMap hm) {
    try {
      JaxbContextImpl jaxbContext = new JaxbContextImpl();
      Marshaller marshaller = jaxbContext.createMarshaller();

      ObjectFactory factory = new ObjectFactory();
      StratisItemMasterRequest request = factory.createStratisItemMasterRequest();
      if (hm.get("countRequested") != null)
        request.setCountRequested(BigInteger.valueOf(Long.parseLong(hm.get("countRequested").toString())));
      if (hm.get("routingIdentifierCode") != null)
        request.setRIC(hm.get("routingIdentifierCode").toString());
      StratisItemMasterRequest.StratisItemCollection requestColl = request.getStratisItemCollection();
      //Add each record to the NIIN collection
      java.util.List aS1List = requestColl.getNIIN();
      if (hm.get("nationalItemIdentificationNumber") != null)
        aS1List.add(factory.createNIIN(hm.get("nationalItemIdentificationNumber").toString()));

      ByteArrayOutputStream output = new ByteArrayOutputStream();
      marshaller.marshal(request, output);

      return output;
    }
    catch (JAXBException e) {
      log.error("Error creating XML String I136", e);
    }
    return null;
  }
}
