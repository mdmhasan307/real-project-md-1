package mil.stratis.model.view.rcv;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class NiinLocListVViewImpl extends ViewObjectImpl {

  /**
   * Gets the bind variable value for wacIdStr
   */
  public Long getwacIdStr() {
    return (Long) getNamedWhereClauseParam("wacIdStr");
  }

  /**
   * Sets <code>value</code> for bind variable wacIdStr
   */
  public void setwacIdStr(Long value) {
    setNamedWhereClauseParam("wacIdStr", value);
  }

  /**
   * Gets the bind variable value for niinIdStr
   */
  public String getniinIdStr() {
    return (String) getNamedWhereClauseParam("niinIdStr");
  }

  /**
   * Sets <code>value</code> for bind variable niinIdStr
   */
  public void setniinIdStr(String value) {
    setNamedWhereClauseParam("niinIdStr", value);
  }
}
