package mil.usmc.mls2.stratis.core.infrastructure.flyway;

import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Publishes database development scripts into the project's main/resources/db/migration folder
 * USAGE:
 * create ${project_dir}\config\local\db_migration
 * create draft.txt inside folder
 * <p>
 * place your database scripts in the db_migration folder
 * define the script order in draft.txt
 * run FlywayScriptPublisher.main(...) to automatically name, version and copy the files into the appropriate db/migration/x folder.
 */
@Slf4j
public class FlywayScriptPublisher {

  private static final String DIR_SUB = "%s/%s";
  private static final String DATE_TIME_FORMAT_STRING = "yyyy.MM.dd.HHmm";
  private static final String APP_NAME = "application";

  public static void main(String[] args) {
    processFiles();
  }

  private static void processFiles() {
    val properties = new Properties();
    val buildProperties = "build.properties";
    try (val input = FlywayScriptPublisher.class.getClassLoader().getResourceAsStream(buildProperties)) {
      if (input == null) {
        log.info("Unable to load {}", buildProperties);
        return;
      }

      //load a properties file from class path, inside static method
      properties.load(input);
      processDBMigration("STRATCOMMON", properties);
      processDBMigration("STRATIS", properties);
    }
    catch (IOException ex) {
      log.error("Failed to process files", ex);
    }
  }

  private static void processDBMigration(String db, Properties properties) throws IOException {
    // process
    val userDir = System.getProperty("user.dir");
    val userHome = System.getProperty("user.home");
    log.info("user.home: {}", userHome);
    log.info("user.dir: {}", userDir);

    val draftDirectory = new File(FilenameUtils.separatorsToSystem(userDir + "/config/local/db_migration/" + db));  // NOSONAR Usage of user-specified directory is intentional.  Developer-only class.
    if (draftDirectory.exists()) {
      // process draft file
      val draftFile = new File(FilenameUtils.separatorsToSystem(draftDirectory.getPath() + "/draft.txt")); // NOSONAR Usage of user-specified directory is intentional.  Developer-only class.
      if (draftFile.exists()) {
        backupDraftFiles(draftDirectory);
        // process draft file
        val files = draftDirectory.listFiles();
        if (files != null) {
          // all db script dates should be based off UTC
          val destinationDirectory = createDestinationDirectory(properties, db);
          val fileMap = Arrays.stream(files).collect(Collectors.toMap(File::getName, file -> file, (a, b) -> b, HashMap::new));
          val draftFileLines = Files.readAllLines(draftFile.toPath(), Charsets.UTF_8);
          val fileNumber = new MutableInt(0);

          draftFileLines
              .stream()
              .map(String::trim)
              .filter(d -> !StringUtils.startsWith(d, "--"))
              .filter(d -> !StringUtils.isBlank(d))
              .forEach(d -> processDraftFile(d, fileNumber.incrementAndGet(), destinationDirectory, fileMap, properties));

          log.info("support files found: {}", fileNumber.getValue());

          clearFileContents(draftFile);
        }
      }
    }
  }

  private static File createDestinationDirectory(Properties properties, String db) throws IOException {
    val fullReleaseVersionWithBuild = fullReleaseVersionWithBuild(properties);
    val fullReleaseVersionWithoutBuild = fullReleaseVersionWithoutBuild(properties);
    val revisionReleaseVersion = StringUtils.substringBeforeLast(fullReleaseVersionWithoutBuild, ".");
    val userDir = System.getProperty("user.dir");
    val rootMigrationDirectoryPath = StringUtils.replace(userDir + '/' + APP_NAME + '/' + "resources/db/migration/" + db, "/", File.separator);

    log.info("fullReleaseVersionWithBuild:{}", fullReleaseVersionWithBuild);
    log.info("fullReleaseVersionWithoutBuild:{}", fullReleaseVersionWithoutBuild);
    log.info("revisionReleaseVersion:{}", revisionReleaseVersion);
    log.info("rootMigrationDirectoryPath:{}", rootMigrationDirectoryPath);

    val rootMigrationDirectory = new File(FilenameUtils.separatorsToSystem(rootMigrationDirectoryPath)); // NOSONAR Usage of user-specified directory is intentional.  Developer-only class.
    val subVersionDirectory = createDirectoryIfMissing(fullReleaseVersionWithoutBuild, rootMigrationDirectory);
    return createDirectoryIfMissing(fullReleaseVersionWithBuild, subVersionDirectory);
  }

  /**
   * SuppressWarnings: Usage of user-specified directory is intentional.  Developer-only class.
   */
  @SuppressWarnings("findsecbugs:PATH_TRAVERSAL_IN")
  private static File createDirectoryIfMissing(String directoryName, File parentDirectory) throws IOException {
    val directory = new File(String.format(DIR_SUB, parentDirectory.getPath(), directoryName));
    if (!directory.exists()) {
      log.info("creating directory '{}' (does not exist)", directory.getPath());
      FileUtils.forceMkdir(directory);
    }
    else {
      log.info("skipping directory '{}', already exists.", directory.getPath());
    }

    return directory;
  }

  private static void backupDraftFiles(File draftDirectory) throws IOException {
    // backup draft files to draft/backup
    File draftBackupDirectory = new File(FilenameUtils.separatorsToSystem(draftDirectory.getPath() + "/backup")); // NOSONAR Usage of user-specified directory is intentional.  Developer-only class.
    if (!draftBackupDirectory.exists()) {
      log.info("creating draft backup directory (does not exist)");
      FileUtils.forceMkdir(draftBackupDirectory);
    }

    val now = new DateTime();
    val newBackupDirectoryName = "" + now.getYear() + "-" + StringUtils.leftPad(String.valueOf(now.getMonthOfYear()), 2, '0') + "-" + StringUtils.leftPad(String.valueOf(now.getDayOfMonth()), 2, '0') + "-" + StringUtils.leftPad(String.valueOf(now.getHourOfDay()), 2, '0') + StringUtils.leftPad(String.valueOf(now.getMinuteOfHour()), 2, '0') + "-" + now.getMillis();
    val draftInstanceBackupDirectory = new File(FilenameUtils.separatorsToSystem(String.format(DIR_SUB, draftBackupDirectory.getPath(), newBackupDirectoryName))); // NOSONAR Usage of user-specified directory is intentional.  Developer-only class.

    // copy files to backup directory
    val copyFilter = FileFilterUtils.notFileFilter(DirectoryFileFilter.DIRECTORY);
    net.jawr.web.util.FileUtils.copyDirectory(draftDirectory, draftInstanceBackupDirectory, copyFilter);
  }

  private static String fullReleaseVersionWithBuild(Properties properties) {
    // removes any "-" builds (as in "-SNAPSHOT")
    return stripAlphaCharacters(StringUtils.substringBeforeLast(properties.getProperty("stratis.releaseVersion"), "-"));
  }

  private static String fullReleaseVersionWithoutBuild(Properties properties) {
    return stripAlphaCharacters(StringUtils.substringBeforeLast(fullReleaseVersionWithBuild(properties), "."));
  }

  private static String stripAlphaCharacters(String token) {
    val sb = new StringBuilder(token.length());
    token.chars().forEach(c -> sb.append(StringUtils.isAlpha(Character.toString((char) c)) ? StringUtils.EMPTY : (char) c));
    return sb.toString();
  }

  private static void processDraftFile(String draftFileEntry, int fileNumber, File destinationDirectory, Map<String, File> fileMap, Properties properties) {
    val utcTimeZone = DateTimeZone.forID("UTC");
    val nowUtc = new DateTime(utcTimeZone);
    val nowUtcFormatted = formatDateTime(nowUtc, utcTimeZone);
    val prefix = "V" + fullReleaseVersionWithBuild(properties);
    val file = fileMap.get(draftFileEntry);
    if (file == null) {
      throw new IllegalStateException(String.format("Flyway publisher failed, referenced file '%s' does not exist", draftFileEntry));
    }

    val filename = file.getName();
    val newFileName = String.format("%s_%s.%d__%s", prefix, nowUtcFormatted, fileNumber, filename);
    log.info("file '{}' renamed to '{}'", filename, newFileName);

    val newFile = new File(FilenameUtils.separatorsToSystem(String.format(DIR_SUB, destinationDirectory.getPath(), newFileName))); // NOSONAR Usage of user-specified directory is intentional.  Developer-only class.
    if (newFile.exists()) {
      throw new IllegalStateException("cannot rename file, already exists!");
    }
    val renamed = file.renameTo(newFile);
    if (!renamed) {
      throw new IllegalStateException("failed to rename file!");
    }
  }

  private static void clearFileContents(File file) throws IOException {
    // clear draft.txt contents
    try (val writer = new PrintWriter(file)) {
      writer.print("");
    }

    log.info("contents of '{}' have been cleared.", file.getName());
  }

  private static String formatDateTime(DateTime dateTime, DateTimeZone dateTimeZone) {
    if (dateTime == null) {
      return "";
    }

    final DateTime dateTimeToFormat;
    if (dateTimeZone != null) {
      dateTimeToFormat = dateTime.toDateTime(dateTimeZone);
    }
    else {
      dateTimeToFormat = dateTime;
    }

    return dateTimeToFormat.toString(DATE_TIME_FORMAT_STRING);
  }
}
