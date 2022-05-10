package mil.stratis.model.view.wlm;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class NiinInfoMATSViewImpl extends ViewObjectImpl {
 
  /**
   * Gets the bind variable value for NIINSearch
   */
  public String getNIINSearch() {
    return (String) getNamedWhereClauseParam("NIINSearch");
  }

  /**
   * Sets <code>value</code> for bind variable NIINSearch
   */
  public void setNIINSearch(String value) {
    setNamedWhereClauseParam("NIINSearch", value);
  }

  /**
   * Gets the bind variable value for CCSearch
   */
  public String getCCSearch() {
    return (String) getNamedWhereClauseParam("CCSearch");
  }

  /**
   * Sets <code>value</code> for bind variable CCSearch
   */
  public void setCCSearch(String value) {
    setNamedWhereClauseParam("CCSearch", value);
  }
}
