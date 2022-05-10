package mil.usmc.mls2.stratis.integration.mls2.core.infrastructure;

import mil.usmc.mls2.integration.common.Mls2Message;

public interface Mls2NotificationProcessor {

    void processMigsNotification(Mls2Message message);
}