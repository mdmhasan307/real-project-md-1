package mil.usmc.mls2.stratis.core.utility;

import exmlservice.EXMLService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.common.share.ResourceUtility;
import org.springframework.stereotype.Component;

/**
 * wrapper class used to convert to throw stratisRuntimeExceptions.
 * EXMLService itself can't, due to the gcss.war now having access to StratisRuntimeExceptions.
 **/
@Component
@RequiredArgsConstructor
public class ExmlServiceManager {

  private final ResourceUtility resourceUtility;

  public byte[] compress(String xmldoc) {
    EXMLService eXMLFactory = getExmlService();

    try {
      return eXMLFactory.compress(xmldoc);
    }
    catch (Exception e) {
      throw new StratisRuntimeException("Exception trying to compress xmldocument", e);
    }
  }

  public String decompress(byte[] xmlbin) {
    EXMLService eXMLFactory = getExmlService();

    try {
      return eXMLFactory.decompress(xmlbin);
    }
    catch (Exception e) {
      throw new StratisRuntimeException("Exception trying to decompress xmldocument", e);
    }
  }

  private EXMLService getExmlService() {
    EXMLService eXMLFactory;
    try {
      val resource = resourceUtility.loadResource("classpath:xsd/RICE-Master-Schema.xsd");
      eXMLFactory = new EXMLService(resource.getFile());
      return eXMLFactory;
    }
    catch (Exception e) {
      throw new StratisRuntimeException("Exception creating EXMLService", e);
    }
  }
}
