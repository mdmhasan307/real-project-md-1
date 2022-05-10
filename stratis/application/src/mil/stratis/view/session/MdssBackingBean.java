package mil.stratis.view.session;

import lombok.NoArgsConstructor;
import mil.stratis.view.BackingHandler;
import mil.stratis.view.util.ADFUtils;
import mil.stratis.view.util.JSFUtils;
import oracle.adf.controller.faces.context.FacesPageLifecycleContext;
import oracle.adf.controller.v2.lifecycle.Lifecycle;
import oracle.adf.controller.v2.lifecycle.PagePhaseEvent;
import oracle.adf.controller.v2.lifecycle.PagePhaseListener;
import oracle.binding.BindingContainer;

@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
public class MdssBackingBean extends BackingHandler implements PagePhaseListener {

  protected transient BindingContainer bc;

  transient Object switch1 = 0;
  transient Object switch2 = 0;

  protected Integer getUserBeanUserId() {
    return Integer.parseInt(JSFUtils.getManagedBeanValue("userbean.userId").toString());
  }

  /**
   * Before the ADF page lifecycle's prepareModel phase, invoke a
   * custom onPageLoad() method. Subclasses override the onPageLoad()
   * to do something interesting during this event.
   */
  public void beforePhase(PagePhaseEvent event) {
    FacesPageLifecycleContext ctx =
        (FacesPageLifecycleContext) event.getLifecycleContext();
    if (event.getPhaseId() == Lifecycle.PREPARE_MODEL_ID) {
      bc = ctx.getBindingContainer();
      onPageLoad();
      bc = null;
    }
  }

  /**
   * After the ADF page lifecycle's prepareRender phase, invoke a
   * custom onPagePreRender() method. Subclasses override the onPagePreRender()
   * to do something interesting during this event.
   */
  public void afterPhase(PagePhaseEvent event) {
    FacesPageLifecycleContext ctx =
        (FacesPageLifecycleContext) event.getLifecycleContext();
    if (event.getPhaseId() == Lifecycle.PREPARE_RENDER_ID) {
      bc = ctx.getBindingContainer();
      onPagePreRender();
      bc = null;
    }
  }

  /**
   * Determine whether the current page request represents a postback.
   *
   * @return true if current page request represents a postback
   */
  @Override
  protected boolean isPostback() {
    return ADFUtils.isPostback();
  }

  public void onPageLoad() {
    // Subclasses can override this.
  }

  public void onPagePreRender() {
    // Subclasses can override this.
  }

  public void processInputValues() {
    // Subclasses can override this.
  }

  public void setSwitch1(Object switch1) {
    this.switch1 = switch1;
  }

  public Object getSwitch1() {
    return switch1;
  }

  public void setSwitch2(Object switch2) {
    this.switch2 = switch2;
  }

  public Object getSwitch2() {
    return switch2;
  }
}
