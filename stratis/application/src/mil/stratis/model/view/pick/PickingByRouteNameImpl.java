package mil.stratis.model.view.pick;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class PickingByRouteNameImpl extends ViewObjectImpl {

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
   * Returns the bind variable value for userIdStr.
   *
   * @return bind variable value for userIdStr
   */
  public String getuserIdStr() {
    return (String) getNamedWhereClauseParam("userIdStr");
  }

  /**
   * Sets <code>value</code> for bind variable userIdStr.
   *
   * @param value value to bind as userIdStr
   */
  public void setuserIdStr(String value) {
    setNamedWhereClauseParam("userIdStr", value);
  }
}
