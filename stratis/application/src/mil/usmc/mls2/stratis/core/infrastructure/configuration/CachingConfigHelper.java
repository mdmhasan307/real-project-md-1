package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientConnectionStrategyConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.client.config.ClientSecurityConfig;
import com.hazelcast.config.SSLConfig;
import com.hazelcast.core.HazelcastInstance;
import lombok.val;

class CachingConfigHelper {

  public static HazelcastInstance createHazelcastInstance(CachingConfigProps.CacheProps cacheProps, CachingConfigProps props, AppProperties appProps) {
    val config = new ClientConfig();
    config.setClusterName(cacheProps.getClusterName());
    config.setInstanceName(resolveInstanceName(cacheProps.getClusterName(), appProps));
    config.setNetworkConfig(createNetworkConfig(cacheProps, props));
    config.setSecurityConfig(createSecurityConfig());
    config.setConnectionStrategyConfig(createConnectionStrategyConfig(props));
    config.setProperty("hazelcast.jmx", String.valueOf(props.isJmxEnabled()));

    return HazelcastClient.newHazelcastClient(config);
  }

  private static ClientNetworkConfig createNetworkConfig(CachingConfigProps.CacheProps cacheProps, CachingConfigProps props) {
    val networkConfig = new ClientNetworkConfig();
    networkConfig.setAddresses(cacheProps.getAddresses());
    networkConfig.setOutboundPortDefinitions(props.getOutboundPorts());
    networkConfig.setSSLConfig(createSslConfig());

    return networkConfig;
  }

  private static SSLConfig createSslConfig() {
    val sslConfig = new SSLConfig();
    sslConfig.setEnabled(false);

    return sslConfig;
  }

  private static ClientSecurityConfig createSecurityConfig() {
    return new ClientSecurityConfig();
  }

  private static ClientConnectionStrategyConfig createConnectionStrategyConfig(CachingConfigProps props) {
    val connectionStrategyConfig = new ClientConnectionStrategyConfig();
    val retryConfig = connectionStrategyConfig.getConnectionRetryConfig();
    retryConfig.setInitialBackoffMillis(props.getConnection().getInitialBackoffMillis());
    retryConfig.setMaxBackoffMillis(props.getConnection().getMaxBackoffMillis());
    retryConfig.setMultiplier(props.getConnection().getMultiplier());
    retryConfig.setClusterConnectTimeoutMillis(props.getConnection().getConnectTimeoutMillis());
    retryConfig.setJitter(props.getConnection().getJitter());

    return connectionStrategyConfig;
  }

  private static String resolveInstanceName(String clusterName, AppProperties appProps) {
    return String.format("stratis-%s-%s", appProps.getInstanceId(), clusterName);
  }
}
