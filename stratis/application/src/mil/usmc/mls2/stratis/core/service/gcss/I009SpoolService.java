package mil.usmc.mls2.stratis.core.service.gcss;

import exmlservice.I009.ShipmentReceiptsInCollection;
import mil.usmc.mls2.stratis.core.domain.model.Issue;
import mil.usmc.mls2.stratis.core.domain.model.Picking;
import mil.usmc.mls2.stratis.core.domain.model.SiteInfo;

import java.util.Optional;

public interface I009SpoolService {

  Optional<String> marshallRecord(ShipmentReceiptsInCollection.MRec record);

  boolean marshallAndSpool(ShipmentReceiptsInCollection.MRec record, Integer niinId, Integer userId);

  void sendSROGCSSMCTransaction(SiteInfo siteInfo, Picking picking);

  void sendAE1GCSSMCTransaction(SiteInfo siteInfo, String scn);

  void sendAsxTrans(SiteInfo siteInfo, Issue issue, Integer transNumber);

  void insertSpoolRecord(String transactionType,
                         String i136Xml, ShipmentReceiptsInCollection.MRec record, Integer niinId, Integer userId);
}
