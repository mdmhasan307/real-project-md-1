package mil.stratis;

import lombok.val;
import mil.stratis.common.util.crypto.CustomEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

public class CryptoPassUtilsTest {

  /**
   * Jasypt Encryption / Decryption test
   */
  public static void main(String[] args) {
    String text;
    boolean encrypt;

    // Examples:
    //    args = new String[]{
    //        "enc", "stratis",
    //        "enc", "F!S3mp3rF!S3mp3r",
    //        "enc", "system",
    //        "enc", "0raDB!!S3mp3rF1",
    //    };

    val errorMessage = "Must supply 2 arguments:  1) 'enc' or 'dec',  2) string to process";
    if (args == null) {
      throw new NullPointerException(errorMessage);
    }
    else if (args.length % 2 != 0) {
      throw new IllegalStateException(errorMessage);
    }

    for (int i = 0; i < args.length; ) {
      encrypt = args[i++].equalsIgnoreCase("enc");
      text = args[i++];

      System.out.println("\n");

      if (encrypt) {
        val encryptedText = CustomEncryptor.encrypt(text);
        val decryptedText = CustomEncryptor.decrypt(encryptedText);
        System.err.format("encrypt [%s]: %s\n", text, encryptedText);
      }
      else {
        try {
          val decryptedText = CustomEncryptor.decrypt(text);
        }
        catch (EncryptionOperationNotPossibleException e) {
          System.err.format("DECRYPTION NOT POSSIBLE FOR CYPHER TEXT [%s]!  Please check cypher text.\n", text);
        }
      }
    }

    System.out.println("\n");
  }
}
