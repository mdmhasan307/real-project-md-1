package mil.usmc.mls2.stratis.core.domain.repository;

import mil.usmc.mls2.stratis.core.domain.model.Shipping;
import mil.usmc.mls2.stratis.core.domain.model.ShippingSearchCriteria;

import java.util.List;

public interface ShippingRepository {
    List<Shipping> search(ShippingSearchCriteria criteria);

    Long count(ShippingSearchCriteria criteria);
}
