package mil.stratis.model.view.inv;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class NIINLocDetailsVOImpl extends ViewObjectImpl {

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
}
