package mil.usmc.mls2.stratis.integration.mls2.core.service;

import mil.usmc.mls2.integration.common.model.MessageMeta;
import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.InboundMessaging;
import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.OutboundMessaging;
import org.slf4j.Logger;

import java.util.UUID;

public interface MessageSupport {

  void flagInboundMessageProcessedAndSaveEach(InboundMessaging inboundMessage, OutboundMessaging outboundMessage, Logger log);

  void flagOutboundMessageProcessedAndSaveEach(OutboundMessaging outboundMessage, Logger log);

  void handleMls2InboundMessageException(Exception e, InboundMessaging inboundMessage, Logger log);

  void handleMls2OutboundMessageException(Exception e, OutboundMessaging outboundMessage, Logger log);

  MessageMeta buildMetaMessage(UUID correlationId, UUID toSystemId);

  void processOutboundMessages(OutboundMessaging outboundMessage);
}