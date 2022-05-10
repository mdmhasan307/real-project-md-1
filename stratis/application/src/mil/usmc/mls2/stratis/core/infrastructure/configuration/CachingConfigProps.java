package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
@Component("integrationCachingConfigProps")
@ConfigurationProperties(prefix = "stratis.integration.caching")
class CachingConfigProps implements InitializingBean {

  private boolean jmxEnabled;
  private Clusters clusters;
  private List<String> outboundPorts = new ArrayList<>();
  private ConnectionProps connection;

  @Override
  public void afterPropertiesSet() {
    log.info("CachingConfigProperties: {}", this);
    clusters.validate();
  }

  @Data
  static class Clusters {

    private CacheProps app;
    private CacheProps mls2Share;

    public void validate() {
      app.validate();
      mls2Share.validate();
    }
  }

  @Data
  static class CacheProps {

    private String clusterName;
    private List<String> addresses = new ArrayList<>();

    public void validate() {
      if (addresses.isEmpty()) {
        throw new IllegalStateException("at least one address must be defined for each cache configuration.");
      }
    }
  }

  @Data
  static class ConnectionProps {

    private int initialBackoffMillis;
    private int maxBackoffMillis;
    private double multiplier;
    private int connectTimeoutMillis;
    private double jitter;
  }
}