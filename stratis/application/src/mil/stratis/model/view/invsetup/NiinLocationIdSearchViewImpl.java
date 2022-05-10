package mil.stratis.model.view.invsetup;

import lombok.NoArgsConstructor;
import oracle.jbo.domain.Number;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class NiinLocationIdSearchViewImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for SearchId
   */
  public Number getSearchId() {
    return (Number) getNamedWhereClauseParam("SearchId");
  }

  /**
   * Sets <code>value</code> for bind variable SearchId
   */
  public void setSearchId(Number value) {
    setNamedWhereClauseParam("SearchId", value);
  }
}
