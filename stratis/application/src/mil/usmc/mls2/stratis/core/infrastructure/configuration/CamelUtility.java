package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.camel.LoggingLevel;
import org.apache.camel.component.jms.ConsumerType;
import org.apache.camel.component.jms.JmsConfiguration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;

import static org.springframework.jms.listener.DefaultMessageListenerContainer.CACHE_AUTO;
import static org.springframework.jms.listener.DefaultMessageListenerContainer.CACHE_NONE;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class CamelUtility {

  private static final int SINGLE_CONSUMER = 1;

  /**
   * Generates default JmsConfiguration
   * Values can be overridden on the returned configuration object.
   * <p>
   * note: do *not* set the clientId in the JmsConfiguration (configured elsewhere)
   * <p>
   * concurrentConsumers, maxConcurrentConsumers:
   * Keep at 1 for topic listeners or if queue ordering is important and to avoid message loss if caching is used
   * <p>
   * transacted:
   * Misnomer property, indicates if *local* jms transactions (transacted sessions) should be used. Do not use with XA or ChainedTransactionManager.
   * <p>
   * transferException:
   * Allows camel to be used as a bridge in routing
   * <p>
   * connectionFactory:
   * The default connection factory to be used if template or listener connection factories are not specified
   * <p>
   * listenerConnectionFactory:
   * Used for consuming messages
   * <p>
   * templateConnectionFactory:
   * Used for producing messages
   * <p>
   * setLazyCreateTransactionManager:
   * If true, Camel will create a JmsTransactionManager, if there is no transaction manager injected with option transacted=true
   * Keep false as we do *not* want camel to create a JmsTransactionManager, we instead want to rely on broker-provided transaction management.
   * <p>
   * <p>
   * setTransacted --> setSessionTransacted
   * <p>
   * Spring DMLC:
   * - https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/jms/listener/AbstractMessageListenerContainer.html
   * - https://access.redhat.com/documentation/en-us/red_hat_jboss_fuse/6.3/html/transaction_guide/fmrtxnjms
   * <p>
   * Note that the "sessionTransacted" flag is strongly recommended over JmsTransactionManager, provided that transactions do not need to be
   * managed externally. As a consequence, set the transaction manager only if you are using JTA or if you need to synchronize with custom
   * external transaction arrangements.
   * <p>
   * Note that *not* setting a transaction manager makes Spring use only one JMS session and one transaction.
   * <p>
   * DMLC transacted session API: transacted=true, lazyCreateTransactionManager(false), transactionManager=null
   */
  public static JmsConfiguration createJmsConfiguration(ConnectionFactory connectionFactory, boolean autoStartup) {
    val config = new JmsConfiguration();
    config.setAcceptMessagesWhileStopping(false);
    config.setAutoStartup(autoStartup);
    config.setCacheLevel(CACHE_AUTO);
    config.setConcurrentConsumers(SINGLE_CONSUMER);
    config.setConnectionFactory(connectionFactory);
    config.setConsumerType(ConsumerType.Default);
    config.setDeliveryPersistent(true);
    config.setDeliveryMode(DeliveryMode.PERSISTENT);
    config.setErrorHandlerLogStackTrace(true);
    config.setErrorHandlerLoggingLevel(LoggingLevel.WARN);
    config.setLazyCreateTransactionManager(false);
    config.setMaxConcurrentConsumers(SINGLE_CONSUMER);
    config.setReceiveTimeout(1000);
    config.setRecoveryInterval(5000);
    config.setRequestTimeout(20000);
    config.setStreamMessageTypeEnabled(true);
    config.setTestConnectionOnStartup(true);
    config.setTransacted(true);
    config.setTransactionManager(null);
    config.setTransferException(true);

    return config;
  }

  public static JmsConfiguration createJmsXAConfiguration(ConnectionFactory connectionFactory, PlatformTransactionManager transactionManager, boolean autoStartup) {
    val config = createJmsConfiguration(connectionFactory, autoStartup);
    configureXATransactionSupport(config, transactionManager);

    return config;
  }

  /**
   * Configures XA Transaction Settings
   * <p>
   * setCacheLevel:
   * CACHE_NONE: "DO *NOT* CHANGE THIS - CACHE_CONSUMER with JTA cannot be used as it will cause messages to be *READ BUT NOT REMOVED FROM A QUEUE*
   * as no acknowledgement is issued because the consumer is never terminated.  This results in no notification to the broker and the message
   * remaining in the source queue.
   * <p>
   * JMS Polling will produce *numerous* entries in the mips-messaging log per poll, when consumers are not cached on the client.
   * mips-messaging audit logging should be updated to a log level of WARN to avoid these alarming but harmless log entries.
   * <p>
   * setLazyCreateTransactionManager: disable transactionTimeout in the client for XA transactions
   * <p>
   * setTransacted: 'false' to disable LOCAL transactions and enable EXTERNAL transactions
   */
  public static void configureXATransactionSupport(JmsConfiguration config, PlatformTransactionManager transactionManager) {
    config.setCacheLevel(CACHE_NONE);
    config.setLazyCreateTransactionManager(false);
    config.setTransacted(false);
    config.setTransactionManager(transactionManager);
  }
}
