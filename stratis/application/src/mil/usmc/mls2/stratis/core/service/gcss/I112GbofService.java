package mil.usmc.mls2.stratis.core.service.gcss;

import mil.usmc.mls2.integration.migs.gcss.i112.outbound.message.GbofNotification;

public interface I112GbofService {

  void handleGbofNotification(GbofNotification gbofNotification);
}