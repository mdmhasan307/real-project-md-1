package mil.stratis.view;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.stratis.model.services.*;
import mil.stratis.view.util.ADFUtils;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;

import javax.faces.context.FacesContext;
import java.io.Serializable;

@Slf4j
@NoArgsConstructor
public class BackingHandler implements Serializable {

  public String getStratisUrlContextPath() {
    return FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
  }

  public StratisRootServiceImpl getStratisRootService() {
    StratisRootServiceImpl service = null;
    try {

      service = (StratisRootServiceImpl) ADFUtils.getApplicationModuleForDataControl("StratisRootServiceDataControl");

      if (service == null) {
        log.error("Unable to obtain a handle to the root service. Sevices is null");
      }
    }
    catch (Exception e) {
      log.error("Unable to obtain a handle to the root service.");
      AdfLogUtility.logException(e);
    }
    return service;
  }

  public ReceiptAMImpl getReceiptAMService() {
    ReceiptAMImpl service = null;
    try {
      service = (ReceiptAMImpl) getStratisRootService().getReceiptAM1();
    }
    catch (Exception e) {
      log.error("Unable to obtain a handle to the service.");
      AdfLogUtility.logException(e);
    }
    return service;
  }

  public WorkLoadManagerImpl getWorkloadManagerService() {
    WorkLoadManagerImpl service = null;
    try {
      service = (WorkLoadManagerImpl) getStratisRootService().getWorkLoadManager1();
    }
    catch (Exception e) {
      log.error("Unable to obtain a handle to the service.");
      AdfLogUtility.logException(e);
    }
    return service;
  }

  /***********************************************************
   * GLOBAL METHODS for module access
   * *********************************************************/
  public WarehouseSetupImpl getWarehouseSetupModule() {
    WarehouseSetupImpl service;
    try {
      service = (WarehouseSetupImpl) getStratisRootService().getWarehouseSetup1();
    }
    catch (Exception e) {
      log.error("Unable to obtain a handle to the service.");
      AdfLogUtility.logException(e);
      service = null;
    }
    return service;
  }

  public AppModuleImpl getAppModule() {
    AppModuleImpl service;
    try {
      service = (AppModuleImpl) getStratisRootService().getAppModule1();
    }
    catch (Exception e) {
      service = null;
      log.error("Unable to obtain a handle to the service.");
      AdfLogUtility.logException(e);
    }
    return service;
  }

  public LoginModuleImpl getLoginModule() {
    LoginModuleImpl service;
    try {
      service = (LoginModuleImpl) getStratisRootService().getLoginModule1();
    }
    catch (Exception e) {
      log.error("Unable to obtain a handle to the service.");
      AdfLogUtility.logException(e);
      service = null;
    }
    return service;
  }

  public SysAdminImpl getSysAdminModule() {
    SysAdminImpl service;
    try {
      service = (SysAdminImpl) getStratisRootService().getSysAdmin1();
    }
    catch (Exception e) {
      log.error("Unable to obtain a handle to the service.");
      AdfLogUtility.logException(e);
      service = null;
    }
    return service;
  }

  public WorkLoadManagerImpl getWorkloadManagerModule() {
    return getWorkloadManagerService();
  }

  public InventorySetupModuleImpl getInventorySetupService() {
    InventorySetupModuleImpl service;
    try {
      service = (InventorySetupModuleImpl) getStratisRootService().getInventorySetupModule1();
    }
    catch (Exception e) {
      log.error("Unable to obtain a handle to the service.");
      AdfLogUtility.logException(e);
      service = null;
    }
    return service;
  }

  public InventorySetupModuleImpl getInventorySetupModuleService() {
    return getInventorySetupService();
  }

  public ImportFilesImpl getImportFilesService() {
    ImportFilesImpl service = null;
    try {
      service = (ImportFilesImpl) getStratisRootService().getImportFiles1();
    }
    catch (Exception e) {
      log.error("Unable to obtain a handle to the service.");
      AdfLogUtility.logException(e);
    }
    return service;
  }

  public PackingModuleImpl getPackingModule() {
    PackingModuleImpl service;
    try {
      service = (PackingModuleImpl) getStratisRootService().getPackingModule1();
    }
    catch (Exception e) {
      log.error("Unable to obtain a handle to the service.");
      AdfLogUtility.logException(e);
      service = null;
    }
    return service;
  }

  public WalkThruAMImpl getWalkThruAMService() {
    WalkThruAMImpl service;
    try {
      service = (WalkThruAMImpl) getStratisRootService().getWalkThruAM1();
    }
    catch (Exception e) {
      log.error("Unable to obtain a handle to the service.");
      AdfLogUtility.logException(e);
      service = null;
    }
    return service;
  }

  public StowingAMImpl getStowingAMService() {
    StowingAMImpl service = null;
    try {
      service = (StowingAMImpl) getStratisRootService().getStowingAM1();
    }
    catch (Exception e) {
      log.error("Unable to obtain a handle to the service.");
      AdfLogUtility.logException(e);
    }
    return service;
  }

  public InventoryModuleImpl getInventoryAMService() {
    InventoryModuleImpl service = null;
    try {
      service = (InventoryModuleImpl) getStratisRootService().getInventoryModule1();
    }
    catch (Exception e) {
      log.error("Unable to obtain a handle to the service.");
      AdfLogUtility.logException(e);
    }
    return service;
  }

  public PickingAMImpl getPickingAMService() {
    PickingAMImpl service = null;
    try {
      service = (PickingAMImpl) getStratisRootService().getPickingAM1();
    }
    catch (Exception e) {
      log.error("Unable to obtain a handle to the service.");
      AdfLogUtility.logException(e);
    }
    return service;
  }

  public GCSSMCTransactionsImpl getGCSSMCAMService() {
    GCSSMCTransactionsImpl service = null;
    try {
      service = (GCSSMCTransactionsImpl) getStratisRootService().getGCSSMCTransactions1();
    }
    catch (Exception e) {
      log.error("Unable to obtain a handle to the service.");
      AdfLogUtility.logException(e);
    }
    return service;
  }

  public GCSSMCTransactionsImpl getGCSSMCTransactionsService() {
    return getGCSSMCAMService();
  }

  public TransactionsImpl getTransactionsService() {
    try {
      return (TransactionsImpl) getStratisRootService().getTransactions1();
    }
    catch (Exception e) {
      log.error("Unable to obtain a handle to the service.");
      AdfLogUtility.logException(e);
    }
    return null;
  }

  /***********************************************************
   * GLOBAL METHODS for module access
   * *********************************************************/
  public ShippingServiceImpl getShippingServiceModule() {
    ShippingServiceImpl service;
    try {
      service = (ShippingServiceImpl) getStratisRootService().getShippingService1();
    }
    catch (Exception e) {
      log.error("Unable to obtain a handle to the service.");
      AdfLogUtility.logException(e);
      service = null;
    }
    return service;
  }

  /**
   * Determine whether the current page request represents a postback.
   *
   * @return true if current page request represents a postback
   */
  protected boolean isPostback() {
    return ADFUtils.isPostback();
  }
}
