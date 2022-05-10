package mil.stratis.common.util;

import mil.stratis.common.util.crypto.CustomEncryptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CustomEncryptorTest {

    @Test
    @DisplayName("Encrypt/decrypt basic use case")
    void cryptoBasic() {
        // Given a clear text string has been encrypted
        String expected = "TEST";
        String cipherText = CustomEncryptor.encrypt(expected);
        // When decrypting
        String actual = CustomEncryptor.decrypt(cipherText);
        // Then the same clear text string should be returned
        assertThat(actual).as("Check decrypted text matches").isEqualTo(expected);
    }

    @Test
    @DisplayName("Encrypt/decrypt multiple times")
    void cryptoMultiples() {
        // Given several clear text strings have been encrypted
        String expected1 = "TEST1";
        String cipherText1 = CustomEncryptor.encrypt(expected1);
        String expected2 = "TEST2";
        String cipherText2 = CustomEncryptor.encrypt(expected2);
        String expected3 = "TEST3";
        String cipherText3 = CustomEncryptor.encrypt(expected3);
        // When decrypting all values
        String actual1 = CustomEncryptor.decrypt(cipherText1);
        String actual2 = CustomEncryptor.decrypt(cipherText2);
        String actual3 = CustomEncryptor.decrypt(cipherText3);
        // Then the same clear text string should be returned
        assertThat(actual1).as("Check decrypted text matches").isEqualTo(expected1);
        assertThat(actual2).as("Check decrypted text matches").isEqualTo(expected2);
        assertThat(actual3).as("Check decrypted text matches").isEqualTo(expected3);
    }

    @Test
    @DisplayName("Encrypt twice on the same value")
    void encryptSameValue() {
        // Given a clear text string has been encrypted
        String clearText = "TEST";
        String expected = CustomEncryptor.encrypt(clearText);
        // When encrypting again
        String actual = CustomEncryptor.encrypt(clearText);
        // Then the cipher texts should not match
        assertThat(actual).as("Check that cipher values change").isNotEqualTo(expected);
    }

}
