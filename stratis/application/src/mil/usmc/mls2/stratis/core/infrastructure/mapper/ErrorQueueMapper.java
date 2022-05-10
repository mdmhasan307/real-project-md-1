package mil.usmc.mls2.stratis.core.infrastructure.mapper;

import mil.usmc.mls2.stratis.core.domain.model.ErrorQueue;
import mil.usmc.mls2.stratis.core.infrastructure.persistence.stratis.model.ErrorQueueEntity;
import org.springframework.stereotype.Component;

@Component
public class ErrorQueueMapper {

  public ErrorQueue map(ErrorQueueEntity input) {
    if (input == null) return null;

    return ErrorQueue.builder()
        .errorQueueId(input.getErrorQueueId())
        .status(input.getStatus())
        .createdBy(input.getCreatedBy())
        .createdDate(input.getCreatedDate())
        .eid(input.getEid())
        .keyType(input.getKeyType())
        .keyNum(input.getKeyNum())
        .modifiedBy(input.getModifiedBy())
        .modifiedDate(input.getModifiedDate())
        .notes(input.getNotes()).build();
  }
}
