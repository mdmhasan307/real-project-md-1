package mil.usmc.mls2.stratis.core.service.gcss;

import mil.usmc.mls2.integration.migs.gcss.i033.outbound.message.GabfNotification;

public interface I033GabfService {

  void handleGabfNotification(GabfNotification gabfNotification);
}