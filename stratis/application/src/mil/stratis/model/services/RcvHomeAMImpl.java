package mil.stratis.model.services;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ApplicationModuleImpl;
import oracle.jbo.server.ViewLinkImpl;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
public class RcvHomeAMImpl extends ApplicationModuleImpl {

  /**
   * Container's getter for RcvStowTodayRO1
   */
  public ViewObjectImpl getRcvStowTodayRO1() {
    return (ViewObjectImpl) findViewObject("RcvStowTodayRO1");
  }

  /**
   * Container's getter for RcvTodayRO1
   */
  public ViewObjectImpl getRcvTodayRO1() {
    return (ViewObjectImpl) findViewObject("RcvTodayRO1");
  }

  /**
   * Container's getter for RcvStowTodayRO2
   */
  public ViewObjectImpl getRcvStowTodayRO2() {
    return (ViewObjectImpl) findViewObject("RcvStowTodayRO2");
  }

  /**
   * Container's getter for RcvTodayRptVL1
   */
  public ViewLinkImpl getRcvTodayRptVL1() {
    return (ViewLinkImpl) findViewLink("RcvTodayRptVL1");
  }
}
