package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.Receipt;
import mil.usmc.mls2.stratis.core.domain.model.ReceiptSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface ReceiptRepository {

  Optional<Receipt> findById(Integer id);

  List<Receipt> search(ReceiptSearchCriteria criteria);

  Long count(ReceiptSearchCriteria criteria);

  void save(Receipt receipt);
}
