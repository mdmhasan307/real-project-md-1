package mil.usmc.mls2.stratis.core.service.gcss;

import mil.usmc.mls2.stratis.modules.mhif.application.model.ItemMasterProcessResult;

import java.util.Set;

public interface I136NiinService {

  String FEED_NAME = "I136";

  ItemMasterProcessResult processI136Niin(String niin);

  ItemMasterProcessResult processI136Niins(Set<String> niins);

  ItemMasterProcessResult processBatchI136Niins();

  boolean isWsDisabled();
}
