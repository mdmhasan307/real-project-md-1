package mil.usmc.mls2.stratis.core.service.gcss;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.model.SortOrder;
import mil.usmc.mls2.stratis.common.model.enumeration.I136ProcessingStatusEnum;
import mil.usmc.mls2.stratis.core.domain.model.NiinInfo;
import mil.usmc.mls2.stratis.core.domain.model.NiinInfoSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.service.NiinInfoService;
import mil.usmc.mls2.stratis.core.service.SiteInfoService;
import mil.usmc.mls2.stratis.integration.mls2.core.service.GcssI136Service;
import mil.usmc.mls2.stratis.modules.mhif.application.model.ItemMasterProcessResult;
import mil.usmc.mls2.stratis.modules.r001.application.service.MigsR001ApiClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Profile(Profiles.INTEGRATION_ANY)
@SuppressWarnings("Duplicates")
public class I136NiinIntegrationServiceImpl implements I136NiinService {

  @Override
  //WS is never disabled in innovation mode.
  public boolean isWsDisabled() {
    return false;
  }

  private final SiteInfoService siteInfoService;
  private final NiinInfoService niinInfoService;
  private final GcssI136Service gcssI136Service;

  private final MigsR001ApiClient migsR001ApiClient;

  @Override
  public ItemMasterProcessResult processI136Niin(String niin) {
    return requestNiinInfoFromApi(Collections.singleton(niin));
  }

  /*
This is called from the front end in the admin -> interfaces screen for the MHIF (no NIIN input process)
 */
  @Override
  public ItemMasterProcessResult processBatchI136Niins() {
    val siteInfo = siteInfoService.getRecord();

    val niinInfoSearchCriteria = NiinInfoSearchCriteria.builder()
        .niinIdGreaterThan(siteInfo.getLastImportedNiinId())
        .build();
    niinInfoSearchCriteria.setSort("niinId", SortOrder.ASC);
    niinInfoSearchCriteria.setMaxItems(siteInfo.getMhifRange());

    val results = niinInfoService.search(niinInfoSearchCriteria);

    val niins = results.stream().map(NiinInfo::getNiin).collect(Collectors.toSet());

    val maxNiinId = results.stream().mapToInt(NiinInfo::getNiinId).max();

    val processingComplete = processI136Niins(niins);

    if (maxNiinId.isPresent()) {
      siteInfo.updateMhifLastImportedNiinId(maxNiinId.getAsInt());
      siteInfoService.save(siteInfo);
    }
    return processingComplete;
  }

  @Override
  public ItemMasterProcessResult processI136Niins(Set<String> niins) {
    log.info("Inside MigsProcessor.processI136Niins");
    return requestNiinInfoFromApi(niins);
  }

  private ItemMasterProcessResult requestNiinInfoFromApi(Set<String> niins) {
    try {
      val results = migsR001ApiClient.fetchItemMaster(niins);
      val itemMasterResult = gcssI136Service.processItemMasterData(new HashSet<>(results), false);
      log.debug("Item Master Result {}", itemMasterResult);
      return itemMasterResult;
    }
    catch (Exception e) {
      log.error("An Exception occurred requesting data from Middleware API", e);
      return ItemMasterProcessResult.ofStatusWithTotal(I136ProcessingStatusEnum.FAILURE_ERROR_GETTING_DATA_FROM_MIGS_API, niins.size());
    }
  }
}