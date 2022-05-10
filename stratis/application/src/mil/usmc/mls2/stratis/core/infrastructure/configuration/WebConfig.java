package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.core.infrastructure.util.UserSiteSelectionTracker;
import mil.usmc.mls2.stratis.core.manager.UserSessionManager;
import mil.usmc.mls2.stratis.core.utility.CertificateUtils;
import net.jawr.web.servlet.JawrServlet;
import oracle.adf.model.servlet.ADFBindingFilter;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.ajp.AbstractAjpProtocol;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletRegistrationBean;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@Slf4j
@Configuration
@EnableAsync
@RequiredArgsConstructor
public class WebConfig {

  private static final String ANY_REQ = "/*";

  @Value("${server.ajp.enabled}")
  private boolean ajpEnabled;

  @Value("${server.ssl.enabled:false}")
  private boolean sslEnabled;

  @Value("${server.ssl.redirect.enabled:false}")
  private boolean sslRedirectEnabled;

  @Value("${server.ajp.port}")
  private int ajpPort;

  @Value("${server.port}")
  private int serverPort;

  @Value("${server.ajp.redirectPort}")
  private int ajpRedirectPort;

  @Value("${server.ajp.protocolHandler.secretRequired:false}")
  private boolean protocolHandlerSecretRequired;

  @Value("${server.ajp.protocolHandler.secret:super-secret}")
  private String protocolHandlerSecret;

  @Value("${stratis.http.request.attribute.certificate}")
  private String requestAttributeCertificateKey;

  @Value("${stratis.http.request.header.certificate}")
  private String requestHeaderCertificateKey;
  
  @Bean
  public TomcatServletWebServerFactory tomcatFactory() {
    val factory = new TomcatServletWebServerFactory() {
      @Override
      protected void postProcessContext(Context context) {
        // disable manifest scanning
        ((StandardJarScanner) context.getJarScanner()).setScanManifest(false);
        if (sslEnabled) {
          val collection = new SecurityCollection();
          collection.addPattern(ANY_REQ);

          val securityConstraint = new SecurityConstraint();
          securityConstraint.setUserConstraint("CONFIDENTIAL");
          securityConstraint.addCollection(collection);
          context.addConstraint(securityConstraint);
        }
      }
    };

    if (ajpEnabled) factory.addAdditionalTomcatConnectors(ajpConnector());
    if (sslEnabled && sslRedirectEnabled) factory.addAdditionalTomcatConnectors(httpConnector());

    return factory;
  }

  /**
   * For INTERNAL view resolution
   */
  @Bean
  public ViewResolver internalResourceViewResolver(WebMvcProperties properties) {
    val resolver = new InternalResourceViewResolver();
    resolver.setViewClass(JstlView.class);
    resolver.setPrefix(properties.getView().getPrefix());
    resolver.setSuffix(properties.getView().getSuffix());
    resolver.setExposedContextBeanNames("appBuildProperties", "appGitProperties");
    return resolver;
  }

  @Bean
  public FilterRegistrationBean<LogContextFilter> logContextFilterRegistration() {
    val registration = new FilterRegistrationBean<>(new LogContextFilter());
    registration.setName("logContextFilter");
    registration.addServletNames(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME);
    registration.setOrder(1);
    return registration;
  }

  @Bean
  public FilterRegistrationBean<CertificateFilter> certificateFilterRegistration(CertificateUtils certificateUtils) {
    val filter = CertificateFilter
        .builder()
        .attributeKey(requestAttributeCertificateKey)
        .headerKey(requestHeaderCertificateKey)
        .certificateUtils(certificateUtils)
        .build();
    val registration = new FilterRegistrationBean<>(filter);
    registration.setName("certificateFilter");
    registration.addUrlPatterns("/*");
    registration.setOrder(2);
    return registration;
  }

  @Bean
  public FilterRegistrationBean<UserSiteSelectionThreadFilter> userSiteSelectionThreadFilterRegistration() {
    val registration = new FilterRegistrationBean<>(new UserSiteSelectionThreadFilter());
    registration.setName("UserSiteSelectionThreadFilter");
    registration.addServletNames(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME, "SlotImage");
    registration.setOrder(3);
    return registration;
  }

  @Bean
  public FilterRegistrationBean<ADFBindingFilter> adfRegistration() {
    val registration = new FilterRegistrationBean<>(new ADFBindingFilter());
    registration.setName("AdfFilter");
    registration.addServletNames(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME);
    registration.setOrder(4);
    return registration;
  }

  @Bean
  public DispatcherServlet dispatcherServlet() {
    return new DispatcherServlet();
  }

  @Bean
  public DispatcherServletRegistrationBean dispatcherServletRegistration() {
    val registration = new DispatcherServletRegistrationBean(dispatcherServlet(), "/app/*");
    registration.setName(DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME);
    registration.setLoadOnStartup(1);
    return registration;
  }

  @Bean
  public ServletRegistrationBean<JawrServlet> javaScriptServletRegistration() {
    val registration = new ServletRegistrationBean<>(new JawrServlet(), "*.js");
    registration.setName("javaScriptServlet");
    registration.setLoadOnStartup(1);
    registration.addInitParameter("configLocation", "/jawr.properties");
    return registration;
  }

  @Bean
  public ServletRegistrationBean<JawrServlet> cssServletRegistration() {
    val registration = new ServletRegistrationBean<>(new JawrServlet(), "*.css");
    registration.setName("cssServlet");
    registration.setLoadOnStartup(1);
    registration.addInitParameter("configLocation", "/jawr.properties");
    registration.addInitParameter("type", "css");
    return registration;
  }

  @Bean                           // bean for http session listener
  @SuppressWarnings("all")
  public HttpSessionListener httpSessionListener() {

    return new HttpSessionListener() {

      private int totalActiveSessions;

      @Override
      public void sessionCreated(HttpSessionEvent se) {               // This method will be called when session created
        totalActiveSessions++;
      }

      @Override
      public void sessionDestroyed(HttpSessionEvent se) {         // This method will be automatically called when session destroyed
        try {
          totalActiveSessions--;
          val session = se.getSession();
          val user = (UserInfo) session.getAttribute("userbean");
          //since the user didn't trigger this, we need to setup the siteSelectionTracker so the remove works.
          String userSiteSelected = (String) session.getAttribute(StratisConfig.USER_DB_SELECTED);
          String siteName = (String) session.getAttribute(StratisConfig.USER_SITE_NAME);
          //this will allow the removeSesionFromUser to find the correct connection pool to run the removal process before
          //the session gets cleared.
          UserSiteSelectionTracker.configureTracker(userSiteSelected, siteName);

          if (user != null) {
            UserSessionManager.removeSessionFromUser(user.getUserId(), session.getId());
            if (user.getUserId() > 0) {
              // if the user's id is negative 1 they have loged out not timed out
              UserSessionManager.logSessionTimeout(user, session.getId());
            }
          }
        }
        catch (Exception e) {
          log.error("Unable to deAssign data from logged in user as session timed out.", e);
        }
      }
    };
  }

  private Connector ajpConnector() {
    val connector = new Connector("AJP/1.3");
    connector.setScheme("http");
    connector.setPort(ajpPort);
    connector.setRedirectPort(ajpRedirectPort);
    connector.setSecure(false);
    connector.setAllowTrace(false);

    // Required for Tomcat v8.5.51+:
    val protocolHandler = ((AbstractAjpProtocol) connector.getProtocolHandler());
    protocolHandler.setSecretRequired(protocolHandlerSecretRequired);
    protocolHandler.setSecret(protocolHandlerSecret);

    return connector;
  }

  private Connector httpConnector() {
    Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
    connector.setScheme("http");
    connector.setPort(8080);
    connector.setRedirectPort(serverPort);
    connector.setSecure(false);
    connector.setAllowTrace(false);
    return connector;
  }
}
