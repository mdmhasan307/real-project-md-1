package mil.stratis.model.util;

import oracle.jbo.server.DBTransaction;

public interface DBTransactionGetter {
    DBTransaction getDBTransaction();
}
