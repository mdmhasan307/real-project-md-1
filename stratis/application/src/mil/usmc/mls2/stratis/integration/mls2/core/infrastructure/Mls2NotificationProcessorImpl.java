package mil.usmc.mls2.stratis.integration.mls2.core.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.integration.common.Mls2Message;
import mil.usmc.mls2.integration.migs.gcss.i033.outbound.message.GabfNotification;
import mil.usmc.mls2.integration.migs.gcss.i111.outbound.message.DasfNotification;
import mil.usmc.mls2.integration.migs.gcss.i112.outbound.message.GbofNotification;
import mil.usmc.mls2.integration.migs.gcss.i112.outbound.message.MatsNotification;
import mil.usmc.mls2.stratis.common.annotation.CommonDbTransaction;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.service.gcss.I033GabfService;
import mil.usmc.mls2.stratis.core.service.gcss.I111DasfService;
import mil.usmc.mls2.stratis.core.service.gcss.I112GbofService;
import mil.usmc.mls2.stratis.core.service.gcss.I112MatsService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
public class Mls2NotificationProcessorImpl implements Mls2NotificationProcessor {

  private final I111DasfService i111DasfService;
  private final I112MatsService i112MatsService;
  private final I112GbofService i112GbofService;
  private final I033GabfService i033GabfService;

  @CommonDbTransaction(propagation = Propagation.MANDATORY)
  public void processMigsNotification(Mls2Message mls2Message) {
    val messageTypeLabel = mls2Message.getMessageType().getLabel();
    log.info("New data notification received from MIGS.  Message type[{}].", messageTypeLabel);

    if (mls2Message instanceof DasfNotification) {
      i111DasfService.handleDasfNotification((DasfNotification) mls2Message);
      return;
    }
    else if (mls2Message instanceof MatsNotification) {
      i112MatsService.handleMatsNotification((MatsNotification) mls2Message);
      return;
    }
    else if (mls2Message instanceof GbofNotification) {
      i112GbofService.handleGbofNotification((GbofNotification) mls2Message);
      return;
    }
    else if (mls2Message instanceof GabfNotification) {
      i033GabfService.handleGabfNotification((GabfNotification) mls2Message);
      return;
    }

    log.error("No handler found for Message Type[{}]!  Exiting processMigsNotification.", messageTypeLabel);
  }
}