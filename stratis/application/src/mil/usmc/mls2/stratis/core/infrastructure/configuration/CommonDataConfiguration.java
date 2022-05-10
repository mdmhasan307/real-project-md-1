package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.share.AdviceConstants;
import mil.usmc.mls2.stratis.core.infrastructure.transaction.CustomJpaTransactionManager;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * STRATCOMN database connection pool configuration for innovation instances of Stratis
 */

@Slf4j
@Configuration
@EnableJpaRepositories(
    entityManagerFactoryRef = "commonEntityManagerFactory",
    transactionManagerRef = GlobalConstants.COMMON_TRANSACTION_MANAGER,
    basePackages = {"mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.infrastructure.persistence",
        "mil.usmc.mls2.stratis.core.infrastructure.persistence.common.repository",
        "mil.usmc.mls2.stratis.integration.mls2.messaging.qry.infrastructure.persistence"})
@EnableTransactionManagement(order = AdviceConstants.ORDER_TRANSACTIONAL)
@RequiredArgsConstructor
@Profile(Profiles.INTEGRATION_ANY)
public class CommonDataConfiguration {

  @Value("${stratis.datasource.baseurl}")
  private String baseUrl;

  @Value("${stratis.datasource.COMMON.pdbname}")
  private String commonPdbName;

  @Value("${stratis.datasource.COMMON.flyway.username}")
  private String flywayUsername;

  @Value("${stratis.datasource.COMMON.flyway.password}")
  private String flywayPassword;

  @Value("${stratis.datasource.COMMON.flyway.locations}")
  private String flywayLocations;

  @Value("${stratis.datasource.COMMON.flyway.table}")
  private String flywayTable;

  @Value("${stratis.datasource.COMMON.flyway.enabled}")
  private boolean flywayEnabled;

  @Value("${stratis.datasource.COMMON.default_schema}")
  private String defaultSchema;

  private final JpaProperties jpaProperties;
  private final JdbcProperties jdbcProperties;
  private final DBConnectionConfigurator dbConnectionConfigurator;
  private final Environment env;

  @Bean("commonEntityManagerFactory")
  LocalContainerEntityManagerFactoryBean commonEntityManagerFactory() throws SQLException {
    val factory = new LocalContainerEntityManagerFactoryBean();
    factory.setPersistenceUnitName(GlobalConstants.COMMON_PERSISTENCE_UNIT);
    factory.setPackagesToScan(GlobalConstants.ROOT_COMMON_DB_PACKAGE,
        "mil.usmc.mls2.stratis.integration.mls2.messaging.cmd.infrastructure.persistence",
        "mil.usmc.mls2.stratis.integration.mls2.messaging.qry.infrastructure.persistence");
    factory.setDataSource(commonDataSource());
    factory.setJpaVendorAdapter(commonJpaVendorAdapter());

    String flywayUrl = baseUrl + commonPdbName;
    log.info("Attempting to connect to Flyway with url:[{}],  username:[{}],  table:[{}],  defaultSchemas:[{}]", flywayUrl, flywayUsername, flywayTable, defaultSchema);

    if (flywayEnabled) {
      Flyway.configure()
          .dataSource(flywayUrl, flywayUsername, flywayPassword)
          .locations(flywayLocations)
          .table(flywayTable)
          .schemas(defaultSchema)
          .validateOnMigrate(false) // This value checks checksums of previously run Flyway scripts, which should not be necessary.
          .load()
          .migrate();
    }

    val properties = new Properties();
    properties.putAll(jpaProperties.getProperties());
    //overriders the default with the common default.
    properties.remove("hibernate.default_schema");
    properties.put("hibernate.default_schema", defaultSchema);

    factory.setJpaProperties(properties);

    return factory;
  }

  @Bean("commonJpaVendorAdapter")
  public HibernateJpaVendorAdapter commonJpaVendorAdapter() {
    val adapter = new HibernateJpaVendorAdapter();
    adapter.setShowSql(jpaProperties.isShowSql());
    adapter.setDatabasePlatform(jpaProperties.getDatabasePlatform());
    return adapter;
  }

  @Bean(GlobalConstants.COMMON_TRANSACTION_MANAGER)
  public CustomJpaTransactionManager commonTransactionManager(@Qualifier("commonEntityManagerFactory") EntityManagerFactory commonEntityManager) {
    return new CustomJpaTransactionManager(commonEntityManager);
  }

  @Bean("commonDataSource")
  public DataSource commonDataSource() throws SQLException {
    String dbConnection = "stratis.datasource.COMMON";

    String datasourceUrl = baseUrl + commonPdbName;
    return dbConnectionConfigurator.configureDataSource(datasourceUrl, env.getProperty(dbConnection + ".username"), env.getProperty(dbConnection + ".password"), "STRATCOMMON", jdbcProperties);
  }
}
