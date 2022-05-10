package mil.stratis.model.util;

import oracle.jbo.*;
import oracle.jbo.server.*;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Properties;

public class TestDBTransactionGetterImpl implements DBTransactionGetter {

    private DBTransaction transaction;

    public TestDBTransactionGetterImpl(DBTransaction transaction) {
        this.transaction = transaction;
    }

    public DBTransaction getDBTransaction() {
        return transaction;
    }
}
