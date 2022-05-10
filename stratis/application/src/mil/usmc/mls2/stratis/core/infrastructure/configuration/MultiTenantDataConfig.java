package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.share.AdviceConstants;
import mil.usmc.mls2.stratis.core.domain.model.Mls2Sites;
import mil.usmc.mls2.stratis.core.domain.repository.common.Mls2SitesRepository;
import mil.usmc.mls2.stratis.core.infrastructure.transaction.CustomJpaTransactionManager;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.*;

/**
 * STRATIS database connection pool configuration for innovation instances of Stratis
 */
@Slf4j
@Configuration
@EnableJpaRepositories(basePackages = {"mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.repository"})
@EnableTransactionManagement(order = AdviceConstants.ORDER_TRANSACTIONAL)
@RequiredArgsConstructor
@ComponentScan("mil.usmc.mls2.stratis.core.infrastructure.configuration")
@Profile(Profiles.INTEGRATION_ANY)
@SuppressWarnings("Duplicates")
public class MultiTenantDataConfig {

  @Value("${stratis.datasource.baseurl}")
  private String baseUrl;

  private final JdbcProperties jdbcProperties;
  private final JpaProperties jpaProperties;
  private final DBConnectionConfigurator dbConnectionConfigurator;
  private final StratisConfig stratisConfig;

  @Primary
  @Bean
  @DependsOn({"commonDataSource", "dataSource"})
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource datasource) {
    val adapter = new HibernateJpaVendorAdapter();
    adapter.setShowSql(jpaProperties.isShowSql());
    adapter.setDatabasePlatform(jpaProperties.getDatabasePlatform());

    val factory = new LocalContainerEntityManagerFactoryBean();
    factory.setPersistenceUnitName(GlobalConstants.PERSISTENCE_UNIT);
    factory.setPackagesToScan(GlobalConstants.ROOT_STRATIS_SYSTEM_DB_PACKAGE);
    factory.setDataSource(datasource);
    factory.setJpaVendorAdapter(adapter);

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
  @DependsOn("commonDataSource")
  @Primary
  public CustomJpaTransactionManager transactionManager(@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
    return new CustomJpaTransactionManager(entityManagerFactory);
  }

  @Bean
  @DependsOn({"commonDataSource"})
  @Primary
  public DataSource dataSource(Mls2SitesRepository mls2SitesRepository, ConfigurableApplicationContext ctx) {

    val activeSites = mls2SitesRepository.getActiveSites();

    TenantAwareRoutingSource dataSource = new TenantAwareRoutingSource();
    Map<Object, Object> targetDataSources = new HashMap<>();

    List<String> datasourcesLoaded = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(activeSites)) {
      Context jdbcCtx;
      try {
        InitialContext initialCtx = new InitialContext();
        initialCtx.createSubcontext("stratis").createSubcontext("jdbc");
        jdbcCtx = (Context) initialCtx.lookup("stratis/jdbc");

        for (Mls2Sites site : activeSites) {
          if (site.isValidSite()) {
            targetDataSources.put(site.getSiteName(), dbConnectionConfigurator.configureDataSource(site, baseUrl, jdbcProperties));
            dbConnectionConfigurator.configureFlywayForPool(site, baseUrl);
            dbConnectionConfigurator.configureAdfConnection(site, baseUrl, jdbcProperties, jdbcCtx);
            //add site name to list of active and configured sites for multi tenancy innovation setup.
            datasourcesLoaded.add(site.getSiteName());
            log.info("Stratis site {} configured successfully", site.getSiteName());
          }
        }
      }
      catch (Exception e) {
        ctx.close();
        throw new IllegalStateException("System Failure. Unable to create connection pools", e);
      }
    }

    stratisConfig.setDatasources(datasourcesLoaded);
    dataSource.setTargetDataSources(targetDataSources);

    dataSource.afterPropertiesSet();

    return dataSource;
  }
}
