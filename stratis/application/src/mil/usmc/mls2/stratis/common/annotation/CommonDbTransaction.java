package mil.usmc.mls2.stratis.common.annotation;

import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Transactional(GlobalConstants.COMMON_TRANSACTION_MANAGER)
public @interface CommonDbTransaction {

  @AliasFor(annotation = Transactional.class)
  Propagation propagation() default Propagation.REQUIRED;

  @AliasFor(annotation = Transactional.class)
  boolean readOnly() default false;
}
