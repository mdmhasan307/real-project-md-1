package mil.stratis.model.view.inv;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class UniqueNiinViewObjImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for NiinSearch
   */
  public String getNiinSearch() {
    return (String) getNamedWhereClauseParam("NiinSearch");
  }

  /**
   * Sets <code>value</code> for bind variable NiinSearch
   */
  public void setNiinSearch(String value) {
    setNamedWhereClauseParam("NiinSearch", value);
  }
}
