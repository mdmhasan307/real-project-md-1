package mil.usmc.mls2.stratis.core.infrastructure.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.Profiles;
import mil.usmc.mls2.stratis.core.infrastructure.configuration.StratisConfig;
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
@Profile(Profiles.INTEGRATION_ANY)
@SuppressWarnings("Duplicates")
public class UCPStatsIntegrationJob implements UCPStatsJob {

  @Value("${stratis.jobs.ucp.enabled}")
  private boolean ucpJobEnabled;

  private final StratisConfig multiTenancyConfig;
  private final AdfDbCtxLookupUtils adfDbCtxLookupUtils;

  public void execute(JobExecutionContext context) {
    if (!ucpJobEnabled) {
      log.debug("UCP Job Disabled");
      return;
    }

    //Note: Statistics on each of the database pools will NOT be available, until each of the pools have been accessed.
    //Meaning when this process starts the pool utilized by the user session that triggered this will have stats, but
    //the others will not until someone else logs in for the other pool(s).
    multiTenancyConfig.getDatasources().forEach(dataSource -> {
      try {
        InitialContext ctx = new InitialContext();
        PoolDataSource ds = (PoolDataSource) ctx.lookup(adfDbCtxLookupUtils.getPathWithoutDB() + dataSource);
        JDBCConnectionPoolStatistics stats = ds.getStatistics();
        logData(stats, dataSource);
      }
      catch (Exception e) {
        log.warn(String.format("Failed to log pool statistics for %s", dataSource), e.getMessage());
      }
    });
  }

  private void logData(JDBCConnectionPoolStatistics stats, String dataSource) {
    StringBuilder sb = new StringBuilder(1000);
    sb.append("Datasource:").append(dataSource).append(": UCP T A B CR CL TOT PEAK: ");
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