package mil.usmc.mls2.stratis.common.domain.model;

import lombok.experimental.UtilityClass;

/**
 * Relocate to qry and cmd packages if the underlying data store is not shared
 */
@UtilityClass
public class TableNames {

  public static final String MLS2_INBOUND_MESSAGE = "MLS2_INBOUND_MSG";
  public static final String MLS2_OUTBOUND_MESSAGE = "MLS2_OUTBOUND_MSG";
  public static final String GCSS_I111_DASF_FEED = "GCSS_I111_DASF_FEED";
  public static final String GCSS_I111_DASF_DATA = "GCSS_I111_DASF_DATA";
}