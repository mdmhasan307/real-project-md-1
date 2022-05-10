package mil.stratis.model.services;

import com.agiledelta.efx.z.r;
import lombok.SneakyThrows;
import mil.stratis.model.util.TestDBTransactionGetterImpl;
import oracle.jbo.server.DBTransaction;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class WorkLoadManagerImplTest {

    @Test
    @DisplayName("Do A Full Execution Of returnBestPickLocation()")
    void testInsertPicking() {

        DBTransaction testDBTransaction = mock(DBTransaction.class);
        setupMocks(testDBTransaction);

        WorkLoadManagerImpl wlmi = new WorkLoadManagerImpl();
        boolean result = wlmi.returnBestPickLocation("SCN", "NIINID", "PID", "CC", new TestDBTransactionGetterImpl(testDBTransaction));
        assertThat("Return value of returnBestPickLocation should be true", result);
    }

    @Test
    @DisplayName("Do A Full Execution Of returnBestPickLocation()")
    void testInsertPickingWithNumbers() {

        DBTransaction testDBTransaction = mock(DBTransaction.class);
        setupMocks(testDBTransaction);

        WorkLoadManagerImpl wlmi = new WorkLoadManagerImpl();
        boolean result = wlmi.returnBestPickLocation("SCN", Long.valueOf("3828"), "PID", "CC", new TestDBTransactionGetterImpl(testDBTransaction));
        assertThat("Return value of returnBestPickLocation should be true", result);
    }

    @Test
    @DisplayName("Execute the testInitializePickMetrics()")
    @SneakyThrows
    void testInitializePickMetrics() {
        DBTransaction testDBTransaction = mock(DBTransaction.class);
        ResultSet mockRs = mock(ResultSet.class);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getString(1)).thenReturn("ANSCN");
        when(mockRs.getInt(2)).thenReturn(9999);
        when(mockRs.getInt(5)).thenReturn(9999);
        prepareForResultSet(testDBTransaction, WorkLoadManagerImpl.GET_STATS_FOR_PID_SQL, 0, mockRs);

        PickMetrics pickMetricsIn = new PickMetrics(null, "NIINID", null, null);
        new WorkLoadManagerImpl().initializePickMetrics("NIINID", pickMetricsIn, new TestDBTransactionGetterImpl(testDBTransaction));
        assertThat("Value of pickMetricsOut.scn should have been updated", pickMetricsIn.scn, equalTo("ANSCN"));
        assertThat("Value of pickMetricsOut.qtyPicked should have been updated", pickMetricsIn.qtyPicked, equalTo(9999));
        assertThat("Value of pickMetricsOut.niinLocId should have been updated", pickMetricsIn.niinLocId, equalTo(9999));
        assertThat("Value of pickMetricsOut.scnText should be the same as .scn", pickMetricsIn.scnText, equalTo(pickMetricsIn.scn));
    }

    @Test
    @DisplayName("Execute the testInitializePickMetricsWithNullPid() with null pid")
    @SneakyThrows
    void testInitializePickMetricsWithNullPid() {
        DBTransaction testDBTransaction = mock(DBTransaction.class);
        ResultSet mockRs = mock(ResultSet.class);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getString(1)).thenReturn("ANSCN");
        when(mockRs.getInt(2)).thenReturn(9999);
        when(mockRs.getInt(5)).thenReturn(9999);
        prepareForResultSet(testDBTransaction, WorkLoadManagerImpl.GET_STATS_FOR_PID_SQL, 0, mockRs);

        PickMetrics pickMetricsIn = new PickMetrics("ANSCN", null, "ANIINID", null);

        new WorkLoadManagerImpl().initializePickMetrics(pickMetricsIn.pid, pickMetricsIn, new TestDBTransactionGetterImpl(testDBTransaction));
        assertThat("Value of pickMetricsOut.scn", pickMetricsIn.scn, equalTo("ANSCN"));
        assertThat("Value of pickMetricsOut.qtyPicked is unchanged", pickMetricsIn.qtyPicked, equalTo(-1));
        assertThat("Value of pickMetricsOut.niinLocId is unchanged", pickMetricsIn.niinLocId, equalTo(-1));
        assertThat("Value of pickMetricsOut.scnText is uninitialized", pickMetricsIn.scnText, equalTo(null));
        assertThat("Value of pickMetricsOut.pid is uninitialized", pickMetricsIn.pid, equalTo(null));
    }

    @Test
    @DisplayName("Execute the verifyInitializePickMetricsValues()")
    @SneakyThrows
    void verifyInitializePickMetricsValues() {
        DBTransaction testDBTransaction = mock(DBTransaction.class);
        ResultSet mockRs = mock(ResultSet.class);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getString(1)).thenReturn("ANSCN");
        when(mockRs.getInt(2)).thenReturn(9999);
        when(mockRs.getInt(5)).thenReturn(9999);
        prepareForResultSet(testDBTransaction, WorkLoadManagerImpl.GET_STATS_FOR_PID_SQL, 0, mockRs);

        PickMetrics pickMetrics = new PickMetrics("ANSCN", null, null, null);

        new WorkLoadManagerImpl().initializePickMetrics("PID", pickMetrics, new TestDBTransactionGetterImpl(testDBTransaction));
        assertThat(pickMetrics.scn, equalTo("ANSCN"));
        assertThat(pickMetrics.pid, equalTo(null));
        assertThat(pickMetrics.niinid, equalTo(null));
        assertThat(pickMetrics.cc, equalTo(null));

        pickMetrics = new PickMetrics("ANSCN", "PID", "NIINID", "CC");
        new WorkLoadManagerImpl().initializePickMetrics("PID", pickMetrics, new TestDBTransactionGetterImpl(testDBTransaction));
        assertThat(pickMetrics.scn, equalTo("ANSCN"));
        assertThat(pickMetrics.pid, equalTo("PID"));
        assertThat(pickMetrics.niinid, equalTo("NIINID"));
        assertThat(pickMetrics.cc, equalTo("CC"));

    }

    @Test
    @DisplayName("Execute the getRequestedQuantityForIssue()")
    @SneakyThrows
    void testRequestedQuantityForIssue() {

        WorkLoadManagerImpl workLoadManager = new WorkLoadManagerImpl();
        DBTransaction testDBTransaction = mock(DBTransaction.class);
        initSelectQtyFromIssuePreparedStatement(7, testDBTransaction);

        // No SCN on pickMetrics, so return 0...
        int quantityForIssue = workLoadManager.getRequestedQuantityForIssue(null, new TestDBTransactionGetterImpl(testDBTransaction));
        assertThat("Value of requested qty for issue", quantityForIssue, equalTo(0));

        PickMetrics pickMetrics = new PickMetrics("ANSCN", null, null, null);
        quantityForIssue = workLoadManager.getRequestedQuantityForIssue(pickMetrics.scn, new TestDBTransactionGetterImpl(testDBTransaction));
        assertThat("Value of requested qty for issue", quantityForIssue, equalTo(7));

        // If db returns no value, return quantity 0
        testDBTransaction = mock(DBTransaction.class);
        initSelectQtyFromIssuePreparedStatement(-1, testDBTransaction);

        quantityForIssue = workLoadManager.getRequestedQuantityForIssue(null, new TestDBTransactionGetterImpl(testDBTransaction));
        assertThat("Value of requested qty for issue", quantityForIssue, equalTo(0));
    }

    @Test
    @DisplayName("Execute the getTotalQuantityForIssueSoFar()")
    @SneakyThrows
    void testRequestedQuantityForIssueSoFar() {
        WorkLoadManagerImpl wlmi = new WorkLoadManagerImpl();
        DBTransaction testDBTransaction = mock(DBTransaction.class);
        initSelectTotalQtyPickedPreparedStatement(4, testDBTransaction);

        // If db returns a value, return that quantity
        PickMetrics pickMetrics = new PickMetrics("ANSCN", null, null, null);
        pickMetrics.sumPicks = 22;
        int sumPicks = wlmi.getTotalQuantityForIssueSoFar(pickMetrics, new TestDBTransactionGetterImpl(testDBTransaction));
        assertThat("Value returned from the database...", sumPicks, equalTo(4));

        // If db returns no value, return quantity 0
        testDBTransaction = mock(DBTransaction.class);
        initSelectTotalQtyPickedPreparedStatement(-1, testDBTransaction);

        pickMetrics = new PickMetrics("ANSCN", null, null, null);
        pickMetrics.sumPicks = 99;
        sumPicks = wlmi.getTotalQuantityForIssueSoFar(pickMetrics, new TestDBTransactionGetterImpl(testDBTransaction));
        assertThat("Value returned from original pickMetrics.sumPicks", sumPicks, equalTo(99));
    }

    @SneakyThrows
    private void prepareForResultSet(DBTransaction testDBTransaction, String sql, int index, ResultSet mockRs) {
        PreparedStatement mockPs = mock(PreparedStatement.class);
        when(mockPs.executeQuery()).thenReturn(mockRs);
        when(testDBTransaction.createPreparedStatement(sql, index)).thenReturn(mockPs);
    }

    /**
     *
     * @param testDBTransaction
     */
    private void setupMocks(DBTransaction testDBTransaction) {
        initSelectScnPreparedStatement(testDBTransaction);
        initSelectQtyFromIssuePreparedStatement(6, testDBTransaction);
        initSelectTotalQtyPickedPreparedStatement(4, testDBTransaction);
        initSelectQtyNotPickedPreparedStatement(testDBTransaction);
        initSelectQtyFromAllNiinLocationsPreparedStatement(testDBTransaction);
        initFifthPreparedStatements(testDBTransaction);
    }

    @SneakyThrows
    private void initSelectScnPreparedStatement(DBTransaction testDBTransaction) {
        ResultSet mockRs = mock(ResultSet.class);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getString(1)).thenReturn("ANSCN");
        when(mockRs.getInt(2)).thenReturn(9999);
        when(mockRs.getInt(5)).thenReturn(9999);

        prepareForResultSet(testDBTransaction, WorkLoadManagerImpl.GET_STATS_FOR_PID_SQL, 0, mockRs);
    }

    @SneakyThrows
    private void initSelectQtyFromIssuePreparedStatement(int retQty, DBTransaction testDBTransaction) {
        ResultSet mockRs = mock(ResultSet.class);
        if (retQty < 0) {
            when(mockRs.next()).thenReturn(false);
        } else {
            when(mockRs.next()).thenReturn(true);
            when(mockRs.getInt(1)).thenReturn(retQty);
        }

        prepareForResultSet(testDBTransaction, WorkLoadManagerImpl.GET_REQUESTED_QTY_FOR_ISSUE_SQL, 0, mockRs);
    }

    @SneakyThrows
    private void initSelectTotalQtyPickedPreparedStatement(int retVal, DBTransaction testDBTransaction) {
        ResultSet mockRs = mock(ResultSet.class);
        if (retVal < 0) {
            when(mockRs.next()).thenReturn(false);
        } else {
            when(mockRs.next()).thenReturn(true);
            when(mockRs.getObject(1)).thenReturn(1);
            when(mockRs.getInt(1)).thenReturn(retVal);
        }

        prepareForResultSet(testDBTransaction, WorkLoadManagerImpl.GET_TOTAL_PICKED_QTY_FOR_ISSUE_SQL, 0, mockRs);
    }

    @SneakyThrows
    private void initSelectQtyNotPickedPreparedStatement(DBTransaction testDBTransaction) {
        ResultSet mockRs = mock(ResultSet.class);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getObject(1)).thenReturn(1);
        when(mockRs.getInt(1)).thenReturn(4);

        prepareForResultSet(testDBTransaction, WorkLoadManagerImpl.GET_TOTAL_UNPICKED_QTY_FOR_ISSUE_SQL, 0, mockRs);
    }

    @SneakyThrows
    private void initSelectQtyFromAllNiinLocationsPreparedStatement(DBTransaction testDBTransaction) {
        ResultSet mockRs = mock(ResultSet.class);
        when(mockRs.next()).thenReturn(true);
        when(mockRs.getLong(2)).thenReturn(5L);

        prepareForResultSet(testDBTransaction, WorkLoadManagerImpl.GET_NIIN_QTYS_BY_LOCATION_SQL, 0, mockRs);
    }

    @SneakyThrows
    private void initFifthPreparedStatements(DBTransaction testDBTransaction) {
        ResultSet mockRsB = mock(ResultSet.class);
        when(mockRsB.next()).thenReturn(true);
        when(mockRsB.getInt(1)).thenReturn(2);

        prepareForResultSet(testDBTransaction, WorkLoadManagerImpl.GET_RESERVED_TOTALS_FOR_LOCATION_SQL, 0, mockRsB);
    }
}
