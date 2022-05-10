package mil.usmc.mls2.stratis.core.infrastructure.aspect;

import lombok.*;
import lombok.extern.slf4j.*;
import mil.usmc.mls2.stratis.common.domain.model.IntegrationMode;
import mil.usmc.mls2.stratis.common.share.AdviceConstants;
import mil.usmc.mls2.stratis.common.share.IntegrationModeRestriction;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Enforces restriction of the execution of services based upon the current integration mode.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@Order(AdviceConstants.ORDER_INTEGRATION_CHECK)
class IntegrationModeRestrictionAspect {

  private static final String TACTICAL_MODE_ONLY = "This service is only supported in Tactical Mode";
  private static final String ENTERPRISE_MODE_ONLY = "This service is only supported in Enterprise Mode";

  private final Environment environment;

  @Before(value = "@annotation(integrationModeRestriction) || @within(integrationModeRestriction)", argNames = "integrationModeRestriction")
  private void doBefore(IntegrationModeRestriction integrationModeRestriction) { // (JoinPoint joinPoint)
    log.trace("INSIDE IntegrationModeRestrictionAspect.before");
    check(integrationModeRestriction);
  }

  private void check(IntegrationModeRestriction params) {
    if (IntegrationMode.ENTERPRISE == params.allow() && Profiles.isNotEnterpriseMode(environment)) {
      throw new IllegalStateException(StringUtils.isNotBlank(params.message()) ? params.message() : ENTERPRISE_MODE_ONLY);
    }
    if (IntegrationMode.TACTICAL == params.allow() && Profiles.isNotTacticalMode(environment)) {
      throw new IllegalStateException(StringUtils.isNotBlank(params.message()) ? params.message() : TACTICAL_MODE_ONLY);
    }
  }
}
