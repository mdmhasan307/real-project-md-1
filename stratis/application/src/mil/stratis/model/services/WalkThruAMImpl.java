package mil.stratis.model.services;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.stratis.model.view.walkThru.WalkThruQueueVOImpl;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import oracle.adf.share.ADFContext;
import oracle.jbo.DMLConstraintException;
import oracle.jbo.server.ApplicationModuleImpl;

import java.sql.PreparedStatement;
import java.sql.SQLIntegrityConstraintViolationException;

@Slf4j
@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
public class WalkThruAMImpl extends ApplicationModuleImpl {

  protected boolean fromPickbyWalkThru;

  public WalkThruQueueVOImpl getWalkThruQueueVO1() {
    return (WalkThruQueueVOImpl) findViewObject("WalkThruQueueVO1");
  }

  public boolean isFromPickbyWalkThru() {
    return this.fromPickbyWalkThru;
  }

  public void setFromPickbyWalkThru(boolean fromPickbyWlkThru) {
    this.fromPickbyWalkThru = fromPickbyWlkThru;
  }

  public String insertDocumentNumber(String documentNumber, String userId, String function, String walkThruInputField) {

    try (PreparedStatement preparedStmt = getDBTransaction().createPreparedStatement(
        "begin insert into WALKTHRU_QUEUE (DOCUMENT_NUMBER, MOD_BY_ID) values " + " (?,?); end;", 0)) {

      preparedStmt.setString(1, documentNumber);
      preparedStmt.setInt(2, Integer.parseInt(userId));
      preparedStmt.executeUpdate();
      getDBTransaction().commit();
      preparedStmt.close();
      ADFContext.getCurrent().getRequestScope().put("refreshNeeded", Boolean.TRUE);
      return "success";
    }
    catch (DMLConstraintException dml) {
      log.error("[{}]", function, dml);
      this.getDBTransaction().rollback();
      return "ERROR1"; // SQLIntegrityConstraintViolationException
    }
    catch (SQLIntegrityConstraintViolationException e2) {
      log.error("[{}]", function, e2);
      this.getDBTransaction().rollback();
      return "ERROR2";
    }
    catch (Exception e3) {
      log.error("[{}]", function, e3);
      this.getDBTransaction().rollback();
      return "ERROR3";
    }
  }

  /**
   * This method is used to delete a row from the WALKTHRU_QUEUE table given its document number,
   * after a successful check is completed ensuring there are no existing row dependencies.
   *
   * @return String
   * @author Hyun Koo
   * @date 05/08/17
   */
  public String deleteDocumentNumber(String documentNumber) {
    String message = "";
    try {
      deleteRow("delete from WALKTHRU_QUEUE where DOCUMENT_NUMBER =?", documentNumber);
      refreshWalkThruTable();
    }
    catch (Exception e) {
      message = "Walk Thru Deletion failed.";
      log.debug("Walk Thru deletion failed  {}", e.toString());
    }
    return message;
  }

  public void refreshWalkThruTable() {
    WalkThruQueueVOImpl walkThruQueueVOImpl = getWalkThruQueueVO1();
    try {
      walkThruQueueVOImpl.executeQuery();
      ADFContext.getCurrent().getRequestScope().put("refreshNeeded", Boolean.TRUE);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  private void deleteRow(String sql, String docNumber) throws Exception {
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
      pstmt.setString(1, docNumber);
      pstmt.executeUpdate();
      getDBTransaction().commit();
      pstmt.close();

      ADFContext.getCurrent().getRequestScope().put("refreshNeeded", Boolean.TRUE);
    }
    catch (Exception e) {
      getDBTransaction().rollback();
      throw e;
    }
  }
}
