package mil.stratis.model.view.hh;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class ShelfLifeBulkVOImpl extends ViewObjectImpl {

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
