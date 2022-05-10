package mil.stratis.model.view.rcv;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class ReceiptViewImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for docNumStr
   */
  public String getdocNumStr() {
    return (String) getNamedWhereClauseParam("docNumStr");
  }

  /**
   * Sets <code>value</code> for bind variable docNumStr
   */
  public void setdocNumStr(String value) {
    setNamedWhereClauseParam("docNumStr", value);
  }
}
