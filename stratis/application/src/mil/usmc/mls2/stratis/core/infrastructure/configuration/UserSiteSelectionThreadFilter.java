package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.val;
import mil.usmc.mls2.stratis.core.infrastructure.util.UserSiteSelectionTracker;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UserSiteSelectionThreadFilter implements Filter {

  public void init(FilterConfig filterConfig) {
    //NO-OP
  }

  public void destroy() {
    //NO-OP
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    StratisConfig multiTenancyConfig = ContextUtils.getBean(StratisConfig.class);

    val isXml = "XMLHttpRequest".equalsIgnoreCase(((HttpServletRequest) request).getHeader("X-Requested-With"));

    val requestUriMobile = ((HttpServletRequest) request).getRequestURI().contains("/stratis/app/mobile");

    try {
      if (multiTenancyConfig.isMultiTenancyEnabled()) {
        val isInvalidUserSettings = configureUserFromSession(request);

        //if this was an Xml request and from SMV do not redirect.  Allow SMV to handle the invalid session
        // by just allowing the filter chain to continue.
        if (isInvalidUserSettings && !isXml && !requestUriMobile) {

          ((HttpServletResponse) response).sendRedirect(((HttpServletRequest) request).getContextPath());
          return;
        }
      }
      else {
        //Non Integration mode, the db connection string is :STRATDBSVRDS
        UserSiteSelectionTracker.configureTracker(GlobalConstants.LEGACY_DATABASE_CONNECTION_NAME, null);
      }
      chain.doFilter(request, response);
    }
    finally {
      UserSiteSelectionTracker.clear();
    }
  }

  private boolean configureUserFromSession(ServletRequest request) {
    String userSiteSelected = (String) ((HttpServletRequest) request).getSession().getAttribute(StratisConfig.USER_DB_SELECTED);
    String siteName = (String) ((HttpServletRequest) request).getSession().getAttribute(StratisConfig.USER_SITE_NAME);

    HttpServletRequest req2 = ((HttpServletRequest) request);
    String url = req2.getRequestURL().toString();

    //Unauthenticated.html should be allowed to pass through the filter without a userSiteSelected, as thats the screen used when no session is available or a user logs out.
    if (url.contains("unauthenticated.html") || url.contains("Close.html")) {
      return false;
    }

    //If no user site has been selected, and the url is not for the site selection screen, force the system to redirect back to the site selection screen
    //this will handle anyone with a bookmark to the Login.jspx screen auto redirecting to the new site selection process screen.
    if (userSiteSelected == null && (!url.contains("/siteSelection"))) {
      //redirect to / if not at /
      return true;
    }
    UserSiteSelectionTracker.configureTracker(userSiteSelected, siteName);
    return false;
  }
}
