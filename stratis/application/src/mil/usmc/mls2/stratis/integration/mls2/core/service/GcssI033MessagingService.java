package mil.usmc.mls2.stratis.integration.mls2.core.service;

import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.InboundMessaging;

public interface GcssI033MessagingService {

  void processMessage(InboundMessaging inboundMessage);
}