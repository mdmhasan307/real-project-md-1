package mil.stratis.model.services;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.stratis.common.util.Util;
import mil.stratis.model.datatype.user.UserRights;
import mil.stratis.model.view.user.LoginUserViewImpl;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import oracle.jbo.Session;
import oracle.jbo.server.ApplicationModuleImpl;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleTypes;

import java.security.SecureRandom;
import java.sql.*;
import java.util.HashMap;
import java.util.List;

/**
 * Code Review: BillG
 * Date of Review: 11/09/13
 * Summary:
 * o First pass to fix gross problems.
 * - Fixed try catch for sql exceptions and removed connection leaks.
 * - Corrected method and variable naming conventions.
 * - Removed public variables and created accessors correcting code as required.
 * Outstanding:
 * o Need to get rid of all  the code that uses direct "Object" references.
 */

@Slf4j
@NoArgsConstructor //ADF BackingHandlers all need a no args constructor
public class LoginModuleImpl extends ApplicationModuleImpl {

  private static final SecureRandom generator = new SecureRandom();
  private String displayName = "";
  private String previousLogin;
  private String loginKey = "";
  private String cacUsername = "";
  private long loginUserRights = 0L;
  private long loginUserRightsHH = 0L;
  private String loginKeyHH = "";
  private int userId = 1;

  protected void prepareSession(Session session) {
    super.prepareSession(session);
  }

  public void lockAccount(String username) {

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "update users set locked='Y' where username=?", 0)) {
      pstmt.setString(1, username);
      pstmt.executeUpdate();
      getDBTransaction().commit();
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
      getDBTransaction().rollback();
    }
  }

  public int checkUserLoginReturn(int userID, int userTypeID) {
    int updateUserId = -1;

    // reset the local vars
    loginUserRights = 0L;
    displayName = "";
    loginKey = "";

    int retval = 0;

    try {

      updateUserId = userID;
      loginKey = generateLoginKey();
      retval = updateLoginInfo(updateUserId, loginKey, false);

      if (retval > 0) {
        loginUserRights = getUserGroupRights(updateUserId, "", userTypeID);
        if (loginUserRights > 0) {
          displayName = getCacUsername();
          LoginUserViewImpl view = getLoginUserView1();
          view.setNamedWhereClauseParam("SearchUser", getCacUsername());
          view.setNamedWhereClauseParam("SearchID", null);
          view.executeQuery();
        }
        else {
          log.error("User with id:  {}  has no privileges.", updateUserId);
          log.error("User with id:  {}  is invalid in this db.", updateUserId);
          updateUserId = 0;  // Mark it invalid
        }
      }
      else {
        updateUserId = retval;
      }
    }
    catch (Exception e) {
      log.error("An error occurred in check user login return for user id:  {}", userId, e);
      this.getDBTransaction().rollback();
    }

    // Update the user id
    userId = updateUserId;
    return updateUserId;
  }

  public void logLoginAttempts(String idnum, String subject, String success, String reason) {
    //CallableStatement st = null;
    try (CallableStatement st = getDBTransaction().createCallableStatement(
        "begin ? := pkg_user_mgmt.f_login_attempts(?, ?, ?, ?); end;", this.getDBTransaction().DEFAULT)) {
      st.registerOutParameter(1, Types.INTEGER);
      st.setString(2, idnum);
      st.setString(3, subject);
      st.setString(4, success);
      st.setString(5, reason);
      st.execute();
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * New functionality for CAC login, username/password no longer allowed
   * instead of using serial number, it uses the DoD ID number.
   */
  public int getUserLoginIdCAC(String cacNumber, String roleSelect) {
    int cacUserId = -1;
    String message = "";
    String role = "";

    try (CallableStatement stmt = getDBTransaction().createCallableStatement(
        "begin ? := pkg_user_mgmt.f_get_user_acct_types_4_cac(?, ?, ?); end;", this.getDBTransaction().DEFAULT)) {

      stmt.registerOutParameter(1, Types.INTEGER);
      stmt.registerOutParameter(2, OracleTypes.CURSOR);
      stmt.registerOutParameter(3, Types.VARCHAR);
      stmt.setString(4, cacNumber);
      stmt.execute();

      cacUserId = stmt.getInt(1);
      if (cacUserId == -1) {
        message = stmt.getString(3);
        log.trace("Message: {}", message);
      }
      else {
        try (ResultSet rs = ((OracleCallableStatement) stmt).getCursor(2)) {
          while (rs.next()) {
            if (rs.getString(5).equals("ACTIVE")) {
              role = rs.getString(4);
              if ((role.equals(roleSelect)) || roleSelect.equals("")) {
                cacUserId = rs.getInt(1);
                setCacUsername(rs.getString(2));
              }
            }
            else
              cacUserId = -2;
          }
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
    return cacUserId;
  }

  /**
   * @return user id or error code -777 = inactive, -555 = locked
   */
  public int getUserLoginId(String username, String cacNumber, boolean useCAC) {
    int userId = 0;
    String cacUserName = "";
    boolean locked = true;
    boolean inactive = true;

    String cacSql = "select user_id, nvl(locked,'N'), nvl(status,'NON-ACTIVE'), username from users where visible_flag='Y' and cac_number=?";
    String uidSql = "select user_id, nvl(locked,'N'), nvl(status,'NON-ACTIVE'), username from users where visible_flag='Y' and username=?";
    try {
      if (useCAC) {
        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(cacSql, 0)) {
          pstmt.setString(1, cacNumber);
          try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
              locked = rs.getString(2).equalsIgnoreCase("Y");
              inactive = rs.getString(3).equalsIgnoreCase("NON-ACTIVE");
              userId = rs.getInt(1);
              cacUserName = rs.getString(4);
            }
          }
        }
      }
      else {
        try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(uidSql, 0)) {
          pstmt.setString(1, username);
          try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
              locked = rs.getString(2).equalsIgnoreCase("Y");
              inactive = rs.getString(3).equalsIgnoreCase("NON-ACTIVE");
              userId = rs.getInt(1);
              cacUserName = rs.getString(4);
            }
          }
        }
      }

      if (inactive) {
        userId = -777;
        cacUserName = "";
      }
      else if (locked) {
        userId = -555;
        cacUserName = "";
      }
      setCacUsername(cacUserName);
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
    return userId;
  }

  /**
   * This method is used to generate a random login key
   */
  public String generateLoginKey() {
    StringBuffer loginKey = new StringBuffer();
    for (int i = 0; i < 15; i++) {
      int k = generator.nextInt(25);

      char j = 'A';
      j += k;

      loginKey.append(j);
    }
    return loginKey.toString();
  }

  /**
   * Redesign Function for get UserRights, gets userId and userType.
   */
  public long getUserGroupRights(int userId, String usertype, int usertypeID) {
    long userRights = 0;
    int userTypeId = 0;

    try {
      //Get user_ID
      if (usertypeID == 0) {
        try (CallableStatement stmt = getDBTransaction().createCallableStatement(
            "begin ? := pkg_user_mgmt.f_get_acct_type_id(?); end;", this.getDBTransaction().DEFAULT)) {

          stmt.registerOutParameter(1, Types.INTEGER);
          stmt.setString(2, usertype);
          stmt.execute();

          userTypeId = stmt.getInt(1);
        }
      }
      else
        userTypeId = usertypeID;

      try (CallableStatement stmt = getDBTransaction().createCallableStatement(
          "begin ? := pkg_user_mgmt.f_get_group_privs_4_user(?,?,?,?); end;", this.getDBTransaction().DEFAULT)) {

        stmt.registerOutParameter(1, Types.INTEGER);
        stmt.registerOutParameter(2, OracleTypes.CURSOR);
        stmt.registerOutParameter(3, Types.VARCHAR);
        stmt.setInt(4, userId);
        stmt.setInt(5, userTypeId);
        stmt.execute();

        //If it worked.
        if (stmt.getInt(1) == 0) {
          try (ResultSet rs = ((OracleCallableStatement) stmt).getCursor(2)) {
            while (rs.next()) {
              userRights |= UserRights.userRightLookUp(rs.getString(4));
            }
          }
        }
        else {
          log.error("Error in user privileges:  {}", stmt.getString(3));
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
    return userRights;
  }

  /**
   * @createdDate 02-26-2009
   */
  public int updateLoginInfo(int userId, String tempKey, boolean hh) {
    int retval;

    String checkExpirationQuery = "select user_id from users where user_id = ? and ((last_login < sysdate - 35) and (last_login_hh < sysdate - 35) and (Trans_Ts < sysdate - 35))";
    String statusUpdate = "update users set status = 'NON-ACTIVE' where user_id = ?";
    String lastLoginQuery = "select last_login from users where user_id = ?";
    String sqlHH = (hh) ? " last_login_hh=sysdate,logged_in_hh='Y',temp_key_hh=?" : " last_login=sysdate,logged_in='Y',temp_key=?";
    String lastLoginUpdate = "update users set " + sqlHH + " where user_id=?";

    try (PreparedStatement pstmt1 = getDBTransaction().createPreparedStatement(checkExpirationQuery, 0);
         PreparedStatement pstmt2 = getDBTransaction().createPreparedStatement(statusUpdate, 0);
         PreparedStatement pstmt3 = getDBTransaction().createPreparedStatement(lastLoginQuery, 0);
         PreparedStatement pstmt4 = getDBTransaction().createPreparedStatement(lastLoginUpdate, 0)) {

      //Check to see if the user has logged in on handheld or workstation in the last 35 days.
      pstmt1.setInt(1, userId);
      try (ResultSet rs = pstmt1.executeQuery()) {
        //If he hasn't logged in, set his status to NON-ACTIVE and return the error.
        if (rs.next()) {
          pstmt2.setInt(1, userId);
          pstmt2.executeUpdate();
          getDBTransaction().commit();
          retval = -2;
        }
        //Otherwise, update last logged in time.
        else {
          pstmt3.setInt(1, userId);
          try (ResultSet rs2 = pstmt3.executeQuery()) {
            if (rs2.next()) {
              previousLogin = rs2.getString("last_login");
            }
          }

          pstmt4.setString(1, tempKey);
          pstmt4.setInt(2, userId);
          pstmt4.executeUpdate();
          getDBTransaction().commit();

          retval = 1;
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
      getDBTransaction().rollback();
      retval = -1;
    }
    return retval;
  }

  /**
   * function to clear the login by id
   */
  public void clearLoggedInUser(int userId) {
    try {
      updateUserLogout(userId);
      // get the view object
      LoginUserViewImpl view = getLoginUserView1();
      view.clearCache();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }

    // clear the id
    this.userId = -1;
  }

  public void updateUserLogout(int user_id) {
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "update users set logged_in='N', temp_key=null where user_id=?", 0)) {
      //* have to rollback first, otherwise, JBO error already updated row inconsistent
      getDBTransaction().rollback();
      pstmt.setInt(1, user_id);
      pstmt.executeUpdate();
      getDBTransaction().commit();
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
      getDBTransaction().rollback();
    }
  }

  /**
   * Function to check if the given user is logged in by id
   * returns true if the user is logged in.
   */
  public boolean checkUserLoggedIn(int userId, String checkLoginKey) {

    boolean loggedIn = false;

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select user_id from users where user_id=? and logged_in='Y' and temp_key=?", 0)) {

      pstmt.setInt(1, userId);
      pstmt.setString(2, checkLoginKey);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          loggedIn = true;
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
    return loggedIn;
  }

  public void updateUserProfile(String lastname, String middle, String email, String user_id) {

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "update users set last_name=?, middle_name=? where user_id=?", 0)) {
      //* have to rollback first, otherwise, JBO error already updated row inconsistent
      getDBTransaction().rollback();
      pstmt.setString(1, lastname);
      pstmt.setString(2, middle);
      pstmt.setString(3, user_id);
      pstmt.executeUpdate();
      getDBTransaction().commit();
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
      getDBTransaction().rollback();
    }
  }

  public HashMap<Integer, String> getUserTypesCAC(String cacNumber) {
    int userId = -1;
    String message = "";
    String role = "";
    int roleID = 0;
    Integer roleIDInt;
    HashMap<Integer, String> userTypesMap = new HashMap<Integer, String>();
    //EXPEDITED LOGIN, REMOVE.
    try (CallableStatement stmt = getDBTransaction().createCallableStatement(
        "begin ? := pkg_user_mgmt.f_get_user_acct_types_4_cac(?, ?, ?); end;", this.getDBTransaction().DEFAULT)) {

      stmt.registerOutParameter(1, Types.INTEGER);
      stmt.registerOutParameter(2, OracleTypes.CURSOR);
      stmt.registerOutParameter(3, Types.VARCHAR);
      stmt.setString(4, cacNumber);

      stmt.execute();

      userId = stmt.getInt(1);
      if (userId == -1) {
        message = stmt.getString(3);
        log.debug(message);
      }
      else {
        try (ResultSet rs = ((OracleCallableStatement) stmt).getCursor(2)) {
          while (rs.next()) {
            if (rs.getString(5).equals("ACTIVE")) {
              role = rs.getString(4);
              roleID = rs.getInt(3);
              roleIDInt = new Integer(roleID);
              userTypesMap.put(roleIDInt, role);
            }
            else
              userId = -2;
          }
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
    return userTypesMap;
  }

  public HashMap<Integer, String> getUserTypes() {
    int userId = -1;
    String message = "";
    String role = "";
    int roleID = 0;
    Integer roleIDInt;
    HashMap<Integer, String> userTypesMap = new HashMap<Integer, String>();

    try (CallableStatement stmt = getDBTransaction().createCallableStatement(
        "begin ? := pkg_user_mgmt.f_get_all_acct_types(?, ?); end;", this.getDBTransaction().DEFAULT)) {
      stmt.registerOutParameter(1, Types.INTEGER);
      stmt.registerOutParameter(2, OracleTypes.CURSOR);
      stmt.registerOutParameter(3, Types.VARCHAR);
      stmt.execute();

      userId = stmt.getInt(1);
      if (userId == -1) {
        message = stmt.getString(3);
        log.debug(message);
      }
      else {
        try (ResultSet rs = ((OracleCallableStatement) stmt).getCursor(2)) {
          while (rs.next()) {
            role = rs.getString(2);
            roleID = rs.getInt(1);
            roleIDInt = new Integer(roleID);
            userTypesMap.put(roleIDInt, role);
          }
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
    return userTypesMap;
  }

  /**
   * the module which logs users in needs to update the
   * current user id in the equip table to the logged in user.
   * the workstations report will only work when this is set.
   * also done on manual logout.
   */
  public void clearUserWorkstation(int userId) {
    //    PreparedStatement pstmt = null;
    try {
      //* clear current_user_id in the equip table
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "update equip set current_user_id=null where current_user_id=?", 0)) {
        pstmt.setInt(1, userId);
        pstmt.executeUpdate();
        getDBTransaction().commit();
      }

      //* clear assign_to_user in the picking table @ZSL 6/2018
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "update picking set assign_to_user = null where assign_to_user = ?", 0)) {
        pstmt.setInt(1, userId);
        pstmt.executeUpdate();
        getDBTransaction().commit();
      }

      //* clear assign_to_user in the inventory_item table - MLS2STRAT-382
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
          "update INVENTORY_ITEM set assign_to_user = null where assign_to_user = ?", 0)) {
        pstmt.setInt(1, userId);
        pstmt.executeUpdate();
        getDBTransaction().commit();
      }
    }
    catch (SQLException e) {
      getDBTransaction().rollback();
      log.error("Exception while clearing user workstation", e);
    }
  }

  public boolean isSystemShutdownUser(Object loggedInUserId) {

    int iLoggedInUserId;

    if (loggedInUserId == null)
      return false;

    iLoggedInUserId = Util.cleanInt(loggedInUserId);

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select count(*) from site_info where shutdown_user_id=?", 0)) {

      //* clear current_user_id in the equip table
      pstmt.setInt(1, iLoggedInUserId);

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          int count = rs.getInt(1);
          if (count > 0)
            return true;
        }
      }
    }
    catch (SQLException e) {
      getDBTransaction().rollback();
      AdfLogUtility.logException(e);
    }
    return false;
  }

  /**
   * function to see if there is a system message waiting
   * returns null if none.
   */
  public Object systemShutdownMessage() {
    Object robj = null;
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select shutdown_message from site_info", 0)) {

      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          robj = rs.getObject(1);
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
    return robj;
  }

  /**
   * function to see if there is a system warning message waiting
   * Returns null if none
   */
  public Object systemShutdownWarningMessage() {
    Object robj = null;
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select shutdown_warning from site_info", 0)) {
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          robj = rs.getObject(1);
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
    return robj;
  }

  /**
   * the module which logs users in needs to update the
   * current user id in the equip table to the logged in user.
   * the workstations report will only work when this is set.
   * also done on manual logout
   */
  public void clearUserPasswordDate(Object username) {
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "update users set last_pw_update=null where username=?", 0)) {
      //* clear current_user_id in the equip table
      pstmt.setObject(1, username);
      pstmt.executeUpdate();
      getDBTransaction().commit();
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * This function returns all the rows in the equip table.
   */
  public void returnEquipRows(List equipId, List display) {
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(
        "select equipment_number, name, description from equip", 0)) {
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          Object Idobj = rs.getObject(1);
          Object Nameobj = rs.getObject(2);
          Object Descobj = rs.getObject(3);

          if ((Idobj != null) && (Nameobj != null) &&
              (Descobj != null)) {
            equipId.add(Idobj.toString());
            display.add(Nameobj.toString());
          }
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
  }

  public boolean isAnythingRunning() {
    final String sql = "select count(*) from site_interfaces where status in ('RUNNING', 'PROCESSING')";
    boolean running = false;
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          int max_value = rs.getInt(1);
          log.debug("==== ZSL ========= max_value =  {}", max_value);
          if (max_value > 0) running = true;
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
    return running;
  }

  /**
   * Container's getter for LoginUserView1
   */
  public LoginUserViewImpl getLoginUserView1() {
    return (LoginUserViewImpl) findViewObject("LoginUserView1");
  }

  /**
   * Container's getter for WorkLoadManager1
   */
  public ApplicationModuleImpl getWorkLoadManager1() {
    return (ApplicationModuleImpl) findApplicationModule("WorkLoadManager1");
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getPreviousLogin() {
    return previousLogin;
  }

  public void setLoginKey(String loginKey) {
    this.loginKey = loginKey;
  }

  public String getLoginKey() {
    return loginKey;
  }

  public void setCacUsername(String cacUsername) {
    this.cacUsername = cacUsername;
  }

  public String getCacUsername() {
    return cacUsername;
  }

  public void setLoginUserRights(long loginUserRights) {
    this.loginUserRights = loginUserRights;
  }

  public long getLoginUserRights() {
    return loginUserRights;
  }

  public void setLoginUserRightsHH(long loginUserRightsHH) {
    this.loginUserRightsHH = loginUserRightsHH;
  }

  public long getLoginUserRightsHH() {
    return loginUserRightsHH;
  }

  public void setLoginKeyHH(String loginKeyHH) {
    this.loginKeyHH = loginKeyHH;
  }

  public String getLoginKeyHH() {
    return loginKeyHH;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public int getUserId() {
    return userId;
  }
}
