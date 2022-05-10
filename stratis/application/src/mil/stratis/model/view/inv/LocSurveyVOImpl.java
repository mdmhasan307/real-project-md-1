package mil.stratis.model.view.inv;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class LocSurveyVOImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for wacIdStr
   */
  public String getwacIdStr() {
    return (String) getNamedWhereClauseParam("wacIdStr");
  }

  /**
   * Sets <code>value</code> for bind variable wacIdStr
   */
  public void setwacIdStr(String value) {
    setNamedWhereClauseParam("wacIdStr", value);
  }

  public String getAssignToUserId() {
    return (String) getNamedWhereClauseParam("assignToUserId");
  }

  public void setAssignToUserId(String value) {
    setNamedWhereClauseParam("assignToUserId", value);
  }
}
