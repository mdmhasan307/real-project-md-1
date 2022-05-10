package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.crypto.CryptoPassUtils;
import mil.usmc.mls2.stratis.ApplicationPackage;
import mil.usmc.mls2.stratis.common.domain.exception.StratisRuntimeException;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.properties.EncryptableProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

/**
 * @ EnableCaching enables Spring Caching (not JPA caching) and processes Spring or JSR-107 cache annotations
 * @ ConfigurationPropertiesScan
 *     Eliminates the need to register property classes manually via @EnableConfigurationProperties
 */
@Slf4j
@Configuration
@EnableCaching
@ConfigurationPropertiesScan(basePackageClasses = ApplicationPackage.class)
@RequiredArgsConstructor
public class ApplicationConfig {

  @Primary
  @Bean(GlobalConstants.TASK_EXECUTOR)
  public ThreadPoolTaskExecutor taskExecutor() {
    val executor = new ThreadPoolTaskExecutor();
    executor.setThreadNamePrefix("app-task-");
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(20);
    executor.setQueueCapacity(1000);
    executor.setRejectedExecutionHandler((r, e) -> log.warn("app task rejected - queue capacity exceeded."));
    executor.setWaitForTasksToCompleteOnShutdown(false);
    executor.setDaemon(true);
    executor.initialize();

    return executor;
  }

  @Bean
  public AsyncTaskExecutor asyncTaskExecutor() {
    val executor = new ThreadPoolTaskExecutor();
    executor.setThreadNamePrefix("app-async-task-");
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(50);
    executor.setQueueCapacity(1000);
    executor.setDaemon(true);
    executor.initialize();

    return executor;
  }

  //  @Bean
  //  public MailTaskExecutor mailTaskExecutor() {
  //    val executor = new MailTaskExecutor();
  //    executor.setThreadNamePrefix("mail-task-");
  //    executor.setCorePoolSize(3);
  //    executor.setMaxPoolSize(3);
  //    executor.setQueueCapacity(500);
  //    executor.setRejectedExecutionHandler((r, e) -> log.warn("mail task rejected - queue capacity exceeded."));
  //    executor.setWaitForTasksToCompleteOnShutdown(false);
  //    executor.setDaemon(true);
  //    executor.initialize();
  //
  //    return executor;
  //  }

  @Bean
  public LocaleResolver localeResolver() {
    log.info("registering local resolver...");
    SessionLocaleResolver slr = new SessionLocaleResolver();
    slr.setDefaultLocale(Locale.ENGLISH);
    return slr;
  }

  @Bean
  public ResourceBundleMessageSource messageSource() {
    log.info("registering message source...");
    val source = new ResourceBundleMessageSource();
    source.setBasename("messages");
    return source;
  }

  @Bean("jasyptStringEncryptor")
  public StringEncryptor stringEncryptor() {
    log.info("registering string encryptor...");
    val config = new SimpleStringPBEConfig();
    config.setPassword(CryptoPassUtils.getEncryptionPassword());
    config.setAlgorithm("PBEWithMD5AndDES");
    config.setPoolSize(3);

    val encryptor = new PooledPBEStringEncryptor();
    encryptor.setConfig(config);
    return encryptor;
  }

  /**
   * Named appBuildProperties (vs buildProperties) to resolve conflict with Spring Actuator / Info "buildProperties" object
   */
  @Bean
  public Properties appBuildProperties(ResourceLoader resourceLoader) {
    log.info("registering build properties...");
    val properties = new EncryptableProperties(stringEncryptor());

    try (val is = resourceLoader.getResource("classpath:/build.properties").getInputStream()) {
      properties.load(is);
    }
    catch (IOException ie) {
      log.error("failed to load build.properties", ie);
      throw new StratisRuntimeException("FAILED to load build.properties", ie);
    }

    return properties;
  }

  /**
   * Named appGitProperties (vs gitProperties) to resolve conflict with Spring Actuator / Info "gitProperties" object
   */
  @Bean
  public Properties appGitProperties(ResourceLoader resourceLoader) {
    log.info("registering git properties...");
    val properties = new EncryptableProperties(stringEncryptor());

    try (val is = resourceLoader.getResource("classpath:/git.properties").getInputStream()) {
      properties.load(is);
    }
    catch (IOException ie) {
      log.error("failed to load git.properties", ie);
      throw new StratisRuntimeException("FAILED to load git.properties", ie);
    }

    return properties;
  }

  /**
   * Provides configuration of default property sources placeholder settings.
   * Actual property files and/or locations need not be specified here (will be auto-detected)
   * <p>
   * Note: method is static to avoid Spring warning about limited wiring in surrounding @Configuration class
   */
  @Bean
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    val config = new PropertySourcesPlaceholderConfigurer();
    config.setIgnoreResourceNotFound(true);
    config.setIgnoreUnresolvablePlaceholders(false);
    config.setLocalOverride(false);

    log.info("registering propertySourcesPlaceholderConfigurer...");
    return config;
  }

  @Bean
  public LocalValidatorFactoryBean validator() {
    log.info("registering validator...");
    return new LocalValidatorFactoryBean();
  }

  @Bean
  public MethodValidationPostProcessor methodValidationPostProcessor() {
    log.info("registering method validation post processor...");
    val processor = new MethodValidationPostProcessor();
    processor.setValidator(validator());
    return processor;
  }
}
