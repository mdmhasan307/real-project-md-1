package mil.usmc.mls2.stratis.integration.mls2.core.infrastructure.routing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * DIRECT_MLS2_INBOUND_MESSAGES:      used for throttled processing of inbound messages (by id)
 * DIRECT_MLS2_STRATIS_MIPS:             stratis -> mips traffic
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IntegrationResources {

  public static final String DIRECT_STRATIS_INTERNAL_TEST = "direct:stratis.internal.test";

  public static final String DIRECT_MLS2_ANY_MIPS_TEST = "direct:mls2.any.mips.test";
  public static final String DIRECT_MLS2_STRATIS_MIPS = "direct:mls2.stratis.mips";
  public static final String DIRECT_MLS2_INBOUND_MESSAGES = "direct:integration.mls2.inboundMessages";

  public static final String NAME_QUEUE_MLS2_ANY_MIPS_TEST = "mls2-share.any.mips.test";
  public static final String NAME_QUEUE_MLS2_STRATIS_MIPS = "mls2-share.stratis.mips";
  public static final String NAME_QUEUE_MLS2_MIPS_STRATIS = "mls2-share.mips.stratis";
  public static final String NAME_TOPIC_MLS2_MIGS_NOTIFICATIONS = "mls2-share.migs.notifications";
  public static final String NAME_QUEUE_STRATIS_INTERNAL_INBOUND_MESSAGES = "stratis-app.internal.mls2InboundMessages";
  public static final String NAME_QUEUE_STRATIS_INTERNAL_TEST = "stratis-app.internal.test";
}