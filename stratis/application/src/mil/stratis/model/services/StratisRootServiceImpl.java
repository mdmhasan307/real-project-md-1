package mil.stratis.model.services;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ApplicationModuleImpl;

@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
public class StratisRootServiceImpl extends StratisApplicationModuleImpl {
  
  /**
   * Container's getter for Inventory_SetupModule1
   */
  public ApplicationModuleImpl getInventorySetupModule1() {
    return (ApplicationModuleImpl) findApplicationModule("InventorySetupModule1");
  }

  /**
   * Container's getter for WarehouseSetup1
   */
  public ApplicationModuleImpl getWarehouseSetup1() {
    return (ApplicationModuleImpl) findApplicationModule("WarehouseSetup1");
  }

  /**
   * Container's getter for AppModule1.
   *
   * @return AppModule1
   */
  public ApplicationModuleImpl getAppModule1() {
    return (ApplicationModuleImpl) findApplicationModule("AppModule1");
  }

  /**
   * Container's getter for GCSSMCTransactions1.
   *
   * @return GCSSMCTransactions1
   */
  public ApplicationModuleImpl getGCSSMCTransactions1() {
    return (ApplicationModuleImpl) findApplicationModule("GCSSMCTransactions1");
  }

  /**
   * Container's getter for ImportFiles1.
   *
   * @return ImportFiles1
   */
  public ApplicationModuleImpl getImportFiles1() {
    return (ApplicationModuleImpl) findApplicationModule("ImportFiles1");
  }

  /**
   * Container's getter for InventoryModule1.
   *
   * @return InventoryModule1
   */
  public ApplicationModuleImpl getInventoryModule1() {
    return (ApplicationModuleImpl) findApplicationModule("InventoryModule1");
  }

  /**
   * Container's getter for PackingModule1.
   *
   * @return PackingModule1
   */
  public ApplicationModuleImpl getPackingModule1() {
    return (ApplicationModuleImpl) findApplicationModule("PackingModule1");
  }

  /**
   * Container's getter for PickingAM1.
   *
   * @return PickingAM1
   */
  public ApplicationModuleImpl getPickingAM1() {
    return (ApplicationModuleImpl) findApplicationModule("PickingAM1");
  }

  public ApplicationModuleImpl getWalkThruAM1() {
    return (ApplicationModuleImpl) findApplicationModule("WalkThruAM1");
  }

  /**
   * Container's getter for RcvHomeAM1.
   *
   * @return RcvHomeAM1
   */
  public ApplicationModuleImpl getRcvHomeAM1() {
    return (ApplicationModuleImpl) findApplicationModule("RcvHomeAM1");
  }

  /**
   * Container's getter for ReceiptAM1.
   *
   * @return ReceiptAM1
   */
  public ApplicationModuleImpl getReceiptAM1() {
    return (ApplicationModuleImpl) findApplicationModule("ReceiptAM1");
  }

  /**
   * Container's getter for ShippingService1.
   *
   * @return ShippingService1
   */
  public ApplicationModuleImpl getShippingService1() {
    return (ApplicationModuleImpl) findApplicationModule("ShippingService1");
  }

  /**
   * Container's getter for StowingAM1.
   *
   * @return StowingAM1
   */
  public ApplicationModuleImpl getStowingAM1() {
    return (ApplicationModuleImpl) findApplicationModule("StowingAM1");
  }

  /**
   * Container's getter for SysAdmin1.
   *
   * @return SysAdmin1
   */
  public ApplicationModuleImpl getSysAdmin1() {
    return (ApplicationModuleImpl) findApplicationModule("SysAdmin1");
  }

  /**
   * Container's getter for Transactions1.
   *
   * @return Transactions1
   */
  public ApplicationModuleImpl getTransactions1() {
    return (ApplicationModuleImpl) findApplicationModule("Transactions1");
  }

  /**
   * Container's getter for WorkLoadManager1.
   *
   * @return WorkLoadManager1
   */
  public ApplicationModuleImpl getWorkLoadManager1() {
    return (ApplicationModuleImpl) findApplicationModule("WorkLoadManager1");
  }

  /**
   * Container's getter for LoginModule1.
   *
   * @return LoginModule1
   */
  public ApplicationModuleImpl getLoginModule1() {
    return (ApplicationModuleImpl) findApplicationModule("LoginModule1");
  }
}
