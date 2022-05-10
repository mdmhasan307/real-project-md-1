package mil.stratis.model.view.whsetup;

import lombok.NoArgsConstructor;
import oracle.jbo.domain.Number;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class WacTableMechViewImpl extends ViewObjectImpl {
 
  /**
   * Gets the bind variable value for WAREHOUSE_ID
   */
  public Number getWAREHOUSE_ID() {
    return (Number) getNamedWhereClauseParam("WAREHOUSE_ID");
  }

  /**
   * Sets <code>value</code> for bind variable WAREHOUSE_ID
   */
  public void setWAREHOUSE_ID(Number value) {
    setNamedWhereClauseParam("WAREHOUSE_ID", value);
  }
}
