package mil.stratis.model.threads.inventory;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.stratis.model.services.InventorySetupModuleImpl;
import mil.usmc.mls2.stratis.core.utility.AdfDbCtxLookupUtils;
import mil.usmc.mls2.stratis.core.utility.ContextUtils;
import mil.usmc.mls2.stratis.core.utility.GlobalConstants;
import oracle.jbo.server.DBTransaction;
import oracle.ucp.jdbc.PoolDataSource;

import javax.naming.InitialContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Slf4j
public abstract class BaseInventoryThread implements Runnable {

  protected DBTransaction dbTransaction = null;
  protected Connection conn = null;
  protected boolean resetAutoCommitValue = false;
  protected InventorySetupModuleImpl appModule = null;
  protected String dbConnectionForUser = null;
  protected AdfDbCtxLookupUtils adfDbCtxLookupUtils = null;

  public enum ThreadingModeEnum {
    THREADED,
    FOREGROUND;
  }

  protected final ThreadingModeEnum THREADING_MODE;

  /**
   * If running in threaded mode, this flag dictates:
   * - when false, will behave as 703 behaved and utilize the object lists passed in from the caller,
   * even though they will have associations to the callers ADF Context.
   * <p>
   * - when true, the thread class will convert the object list to a simple list of disconnected data elements.
   */
  private final boolean featFlagThreadedWithObjectConversions;

  private BaseInventoryThread() {
    {
      val globalConstants = ContextUtils.getBean(GlobalConstants.class);

      if (globalConstants.isRunInForeground()) {
        THREADING_MODE = ThreadingModeEnum.FOREGROUND;
        this.featFlagThreadedWithObjectConversions = false;
      }
      else {
        THREADING_MODE = ThreadingModeEnum.THREADED;
        this.featFlagThreadedWithObjectConversions = globalConstants.isUseObjectConversionsInThreadedMode();
      }
    }
  }

  public BaseInventoryThread(DBTransaction db, String dbConnectionForUser, AdfDbCtxLookupUtils adfDbCtxLookupUtils) {
    this();

    if (THREADING_MODE.equals(ThreadingModeEnum.FOREGROUND)) {
      //if in foreground mode, we will utilize the caller's database connection.
      this.dbTransaction = db;
    }
    else {

      this.adfDbCtxLookupUtils = adfDbCtxLookupUtils;//used for path lookup to db.
      this.dbConnectionForUser = dbConnectionForUser;//THREADED mode will utilize a JDBC connection from the db pool of the user
      this.dbTransaction = null;
    }
  }

  public boolean isFeatFlagThreadedWithObjectConversions() {
    return featFlagThreadedWithObjectConversions;
  }

  /**
   * Commit database transaction (in FOREGROUND mode)
   * or commit jdbc connection (in THREADED mode)
   */
  protected void commit() throws SQLException {
    if (THREADING_MODE.equals(ThreadingModeEnum.THREADED)) {
      conn.commit();
    }
    else {
      dbTransaction.commit();
    }
  }

  /**
   * Rollback database transaction (in FOREGROUND mode)
   * or rollback jdbc connection (in THREADED mode)
   */
  protected void rollback() throws SQLException {
    if (THREADING_MODE.equals(ThreadingModeEnum.THREADED)) {
      conn.rollback();
    }
    else {
      dbTransaction.rollback();
    }
  }

  /**
   * Created prepared statement using db transaction (in FOREGROUND mode)
   * or using jdbc connection (in THREADED mode)
   */
  protected PreparedStatement createPreparedStatement(String sql, int rowsToPrefetch) throws SQLException {
    if (THREADING_MODE.equals(ThreadingModeEnum.THREADED)) {
      return conn.prepareStatement(sql);
    }
    else {
      return dbTransaction.createPreparedStatement(sql, 0);
    }
  }

  /**
   * If running in THREADED mode, database connection must be retrieved from the db connection pool
   */
  private void setupForThreading() throws Exception {
    if (THREADING_MODE.equals(ThreadingModeEnum.THREADED)) {
      log.info("setupForThreading - using straight jdbc connection {} for thread", dbConnectionForUser);
      InitialContext ctx = new InitialContext();
      PoolDataSource ds = (PoolDataSource) ctx.lookup(adfDbCtxLookupUtils.getPathWithoutDB() + dbConnectionForUser);
      conn = ds.getConnection();
      resetAutoCommitValue = conn.getAutoCommit();

      //must set autoCommit to false as the inventory creation thread classes have conditions that can call rollback
      conn.setAutoCommit(false);
    }
  }

  /**
   * Kick off the inventory creation process.
   * <p>
   * This to be called by the individual threads after their init methods are done with setup.
   * <p>
   * In THREADED mode, will start a new thread to execute the process in the background.
   * In FOREGROUND mode, will simply call run() and execute in the foreground.
   * <p>
   * Mode is set per the THREADING_MODE specified in the stratis.xml context file.
   */
  protected void kickOffProcess() {
    log.info("Kicking off - threading mode {}, convert objects if threaded: {}", THREADING_MODE.name(), this.isFeatFlagThreadedWithObjectConversions());

    if (THREADING_MODE.equals(ThreadingModeEnum.THREADED)) {
      //kick off thread, which will in turn call run()
      Thread t = new Thread(this);
      t.start();
    }
    else {
      //do NOT kick off a new thread.  simply call run() to execute in the foreground!
      run();
    }
  }

  /**
   * Run - if run as a thread, this is the method which will be initated after thread.start() is called
   */
  public void run() {
    try {
      setupForThreading();

      doRun();
    }
    catch (RuntimeException rte) {
      log.error("RuntimeException in run() method", rte);
    }
    catch (Exception e) {
      log.error("Exception in run() method", e);
    }
    finally {
      cleanupResources();
    }
  }

  /**
   * doRun() is to be implemented by classes which extend BaseInventoryThread, where they will perform
   * their actual work of creating inventory data.
   */
  protected abstract void doRun() throws SQLException;

  /**
   * Cleanup any resources which must cleaned up to prevent memory leak or resource exhaustion.
   * <p>
   * In THREADED mode, this will clean up the JDBC connection.
   * <p>
   * In FOREGROUND mode, this is a NOOP.
   */
  private void cleanupResources() {
    if (THREADING_MODE.equals(ThreadingModeEnum.THREADED)) {
      log.info("cleanup - cleaning up jdbc connection");
      try {
        if (conn != null) {
          conn.setAutoCommit(resetAutoCommitValue);
          conn.close();
          conn = null;
        }
      }
      catch (Exception e) {
        log.warn("Failed to close database connection!  Likely connection leak to occur.", e);
      }
    }
  }

  @Override
  protected void finalize() {
    cleanupResources();
  }
}
