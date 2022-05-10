package mil.stratis.model.util;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@Slf4j
public class ImportMetricsLogHelper {

  public static final String METRICS_LOGGING_PREFIX = "IMPORTSTAT: ";

  private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  private final String prefix;

  public ImportMetricsLogHelper(String prefix) {
    this.prefix = prefix;
  }

  private void logTraceImportMetric(String message) {
    log.trace("{}{}", METRICS_LOGGING_PREFIX, message);
  }

  private void logInfoImportMetric(String message) {
    log.info("{}{}", METRICS_LOGGING_PREFIX, message);
  }

  public void logStartTimestamp(String method) {
    Calendar cal = Calendar.getInstance();
    logTraceImportMetric(String.format("%s START: %s - %s", prefix, method, sdf.format(cal.getTime())));
  }

  public void logStepTimestamp(String method) {
    Calendar cal = Calendar.getInstance();
    logTraceImportMetric(String.format("%s LOG: %s - %s", prefix, method, sdf.format(cal.getTime())));
  }

  public void logEndTimestamp(String method) {
    Calendar endTime = Calendar.getInstance();
    logTraceImportMetric(String.format("%s END: %s - %s", prefix, method, sdf.format(endTime.getTime())));
  }

  public void logSummary(String msg) {
    Calendar cal = Calendar.getInstance();
    logInfoImportMetric(String.format("%s LOG: %s - %s", prefix, msg, sdf.format(cal.getTime())));
  }
}
