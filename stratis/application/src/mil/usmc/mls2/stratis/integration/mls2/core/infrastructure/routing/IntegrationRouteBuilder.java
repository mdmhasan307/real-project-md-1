package mil.usmc.mls2.stratis.integration.mls2.core.infrastructure.routing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.BeanConstants;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.integration.mls2.core.infrastructure.service.IntegrationRouteService;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static java.lang.String.format;
import static mil.usmc.mls2.stratis.integration.mls2.core.infrastructure.routing.IntegrationResources.*;

/**
 * MLS2 Integration Route Builder responsible for managing inbound and outbound routes.
 * <p>
 * Routes
 * Direct routes are used to provide an internal filter for outbound messages before they are sent to the MLS2 core endpoints.
 */
@Slf4j
@Component
@DependsOn(BeanConstants.INTEGRATION_JMS)
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
public class IntegrationRouteBuilder extends RouteBuilder {

  private static final String QUEUE_STRATIS_INTERNAL_INBOUND_MESSAGES = resolveQueue(NAME_QUEUE_STRATIS_INTERNAL_INBOUND_MESSAGES);
  private static final String QUEUE_STRATIS_INTERNAL_TEST = resolveQueue(NAME_QUEUE_STRATIS_INTERNAL_TEST);
  private static final String QUEUE_MLS2_ANY_MIPS_TEST = resolveQueue(NAME_QUEUE_MLS2_ANY_MIPS_TEST);
  private static final String QUEUE_MLS2_STRATIS_MIPS = resolveQueue(NAME_QUEUE_MLS2_STRATIS_MIPS);
  private static final String QUEUE_MLS2_MIPS_STRATIS = resolveQueue(NAME_QUEUE_MLS2_MIPS_STRATIS);
  private static final String TOPIC_MLS2_MIGS_NOTIFICATIONS = resolveTopic(NAME_TOPIC_MLS2_MIGS_NOTIFICATIONS);

  private final IntegrationRouteService integrationRouteService;

  @Override
  public void configure() {
    log.info("Configuring STRATIS Integration Routes...");

    configureInterceptors();
    configureDirectRoutes();
    configureInternalRoutes();
    configureOutboundBridges();
    configureInboundBridges();

    log.info("STRATIS Integration Routes configured.");
  }

  private static String resolveQueue(String name) {
    return format("%s:queue:%s", BeanConstants.INTEGRATION_JMS, name);
  }

  private static String resolveTopic(String name) {
    return format("%s:topic:%s", BeanConstants.INTEGRATION_JMS, name);
  }

  private void configureInterceptors() {
    interceptSendToEndpoint(QUEUE_MLS2_STRATIS_MIPS).bean(integrationRouteService, "interceptOutbound");
  }

  private void configureDirectRoutes() {
    txFrom(DIRECT_STRATIS_INTERNAL_TEST).to(QUEUE_STRATIS_INTERNAL_TEST);
    txFrom(DIRECT_MLS2_ANY_MIPS_TEST).to(QUEUE_MLS2_ANY_MIPS_TEST);
    txFrom(DIRECT_MLS2_INBOUND_MESSAGES).to(QUEUE_STRATIS_INTERNAL_INBOUND_MESSAGES);
    txFrom(DIRECT_MLS2_STRATIS_MIPS).to(QUEUE_MLS2_STRATIS_MIPS);
  }

  private void configureInternalRoutes() {
    txFrom(QUEUE_STRATIS_INTERNAL_INBOUND_MESSAGES).bean(integrationRouteService, "processMls2InboundMessage");
    txFrom(QUEUE_STRATIS_INTERNAL_TEST).bean(integrationRouteService, "processTest");
    txFrom(QUEUE_MLS2_MIPS_STRATIS).bean(integrationRouteService, "processMls2Message");
    txFrom(TOPIC_MLS2_MIGS_NOTIFICATIONS).bean(integrationRouteService, "processMigsNotification");
  }

  private void configureOutboundBridges() {
    // noop - app does not communicate with external brokers
  }

  private void configureInboundBridges() {
    // noop - app does not communicate with external brokers
  }

  //JMS transactions disabled in stratis
  private RouteDefinition txFrom(String endpoint) {
    //return from(endpoint).transacted();
    return from(endpoint);
  }
}