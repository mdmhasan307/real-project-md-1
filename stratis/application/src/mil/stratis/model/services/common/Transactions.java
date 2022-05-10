package mil.stratis.model.services.common;

import oracle.jbo.ApplicationModule;

public interface Transactions extends ApplicationModule {

  int sendAsxTrans(String scnVal, int transNum);
}
