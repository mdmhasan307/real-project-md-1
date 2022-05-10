package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.Data;
import org.apache.activemq.artemis.api.core.client.ActiveMQClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MessagingConfigurationProperties
 * <p>
 * Flow control is used to limit the flow of data between a client and server, or a server and another server in order to prevent the
 * client or server being overwhelmed with data.
 * <p>
 * consumerWindowSize:
 * default: 1048576 (1 MiB)
 * 0 = disables client-side buffering (load-balancing friendly), -1 disables flow control (unbounded), > 0 buffer of max bytes
 */
@Data
@Component("integrationMessagingConfigProps")
@ConfigurationProperties(prefix = "stratis.integration.messaging")
class MessagingConfigProps {

  private String brokerUrl;
  private int retryInterval;
  private double retryIntervalMultiplier;
  private long maxRetryInterval;
  private int initialConnectAttempts;
  private int reconnectAttempts;
  private boolean blockOnAcknowledge;
  private boolean blockOnDurableSend;
  private boolean blockOnNonDurableSend;

  private long callTimeout = ActiveMQClient.DEFAULT_CALL_TIMEOUT;
  private long connectionTtl = ActiveMQClient.DEFAULT_CONNECTION_TTL;

  private int consumerWindowSize = 1048576;
  private int consumerMaxRate = -1;
  private StdProps std = new StdProps();
  private XAProps xa = new XAProps();

  @Data
  static class StdProps {

    private Pool pool = new Pool();
  }

  @Data
  static class XAProps {

  }

  @Data
  static class Pool {

    private boolean enabled;
    private int maxConnections = 1;
    private int maxSessionsPerConnection = 500;
    private boolean anonymousProducers = true;
    private int idleTimeout = 300000;
  }
}