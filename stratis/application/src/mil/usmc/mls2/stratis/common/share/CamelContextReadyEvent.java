package mil.usmc.mls2.stratis.common.share;

import lombok.*;

import java.time.OffsetDateTime;

/**
 * Indicates that the camel context is now ready for use.
 */
@Value
public class CamelContextReadyEvent {

  OffsetDateTime timestamp = OffsetDateTime.now();
}
