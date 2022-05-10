package mil.stratis.common.util;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * This is a utility class.
 */
@Slf4j
public final class ZipUtils {

  /**
   * Private Constructor.
   */
  private ZipUtils() {}

  /**
   * Copy input stream data to the output stream.
   *
   * @param in  - input stream data
   * @param out - output stream
   */
  private static void copyInputStream(
      InputStream in,
      OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    int len;

    while ((len = in.read(buffer)) >= 0)
      out.write(buffer, 0, len);

    in.close();
    out.close();
  }

  /**
   * This function takes a given filename and unzips its contents.
   *
   * @param fileName   - the file to unzip
   * @param outputPath - the path to unzip the files to
   * @return String formatted list of the files unzipped, pipe delimited
   */
  public static String unzipFile(final String fileName, String outputPath) {
    StringBuilder outputName = new StringBuilder();

    if (!outputPath.endsWith("\\") && !outputPath.endsWith("/") && !outputPath.endsWith("//"))
      outputPath += "/";

    try (ZipFile zipFile = new ZipFile(fileName)) {
      val entries = zipFile.entries();
      while (entries.hasMoreElements()) {

        ZipEntry entry = entries.nextElement();
        if (!entry.isDirectory()) {
          outputName.append(entry.getName());
          int idx = outputName.toString().lastIndexOf('/');

          if (idx > -1) outputName = new StringBuilder(outputName.substring(idx + 1));

          File testFile = new File(outputPath + outputName);

          //Delete the previous unzipped file

          if (testFile.exists()) testFile.delete();
          try (InputStream in = zipFile.getInputStream(entry);
               BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outputPath + outputName))) {
            copyInputStream(in, out);
          }
        }
        if (entries.hasMoreElements()) outputName.append('|');
      }
      return outputName.toString();
    }
    catch (IOException e) {
      log.error("Error in {}", e.getStackTrace()[0].getMethodName(), e);
      return "ERROR - " + e.getMessage();
    }
    catch (Exception e) {
      log.error("Error in {}", e.getStackTrace()[0].getMethodName(), e);
    }
    return outputName.toString();
  }

  /**
   * Zips up a set of files to a specified file and file path
   *
   * @param filenames   - files to include in the zip
   * @param outFilename - the name of the zipped file to create
   * @param filePath    - the path of the zipped file
   * @return true or false, true means a successful zip
   */
  public static boolean zipFiles(String[] filenames,
                                 final String outFilename,
                                 final String filePath) {
    final String zipFilePath = filePath + outFilename;
    boolean result = false;

    //remove the zip file if it exists
    try {
      File testFile = new File(zipFilePath);
      if (testFile.exists()) testFile.delete();
    }
    catch (Exception e) {
      log.error("Error in {}", e.getStackTrace()[0].getMethodName(), e);
    }

    // Create a buffer for reading the files
    try (FileOutputStream fos = new FileOutputStream(zipFilePath);
         ZipOutputStream out = new ZipOutputStream(fos)) {
      byte[] buf = new byte[1024];

      // Compress the files
      for (final String filename : filenames) {
        try (FileInputStream in = new FileInputStream(filePath + filename)) {
          // Add ZIP entry to output stream.
          ZipEntry z = new ZipEntry(filename);
          out.putNextEntry(z);

          // Transfer bytes from the file to the ZIP file
          int len;
          while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
          }

          // Complete the entry
          out.closeEntry();
        }
      }
      result = true;
    }
    catch (Exception e) {
      log.error("Error in {}", e.getStackTrace()[0].getMethodName(), e);
    }
    return result;
  }

  /**
   * Override toString method.
   *
   * @return String
   */
  public String toString() {
    return "All classes may override non-final methods.";
  }
}
