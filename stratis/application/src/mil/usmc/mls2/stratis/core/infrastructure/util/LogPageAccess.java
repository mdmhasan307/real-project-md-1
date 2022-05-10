package mil.usmc.mls2.stratis.core.infrastructure.util;

import lombok.NoArgsConstructor;
import mil.stratis.view.user.UserInfo;
import mil.stratis.view.util.JSFUtils;
import mil.usmc.mls2.stratis.common.model.enumeration.AuditLogTypeEnum;
import mil.usmc.mls2.stratis.core.domain.event.AdminPageAccessEvent;
import mil.usmc.mls2.stratis.core.service.EventService;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;

@NoArgsConstructor
public class LogPageAccess {
  
  public void logLoadPage(PhaseEvent phaseEvent) {
    if (phaseEvent.getPhaseId().equals(PhaseId.RENDER_RESPONSE)) {
      if (!isPostback()) {
        EventService eventPublisher = ContextUtils.getBean(EventService.class);
        UserInfo userInfo = (UserInfo) JSFUtils.getManagedBeanValue("userbean");
        AdminPageAccessEvent event = AdminPageAccessEvent.builder().page(page).userInfo(userInfo).result(AuditLogTypeEnum.SUCCESS).build();
        eventPublisher.publishEvent(event);
      }
    }
  }

  private boolean isPostback() {
    return Boolean.TRUE.equals(resolveExpression());
  }

  private Object resolveExpression() {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    Application app = facesContext.getApplication();
    ExpressionFactory elFactory = app.getExpressionFactory();
    ELContext elContext = facesContext.getELContext();
    ValueExpression valueExp = elFactory.createValueExpression(elContext, "#{adfFacesContext.postback}", Object.class);
    return valueExp.getValue(elContext);
  }

  private String page;

  public void setPage(String page) { this.page = page; }
}
