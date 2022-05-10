package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.Message;

/**
 * Publishes application events and messages
 */
public interface EventService {

  void publishEvent(Message message);
}
