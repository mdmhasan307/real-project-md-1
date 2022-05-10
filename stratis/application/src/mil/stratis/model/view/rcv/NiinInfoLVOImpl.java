package mil.stratis.model.view.rcv;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class NiinInfoLVOImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for niinStr
   */
  public String getniinStr() {
    return (String) getNamedWhereClauseParam("niinStr");
  }

  /**
   * Sets <code>value</code> for bind variable niinStr
   */
  public void setniinStr(String value) {
    setNamedWhereClauseParam("niinStr", value);
  }
}
