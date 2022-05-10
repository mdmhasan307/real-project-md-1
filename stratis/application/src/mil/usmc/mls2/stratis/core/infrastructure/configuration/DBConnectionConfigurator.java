package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.crypto.CustomEncryptor;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.domain.model.Mls2Sites;
import oracle.jdbc.driver.OracleConnection;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import oracle.ucp.jdbc.PoolDataSourceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.naming.Context;
import javax.sql.DataSource;
import java.sql.SQLException;

@Component
@Slf4j
public class DBConnectionConfigurator {

  @Value("${stratis.datasource.STRATIS.flyway.locations}")
  private String flywayLocations;

  @Value("${stratis.datasource.STRATIS.flyway.table}")
  private String flywayTable;

  @Value("${stratis.datasource.STRATIS.flyway.enabled}")
  private boolean flywayEnabled;

  public DataSource configureDataSource(Mls2Sites site, String baseUrl, JdbcProperties jdbcProperties) throws SQLException {
    String dbUrl = baseUrl + site.getPdbName();
    val password = site.getPdbPassword();
    return configureDataSource(dbUrl, site.getPdbUsername(), password, site.getSiteName(), jdbcProperties);
  }

  public DataSource configureDataSource(String dbUrl, String username, String password, String siteName, JdbcProperties jdbcProperties) throws SQLException {
    val props = jdbcProperties;
    val source = PoolDataSourceFactory.getPoolDataSource();
    source.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
    source.setDescription("stratisConnectionPool");
    source.setUser(username);
    source.setPassword(decryptPassword(password));
    source.setURL(dbUrl);
    source.setConnectionPoolName(siteName + "_POOL");
    source.setInitialPoolSize(props.getInitialPoolSize());
    source.setMinPoolSize(props.getMinPoolSize());
    source.setMaxPoolSize(props.getMaxPoolSize());
    source.setMaxConnectionReuseTime(props.getMaxConnectionReuseTime());
    source.setConnectionWaitTimeout(props.getConnectionWaitTimeout());
    source.setInactiveConnectionTimeout(props.getInactiveConnectionTimeout());
    source.setTimeoutCheckInterval(props.getTimeoutCheckInterval());
    source.setAbandonedConnectionTimeout(props.getAbandonedConnectionTimeout());
    source.setTimeToLiveConnectionTimeout(props.getTimeToLiveConnectionTimeout());
    source.setValidateConnectionOnBorrow(props.isValidateConnectionOnBorrow());
    source.setConnectionProperty(OracleConnection.CONNECTION_PROPERTY_AUTOCOMMIT, String.valueOf(false));

    // configure connection initialization (type maps, default schema, etc.)
    // NOOP source.registerConnectionInitializationCallback(...)

    if (props.isValidateConnectionOnBorrow()) {
      source.setSecondsToTrustIdleConnection(props.getSecondsToTrustIdleConnection());
    }

    if (props.getMaxStatements() != null) {
      source.setMaxStatements(props.getMaxStatements());
    }

    if (props.getImplicitStatementCacheSize() != null) {
      source.setConnectionProperty(OracleConnection.CONNECTION_PROPERTY_IMPLICIT_STATEMENT_CACHE_SIZE, String.valueOf(props.getImplicitStatementCacheSize()));
    }

    return source;
  }

  public void configureFlywayForPool(Mls2Sites site, String baseUrl) {
    String flywayUrl = baseUrl + site.getPdbName();
    val flywayPassword = site.getFlywayPassword();

    FlywaySetupUtils.setupFlyway(flywayEnabled, flywayUrl, site.getFlywayUsername(), decryptPassword(flywayPassword), flywayLocations, flywayTable, site.getFlywaySchema());
  }

  public String decryptPassword(String password) {
    if (password != null && password.startsWith("ENC(")) {
      // Return decoded value
      return CustomEncryptor.decrypt(password.substring(4, password.length() - 1));
    }
    return password;
  }

  public void configureAdfConnection(String url, String username, String password, String siteName, JdbcProperties jdbcProperties, Context ctx) {
    try {
      val props = jdbcProperties;
      //FUTURE post innov 1.0 for now this is not being done.
      //These connections are not fully functional with ADF due to the location they are being placed in context.
      // more research is required.
      if (true) return;
      PoolDataSourceImpl pds = new PoolDataSourceImpl();

      pds.setURL(url);

      pds.setSQLForValidateConnection("select 1 from DUAL");
      pds.setUser(username);
      pds.setPassword(decryptPassword(password));
      pds.setConnectionPoolName(siteName + "Pool");
      pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
      pds.setMinPoolSize(props.getMinPoolSize());
      pds.setMaxPoolSize(props.getMaxPoolSize());
      pds.setInactiveConnectionTimeout(props.getInactiveConnectionTimeout());
      pds.setInitialPoolSize(props.getInitialPoolSize());
      pds.setMaxConnectionReuseTime(props.getMaxConnectionReuseTime());
      pds.setValidateConnectionOnBorrow(props.isValidateConnectionOnBorrow());
      pds.setLoginTimeout(props.getLoginTimeout());
      pds.setMaxStatements(10); //hard coded for now. ADF used 10.  Spring uses different caching for statements.

      ctx.bind(siteName, pds);
    }
    catch (Exception e) {
      throw new StratisRuntimeException("System Failure. Unable to create ADF connection pool object for site {}", siteName, e);
    }
  }

  public void configureAdfConnection(Mls2Sites site, String baseUrl, JdbcProperties jdbcProperties, Context ctx) {
    configureAdfConnection(baseUrl + site.getPdbName(), site.getPdbUsername(), site.getPdbPassword(), site.getSiteName(), jdbcProperties, ctx);
  }
}
