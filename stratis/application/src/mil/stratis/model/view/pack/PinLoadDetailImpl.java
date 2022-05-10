package mil.stratis.model.view.pack;

import lombok.NoArgsConstructor;
import oracle.jbo.domain.Number;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class PinLoadDetailImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for equipNum
   */
  public Number getequipNum() {
    return (Number) getNamedWhereClauseParam("equipNum");
  }

  /**
   * Sets <code>value</code> for bind variable equipNum
   */
  public void setequipNum(Number value) {
    setNamedWhereClauseParam("equipNum", value);
  }
}
