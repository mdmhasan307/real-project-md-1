package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.share.caching.Mls2CacheManager;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQSession;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;

import javax.jms.ConnectionFactory;
import javax.jms.Session;

import static java.lang.String.format;

/**
 * Integration Configuration - same for Enterprise and Tactical
 * <p>
 * Note: sync changes to this file with CLC2S/TCPT/MIPS/MIGS/STRATIS
 * Note: Manual constructor must be kept due to needing to qualify the database transaction manager to use.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@AutoConfigureAfter(value = {ApplicationConfig.class, CachingConfig.class})
@Profile(Profiles.INTEGRATION_ANY)
@ConditionalOnProperty(prefix = "spring.jta", name = "enabled", havingValue = "false")
public class MessagingConfig {

  private final AppProperties appProps;
  private final MessagingConfigProps props;

  /**
   * Integration JmsComponent
   * For camel-based interaction with the local integration broker
   */
  @Bean(BeanConstants.INTEGRATION_JMS)
  @DependsOn(BeanConstants.INTEGRATION_JMS_CONFIGURATION)
  public JmsComponent integration() {
    val config = CamelUtility.createJmsConfiguration(integrationJmsConnectionFactory(), true);
    configureSubscriptions(config);

    return new JmsComponent(config);
  }

  /**
   * Ensures all MLS2-related endpoints are configured prior to reference by other application and services (ex: Camel)
   */
  @Bean(BeanConstants.INTEGRATION_JMS_CONFIGURATION)
  public Void integrationJmsConfiguration(Mls2CacheManager mls2CacheManager) throws Exception {
    configureResources(mls2CacheManager);
    return null;
  }

  @Bean(BeanConstants.INTEGRATION_JMS_CONNECTION_FACTORY)
  public ConnectionFactory integrationJmsConnectionFactory() {
    // create amq connection factory
    val amqConnectionFactory = createAmqConnectionFactory();

    // create pooled connection factory (optional)
    return props.getStd().getPool().isEnabled() ? createPoolConnectionFactory(amqConnectionFactory) : amqConnectionFactory;
  }

  /**
   * Creates an ActiveMQConnectionFactory
   * clientID - primarily used for durable subscriptions to topics
   * setEnableSharedClientID required when using a connection pool to allow the same client id to be used for all connections
   */
  private ActiveMQConnectionFactory createAmqConnectionFactory() {
    val amqConnectionFactory = new ActiveMQConnectionFactory(props.getBrokerUrl());
    MessagingConfigHelper.configureConnectionFactory(amqConnectionFactory, props, appProps);

    return amqConnectionFactory;
  }

  /**
   * setConnectionIdleTimeout:
   * ...
   * The pool's connectionIdleTimeout setting should be lte to the underlying connection factory's connectionTtl setting.
   */
  private JmsPoolConnectionFactory createPoolConnectionFactory(ActiveMQConnectionFactory amqConnectionFactory) {
    val poolProps = props.getStd().getPool();
    val pool = new JmsPoolConnectionFactory();
    pool.setConnectionFactory(amqConnectionFactory);
    pool.setConnectionIdleTimeout(poolProps.getIdleTimeout());
    pool.setMaxConnections(poolProps.getMaxConnections());
    pool.setMaxSessionsPerConnection(poolProps.getMaxSessionsPerConnection());
    pool.setUseAnonymousProducers(poolProps.isAnonymousProducers());

    return pool;
  }

  /**
   * Shared subscriptions allow multiple consumers (ex: on different nodes of a cluster) to share a single Topic subscription
   */
  private void configureSubscriptions(JmsConfiguration config) {
    config.setSubscriptionDurable(true);
    config.setSubscriptionShared(true);
    config.setDurableSubscriptionName(format("%s-sub", appProps.getName()));
  }

  private void configureResources(Mls2CacheManager mls2CacheManager) throws Exception {
    log.info("Configuring messaging resources...");
    log.info("Establishing JMS connection...");

    try (
        val amqConnectionFactory = createAmqConnectionFactory();
        val connection = amqConnectionFactory.createConnection();
        val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
    ) {
      val activeMQSession = ((ActiveMQSession) session);
      MessagingConfigHelper.configureResources(activeMQSession, mls2CacheManager);
    }

    log.info("Messaging resources configured.");
  }
}