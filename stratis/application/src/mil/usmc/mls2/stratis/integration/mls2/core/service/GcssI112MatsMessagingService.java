package mil.usmc.mls2.stratis.integration.mls2.core.service;

import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.InboundMessaging;

public interface GcssI112MatsMessagingService {

  void processMessage(InboundMessaging inboundMessage);
}