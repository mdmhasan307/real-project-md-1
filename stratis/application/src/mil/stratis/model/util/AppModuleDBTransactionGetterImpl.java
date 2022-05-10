package mil.stratis.model.util;

import oracle.jbo.ApplicationModule;
import oracle.jbo.server.ApplicationModuleImpl;
import oracle.jbo.server.DBTransaction;

public class AppModuleDBTransactionGetterImpl implements DBTransactionGetter {

    public AppModuleDBTransactionGetterImpl(ApplicationModuleImpl appModule) {
        this.appModule = appModule;
    }
    private ApplicationModuleImpl appModule;

    @Override
    public DBTransaction getDBTransaction() {
        return appModule.getDBTransaction();
    }
}
