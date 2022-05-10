package mil.stratis.model.view.stow;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class ScannedStowLVOImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for wacId
   */
  public Integer getwacId() {
    return (Integer) getNamedWhereClauseParam("wacId");
  }

  /**
   * Sets <code>value</code> for bind variable wacId
   */
  public void setwacId(Integer value) {
    setNamedWhereClauseParam("wacId", value);
  }
}
