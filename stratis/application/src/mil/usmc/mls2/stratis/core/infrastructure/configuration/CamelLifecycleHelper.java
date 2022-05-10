package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.*;
import lombok.extern.slf4j.*;
import mil.usmc.mls2.stratis.common.share.CamelContextReadyEvent;
import org.apache.camel.CamelContext;
import org.apache.camel.ServiceStatus;
import org.apache.camel.impl.DefaultCamelContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * This helper starts all previously registered Camel routes in the CamelContext after receiving an ApplicationReadyEvent event.
 *
 * Design:
 * By design, auto-startup of the context is enabled.
 * By design, auto-startup of routes is disabled.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class CamelLifecycleHelper {

  @Value("${camel.springboot.auto-startup}")
  private boolean autoStartupEnabled;

  private final CamelContext context;
  private final ApplicationEventPublisher eventPublisher;

  @EventListener
  public void onApplicationReady(ApplicationReadyEvent event) {
    log.debug("CamelLifecycleHelper onApplicationReady has been fired at timestamp [{}].  CamelContext type [{}].", event.getTimestamp(), context.getClass().toString());

    try {
      log.info("Camel Context Service Status: {}", context.getStatus());

      if (ServiceStatus.Starting == context.getStatus()) {
        log.info("Camel Context is already starting.");
      }
      else if (ServiceStatus.Started == context.getStatus()) {
        log.info("Camel Context already started.");
      }
      else if (ServiceStatus.Stopped == context.getStatus()) {
        log.info("Starting context...");
        context.start();
        log.info("Executed CamelContext.start() with [{}] endpoints registered.", context.getEndpoints().size());
      }

      if (!autoStartupEnabled) {
        log.info("Starting all routes...");
        ((DefaultCamelContext)context).startAllRoutes();
        log.info("Executed CamelContext.startAllRoutes().");
      }

      log.info("Publishing CamelContextReady event...");
      publishCamelContextReady();
    }
    catch (Exception e) {
      log.error("Exception encountered while starting Camel routes and processing.");
      throw new IllegalStateException("Could not successfully start CamelContext during Spring onApplicationReady event.", e);
    }
  }

  private void publishCamelContextReady() {
    val event = new CamelContextReadyEvent();
    eventPublisher.publishEvent(event);
  }
}
