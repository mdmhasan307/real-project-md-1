package mil.stratis.view.receiving;

import lombok.NoArgsConstructor;
import mil.stratis.model.services.RcvHomeAMImpl;
import mil.stratis.model.services.StratisRootServiceImpl;
import mil.stratis.view.util.ADFUtils;

@NoArgsConstructor
public class RcvBacking {
  
  private String sForwardAction = "";
  private boolean uiDialog = false;

  public String getForwardAction() {
    return sForwardAction;
  }

  public void clearForwardAction() {
    this.sForwardAction = "";
  }

  public void setForwardAction(String sForwardAction) {
    this.sForwardAction = sForwardAction;
  }

  public void setUiDialog(boolean uiDialog) {
    this.uiDialog = uiDialog;
  }

  public boolean getUiDialog() {
    return uiDialog;
  }

  protected StratisRootServiceImpl getRootService() throws Exception {
    StratisRootServiceImpl service = null;
    try {
      return (StratisRootServiceImpl) ADFUtils.getApplicationModuleForDataControl("StratisRootServiceDataControl");
    }
    catch (Exception e) {
      service = null;
    }
    return service;
  }

  protected RcvHomeAMImpl getRcvService() throws Exception {
    return (RcvHomeAMImpl) getRootService().getRcvHomeAM1();
  }
}
