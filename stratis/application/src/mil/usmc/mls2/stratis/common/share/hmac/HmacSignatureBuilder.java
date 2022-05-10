package mil.usmc.mls2.stratis.common.share.hmac;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.val;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@Data
@Accessors(fluent = true, chain = true)
public class HmacSignatureBuilder {

  public static final String HMAC_SHA_512 = "HmacSHA512";

  private static final byte DELIMITER = '\n';
  private String algorithm = HMAC_SHA_512;
  private String scheme;
  private String host;
  private String method;
  private String resource;
  private String nonce;
  private String apiKey;
  private byte[] apiSecret;
  // private byte[] payload;
  private String date;
  private String contentType;

  public HmacSignatureBuilder apiSecret(String secretString) {
    this.apiSecret = secretString.getBytes(StandardCharsets.UTF_8);
    return this;
  }

  public byte[] build() {
    Objects.requireNonNull(algorithm, "algorithm");
    Objects.requireNonNull(scheme, "scheme");
    Objects.requireNonNull(host, "host");
    Objects.requireNonNull(method, "method");
    Objects.requireNonNull(resource, "resource");
    Objects.requireNonNull(contentType, "contentType");
    Objects.requireNonNull(apiKey, "apiKey");
    Objects.requireNonNull(date, "date");

    try {
      val digest = Mac.getInstance(algorithm);
      val secretKey = new SecretKeySpec(apiSecret, algorithm);
      digest.init(secretKey);
      digest.update(method.getBytes(StandardCharsets.UTF_8));
      digest.update(DELIMITER);
      digest.update(scheme.getBytes(StandardCharsets.UTF_8));
      digest.update(DELIMITER);
      digest.update(host.getBytes(StandardCharsets.UTF_8));
      digest.update(DELIMITER);
      digest.update(resource.getBytes(StandardCharsets.UTF_8));
      digest.update(DELIMITER);
      digest.update(contentType.getBytes(StandardCharsets.UTF_8));
      digest.update(DELIMITER);
      digest.update(apiKey.getBytes(StandardCharsets.UTF_8));
      digest.update(DELIMITER);
      if (nonce != null) {
        digest.update(nonce.getBytes(StandardCharsets.UTF_8));
      }
      digest.update(DELIMITER);
      digest.update(date.getBytes(StandardCharsets.UTF_8));
      //      digest.update(DELIMITER);
      //      digest.update(payload);
      digest.update(DELIMITER);
      val signatureBytes = digest.doFinal();
      digest.reset();
      return signatureBytes;
    }
    catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new RuntimeException("Can't create signature: " + e.getMessage(), e);
    }
  }

  public boolean isHashEquals(byte[] expectedSignature) {
    val signature = build();
    return MessageDigest.isEqual(signature, expectedSignature);
  }

  public String buildAsHexString() {
    return DatatypeConverter.printHexBinary(build());
  }

  public String buildAsBase64String() {
    return DatatypeConverter.printBase64Binary(build());
  }
}
