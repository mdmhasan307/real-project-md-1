package mil.stratis.model.view.whsetup;

import lombok.NoArgsConstructor;
import oracle.jbo.domain.Number;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class EquipTableConsolidationViewImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for EQUIPMENT_NUMBER
   */
  public Number getEQUIPMENT_NUMBER() {
    return (Number) getNamedWhereClauseParam("EQUIPMENT_NUMBER");
  }

  /**
   * Sets <code>value</code> for bind variable EQUIPMENT_NUMBER
   */
  public void setEQUIPMENT_NUMBER(Number value) {
    setNamedWhereClauseParam("EQUIPMENT_NUMBER", value);
  }
}
