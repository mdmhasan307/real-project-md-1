package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.Getter;
import lombok.val;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

@Getter
@ApplicationScoped
@ManagedBean(name = "appConfig") //keep eager false
public class JsfAppConfiguration {

  private final boolean smvEnabled;

  public JsfAppConfiguration() {
    val globalConstants = ContextUtils.getBean(GlobalConstants.class);
    smvEnabled = globalConstants.isSmvEnabled();
  }
}
