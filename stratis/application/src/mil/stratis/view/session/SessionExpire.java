package mil.stratis.view.session;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SessionExpire implements Filter {

  private FilterConfig filterConfig = null;

  public void init(FilterConfig filterConfig) {
    this.filterConfig = filterConfig;
  }

  public void destroy() {
    filterConfig = null;
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    String requestedSession = ((HttpServletRequest) request).getRequestedSessionId();
    String currentWebSession = ((HttpServletRequest) request).getSession().getId();
    boolean sessionOk = currentWebSession.equalsIgnoreCase(requestedSession);
    // if the requested session is null then this is the first application
    // request and "false" is acceptable
    if (!sessionOk && requestedSession != null) {
      // the session has expired or renewed. Redirect request
      ((HttpServletResponse) response).sendRedirect(filterConfig.getInitParameter("SessionTimeoutRedirect"));
    }
    else {
      chain.doFilter(request, response);
    }
  }
} 