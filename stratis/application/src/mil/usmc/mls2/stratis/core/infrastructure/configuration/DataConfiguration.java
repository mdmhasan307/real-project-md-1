package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.common.share.AdviceConstants;
import mil.usmc.mls2.stratis.core.infrastructure.transaction.CustomJpaTransactionManager;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * STRATIS Database connection pool configuration for Legacy instances of Stratis
 */
@Slf4j
@Configuration
@EnableTransactionManagement(order = AdviceConstants.ORDER_TRANSACTIONAL)
@EnableJpaRepositories(basePackages = {"mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository"})
@RequiredArgsConstructor
@Profile(Profiles.LEGACY)
@SuppressWarnings("Duplicates")
public class DataConfiguration {

  @Value("${spring.datasource.url}")
  private String url;

  @Value("${stratis.datasource.STRATIS.flyway.username}")
  private String flywayUsername;

  @Value("${stratis.datasource.STRATIS.flyway.password}")
  private String flywayPassword;

  @Value("${stratis.datasource.STRATIS.flyway.locations}")
  private String flywayLocations;

  @Value("${stratis.datasource.STRATIS.flyway.table}")
  private String flywayTable;

  @Value("${stratis.datasource.STRATIS.flyway.enabled}")
  private boolean flywayEnabled;

  @Value("${stratis.datasource.STRATIS.flyway.default_schema}")
  private String flywayDefaultSchema;

  @Value("${spring.datasource.username}")
  private String dbUsername;

  @Value("${spring.datasource.password}")
  private String dbPassword;

  private final JpaProperties jpaProperties;
  private final JdbcProperties jdbcProperties;
  private final DBConnectionConfigurator dbConnectionConfigurator;
  private final ConfigurableApplicationContext ctx;

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws SQLException {
    val adapter = new HibernateJpaVendorAdapter();
    adapter.setShowSql(jpaProperties.isShowSql());
    adapter.setDatabasePlatform(jpaProperties.getDatabasePlatform());

    val factory = new LocalContainerEntityManagerFactoryBean();
    factory.setPersistenceUnitName(GlobalConstants.PERSISTENCE_UNIT);
    factory.setPackagesToScan(GlobalConstants.ROOT_STRATIS_SYSTEM_DB_PACKAGE);
    factory.setDataSource(dataSource());
    factory.setJpaVendorAdapter(adapter);

    FlywaySetupUtils.setupFlyway(flywayEnabled, url, flywayUsername, flywayPassword, flywayLocations, flywayTable, flywayDefaultSchema);

    val properties = new Properties();
    properties.putAll(jpaProperties.getProperties());

    // Custom interceptors
    //properties.put("hibernate.ejb.interceptor", new CustomHibernateInterceptor());

    // L2 cache configuration with cluster-cache client instance
    //properties.put("hibernate.cache.hazelcast.use_native_client", true);
    //properties.put("hibernate.cache.hazelcast.native_client_instance_name", appCacheManager.getInstanceName());

    factory.setJpaProperties(properties);

    return factory;
  }

  @Bean
  public CustomJpaTransactionManager transactionManager(DataSource dataSource) throws SQLException {
    val transactionManager = new CustomJpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
    transactionManager.setDataSource(dataSource);
    return transactionManager;
  }

  @Bean
  @Primary
  public DataSource dataSource() throws SQLException {
    try {
      InitialContext initialCtx = new InitialContext();
      initialCtx.createSubcontext("stratis").createSubcontext("jdbc");
      val jdbcCtx = (Context) initialCtx.lookup("stratis/jdbc");
      dbConnectionConfigurator.configureAdfConnection(url, dbUsername, dbPassword, GlobalConstants.LEGACY_DATABASE_CONNECTION_NAME, jdbcProperties, jdbcCtx);
    }
    catch (Exception e) {
      ctx.close();
      throw new StratisRuntimeException("System Failure. Unable to create ADF connection pool object for site {}", GlobalConstants.LEGACY_DATABASE_CONNECTION_NAME, e);
    }

    return dbConnectionConfigurator.configureDataSource(url, dbUsername, dbPassword, "STRATDBSVRDS", jdbcProperties);
  }
}
