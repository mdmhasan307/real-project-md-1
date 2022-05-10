package mil.stratis.common.util.crypto;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unused")
public class CryptoPassUtils {

  private CryptoPassUtils() {}

  private static final String PASS_DATA_SOURCE = "kj2h34h";
  @Getter
  private static final String ALGORITHM = "PBEWithMD5AndDES";

  public static String getEncryptionPassword() {
    return obscure(System.getProperty("mls2.stratus.propertyPass", PASS_DATA_SOURCE));
  }

  private static String obscure(String plainText) {
    if (plainText == null) {
      return null;
    }

    StringBuilder obscuredMessage = new StringBuilder();
    for (int i = 0; i < plainText.length(); i++) {
      char c = plainText.charAt(i);
      if (c >= 'a' && c <= 'm') {
        c += 13;
      }
      else if (c >= 'A' && c <= 'M') {
        c += 13;
      }
      else if (c >= 'n' && c <= 'z') {
        c -= 13;
      }
      else if (c >= 'N' && c <= 'Z') {
        c -= 13;
      }
      obscuredMessage.append(c);
    }

    return obscuredMessage.toString();
  }
}
