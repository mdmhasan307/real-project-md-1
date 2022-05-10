package mil.usmc.mls2.stratis;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.ApplicationInitializer;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Spring Boot Application Bootstrap class
 * Configured for WAR Initialization
 */
@Slf4j
@SpringBootApplication(exclude = {HazelcastAutoConfiguration.class})
public class Application extends SpringBootServletInitializer {

  /**
   * External Tomcat Container Configuration
   */
  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    setRegisterErrorPageFilter(false);
    return configureApplication(builder);
  }

  /**
   * Executable Configuration
   * NOTE: By design, catching 'Throwable' instead of 'Exception' to provide visibility into startup exceptions that
   * may occur; otherwise bootRun will fail without any exception details (exception is consumed without being logged).
   */
  @SuppressWarnings("squid:S1181")
  public static void main(String... args) {
    try {
      configureApplication(new SpringApplicationBuilder()).run(args);
    }
    catch (RuntimeException re) {
      val filename = re.getStackTrace()[0].getFileName();
      if (filename != null && filename.startsWith("SilentExitExceptionHandler")) {
        log.info("suppressing Spring DevTool's SilentExitExceptionHandler Exceptions...");
      }
      else {
        throw re;
      }
    }
    catch (Throwable t) {
      log.error("FATAL exception in Application", t);
      throw t;
    }
  }

  private static SpringApplicationBuilder configureApplication(SpringApplicationBuilder builder) {
    //The below two SLF4HBridgeHandler calls allows ADFLogger to be redirected to LogBack.
    // Optionally remove existing handlers attached to j.u.l root logger
    SLF4JBridgeHandler.removeHandlersForRootLogger();  // (since SLF4J 1.6.5)

    // add SLF4JBridgeHandler to j.u.l's root logger, should be done once during
    // the initialization phase of your application
    SLF4JBridgeHandler.install();

    return builder
        .bannerMode(Banner.Mode.CONSOLE)
        .sources(Application.class)
        .initializers(new ApplicationInitializer());
  }
}