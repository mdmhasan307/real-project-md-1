package mil.stratis.model.view.sysadm;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class NiinUIchangeImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for iLoc
   */
  public Integer getiLoc() {
    return (Integer) getNamedWhereClauseParam("iLoc");
  }

  /**
   * Sets <code>value</code> for bind variable iLoc
   */
  public void setiLoc(Integer value) {
    setNamedWhereClauseParam("iLoc", value);
  }
}
