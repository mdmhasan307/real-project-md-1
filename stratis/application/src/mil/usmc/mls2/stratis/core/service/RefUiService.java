package mil.usmc.mls2.stratis.core.service;

import mil.usmc.mls2.stratis.core.domain.model.Receipt;
import mil.usmc.mls2.stratis.core.domain.model.RefUi;
import mil.usmc.mls2.stratis.core.domain.model.RefUiSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface RefUiService {

    Optional<RefUi> findById(Integer id);

    Long count(RefUiSearchCriteria criteria);

    List<RefUi> search(RefUiSearchCriteria criteria);

    RefUi findSingleMatch(RefUiSearchCriteria criteria);

    Boolean convertReceiptUI(String to, String from, Receipt receipt);
}