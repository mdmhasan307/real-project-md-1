package mil.stratis.view.session;

import lombok.extern.slf4j.Slf4j;
import mil.stratis.view.util.LoginUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class SessionRequiredFilter implements Filter {

  private static final Set<String> allowedUriFragments = new HashSet<>(25);

  static {
    allowedUriFragments.add("faces/login".toLowerCase()); //will allow both faces/Login and faces/Login.jspx
    allowedUriFragments.add("faces/unauthenticated.html".toLowerCase());
    allowedUriFragments.add("faces/close.html".toLowerCase());
    allowedUriFragments.add("faces/unauthorized.html".toLowerCase());
    allowedUriFragments.add("faces/resources/images/".toLowerCase());
  }

  private FilterConfig filterConfig = null;

  public void init(FilterConfig filter) {
    filterConfig = filter;
  }

  public void destroy() {
    filterConfig = null;
  }

  private boolean isPageAllowedIfNotLoggedIn(String uri) {
    uri = uri.toLowerCase();

    for (String allowedUriFragment : allowedUriFragments) {
      if (uri.contains(allowedUriFragment)) {
        return true;
      }
    }

    return false;
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

    String pageUri = ((HttpServletRequest) request).getRequestURI();
    boolean isAllowedUnauthenticatedPage = isPageAllowedIfNotLoggedIn(pageUri);

    boolean loggedIn = false;

    if (!isAllowedUnauthenticatedPage) {
      LoginUtils.LoginStatus status = LoginUtils.checkLogedIn(((HttpServletRequest) request).getSession());
      loggedIn = LoginUtils.checkAndLogStatus(status);
    }

    if (isAllowedUnauthenticatedPage || loggedIn) {
      log.trace("ok: {}, {}, {}", isAllowedUnauthenticatedPage, loggedIn, pageUri);

      chain.doFilter(request, response);
    }
    else {
      log.info("NOT LOGGED IN: {}", pageUri);

      // the session has expired or renewed. Redirect request
      ((HttpServletResponse) response).sendRedirect(filterConfig.getInitParameter("NotLoggedIn"));
    }
  }
} 