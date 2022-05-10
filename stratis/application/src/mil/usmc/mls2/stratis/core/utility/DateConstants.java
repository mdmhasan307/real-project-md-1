package mil.usmc.mls2.stratis.core.utility;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Component
@Getter
public class DateConstants {

  public static final OffsetDateTime maxOffsetDate = OffsetDateTime.now().withYear(9999).withDayOfYear(1);

  public static final LocalDate maxLocalDate = LocalDate.now().withYear(9999).withDayOfYear(1);

  public static final String REPORT_CURRENT_DAY_DEFAULT_DISPLAY_VALUE = "01-JAN-9999";
  public static final String DEFAULT_EXPIRATION_DATE = "9999-01-01";

  public static final String SITE_DATE_WITH_TIME_FORMATTER_PATTERN = "dd MMM yyyy HH:mm:ss";
  public static final String SITE_DATE_FORMATTER_PATTERN = "dd MMM yyyy";
  public static final String SITE_DATE_FORMATTER_PATTERN_WITH_OFFSET = "dd MMM yyyy xxxxx";

  public static final String SITE_DATE_DDMMYYYY_INPUT_FORMATTER_PATTERN = "ddMMyyyy";

  public static final String DEFAULT_INPUT_DATE_FORMAT = "yyyyMMdd";

  public static final String SITE_DATE_INPUT_FORMATTER_PATTERN = "yyyy-MM-dd";
  public static final String SITE_DATE_INPUT_FORMATTER_REGEX_PATTERN = "\\d{4}-\\d{1,2}-\\d{1,2}";

  public static final String ADF_DATE_WITH_TIME_FORMATTER_PATTERN = "dd MMM yyyy kk:mm:ss";
  // 2020-08-20 05:35:06.0
  public static final String ADF_ROW_DATE_RETURN_PATTERN = "yyyy-MM-dd HH:mm:ss[.S][.SS][.SSS]";

  //Friday    14 Aug 2020, 07:22:09 (227)  Reports return most fields in this format
  public static final String ADF_REPORT_DATE_TIME_WITH_JULIAN = "EEEE dd MMM yyyy, HH:mm:ss (D)";

  //09/14/2020 (258), 14:12:04
  public static final String ADF_REPORT_DATE_TIME_WITH_JULIAN_IN_THE_MIDDLE_WITH_DASHES = "yyyy-MM-dd (D), HH:mm:ss";

  //2020-08-14T07:21:00.583-04:00  Reports return fields such as the GCSS Transaction time in this format
  public static final String ADF_REPORT_DATE_TIME_WITH_TIMEZONE = "yyyy-MM-dd'T'HH:mm:ss.SSSxxxxx";

  public static final String DB_REPORT_DATE_INPUT_FORMATTER_PATTERN = "dd-MMM-yyyy";

  public static final String ADF_REPORT_DATE_TIME_NO_SECONDS = "MM/dd/yyyy HH:mm";

  public static final String SITE_DATE_AND_TIME_INPUT_FORMATTER_PATTERN = "yyyy-MM-dd HH:mm:ss";

  public static final String BARCODE_1348_DATE_FORMAT = "yyyy/dd/MM HH:mm:ss";

  //untested/unused
  public static final String SITE_DATE_AND_TIME_INPUT_FORMATTER_REGEX_PATTERN = "\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}";
}
