package mil.usmc.mls2.stratis.core.utility;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AmsCmosUtils {

  private final Environment env;
  private final GlobalConstants globalConstants;

  public String getAmsCmosPath() {
    val innovation = Profiles.isIntegrationAny(env);
    if (innovation) return globalConstants.getInnovationAmsCmosPath();
    return globalConstants.getLegacyAmsCmosPath();
  }
}
