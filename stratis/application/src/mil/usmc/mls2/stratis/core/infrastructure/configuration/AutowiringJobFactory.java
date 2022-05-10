package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.val;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.stereotype.Component;

/**
 * Custom Job Scheduler with auto-wiring support for job instances
 * <p>
 * Note: extends Spring's Quartz-based SpringBeanJobFactory
 */
@Component
@SuppressWarnings("all")
public class AutowiringJobFactory extends SpringBeanJobFactory {

  @Autowired
  private AutowireCapableBeanFactory beanFactory;

  @Override
  public Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
    val job = super.createJobInstance(bundle);
    beanFactory.autowireBean(job);
    return job;
  }
}
