package mil.usmc.mls2.stratis.core.infrastructure;

import lombok.*;
import lombok.extern.slf4j.*;
import mil.usmc.mls2.stratis.common.domain.model.CamelMessage;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Processes CamelMessages.
 * After a transaction is committed, each CamelMessage is published to a camel endpoint using a camel ProducerTemplate.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CamelMessageListener {

  private final ProducerTemplate camelTemplate;

  /**
   * Synchronous message processor that places transactionally-committed camel messages onto camel endpoints.
   * <p>
   * DO *NOT* MAKE ASYNC (THIS IS SIMPLY POSTING TO A QUEUE - ASYNC WILL CAUSE MESSAGES TO BE PROCESSED OUT OF ORDER)
   */
  @TransactionalEventListener(fallbackExecution = true)
  public void handle(CamelMessage camelMessage) {
    log.debug("publishing camelMessage [{}] to {}", camelMessage.getMeaningfulIdentifier(), camelMessage.getUri());
    try {
      if (camelMessage.getHeaders().isEmpty()) {
        camelTemplate.sendBody(camelMessage.getUri(), camelMessage.getExchangePattern(), camelMessage.getBody());
      }
      else {
        camelTemplate.sendBodyAndHeaders(camelMessage.getUri(), camelMessage.getExchangePattern(), camelMessage.getBody(), camelMessage.getHeaders());
      }
    }
    catch (Exception e) {
      log.error("unable to publish camel message (trapping)", e);
    }
  }
}