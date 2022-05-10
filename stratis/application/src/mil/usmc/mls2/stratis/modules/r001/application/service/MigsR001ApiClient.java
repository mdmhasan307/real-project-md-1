package mil.usmc.mls2.stratis.modules.r001.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.integration.migs.gcss.r001.outbound.model.ItemMasterData;
import mil.usmc.mls2.stratis.common.share.ResourceUtility;
import mil.usmc.mls2.stratis.common.share.caching.Mls2CacheManager;
import mil.usmc.mls2.stratis.common.share.hmac.HmacUtility;
import mil.usmc.mls2.stratis.common.share.rest.BaseMls2ApiClient;
import mil.usmc.mls2.stratis.common.share.rest.Mls2ClientSettings;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
public class MigsR001ApiClient extends BaseMls2ApiClient {

  private static final String SHARED_MAP_SYSTEM_URL_LIST = "system_url_list"; //matches value in StartupListener of MIPS

  private static final String BASE_URI = "restws/ext/mls2/r001";

  private final Mls2CacheManager mls2CacheManager;
  private final HmacUtility hmacUtility;
  private final ResourceUtility resourceUtility;
  private final Mls2ClientSettings settings;

  /**
   * Fetch Item Master Data from the MIGS R-001 API
   */
  @Transactional
  public Collection<ItemMasterData> fetchItemMaster(Set<String> niins) {
    try {
      val responseJson = postJson("bulk-item-master", niins);
      val response = convertToClientGenericCollectionResponse(ItemMasterData.class, responseJson);

      return response.getData();
    }
    catch (Exception e) {
      throw new IllegalStateException("failed to connect to MIGS", e);
    }
  }

  @PostConstruct
  private void postConstruct() throws Exception {
    super.configure(settings, hmacUtility, resourceUtility);
  }

  @Override
  protected String baseEndpointAddress() {

    val hmap = mls2CacheManager.getMap(SHARED_MAP_SYSTEM_URL_LIST);

    return (String) hmap.get("MIGS");
  }

  @Override
  protected String baseUri() {
    return BASE_URI;
  }
}
