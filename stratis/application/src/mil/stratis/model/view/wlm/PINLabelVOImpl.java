package mil.stratis.model.view.wlm;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class PINLabelVOImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for pinStr
   */
  public String getpinStr() {
    return (String) getNamedWhereClauseParam("pinStr");
  }

  /**
   * Sets <code>value</code> for bind variable pinStr
   */
  public void setpinStr(String value) {
    setNamedWhereClauseParam("pinStr", value);
  }
}
