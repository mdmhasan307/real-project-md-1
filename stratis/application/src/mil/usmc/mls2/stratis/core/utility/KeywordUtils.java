package mil.usmc.mls2.stratis.core.utility;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings({"unused", "WeakerAccess"})
public class KeywordUtils {

  public static boolean matchesKeyword(CharSequence sequence, String keyword) {
    return matchesKeyword(sequence, keyword, false);
  }

  public static boolean matchesIgnoreCaseKeyword(CharSequence sequence, String keyword) {
    return matchesKeyword(sequence, keyword, true);
  }

  private static boolean matchesKeyword(CharSequence sequence, String keyword, boolean ignoreCase) {
    if (StringUtils.isBlank(keyword)) return false;
    val value = StringUtils.stripStart(StringUtils.stripEnd(keyword, "*"), "*").toLowerCase();

    if (keyword.startsWith("*") && keyword.endsWith("*")) {
      return ignoreCase ? StringUtils.containsIgnoreCase(sequence, value) : StringUtils.contains(sequence, value);
    }
    else if (keyword.startsWith("*")) {
      return ignoreCase ? StringUtils.endsWithIgnoreCase(sequence, value) : StringUtils.endsWith(sequence, value);
    }
    else if (keyword.endsWith("*")) {
      return ignoreCase ? StringUtils.startsWithIgnoreCase(sequence, value) : StringUtils.startsWith(sequence, value);
    }
    else {
      return ignoreCase ? StringUtils.equalsIgnoreCase(sequence, value) : StringUtils.equals(sequence, value);
    }
  }
}