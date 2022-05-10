package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.integration.cache.common.CacheConstants;
import mil.usmc.mls2.integration.cache.common.Mls2ShareMessagingConfigState;
import mil.usmc.mls2.integration.common.Endpoints;
import mil.usmc.mls2.stratis.common.share.caching.Mls2CacheManager;
import mil.usmc.mls2.stratis.common.utility.JavaExtensions;
import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.QueueConfiguration;
import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQSession;

import javax.jms.JMSException;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static mil.usmc.mls2.stratis.integration.mls2.core.infrastructure.routing.IntegrationResources.NAME_QUEUE_STRATIS_INTERNAL_INBOUND_MESSAGES;
import static mil.usmc.mls2.stratis.integration.mls2.core.infrastructure.routing.IntegrationResources.NAME_QUEUE_STRATIS_INTERNAL_TEST;

@Slf4j
class MessagingConfigHelper {

  /**
   * setCallTimeout:
   * Defines the blocking calls timeout
   * <p>
   * setConnectionTTL:
   * This TTL determines how long the server will keep a connection alive in the absence of any data arriving from the client.
   */
  static void configureConnectionFactory(ActiveMQConnectionFactory amqConnectionFactory, MessagingConfigProps props, AppProperties appProps) {
    // profile settings
    amqConnectionFactory.setClientID(resolveClientId(appProps));
    amqConnectionFactory.setEnableSharedClientID(true);
    amqConnectionFactory.setUser(appProps.getName());

    // fault-tolerance settings
    amqConnectionFactory.setCallTimeout(props.getCallTimeout());
    amqConnectionFactory.setConnectionTTL(props.getConnectionTtl());
    amqConnectionFactory.setInitialConnectAttempts(props.getInitialConnectAttempts());
    amqConnectionFactory.setReconnectAttempts(props.getReconnectAttempts());
    amqConnectionFactory.setRetryInterval(props.getRetryInterval());
    amqConnectionFactory.setRetryIntervalMultiplier(props.getRetryIntervalMultiplier());
    amqConnectionFactory.setMaxRetryInterval(props.getMaxRetryInterval());

    // performance / ensure ordering
    amqConnectionFactory.setConsumerWindowSize(props.getConsumerWindowSize());
    amqConnectionFactory.setConsumerMaxRate(props.getConsumerMaxRate());

    // non-transactional settings
    amqConnectionFactory.setBlockOnAcknowledge(props.isBlockOnAcknowledge());
    amqConnectionFactory.setBlockOnDurableSend(props.isBlockOnDurableSend());
    amqConnectionFactory.setBlockOnNonDurableSend(props.isBlockOnNonDurableSend());

    // thread settings: configures separate thread pools for factory (not shared in jvm)
    amqConnectionFactory.setUseGlobalPools(false); // def: true
    amqConnectionFactory.setScheduledThreadPoolMaxSize(5); // scheduled thread pool (def: 5)
    amqConnectionFactory.setThreadPoolMaxSize(-1); // general purpose thread pool (def: -1)
  }

  static void configureResources(ActiveMQSession activeMQSession, Mls2CacheManager mls2CacheManager) throws Exception {
    val clientSession = activeMQSession.getCoreSession();

    // mls2-share: configuration is limited to verifying existence of queues used by app (tests 1 queue)
    log.info("Creating mls2-share resources...");
    validateMls2ShareMessagingConfiguration(mls2CacheManager);
    val qq = clientSession.queueQuery(SimpleString.toSimpleString(Endpoints.NAME_QUEUE_MLS2_STRATIS_MIPS));
    if (!qq.isExists()) {
      throw new IllegalStateException("STRATIS Startup Exception - MIPS Caching Services not available or queues not created.");
    }

    // stratis-app
    log.info("Creating stratis-app resources...");
    createQueue(NAME_QUEUE_STRATIS_INTERNAL_INBOUND_MESSAGES, true, clientSession);
    createQueue(NAME_QUEUE_STRATIS_INTERNAL_TEST, true, clientSession);

    log.info("Messaging resources configured.");
  }

  static boolean resourceExists(String name, ClientSession clientSession) throws JMSException {
    try {
      val qq = clientSession.queueQuery(SimpleString.toSimpleString(name));
      return qq.isExists();
    }
    catch (ActiveMQException e) {
      val jmsException = new JMSException(String.format("Failed to determine if resource '%s' exists", name));
      jmsException.setLinkedException(e);
      throw jmsException;
    }
  }

  /**
   * Blocks until the mls2-share messaging configuration has completed.
   * Note: MIPS is responsible for the lifecycle configuration of mls2-share messaging.
   */
  private static void validateMls2ShareMessagingConfiguration(Mls2CacheManager mls2CacheManager) {
    boolean configured = false;

    log.info("MLS2-Share Messaging Configuration validation (max duration: 100 seconds)...");

    for (int i = 1; i <= 10; i++) {
      val currentState = (Mls2ShareMessagingConfigState) mls2CacheManager.get(CacheConstants.MLS2_SHARE_MESSAGING_CONFIG_STATE, CacheConstants.MLS2_SHARE_MESSAGING_CONFIG_STATE);
      if (currentState == null) {
        log.info("MLS2-Share Messaging Configuration Not Found: sleeping for 10 seconds to allow MIPS to complete startup...");
        JavaExtensions._safeSleep(10, TimeUnit.SECONDS);
      }
      else if (Mls2ShareMessagingConfigState.CURRENT_VERSION > currentState.version()) {
        log.info("MLS2-Share Messaging Configuration Outdated:  sleeping for 10 seconds to allow MIPS to update messaging configuration from version {} to {}", currentState.version(), Mls2ShareMessagingConfigState.CURRENT_VERSION);
        JavaExtensions._safeSleep(10, TimeUnit.SECONDS);
      }
      else if (Mls2ShareMessagingConfigState.CURRENT_VERSION < currentState.version()) {
        throw new IllegalStateException(format("MIPS/APP misconfiguration - This application is expecting a messaging configuration version of %s but found newer version of %s - This application is outdated (please upgrade)", Mls2ShareMessagingConfigState.CURRENT_VERSION, currentState.version()));
      }
      else {
        log.info("MLS2-Share Messaging Configuration validation complete.");
        configured = true;
        break;
      }
    }

    if (!configured) {
      throw new IllegalStateException("Application startup exception - timeout waiting for MIPS to configure the mls2-share messaging configuration");
    }
  }

  /**
   * Creates the given queue (iff does not exist)
   * Uses ClientSession (from ActiveMQSession) to properly configure the given queue
   * <p>
   * Note:  attempting to configure the queue using a simple ActiveMQSession or Session.createQueue(<queue_name>) will ignore any queue options (ex: 'myQueue?exclusive=true')
   *
   * @param name          simple name of the queue
   * @param exclusive     indicates if the queue is an exclusive queue (only 1 consumer)
   * @param clientSession session on which to operate
   */
  @SuppressWarnings("SameParameterValue")
  private static void createQueue(String name, boolean exclusive, ClientSession clientSession) throws Exception {
    if (!resourceExists(name, clientSession)) {
      val queueConfig = new QueueConfiguration(name);
      queueConfig.setExclusive(exclusive);
      queueConfig.setMaxConsumers(exclusive ? 1 : -1);
      queueConfig.setRoutingType(RoutingType.ANYCAST);
      queueConfig.setDurable(true);

      clientSession.createQueue(queueConfig);
    }
  }

  @SuppressWarnings({"SameParameterValue", "unused"})
  private static void createTopic(String name, ActiveMQSession activeMQSession) throws Exception {
    // note: no need to perform exists check here
    activeMQSession.createTopic(name);
  }

  private static String resolveClientId(AppProperties appProps) {
    return format("%s-%s-%s", appProps.getName(), appProps.getEnvironmentId(), appProps.getInstanceId());
  }
}
