package mil.usmc.mls2.stratis.integration.mls2.core.infrastructure;

import java.util.UUID;

/**
 * Responsible for the processing of MLS2InboundMessages (called via routes)
 */
public interface Mls2InboundMessageProcessor {

  /**
   * Process inbound message (call configured via Routes)
   *
   * @param inboundMessageId inboundMessage identifier
   */
  @SuppressWarnings("unused")
  void process(UUID inboundMessageId);
}
