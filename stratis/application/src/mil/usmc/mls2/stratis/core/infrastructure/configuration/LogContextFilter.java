package mil.usmc.mls2.stratis.core.infrastructure.configuration;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.user.UserInfo;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

@Slf4j
public class LogContextFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    val httpServletRequest = (HttpServletRequest) request;

    try {
      // configure MDC
      MDC.put("username", username(httpServletRequest));
      MDC.put("requestId", UUID.randomUUID().toString());
      MDC.put("requestUrl", httpServletRequest.getRequestURL().toString());
      MDC.put("requestMethod", httpServletRequest.getMethod());

      chain.doFilter(request, response);
    }
    finally {
      // Tear down MDC data: cleans up ThreadLocal data
      MDC.clear();
    }
  }

  @Override
  public void destroy() {
    // nothing
  }

  @Override
  public void init(FilterConfig filterConfig) {
    // nothing
  }

  private String userId(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session == null) return null;
    val user = session.getAttribute("userbean");
    if (user == null) return null;
    return String.valueOf(((UserInfo) user).getUserId());
  }

  private String username(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session == null) return null;
    val user = session.getAttribute("userbean");
    if (user == null) return null;
    return ((UserInfo) user).getUsername();
  }
}
