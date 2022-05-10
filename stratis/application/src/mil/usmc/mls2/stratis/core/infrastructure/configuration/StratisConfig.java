package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import java.util.List;

public interface StratisConfig {

  String USER_DB_SELECTED = "userDbSelected";
  String USER_SITE_NAME = "userSiteName";

  boolean isMultiTenancyEnabled();

  boolean isDisplayCurrentSiteName();

  List<String> getDatasources();

  void setDatasources(List<String> sites);
}
