package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.config.ConfigurableSiteMeshFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Properties;

/**
 * # Overview
 * Application SiteMesh 3.0.1 configuration (replaces xml configuration)
 * <p>
 * # Notes
 * similar implementation to TCPT's TcptSiteMesh3Filter
 * To exclude ANY page from being decorated without an exclusion pattern, add a <meta name="decorator" content="<path to a decorator, real or imagined>" />
 */
@Slf4j
@RequiredArgsConstructor
public class AppSiteMeshFilter extends ConfigurableSiteMeshFilter {

  private static final String DECORATOR_TEMPLATE_INTERNAL = "/WEB-INF/jsp/sitemesh/template.jsp";

  private final Properties buildProperties;
  private final Properties gitProperties;

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    servletRequest.setAttribute("appBuildProperties", buildProperties);
    servletRequest.setAttribute("appGitProperties", gitProperties);

    val httpServletRequest = (HttpServletRequest) servletRequest;
    if (httpServletRequest.getRequestURI().contains("h2-console")) {
      filterChain.doFilter(servletRequest, servletResponse);
    }
    else {
      super.doFilter(servletRequest, servletResponse, filterChain);
    }
  }

  @Override
  protected void applyCustomConfiguration(SiteMeshFilterBuilder builder) {
    configureMappings(builder);
    configureExclusions(builder);
  }

  private void configureMappings(SiteMeshFilterBuilder builder) {
    // default
    builder.addDecoratorPath("/*", DECORATOR_TEMPLATE_INTERNAL);
  }

  private void configureExclusions(SiteMeshFilterBuilder builder) {
    builder
        .addExcludedPath("*/unsupportedBrowser")
        .addExcludedPath("*/errorPage");
  }
}
