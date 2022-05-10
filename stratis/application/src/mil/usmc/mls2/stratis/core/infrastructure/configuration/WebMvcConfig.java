package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

@Slf4j
@Configuration
@EnableAsync
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

  @Bean
  public LocaleChangeInterceptor localeChangeInterceptor() {
    log.info("*** localeChangeInterceptor (locale development)");
    LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
    lci.setParamName("lang");
    return lci;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    log.info("*** configuring interceptors...");
    registry.addInterceptor(localeChangeInterceptor());
  }

  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    log.info("*** configuring content negotiation...");
    configurer
      .ignoreAcceptHeader(false)
      .useRegisteredExtensionsOnly(false)
      .defaultContentType(MediaType.APPLICATION_JSON)
      .mediaType("html", MediaType.TEXT_HTML)
      .mediaType("xml", MediaType.APPLICATION_XML)
      .mediaType("json", MediaType.APPLICATION_JSON);
  }

  @Override
  public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
    ThreadPoolTaskExecutor t = new ThreadPoolTaskExecutor();
    t.setThreadNamePrefix("stream-async-");
    t.setCorePoolSize(10);
    t.setMaxPoolSize(20);
    t.setQueueCapacity(Integer.MAX_VALUE);
    t.setAllowCoreThreadTimeOut(true);
    t.setKeepAliveSeconds(300);
    t.setDaemon(true);
    t.initialize();
    configurer.setTaskExecutor(t);
  }
}
