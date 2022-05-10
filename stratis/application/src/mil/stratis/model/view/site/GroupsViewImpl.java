package mil.stratis.model.view.site;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import oracle.jbo.JboException;
import oracle.jbo.server.ViewObjectImpl;
import oracle.jbo.server.ViewRowImpl;
import oracle.jbo.server.ViewRowSetImpl;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleTypes;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

@Slf4j

@NoArgsConstructor //ViewObjImpl need default no args constructor
public class GroupsViewImpl extends ViewObjectImpl {

  /*
   * Executed when the framework needs to issue the database query for
   * the query collection based on this view object. One view object
   * can produce many related result sets, each potentially the result
   * of different bind variable values. If the rowset in query is involved
   * in a framework-coordinated master/detail viewlink, then the params array
   * will contain one or more framework-supplied bind parameters. If there
   * are any user-supplied bind parameter values, they will *PRECEED* the
   * framework-supplied bind variable values in the params array, and the
   * number of user parameters will be indicated by the value of the
   * numUserParams argument.
   */
  @Override
  protected void executeQueryForCollection(Object qc, Object[] params, int numUserParams) {
    storeNewResultSet(qc, retrieveRefCursor());
    super.executeQueryForCollection(qc, params, numUserParams);
  }

  /**
   * Overridden framework method.
   * <p>
   * Wipe out all traces of a built-in query for this VO
   */
  @Override
  protected void create() {
    getViewDef().setQuery(null);
    getViewDef().setSelectClause(null);
    setQuery(null);
  }

  /**
   * Overridden framework method.
   * <p>
   * The role of this method is to "fetch", populate, and return a single row
   * from the datasource by calling createNewRowForCollection() and populating
   * its attributes using populateAttributeForRow().
   */
  @Override
  protected ViewRowImpl createRowFromResultSet(Object qc, ResultSet rs) {
    /*
     * We ignore the JDBC ResultSet passed by the framework (null anyway) and
     * use the resultset that we've stored in the query-collection-private
     * user data storage
     */
    rs = getResultSet(qc);

    /*
     * Create a new row to populate
     */
    ViewRowImpl r = createNewRowForCollection(qc);

    try {
      /*
       * Populate new row by attribute slot number for current row in Result Set
       */
      populateAttributeForRow(r, 0, rs.getLong(1));
      populateAttributeForRow(r, 1, rs.getString(2));
      populateAttributeForRow(r, 2, rs.getString(3));
      populateAttributeForRow(r, 3, rs.getDate(4));
      populateAttributeForRow(r, 4, rs.getLong(5));
    }
    catch (SQLException s) {
      throw new JboException(s);
    }
    return r;
  }

  /**
   * Overridden framework method.
   * <p>
   * Return true if the datasource has at least one more record to fetch.
   */
  @Override
  protected boolean hasNextForCollection(Object qc) {
    ResultSet rs = getResultSet(qc);
    boolean nextOne;
    try {
      nextOne = rs.next();
      /*
       * When were at the end of the result set, mark the query collection
       * as "FetchComplete".
       */
      if (!nextOne) {
        setFetchCompleteForCollection(qc, true);
        /*
         * Close the result set, we're done with it
         */
        rs.close();
      }
    }
    catch (SQLException s) {
      throw new JboException(s);
    }
    return nextOne;
  }

  /**
   * Overridden framework method.
   * <p>
   * The framework gives us a chance to clean up any resources related
   * to the datasource when a query collection is done being used.
   */
  @Override
  protected void releaseUserDataForCollection(Object qc, Object rs) {
    /*
     * Ignore the ResultSet passed in since we've created our own.
     * Fetch the ResultSet from the User-Data context instead
     */
    ResultSet userDataRS = getResultSet(qc);
    if (userDataRS != null) {
      try {
        userDataRS.close();
      }
      catch (SQLException s) {
        /* Ignore */
      }
    }
    super.releaseUserDataForCollection(qc, rs);
  }

  /**
   * Overridden framework method
   * <p>
   * Return the number of rows that would be returned by executing
   * the query implied by the datasource. This gives the developer a
   * chance to perform a fast count of the rows that would be retrieved
   * if all rows were fetched from the database. In the default implementation
   * the framework will perform a SELECT COUNT(*) FROM (...) wrapper query
   * to let the database return the count. This count might only be an estimate
   * depending on how resource-intensive it would be to actually count the rows.
   */
  @Override
  public long getQueryHitCount(ViewRowSetImpl viewRowSet) {
    return callStoredFunctionCount();
  }
  // ------------- PRIVATE METHODS ----------------

  /**
   * Return a JDBC ResultSet representing the REF CURSOR return
   * value from our stored package function.
   */
  private ResultSet retrieveRefCursor() {
    return (ResultSet) callStoredFunction();
  }

  /**
   * Store a new result set in the query-collection-private user-data context
   */
  private void storeNewResultSet(Object qc, ResultSet rs) {
    ResultSet existingRs = getResultSet(qc);
    // If this query collection is getting reused, close out any previous rowset
    if (existingRs != null) {
      try {existingRs.close();}
      catch (SQLException s) {
        log.info("Error when attempting to close an existing Resultset", s);
      }
    }
    setUserDataForCollection(qc, rs);
    hasNextForCollection(qc); // Prime the pump with the first row.
  }

  /**
   * Retrieve the result set wrapper from the query-collection user-data
   */
  private ResultSet getResultSet(Object qc) {
    return (ResultSet) getUserDataForCollection(qc);
  }

  /**
   * Call Stored Function, hard coded with the name of function
   *
   * @return function return value as an Object
   */
  protected Object callStoredFunction() {
    String sql = "begin ? := pkg_user_mgmt.f_get_all_groups(?,?); end;";
    try {
      //We can't use try-with-resources here.
      // The result returned is a cursor, which gets closed when the statement is closed with try-with-resources.
      CallableStatement st = getDBTransaction().createCallableStatement(sql, 0);
      st.registerOutParameter(1, Types.INTEGER);
      st.registerOutParameter(2, OracleTypes.CURSOR);
      st.registerOutParameter(3, Types.VARCHAR);
      st.execute();

      return st.getObject(2);
    }
    catch (SQLException e) {
      throw new JboException(e);
    }
  }

  /**
   * Call Stored Function Count, hard coded with the name of function
   *
   * @return function return value as an Object
   */
  protected long callStoredFunctionCount() {
    long size = 0;
    String sql = "begin ? := pkg_user_mgmt.f_get_all_groups(?,?); end;";
    try (CallableStatement st = getDBTransaction().createCallableStatement(sql, 0)) {
      st.registerOutParameter(1, Types.INTEGER);
      st.registerOutParameter(2, OracleTypes.CURSOR);
      st.registerOutParameter(3, Types.VARCHAR);
      st.execute();
      ResultSet rs = ((OracleCallableStatement) st).getCursor(2);
      while (rs.next()) {
        size++;
      }
    }
    catch (SQLException e) {
      throw new JboException(e);
    }
    return size;
  }
}
