package mil.usmc.mls2.stratis.modules.smv.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.model.datatype.user.UserRights;
import mil.stratis.model.services.LoginModuleImpl;
import mil.stratis.view.session.Nav;
import mil.stratis.view.user.UserBacking;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.core.domain.event.UserLoggedInEvent;
import mil.usmc.mls2.stratis.core.domain.model.Equipment;
import mil.usmc.mls2.stratis.core.domain.model.EquipmentSearchCriteria;
import mil.usmc.mls2.stratis.core.domain.model.UserType;
import mil.usmc.mls2.stratis.core.domain.model.UserTypeSearchCriteria;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.manager.UserSessionManager;
import mil.usmc.mls2.stratis.core.service.EquipmentService;
import mil.usmc.mls2.stratis.core.service.EventService;
import mil.usmc.mls2.stratis.core.service.UserTypeService;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.SpringAdfBindingUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
@RequestMapping
@RequiredArgsConstructor
@Profile(Profiles.DEV)
public class LoginBypassController {

  private final UserTypeService userTypeService;
  private final EquipmentService equipmentService;

  @GetMapping(value = "/loginBypass")
  @SuppressWarnings("Duplicates")
  public String show(@RequestParam String cacId, @RequestParam String role, @RequestParam String workstation, @RequestParam String view, HttpServletRequest request) {
    String defaultRedirect = "redirect:/";

    val criteria = UserTypeSearchCriteria.builder()
        .type(role.toUpperCase())
        .build();

    UserType userType;
    val userTypes = userTypeService.search(criteria);
    if (CollectionUtils.isEmpty(userTypes)) {
      log.warn("User was not able to auto login (invalid role).  Returning to regular process.");
      return defaultRedirect;
    }
    else {
      userType = userTypes.get(0);
    }

    LoginModuleImpl service = SpringAdfBindingUtils.getLoginModuleService();

    int workstationId;

    val equipmentCriteria = EquipmentSearchCriteria.builder()
        .name(workstation.toUpperCase())
        .includeDummy(false)
        .build();
    val equipmentList = equipmentService.search(equipmentCriteria);
    Equipment equipment;
    if (CollectionUtils.isEmpty(equipmentList)) {
      log.warn("User was not able to auto login (invalid workstation).  Returning to regular process.");
      return defaultRedirect;
    }
    else {
      equipment = equipmentList.get(0);
      workstationId = equipment.getEquipmentNumber();
    }

    int userId = service.getUserLoginIdCAC(cacId, "");
    userId = service.checkUserLoginReturn(userId, userType.getUserTypeId());

    if (userId > 0) {
      // get the user bean
      val txtUsername = service.getCacUsername();

      UserInfo user = (UserInfo) request.getSession().getAttribute("userbean");
      if (user == null) { user = new UserInfo();}
      user.setUsername(txtUsername);
      user.setDisplayName(service.getDisplayName());
      user.setLoginKey(service.getLoginKey());
      user.setUserTypeId(userType.getUserTypeId());
      user.setUsertypestring(userType.getType());

      user.setWorkstationId(workstationId);

      val newWorkstation = equipmentService.setWorkstation(userId, workstationId);

      user.setComCommandIndex("1");
      user.setComPrintIndex("2");
      user.setWorkstationId(newWorkstation.getEquipmentNumber());
      user.setWorkstationType(newWorkstation.getDescription());
      user.setWorkstationName(newWorkstation.getName());
      user.setUserRights(new UserRights(service.getLoginUserRights(), user.getWorkstationType()));

      user.setUserId(userId);

      // publish event
      val userLoggedInEvent = UserLoggedInEvent.builder()
          .userInfo(user)
          .build();

      val eventPublisher = ContextUtils.getBean(EventService.class);
      eventPublisher.publishEvent(userLoggedInEvent);

      UserSessionManager.addUserSession(user.getUserId(), request.getSession().getId());

      Nav nav = (Nav) request.getSession().getAttribute("footerbean");
      if (nav == null) { nav = new Nav(); }
      nav.setFooterMainSwitcher(1);
      request.getSession().setAttribute("footerbean", nav);

      String mechanizedFlag = null;
      if (newWorkstation.getWac() != null) {
        mechanizedFlag = newWorkstation.getWac().getMechanizedFlag();
      }

      // update the session bean
      long modifyrights = service.getLoginUserRights();

      // flip the bits over
      modifyrights = ~modifyrights;

      equipmentService.checkAndSetNonMech(mechanizedFlag, modifyrights, user);
      request.getSession().setAttribute("userbean", user);

      UserBacking userBacking = new UserBacking();
      userBacking.setUserInfo(user);
      request.getSession().setAttribute("userBackingBean", userBacking);

      if ("MOBILE".equalsIgnoreCase(view)) {
        return "redirect:/app/mobile";
      }
      return "redirect:/faces/WarehouseHome";
    }

    log.warn("User was not able to auto login (no user found).  Returning to regular process.");
    return defaultRedirect;
  }
}
