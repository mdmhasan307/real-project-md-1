package mil.stratis.model.util;

import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import oracle.jbo.server.DBTransaction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * This is utility class used for common database SQL functions.
 */
@Slf4j
public final class DBUtil {

  /**
   * Private Constructor.
   */
  private DBUtil() {
  }

  /**
   * SQL Utility Method
   * Pass in a sql select statement with one parameter only and get back first
   * field in resultset
   *
   * @param sql    - SQL select statement
   * @param iparam - int parameter used for SQL where clause
   * @param db     - database transaction handle
   * @return String - first field of the resultset
   */
  public static String getSingleReturnValue(String sql, int iparam, DBTransaction db) {
    return getSingleReturnValue(sql, String.valueOf(iparam), db);
  }

  /**
   * SQL Utility Method
   * Pass in a sql select statement with one parameter only and get back first
   * field in resultset
   *
   * @param sql   - SQL select statement
   * @param param - parameter used for SQL where clause
   * @param db    - database transaction handle
   * @return String - first field of the resultset
   */
  public static String getSingleReturnValue(String sql, String param, DBTransaction db) {
    String result = "";
    try (PreparedStatement pstmt = db.createPreparedStatement(sql, 0)) {
      pstmt.setString(1, param);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          result = rs.getString(1);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return result;
  }

  /**
   * SQL Utility Method
   * Pass in a sql select statement with zero parameters and
   * get back first field in resultset.
   * REQUIRED: First Field of the resultset must be an int value.
   *
   * @param sql - SQL select statement
   * @param db  - database transaction handle
   * @return int
   */
  public static int getCountValue(String sql, DBTransaction db) {
    int result = 0;
    try (PreparedStatement pstmt = db.createPreparedStatement(sql, 0)) {
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          result = rs.getInt(1);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return result;
  }

  /**
   * SQL Utility Method
   * Pass in a sql select statement with zero parameters and
   * get back first field in resultset.
   * REQUIRED: First Field of the resultset must be an int value.
   *
   * @param sql - SQL select statement
   * @param db  - database transaction handle
   * @return int
   */
  public static int getCountValueBy(String sql, String param, DBTransaction db) {
    int result = 0;
    try (PreparedStatement pstmt = db.createPreparedStatement(sql, 0)) {
      pstmt.setString(1, param);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          result = rs.getInt(1);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return result;
  }

  /**
   * SQL Utility Method
   *
   * @param sql - sql command of table to delete from
   * @param id  - int value of row id to delete
   * @param db  - database transaction handle
   */
  public static void deleteRow(String sql, int id, DBTransaction db) {
    try (PreparedStatement pstmt = db.createPreparedStatement(sql, 0)) {
      pstmt.setInt(1, id);
      pstmt.executeUpdate();
      db.commit();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * SQL Utility Method
   *
   * @param sqlCheck - sql command of table to check
   * @param id       - int value of row id to check
   * @param db       - database transaction handle
   * @return int - positive number indicates there are other table dependencies or an error
   */
  public static int checkDependencies(String sqlCheck, int id, DBTransaction db) {
    int count = 0;
    try (PreparedStatement pstmtCheck = db.createPreparedStatement(sqlCheck, 0)) {
      pstmtCheck.setInt(1, id);
      try (ResultSet rs = pstmtCheck.executeQuery()) {
        if (rs.next()) {
          count = rs.getInt(1);
        }
      }
    }
    catch (Exception e) {
      count = 1;
    }
    return count;
  }

  /**
   * SQL Utility Method
   *
   * @param sqlUPD   - sql command of table to update
   * @param keyValue - database parameter for where clause
   * @param db       - database transaction handle
   */
  public static void updateSingleRow(String sqlUPD, String keyValue, DBTransaction db) throws Exception {
    try (PreparedStatement pstmt = db.createPreparedStatement(sqlUPD, 0)) {
      pstmt.setString(1, keyValue);
      pstmt.executeUpdate();
      db.commit();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      db.rollback();
      throw e;
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
