package mil.stratis.model.view.rcv;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class ReceiptTransshipmentVOImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for rcnStr
   */
  public String getrcnStr() {
    return (String) getNamedWhereClauseParam("rcnStr");
  }

  /**
   * Sets <code>value</code> for bind variable rcnStr
   */
  public void setrcnStr(String value) {
    setNamedWhereClauseParam("rcnStr", value);
  }
}
