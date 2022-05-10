package mil.stratis;

import lombok.val;
import mil.usmc.mls2.stratis.core.utility.DateConstants;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class DateServiceTest {

  public static void main(String... args) {
    //Proof of logic in DateService
    //    testDateOnly();
    //    testDateAndTime();
    //testWackyFormat();
    //testDateTimeFormatWithZone();
    test();
  }

  private static void test() {
    val displayDate = OffsetDateTime.now();

    System.out.println("displayDate Offset: " + displayDate.getOffset());

    val zoneId = ZoneId.of("UTC");

    val db = displayDate.toInstant().atZone(zoneId);
    System.out.println("shifted offset: " + db.getOffset());
    System.out.println("shifted offset Id: " + db.getOffset().getId());

    System.out.println("offset then db: " + displayDate.getOffset().compareTo(db.getOffset()) / 60 / 60);

    System.out.println("db then offset: " + db.getOffset().compareTo(displayDate.getOffset()) / 60 / 60);

    //    val instant = offset.toInstant();
    //    val zonedInstant = instant.atZone(ZoneId.of("UTC"));
    //
    //    System.out.println("instant: " + instant);
    //    System.out.println("Zoned Instant: " + zonedInstant);
  }

  private static void testDateTimeFormatWithZone() {
    System.out.println("date time with zone");
    val dateString = "2020-08-14T07:21:00.583-04:00";
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSxxxxx");
    val localDateTime = LocalDateTime.parse(dateString, formatter);
    System.out.println("LocalDateTime: " + localDateTime);

    val l = ZonedDateTime.now();
    System.out.println(l.format(formatter));
  }

  private static void testWackyFormat() {
    System.out.println("wacky date time");
    val temp = "Thursday  13 Aug 2020, 12:19:24 (226)";
    val dateString = temp.replaceAll("  +", " ");
    val formatter = DateTimeFormatter.ofPattern("EEEE dd MMM yyyy, HH:mm:ss (D)");
    val localDateTime = LocalDateTime.parse(dateString, formatter);
    System.out.println("LocalDateTime: " + localDateTime);
  }

  private static void testDateOnly() {
    System.out.println("Test Date Only");
    val dateString = "01029999";
    System.out.println("initial Date: " + dateString);

    val siteTimezone = "America/Los_Angeles";

    val formatter = DateTimeFormatter.ofPattern(DateConstants.SITE_DATE_DDMMYYYY_INPUT_FORMATTER_PATTERN);
    val localDate = LocalDate.parse(dateString, formatter);

    System.out.println("LocalDate: " + localDate);
    val zoneId = ZoneId.of(siteTimezone);
    val zonedDate = localDate.atStartOfDay(zoneId);

    System.out.println("zonedDate: " + zonedDate);

    val t = zonedDate.toOffsetDateTime();
    Instant instant = Instant.now(); //can be LocalDateTime
    ZoneId systemZone = ZoneId.systemDefault(); // my timezone
    ZoneOffset currentOffsetForMyZone = systemZone.getRules().getOffset(instant);
    val offsetDate = t.withOffsetSameInstant(currentOffsetForMyZone);
    System.out.println("Final Shifted Time: " + offsetDate);

    System.out.println("final Local Date: " + offsetDate.toLocalDateTime());
    java.util.Date temp = Calendar.getInstance().getTime();

    val instant2 = offsetDate.toLocalDateTime().toInstant(currentOffsetForMyZone);

    val d = java.sql.Date.from(instant2);
    System.out.println("Date from instant: " + d);
  }

  private static void testDateAndTime() {
    System.out.println("Test Date And Time");
    val dateString = "2020-02-28";// 11:00:00";
    System.out.println("initial Date: " + dateString);

    val siteTimezone = "America/Los_Angeles";
    val zoneId = ZoneId.of(siteTimezone);

    val formatter = DateTimeFormatter.ofPattern(DateConstants.SITE_DATE_INPUT_FORMATTER_PATTERN);
    val localDate = LocalDateTime.parse(dateString, formatter);

    val zonedDate = localDate.atZone(zoneId);
    System.out.println("zonedDate: " + zonedDate);

    val t = zonedDate.toOffsetDateTime();
    System.out.println("offsetDate: " + t);

    Instant instant = Instant.now(); //can be LocalDateTime
    ZoneId systemZone = ZoneId.systemDefault(); // my timezone
    ZoneOffset currentOffsetForMyZone = systemZone.getRules().getOffset(instant);

    System.out.println("Final Shifted Time: " + t.withOffsetSameInstant(currentOffsetForMyZone));
  }
}
