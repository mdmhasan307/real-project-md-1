package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import lombok.var;
import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogCategoryEnum;
import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogSourceEnum;
import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogTypeEnum;
import mil.usmc.mls2.stratis.core.domain.event.*;
import mil.usmc.mls2.stratis.core.domain.event.domain.*;
import mil.usmc.mls2.stratis.core.domain.model.AuditLog;
import mil.usmc.mls2.stratis.core.domain.repository.AuditLogRepository;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.StratisConfig;
import mil.usmc.mls2.stratis.core.infrastructure.util.UserSiteSelectionTracker;
import mil.usmc.mls2.stratis.integration.mls2.core.domain.event.AuditCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogGenerator {

  private final AuditLogRepository auditLogRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final StratisConfig multiTenancyConfig;

  @EventListener
  @Transactional
  public void handle(UserLoggedOutEvent event) {
    val userInfo = event.getUserInfo();
    val auditLog = createAuditLog(AuditLogSourceEnum.SECURITY, AuditLogTypeEnum.SUCCESS, AuditLogCategoryEnum.AUTH_LOGOUT,
        userInfo, String.format("%s - Logout Success", userInfo.getUsername()));
    persist(auditLog);
  }

  @EventListener
  @Transactional
  public void handle(UserLoginFailedEvent event) {
    val userInfo = event.getUserInfo();
    val auditLog = createAuditLog(AuditLogSourceEnum.SECURITY, AuditLogTypeEnum.FAILURE, AuditLogCategoryEnum.AUTH_LOGIN_FAILED,
        userInfo, String.format("%s - %s :: %s", userInfo.getUsername(), event.getMessage(), userInfo.getUserId()));
    persist(auditLog);
  }

  @EventListener
  @Transactional
  public void handle(UserLoggedInEvent event) {
    val userInfo = event.getUserInfo();
    val auditLog = createAuditLog(AuditLogSourceEnum.SECURITY, AuditLogTypeEnum.SUCCESS, AuditLogCategoryEnum.AUTH_SUCCESS,
        userInfo, String.format("%s - Logged in to %s with role %s", userInfo.getUsername(), userInfo.getWorkstationName(), userInfo.getUsertypestring()));
    persist(auditLog);
  }

  @EventListener
  @Transactional
  public void handle(UserSwitchedWorkstationEvent event) {
    val userInfo = event.getUserInfo();
    val auditLog = createAuditLog(AuditLogSourceEnum.SECURITY, AuditLogTypeEnum.SUCCESS, AuditLogCategoryEnum.AUTH_WORKSTATION_SWITCHED,
        userInfo, String.format("%s - Switched Workstation to %s with role %s", userInfo.getUsername(), userInfo.getWorkstationName(), userInfo.getUsertypestring()));
    persist(auditLog);
  }

  @EventListener
  @Transactional
  public void handle(UserSessionTimeoutEvent event) {
    val userInfo = event.getUserInfo();
    val sessionId = event.getSessionId();
    val auditLog = createAuditLog(AuditLogSourceEnum.SECURITY, AuditLogTypeEnum.SUCCESS, AuditLogCategoryEnum.SESSION_TIMEOUT,
        userInfo, String.format("%s - Session %s timeout", userInfo.getUsername(), sessionId));
    persist(auditLog);
  }

  @EventListener
  @Transactional
  public void handle(UserPermissionEvent event) {
    val modifyingUser = event.getModifyingUser();
    val modifiedUser = event.getModifiedUser();
    val permission = event.getPermission();
    val type = event.getType();
    if (UserPermissionEvent.Action.ADD == event.getAction())
      handleAddUserPermissionEvent(modifyingUser, modifiedUser, permission, type, event);
    else if (UserPermissionEvent.Action.ERROR == event.getAction())
      handleErrorUserPermissionEvent(modifyingUser, modifiedUser, permission, type, event);
    else
      handleDeleteUserPermissionEvent(modifyingUser, modifiedUser, permission, type, event);
  }

  private void handleAddUserPermissionEvent(UserInfo modifyingUser, String modifiedUser, String permission, UserPermissionEvent.PermissionType type, UserPermissionEvent event) {
    val logType = UserPermissionEvent.PermissionType.TYPE == type ? AuditLogCategoryEnum.USER_TYPE_ADDED : AuditLogCategoryEnum.USER_GROUP_ADDED;
    val message = AuditLogTypeEnum.SUCCESS == event.getResult() ? "%s added %s permission %s to %s" : "%s failed to add %s permission %s to %s";
    val auditLog = createAuditLog(AuditLogSourceEnum.SECURITY, AuditLogTypeEnum.SUCCESS, logType,
        modifyingUser, String.format(message, modifyingUser.getUsername(), type.name(), permission, modifiedUser));
    persist(auditLog);
  }

  private void handleDeleteUserPermissionEvent(UserInfo modifyingUser, String modifiedUser, String permission, UserPermissionEvent.PermissionType type, UserPermissionEvent event) {
    val logType = UserPermissionEvent.PermissionType.TYPE == type ? AuditLogCategoryEnum.USER_TYPE_DELETED : AuditLogCategoryEnum.USER_GROUP_DELETED;
    val message = AuditLogTypeEnum.SUCCESS == event.getResult() ? "%s deleted %s permission %s from %s" : "%s failed to deleted %s permission %s from %s";
    val auditLog = createAuditLog(AuditLogSourceEnum.SECURITY, AuditLogTypeEnum.SUCCESS, logType,
        modifyingUser, String.format(message, modifyingUser.getUsername(), type.name(), permission, modifiedUser));
    persist(auditLog);
  }

  private void handleErrorUserPermissionEvent(UserInfo modifyingUser, String modifiedUser, String permission, UserPermissionEvent.PermissionType type, UserPermissionEvent event) {
    val auditLog = createAuditLog(AuditLogSourceEnum.SECURITY, AuditLogTypeEnum.SUCCESS, AuditLogCategoryEnum.USER_PERMISSION_ERROR, modifyingUser, event.getMessage());
    persist(auditLog);
  }

  @EventListener
  @Transactional
  public void handle(UserGroupPrivilegeEvent event) {
    val modifyingUser = event.getModifyingUser();
    val group = event.getGroup();
    val privilege = event.getPrivilege();
    val result = event.getResult();
    val logType = UserGroupPrivilegeEvent.Action.ADD == event.getAction() ? AuditLogCategoryEnum.USER_GROUP_PRIVILEGE_ADDED : AuditLogCategoryEnum.USER_GROUP_PRIVILEGE_DELETED;
    var message = "";
    if (UserGroupPrivilegeEvent.Action.ADD == event.getAction())
      message = AuditLogTypeEnum.SUCCESS == result ? "added" : "failed to add";
    else
      message = AuditLogTypeEnum.SUCCESS == result ? "deleted" : "failed to delete";

    val auditLog = createAuditLog(AuditLogSourceEnum.SECURITY, AuditLogTypeEnum.SUCCESS, logType,
        modifyingUser, String.format("%s %s the %s privilege for group %s", modifyingUser.getUsername(), message, privilege, group));
    persist(auditLog);
  }

  @EventListener
  @Transactional
  public void handle(CustomerUpdateEvent event) {
    val modifyingUser = event.getUserInfo();
    val message = "Customer %s was modified by user %s";
    val auditLog = createAuditLog(AuditLogSourceEnum.APPLICATION, event.getResult(), AuditLogCategoryEnum.ACTION_UPDATE_CUSTOMER, modifyingUser,
        String.format(message, event.getCustomer().getAac(), modifyingUser.getUsername()));
    persist(auditLog);
  }

  @EventListener
  @Transactional
  public void handle(WorkstationDeletionEvent event) {
    val modifyingUser = event.getUserInfo();
    val message = "Workstation %s was deleted by user %s";
    val auditLog = createAuditLog(AuditLogSourceEnum.APPLICATION, event.getResult(), AuditLogCategoryEnum.ACTION_UPDATE_CUSTOMER, modifyingUser,
        String.format(message, event.getWacId(), modifyingUser.getUsername()));
    persist(auditLog);
  }

  @EventListener
  @Transactional
  public void handle(ChangeLocationEvent event) {
    val user = event.getUserInfo();
    val message = "Location for NIIN %s was changed from %s to %s by user %s";
    val auditLog = createAuditLog(AuditLogSourceEnum.APPLICATION, event.getResult(), AuditLogCategoryEnum.ACTION_CHANGE_LOCATION, user,
        String.format(message, event.getNiin(), event.getFromLocation(), event.getToLocation(), user.getUsername()));
    persist(auditLog);
  }

  @EventListener
  @Transactional
  public void handle(CreateNiinEvent event) {
    val user = event.getUserInfo();
    val message = "NIIN %s with FSC %s was created by user %s";
    val auditLog = createAuditLog(AuditLogSourceEnum.APPLICATION, event.getResult(), AuditLogCategoryEnum.ACTION_CHANGE_LOCATION, user,
        String.format(message, event.getNiin(), event.getFsc(), user.getUsername()));
    persist(auditLog);
  }

  @EventListener
  @Transactional
  public void handle(DeleteNiinEvent event) {
    val user = event.getUserInfo();
    val message = "NIIN %s was deleted by user %s";
    val auditLog = createAuditLog(AuditLogSourceEnum.APPLICATION, event.getResult(), AuditLogCategoryEnum.ACTION_CHANGE_LOCATION, user,
        String.format(message, event.getNiin(), user.getUsername()));
    persist(auditLog);
  }

  @EventListener
  @Transactional
  public void handle(AdminPageAccessEvent event) {
    val userInfo = event.getUserInfo();
    val page = event.getPage();
    val result = event.getResult();
    val message = AuditLogTypeEnum.SUCCESS == result ? "%s - accessed page %s" : "%s - attempted to access page %s but was not an admin";
    val auditLog = createAuditLog(AuditLogSourceEnum.SECURITY, result, AuditLogCategoryEnum.ADMIN_PAGE_ACCESS,
        userInfo, String.format(message, userInfo.getUsername(), page));
    persist(auditLog);
  }

  @EventListener
  @Transactional
  public void handle(WorkstationPageAccessFailureEvent event) {
    val userInfo = event.getUserInfo();
    val page = event.getPage();
    String message = "";
    switch (event.getResult()) {
      case ADMIN:
        message = "but was an admin";
        break;
      case PERMISSION:
        message = "but did not have permission";
        break;
      case WORKSTATION:
        message = "but was on a different workstation";
        break;
    }
    val auditLog = createAuditLog(AuditLogSourceEnum.SECURITY, AuditLogTypeEnum.FAILURE, AuditLogCategoryEnum.ADMIN_PAGE_ACCESS,
        userInfo, String.format("%s - attempted to access page %s %s", userInfo.getUsername(), page, message));
    persist(auditLog);
  }

  @EventListener
  @Transactional
  public void handle(UserGroupEvent event) {
    val modifyingUser = event.getModifyingUser();
    val group = event.getGroup();
    val result = event.getResult();
    val logType = UserGroupEvent.Action.ADD == event.getAction() ? AuditLogCategoryEnum.USER_GROUP_ADDED : AuditLogCategoryEnum.USER_GROUP_DELETED;
    var message = "";
    if (UserGroupEvent.Action.ADD == event.getAction())
      message = AuditLogTypeEnum.SUCCESS == result ? "added" : "failed to add";
    else
      message = AuditLogTypeEnum.SUCCESS == result ? "deleted" : "failed to delete";

    val auditLog = createAuditLog(AuditLogSourceEnum.SECURITY, AuditLogTypeEnum.SUCCESS, logType,
        modifyingUser, String.format("%s %s group %s", modifyingUser.getUsername(), message, group));
    persist(auditLog);
  }

  private AuditLog createAuditLog(AuditLogSourceEnum source, AuditLogTypeEnum type, AuditLogCategoryEnum category, UserInfo userInfo, String message) {
    return AuditLog.builder()
        .source(source)
        .type(type)
        .timestamp(OffsetDateTime.now())
        .category(category)
        .event(null)
        .userId(userInfo.getUserId())
        .equipmentNumber(null)
        .description(message)
        .username(userInfo.getUsername())
        .build();
  }

  private void persist(AuditLog auditLog) {
    val site = UserSiteSelectionTracker.getUserSiteSelection();
    // persist
    auditLogRepository.persist(auditLog);

    //The logging of messages being sent to MIPS should add in the Stratis Site name to the description, so the audit logging
    //shows which site of stratis the auditing is from.
    val multiTenentMessage = multiTenancyConfig.isMultiTenancyEnabled() ? String.format(" for site %s", site) : "";
    auditLog.setDescription(auditLog.getDescription() + multiTenentMessage);
    // raise event
    val auditCreatedEvent = AuditCreatedEvent.builder()
        .auditLog(auditLog)
        .messageTimestamp(OffsetDateTime.now())
        .siteIdentifier(site)
        .build();

    eventPublisher.publishEvent(auditCreatedEvent);
  }
}
