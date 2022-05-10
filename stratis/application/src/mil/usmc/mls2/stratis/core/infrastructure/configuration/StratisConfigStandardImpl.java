package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile(Profiles.LEGACY)
public class StratisConfigStandardImpl implements StratisConfig {

  public boolean isMultiTenancyEnabled() {
    return false;
  }

  public boolean isDisplayCurrentSiteName() {
    return false;
  }

  public List<String> getDatasources() {
    return Collections.singletonList(GlobalConstants.LEGACY_DATABASE_CONNECTION_NAME);
  }

  public void setDatasources(List<String> sites) {
    //no-op
  }
}
