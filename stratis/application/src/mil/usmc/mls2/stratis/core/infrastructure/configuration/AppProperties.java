package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "stratis.app")
public class AppProperties {

  final String name = "stratis-app";
  String environmentId;
  String instanceId;
}
