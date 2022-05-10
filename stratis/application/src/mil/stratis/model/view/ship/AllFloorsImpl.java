package mil.stratis.model.view.ship;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class AllFloorsImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for EQUIPMENT_NUMBER
   */
  public String getEQUIPMENT_NUMBER() {
    return (String) getNamedWhereClauseParam("EQUIPMENT_NUMBER");
  }

  /**
   * Sets <code>value</code> for bind variable EQUIPMENT_NUMBER
   */
  public void setEQUIPMENT_NUMBER(String value) {
    setNamedWhereClauseParam("EQUIPMENT_NUMBER", value);
  }
}
