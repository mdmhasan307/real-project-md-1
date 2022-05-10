package mil.usmc.mls2.stratis.core.utility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.model.services.*;
import oracle.adf.model.BindingContext;
import oracle.adf.model.bc4j.DCJboDataControl;
import oracle.adf.model.binding.DCDataControl;
import oracle.jbo.ApplicationModule;
import org.springframework.stereotype.Component;

/**
 * Transitional class used to support cases where static access to context elements are needed
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpringAdfBindingUtils {

  public static LoginModuleImpl getLoginModuleService() {
    return (LoginModuleImpl) getSTRATISApplicationModule("LoginModuleDataControl");
  }

  public static InventorySetupModuleImpl getInventorySetupService() {
    return (InventorySetupModuleImpl) getSTRATISApplicationModule("InventorySetupModuleDataControl");
  }

  public static WorkLoadManagerImpl getWorkloadManagerService() {
    return (WorkLoadManagerImpl) getSTRATISApplicationModule("WorkLoadManagerDataControl");
  }

  public static ShippingServiceImpl getShippingService() {
    return (ShippingServiceImpl) getSTRATISApplicationModule("ShippingServiceDataControl");
  }

  public static GCSSMCTransactionsImpl getGCSSMCTransactionsService() {
    return getWorkloadManagerService().getGCSSMCTransactionsService();
  }

  public static ReceiptAMImpl getReceiptAM() {
    StratisRootServiceImpl rootService = (StratisRootServiceImpl) getSTRATISApplicationModule("StratisRootServiceDataControl");
    return (ReceiptAMImpl) rootService.getReceiptAM1();
  }

  private static ApplicationModule getSTRATISApplicationModule(String dataControlName) {
    val binder = BindingContext.getCurrent();
    DCDataControl dc = binder.findDataControl(dataControlName);
    /*
     * Test to see if our data control is an instance of the ADF Business Components
     * Data Control before trying to cast the data provider to a specific
     * service interface
     */
    if (dc instanceof DCJboDataControl) {
      return (ApplicationModule) dc.getDataProvider();
    }
    return null;
  }
}
