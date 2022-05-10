package mil.usmc.mls2.stratis.common.share;

import mil.usmc.mls2.stratis.common.domain.model.IntegrationMode;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IntegrationModeRestriction {

  IntegrationMode allow();

  String message() default "";
}
