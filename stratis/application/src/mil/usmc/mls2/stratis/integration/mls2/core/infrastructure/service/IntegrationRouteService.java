package mil.usmc.mls2.stratis.integration.mls2.core.infrastructure.service;

import mil.usmc.mls2.integration.common.Mls2Message;
import org.apache.camel.Exchange;

import java.util.UUID;

/**
 * Provides routing services.
 * This class is primarily used by MLs2IntegrationRouteBuilder (dynamic configuration)
 */
@SuppressWarnings("unused")
public interface IntegrationRouteService {

  void processMls2Message(Mls2Message message, Exchange exchange);

  void processTest(Object message, Exchange exchange);

  void processMls2InboundMessage(UUID inboundMessageId);

  void interceptOutbound(Mls2Message message, Exchange exchange);

  void processMigsNotification(Mls2Message message, Exchange exchange);
}