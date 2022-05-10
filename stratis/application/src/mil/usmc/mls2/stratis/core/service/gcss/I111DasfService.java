package mil.usmc.mls2.stratis.core.service.gcss;

import mil.usmc.mls2.integration.migs.gcss.i111.outbound.message.DasfNotification;

public interface I111DasfService {

  void handleDasfNotification(DasfNotification dasfNotification);
}