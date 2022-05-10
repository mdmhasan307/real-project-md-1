package mil.stratis.model.view.sysadm;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class NiinSearchViewImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for SearchNIIN
   */
  public String getSearchNIIN() {
    return (String) getNamedWhereClauseParam("SearchNIIN");
  }

  /**
   * Sets <code>value</code> for bind variable SearchNIIN
   */
  public void setSearchNIIN(String value) {
    setNamedWhereClauseParam("SearchNIIN", value);
  }
}
