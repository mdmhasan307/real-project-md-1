package mil.stratis.common.util.crypto;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;

/**
 * Utility class to simplify the encryption/decryption of passwords using jasypt
 */
public class CustomEncryptor {

  private static final PooledPBEStringEncryptor ENCRYPTOR;

  // Configure jasypt
  static {
    ENCRYPTOR = new PooledPBEStringEncryptor();
    ENCRYPTOR.setPassword(CryptoPassUtils.getEncryptionPassword());
    ENCRYPTOR.setAlgorithm(CryptoPassUtils.getALGORITHM());
    ENCRYPTOR.setPoolSize(3);
  }

  // Private constructor to prevent instantiation
  private CustomEncryptor() {}


  /**
   * Wrapper around jasypt's {@link PooledPBEStringEncryptor#decrypt(String)} method
   *
   * @param cipherText value to be decrypted
   * @return the clear text String
   */
  public static String decrypt(String cipherText) {

    return ENCRYPTOR.decrypt(cipherText);
  }

  /**
   * Wrapper around jasypt's {@link PooledPBEStringEncryptor#encrypt(String)} method
   *
   * @param clearText value to be encrypted
   * @return the ciphertext
   */
  public static String encrypt(String clearText) {

    return ENCRYPTOR.encrypt(clearText);
  }

}

