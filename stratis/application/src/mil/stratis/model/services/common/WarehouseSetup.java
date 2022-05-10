package mil.stratis.model.services.common;

import oracle.jbo.ApplicationModule;

public interface WarehouseSetup extends ApplicationModule {

  void filterNonMechLocationData(String WacID, String LocationLabel);

  void filterNonMechRemovalData(String WacID, String LocationLabel);
}
