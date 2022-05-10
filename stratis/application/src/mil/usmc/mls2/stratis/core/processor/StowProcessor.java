package mil.usmc.mls2.stratis.core.processor;

import mil.stratis.view.user.UserInfo;
import mil.usmc.mls2.stratis.core.domain.model.Error;
import mil.usmc.mls2.stratis.core.domain.model.ErrorQueueCriteria;
import mil.usmc.mls2.stratis.core.domain.model.Receipt;

public interface StowProcessor {

  void createStowForReceipt(Receipt receipt, int qtyToStow, int userId, String expDate, String manufactureDate, String serialNumber);

  void deleteErrorRecordsForReceipt(ErrorQueueCriteria errSearch);

  void deleteStowsIfExistsForReceipt(Receipt receipt);

  void saveErrorForReceipt(Error error, Receipt receipt, UserInfo user);
}
