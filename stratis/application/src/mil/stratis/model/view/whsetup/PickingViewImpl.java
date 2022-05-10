package mil.stratis.model.view.whsetup;

import lombok.NoArgsConstructor;
import oracle.jbo.domain.Number;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class PickingViewImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for WacSearch
   */
  public Number getWacSearch() {
    return (Number) getNamedWhereClauseParam("WacSearch");
  }

  /**
   * Sets <code>value</code> for bind variable WacSearch
   */
  public void setWacSearch(Number value) {
    setNamedWhereClauseParam("WacSearch", value);
  }
}
