package mil.stratis.view.session;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.model.services.LoginModuleImpl;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.domain.exception.ValidationException;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import mil.usmc.mls2.stratis.core.utility.CertificateManager;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Slf4j
@NoArgsConstructor
public class SessionBean extends Navigation {

  protected String txtMsgFailed = "";

  // Injected user info bean

  private UserInfo userInfo;
  private boolean validateUsingCac = false;
  
  public String actionCacLogin() {

    // If CAC login then redirect to login page on failure otherwise we are pass thu.
    String result = ((validateUsingCac == true) ? "GoLogin" : "GoLogin");

    LoginModuleImpl service = getLoginModule();
    Optional<CertificateManager.CertInformation> certInformation;
    int Userid = 0;
    try {
      HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

      try {
        val certUtils = ContextUtils.getBean(CertificateManager.class);
        certInformation = certUtils.getCertInformationFromRequest(request);
        if (!certInformation.isPresent()) {
          log.debug("No valid cert found.");
          return result;
        }
      }
      catch (ValidationException e) {
        log.debug("No valid PIV Auth cert found.");
        return result;
      }

      val certificateInformation = certInformation.get();
      val edipi = certificateInformation.getEdipi();
      val commonName = certificateInformation.getCommonName();
      if (edipi.length() > 0) {
        Userid = service.getUserLoginIdCAC(edipi, "");
        if (Userid > 0) {
          result = "GoLogin";
        }
        //No user account
        else {
          txtMsgFailed = "No User Account found. Please contact an administrator.";
          service.logLoginAttempts(edipi, commonName, "FAILURE", "No user account for id number");
        }
      }
      else {
        //No certificate
        if (commonName.length() > 0) {
          service.logLoginAttempts(edipi, commonName, "FAILURE", "Unable to parse id number");
        }
        else {
          service.logLoginAttempts(edipi, commonName, "FAILURE", "No certificate.");
        }
        txtMsgFailed = "No valid certificate was found. Please contact an administrator.";
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }

    log.trace("returning: {}", result);
    return result;
  }

  public LoginModuleImpl getLoginModule() {
    LoginModuleImpl service = null;
    try {
      service = (LoginModuleImpl) getStratisRootService().getLoginModule1();
    }
    catch (Exception e) {
      service = null;
    }
    return service;
  }

  public String getTxtMsgFailed() {
    return txtMsgFailed;
  }

  public void setTxtMsgFailed(String txtMsgFailed) {
    this.txtMsgFailed = txtMsgFailed;
  }

  public String goToPage1() {

    return "GoToPage1";
  }

  public String goToPage2() {
    return "GoToPage2";
  }

  public String goToLogin() {

    return "GoToLogin";
  }

  public void loginButtonListener(@SuppressWarnings("all") ActionEvent actionEvent) {
  }

  public void loginContinuedListener(@SuppressWarnings("all") ActionEvent actionEvent) {
  }

  public void setUserInfo(UserInfo bean) {
    userInfo = bean;
    validateUsingCac = userInfo.isCacLogin();
    log.trace("Use cac login: {}", validateUsingCac);
  }
}
