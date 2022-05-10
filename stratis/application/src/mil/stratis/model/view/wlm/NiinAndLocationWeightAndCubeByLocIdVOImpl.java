package mil.stratis.model.view.wlm;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class NiinAndLocationWeightAndCubeByLocIdVOImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for locIdStr
   */
  public String getlocIdStr() {
    return (String) getNamedWhereClauseParam("locIdStr");
  }

  /**
   * Sets <code>value</code> for bind variable locIdStr
   */
  public void setlocIdStr(String value) {
    setNamedWhereClauseParam("locIdStr", value);
  }

  /**
   * Gets the bind variable value for niinIdStr
   */
  public String getniinIdStr() {
    return (String) getNamedWhereClauseParam("niinIdStr");
  }

  /**
   * Sets <code>value</code> for bind variable niinIdStr
   */
  public void setniinIdStr(String value) {
    setNamedWhereClauseParam("niinIdStr", value);
  }
}
