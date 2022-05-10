package mil.stratis.view.session;

import lombok.extern.slf4j.Slf4j;
import oracle.adf.controller.ControllerContext;
import oracle.adf.controller.ViewPortContext;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

// Backing bean for exception handler
@Slf4j
public class TaskFlowExceptionHandler extends mil.stratis.view.BackingHandler {

  private String debugMessage = "Message not set.";

  public TaskFlowExceptionHandler() {
    super();
  }

  public void exceptionHandler() {

    FacesMessage message = new FacesMessage("Custom message ");
    message.setSeverity(FacesMessage.SEVERITY_WARN);
    FacesContext fc = FacesContext.getCurrentInstance();
    fc.addMessage(null, message);
  }

  public void controllerExceptionHandler() {

    ControllerContext context = ControllerContext.getInstance();
    ViewPortContext currentRootViewPort = context.getCurrentRootViewPort();

    if (currentRootViewPort.isExceptionPresent()) {
      currentRootViewPort.clearException();
    }
  }

  public void setDebugMessage(String str) {
    debugMessage = str;
    log.trace(str);
  }

  public String getDebugMessage() {
    return debugMessage;
  }
}
