package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.share.caching.LocalHazelcastCacheManager;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Local (non-distributed, non-clustered) caching configuration
 * <p>
 * Environments: Legacy and Innovation (Integration)
 * <p>
 * Implementation
 * - Hazelcast implementation
 * - Local Hazelcast server is used to provide non-distributed caching for the sake of simplicity in the tech stack (in exchange for any performance impact)
 * - Access is restricted via network configuration (though port registration is required for operation)
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class LocalCachingConfig {

  private final CachingConfigProperties props;

  @Bean
  public LocalHazelcastCacheManager localHazelcastCacheManager() {
    return new LocalHazelcastCacheManager(localHazelcastInstance());
  }

  @Bean(destroyMethod = "shutdown")
  public HazelcastInstance localHazelcastInstance() {
    val config = new Config();
    config.setClusterName(props.getClusterName());
    config.setInstanceName(props.getInstanceName());
    config.setManagementCenterConfig(createManagementCenterConfig());
    config.setMapConfigs(createMapConfigs());
    config.setNetworkConfig(createNetworkConfig());
    config.setSecurityConfig(createSecurityConfig());

    config.setProperty("hazelcast.jmx", String.valueOf(props.isJmxEnabled()));
    config.setProperty("hazelcast.initial.min.cluster.size", "1");  // blocks until it has 1 members (self)

    return Hazelcast.newHazelcastInstance(config);
  }

  private ManagementCenterConfig createManagementCenterConfig() {
    val managementConfig = new ManagementCenterConfig();
    if (props.getManagement().isEnabled()) {
      if (StringUtils.isNotBlank(props.getManagement().getUrl())) {
        managementConfig.addTrustedInterface(props.getManagement().getUrl());
        managementConfig.addTrustedInterface(GlobalConstants.LOCALHOST); // <-- LOCAL MANAGEMENT (url above includes ip that will not be used by hzmc spawns)
      }
    }

    return managementConfig;
  }

  private Map<String, MapConfig> createMapConfigs() {
    val defEvictionConfig = new EvictionConfig();
    defEvictionConfig.setEvictionPolicy(EvictionPolicy.LRU);
    defEvictionConfig.setMaxSizePolicy(MaxSizePolicy.PER_NODE);

    val map = new HashMap<String, MapConfig>();

    // default
    val defaultConfig = new MapConfig();
    defaultConfig.setName("default");
    defaultConfig.setEvictionConfig(defEvictionConfig);
    defaultConfig.setTimeToLiveSeconds(-1);
    map.put(defaultConfig.getName(), defaultConfig);

    return map;
  }

  /**
   * Simplified network config (required to prevent multi-cast auto-registration by Hazelcast)
   * <p>
   * Implementation: currently forced to specify port (else 5701 is registered)
   */
  private NetworkConfig createNetworkConfig() {
    val joinConfig = new JoinConfig();
    joinConfig.getAwsConfig().setEnabled(false);
    joinConfig.getMulticastConfig().setEnabled(false);
    joinConfig.getTcpIpConfig().setEnabled(false);

    val networkConfig = new NetworkConfig();
    networkConfig.setPort(props.getPort());
    networkConfig.setPortAutoIncrement(false);
    networkConfig.setPortCount(1);
    networkConfig.setInterfaces(createInterfacesConfig());
    networkConfig.setJoin(joinConfig);
    networkConfig.setReuseAddress(true);

    val socketConfig = new SocketInterceptorConfig();
    socketConfig.setEnabled(false);
    networkConfig.setSocketInterceptorConfig(socketConfig);

    return networkConfig;
  }

  private SecurityConfig createSecurityConfig() {
    val securityConfig = new SecurityConfig();
    securityConfig.setEnabled(false);
    return securityConfig;
  }

  private InterfacesConfig createInterfacesConfig() {
    val interfacesConfig = new InterfacesConfig();
    interfacesConfig.addInterface(GlobalConstants.LOCALHOST);
    return interfacesConfig;
  }

  @Data
  @Component("localCachingConfigProperties")
  @ConfigurationProperties(prefix = "stratis.local.caching")
  private static class CachingConfigProperties {

    private boolean jmxEnabled;
    private String instanceName;
    private Management management;
    private Integer port; // self

    /**
     * clusterName is based upon the instance name to ensure uniqueness of local caches in the event they are accidentally discovered (see Hazelcast issues)
     */
    public String getClusterName() { return String.format("%s-local-no-cluster", getInstanceName());}

    @Data
    static class Management {

      private boolean enabled;
      private String url;
    }
  }
}
