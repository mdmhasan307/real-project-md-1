package mil.usmc.mls2.stratis.core.service.gcss;

import mil.usmc.mls2.integration.migs.gcss.i112.outbound.message.MatsNotification;

public interface I112MatsService {

  void handleMatsNotification(MatsNotification matsNotification);
}