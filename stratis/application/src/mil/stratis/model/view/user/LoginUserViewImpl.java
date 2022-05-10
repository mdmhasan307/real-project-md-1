package mil.stratis.model.view.user;

import lombok.NoArgsConstructor;
import oracle.jbo.domain.Number;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class LoginUserViewImpl extends ViewObjectImpl {

  /**
   * Returns the bind variable value for SearchUser.
   *
   * @return bind variable value for SearchUser
   */
  public String getSearchUser() {
    return (String) getNamedWhereClauseParam("SearchUser");
  }

  /**
   * Sets <code>value</code> for bind variable SearchUser.
   *
   * @param value value to bind as SearchUser
   */
  public void setSearchUser(String value) {
    setNamedWhereClauseParam("SearchUser", value);
  }

  /**
   * Returns the bind variable value for SearchID.
   *
   * @return bind variable value for SearchID
   */
  public Number getSearchID() {
    return (Number) getNamedWhereClauseParam("SearchID");
  }

  /**
   * Sets <code>value</code> for bind variable SearchID.
   *
   * @param value value to bind as SearchID
   */
  public void setSearchID(Number value) {
    setNamedWhereClauseParam("SearchID", value);
  }
}
