package mil.usmc.mls2.stratis.core.service.multitenancy;

import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

public interface DateService {

  Double getDatabaseVsDisplayTimeZoneShiftInSecondsForOracle();

  XMLGregorianCalendar getXMLGregorianCalendarNow();

  XMLGregorianCalendar getXMLGregorianCalendarFromDateOrElseToday(LocalDate date);

  LocalDate getLastDateOfThisMonth();

  boolean isValidDate(String date);

  OffsetDateTime getOffsetDateTimeFromResultSet(ResultSet rs, String property) throws SQLException;

  ZonedDateTime shiftTimezoneToDisplayZone(OffsetDateTime offsetDate);

  String formatDate(LocalDate localDate, String pattern);

  String formatDate(ZonedDateTime zonedDate, String pattern);

  String formatDate(ZonedDateTime zonedDate, boolean showTime);

  String shiftAndFormatDateForTag(OffsetDateTime offsetDateTime, boolean showTime);

  String formatDateForTag(OffsetDateTime offsetDateTime, boolean showTime);

  String shiftAndFormatDate(OffsetDateTime offsetDateTime, String pattern);

  LocalDate convertLocalDateStringToLocalDate(String date, String pattern);

  OffsetDateTime convertLocalDateTimeStringToDbTimezoneOffsetDateTime(String date, String pattern);

  OffsetDateTime convertLocalDateTimeToDbTimezoneOffsetDateTime(LocalDateTime zonedDateTime);

  String formatForAdfDisplay(String date, String patternToConvertFrom, String patternToDisplayIn, boolean shiftZone, boolean displayTimeZone);
}
