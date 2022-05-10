package mil.usmc.mls2.stratis.integration.mls2.core.infrastructure.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(staticName = "of")
public class IncrementMls2InboundMessageReceivedCount implements Command {

  @NotNull(message = "message UUID must not be null")
  private UUID uuid;
}
