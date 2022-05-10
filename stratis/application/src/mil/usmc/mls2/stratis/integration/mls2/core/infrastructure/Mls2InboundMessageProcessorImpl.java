package mil.usmc.mls2.stratis.integration.mls2.core.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.annotation.CommonDbTransaction;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.integration.mls2.core.service.GcssI033MessagingService;
import mil.usmc.mls2.stratis.integration.mls2.core.service.GcssI111MessagingService;
import mil.usmc.mls2.stratis.integration.mls2.core.service.GcssI112GbofMessagingService;
import mil.usmc.mls2.stratis.integration.mls2.core.service.GcssI112MatsMessagingService;
import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.InboundMessagingId;
import mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.domain.model.InboundMessagingRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.util.UUID;

/**
 * FUTURE MIKE - this class will need to be updated to dynamically discover the appropriate IntegrationServiceProvider
 * from the IntegrationServiceProviderRegistry.  This will eliminate the necessity for manual configuration and maintenance
 * of message processors, and remove compile-time dependencies on other ext modules.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
class Mls2InboundMessageProcessorImpl implements Mls2InboundMessageProcessor {

  private final InboundMessagingRepository cmdInboundMessagingRepository;

  private final GcssI111MessagingService gcssI111MessagingService;
  private final GcssI112MatsMessagingService gcssI112MatsMessagingService;
  private final GcssI112GbofMessagingService gcssI112GbofMessagingService;
  private final GcssI033MessagingService gcssI033MessagingService;

  @Override
  @CommonDbTransaction(propagation = Propagation.MANDATORY)
  public void process(UUID inboundMessageId) {
    try {
      log.info("processing message {}", inboundMessageId);
      if (inboundMessageId == null) {
        log.warn("Warning: received a null message!");
        return;
      }

      val inboundMessage = cmdInboundMessagingRepository.find(InboundMessagingId.of(inboundMessageId)).orElseThrow(() -> new RuntimeException(String.format("Failed to find mls2 inbound message with id '%s'", inboundMessageId.toString())));
      switch (inboundMessage.type()) {
        case I111_DASF_RESPONSE:
          gcssI111MessagingService.processMessage(inboundMessage);
          break;
        case I033_GABF_RESPONSE:
          gcssI033MessagingService.processMessage(inboundMessage);
          break;
        case I112_MATS_RESPONSE:
          gcssI112MatsMessagingService.processMessage(inboundMessage);
          break;
        case I112_GBOF_RESPONSE:
          gcssI112GbofMessagingService.processMessage(inboundMessage);
          break;
        default:
          throw new StratisRuntimeException(String.format("Unsupported Mls2InboundMessageType of '%s' for message '%s'", inboundMessage.type(), inboundMessageId.toString()));
      }
    }
    catch (Exception e) {
      log.error("Failed to retrieveSummary inbound message '{}'", inboundMessageId, e);
    }
  }
}