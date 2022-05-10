package mil.stratis.view.session;

import lombok.extern.slf4j.Slf4j;
import mil.stratis.model.services.StratisRootServiceImpl;
import mil.stratis.view.util.ADFUtils;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import oracle.adf.controller.ControllerContext;

import java.io.Serializable;

@Slf4j
public class Navigation implements Serializable {

  public Navigation() {
    super();
  }

  /**
   * works out what the url is to navigate to a view activity in
   * the unbounded flow.
   *
   * @param view the name of the view activity.
   * @return the url
   */
  protected String getCorrectURL(String view) {
    return "/" + ControllerContext.getInstance().getGlobalViewActivityURL(view);
  }

  public StratisRootServiceImpl getStratisRootService() {
    try {
      return (StratisRootServiceImpl) ADFUtils.getApplicationModuleForDataControl("StratisRootServiceDataControl");
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }
}
