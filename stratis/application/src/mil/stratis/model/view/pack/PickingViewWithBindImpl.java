package mil.stratis.model.view.pack;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class PickingViewWithBindImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for pinVal
   */
  public String getpinVal() {
    return (String) getNamedWhereClauseParam("pinVal");
  }

  /**
   * Sets <code>value</code> for bind variable pinVal
   */
  public void setpinVal(String value) {
    setNamedWhereClauseParam("pinVal", value);
  }
}
