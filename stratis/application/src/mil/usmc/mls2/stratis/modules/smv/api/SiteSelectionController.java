package mil.usmc.mls2.stratis.modules.smv.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.ValidationException;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.utility.CertificateManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * On initial hit to website:
 * grab CAC info
 * access common database for sites associated with CAC user
 * if a single site is found
 * add site details to HttpSession
 * proceed to existing log in page
 * if multiple sites are found
 * create screen that allows selection of STRATIS site
 * add site details to HttpSession
 * proceed to existing log in page
 * If multi-tenancy IS NOT active - go to existing log in page
 * goal is to ensure site works exactly the same way it does now, or as closely as possible, when multi-tenancy is not enabled
 */
@Slf4j
@Controller
@RequestMapping(value = "/siteSelection")
@RequiredArgsConstructor
@Profile(Profiles.LEGACY)
@SuppressWarnings("Duplicates")
public class SiteSelectionController {

  private final Environment env;
  private final CertificateManager certificateUtils;

  private static final String INVALID_CAC_PAGE = "siteSelection/invalidCac";
  private static final String LOGIN_REDIRECT = "redirect:/faces/Login.jspx";

  @GetMapping
  public String get(HttpServletRequest request) {

    String bypass = handleLoginBypass(request);

    if (!StringUtils.isEmpty(bypass))
      return bypass;

    boolean isCacValid;
    try {
      val certInformation = certificateUtils.getCertInformationFromRequest(request);
      isCacValid = certInformation.isPresent();
    }
    catch (ValidationException e) {
      isCacValid = false;
    }

    if (!isCacValid) {
      return INVALID_CAC_PAGE;
    }
    return LOGIN_REDIRECT;
  }

  private String handleLoginBypass(HttpServletRequest request) {
    if (!Profiles.isDevelopment(env)) {
      log.warn("Dev profile inactive, login bypass disabled");
      return null;
    }
    String cacId = request.getParameter("cacId");
    String role = request.getParameter("role");
    String workstation = request.getParameter("workstation");
    String view = request.getParameter("view");
    if (!StringUtils.isEmpty(cacId) && !StringUtils.isEmpty(role) && !StringUtils.isEmpty(workstation)) {
      //login is attempting to be bypassed.
      return "redirect:/app/loginBypass?cacId=" + cacId + "&role=" + role + "&workstation=" + workstation + "&view=" + view;
    }
    return null;
  }
}
