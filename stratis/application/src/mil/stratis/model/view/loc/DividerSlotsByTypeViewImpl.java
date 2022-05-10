package mil.stratis.model.view.loc;

import lombok.NoArgsConstructor;
import oracle.jbo.domain.Number;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class DividerSlotsByTypeViewImpl extends ViewObjectImpl {

  /**
   * Returns the bind variable value for SlotType.
   *
   * @return bind variable value for SlotType
   */
  public Number getSlotType() {
    return (Number) getNamedWhereClauseParam("SlotType");
  }

  /**
   * Sets <code>value</code> for bind variable SlotType.
   *
   * @param value value to bind as SlotType
   */
  public void setSlotType(Number value) {
    setNamedWhereClauseParam("SlotType", value);
  }
}
