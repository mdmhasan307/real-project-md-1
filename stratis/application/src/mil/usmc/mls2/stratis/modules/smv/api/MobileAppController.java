package mil.usmc.mls2.stratis.modules.smv.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.StratisConfig;
import mil.usmc.mls2.stratis.core.infrastructure.util.UserSiteSelectionTracker;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
@RequestMapping(value = "/mobile")
@RequiredArgsConstructor
public class MobileAppController {

  private static final String DEFAULT_PAGE = "mobile/mobileApp";

  private final StratisConfig multiTenancyConfig;

  @GetMapping
  public String show(HttpServletRequest request) {
    request.setAttribute("siteName", UserSiteSelectionTracker.getSiteName());
    request.setAttribute("multiTenancyEnabled", multiTenancyConfig.isMultiTenancyEnabled());

    if (!SMVUtility.authenticated(request.getSession())) {
      //session timed out, redirect to login.
      return "redirect:/";
    }
    return DEFAULT_PAGE;
  }
}
