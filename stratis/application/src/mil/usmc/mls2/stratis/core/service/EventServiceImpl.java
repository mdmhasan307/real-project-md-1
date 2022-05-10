package mil.usmc.mls2.stratis.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mil.usmc.mls2.stratis.core.domain.model.Message;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Propagation.SUPPORTS;

/*
Used to allow ADF Backing objects to have access to the ApplicationEventPublisher.
 */
@Slf4j
@Service("eventService")
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional(propagation = SUPPORTS)
  public void publishEvent(Message message) { eventPublisher.publishEvent(message);}
}
