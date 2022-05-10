package mil.stratis.common.db;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.stratis.common.util.StringUtil;
import oracle.jbo.server.DBTransaction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@NoArgsConstructor
public class DbCommon {
    
  /**
   * SQL Utility Method
   * Pass in a sql select statement with one parameter only and get back first
   * field in resultset
   *
   * @param sql   - SQL select statement
   * @param param - parameter used for SQL where clause
   * @return String - first field of the resultset
   */
  public String getSingleReturnValue(String sql, String param, DBTransaction db) {
    String result = "";
    try (PreparedStatement preparedStatement = db.createPreparedStatement(sql, 0)) {
      preparedStatement.setString(1, param);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          result = StringUtil.clean(resultSet.getString(1));
        }
      }
    }
    catch (SQLException e) {
      logError(e);
    }
    return result;
  }

  /**
   * SQL Utility Method
   * Pass in a sql select statement with two parameter only and get back first
   * field in resultset
   *
   * @param sql    - SQL select statement
   * @param param1 - parameter used for SQL where clause
   * @return String - first field of the resultset
   */
  public String getSingleReturnValue(String sql, String param1, String param2, DBTransaction db) {
    String result = "";
    try (PreparedStatement preparedStatement = db.createPreparedStatement(sql, 0)) {
      preparedStatement.setString(1, param1);
      preparedStatement.setString(2, param2);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          result = StringUtil.clean(resultSet.getString(1));
        }
      }
    }
    catch (SQLException e) {
      logError(e);
    }
    return result;
  }

  /**
   * SQL Utility Method
   * Pass in a sql select statement with three parameter only and get back first
   * field in resultset
   *
   * @param sql    - SQL select statement
   * @param param1 - parameter used for SQL where clause
   * @return String - first field of the resultset
   */
  public String getSingleReturnValue(String sql, String param1, String param2, String param3, DBTransaction db) {
    String result = "";
    try (PreparedStatement preparedStatement = db.createPreparedStatement(sql, 0)) {
      preparedStatement.setString(1, param1);
      preparedStatement.setString(2, param2);
      preparedStatement.setString(3, param3);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          result = StringUtil.clean(resultSet.getString(1));
        }
      }
    }
    catch (SQLException e) {
      logError(e);
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
   * @return int
   */
  public int getCountValue(String sql, DBTransaction db) {
    int result = 0;
    try (PreparedStatement preparedStatement = db.createPreparedStatement(sql, 0)) {
      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          result = resultSet.getInt(1);
        }
      }
    }
    catch (SQLException e) {
      logError(e);
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
   * @return int
   */
  public int getCountValueBy(String sql, String param, DBTransaction db) {
    int result = 0;
    try (PreparedStatement preparedStatement = db.createPreparedStatement(sql, 0)) {
      preparedStatement.setString(1, param);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          result = resultSet.getInt(1);
        }
      }
    }
    catch (SQLException e) {
      logError(e);
    }
    return result;
  }

  /**
   * SQL Utility Method
   *
   * @param sql - sql command of table to delete from
   * @param id  - String value of row id to delete
   */
  public void deleteRow(String sql, String id, DBTransaction db) {
    try (PreparedStatement pstmt = db.createPreparedStatement(sql, 0)) {
      pstmt.setString(1, id);
      pstmt.executeUpdate();
      db.commit();
    }
    catch (SQLException e) {
      db.rollback();
      logError(e);
    }
  }

  /**
   * SQL Utility Method
   *
   * @param sql    - sql command of table to delete from
   * @param param1 - String value of row id to delete
   */
  public void deleteRow(String sql, String param1, String param2, String param3, DBTransaction db) {
    try (PreparedStatement preparedStatement = db.createPreparedStatement(sql, 0)) {
      preparedStatement.setString(1, param1);
      preparedStatement.setString(2, param2);
      preparedStatement.setString(3, param3);
      preparedStatement.executeUpdate();
      db.commit();
    }
    catch (SQLException e) {
      db.rollback();
      logError(e);
    }
  }

  private void logError(Exception e) {
    log.error("Error in {}", e.getStackTrace()[0].getMethodName(), e);
  }
}
