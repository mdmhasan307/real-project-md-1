package mil.stratis.model.view.rcv;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class StowCancelVOImpl extends ViewObjectImpl {

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
