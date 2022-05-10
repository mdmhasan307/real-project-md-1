package mil.usmc.mls2.stratis.modules.smv.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.common.domain.exception.ValidationException;
import mil.usmc.mls2.stratis.core.domain.model.CommonUserSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.model.SiteSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.model.SiteStatusEnum;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.StratisConfig;
import mil.usmc.mls2.stratis.core.service.common.CommonUserService;
import mil.usmc.mls2.stratis.core.service.common.SiteService;
import mil.usmc.mls2.stratis.core.utility.CertificateManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
@Profile(Profiles.INTEGRATION_ANY)
@SuppressWarnings("Duplicates")
public class MultiTenancySiteSelectionController {

  private final StratisConfig multiTenancyConfig;
  private final Environment env;
  private final CertificateManager certificateUtils;

  private static final String INVALID_CAC_PAGE = "siteSelection/invalidCac";
  private static final String NO_USER_PAGE = "siteSelection/noUser";
  private static final String NO_SITES_CONFIGURED = "siteSelection/noSitesConfigured";
  private static final String DEFAULT_PAGE = "siteSelection/siteSelection";

  private static final String LOGIN_REDIRECT = "redirect:/faces/Login.jspx";

  private final CommonUserService commonUserService;
  private final SiteService siteService;

  @GetMapping
  public String get(HttpServletRequest request) {

    //ADF Binds the db connection specified to the session for the length of the session.  To allow going to this
    //page after previous login (without closing/logging out) this will force the logout through invalidating the session
    //allowing the selection of another system.
    try {
      request.getSession().invalidate();
    }
    catch (IllegalStateException e) {
      // do nothing ADF has a session invalidate bug when running in tomcat, which throws a session already invalidated IllegalStateException
    }

    if (CollectionUtils.isEmpty(multiTenancyConfig.getDatasources())) {
      return NO_SITES_CONFIGURED;
    }

    SiteSearchCriteria siteCriteria = SiteSearchCriteria.builder()
        .sitesAvailable(multiTenancyConfig.getDatasources())
        .statusId(SiteStatusEnum.ACTIVE.getId())
        .build();

    val sites = siteService.search(siteCriteria);

    request.setAttribute("sitesAvailable", sites);

    String bypass = handleLoginBypass(request);

    if (!StringUtils.isEmpty(bypass))
      return bypass;

    String userEdipi = null;
    boolean isCacValid;
    try {
      val certInformation = certificateUtils.getCertInformationFromRequest(request);
      isCacValid = certInformation.isPresent();
      if (certInformation.isPresent()) {
        userEdipi = certInformation.get().getEdipi();
      }
    }
    catch (
        ValidationException e) {
      isCacValid = false;
    }

    if (!isCacValid) {
      return INVALID_CAC_PAGE;
    }

    CommonUserSearchCriteria userCriteria = CommonUserSearchCriteria.builder()
        .cacNumber(userEdipi)
        .sitesAvailable(multiTenancyConfig.getDatasources())
        .build();

    val matchingAccounts = commonUserService.search(userCriteria);

    if (CollectionUtils.isEmpty(matchingAccounts)) {
      //show user a page saying they do not have accounts on any sites.
      return NO_USER_PAGE;
    }
    else if (matchingAccounts.size() == 1) {
      request.getSession().setAttribute(StratisConfig.USER_DB_SELECTED, matchingAccounts.get(0).getSiteName());
      request.getSession().setAttribute(StratisConfig.USER_SITE_NAME, getSiteNameForSite(matchingAccounts.get(0).getSiteName()));
      return LOGIN_REDIRECT;
    }

    request.setAttribute("matchingAccounts", matchingAccounts);
    return DEFAULT_PAGE;
  }

  @PostMapping
  public String post(HttpServletRequest request) {
    String siteSelected = request.getParameter("siteSelected");
    request.getSession().setAttribute(StratisConfig.USER_DB_SELECTED, siteSelected);

    request.getSession().setAttribute(StratisConfig.USER_SITE_NAME, getSiteNameForSite(siteSelected));
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
    String site = request.getParameter("site");
    String view = request.getParameter("view");
    List<String> sites = new ArrayList<>();
    if (!StringUtils.isEmpty(cacId) && !StringUtils.isEmpty(role) && !StringUtils.isEmpty(workstation) && !StringUtils.isEmpty(site)) {
      sites.add(site);

      CommonUserSearchCriteria criteria = CommonUserSearchCriteria.builder()
          .cacNumber(cacId)
          .sitesAvailable(sites)
          .build();

      val matchingAccounts = commonUserService.search(criteria);

      //If the login bypass user doesn't exist for the specified site, don't allow the bypass.
      if (CollectionUtils.isEmpty(matchingAccounts)) {
        log.warn(String.format("User Bypass Failed, User doesn't exist in specified DB %s", site));
        return null;
      }

      request.getSession().setAttribute(StratisConfig.USER_DB_SELECTED, site);
      request.getSession().setAttribute(StratisConfig.USER_SITE_NAME, getSiteNameForSite(site));

      //login is attempting to be bypassed.
      return "redirect:/app/loginBypass?cacId=" + cacId + "&role=" + role + "&workstation=" + workstation + "&view=" + view;
    }
    return null;
  }

  private String getSiteNameForSite(String siteSelected) {
    SiteSearchCriteria siteCriteria = SiteSearchCriteria.builder()
        .sitesAvailable(Collections.singletonList(siteSelected))
        .build();

    val sites = siteService.search(siteCriteria);
    return sites.get(0).getDescription();
  }
}
