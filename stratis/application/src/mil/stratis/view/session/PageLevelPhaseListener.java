package mil.stratis.view.session;

import lombok.extern.slf4j.Slf4j;
import oracle.adf.controller.v2.lifecycle.Lifecycle;
import oracle.adf.controller.v2.lifecycle.PagePhaseEvent;
import oracle.adf.controller.v2.lifecycle.PagePhaseListener;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;

@Slf4j
public class PageLevelPhaseListener implements PagePhaseListener {

  public PageLevelPhaseListener() {
    super();
  }

  @Override
  public void afterPhase(PagePhaseEvent pagePhaseEvent) {
    //NO-OP
  }

  @Override
  public void beforePhase(PagePhaseEvent pagePhaseEvent) {

    if (pagePhaseEvent.getPhaseId() == Lifecycle.PREPARE_MODEL_ID && isPostback()) {
      log.trace("This Will Execute AT Page Load");
    }

    if (pagePhaseEvent.getPhaseId() == Lifecycle.METADATA_COMMIT_ID && !isPostback()) {
      log.trace("This Will Execute When Leave The Page");
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
}
