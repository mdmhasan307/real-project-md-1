package mil.usmc.mls2.stratis.system;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.user.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

//FUTURE INNOV Backlog no longer used for SMV view.  will leave here for re-addition if other non SMV spring pages are added and need authentication checking.
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationInterceptor extends HandlerInterceptorAdapter {

  @Override
  public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object potLuck) throws Exception {

    // Set the Request Url in the request object to get the base for the app.
    val rawRequestUrl = httpServletRequest.getRequestURL().toString();

    String contextPath = httpServletRequest.getContextPath();
    if (StringUtils.isBlank(contextPath)) {
      contextPath = "/";
    }

    String requestUrl = rawRequestUrl.substring(0, rawRequestUrl.indexOf(contextPath, 10) + contextPath.length());
    if (!contextPath.equals("/")) {
      requestUrl += "/";
    }

    val requestURI = httpServletRequest.getRequestURI();
    val session = httpServletRequest.getSession();

    //ADF processes the login, and stores the userbean in session.  If its not there, redirect to the adf login screen.
    val userInfo = (UserInfo) session.getAttribute("userbean");

    // For handling redirect requests, we extract out the target of the redirect first.
    if (httpServletRequest.getRequestURI().contains("/redir/")) {
      val redirectURI = requestURI.substring(requestURI.indexOf("/redir/") + 6);
      log.debug("Intercepted a redirect request.  redirectURL={}", redirectURI);
      session.setAttribute("redirectURL", redirectURI);
    }

    if (userInfo == null || !userInfo.isLoggedIn()) {
      redirectToLogin(httpServletRequest, httpServletResponse);
      return false;
    }

    return true;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView model) {
    //NO-OP
  }

  private void safeSessionInvalidate(HttpSession session) {
    try {
      session.invalidate();
    }
    catch (Exception e) {
      // do nothing ADF has a session invalidate bug when running in tomcat, which throws a session already invalidated IllegalStateException
    }
  }

  private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
    redirect(request, response, request.getContextPath() + "/");
  }

  private void redirect(HttpServletRequest request, HttpServletResponse response, String redirectUrl) throws IOException {
    response.sendRedirect(redirectUrl);
  }
}