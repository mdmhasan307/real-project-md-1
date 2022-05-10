package mil.stratis.model.view.invsetup;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class InventorySearchViewImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for SearchDesc
   */
  public String getSearchDesc() {
    return (String) getNamedWhereClauseParam("SearchDesc");
  }

  /**
   * Sets <code>value</code> for bind variable SearchDesc
   */
  public void setSearchDesc(String value) {
    setNamedWhereClauseParam("SearchDesc", value);
  }
}
