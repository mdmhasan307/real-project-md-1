package mil.stratis.view.util;

import lombok.extern.slf4j.Slf4j;
import mil.stratis.view.user.UserInfo;

import javax.servlet.http.HttpSession;

@Slf4j
public class LoginUtils {

  public enum LoginStatus {
    SUCCESS,
    FAILNOUSERBEAN,
    FAILNOID,
    FAILEXCEPTION
  }

  public static LoginStatus checkLogedIn(HttpSession session) {
    LoginStatus result = LoginStatus.SUCCESS;
    try {
      UserInfo userbean = (UserInfo) session.getAttribute("userbean");
      if (userbean == null) {
        result = LoginStatus.FAILNOUSERBEAN;
      }
      else if (userbean.getUserId() <= 0) {
        result = LoginStatus.FAILNOID;
      }
    }
    catch (Exception e) {
      result = LoginStatus.FAILNOUSERBEAN;
      log.info("Unable to check if user is logged in or not, will treat loggedIn as false", e);
    }
    return result;
  }

  public static boolean checkAndLogStatus(LoginStatus status) {
    boolean loggedIn = false;
    switch (status) {
      case SUCCESS:
        loggedIn = true;
        break;
      case FAILNOID:
        log.debug("userbean.UserId is null or empty");
        break;
      case FAILNOUSERBEAN:
        log.debug("userbean is null");
        break;
      case FAILEXCEPTION:
        log.debug("Exception checking for login see exception logged above");
        break;
    }
    return loggedIn;
  }
}
