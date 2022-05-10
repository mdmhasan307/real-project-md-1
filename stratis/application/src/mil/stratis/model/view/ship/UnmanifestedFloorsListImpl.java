package mil.stratis.model.view.ship;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class UnmanifestedFloorsListImpl extends ViewObjectImpl {

  /**
   * Returns the bind variable value for EQUIPMENT_NUMBER.
   *
   * @return bind variable value for EQUIPMENT_NUMBER
   */
  public String getEQUIPMENT_NUMBER() {
    return (String) getNamedWhereClauseParam("EQUIPMENT_NUMBER");
  }

  /**
   * Sets <code>value</code> for bind variable EQUIPMENT_NUMBER.
   *
   * @param value value to bind as EQUIPMENT_NUMBER
   */
  public void setEQUIPMENT_NUMBER(String value) {
    setNamedWhereClauseParam("EQUIPMENT_NUMBER", value);
  }
}
