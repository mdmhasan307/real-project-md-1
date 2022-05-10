package mil.stratis.model.view.wlm;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class ErrorLVOImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for errorCodeStr
   */
  public String geterrorCodeStr() {
    return (String) getNamedWhereClauseParam("errorCodeStr");
  }

  /**
   * Sets <code>value</code> for bind variable errorCodeStr
   */
  public void seterrorCodeStr(String value) {
    setNamedWhereClauseParam("errorCodeStr", value);
  }
}
