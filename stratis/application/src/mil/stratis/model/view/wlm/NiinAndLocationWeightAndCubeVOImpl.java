package mil.stratis.model.view.wlm;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class NiinAndLocationWeightAndCubeVOImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for niinLocIdStr
   */
  public String getniinLocIdStr() {
    return (String) getNamedWhereClauseParam("niinLocIdStr");
  }

  /**
   * Sets <code>value</code> for bind variable niinLocIdStr
   */
  public void setniinLocIdStr(String value) {
    setNamedWhereClauseParam("niinLocIdStr", value);
  }
}
