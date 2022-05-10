package mil.usmc.mls2.stratis.core.service.multitenancy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mil.usmc.mls2.stratis.core.domain.model.StaticSiteInfos;
import mil.usmc.mls2.stratis.core.infrastructure.util.UserSiteSelectionTracker;
import mil.usmc.mls2.stratis.core.utility.DateConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.GregorianCalendar;

@Slf4j
@Service
@RequiredArgsConstructor
public class DateServiceImpl implements DateService {

  private final StaticSiteInfos staticSiteInfos;

  @Override
  public Double getDatabaseVsDisplayTimeZoneShiftInSecondsForOracle() {
    val dbDate = OffsetDateTime.now(); //JVM is setup to the same timezone that data is stored in the DB.

    val userSelectedDatabase = UserSiteSelectionTracker.getUserSiteSelection();
    val siteInfo = staticSiteInfos.getSiteInfo(userSelectedDatabase);
    val siteTimezone = siteInfo.getSiteTimezone();

    //Zone ID of Site_Info
    val userDisplayZoneId = ZoneId.of(siteTimezone); //This is the zoneId of the user Display
    val userDisplayZoneDate = dbDate.toInstant().atZone(userDisplayZoneId); //A Zoned Date in the user display timezone.

    //Offset compareTo returns in total seconds.  For oracle to interpret the value as seconds we need to devide by 24 hours, 60 minutes, 60 seconds
    //When the resulting value gets added/subtracted to a Date field in oracle, it shifts the time X seconds (where X is the offset seconds determined)
    return (double) dbDate.getOffset().compareTo(userDisplayZoneDate.getOffset()) / 24 / 60 / 60;
  }

  @Override
  public XMLGregorianCalendar getXMLGregorianCalendarNow() {
    try {
      GregorianCalendar gregorianCalendar = new GregorianCalendar();
      DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
      return datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
    }
    catch (DatatypeConfigurationException e) {
      return null;
    }
  }

  @Override
  public XMLGregorianCalendar getXMLGregorianCalendarFromDateOrElseToday(LocalDate date) {
    try {
      val localDateTime = LocalDateTime.of(date, LocalTime.MIDNIGHT);
      val instant = localDateTime.toInstant(ZoneOffset.UTC); //since there is no time, always use UTC.
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(instant.toString());
    }
    catch (DatatypeConfigurationException e) {
      return getXMLGregorianCalendarNow();
    }
  }

  @Override
  public LocalDate getLastDateOfThisMonth() {
    val localDate = LocalDate.now();
    return localDate.withDayOfMonth(localDate.lengthOfMonth());
  }

  @Override
  public boolean isValidDate(String date) {
    SimpleDateFormat sdf = new SimpleDateFormat(DateConstants.DEFAULT_INPUT_DATE_FORMAT);
    sdf.setLenient(false);
    try {
      val d = sdf.parse(date);
      d.getTime();
    }
    catch (ParseException e) {
      return false;
    }
    return true;
  }

  @Override
  public OffsetDateTime getOffsetDateTimeFromResultSet(ResultSet rs, String property) throws SQLException {
    java.sql.Timestamp ts = rs.getTimestamp(property);
    return toOffsetDateTime(ts.toInstant());
  }

  private OffsetDateTime toOffsetDateTime(Instant input) {
    return input != null ? input.atOffset(ZoneOffset.UTC) : null;
  }

  @Override
  public ZonedDateTime shiftTimezoneToDisplayZone(OffsetDateTime offsetDate) {
    val userSelectedDatabase = UserSiteSelectionTracker.getUserSiteSelection();
    val siteInfo = staticSiteInfos.getSiteInfo(userSelectedDatabase);

    val instant = offsetDate.toInstant();
    return instant.atZone(ZoneId.of(siteInfo.getSiteTimezone()));
  }

  @Override
  public String shiftAndFormatDateForTag(OffsetDateTime offsetDateTime, boolean showTime) {
    val shiftedDate = shiftTimezoneToDisplayZone(offsetDateTime);
    return formatDate(shiftedDate, showTime);
  }

  @Override
  public String shiftAndFormatDate(OffsetDateTime offsetDateTime, String pattern) {
    val shiftedDate = shiftTimezoneToDisplayZone(offsetDateTime);
    return formatDate(shiftedDate, pattern);
  }

  @Override
  public String formatDate(LocalDate localDate, String pattern) {
    val formatter = DateTimeFormatter.ofPattern(pattern);
    return localDate.format(formatter);
  }

  @Override
  public String formatDate(ZonedDateTime zonedDate, boolean showTime) {
    val pattern = showTime ? DateConstants.SITE_DATE_WITH_TIME_FORMATTER_PATTERN : DateConstants.SITE_DATE_FORMATTER_PATTERN;
    return formatDate(zonedDate, pattern);
  }

  @Override
  public String formatDate(ZonedDateTime zonedDate, String pattern) {
    val formatter = DateTimeFormatter.ofPattern(pattern);
    return zonedDate.format(formatter);
  }

  @Override
  public String formatDateForTag(OffsetDateTime offsetDateTime, boolean showTime) {
    val pattern = showTime ? DateConstants.SITE_DATE_WITH_TIME_FORMATTER_PATTERN : DateConstants.SITE_DATE_FORMATTER_PATTERN;

    //the non shifted dates are for tooltips to show the date directly from the db.
    //we want the tooltips to show the timezone they are in since the db may be different from the display
    //adding ' xxxxx' to the pattern to display will add in the timezone to the display in -04:00 format.
    val formatter = DateTimeFormatter.ofPattern(pattern + " xxxxx");
    return offsetDateTime.format(formatter);
  }

  @Override
  public LocalDate convertLocalDateStringToLocalDate(String date, String pattern) {
    val userSelectedDatabase = UserSiteSelectionTracker.getUserSiteSelection();
    val siteInfo = staticSiteInfos.getSiteInfo(userSelectedDatabase);
    val siteTimezone = siteInfo.getSiteTimezone();

    //convert user input date to localDate
    val formatter = DateTimeFormatter.ofPattern(pattern);
    return LocalDate.parse(date, formatter);
  }

  @Override
  public OffsetDateTime convertLocalDateTimeStringToDbTimezoneOffsetDateTime(String date, String pattern) {
    val formatter = DateTimeFormatter.ofPattern(pattern);

    //convert user input date to localDateTime
    val localDateTime = LocalDateTime.parse(date, formatter);
    //convert localDateTime to zonedDate at the sites zone.

    return convertLocalDateTimeToDbTimezoneOffsetDateTime(localDateTime);
  }

  @Override
  public OffsetDateTime convertLocalDateTimeToDbTimezoneOffsetDateTime(LocalDateTime localDateTime) {
    val userSelectedDatabase = UserSiteSelectionTracker.getUserSiteSelection();
    val siteInfo = staticSiteInfos.getSiteInfo(userSelectedDatabase);
    val siteTimezone = siteInfo.getSiteTimezone();

    //Zone ID of Site_Info
    val zoneId = ZoneId.of(siteTimezone);

    val zonedDateTime = localDateTime.atZone(zoneId);
    //convert zonedDateTime to OffsetDateTime, and shift to system default (JVM/DB timezone)
    Instant instant = Instant.now(); //can be LocalDateTime
    ZoneId systemZone = ZoneId.systemDefault(); // my timezone
    ZoneOffset currentOffsetForMyZone = systemZone.getRules().getOffset(instant);
    return zonedDateTime.toOffsetDateTime().withOffsetSameInstant(currentOffsetForMyZone);
  }

  @Override
  public String formatForAdfDisplay(String date, String patternToConvertFrom, String patternToDisplayIn, boolean shiftZone, boolean displayTimeZone) {
    if (StringUtils.isEmpty(date)) return null;
    String dateString = date.replaceAll("  +", " "); //this will strip anywhere that there are extra spaces

    //check for date only (ADF strips 00:00:00 off of their date objects, even though if you try and format their date it then displays the 00:00:00
    // the date object itself is missing the time)
    if (dateString.length() == 10) {
      //this means the date is only the date, so lets add the 00:00:00 to it, so our formatter and time shift works.
      dateString += " 00:00:00";
    }

    val formatter = DateTimeFormatter.ofPattern(patternToConvertFrom);
    //convert user input date to localDateTime
    ZonedDateTime zonedDateTime;

    if (patternToConvertFrom == DateConstants.ADF_REPORT_DATE_TIME_WITH_TIMEZONE) {
      zonedDateTime = ZonedDateTime.parse(dateString, formatter);
    }
    else {
      val localDateTime = LocalDateTime.parse(dateString, formatter);
      //convert localDateTime to zonedDate at the sites zone.
      zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
    }

    return formatForAdfDisplay(zonedDateTime, patternToDisplayIn, shiftZone, displayTimeZone);
  }

  private String formatForAdfDisplay(ZonedDateTime date, String patternToDisplayIn, boolean shiftDate, boolean displayTimeZone) {
    //adding ' xxxxx' to the pattern to display will add in the timezone to the display in -04:00 format.  This mostly false.
    //tooltips will add this.
    if (displayTimeZone) patternToDisplayIn += " xxxxx";

    if (shiftDate) {
      //shift to display timezone
      val userSelectedDatabase = UserSiteSelectionTracker.getUserSiteSelection();
      val siteInfo = staticSiteInfos.getSiteInfo(userSelectedDatabase);
      val siteTimezone = siteInfo.getSiteTimezone();

      val shiftedZonedDateTime = date.withZoneSameInstant(ZoneId.of(siteTimezone));

      return formatDate(shiftedZonedDateTime, patternToDisplayIn);
    }
    else {
      return formatDate(date, patternToDisplayIn);
    }
  }
}
