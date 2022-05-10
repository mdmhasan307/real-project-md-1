package mil.usmc.mls2.stratis.modules.smv.api.picking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.SpaGetResponse;
import mil.usmc.mls2.stratis.core.domain.model.SpaResponse;
import mil.usmc.mls2.stratis.modules.smv.utility.MappingConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping(value = MappingConstants.PICKING_HOME)
@RequiredArgsConstructor
public class PickingHomeController {

  private static final String DEFAULT_PAGE = "mobile/picking/home";

  @GetMapping
  public SpaGetResponse show(HttpServletRequest request) {
    if (!SMVUtility.authenticated(request.getSession())) {
      return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
    }

    return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Picking");
  }
}
