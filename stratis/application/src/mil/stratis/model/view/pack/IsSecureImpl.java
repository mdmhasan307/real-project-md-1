package mil.stratis.model.view.pack;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class IsSecureImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for cName
   */
  public String getcName() {
    return (String) getNamedWhereClauseParam("cName");
  }

  /**
   * Sets <code>value</code> for bind variable cName
   */
  public void setcName(String value) {
    setNamedWhereClauseParam("cName", value);
  }

  /**
   * Gets the bind variable value for cValue
   */
  public String getcValue() {
    return (String) getNamedWhereClauseParam("cValue");
  }

  /**
   * Sets <code>value</code> for bind variable cValue
   */
  public void setcValue(String value) {
    setNamedWhereClauseParam("cValue", value);
  }
}
