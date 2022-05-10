package mil.usmc.mls2.stratis.integration.mls2.core.service;

import mil.usmc.mls2.integration.migs.gcss.r001.outbound.model.ItemMasterData;
import mil.usmc.mls2.stratis.modules.mhif.application.model.ItemMasterProcessResult;

import java.util.Set;

public interface GcssI136Service {

  ItemMasterProcessResult processItemMasterData(Set<ItemMasterData> data, boolean alwaysProcess);
}
