package mil.stratis.model.view.sysadm;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class RefDataloadLogViewImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for INTERFACE_NAME
   */
  public String getINTERFACE_NAME() {
    return (String) getNamedWhereClauseParam("INTERFACE_NAME");
  }

  /**
   * Sets <code>value</code> for bind variable INTERFACE_NAME
   */
  public void setINTERFACE_NAME(String value) {
    setNamedWhereClauseParam("INTERFACE_NAME", value);
  }
}
