package mil.stratis.model.view.user;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class UserGroupViewImpl extends ViewObjectImpl {

  /**
   * Returns the bind variable value for SelectedUserID.
   *
   * @return bind variable value for SelectedUserID
   */
  public Long getSelectedUserID() {
    return (Long) getNamedWhereClauseParam("SelectedUserID");
  }

  /**
   * Sets <code>value</code> for bind variable SelectedUserID.
   *
   * @param value value to bind as SelectedUserID
   */
  public void setSelectedUserID(Long value) {
    setNamedWhereClauseParam("SelectedUserID", value);
  }
}
