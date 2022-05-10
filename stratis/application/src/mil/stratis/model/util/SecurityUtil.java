package mil.stratis.model.util;

import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import oracle.jbo.server.DBTransaction;

import java.sql.PreparedStatement;

@Slf4j
public final class SecurityUtil {

  /**
   * Empty constructor.
   */
  private SecurityUtil() {
    //* do nothing
  }

  /**
   * Gets the encrypted value from database given the decrypted value.
   */
  public static String getEncryptedValue(String val, DBTransaction db) {
    return val;
  }

  public static boolean isValueEncrypted(String value, DBTransaction db) {
    return false;
  }

  /**
   * Gets the decrypted value from database.
   */
  public static String getDecryptedValue(String enc_val, DBTransaction db) {

    return enc_val;
  }

  public static void insertSecurityAuditLog(boolean successType, String category, Object equip, String desc, DBTransaction db) {
    PreparedStatement pstmt = null;
    try {
      String sqlINS = "insert into audit_log (source, type, timestamp, category, event, user_id, equipment_number, description) " +
          "values ('SECURITY',?,sysdate, ?,?,1, ?,?)";
      pstmt = db.createPreparedStatement(sqlINS, 0);

      pstmt.setString(1, (successType) ? "Success Audit" : "Failed Audit");
      pstmt.setString(2, category);
      pstmt.setString(3, "");
      pstmt.setObject(4, equip);
      pstmt.setString(5, desc);

      pstmt.execute();
      db.commit();
      pstmt.close();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      db.rollback();
    }
    finally {
      if (pstmt != null) {
        try {
          pstmt.close();
        }
        catch (Exception e) {log.trace("Exception caught and ignored", e);}
      }
    }
  }

  public static void lockAccount(String username, DBTransaction db) {
    try {
      String sqlUPD = "update users set locked='Y' where username=?";
      DBUtil.updateSingleRow(sqlUPD, username, db);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Override toString method.
   *
   * @return String
   */
  public String toString() {
    return "All classes may override non-final methods.";
  }
}
