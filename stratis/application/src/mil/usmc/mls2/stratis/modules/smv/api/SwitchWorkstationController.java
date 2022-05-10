package mil.usmc.mls2.stratis.modules.smv.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.domain.model.SortOrder;
import mil.usmc.mls2.stratis.core.domain.model.*;
import mil.usmc.mls2.stratis.core.service.EquipmentService;
import mil.usmc.mls2.stratis.core.service.UserTypeService;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import mil.usmc.mls2.stratis.modules.smv.utility.SMVUtility;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/mobile/switch")
@RequiredArgsConstructor
public class SwitchWorkstationController {

  private static final String DEFAULT_PAGE = "mobile/switchWorkstation";

  private final EquipmentService equipmentService;
  private final UserTypeService userTypeService;
  private final GlobalConstants globalConstants;

  @GetMapping
  public SpaGetResponse show(HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaGetResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.GET);
      }

      val user = (UserInfo) request.getSession().getAttribute(globalConstants.getUserbeanSession());
      EquipmentSearchCriteria criteria = EquipmentSearchCriteria.builder()
          .includeDummy(false).build();
      criteria.setSort("name", SortOrder.ASC);
      List<Equipment> equipLoginAllValues = equipmentService.search(criteria);

      request.setAttribute("switchWorkstationCriteria", new SwitchWorkstationCriteria(user.getWorkstationId(), user.getWorkstationName(), user.getUserTypeId(), user.getUsertypestring()));
      request.setAttribute("workstations", equipLoginAllValues);
      request.setAttribute("userTypes", userTypeService.getAll());
      request.setAttribute("switchRolesEnabled", globalConstants.isSwitchUserRoleEnabledBySystem());

      return SMVUtility.processGetResponse(request, DEFAULT_PAGE, "Switch Workstations");
    }
    catch (Exception e) {
      log.error("Error occurred processing Switch Workstation show", e);
      return (SpaGetResponse) SMVUtility.processException(SpaResponse.SpaResponseType.GET, ExceptionUtils.getStackTrace(e));
    }
  }

  @PostMapping
  public SpaPostResponse post(SwitchWorkstationCriteria switchWorkstationCriteria, HttpServletRequest request) {
    try {
      if (!SMVUtility.authenticated(request.getSession())) {
        return (SpaPostResponse) SMVUtility.processSessionInvalid(SpaResponse.SpaResponseType.POST);
      }
      val spaPostResponse = SMVUtility.processPostResponse(request.getSession(), "mobile/home");
      val user = (UserInfo) request.getSession().getAttribute("userbean");
      if (user.getWorkstationId() != switchWorkstationCriteria.getWorkstationId() || user.getUserTypeId() != switchWorkstationCriteria.getUserTypeId()) {
        val previousWorkstation = user.getWorkstationName();
        val previousUserTypeString = user.getUsertypestring();

        long userRights = equipmentService.getNewUserRights(user.getUserId(), switchWorkstationCriteria.getUserTypeId());
        if (userRights != 0) {
          user.setUserTypeId(switchWorkstationCriteria.getUserTypeId());
          user.setUsertypestring(switchWorkstationCriteria.getUserTypeName());
          user.setWorkstationId(switchWorkstationCriteria.getWorkstationId());

          equipmentService.switchWorkstation(user, userRights);

          request.getSession().setAttribute("userbean", user);

          if (!previousWorkstation.equals(user.getWorkstationName())) {
            spaPostResponse.addNotification(String.format("Workstation Switched Successfully from %s, to %s", previousWorkstation, user.getWorkstationName()));
          }
          if (!previousUserTypeString.equals(user.getUsertypestring())) {
            spaPostResponse.addNotification(String.format("Role Switched Successfully from %s, to %s", previousUserTypeString, user.getUsertypestring()));
          }
          spaPostResponse.setUserRole(user.getUsertypestring());
          spaPostResponse.setUserWorkstation(user.getWorkstationName());
        }
        else {
          spaPostResponse.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
          spaPostResponse.addWarning(String.format("You do not have permission to the role %s.", switchWorkstationCriteria.getUserTypeName()));
        }
      }
      else {
        spaPostResponse.setResult(SpaResponse.SpaResponseResult.VALIDATION_WARNINGS);
        if (globalConstants.isSwitchUserRoleEnabledBySystem()) {
          spaPostResponse.addWarning("Either role or workstation must be changed.  No change made.");
        }
        else {
          spaPostResponse.addWarning("Selected workstation matched current workstation.  No change made.");
        }
      }
      return spaPostResponse;
    }
    catch (
        Exception e) {
      log.error("Error occurred processing Switch Workstation post", e);
      return (SpaPostResponse) SMVUtility.processException(SpaResponse.SpaResponseType.POST, ExceptionUtils.getStackTrace(e));
    }
  }
}
