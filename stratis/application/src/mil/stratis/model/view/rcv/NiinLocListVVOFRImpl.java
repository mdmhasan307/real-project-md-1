package mil.stratis.model.view.rcv;

import lombok.NoArgsConstructor;
import oracle.jbo.server.ViewObjectImpl;

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class NiinLocListVVOFRImpl extends ViewObjectImpl {

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

  /**
   * Gets the bind variable value for expDtStr
   */
  public String getexpDtStr() {
    return (String) getNamedWhereClauseParam("expDtStr");
  }

  /**
   * Sets <code>value</code> for bind variable expDtStr
   */
  public void setexpDtStr(String value) {
    setNamedWhereClauseParam("expDtStr", value);
  }

  /**
   * Gets the bind variable value for newWeight
   */
  public Double getnewWeight() {
    return (Double) getNamedWhereClauseParam("newWeight");
  }

  /**
   * Sets <code>value</code> for bind variable newWeight
   */
  public void setnewWeight(Double value) {
    setNamedWhereClauseParam("newWeight", value);
  }

  /**
   * Gets the bind variable value for newCube
   */
  public Double getnewCube() {
    return (Double) getNamedWhereClauseParam("newCube");
  }

  /**
   * Sets <code>value</code> for bind variable newCube
   */
  public void setnewCube(Double value) {
    setNamedWhereClauseParam("newCube", value);
  }
}
