package mil.usmc.mls2.stratis.core.processor;

import mil.usmc.mls2.stratis.core.domain.model.Location;
import mil.usmc.mls2.stratis.core.domain.model.Receipt;

public interface ReceiptProcessor {

  public Receipt copyAndUpdateToD6AReceipt(Receipt d6tReceipt, Location location);

}
