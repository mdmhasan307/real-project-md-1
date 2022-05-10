package mil.usmc.mls2.stratis.core.infrastructure.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.utility.AdfDbCtxLookupUtils;
import oracle.ucp.jdbc.JDBCConnectionPoolStatistics;
import oracle.ucp.jdbc.PoolDataSource;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.naming.InitialContext;

@Slf4j
@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
@Profile(Profiles.LEGACY)
@SuppressWarnings("Duplicates")
public class UCPStatsStandardJob implements UCPStatsJob {

  @Value("${stratis.jobs.ucp.enabled}")
  private boolean ucpJobEnabled;

  private final AdfDbCtxLookupUtils adfDbCtxLookupUtils;

  public void execute(JobExecutionContext context) {
    if (!ucpJobEnabled) {
      log.debug("UCP Job Disabled");
      return;
    }

    try {
      InitialContext ctx = new InitialContext();
      PoolDataSource ds = (PoolDataSource) ctx.lookup(adfDbCtxLookupUtils.getDbCtxLookupPath());
      JDBCConnectionPoolStatistics stats = ds.getStatistics();
      logData(stats);
    }
    catch (Exception e) {
      log.warn("Failed to log pool statistics", e);
    }
  }

  private void logData(JDBCConnectionPoolStatistics stats) {
    StringBuilder sb = new StringBuilder(1000);
    sb.append(": UCP T A B CR CL TOT PEAK: ");
    sb.append(stats.getTotalConnectionsCount()).append(", ");
    sb.append(stats.getAvailableConnectionsCount()).append(", ");
    sb.append(stats.getBorrowedConnectionsCount()).append(", ");
    sb.append(stats.getConnectionsCreatedCount()).append(", ");
    sb.append(stats.getConnectionsClosedCount()).append(", ");

    sb.append(stats.getCumulativeConnectionBorrowedCount()).append(", ");
    sb.append(stats.getPeakConnectionsCount());

    log.info(sb.toString());
  }
}