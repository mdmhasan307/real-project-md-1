package mil.stratis.view.shipping;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.stratis.common.util.Util;
import mil.stratis.model.services.ShippingServiceImpl;
import mil.stratis.model.services.StratisRootServiceImpl;
import mil.stratis.view.session.MdssBackingBean;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@NoArgsConstructor
public class ShippingHomeBean extends MdssBackingBean {

  @Override
  public void onPageLoad() {
    try {
      int iWorkstationId = Util.cleanInt(JSFUtils.getManagedBeanValue("userbean.workstationId"));
      HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
      String requestURL = request.getRequestURL().toString();
      if (requestURL.contains("Shipping_Home")) {
        getShippingServiceModule().refreshShippingHomeReports(iWorkstationId);
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  @Override
  public ShippingServiceImpl getShippingServiceModule() {
    ShippingServiceImpl service;
    try {
      service = (ShippingServiceImpl) getStratisRootService().getShippingService1();
    }
    catch (Exception e) {
      service = null;
    }
    return service;
  }

  @Override
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