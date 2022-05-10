package mil.stratis.model.services;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.common.util.Util;
import mil.stratis.model.view.loc.*;
import mil.stratis.model.view.pack.EquipViewImpl;
import mil.stratis.model.view.rcv.WACListImpl;
import mil.stratis.model.view.security.MasterDropView_SCCImpl;
import mil.stratis.model.view.security.MasterDropView_SCImpl;
import mil.stratis.model.view.security.MasterDropView_SMCCImpl;
import mil.stratis.model.view.ship.SingleFloorLocationViewImpl;
import mil.stratis.model.view.site.*;
import mil.stratis.model.view.user.CompleteUserListImpl;
import mil.stratis.model.view.user.UserGroupViewImpl;
import mil.stratis.model.view.whsetup.WacTableMechViewImpl;
import mil.usmc.mls2.stratis.core.utility.AdfLogUtility;
import oracle.jbo.Row;
import oracle.jbo.ViewCriteria;
import oracle.jbo.ViewCriteriaRow;
import oracle.jbo.server.ApplicationModuleImpl;
import oracle.jbo.server.DBTransaction;
import oracle.jbo.server.ViewLinkImpl;
import oracle.jbo.server.ViewObjectImpl;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleTypes;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@NoArgsConstructor
@SuppressWarnings({"Duplicates"})
public class AppModuleImpl extends ApplicationModuleImpl {

  public static final String LIKE_PERCENT = "like '%";
  public static final String IMAGE_FILENAME = "ImageFilename";
  public static final String DIVIDER_TYPE_ID = "DividerTypeId";
  public static final String LENGTH = "Length";
  public static final String WIDTH = "Width";
  public static final String HEIGHT = "Height";
  private String cacUserName = "";
  Object userId = 1;

  /**
   * Container's getter for SelectedUserGroups1.
   *
   * @return SelectedUserGroups1
   */
  public ViewObjectImpl getSelectedUserGroups1() {
    return (ViewObjectImpl) findViewObject("SelectedUserGroups1");
  }

  /**
   * Container's getter for GroupViewAll1.
   *
   * @return GroupViewAll1
   */
  public ViewObjectImpl getGroupViewAll1() {
    return (ViewObjectImpl) findViewObject("GroupViewAll1");
  }

  /**
   * Container's getter for GroupAllView2.
   *
   * @return GroupAllView2
   */
  public ViewObjectImpl getGroupAllView2() {
    return (ViewObjectImpl) findViewObject("GroupAllView2");
  }

  /**
   * Container's getter for GroupViewByID1.
   *
   * @return GroupViewByID1
   */
  public ViewObjectImpl getGroupViewByID1() {
    return (ViewObjectImpl) findViewObject("GroupViewByID1");
  }

  /**
   * Container's getter for GivenUserGrops.
   *
   * @return GivenUserGrops
   */
  public ViewObjectImpl getGivenUserGrops() {
    return (ViewObjectImpl) findViewObject("GivenUserGrops");
  }

  private NiinLocLocationViewImpl getNiinLocLocationView1() {
    return (NiinLocLocationViewImpl) findViewObject("NiinLoc_LocationView1");
  }

  public NiinInfoCcChangeViewImpl getNiinInfoCcChangeView2() {
    return (NiinInfoCcChangeViewImpl) findViewObject("NiinInfoCcChangeView2");
  }

  public NiinInfoCcChangeViewImpl getNiinInfoCcChangeView3() {
    return (NiinInfoCcChangeViewImpl) findViewObject("NiinInfoCcChangeView3");
  }

  public void setCacUserName(String cacUserName) {
    this.cacUserName = cacUserName;
  }

  public String getCacUserName() {
    return cacUserName;
  }

  public void setUserId(Object userId) {
    this.userId = userId;
  }

  public Object getUserId() {
    return userId;
  }

  public boolean isAlphaNumeric(String str) {
    return Pattern.matches("^[\\w]*$", str);
  }

  /******************************************************************
   * This function is responsible for returning a list of the groups that the current row in full user list has
   * ***************************************************************/
  public Map<String, String> returnSelectUserGroups(long userID) {
    String sql = "begin ? := pkg_user_mgmt.f_get_all_groups_4_user(?, ?, ?); end;";
    HashMap<String, String> groupMap = new HashMap<>();
    try (CallableStatement pstmt = getDBTransaction().createCallableStatement(sql, DBTransaction.DEFAULT)) {

      pstmt.registerOutParameter(1, Types.INTEGER);
      pstmt.registerOutParameter(2, OracleTypes.CURSOR);
      pstmt.registerOutParameter(3, Types.VARCHAR);
      pstmt.setLong(4, userID);

      pstmt.execute();

      val result = pstmt.getInt(1);
      if (result == 0) {
        try (ResultSet rs = ((OracleCallableStatement) pstmt).getCursor(2)) {
          while (rs.next()) {
            String acctTypeId = rs.getString(1);
            String acctType = rs.getString(2);
            String groupId = rs.getString(3);
            String groupName = rs.getString(4);
            String ids = acctTypeId + "," + groupId;
            String names = groupName + " (" + acctType + ")";
            groupMap.put(ids, names);
          }
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
    return groupMap;
  }

  /******************************************************************
   * This function is responsible for returning a list of the groups that the current row in full user list has
   * ***************************************************************/
  public Map<String, String> returnSelectUserGroups(long userID, int typeID) {
    String sql = "begin ? := pkg_user_mgmt.f_get_all_groups_4_user_type(?, ?, ?, ?); end;";
    HashMap<String, String> groupMap = new HashMap<>();
    try (CallableStatement st = getDBTransaction().createCallableStatement(sql, DBTransaction.DEFAULT)) {
      st.registerOutParameter(1, Types.INTEGER);
      st.registerOutParameter(2, OracleTypes.CURSOR);
      st.registerOutParameter(3, Types.VARCHAR);
      st.setLong(4, userID);
      st.setInt(5, typeID);
      st.execute();

      val result = st.getInt(1);
      if (result == 0) {
        try (ResultSet rs = ((OracleCallableStatement) st).getCursor(2)) {
          while (rs.next()) {
            String groupId = rs.getString(3);
            String groupName = rs.getString(4);
            String typeName = rs.getString(2);
            String ids = typeID + "," + groupId;
            String names = groupName + " (" + typeName + ")";
            groupMap.put(ids, names);
          }
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
    return groupMap;
  }

  public Map<Integer, String> returnAllGroups() {
    String sql = "begin ? := pkg_user_mgmt.f_get_all_groups(?, ?); end;";
    HashMap<Integer, String> groupMap = new HashMap<>();
    try (CallableStatement st = getDBTransaction().createCallableStatement(sql, DBTransaction.DEFAULT)) {
      st.registerOutParameter(1, Types.INTEGER);
      st.registerOutParameter(2, OracleTypes.CURSOR);
      st.registerOutParameter(3, Types.VARCHAR);
      st.execute();

      val result = st.getInt(1);
      if (result == 0) {
        try (ResultSet rs = ((OracleCallableStatement) st).getCursor(2)) {
          while (rs.next()) {
            groupMap.put(rs.getInt(1), rs.getString(2));
          }
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
    return groupMap;
  }

  public Map<Integer, String> returnSelectUserTypes(long userID) {

    String sql = "begin ? := pkg_user_mgmt.f_get_acct_types_4_user(?, ?, ?); end;";

    HashMap<Integer, String> userMap = new HashMap<>();
    try (CallableStatement st = getDBTransaction().createCallableStatement(sql, DBTransaction.DEFAULT)) {
      st.registerOutParameter(1, Types.INTEGER);
      st.registerOutParameter(2, OracleTypes.CURSOR);
      st.registerOutParameter(3, Types.VARCHAR);
      st.setLong(4, userID);
      st.execute();

      val result = st.getInt(1);
      if (result == 0) {
        try (ResultSet rs = ((OracleCallableStatement) st).getCursor(2)) {
          while (rs.next()) {
            userMap.put(rs.getInt(1), rs.getString(2));
          }
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
    return userMap;
  }

  public Map<Integer, String> returnAllTypes() {

    String sql = "begin ? := pkg_user_mgmt.f_get_all_acct_types(?, ?); end;";
    HashMap<Integer, String> typeMap = new HashMap<>();
    try (CallableStatement st = getDBTransaction().createCallableStatement(sql, DBTransaction.DEFAULT)) {
      st.registerOutParameter(1, Types.INTEGER);
      st.registerOutParameter(2, OracleTypes.CURSOR);
      st.registerOutParameter(3, Types.VARCHAR);
      st.execute();

      val result = st.getInt(1);
      if (result == 0) {
        try (ResultSet rs = ((OracleCallableStatement) st).getCursor(2)) {
          while (rs.next()) {
            typeMap.put(rs.getInt(1), rs.getString(2));
          }
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
    return typeMap;
  }

  /**
   * This method is used to save a user Type to a user.
   */
  public String saveUserType(long userID, int userTypeID) {
    String message = "";
    String sql = "begin ? := pkg_user_mgmt.f_assign_acct_type_2_user(?, ?, ?); end;";
    try (CallableStatement st = getDBTransaction().createCallableStatement(sql, DBTransaction.DEFAULT)) {
      st.registerOutParameter(1, Types.INTEGER);
      st.registerOutParameter(2, Types.VARCHAR);
      st.setLong(3, userID);
      st.setInt(4, userTypeID);
      st.executeUpdate();
      if (st.getInt(1) < 0) {
        message = st.getString(2);
      }
    }
    catch (SQLException e) {
      message = "Save User Type failed.";
      AdfLogUtility.logException(e);
    }
    return message;
  }

  /**
   * This method is used to delete a user Type to a user.
   */
  public String deleteUserType(long userID, int userTypeID) {
    String message = "";
    String sql = "begin ? := pkg_user_mgmt.f_remove_acct_type_from_user(?, ?, ?); end;";
    try (CallableStatement st = getDBTransaction().createCallableStatement(sql, DBTransaction.DEFAULT)) {
      st.registerOutParameter(1, Types.INTEGER);
      st.registerOutParameter(2, Types.VARCHAR);
      st.setLong(3, userID);
      st.setInt(4, userTypeID);
      st.executeUpdate();
      if (st.getInt(1) < 0) {
        message = st.getString(2);
      }
    }
    catch (SQLException e) {
      message = "Delete User Type failed.";
      AdfLogUtility.logException(e);
    }
    return message;
  }

  /**
   * This method is used to save a group to a user.
   */
  public String saveUserGroup(long userID, int userTypeID, int groupID, int uid) {
    String message = "";
    String sql = "begin ? := pkg_user_mgmt.f_assign_group_2_user(?, ?, ?, ?, ?); end;";
    try (CallableStatement st = getDBTransaction().createCallableStatement(sql, DBTransaction.DEFAULT)) {
      st.registerOutParameter(1, Types.INTEGER);
      st.registerOutParameter(2, Types.VARCHAR);
      st.setLong(3, userID);
      st.setInt(4, groupID);
      st.setInt(5, userTypeID);
      st.setInt(6, uid);
      st.executeUpdate();
      if (st.getInt(1) < 0) {
        message = st.getString(2);
        log.debug(message);
      }
    }
    catch (SQLException e) {
      message = "Save User Group failed.";
      AdfLogUtility.logException(e);
    }
    return message;
  }

  /**
   * This method is used to delete a group to a user.
   */
  public void deleteUserGroup(long userID, int userTypeID, int groupID, int uid) {
    String sql = "begin ? := pkg_user_mgmt.f_remove_group_from_user(?, ?, ?, ?, ?); end;";
    try (CallableStatement st = getDBTransaction().createCallableStatement(sql, DBTransaction.DEFAULT)) {
      st.registerOutParameter(1, Types.INTEGER);
      st.registerOutParameter(2, Types.VARCHAR);
      st.setLong(3, userID);
      st.setInt(4, groupID);
      st.setInt(5, userTypeID);
      st.setInt(6, uid);
      st.executeUpdate();
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
  }

  public boolean deleteUser(long userID) {
    boolean success = false;
    String sql = "begin ? := pkg_user_mgmt.f_delete_users(?, ?, ?, ?); end;";
    try (CallableStatement st = getDBTransaction().createCallableStatement(sql, DBTransaction.DEFAULT)) {
      st.registerOutParameter(1, Types.INTEGER);
      st.registerOutParameter(2, Types.VARCHAR);
      st.setLong(3, userID);
      st.setObject(4, null);
      st.setString(5, "L");
      st.executeUpdate();
      success = true;
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
    return success;
  }

  /**
   * This method is used to commit the database.
   */

  public int createUser(String firstName, String lastName, String middleName, String cacNumber, String status, int modId, StringBuilder sBuilder) {
    String sql = "begin ? := pkg_user_mgmt.f_create_user(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?); end;";
    int result = -1;
    try (CallableStatement st = getDBTransaction().createCallableStatement(sql, DBTransaction.DEFAULT)) {
      st.registerOutParameter(1, Types.INTEGER);
      st.registerOutParameter(2, Types.VARCHAR);
      st.setString(3, firstName);
      st.setString(4, lastName);
      st.setString(5, middleName);
      st.setString(6, cacNumber);
      st.setObject(7, null); //userId
      st.setString(8, status);
      st.setObject(9, null); //last_login
      st.setObject(10, null);
      st.setString(11, "Y"); //visible
      st.setString(12, "N"); //logged_in
      st.setObject(13, null); //temp_key
      st.setString(14, "N"); //logged_in_hh
      st.setObject(15, null); //temp_key_hh
      st.setObject(16, null); //last_login_hh
      st.setObject(17, "N"); //locked
      st.setObject(18, null); //startdate
      st.setObject(19, null); //enddate
      st.setObject(20, null); //transTS
      st.setInt(21, modId);

      st.executeUpdate();

      result = st.getInt(1);
      sBuilder.append(st.getString(2));
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
    return result;
  }

  /**
   * This method is used to commit the database.
   */
  public void dbCommit() {
    final String sql = "{call pkg_stratis_common.p_commit}";
    try (CallableStatement st = getDBTransaction().createCallableStatement(sql, DBTransaction.DEFAULT)) {
      st.execute();
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * This method checks if there is a FSR and returns a "" if there is none, or returns the Username if it does exist.
   */

  public String hasFSR() {
    String sql = "begin ? := pkg_user_mgmt.f_get_users_4_acct_type(?, ?, ?); end;";
    String fsrName = "";
    try (CallableStatement st = getDBTransaction().createCallableStatement(sql, DBTransaction.DEFAULT)) {
      st.registerOutParameter(1, Types.INTEGER);
      st.registerOutParameter(2, OracleTypes.CURSOR);
      st.registerOutParameter(3, Types.VARCHAR);
      st.setString(4, "FSR");
      st.executeUpdate();
      if (st.getInt(1) >= 0) {
        try (ResultSet rs = ((OracleCallableStatement) st).getCursor(2)) {
          if (rs.next()) {
            fsrName = rs.getString(1) + "," + rs.getString(2);
          }
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
    return fsrName;
  }

  @SuppressWarnings("java:S100") //(FUTURE) to rename this, PageDef xml would need updated.  and unsure of ADF impact.
  public void FilterUserData(String usernameQry, String lastNameQry) {
    try {
      CompleteUserListImpl view = getCompleteUserList1();
      view.applyViewCriteria(null);

      ViewCriteria vc = view.createViewCriteria();
      ViewCriteriaRow vcr = vc.createViewCriteriaRow();
      vcr.setUpperColumns(true);

      //If the UsernameQry value passed in by the user is not null
      if (usernameQry != null) {
        String username = Util.trimUpperCaseClean(usernameQry);
        //Select everything from the users table where
        //the Username attribute has the UsernameQry value anywhere in it
        vcr.setAttribute("Username", "like '" + username + "%'");
      }

      //If the LastNameQry value passed in by the user is not null
      if (lastNameQry != null) {
        String lastname = Util.trimUpperCaseClean(lastNameQry);
        //Select everything from the users table where
        //the LastName attribute has the LastNameQry value anywhere in it
        vcr.setAttribute("LastName", "like '" + lastname + "%'");
      }

      vc.addElement(vcr);
      view.applyViewCriteria(vc);
      view.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  @SuppressWarnings("unused")  //called from .jspx
  public void filterFloorData(String floorQry) {
    SingleFloorLocationViewImpl view;
    try {
      view = getSingleFloorLocationView1();
      view.applyViewCriteria(null);

      ViewCriteria vc = view.createViewCriteria();
      ViewCriteriaRow vcr = vc.createViewCriteriaRow();
      vcr.setUpperColumns(true);

      //If the FloorQry value passed in by the user is not null
      if (floorQry != null) {
        String floor = Util.trimUpperCaseClean(floorQry);
        if (isAlphaNumeric(floor))
          //Select everything from the floor_location table where
          //the FloorLocation attribute has the FloorQry value anywhere in it
          vcr.setAttribute("FloorLocation1", LIKE_PERCENT + floor + "%'");
      }

      vc.addElement(vcr);
      view.applyViewCriteria(vc);
      view.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /****************************************************************
   * This function is responsible for filtering the niininfo view from a page
   * *************************************************************/
  public void filterNIINData(String niinQry, String fscqry, String nomenclatureQry) {
    NiinInfoViewImpl view;
    try {
      view = getNiinInfoView1();

      // clear the last ones
      view.applyViewCriteria(null);

      ViewCriteria vc = view.createViewCriteria();
      ViewCriteriaRow vcr = vc.createViewCriteriaRow();
      vcr.setUpperColumns(true);

      //If the NIINQry value passed in by the user is not null
      if (niinQry != null) {
        String niin = Util.trimUpperCaseClean(niinQry);

        if (isAlphaNumeric(niin)) {

          //Select everything from the Niin_info table where
          //the Niin attribute has the NIINQry value anywhere in it
          vcr.setAttribute("Niin", LIKE_PERCENT + niin + "%'");
        }
      }

      //If the FSCQry value passed in by the user is not null
      if (fscqry != null) {
        String fsc = Util.trimUpperCaseClean(fscqry);
        if (isAlphaNumeric(fsc))
          //Select everything from the Niin_info table where
          //the FSC attribute has the FSCQry value anywhere in it
          vcr.setAttribute("Fsc", LIKE_PERCENT + fscqry + "%'");
      }

      if (nomenclatureQry != null) {
        String nomen = Util.trimUpperCaseClean(nomenclatureQry);
        vcr.setAttribute("Nomenclature", LIKE_PERCENT + nomen + "%'");
      }
      vc.addElement(vcr);
      //Apply the criteria
      view.applyViewCriteria(vc);
      //Execute the query
      view.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**************************************************************
   * This function is responsible for filtering the customer view by AAC
   * ***********************************************************/
  public void filterCustomerDataAAC(String aac) {
    CustomerViewImpl view;

    try {
      view = getCustomerView1();

      // clear the last ones
      view.applyViewCriteria(null);

      ViewCriteria vc = view.createViewCriteria();
      ViewCriteriaRow vcr = vc.createViewCriteriaRow();
      vcr.setUpperColumns(true);

      //Select everything from the Customer table where
      //the Aac attribute is equal to the AAC value
      //passed in by the user
      String aacFilter = Util.trimUpperCaseClean(aac);
      if (isAlphaNumeric(aacFilter))
        vcr.setAttribute("Aac", LIKE_PERCENT + aacFilter + "%'");

      vc.addElement(vcr);
      //Apply the criteria
      view.applyViewCriteria(vc);
      //Execute the query
      view.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /*****************************************************************
   * This function is responsible for filtering the location data being displayed
   * **************************************************************/
  public void filterLocationData(String wacID, String locationLabel) {
    LocationWacViewImpl view;

    try {
      view = getLocationViewWAC1();

      // clear the last ones
      view.applyViewCriteria(null);

      ViewCriteria vc = view.createViewCriteria();
      ViewCriteriaRow vcr = vc.createViewCriteriaRow();
      vcr.setUpperColumns(true);

      // If the Wac ID provided by the user is not null, or
      // a blank space
      if ((wacID != null) && (!wacID.equals(""))) {
        log.debug("filtering wac id {}", wacID);
        //Select everything from the location and
        //location_classification tables where the WacId attribute
        //is equal to the Integer value of the WacID value passed
        //in by the user
        vcr.setAttribute("WacId", "= " + Integer.parseInt(wacID));
      }

      // if the location isn't null add that filter too
      if (locationLabel != null) {
        log.debug("filtering location label  {}", locationLabel);
        String location = Util.trimUpperCaseClean(locationLabel);
        if (isAlphaNumeric(location))
          //Select everything from the location and
          //location_classification tables where the Location Label
          //attribute is equal to the LocationLabel value passed
          //in by the user
          vcr.setAttribute("LocationLabel", LIKE_PERCENT + location + "%'");
      }

      vc.addElement(vcr);
      //Apply the criteria
      view.applyViewCriteria(vc);
      //Execute the query
      view.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /*******************************************************************
   * This function is responsible for filtering the NIIN data for the Change CC table
   * ****************************************************************/
  public void filterNIINCC(String location, String niin, String nomenclature) {
    NiinLocLocationViewImpl view;
    try {
      view = getNiinLocLocationView1();

      // clear the last ones
      view.applyViewCriteria(null);

      ViewCriteria vc = view.createViewCriteria();
      ViewCriteriaRow vcr = vc.createViewCriteriaRow();
      vcr.setUpperColumns(true);

      //If the NIIN value passed in by the user is not null

      if (niin != null) {
        //Select everything from the NiinInfo table
        //where the Niin attribute contains the NIIN
        //value passed in by the user anywhere
        String niinFilter = Util.trimUpperCaseClean(niin);
        if (isAlphaNumeric(niinFilter))
          vcr.setAttribute("Niin", LIKE_PERCENT + niinFilter + "%'");
      }

      //If the FSC value passed in by the user is not null
      if (location != null) {
        String locationFilter = Util.trimUpperCaseClean(location);
        if (isAlphaNumeric(locationFilter))
          //Select everything from the NiinInfo table
          //where the LocationLabel attribute contains the Location
          //value passed in by the user anywhere
          vcr.setAttribute("LocationLabel", LIKE_PERCENT + locationFilter + "%'");
      }

      //If the Nomenclature value passed in by the user is
      //not null
      if (nomenclature != null) {
        //Select everything from the NiinInfo table
        //where the Nomenclature attribute contains
        //the Nomenclature value passed in by the
        //user anywhere
        String nomenFilter = Util.trimUpperCaseClean(nomenclature);
        if (isAlphaNumeric(nomenFilter))
          vcr.setAttribute("Nomenclature", LIKE_PERCENT + nomenFilter + "%'");
      }

      vc.addElement(vcr);
      //Apply the criteria
      view.applyViewCriteria(vc);
      //Execute the query
      view.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  @SuppressWarnings("unused") //called from .jspx
  public void filterNIINRecon(String niin, String nomenclature) {
    log.trace("NIIN: {} Nomen: {}", niin, nomenclature);

    NiinInfoCcChangeViewImpl view;
    try {

      view = getNiinInfoView_CC_Change3();
      // clear the last ones
      view.applyViewCriteria(null);

      ViewCriteria vc = view.createViewCriteria();
      ViewCriteriaRow vcr = vc.createViewCriteriaRow();
      vcr.setUpperColumns(true);

      //If the NIIN value passed in by the user is not null
      if (niin != null) {
        String niinFilter = Util.trimUpperCaseClean(niin);
        if (isAlphaNumeric(niinFilter))
          vcr.setAttribute("Niin", LIKE_PERCENT + niinFilter + "%'");
      }

      //If the Nomenclature value passed in by the user is
      //not null
      if (nomenclature != null) {
        String nomenFilter = Util.trimUpperCaseClean(nomenclature);
        if (isAlphaNumeric(nomenFilter))
          vcr.setAttribute("Nomenclature", LIKE_PERCENT + nomenFilter + "%'");
      }

      vc.addElement(vcr);
      view.applyViewCriteria(vc);
      view.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public void filterNIINInquiry(String niin, String nomenclature) {
    NiinInfoCcChangeViewImpl view;
    log.trace("NIIN: {} Nomen: {}", niin, nomenclature);
    try {
      view = getNiinInfoView_CC_Change2();

      // clear the last ones
      view.applyViewCriteria(null);

      ViewCriteria vc = view.createViewCriteria();
      ViewCriteriaRow vcr = vc.createViewCriteriaRow();
      vcr.setUpperColumns(true);

      //If the NIIN value passed in by the user is not null
      if (niin != null) {
        String niinFilter = Util.trimUpperCaseClean(niin);
        if (isAlphaNumeric(niinFilter))
          vcr.setAttribute("Niin", LIKE_PERCENT + niinFilter + "%'");
      }

      //If the Nomenclature value passed in by the user is
      //not null
      if (nomenclature != null) {
        String nomenFilter = Util.trimUpperCaseClean(nomenclature);
        vcr.setAttribute("Nomenclature", LIKE_PERCENT + nomenFilter + "%'");
      }

      vc.addElement(vcr);
      view.applyViewCriteria(vc);
      view.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  @SuppressWarnings({"java:S100", "unused"}) //(FUTURE) to rename this, PageDef xml would need updated.  and unsure of ADF impact.
  public void filterNIINInquiry_AddLocation(String location, String secureFlag) {

    LocationViewImpl view;

    try {
      view = getLocationView1();

      // clear the last ones
      view.applyViewCriteria(null);
      ViewCriteria vc = view.createViewCriteria();
      ViewCriteriaRow vcr = vc.createViewCriteriaRow();
      vcr.setUpperColumns(true);

      //If the NIIN value passed in by the user is not null
      if (location != null) {
        String locationFilter = Util.trimUpperCaseClean(location);
        if (isAlphaNumeric(locationFilter))
          vcr.setAttribute("LocationLabel", LIKE_PERCENT + locationFilter + "%'");
      }

      //If the Nomenclature value passed in by the user is
      //not null
      if (secureFlag != null) {
        vcr.setAttribute("SecureFlag", LIKE_PERCENT + secureFlag + "%'");
      }

      vc.addElement(vcr);
      view.applyViewCriteria(vc);
      view.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**************************************************************
   * This function is responsible for creating a range of bulk locations
   * ***********************************************************/
  public void createBulkLocations(int wacId, int numRows, int numShelfGroups,
                                  int locationsPerShelfGroup,
                                  int numLevels) {

    // find the first row in the given wac
    String wacLabel = "";

    // get all the wac's for this warehouse
    try (PreparedStatement wacps = getDBTransaction().createPreparedStatement("select wac_number from wac where wac_id = ?", 0)) {

      wacps.setObject(1, wacId);

      try (ResultSet wacrs = wacps.executeQuery()) {
        if (wacrs.next()) {
          wacLabel = wacrs.getString(1);
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }

    int startRow = 0;

    try (PreparedStatement rowcountps = getDBTransaction().createPreparedStatement("select location_label from location where wac_id = ?", 0)) {

      rowcountps.setObject(1, wacId);

      try (ResultSet rowcountrs = rowcountps.executeQuery()) {
        while (rowcountrs.next()) {
          Object thisrow = rowcountrs.getObject(1);

          // break apart the string for chars 4 and 5
          char[] crow = {'0', '0'};
          crow[0] = thisrow.toString().charAt(3);
          crow[1] = thisrow.toString().charAt(4);

          // turn var into string
          String crowstr = "";

          crowstr += crow[0];
          crowstr += crow[1];

          // is this higher than what we have
          if (Integer.parseInt(crowstr) > startRow) {
            // get this one
            startRow = Integer.parseInt(crowstr);
          }
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }

    try {
      // increment the startrow for the next row
      startRow++;

      processCreateBulkLocations(startRow, numRows, numShelfGroups, numLevels, locationsPerShelfGroup, wacId, wacLabel);

      // commit the changes to the database
      this.getDBTransaction().commit();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  private void processCreateBulkLocations(int startRow, int numRows, int numShelfGroups, int numLevels, int locationsPerShelfGroup,
                                          int wacId, String wacLabel) {
    // loop all rows
    for (int row = startRow; row < numRows + startRow; row++) {
      String baseLocLabel = "" + wacLabel;
      String baseBayLabel = "";
      if (row < 10) {
        baseLocLabel += "0";
        baseBayLabel += "0";
      }
      // loop all shelf groups
      for (int shelfgroup = 0; shelfgroup < numShelfGroups;
           shelfgroup++) {
        String shelfGroupLocLabel = "";
        String shelfGroupLocLevelLabel = "";
        if (shelfgroup < 10) {
          shelfGroupLocLabel += "0";
          shelfGroupLocLevelLabel += "0";
        }
        shelfGroupLocLabel += Integer.toString(shelfgroup);
        shelfGroupLocLevelLabel += Integer.toString(shelfgroup);
        // loop each level
        for (int level = 0; level < numLevels; level++) {

          // loop each location
          for (int location = 0;
               location < locationsPerShelfGroup; location++) {

            // build the location label

            String slotLabel = "";
            String locLabel = baseLocLabel + row + shelfGroupLocLabel;
            String bayLabel = baseBayLabel + row;

            locLabel += "A" + level;
            slotLabel += "A" + level;

            locLabel += "A" + location;
            slotLabel += "A" + location;

            insertLocation(wacId, locLabel, bayLabel, shelfGroupLocLevelLabel, slotLabel, row);
          }
        }
      }
    }
  }

  private void insertLocation(int wacId, String locLabel, String bayLabel, String shelfGroupLocLevelLabel, String slotLabel, int row) {
    try (PreparedStatement locps =
             getDBTransaction().createPreparedStatement("insert into location (WAC_ID, Availability_Flag, Mechanized_Flag, Loc_Classification_Id, Divider_Index, Side, Location_Label, Bay, Loc_Level, Slot, Aisle, created_by) values(?,?,?,?,?,?,?,?,?,?,?,?)",
                 0)) {

      locps.setObject(1, wacId);
      locps.setObject(2, "A");
      locps.setObject(3, "N");
      locps.setObject(4, null);
      locps.setObject(5, 0);
      locps.setObject(6, "A");
      locps.setObject(7, locLabel);
      locps.setObject(8, bayLabel);
      locps.setObject(9, shelfGroupLocLevelLabel);
      locps.setObject(10, slotLabel);
      locps.setObject(11, row);
      locps.setObject(12, userId);

      // run the insert
      locps.execute();
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
  }

  public void addOneBulkLocation(int wacId, String locLabel) {

    if (Util.isEmpty(locLabel)) {
      return;
    }

    String bay = locLabel.substring(3, 5);
    String locLevel = locLabel.substring(5, 7);
    String slot = locLabel.substring(7, 9);

    try (PreparedStatement locps = getDBTransaction().createPreparedStatement("insert into location (WAC_ID, Availability_Flag, Mechanized_Flag, Loc_Classification_Id, Divider_Index, Side, Location_Label, Bay, Loc_Level, Slot, Aisle, created_by) values(?,?,?,?,?,?,?,?,?,?,?,?)", 0)) {

      locps.setObject(1, wacId);
      locps.setObject(2, "A");
      locps.setObject(3, "N");
      locps.setObject(4, null);
      locps.setObject(5, 0);
      locps.setObject(6, "A");
      locps.setObject(7, locLabel);
      locps.setObject(8, bay);
      locps.setObject(9, locLevel);
      locps.setObject(10, slot);
      locps.setObject(11, locLevel);
      locps.setObject(12, userId);

      // run the insert
      locps.execute();
      getDBTransaction().commit();
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
      getDBTransaction().rollback();
    }
  }

  public void extendBulkLocations(int wacId, String beginLoc, String endLoc) {

    try {
      if (Util.isEmpty(beginLoc) || Util.isEmpty(endLoc)) {
        return;
      }

      String expectedWACLabel = "";
      String foundBeginWacLabel = beginLoc.substring(0, 3);
      String foundEndWacLabel = endLoc.substring(0, 3);

      // get all the wac's for this warehouse
      try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement("select wac_number from wac where wac_id = ?", 0)) {
        pstmt.setObject(1, wacId);
        try (ResultSet rs = pstmt.executeQuery()) {
          if (rs.next()) {
            expectedWACLabel = rs.getString(1);
          }
        }

        // WAC Label do not match WAC
        if (!expectedWACLabel.equalsIgnoreCase(foundBeginWacLabel) && !expectedWACLabel.equalsIgnoreCase(foundEndWacLabel))
          return;
        String beginRange = beginLoc.substring(7, 8);
        String beginRange2 = beginLoc.substring(8, 9);
        log.trace("{} _ {}", beginRange, beginRange2);
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
  }

  // function to add to the temp view for creating a carousel
  public String getImageSource(String locationType) {
    val loview = getLocationClassificationView1();

    // add a filter
    val vc = loview.createViewCriteria();
    ViewCriteriaRow vcr = vc.createViewCriteriaRow();
    vcr.setUpperColumns(true);

    // add the query
    vcr.setAttribute("LocClassificationId", "= " + locationType);
    vc.addElement(vcr);

    loview.applyViewCriteria(vc);
    // get the results
    loview.executeQuery();
    if (loview.hasNext()) {
      Row locRow = loview.getCurrentRow();
      return locRow.getAttribute(IMAGE_FILENAME).toString();
    }
    else {
      return "NoImage.jpg";
    }
  }

  @SuppressWarnings("unused")  //called from .jspx
  public void addNewCarouselLevel(int locationType) {
    // get the imagename and the divider type for this type to add to the temp view
    LocationClassificationViewImpl loview;

    loview = getLocationClassificationView1();

    // add a filter
    ViewCriteria vc = loview.createViewCriteria();
    ViewCriteriaRow vcr = vc.createViewCriteriaRow();
    vcr.setUpperColumns(true);

    // add the query
    vcr.setAttribute("LocClassificationId", "= " + locationType);
    vc.addElement(vcr);

    loview.applyViewCriteria(vc);
    // get the results
    loview.executeQuery();
    Row locRow = loview.getCurrentRow();

    String binImage = "";
    String dividerType = "";
    String name = "";
    String length = "";
    String width = "";
    String height = "";

    // get the info if we have something
    if (locRow != null) {
      if (locRow.getAttribute(IMAGE_FILENAME) != null) {
        binImage = locRow.getAttribute(IMAGE_FILENAME).toString();
      }

      if (locRow.getAttribute(DIVIDER_TYPE_ID) != null) {
        dividerType =
            locRow.getAttribute(DIVIDER_TYPE_ID).toString();
      }

      if (locRow.getAttribute("Name") != null) {
        name = locRow.getAttribute("Name").toString();
      }
      if (locRow.getAttribute(LENGTH) != null) {
        length = locRow.getAttribute(LENGTH).toString();
      }
      if (locRow.getAttribute(WIDTH) != null) {
        width = locRow.getAttribute(WIDTH).toString();
      }
      if (locRow.getAttribute(HEIGHT) != null) {
        height = locRow.getAttribute(HEIGHT).toString();
      }
    }

    // clear the filter
    loview.applyViewCriteria(null);
    loview.executeQuery();

    // check if we have reach the max locations per bin of 26

    TempMechCreateRowsImpl view = getTempMechCreateRows1();

    Row row = view.createRow();

    // fill the fields
    row.setAttribute("LocationType", locationType);
    row.setAttribute("Name", name);
    row.setAttribute(LENGTH, length);
    row.setAttribute(WIDTH, width);
    row.setAttribute(HEIGHT, height);
    row.setAttribute("BinImage", binImage);
    row.setAttribute("DivideId", dividerType);

    // add this to the view
    int size = view.getRowCountInRange();
    view.insertRowAtRangeIndex(size, row);
  }

  public boolean checkMaxLocations(int width, String dividerName) {
    boolean retVal = false;
    try {
      try (PreparedStatement divslotps = getDBTransaction().createPreparedStatement("select count(*) from divider_slots where divider_type_id in (Select divider_type_id from divider_type where name = ?)", 0)) {
        divslotps.setString(1, dividerName);
        int slotcount = -1;

        try (ResultSet divslotrs = divslotps.executeQuery()) {
          if (divslotrs.next()) {
            // get the slot count
            slotcount = divslotrs.getInt(1);
          }
        }

        int locQty = (25 / width) * slotcount;
        if (locQty < 37) {
          retVal = true;
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return retVal;
  }

  public void removeNewCarouselLevel() {
    // get the imagename and the divider type for this type to add to the temp view
    try {
      TempMechCreateRowsImpl view = getTempMechCreateRows1();
      int size = view.getRowCountInRange();
      if (size > 0) {
        Row row = view.getRowAtRangeIndex(size - 1);
        row.remove();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  // function to add to the temp view for creating a carousel

  @SuppressWarnings("unused")  //called from .jspx
  public void addNewVerticalCarouselLevel(int locationType) {
    // get the imagename and the divider type for this type to add to the temp view
    LocationClassificationViewImpl loview;
    TempMechCreateRowsImpl view;

    try {

      loview = getLocationClassificationView1();

      // add a filter
      ViewCriteria vc = loview.createViewCriteria();
      ViewCriteriaRow vcr = vc.createViewCriteriaRow();
      vcr.setUpperColumns(true);

      String binImage = "";
      String dividerType = "";
      String name = "";
      String length = "";
      String width = "";
      String height = "";

      // add the query
      vcr.setAttribute("LocClassificationId", "= " + (locationType));
      vc.addElement(vcr);

      loview.applyViewCriteria(vc);
      // get the results
      loview.executeQuery();

      Row locRow = loview.getCurrentRow();
      // get the info if we have something
      if (locRow != null) {
        if (locRow.getAttribute(IMAGE_FILENAME) != null) {
          binImage = locRow.getAttribute(IMAGE_FILENAME).toString();
        }

        if (locRow.getAttribute(DIVIDER_TYPE_ID) != null) {
          dividerType =
              locRow.getAttribute(DIVIDER_TYPE_ID).toString();
        }
        if (locRow.getAttribute("Name") != null) {
          name = locRow.getAttribute("Name").toString();
        }
        if (locRow.getAttribute(LENGTH) != null) {
          length = locRow.getAttribute(LENGTH).toString();
        }
        if (locRow.getAttribute(WIDTH) != null) {
          width = locRow.getAttribute(WIDTH).toString();
        }
        if (locRow.getAttribute(HEIGHT) != null) {
          height = locRow.getAttribute(HEIGHT).toString();
        }
      }

      // clear the filter
      loview.applyViewCriteria(null);
      loview.executeQuery();

      // get the view and add a new row to it

      view = getTempMechCreateRows2();
      Row row = view.createRow();

      // fill the fields
      row.setAttribute("LocationType", locationType);
      row.setAttribute("Name", name);
      row.setAttribute(LENGTH, length);
      row.setAttribute(WIDTH, width);
      row.setAttribute(HEIGHT, height);
      row.setAttribute("BinImage", binImage);
      row.setAttribute("DivideId", dividerType);

      // add this to the view
      int size = view.getRowCountInRange();
      view.insertRowAtRangeIndex(size, row);
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  @SuppressWarnings("unused")  //called from .jspx
  public void removeNewVerticalCarouselLevel() {
    // get the imagename and the divider type for this type to add to the temp view
    try {
      TempMechCreateRowsImpl view = getTempMechCreateRows2();
      int size = view.getRowCountInRange();
      if (size > 0) {
        Row row = view.getRowAtRangeIndex(size - 1);
        row.remove();
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }
  // function to return all boxes for a given divider type

  public void returnDividerTypeBoxes(List<String> xList, List<String> yList, List<String> xLength, List<String> yLength,
                                     List<String> index, int dividerType, List<String> displays) {
    // ensure the lists are empty
    xList.clear();
    yList.clear();
    xLength.clear();
    yLength.clear();
    index.clear();

    // get the view
    DividerSlotsByTypeViewImpl view = getDividerSlotsByTypeView1();

    // update the bind var
    view.setNamedWhereClauseParam("SlotType", dividerType);
    view.setRangeSize(-1);
    view.executeQuery();
    Row[] rows = view.getAllRowsInRange();

    for (Row row : rows) {
      Object xObj = row.getAttribute("RowNumber");
      Object yObj = row.getAttribute("ColumnNumber");
      Object xlObj = row.getAttribute(LENGTH);
      Object ylObj = row.getAttribute(WIDTH);
      Object iObj = row.getAttribute("SelectIndex");
      Object displayObj = row.getAttribute("DisplayPosition");

      xList.add(xObj != null ? xObj.toString() : "0");
      yList.add(yObj != null ? yObj.toString() : "0");
      xLength.add(xlObj != null ? xlObj.toString() : "0");
      yLength.add(ylObj != null ? ylObj.toString() : "0");
      index.add(iObj != null ? iObj.toString() : "0");
      displays.add(displayObj != null ? displayObj.toString() : "0");
    }
  }

  // function to return the divide types with there ids

  public void getDivideTypes(List<String> id, List<String> name) {
    DividerTypeViewImpl view = getDividerTypeView1();
    int rowcount = view.getRangeSize();
    view.setRangeSize(-1);
    view.executeQuery();

    Row[] rows = view.getAllRowsInRange();
    for (Row row : rows) {
      Object idobj = row.getAttribute(DIVIDER_TYPE_ID);
      Object naobj = row.getAttribute("Name");

      if ((idobj != null) && (naobj != null)) {
        id.add(idobj.toString());
        name.add(naobj.toString());
      }
    }

    // put the size back
    view.setRangeSize(rowcount);
    view.executeQuery();
  }

  public int createDividerSlot(String dividerTypeId, String position,
                               String column, String row, String length,
                               String width) {
    int result = 0;
    String sql = "select count(*) from location_classification where divider_type_id=?";
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {
      pstmt.setString(1, dividerTypeId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          int i = rs.getInt(1);
          if (i > 0) {
            result = -2;
            return result;
          }
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }

    try {
      DividerSlotsViewImpl d = getDividerSlotsView2();
      long count = d.getEstimatedRowCount();
      Row r = d.createRow();
      r.setAttribute(DIVIDER_TYPE_ID, dividerTypeId);
      r.setAttribute("SelectIndex", count);
      r.setAttribute("DisplayPosition", position);
      r.setAttribute("ColumnNumber", column);
      r.setAttribute("RowNumber", row);
      r.setAttribute(LENGTH, length);
      r.setAttribute(WIDTH, width);
      d.insertRow(r);
      getDBTransaction().commit();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      getDBTransaction().rollback();
      result = -1;
    }

    return result;
  }

  public void returnAllLocationClassifications(List<String> ids, List<String> files) {

    LocationClassificationViewImpl vo;

    try {
      // ensure the lists are clear
      ids.clear();
      files.clear();

      vo = getLocationClassificationView1();
      vo.setRangeSize(-1);
      vo.executeQuery();

      Row[] rows = vo.getAllRowsInRange();
      for (Row row : rows) {
        Object dividerTypeId = row.getAttribute(DIVIDER_TYPE_ID);
        if (!Util.isEmpty(dividerTypeId)) {
          Object fnObj = row.getAttribute(IMAGE_FILENAME);
          Object nmObj = row.getAttribute("Name");
          Object dsObj = row.getAttribute("Description");

          // add these to the lists
          if ((nmObj != null) && (dsObj != null)) {
            ids.add(nmObj + " " + dsObj);
          }
          else if (nmObj != null) {
            ids.add(nmObj.toString());
          }
          else {
            ids.add(dsObj.toString());
          }

          if (fnObj != null)
            files.add((fnObj.toString()));
          else
            files.add("");
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  // function to return all the buildings and wacs with there id's

  public void returnBuildingWACList(List<Object> id, List<String> display, boolean isMech) {
    // ensure the lists are clear
    id.clear();
    display.clear();

    // just use a prepared statment for this work
    // get all the pick_qty from the picking table
    String pstring;

    if (isMech) {
      pstring =
          "select w.wac_id, wa.building, w.wac_number from wac w, warehouse wa where (w.warehouse_id = wa.warehouse_id) and ((w.mechanized_flag = 'H') OR (w.mechanized_flag = 'V'))";
    }
    else {
      pstring =
          "select w.wac_id, wa.building, w.wac_number from wac w, warehouse wa where (w.warehouse_id = wa.warehouse_id) and (w.mechanized_flag = 'N')";
    }

    try (PreparedStatement stR = getDBTransaction().createPreparedStatement(pstring, 0)) {
      try (ResultSet rs = stR.executeQuery()) {
        while (rs.next()) {
          id.add(rs.getInt(1));
          display.add(rs.getString(2) + " - " + rs.getString(3));
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
  }

  // function to return all the buildings and wacs with there id's

  public void returnBuildingWACMechList(List<Object> id, List<String> display,
                                        boolean isHorizontal) {

    // ensure the lists are clear
    id.clear();
    display.clear();

    String sql;
    if (isHorizontal) {
      sql =
          "select wac_id, wac_number from wac where wac_id in" +
              "  ((select wac_id from wac where mechanized_flag = 'H'" +
              "     minus" +
              "    select wac_id from location)" +
              "   union" +
              "   select wac_id from location where mechanized_flag = 'H'" +
              "   group by wac_Id having count(distinct side) = 1" + ")";
    }
    else {
      sql =
          "select wac_id, wac_number from wac x where mechanized_flag = 'V' and " +
              "not exists (select null from location where wac_id = x.wac_id)";
    }
    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sql, 0)) {

      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          id.add(rs.getInt(1));
          display.add(rs.getString(2));
        }
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
  }

  public WorkLoadManagerImpl getWorkLoadManagerService() {
    WorkLoadManagerImpl service = null;
    try {
      service = (WorkLoadManagerImpl) getWorkLoadManager1();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return service;
  }

  public ApplicationModuleImpl getWorkLoadManager1() {
    return (ApplicationModuleImpl) findApplicationModule("WorkLoadManager1");
  }

  public GCSSMCTransactionsImpl getGCSSMCTransactionsService() {
    try {
      return getTransactionsService().getGCSSMCTransactions1();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  public TransactionsImpl getTransactionsService() {
    try {
      return (TransactionsImpl) getWorkLoadManagerService().getTransactions1();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return null;
  }

  // function to filter the change cc results

  public void filterChangeCC(String cc, String filterDate) {
    NiinInfoCcChangeViewImpl fromView;
    mil.stratis.model.view.loc.NiinLocLocationViewImpl view;

    try {
      fromView = getNiinInfoView_CC_Change1();

      Row currentRow = fromView.getCurrentRow();
      view = getNiinLoc_LocationView1();

      String niin = currentRow.getAttribute("NiinId").toString();

      ViewCriteria crit = view.createViewCriteria();
      ViewCriteriaRow critRow = crit.createViewCriteriaRow();
      critRow.setUpperColumns(true);

      if (!Util.isEmpty(cc)) {
        // update the search prams
        critRow.setAttribute("Cc", "=" + "'" + cc + "'");
      }
      if (!filterDate.equals("")) {
        Date expire = Date.valueOf(filterDate);
        critRow.setAttribute("ExpirationDate", "=" + "'" + expire + "'");
      }

      critRow.setAttribute("NiinId", "=" + niin);

      crit.addElement(critRow);
      view.applyViewCriteria(crit);
      view.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  /**
   * Check Condition Code for NIIN will return true if multiple niin are found at a location
   *
   * @return boolean
   */
  public boolean checkCCNIIN(String locationId, String ccFrom) {
    boolean foundMore = false;
    String sqlClear = "select count(*) from niin_location where location_id=? and cc=?";

    try (PreparedStatement pstmt = getDBTransaction().createPreparedStatement(sqlClear, 0)) {

      //* how many are at the same location with the old condition code
      pstmt.setString(1, locationId);
      pstmt.setString(2, ccFrom);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next())
          foundMore = rs.getInt(1) > 1;
      }
    }
    catch (SQLException e) {
      AdfLogUtility.logException(e);
    }
    return foundMore;
  }

  // function to clear all the rows in the create carosel temp table
  public void clearCarouselRow() {
    try {
      TempMechCreateRowsImpl view = getTempMechCreateRows1();

      // clear the rows out
      view.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
  }

  public int sendDACGCSSMCTransaction(int niinid, int niinlocid, String oldCC,
                                      Object serialNumber, Object lotControlNumber, Object qty) {
    GCSSMCTransactionsImpl gcssTrans = getGCSSMCTransactionsService();
    return gcssTrans.sendDACGCSSMCTransaction(niinid, niinlocid, oldCC, serialNumber, lotControlNumber, qty);
  }

  public int sendDACGCSSMCTransactionsSerial(int niinid, int niinlocid, int locid, String oldCC, String newCC) {
    GCSSMCTransactionsImpl gcssTrans = getGCSSMCTransactionsService();
    try {
      int result = changeCCSerialTrack(niinid, locid, newCC);
      //Change CC worked, send out the DACs.
      if (result == 0) {
        result = gcssTrans.sendDACGCSSMCTransaction(niinid, niinlocid, oldCC, "Y", null, 1);
        //Something happened in the DAC, reverse the CC changes!
        if (result != 0) {
          changeCCSerialTrack(niinid, locid, oldCC);
          return -1;
        }
        else {
          return 0;
        }
      }
      else {
        //Something happened during the switch, return failure!
        return -1;
      }
    }
    catch (Exception e) {
      //Something happened during DAC, reverse the CC changes!
      changeCCSerialTrack(niinid, locid, oldCC);
    }
    return -1;
  }

  public int changeCCSerialTrack(int niinid, int locid, String newCC) {
    try (PreparedStatement stR = getDBTransaction().createPreparedStatement("update serial_lot_num_track set cc = ? where niin_id = ? and location_id = ?", 0)) {
      stR.setString(1, newCC);
      stR.setInt(2, niinid);
      stR.setInt(3, locid);
      stR.executeUpdate();
      getDBTransaction().commit();
      return 0;
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
      return -1;
    }
  }

  public String getSiteGCCSSMCFlag() {
    String flag = "N";
    try (PreparedStatement stR = getDBTransaction().createPreparedStatement("select nvl(GCSS_MC,'N') from site_info", 0)) {
      try (ResultSet rs = stR.executeQuery()) {
        if (rs.next()) {
          flag = rs.getString(1);
        }
      }
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return flag;
  }

  /**
   * Used by Change Condition Code
   */
  public int getLocations() {
    NiinLocLocationViewImpl toView;
    NiinInfoCcChangeViewImpl fromView;

    try {
      fromView = getNiinInfoView_CC_Change1();

      toView = getNiinLoc_LocationView1();

      Row currentRow = fromView.getCurrentRow();

      String niin = currentRow.getAttribute("NiinId").toString();

      toView.applyViewCriteria(null);
      ViewCriteria crit = toView.createViewCriteria();
      ViewCriteriaRow critRow = crit.createViewCriteriaRow();

      critRow.setAttribute("NiinId", "=" + "'" + niin + "'");
      crit.addElement(critRow);
      toView.applyViewCriteria(crit);

      toView.executeQuery();
    }
    catch (Exception e) {
      AdfLogUtility.logException(e);
    }
    return 1;
  }

  /**
   * Container's getter for AllComPortView1.
   *
   * @return AllComPortView1
   */
  public ViewObjectImpl getAllComPortView1() {
    return (ViewObjectImpl) findViewObject("AllComPortView1");
  }

  /**
   * Container's getter for AllPrintPortView1.
   *
   * @return AllPrintPortView1
   */
  public ViewObjectImpl getAllPrintPortView1() {
    return (ViewObjectImpl) findViewObject("AllPrintPortView1");
  }

  /**
   * Container's getter for CompleteUserList1.
   *
   * @return CompleteUserList1
   */
  public CompleteUserListImpl getCompleteUserList1() {
    return (CompleteUserListImpl) findViewObject("CompleteUserList1");
  }

  /**
   * Container's getter for CompleteUserList2.
   *
   * @return CompleteUserList2
   */
  public CompleteUserListImpl getCompleteUserList2() {
    return (CompleteUserListImpl) findViewObject("CompleteUserList2");
  }

  /**
   * Container's getter for ComPortView1.
   *
   * @return ComPortView1
   */
  public ViewObjectImpl getComPortView1() {
    return (ViewObjectImpl) findViewObject("ComPortView1");
  }

  /**
   * Container's getter for ComPortView1_Workstation.
   *
   * @return ComPortView1_Workstation
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage xml would need updated.  and unsure of ADF impact.
  public ViewObjectImpl getComPortView1_Workstation() {
    return (ViewObjectImpl) findViewObject("ComPortView1_Workstation");
  }

  /**
   * Container's getter for CustomerView1.
   *
   * @return CustomerView1
   */
  public CustomerViewImpl getCustomerView1() {
    return (CustomerViewImpl) findViewObject("CustomerView1");
  }

  /**
   * Container's getter for DividerSlotsView1.
   *
   * @return DividerSlotsView1
   */
  public DividerSlotsViewImpl getDividerSlotsView1() {
    return (DividerSlotsViewImpl) findViewObject("DividerSlotsView1");
  }

  /**
   * Container's getter for DividerTypeView1.
   *
   * @return DividerTypeView1
   */
  public DividerTypeViewImpl getDividerTypeView1() {
    return (DividerTypeViewImpl) findViewObject("DividerTypeView1");
  }

  /**
   * Container's getter for DividerTypeView1_StorageBin.
   *
   * @return DividerTypeView1_StorageBin
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage xml would need updated.  and unsure of ADF impact.
  public DividerTypeViewImpl getDividerTypeView1_StorageBin() {
    return (DividerTypeViewImpl) findViewObject("DividerTypeView1_StorageBin");
  }

  /**
   * Container's getter for EquipPackGroupView1.
   *
   * @return EquipPackGroupView1
   */
  public ViewObjectImpl getEquipPackGroupView1() {
    return (ViewObjectImpl) findViewObject("EquipPackGroupView1");
  }

  /**
   * Container's getter for EquipView1.
   *
   * @return EquipView1
   */
  public EquipViewImpl getEquipView1() {
    return (EquipViewImpl) findViewObject("EquipView1");
  }

  /**
   * Container's getter for FloorLocationView1.
   *
   * @return FloorLocationView1
   */
  public ViewObjectImpl getFloorLocationView1() {
    return (ViewObjectImpl) findViewObject("FloorLocationView1");
  }

  /**
   * Container's getter for FloorLocationView1_WarehouseSetup.
   *
   * @return FloorLocationView1_WarehouseSetup
   */

  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage xml would need updated.  and unsure of ADF impact.
  public ViewObjectImpl getFloorLocationView1_WarehouseSetup() {
    return (ViewObjectImpl) findViewObject("FloorLocationView1_WarehouseSetup");
  }

  /**
   * Container's getter for GroupView1.
   *
   * @return GroupView1
   */
  public ViewObjectImpl getGroupView1() {
    return (ViewObjectImpl) findViewObject("GroupView1");
  }

  /**
   * Container's getter for GroupViewAll2.
   *
   * @return GroupViewAll2
   */
  public ViewObjectImpl getGroupViewAll2() {
    return (ViewObjectImpl) findViewObject("GroupViewAll2");
  }

  /**
   * Container's getter for UserViewById1.
   *
   * @return UserViewById1
   */
  public ViewObjectImpl getUserViewById1() {
    return (ViewObjectImpl) findViewObject("UserViewById1");
  }

  /**
   * Container's getter for LocationClassification_StorageBinView1.
   *
   * @return LocationClassification_StorageBinView1
   */

  @SuppressWarnings("java:S100") //(FUTURE) to rename this, PageDef xml would need updated.  and unsure of ADF impact.
  public ViewObjectImpl getLocationClassification_StorageBinView1() {
    return (ViewObjectImpl) findViewObject("LocationClassification_StorageBinView1");
  }

  /**
   * Container's getter for LocationClassificationView1.
   *
   * @return LocationClassificationView1
   */
  public LocationClassificationViewImpl getLocationClassificationView1() {
    return (LocationClassificationViewImpl) findViewObject("LocationClassificationView1");
  }

  /**
   * Container's getter for LocationView1.
   *
   * @return LocationView1
   */
  public LocationViewImpl getLocationView1() {
    return (LocationViewImpl) findViewObject("LocationView1");
  }

  /**
   * Container's getter for LocationViewWAC1.
   *
   * @return LocationViewWAC1
   */
  public LocationWacViewImpl getLocationViewWAC1() {
    return (LocationWacViewImpl) findViewObject("LocationViewWAC1");
  }

  /**
   * Container's getter for MasterDropView_SC1.
   *
   * @return MasterDropView_SC1
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, PageDef xml would need updated.  and unsure of ADF impact.
  public MasterDropView_SCImpl getMasterDropView_SC1() {
    return (MasterDropView_SCImpl) findViewObject("MasterDropView_SC1");
  }

  /**
   * Container's getter for MasterDropView_SCC1.
   *
   * @return MasterDropView_SCC1
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, PageDef xml would need updated.  and unsure of ADF impact.
  public MasterDropView_SCCImpl getMasterDropView_SCC1() {
    return (MasterDropView_SCCImpl) findViewObject("MasterDropView_SCC1");
  }

  /**
   * Container's getter for MasterDropView_SMCC1.
   *
   * @return MasterDropView_SMCC1
   */

  @SuppressWarnings("java:S100") //(FUTURE) to rename this, PageDef xml would need updated.  and unsure of ADF impact.
  public MasterDropView_SMCCImpl getMasterDropView_SMCC1() {
    return (MasterDropView_SMCCImpl) findViewObject("MasterDropView_SMCC1");
  }

  /**
   * Container's getter for NiinInfoView_CC_Change1.
   *
   * @return NiinInfoView_CC_Change1
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage xml would need updated.  and unsure of ADF impact.
  public NiinInfoCcChangeViewImpl getNiinInfoView_CC_Change1() {
    return (NiinInfoCcChangeViewImpl) findViewObject("NiinInfoView_CC_Change1");
  }

  /**
   * Container's getter for NiinInfoView_CC_Change2.
   *
   * @return NiinInfoView_CC_Change2
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, PageDef xml would need updated.  and unsure of ADF impact.
  public NiinInfoCcChangeViewImpl getNiinInfoView_CC_Change2() {
    return (NiinInfoCcChangeViewImpl) findViewObject("NiinInfoView_CC_Change2");
  }

  /**
   * Container's getter for NiinInfoView_CC_Change3.
   *
   * @return NiinInfoView_CC_Change3
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage xml would need updated.  and unsure of ADF impact.
  public NiinInfoCcChangeViewImpl getNiinInfoView_CC_Change3() {
    return (NiinInfoCcChangeViewImpl) findViewObject("NiinInfoView_CC_Change3");
  }

  /**
   * Container's getter for NiinInfoView_CC_ChangeFilter1.
   *
   * @return NiinInfoView_CC_ChangeFilter1
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage xml would need updated.  and unsure of ADF impact.
  public NiinInfoCcChangeFilterViewImpl getNiinInfoView_CC_ChangeFilter1() {
    return (NiinInfoCcChangeFilterViewImpl) findViewObject("NiinInfoView_CC_ChangeFilter1");
  }

  /**
   * Container's getter for NiinInfoView1.
   *
   * @return NiinInfoView1
   */
  public NiinInfoViewImpl getNiinInfoView1() {
    return (NiinInfoViewImpl) findViewObject("NiinInfoView1");
  }

  /**
   * Container's getter for RefSlcView1.
   *
   * @return RefSlcView1
   */
  public ViewObjectImpl getRefSlcView1() {
    return (ViewObjectImpl) findViewObject("RefSlcView1");
  }

  /**
   * Container's getter for RouteView1.
   *
   * @return RouteView1
   */
  public ViewObjectImpl getRouteView1() {
    return (ViewObjectImpl) findViewObject("RouteView1");
  }

  /**
   * Container's getter for RouteView1_WarehouseSetup.
   *
   * @return RouteView1_WarehouseSetup
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage xml would need updated.  and unsure of ADF impact.
  public ViewObjectImpl getRouteView1_WarehouseSetup() {
    return (ViewObjectImpl) findViewObject("RouteView1_WarehouseSetup");
  }

  /**
   * Container's getter for ShippingRouteView1.
   *
   * @return ShippingRouteView1
   */
  public ViewObjectImpl getShippingRouteView1() {
    return (ViewObjectImpl) findViewObject("ShippingRouteView1");
  }

  /**
   * Container's getter for ShippingRouteView1_WarehouseSetup.
   *
   * @return ShippingRouteView1_WarehouseSetup
   */
  @SuppressWarnings({"java:S100", "unused"}) //(FUTURE) to rename this, PageDef xml would need updated.  and unsure of ADF impact.
  public ViewObjectImpl getShippingRouteView1_WarehouseSetup() {
    return (ViewObjectImpl) findViewObject("ShippingRouteView1_WarehouseSetup");
  }

  /**
   * Container's getter for SingleFloorLocationView1.
   *
   * @return SingleFloorLocationView1
   */
  public SingleFloorLocationViewImpl getSingleFloorLocationView1() {
    return (SingleFloorLocationViewImpl) findViewObject("SingleFloorLocationView1");
  }

  /**
   * Container's getter for SiteInfoView1.
   *
   * @return SiteInfoView1
   */
  public SiteInfoViewImpl getSiteInfoView1() {
    return (SiteInfoViewImpl) findViewObject("SiteInfoView1");
  }

  /**
   * Container's getter for SiteSecurityView_SCC1.
   *
   * @return SiteSecurityView_SCC1
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage xml would need updated.  and unsure of ADF impact.
  public ViewObjectImpl getSiteSecurityView_SCC1() {
    return (ViewObjectImpl) findViewObject("SiteSecurityView_SCC1");
  }

  /**
   * Container's getter for SiteSecurityView_SMCC1.
   *
   * @return SiteSecurityView_SMCC1
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage xml would need updated.  and unsure of ADF impact.
  public ViewObjectImpl getSiteSecurityView_SMCC1() {
    return (ViewObjectImpl) findViewObject("SiteSecurityView_SMCC1");
  }

  /**
   * Container's getter for UserGroupView1.
   *
   * @return UserGroupView1
   */
  public UserGroupViewImpl getUserGroupView1() {
    return (UserGroupViewImpl) findViewObject("UserGroupView1");
  }

  /**
   * Container's getter for WacDropdown1.
   *
   * @return WacDropdown1
   */
  public ViewObjectImpl getWacDropdown1() {
    return (ViewObjectImpl) findViewObject("WacDropdown1");
  }

  /**
   * Container's getter for WacDropdown1_Workstation.
   *
   * @return WacDropdown1_Workstation
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage xml would need updated.  and unsure of ADF impact.
  public ViewObjectImpl getWacDropdown1_Workstation() {
    return (ViewObjectImpl) findViewObject("WacDropdown1_Workstation");
  }

  /**
   * Container's getter for WacDropdown2.
   *
   * @return WacDropdown2
   */
  public ViewObjectImpl getWacDropdown2() {
    return (ViewObjectImpl) findViewObject("WacDropdown2");
  }

  /**
   * Container's getter for WACList1.
   *
   * @return WACList1
   */
  public WACListImpl getWACList1() {
    return (WACListImpl) findViewObject("WACList1");
  }

  /**
   * Container's getter for WACList1_Mech.
   *
   * @return WACList1_Mech
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage xml would need updated.  and unsure of ADF impact.
  public WACListImpl getWACList1_Mech() {
    return (WACListImpl) findViewObject("WACList1_Mech");
  }

  /**
   * Container's getter for WACList1_NonMech.
   *
   * @return WACList1_NonMech
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage xml would need updated.  and unsure of ADF impact.
  public WACListImpl getWACList1_NonMech() {
    return (WACListImpl) findViewObject("WACList1_NonMech");
  }

  /**
   * Container's getter for WacTableMechView1.
   *
   * @return WacTableMechView1
   */
  public WacTableMechViewImpl getWacTableMechView1() {
    return (WacTableMechViewImpl) findViewObject("WacTableMechView1");
  }

  /**
   * Container's getter for WacViewAll1.
   *
   * @return WacViewAll1
   */
  public ViewObjectImpl getWacViewAll1() {
    return (ViewObjectImpl) findViewObject("WacViewAll1");
  }

  /**
   * Container's getter for WarehouseView1.
   *
   * @return WarehouseView1
   */
  public ViewObjectImpl getWarehouseView1() {
    return (ViewObjectImpl) findViewObject("WarehouseView1");
  }

  /**
   * Container's getter for WarehouseView1_Building.
   *
   * @return WarehouseView1_Building
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage and PageDef xml would need updated.  and unsure of ADF impact.
  public ViewObjectImpl getWarehouseView1_Building() {
    return (ViewObjectImpl) findViewObject("WarehouseView1_Building");
  }

  /**
   * Container's getter for WarehouseView1_NonMech.
   *
   * @return WarehouseView1_NonMech
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage xml would need updated.  and unsure of ADF impact.
  public ViewObjectImpl getWarehouseView1_NonMech() {
    return (ViewObjectImpl) findViewObject("WarehouseView1_NonMech");
  }

  /**
   * Container's getter for WarehouseViewAll1.
   *
   * @return WarehouseViewAll1
   */
  public WarehouseViewAllImpl getWarehouseViewAll1() {
    return (WarehouseViewAllImpl) findViewObject("WarehouseViewAll1");
  }

  /**
   * Container's getter for WarehouseViewAll1_Workstation.
   *
   * @return WarehouseViewAll1_Workstation
   */
  @SuppressWarnings({"unused", "java:S100"})
  //(FUTURE) to rename this, ViewUsage and PageDef xml would need updated.  and unsure of ADF impact.
  public WarehouseViewAllImpl getWarehouseViewAll1_Workstation() {
    return (WarehouseViewAllImpl) findViewObject("WarehouseViewAll1_Workstation");
  }

  /**
   * Container's getter for TempMechCreateRows1.
   *
   * @return TempMechCreateRows1
   */
  public TempMechCreateRowsImpl getTempMechCreateRows1() {
    return (TempMechCreateRowsImpl) findViewObject("TempMechCreateRows1");
  }

  /**
   * Container's getter for TempMechCreateRows2.
   *
   * @return TempMechCreateRows2
   */
  public TempMechCreateRowsImpl getTempMechCreateRows2() {
    return (TempMechCreateRowsImpl) findViewObject("TempMechCreateRows2");
  }

  /**
   * Container's getter for NiinLoc_LocationView1.
   *
   * @return NiinLoc_LocationView1
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage and PageDef xml would need updated.  and unsure of ADF impact.
  public NiinLocLocationViewImpl getNiinLoc_LocationView1() {
    return (NiinLocLocationViewImpl) findViewObject("NiinLoc_LocationView1");
  }

  /**
   * Container's getter for WarehouseSetup1.
   *
   * @return WarehouseSetup1
   */
  public ApplicationModuleImpl getWarehouseSetup1() {
    return (ApplicationModuleImpl) findApplicationModule("WarehouseSetup1");
  }

  /**
   * Container's getter for DividerSlotsView2.
   *
   * @return DividerSlotsView2
   */
  public DividerSlotsViewImpl getDividerSlotsView2() {
    return (DividerSlotsViewImpl) findViewObject("DividerSlotsView2");
  }

  /**
   * Container's getter for EquipView1_Workstation.
   *
   * @return EquipView1_Workstation
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage and PageDef xml would need updated.  and unsure of ADF impact.
  public mil.stratis.model.view.site.EquipViewImpl getEquipView1_Workstation() {
    return (mil.stratis.model.view.site.EquipViewImpl) findViewObject("EquipView1_Workstation");
  }

  /**
   * Container's getter for GivenUserGroups.
   *
   * @return GivenUserGroups
   */
  public ViewObjectImpl getGivenUserGroups() {
    return (ViewObjectImpl) findViewObject("GivenUserGroups");
  }

  /**
   * Container's getter for NiinLoc_LocationView2.
   *
   * @return NiinLoc_LocationView2
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage xml would need updated.  and unsure of ADF impact.
  public NiinLocLocationViewImpl getNiinLoc_LocationView2() {
    return (NiinLocLocationViewImpl) findViewObject("NiinLoc_LocationView2");
  }

  /**
   * Container's getter for NiinLoc_LocationView_CC_Change2.
   *
   * @return NiinLoc_LocationView_CC_Change2
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage and PageDef xml would need updated.  and unsure of ADF impact.
  public NiinLocLocationViewImpl getNiinLoc_LocationView_CC_Change2() {
    return (NiinLocLocationViewImpl) findViewObject("NiinLoc_LocationView_CC_Change2");
  }

  /**
   * Container's getter for NIINSerialNumbers1.
   *
   * @return NIINSerialNumbers1
   */
  public ViewObjectImpl getNIINSerialNumbers1() {
    return (ViewObjectImpl) findViewObject("NIINSerialNumbers1");
  }

  /**
   * Container's getter for DividerSlotsByTypeView1.
   *
   * @return DividerSlotsByTypeView1
   */
  public DividerSlotsByTypeViewImpl getDividerSlotsByTypeView1() {
    return (DividerSlotsByTypeViewImpl) findViewObject("DividerSlotsByTypeView1");
  }

  /**
   * Container's getter for NIIN_CC_LOCATION_LINK1.
   *
   * @return NIIN_CC_LOCATION_LINK1
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage xml would need updated.  and unsure of ADF impact.
  public ViewLinkImpl getNIIN_CC_LOCATION_LINK1() {
    return (ViewLinkImpl) findViewLink("NIIN_CC_LOCATION_LINK1");
  }

  /**
   * Container's getter for DividerType_Slot_View1.
   *
   * @return DividerType_Slot_View1
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage xml would need updated.  and unsure of ADF impact.
  public ViewLinkImpl getDividerType_Slot_View1() {
    return (ViewLinkImpl) findViewLink("DividerType_Slot_View1");
  }

  /**
   * Container's getter for NIIN_CC_LOCATION_LINK2.
   *
   * @return NIIN_CC_LOCATION_LINK2
   */
  @SuppressWarnings("java:S100") //(FUTURE) to rename this, ViewUsage xml would need updated.  and unsure of ADF impact.
  public ViewLinkImpl getNIIN_CC_LOCATION_LINK2() {
    return (ViewLinkImpl) findViewLink("NIIN_CC_LOCATION_LINK2");
  }

  /**
   * Container's getter for NIINSerialNumberLink1.
   *
   * @return NIINSerialNumberLink1
   */
  public ViewLinkImpl getNIINSerialNumberLink1() {
    return (ViewLinkImpl) findViewLink("NIINSerialNumberLink1");
  }

  /**
   * Container's getter for WorkstationTypeView1.
   *
   * @return WorkstationTypeView1
   */
  public ViewObjectImpl getWorkstationTypeView1() {
    return (ViewObjectImpl) findViewObject("WorkstationTypeView1");
  }

  /**
   * Container's getter for WacByBuildingView1.
   *
   * @return WacByBuildingView1
   */
  public ViewObjectImpl getWacByBuildingView1() {
    return (ViewObjectImpl) findViewObject("WacByBuildingView1");
  }

  /**
   * Container's getter for GCSSImportsLog1.
   *
   * @return GCSSImportsLog1
   */
  public ViewObjectImpl getGCSSImportsLog1() {
    return (ViewObjectImpl) findViewObject("GCSSImportsLog1");
  }
}
