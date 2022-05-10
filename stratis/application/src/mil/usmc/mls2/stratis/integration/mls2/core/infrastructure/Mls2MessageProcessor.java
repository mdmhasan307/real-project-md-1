package mil.usmc.mls2.stratis.integration.mls2.core.infrastructure;

import mil.usmc.mls2.integration.common.Mls2Message;

/**
 * Responsible for processing MLS2 Messages received via MLS2 Integration Services
 */
public interface Mls2MessageProcessor {

  /**
   * Processes an mls2 message
   * * 1. Persist inbound message
   * * 2. Publish inbound message to a queue for further processing
   *
   * @param message message to process
   */
  void process(Mls2Message message);
}
