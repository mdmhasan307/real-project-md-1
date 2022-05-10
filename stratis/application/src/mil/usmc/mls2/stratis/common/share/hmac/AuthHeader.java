package mil.usmc.mls2.stratis.common.share.hmac;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Represents contents for HMAC HTTP <code>Authorization</code> header.
 */
@Value
@Builder
@Accessors(fluent = true)
public class AuthHeader {

  String algorithm;
  String apiKey;
  String nonce;
  byte[] digest;
}
