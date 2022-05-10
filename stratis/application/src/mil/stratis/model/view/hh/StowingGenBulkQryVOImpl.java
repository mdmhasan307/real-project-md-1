package mil.stratis.model.view.hh;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class StowingGenBulkQryVOImpl extends ViewObjectImpl {

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
   * Gets the bind variable value for sidStr
   */
  public String getsidStr() {
    return (String) getNamedWhereClauseParam("sidStr");
  }

  /**
   * Sets <code>value</code> for bind variable sidStr
   */
  public void setsidStr(String value) {
    setNamedWhereClauseParam("sidStr", value);
  }
}
