package mil.stratis.model.view.pack;

import lombok.NoArgsConstructor;
import oracle.jbo.domain.Number;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class ConsolidationViewWithBindImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for consolId
   */
  public Number getconsolId() {
    return (Number) getNamedWhereClauseParam("consolId");
  }

  /**
   * Sets <code>value</code> for bind variable consolId
   */
  public void setconsolId(Number value) {
    setNamedWhereClauseParam("consolId", value);
  }
}
