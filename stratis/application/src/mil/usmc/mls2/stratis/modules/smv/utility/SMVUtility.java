package mil.usmc.mls2.stratis.modules.smv.utility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.core.domain.model.SpaGetResponse;
import mil.usmc.mls2.stratis.core.domain.model.SpaPostResponse;
import mil.usmc.mls2.stratis.core.domain.model.SpaResponse;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.processor.gcss.event.FeedEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SMVUtility {

  private static boolean devActive;
  private static boolean integrationActive;

  private final Environment environment;

  @PostConstruct
  @SuppressWarnings("squid:S2696")
  private void postConstruct() {
    devActive = Profiles.isDevelopment(environment);
    integrationActive = Profiles.isIntegrationAny(environment);
  }

  public static SpaGetResponse processGetResponse(HttpServletRequest request, String page, String pageTitle) {
    try {
      val session = request.getSession();
      val eventMessage = (String) session.getAttribute(FeedEvent.SESSION_IDENTIFIER);

      val body = getResponseBodyPage(request, page);
      SpaGetResponse spaGetResponse = SpaGetResponse.builder()
          .result(SpaResponse.SpaResponseResult.SUCCESS)
          .pageTitle(pageTitle)
          .devProfileActive(devActive)
          .build();

      if (StringUtils.isNotBlank(eventMessage)) {
        spaGetResponse.addEventMessage(eventMessage);
        session.removeAttribute(FeedEvent.SESSION_IDENTIFIER);
      }

      spaGetResponse.setResponseBody(body);
      return spaGetResponse;
    }
    catch (Exception e) {
      return (SpaGetResponse) processException(SpaResponse.SpaResponseType.GET, ExceptionUtils.getStackTrace(e));
    }
  }

  public static SpaPostResponse processPostResponse(HttpSession session, String redirectUrl) {
    val eventMessage = (String) session.getAttribute(FeedEvent.SESSION_IDENTIFIER);

    val spaPostResponse = SpaPostResponse.builder()
        .redirectUrl(redirectUrl)
        .result(SpaResponse.SpaResponseResult.SUCCESS)
        .build();

    if (StringUtils.isNotBlank(eventMessage)) {
      spaPostResponse.addEventMessage(eventMessage);
      session.removeAttribute(FeedEvent.SESSION_IDENTIFIER);
    }
    return spaPostResponse;
  }

  public static SpaResponse processException(SpaResponse.SpaResponseType type, String errorMessage) {
    SpaResponse spaResponse;
    if (type == SpaResponse.SpaResponseType.GET) {
      spaResponse = SpaGetResponse.builder()
          .result(SpaResponse.SpaResponseResult.EXCEPTION)
          .devProfileActive(devActive)
          .build();
    }
    else {
      spaResponse = SpaPostResponse.builder()
          .result(SpaResponse.SpaResponseResult.EXCEPTION)
          .devProfileActive(devActive)
          .build();
    }

    spaResponse.addException(errorMessage);

    return spaResponse;
  }

  public static SpaResponse processSessionInvalid(SpaResponse.SpaResponseType type) {
    SpaResponse spaResponse;
    if (type == SpaResponse.SpaResponseType.GET) {
      spaResponse = SpaGetResponse.builder()
          .result(SpaResponse.SpaResponseResult.SESSION_INVALID)
          .build();
    }
    else {
      spaResponse = SpaPostResponse.builder()
          .result(SpaResponse.SpaResponseResult.SESSION_INVALID)
          .build();
    }
    spaResponse.addWarning("Your session has timed out, you will be now redirected to the login page.");

    return spaResponse;
  }

  public static <T> List<T> mergeLists(Collection<T> listA, Collection<T> listB) {
    if (CollectionUtils.isEmpty(listA) && CollectionUtils.isEmpty(listB)) return null;

    Iterator<T> itA = listA.iterator();
    Iterator<T> itB = listB.iterator();
    ArrayList<T> result = new ArrayList<>();

    while (itA.hasNext() || itB.hasNext()) {
      if (itA.hasNext()) result.add(itA.next());
      if (itB.hasNext()) result.add(itB.next());
    }

    return result;
  }

  public static boolean authenticated(HttpSession session) {
    //ADF processes the login, and stores the userbean in session.  If its not there, redirect to the adf login screen.
    val userInfo = (UserInfo) session.getAttribute("userbean");
    return userInfo != null && userInfo.isLoggedIn();
  }

  public static boolean isDevActive() {
    return devActive;
  }

  public static boolean isIntegrationActive() {
    return integrationActive;
  }

  private static String getResponseBodyPage(HttpServletRequest request, String page) throws Exception {
    val rd = request.getRequestDispatcher("/WEB-INF/jsp/" + page + ".jsp");
    val r = new MockHttpServletResponse();
    rd.forward(request, r);
    return r.getContentAsString();
  }
}