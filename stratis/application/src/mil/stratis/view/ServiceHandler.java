package mil.stratis.view;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.stratis.model.services.StratisRootServiceAMImpl;
import mil.stratis.view.util.ADFUtils;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;

@Slf4j
@NoArgsConstructor
public class ServiceHandler {

  public StratisRootServiceAMImpl getStratisRootService() {
    StratisRootServiceAMImpl service = null;
    try {
      service = (StratisRootServiceAMImpl) ADFUtils.getApplicationModuleForDataControl("StratisRootServiceAMDataControl");
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return service;
  }
}
