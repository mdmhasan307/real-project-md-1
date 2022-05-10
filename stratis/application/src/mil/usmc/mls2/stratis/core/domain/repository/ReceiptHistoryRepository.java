package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.ReceiptHistory;
import mil.usmc.mls2.stratis.core.domain.model.ReceiptHistorySearchCriteria;

import java.util.List;

public interface ReceiptHistoryRepository {
    List<ReceiptHistory> search(ReceiptHistorySearchCriteria criteria);

    Long count(ReceiptHistorySearchCriteria criteria);
}
